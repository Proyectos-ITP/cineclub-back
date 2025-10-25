package com.cineclub_backend.cineclub_backend.users.controllers;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cineclub_backend.cineclub_backend.shared.helpers.SerializeHelper;
import com.cineclub_backend.cineclub_backend.users.models.User;
import com.cineclub_backend.cineclub_backend.users.services.CrudUserService;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private final CrudUserService crudUserService;

    public UserController(CrudUserService crudUserService) {
        this.crudUserService = crudUserService;
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable String id) {
        return crudUserService.getUserById(id);
    }

    @GetMapping("/email/{email}")
    public User getUserByEmail(@PathVariable String email) {
        return crudUserService.getUserByEmail(email);
    }

    @PostMapping
    public void createUser(@RequestBody Map<String, Object> payload) {
        Object record = Optional.ofNullable(payload.get("record")).orElse(Map.of());
        User user = SerializeHelper.toObject(record, User.class);
        crudUserService.saveUser(user);
    }

    @PatchMapping("/{id}")
    public User updateUser(@PathVariable String id, @RequestBody User user) {
        return crudUserService.saveUser(user);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable String id) {
        crudUserService.deleteUser(id);
    }

    @GetMapping("/")
    public List<User> getAllUsers() {
        return crudUserService.getAllUsers();
    }
}
