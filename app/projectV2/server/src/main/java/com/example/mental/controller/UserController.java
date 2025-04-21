package com.example.mental.controller;

import com.example.mental.dto.UserDTO;
import com.example.mental.entity.User;
import com.example.mental.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@RequestBody User user) {
        return ResponseEntity.ok(userService.register(user));
    }

    @PostMapping("/login")
    public ResponseEntity<UserDTO> login(@RequestBody User user) {
        return ResponseEntity.ok(userService.login(user.getUsername(), user.getPassword()));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUserInfo(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserInfo(userId));
    }

    @PutMapping("/{userId}/field")
    public ResponseEntity<UserDTO> updateUserField(
            @PathVariable Long userId,
            @RequestBody Map<String, String> fieldData) {
        return ResponseEntity.ok(userService.updateUserField(userId, fieldData));
    }

    @PutMapping("/{userId}/password")
    public ResponseEntity<Void> updatePassword(
            @PathVariable Long userId,
            @RequestBody Map<String, String> passwordData) {
        userService.updatePassword(userId, passwordData.get("oldPassword"), passwordData.get("newPassword"));
        return ResponseEntity.ok().build();
    }
} 