package ru.itmentor.spring.boot_security.demo.services;

import org.springframework.stereotype.Service;
import ru.itmentor.spring.boot_security.demo.models.Role;
import ru.itmentor.spring.boot_security.demo.repositories.RolesRepository;

@Service
public class RoleService {


    private final RolesRepository roleRepository;

    public RoleService(RolesRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Role findByName(String name) {
        return roleRepository.findByName(name);
    }

    public void save(Role role) {
        roleRepository.save(role);
    }
}
