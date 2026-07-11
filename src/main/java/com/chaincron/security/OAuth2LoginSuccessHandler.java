package com.chaincron.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final String frontendRedirectUrl;

    public OAuth2LoginSuccessHandler(
            @Value("${chaincron.frontend.redirect-url:http://localhost:3000/auth/callback}") String frontendRedirectUrl
    ) {
        this.frontendRedirectUrl = frontendRedirectUrl;
        setDefaultTargetUrl(frontendRedirectUrl);
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        CustomOAuth2User principal = (CustomOAuth2User) authentication.getPrincipal();
        log.info("OAuth2 login success for user id={}", principal.getUserId());
        getRedirectStrategy().sendRedirect(request, response, frontendRedirectUrl);
    }
}
