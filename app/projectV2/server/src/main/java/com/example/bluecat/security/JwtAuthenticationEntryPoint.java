package com.example.bluecat.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, 
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        
        log.warn("未经授权的访问尝试: {} {}, 错误信息: {}", 
                request.getMethod(), request.getRequestURI(), authException.getMessage());
        
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        String jsonResponse = "{\n" +
                "  \"error\": \"Unauthorized\",\n" +
                "  \"message\": \"访问被拒绝，请提供有效的JWT令牌\",\n" +
                "  \"status\": 401,\n" +
                "  \"timestamp\": " + System.currentTimeMillis() + "\n" +
                "}";
        
        response.getWriter().write(jsonResponse);
    }
} 