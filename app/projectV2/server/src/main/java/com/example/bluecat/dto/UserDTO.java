package com.example.bluecat.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String token;
    private String mbtiType;
    private String grade;
    private String gender;
    private Integer age;
    private String bio;
    private String avatarUrl;
} 