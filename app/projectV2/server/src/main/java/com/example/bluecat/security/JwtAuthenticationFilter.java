package com.example.bluecat.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // 直接检查登录和注册请求，跳过JWT验证
        String path = request.getRequestURI();
        if (path.equals("/api/user/login") || path.equals("/api/user/register")) {
            log.debug("跳过JWT验证，路径: {}", path);
            filterChain.doFilter(request, response);
            return;
        }
        
        final String requestTokenHeader = request.getHeader("Authorization");
        
        String username = null;
        String jwtToken = null;
        
        // JWT Token格式为 "Bearer token"，移除Bearer前缀获取实际token
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                username = jwtTokenUtil.extractUsername(jwtToken);
                log.debug("JWT Token中提取的用户名: {}", username);
            } catch (Exception e) {
                log.warn("无法从JWT Token中提取用户名: {}", e.getMessage());
            }
        } else {
            log.debug("JWT Token不存在或格式不正确");
        }

        // 验证token并设置认证信息
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                // 验证token是否有效
                if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(
                            userDetails, 
                            null, 
                            userDetails.getAuthorities()
                        );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // 设置认证信息到SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("用户 {} 的JWT验证成功", username);
                } else {
                    log.warn("用户 {} 的JWT Token验证失败", username);
                }
            } catch (Exception e) {
                log.error("JWT认证过程中发生错误: {}", e.getMessage());
            }
        }
        
        // 继续过滤器链
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        boolean shouldSkip = path.equals("/api/user/login") || 
               path.equals("/api/user/register") ||
               path.startsWith("/uploads/") ||
               path.startsWith("/static/");
        
        log.debug("请求路径: {}, 是否跳过JWT验证: {}", path, shouldSkip);
        return shouldSkip;
    }
} 