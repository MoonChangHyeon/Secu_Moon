package com.example.vulnscanner.module.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleTemplateService {

    private final RoleTemplateRepository roleTemplateRepository;
    private final PrivilegeRepository privilegeRepository;

    public List<RoleTemplate> getAllTemplates() {
        return roleTemplateRepository.findAll();
    }

    public List<Privilege> getAllPrivileges() {
        return privilegeRepository.findAll();
    }

    public RoleTemplate getTemplate(Long id) {
        return roleTemplateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Template not found"));
    }

    public RoleTemplate getTemplateByName(String name) {
        return roleTemplateRepository.findByName(name).orElse(null);
    }

    @Transactional
    public RoleTemplate createTemplate(String name, String description, List<Long> privilegeIds) {
        if (roleTemplateRepository.findByName(name).isPresent()) {
            throw new IllegalArgumentException("Template name already exists");
        }
        RoleTemplate template = new RoleTemplate();
        template.setName(name);
        template.setDescription(description);

        if (privilegeIds != null && !privilegeIds.isEmpty()) {
            List<Privilege> privileges = privilegeRepository.findAllById(privilegeIds);
            template.setPrivileges(privileges);
        }

        return roleTemplateRepository.save(template);
    }

    @Transactional
    public void updateTemplate(Long id, String name, String description, List<Long> privilegeIds) {
        RoleTemplate template = getTemplate(id);
        template.setName(name);
        template.setDescription(description);

        List<Privilege> privileges = privilegeRepository.findAllById(privilegeIds);
        template.setPrivileges(privileges);

        roleTemplateRepository.save(template);
    }

    @Transactional
    public void deleteTemplate(Long id) {
        roleTemplateRepository.deleteById(id);
    }
}
