package com.example.vulnscanner.module.fortify;

import com.example.vulnscanner.repository.ComplianceMappingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class FortifyService {

    private final ComplianceMappingRepository complianceMappingRepository;
    private final ObjectMapper objectMapper;

    private static final String DATA_DIR = "data/fortify";

    /**
     * ZIP 파일 업로드 및 압축 해제
     * 파일명 형식: fortify_data_YYYYMMDD.zip -> 20251226 폴더 생성
     */
    public void uploadData(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.endsWith(".zip")) {
            throw new IllegalArgumentException("ZIP 파일만 업로드 가능합니다.");
        }

        // 날짜 추출 (fortify_data_20251226.zip)
        String datePart = originalFilename.replace("fortify_data_", "").replace(".zip", "");
        if (!datePart.matches("\\d{8}")) {
            throw new IllegalArgumentException("파일명 형식이 올바르지 않습니다. (예: fortify_data_20251226.zip)");
        }

        Path targetDir = Paths.get(DATA_DIR, datePart);
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        try (InputStream is = file.getInputStream();
                ZipInputStream zis = new ZipInputStream(is, StandardCharsets.UTF_8)) {

            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                if (!zipEntry.isDirectory()) {
                    // 파일명만 추출 (경로 제외)
                    String fileName = new File(zipEntry.getName()).getName();
                    if (fileName.endsWith(".json")) {
                        Path newFile = targetDir.resolve(fileName);
                        Files.copy(zis, newFile, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
                zipEntry = zis.getNextEntry();
            }
        }
    }

    /**
     * 사용 가능한 날짜 목록 조회
     */
    public List<String> getAvailableDates() {
        File folder = new File(DATA_DIR);
        if (!folder.exists() || !folder.isDirectory()) {
            return Collections.emptyList();
        }

        File[] directories = folder.listFiles(File::isDirectory);
        if (directories == null) {
            return Collections.emptyList();
        }

        return Arrays.stream(directories)
                .map(File::getName)
                .filter(name -> name.matches("\\d{8}"))
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
    }

    /**
     * 특정 날짜의 사용 가능한 언어 목록 조회
     */
    public List<String> getAvailableLanguages(String date) {
        File folder = new File(Paths.get(DATA_DIR, date).toString());
        if (!folder.exists()) {
            return Collections.emptyList();
        }

        File[] files = folder.listFiles((dir, name) -> name.startsWith("fortify_") && name.endsWith(".json"));
        if (files == null) {
            return Collections.emptyList();
        }

        return Arrays.stream(files)
                .map(File::getName)
                .map(name -> name.replace("fortify_", "").replace(".json", ""))
                .map(name -> name.replace("_", "/").replace("sharp", "#")) // 파일명 복원
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * 특정 날짜/언어의 취약점 목록 조회
     */
    public List<WeaknessItem> getWeaknessesByLanguage(String date, String language) throws IOException {
        String safeName = language.replace("/", "_").replace("#", "sharp").replace(" ", "_");
        Path filePath = Paths.get(DATA_DIR, date, "fortify_" + safeName + ".json");

        if (!Files.exists(filePath)) {
            return Collections.emptyList();
        }

        FortifyWeaknessResponse response = objectMapper.readValue(filePath.toFile(), FortifyWeaknessResponse.class);
        List<WeaknessItem> items = response.getWeaknesses();

        // Compliance Mapping 확인
        checkComplianceMapping(items);

        return items;
    }

    /**
     * Compliance Mapping 여부 확인
     */
    private void checkComplianceMapping(List<WeaknessItem> items) {
        // 성능 최적화를 위해 한 번에 조회하거나, 캐싱할 수 있으나 현재는 개별 조회 혹은 전체 로딩 후 매칭
        // 여기서는 간단하게 반복문으로 처리 (데이터 양이 많지 않을 경우)
        // 실제로는 findAll()해서 메모리 매핑하는게 빠를 수 있음.

        // 모든 매핑 데이터를 가져와서 내부 카테고리(Fortify Category) 목록을 Set으로 만듬 (가정)
        // ComplianceMapping 엔티티의 internalCategory가 Fortify의 title과 매핑된다고 가정
        List<String> mappedCategories = complianceMappingRepository.findAllInternalCategories();
        Set<String> mappedSet = new HashSet<>(mappedCategories);

        for (WeaknessItem item : items) {
            item.setMapped(mappedSet.contains(item.getTitle()));
        }
    }

    /**
     * 두 버전 간 취약점 비교
     */
    public List<WeaknessItem> compareVersions(String date1, String date2, String language) throws IOException {
        List<WeaknessItem> list1 = getWeaknessesByLanguage(date1, language); // Old
        List<WeaknessItem> list2 = getWeaknessesByLanguage(date2, language); // New

        Map<String, WeaknessItem> map1 = list1.stream()
                .collect(Collectors.toMap(WeaknessItem::getId, item -> item, (a, b) -> a));
        Map<String, WeaknessItem> map2 = list2.stream()
                .collect(Collectors.toMap(WeaknessItem::getId, item -> item, (a, b) -> a));

        List<WeaknessItem> result = new ArrayList<>();

        // 1. New items in date2
        for (WeaknessItem item2 : list2) {
            if (!map1.containsKey(item2.getId())) {
                item2.setDiffStatus("NEW");
                result.add(item2);
            } else {
                // Check modified
                WeaknessItem item1 = map1.get(item2.getId());
                if (!Objects.equals(item1.getAbstractContent(), item2.getAbstractContent())) {
                    item2.setDiffStatus("MODIFIED");
                    result.add(item2);
                } else {
                    item2.setDiffStatus("SAME");
                    result.add(item2);
                }
            }
        }

        // 2. Removed items in date1
        for (WeaknessItem item1 : list1) {
            if (!map2.containsKey(item1.getId())) {
                item1.setDiffStatus("REMOVED");
                result.add(item1);
            }
        }

        // Sort by ID or Title
        result.sort(Comparator.comparing(WeaknessItem::getTitle));

        return result;
    }
}
