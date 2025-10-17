package com.deliverapp.backend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.userdetails.UserDetailsService;

import jakarta.servlet.FilterChain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {
    private JwtTokenProvider jwtTokenProvider;
    private UserDetailsService userDetailsService;
    private TokenBlacklist tokenBlacklist;
    private JwtAuthenticationFilter filter;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = mock(JwtTokenProvider.class);
        userDetailsService = mock(UserDetailsService.class);
        tokenBlacklist = mock(TokenBlacklist.class);
        filter = new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService, tokenBlacklist);
        filterChain = mock(FilterChain.class);
    }

    @Test
    void testNoHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilterInternal(request, response, filterChain);
        assertEquals(200, response.getStatus());
    }

    @Test
    void testBlacklistedToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer testtoken");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(tokenBlacklist.isBlacklisted("testtoken")).thenReturn(true);
        filter.doFilterInternal(request, response, filterChain);
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Token iptal edilmiş"));
    }
}
