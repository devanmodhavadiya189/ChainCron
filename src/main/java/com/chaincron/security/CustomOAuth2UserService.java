package com.chaincron.security;

import com.chaincron.domain.entity.User;
import com.chaincron.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String googleSub = (String) attributes.get("sub");
        String email = (String) attributes.get("email");

        User user = userRepository.findByGoogleSub(googleSub)
                .orElseGet(() -> createUser(googleSub, email));

        return new CustomOAuth2User(user, attributes);
    }

    private User createUser(String googleSub, String email) {
        log.info("Registering new user with email={}", email);
        User newUser = User.builder()
                .googleSub(googleSub)
                .email(email)
                .build();
        return userRepository.save(newUser);
    }
}
