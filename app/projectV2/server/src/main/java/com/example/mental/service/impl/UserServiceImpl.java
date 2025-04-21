package com.example.mental.service.impl;

import com.example.mental.dto.UserDTO;
import com.example.mental.entity.User;
import com.example.mental.repository.UserRepository;
import com.example.mental.security.JwtTokenUtil;
import com.example.mental.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;

    @Override
    @Transactional
    public UserDTO register(User user) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (user.getEmail() != null && userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }

        // 检查手机号是否已存在
        if (user.getPhone() != null && userRepository.existsByPhone(user.getPhone())) {
            throw new RuntimeException("手机号已被注册");
        }

        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 保存用户
        User savedUser = userRepository.save(user);

        // 生成token
        UserDetails userDetails = loadUserByUsername(savedUser.getUsername());
        String token = jwtTokenUtil.generateToken(userDetails);

        // 返回UserDTO
        return convertToDTO(savedUser, token);
    }

    @Override
    public UserDTO login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("密码错误");
        }

        UserDetails userDetails = loadUserByUsername(username);
        String token = jwtTokenUtil.generateToken(userDetails);

        return convertToDTO(user, token);
    }

    @Override
    public UserDTO getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));
        return convertToDTO(user, null);
    }

    @Override
    public UserDTO updateUserField(Long userId, Map<String, String> fieldData) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));

        String field = fieldData.keySet().iterator().next();
        String value = fieldData.get(field);

        switch (field) {
            case "username":
                // 检查新用户名是否已存在
                if (userRepository.existsByUsername(value)) {
                    throw new RuntimeException("用户名已存在");
                }
                user.setUsername(value);
                break;
            case "bio":
                user.setBio(value);
                break;
            case "grade":
                user.setGrade(value);
                break;
            case "gender":
                user.setGender(value);
                break;
            case "age":
                user.setAge(Integer.parseInt(value));
                break;
            case "avatarUrl":
                user.setAvatarUrl(value);
                break;
            default:
                throw new IllegalArgumentException("不支持的字段: " + field);
        }

        user = userRepository.save(user);
        return convertToDTO(user, null);
    }

    @Override
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadCredentialsException("原密码错误");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                new ArrayList<>()
        );
    }

    private UserDTO convertToDTO(User user, String token) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setToken(token);
        dto.setMbtiType(user.getMbtiType());
        dto.setGrade(user.getGrade());
        dto.setGender(user.getGender());
        dto.setAge(user.getAge());
        dto.setBio(user.getBio());
        dto.setAvatarUrl(user.getAvatarUrl());
        return dto;
    }
} 