package com.pelosa.rasutoda.service;

import com.pelosa.rasutoda.domain.User;
//import com.pelosa.rasutoda.domain.UserAddress;
import com.pelosa.rasutoda.domain.UserRole;
//import com.pelosa.rasutoda.dto.UserAddressDto;
import com.pelosa.rasutoda.dto.UserRegisterRequestDto;
import com.pelosa.rasutoda.repository.UserRepository;
//import com.pelosa.rasutoda.repository.UserAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
//    private final UserAddressRepository userAddressRepository;
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

//        UserAddressDto addressDto = requestDto.getAddress();
//        if (addressDto != null){
//            UserAddress address = UserAddress.builder()
//                    .user(savedUser)
//                    .postalCode(addressDto.getPostalCode())
//                    .addressLine1(addressDto.getAddressLine1())
//                    .addressLine2(addressDto.getAddressLine2())
//                    .isDefault(true)
//                    .build();
//            userAddressRepository.save(address);
//        }

        return savedUser.getId();
    }
}
