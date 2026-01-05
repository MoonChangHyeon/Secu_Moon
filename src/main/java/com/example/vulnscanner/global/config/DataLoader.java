package com.example.vulnscanner.global.config;

import com.example.vulnscanner.module.user.Privilege;
import com.example.vulnscanner.module.user.PrivilegeRepository;
import com.example.vulnscanner.module.user.RoleTemplate;
import com.example.vulnscanner.module.user.RoleTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final PrivilegeRepository privilegeRepository;
    private final RoleTemplateRepository roleTemplateRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 1. Seed Privileges
        createPrivilegeIfNotFound("READ_PRIVILEGE", "Read access");
        createPrivilegeIfNotFound("WRITE_PRIVILEGE", "Write access");
        createPrivilegeIfNotFound("DELETE_PRIVILEGE", "Delete access");
        createPrivilegeIfNotFound("ADMIN_PRIVILEGE", "Full admin access");

        // 2. Seed Default Admin Role Template
        createRoleTemplateIfNotFound("Admin", "Default Administrator Role",
                Arrays.asList("READ_PRIVILEGE", "WRITE_PRIVILEGE", "DELETE_PRIVILEGE", "ADMIN_PRIVILEGE"));

        // 3. Seed Default User Role Template
        createRoleTemplateIfNotFound("User", "Default User Role",
                Arrays.asList("READ_PRIVILEGE"));
    }

    @Transactional
    public Privilege createPrivilegeIfNotFound(String name, String description) {
        return privilegeRepository.findByName(name)
                .orElseGet(() -> privilegeRepository.save(new Privilege(name, description)));
    }

    @Transactional
    public RoleTemplate createRoleTemplateIfNotFound(String name, String description, List<String> privilegeNames) {
        Optional<RoleTemplate> roleTemplateOpt = roleTemplateRepository.findByName(name);
        if (roleTemplateOpt.isPresent()) {
            return roleTemplateOpt.get();
        }

        RoleTemplate roleTemplate = new RoleTemplate();
        roleTemplate.setName(name);
        roleTemplate.setDescription(description);

        List<Privilege> privileges = privilegeRepository.findAll();
        for (String pName : privilegeNames) {
            privileges.stream()
                    .filter(p -> p.getName().equals(pName))
                    .findFirst()
                    .ifPresent(p -> roleTemplate.getPrivileges().add(p));
        }

        return roleTemplateRepository.save(roleTemplate);
    }
}
