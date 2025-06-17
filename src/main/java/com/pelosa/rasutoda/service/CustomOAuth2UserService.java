package com.pelosa.rasutoda.service;

import com.pelosa.rasutoda.domain.User;
import com.pelosa.rasutoda.domain.UserRole;
import com.pelosa.rasutoda.domain.UserStatus;
import com.pelosa.rasutoda.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpSession;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final HttpSession httpSession;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oauth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        Map<String, Object> attributes = oauth2User.getAttributes();

        String email = null;
        String name = null;
        String nickname = null;
        String providerId;

        if ("google".equals(registrationId)) {
            email = (String) attributes.get("email");
            name = (String) attributes.get("name");
            nickname = (String) attributes.get("name");
            providerId = (String) attributes.get("sub");
        } else if ("kakao".equals(registrationId)) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

            email = (String) kakaoAccount.get("email");
            nickname = (String) profile.get("nickname");
            name = (String) profile.get("nickname");
            providerId = String.valueOf(attributes.get("id"));
        } else {
            throw new OAuth2AuthenticationException("지원하지 않는 OAuth2 등록 ID: " + registrationId);
        }

        Optional<User> userOptionalByProviderId = userRepository.findByProviderAndProviderId(registrationId, providerId);
        User user;

        if (userOptionalByProviderId.isPresent()) {
            user = userOptionalByProviderId.get();
            user.setNickname(nickname);
            user.setUsername(name);
            userRepository.save(user);

            if (user.getPhoneNumber() == null || user.getPhoneNumber().isEmpty() || user.getStatus() == UserStatus.PENDING_ADDITIONAL_INFO) {
                httpSession.setAttribute("needs_additional_oauth2_info", true);
                httpSession.setAttribute("oauth2_user_loginId", user.getLoginId());
            } else {
                httpSession.removeAttribute("needs_additional_oauth2_info");
                httpSession.removeAttribute("oauth2_user_loginId");
            }

        } else {
            Optional<User> userOptionalByEmail = userRepository.findByEmail(email);

            if (userOptionalByEmail.isPresent()) {
                user = userOptionalByEmail.get();
                user.setProvider(registrationId);
                user.setProviderId(providerId);
                user.setNickname(nickname);
                user.setUsername(name);
                if (user.getStatus() == UserStatus.PENDING_ADDITIONAL_INFO && user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) {
                    user.setStatus(UserStatus.ACTIVE);
                } else if (user.getStatus() == UserStatus.PENDING) {
                    user.setStatus(UserStatus.ACTIVE);
                }
                userRepository.save(user);

                httpSession.removeAttribute("needs_additional_oauth2_info");
                httpSession.removeAttribute("oauth2_user_loginId");

            } else {
                user = User.builder()
                        .loginId(registrationId + "_" + providerId)
                        .password("oauth2_password")
                        .username(name)
                        .nickname(nickname != null && !nickname.isEmpty() ? nickname : "소셜사용자")
                        .email(email)
                        .phoneNumber(null)
                        .marketingConsent(false)
                        .status(UserStatus.PENDING_ADDITIONAL_INFO)
                        .role(UserRole.USER)
                        .provider(registrationId)
                        .providerId(providerId)
                        .build();
                userRepository.save(user);

                httpSession.setAttribute("needs_additional_oauth2_info", true);
                httpSession.setAttribute("oauth2_user_loginId", user.getLoginId());
            }
        }

        return new DefaultOAuth2User(
                Collections.singleton(new org.springframework.security.core.authority.SimpleGrantedAuthority(user.getRole().name())),
                attributes,
                userNameAttributeName
        );
    }
}