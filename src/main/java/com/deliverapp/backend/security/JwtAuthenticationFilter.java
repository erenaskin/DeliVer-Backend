package com.deliverapp.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklist tokenBlacklist;

    @Override
    protected void doFilterInternal(@org.springframework.lang.NonNull HttpServletRequest request,
                                    @org.springframework.lang.NonNull HttpServletResponse response,
                                    @org.springframework.lang.NonNull FilterChain filterChain) throws ServletException, IOException {

        // 1. Token'ı yardımcı metot ile alıyoruz.
        String token = getTokenFromRequest(request);

        // 2. Token varsa işlemlere devam ediyoruz.
        if (token != null) {
            if (tokenBlacklist.isBlacklisted(token)) {
                // Daha iyi hata yönetimi için burada custom exception fırlatılabilir.
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token iptal edilmiş veya geçersiz");
                return;
            }

            try {
                String username = jwtTokenProvider.getUsernameFromToken(token);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    if (jwtTokenProvider.validateToken(token)) {
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                // Token süresi dolmuş
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"error\": \"TOKEN_EXPIRED\", \"message\": \"Token süresi dolmuş. Lütfen yeniden giriş yapın.\"}");
                return;
            } catch (io.jsonwebtoken.MalformedJwtException e) {
                // Geçersiz token formatı
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"error\": \"INVALID_TOKEN\", \"message\": \"Geçersiz token formatı.\"}");
                return;
            } catch (io.jsonwebtoken.JwtException e) {
                // Diğer JWT hataları
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"error\": \"JWT_ERROR\", \"message\": \"Token doğrulama hatası.\"}");
                return;
            } catch (Exception e) {
                // Diğer hatalar
                org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(JwtAuthenticationFilter.class);
                logger.warn("JWT işlenirken bir hata oluştu: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"error\": \"AUTHENTICATION_ERROR\", \"message\": \"Kimlik doğrulama hatası.\"}");
                return;
            }
        }

        // 3. İsteği bir sonraki filtreye iletiyoruz.
        filterChain.doFilter(request, response);
    }

    /**
     * Gelen HttpServletRequest'in "Authorization" başlığından JWT'yi çıkarır.
     * @param request Gelen istek
     * @return JWT String'i veya token bulunamazsa null
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " (7 karakter) sonrasını döndür
        }

        return null;
    }
}