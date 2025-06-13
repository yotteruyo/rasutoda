package com.pelosa.rasutoda.service;


import com.pelosa.rasutoda.domain.User;
import com.pelosa.rasutoda.dto.PasswordUpdateDto;
import com.pelosa.rasutoda.dto.UserRegisterRequestDto;
import com.pelosa.rasutoda.dto.UserProfileUpdateDto;
import com.pelosa.rasutoda.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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

        User user = User.builder()
            .loginId(requestDto.getLoginId())
            .password(passwordEncoder.encode(requestDto.getPassword()))
            .username(requestDto.getUsername())
            .nickname(requestDto.getNickname())
            .email(requestDto.getEmail())
            .phoneNumber(requestDto.getPhoneNumber())
            .marketingConsent(requestDto.isMarketingConsent())
            .build();
        User savedUser = userRepository.save(user);


        return savedUser.getId();
    }

    @Transactional
    public void profileEdit(UserProfileUpdateDto profileDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();


        User currentUser = userRepository.findByLoginId(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("로그인된 사용자를 찾을 수 없습니다."));


        if (!currentUser.getNickname().equals(profileDto.getNickname())) {
            if (userRepository.findByNickname(profileDto.getNickname()).isPresent()) {
                throw new IllegalArgumentException("이미 사용중인 닉네임입니다.");
            }
        }

        if (!currentUser.getEmail().equals(profileDto.getEmail())) {
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
}
