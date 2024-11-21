package ru.itmentor.spring.boot_security.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmentor.spring.boot_security.demo.models.Role;
import ru.itmentor.spring.boot_security.demo.models.User;
import ru.itmentor.spring.boot_security.demo.services.RoleService;
import ru.itmentor.spring.boot_security.demo.services.UserService;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
public class AdminsController {

    private final UserService userService;
    private final RoleService roleService;

    @Autowired
    public AdminsController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping("/mainPage")
    public ResponseEntity<User> getAdminInfo(Principal principal) {
        String username = principal.getName();
        User admin = userService.getCurrentUser(username);
        return ResponseEntity.ok(admin);
    }

    @GetMapping("/users")
    public List<User> listUsers() {
        List<User> users = userService.findAll();
        List<User> filteredUsers = users.stream()
                .filter(user -> user.getAuthorities()
                        .stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")))
                .collect(Collectors.toList());
        return filteredUsers;
    }

    @PostMapping("/addUser")
    public ResponseEntity<Void> addUser(@RequestBody User user) {
        Role userRole = roleService.findByName("ROLE_USER");

        if (userRole == null) {
            userRole = new Role();
            userRole.setName("ROLE_USER");
            roleService.save(userRole);
        }

        if (user.getRoles() == null) {
            user.setRoles(new ArrayList<>());
        }

        user.getRoles().add(userRole);
        userService.saveUser(user);
        return ResponseEntity.status(201).build();
    }


    @GetMapping("/separateUser/{id}")
    public ResponseEntity<Optional<User>> getUserInfo(@PathVariable Long id) {
        try {
            Optional<User> user = userService.findById(id);
            if (user.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Возвращаем 404, если пользователь не найден
            }
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // Возвращаем 500 в случае ошибки
        }
    }

    @DeleteMapping("/deleteUser/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/updateUser/{id}")
    public ResponseEntity<Void> updateUser(@PathVariable Long id, @RequestBody User user) {
        Optional<User> existingUserOptional = userService.findById(id);

        if (existingUserOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        User existingUser = existingUserOptional.get();

        existingUser.setUsername(user.getUsername());
        existingUser.setPassword(user.getPassword());
        existingUser.setYearOfBirth(user.getYearOfBirth());


        Role userRole = roleService.findByName("ROLE_USER");
        if (userRole == null) {
            userRole = new Role();
            userRole.setName("ROLE_USER");
            roleService.save(userRole);
        }

        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            existingUser.setRoles(new ArrayList<>());
        } else {
            existingUser.setRoles(user.getRoles());
        }
        existingUser.getRoles().add(userRole);

        userService.saveUser(existingUser);

        return ResponseEntity.noContent().build();
    }

}