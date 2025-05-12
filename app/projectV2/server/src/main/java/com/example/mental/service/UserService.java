package com.example.mental.service;

import com.example.mental.dto.UserDTO;
import com.example.mental.entity.User;
import java.util.Map;

public interface UserService {
    UserDTO register(User user);
    UserDTO login(String username, String password);
    UserDTO getUserInfo(Long userId);
    UserDTO updateUserField(Long userId, Map<String, String> fieldData);
    void updatePassword(Long userId, String oldPassword, String newPassword);
} 