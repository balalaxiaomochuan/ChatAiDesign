package com.huawei.chataidesign.config.filter;

import com.huawei.chataidesign.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    @Qualifier("customUserDetailsService")
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        printMethLog(request, response);

        // 对于流式接口进行特殊处理
        String requestURI = request.getRequestURI();
        if (requestURI.endsWith("/api/ai/stream")) {
            handleStreamRequest(request, response, filterChain);
        } else {
            handleNormalRequest(request, response, filterChain);
        }
    }

    private void handleStreamRequest(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 只在首次请求时进行认证，避免重复认证
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                String jwt = parseJwt(request);
                if (jwt != null && jwtUtil.validateToken(jwt, userDetailsService.loadUserByUsername(jwtUtil.extractUsername(jwt)))) {
                    String username = jwtUtil.extractUsername(jwt);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            log.error("Stream authentication error:", e);
            // 对于流式接口，即使认证失败也继续执行，让业务层处理
        }

        filterChain.doFilter(request, response);
    }
 
    private void handleNormalRequest(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);
            if (jwt != null && jwtUtil.validateToken(jwt, userDetailsService.loadUserByUsername(jwtUtil.extractUsername(jwt)))) {
                String username = jwtUtil.extractUsername(jwt);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication:", e);
        }

        filterChain.doFilter(request, response);
    }


    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth)) {
            if (headerAuth.startsWith("Bearer ")) {
                return headerAuth.substring(7); // 去掉 "Bearer " 前缀
            } else {
                return headerAuth; // 直接返回纯 token
            }
        }

        return null;
    }

    private void printMethLog(HttpServletRequest request,
                                    HttpServletResponse response) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String remoteAddr = request.getRemoteAddr();

        String requestUrl = queryString != null ? uri + "?" + queryString : uri;

        log.info(">>> API Request: {} {} from {}", method, requestUrl, remoteAddr);
    }
}
