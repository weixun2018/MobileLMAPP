package com.example.bluecat.service.impl;

import com.example.bluecat.dto.UserDTO;
import com.example.bluecat.entity.User;
import com.example.bluecat.mapper.UserMapper;
import com.example.bluecat.security.JwtTokenUtil;
import com.example.bluecat.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;

    @Override
    @Transactional
    public UserDTO register(User user) {
        // 检查用户名是否已存在
        if (userMapper.countByUsername(user.getUsername()) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (user.getEmail() != null && userMapper.countByEmail(user.getEmail()) > 0) {
            throw new RuntimeException("邮箱已被注册");
        }

        // 检查手机号是否已存在
        if (user.getPhone() != null && userMapper.countByPhone(user.getPhone()) > 0) {
            throw new RuntimeException("手机号已被注册");
        }

        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 保存用户
        userMapper.insert(user);

        // 生成token
        UserDetails userDetails = loadUserByUsername(user.getUsername());
        String token = jwtTokenUtil.generateToken(userDetails);

        // 返回UserDTO
        return convertToDTO(user, token);
    }

    @Override
    public UserDTO login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("密码错误");
        }

        UserDetails userDetails = loadUserByUsername(username);
        String token = jwtTokenUtil.generateToken(userDetails);

        return convertToDTO(user, token);
    }

    @Override
    public UserDTO getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return convertToDTO(user, null);
    }

    @Override
    @Transactional
    public UserDTO updateUserField(Long userId, Map<String, String> fieldData) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        String field = fieldData.keySet().iterator().next();
        String value = fieldData.get(field);

        switch (field) {
            case "username":
                // 检查新用户名是否已存在
                if (userMapper.countByUsername(value) > 0) {
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

        // 设置更新时间
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        return convertToDTO(user, null);
    }

    @Override
    @Transactional
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadCredentialsException("原密码错误");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        // 设置更新时间
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }

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