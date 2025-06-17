package com.pelosa.rasutoda.service;


import com.pelosa.rasutoda.domain.User;
import com.pelosa.rasutoda.domain.UserStatus;
import com.pelosa.rasutoda.domain.UserRole;
import com.pelosa.rasutoda.dto.PasswordUpdateDto;
import com.pelosa.rasutoda.dto.UserRegisterRequestDto;
import com.pelosa.rasutoda.dto.UserProfileUpdateDto;
import com.pelosa.rasutoda.dto.OAuth2AdditionalInfoRequestDto;
import com.pelosa.rasutoda.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;


import java.util.Optional;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Optional<User> findUserByLoginId(String loginId) {
        return userRepository.findByLoginId(loginId);
    }


    @Transactional
    public Long register(UserRegisterRequestDto requestDto) {
        if (userRepository.findByLoginId(requestDto.getLoginId()).isPresent()) {
            throw new IllegalArgumentException("이미 사용중인 아이디입니다.");
        }
        if (userRepository.findByNickname(requestDto.getNickname()).isPresent()) {
            throw new IllegalArgumentException("이미 사용중인 닉네임입니다.");
        }
        if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }

        User user = User.builder()
                .loginId(requestDto.getLoginId())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .username(requestDto.getUsername())
                .nickname(requestDto.getNickname())
                .email(requestDto.getEmail())
                .phoneNumber(requestDto.getPhoneNumber())
                .marketingConsent(requestDto.isMarketingConsent())
                .status(UserStatus.PENDING)
                .role(UserRole.USER)
                .build();
        User savedUser = userRepository.save(user);


        return savedUser.getId();
    }

    @Transactional
    public void profileEdit(UserProfileUpdateDto profileDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("로그인된 사용자 정보가 없습니다. 프로필을 업데이트할 수 없습니다.");
        }
        String currentUsername = authentication.getName();


        User currentUser = userRepository.findByLoginId(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("로그인된 사용자를 찾을 수 없습니다."));


        if (!Objects.equals(currentUser.getNickname(), profileDto.getNickname())) {
            if (userRepository.findByNickname(profileDto.getNickname()).isPresent()) {
                throw new IllegalArgumentException("이미 사용중인 닉네임입니다.");
            }
        }

        if (!Objects.equals(currentUser.getEmail(), profileDto.getEmail())) {
            if (userRepository.findByEmail(profileDto.getEmail()).isPresent()) {
                throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
            }
        }

        currentUser.setNickname(profileDto.getNickname());
        currentUser.setEmail(profileDto.getEmail());
        currentUser.setPhoneNumber(profileDto.getPhoneNumber());
        userRepository.save(currentUser);


    }

    @Transactional
    public void passwordUpdate(PasswordUpdateDto passwordDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();


        User currentUser = userRepository.findByLoginId(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("로그인된 사용자를 찾을 수 없습니다."));


        if (!passwordEncoder.matches(passwordDto.getCurrentPassword(), currentUser.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        if (!passwordDto.getNewPassword().equals(passwordDto.getConfirmNewPassword())) {
            throw new IllegalArgumentException("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
        }

        String encodedNewPassword = passwordEncoder.encode(passwordDto.getNewPassword());
        currentUser.setPassword(encodedNewPassword);
        userRepository.save(currentUser);

    }

    @Transactional
    public void completeOAuth2Registration(OAuth2AdditionalInfoRequestDto additionalInfoDto, Authentication authentication) {
        if (!(authentication instanceof OAuth2AuthenticationToken)) {
            throw new IllegalStateException("OAuth2 인증 정보가 없습니다.");
        }
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String provider = oauthToken.getAuthorizedClientRegistrationId();
        String providerId = oauthToken.getName();

        User userToUpdate = userRepository.findByProviderAndProviderId(provider, providerId)
                .orElseThrow(() -> new IllegalArgumentException("OAuth2 사용자 정보를 찾을 수 없습니다: provider=" + provider + ", providerId=" + providerId));

        if (!Objects.equals(userToUpdate.getNickname(), additionalInfoDto.getNickname())) {
            if (userRepository.findByNickname(additionalInfoDto.getNickname()).isPresent()) {
                throw new IllegalArgumentException("이미 사용중인 닉네임입니다.");
            }
        }


        userToUpdate.setNickname(additionalInfoDto.getNickname());
        userToUpdate.setPhoneNumber(additionalInfoDto.getPhoneNumber());
        userToUpdate.setStatus(UserStatus.ACTIVE);

        userRepository.save(userToUpdate);

        try {
            org.springframework.security.core.userdetails.User userDetails =
                    new org.springframework.security.core.userdetails.User(
                            userToUpdate.getLoginId(),
                            userToUpdate.getPassword(),
                            userToUpdate.getStatus() == UserStatus.ACTIVE,
                            true, true, true,
                            userToUpdate.getAuthorities()
                    );

            UsernamePasswordAuthenticationToken newAuthentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    userToUpdate.getPassword(),
                    userDetails.getAuthorities()
            );

            if (RequestContextHolder.getRequestAttributes() != null) {
                newAuthentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(
                        (HttpServletRequest) ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest()));
            }

            SecurityContextHolder.getContext().setAuthentication(newAuthentication);

            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            request.getSession().removeAttribute("needs_additional_oauth2_info");
            request.getSession().removeAttribute("oauth2_user_loginId"); // 저장했던 loginId도 제거

        } catch (Exception e) {
            System.err.println("인증 컨텍스트 갱신 중 오류: " + e.getMessage());
        }
    }


    @Transactional(readOnly = true)
    public Optional<User> findByProviderAndProviderId(String provider, String providerId) {
        return userRepository.findByProviderAndProviderId(provider, providerId);
    }
}