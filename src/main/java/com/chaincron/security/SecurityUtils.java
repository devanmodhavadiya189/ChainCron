package com.chaincron.security;

import com.chaincron.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static CustomOAuth2User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof CustomOAuth2User principal)) {
            throw new UnauthorizedException("No authenticated user in context");
        }
        return principal;
    }

    public static Long currentUserId() {
        return currentUser().getUserId();
    }
}
