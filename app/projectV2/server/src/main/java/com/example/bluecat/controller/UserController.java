package com.example.bluecat.controller;

import com.example.bluecat.dto.UserDTO;
import com.example.bluecat.entity.User;
import com.example.bluecat.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
        return ResponseEntity.ok(userService.register(user));
        } catch (RuntimeException e) {
            // 返回错误信息和400状态码
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
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