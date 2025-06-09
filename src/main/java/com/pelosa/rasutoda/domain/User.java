package com.pelosa.rasutoda.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String loginID;

    @Column(unique = false, nullable = false, length = 50)
    private String username;

    @Column(unique = true, length = 50)
    private String nickname;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.USER;

    @Column(nullable = false)
    private boolean marketingConsent = false;

    private LocalDateTime lastLoginAt;
    private LocalDateTime deletedAt;

    // User가 삭제되면 주소 정보도 함께 삭제 (Cascade)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserAddress> addresses = new ArrayList<>();

    // User가 삭제되면 로그인 기록도 함께 삭제
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LoginHistory> loginHistories = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade =  CascadeType.ALL)
    private List<PartyMember> joinedParties = new ArrayList<>();


    @Builder
    public User(String loginID, String username, String nickname, String password, String email,
                String phoneNumber, UserStatus status, UserRole role, boolean marketingConsent) {
        this.loginID = loginID;
        this.username = username;
        this.nickname = nickname;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.status = status != null ? status : UserStatus.PENDING;
        this.role = role != null ? role : UserRole.USER;
        this.marketingConsent = marketingConsent;
    }
}

enum UserStatus { ACTIVE, DORMANT, BANNED, PENDING }