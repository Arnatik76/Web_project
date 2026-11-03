package org.example.mangaverse.config;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Guards write requests to the catalog API: a signed-in administrator, or the {@code X-Admin-Token} header for scripts.
 * Runs before the request body is read, so unauthorized callers get 403 rather than validation details.
 * Temporary until real accounts and roles exist.
 */
public class AdminTokenInterceptor implements HandlerInterceptor {

    static final String HEADER = "X-Admin-Token";

    private final byte[] token;

    public AdminTokenInterceptor(String token) {
        this.token = token == null ? new byte[0] : token.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HttpMethod method = HttpMethod.valueOf(request.getMethod());
        if (method == HttpMethod.GET || method == HttpMethod.HEAD || method == HttpMethod.OPTIONS) {
            return true;
        }
        if (request.isUserInRole("ADMIN")) {
            return true;
        }
        String provided = request.getHeader(HEADER);
        boolean valid = token.length > 0 && provided != null
                && MessageDigest.isEqual(token, provided.getBytes(StandardCharsets.UTF_8));
        if (!valid) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Write access requires a valid " + HEADER + " header");
        }
        return true;
    }
}
