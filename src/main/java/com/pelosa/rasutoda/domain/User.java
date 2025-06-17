package com.pelosa.rasutoda.domain;

import jakarta.persistence.*;
import lombok.*;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collection;
import java.util.Collections;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = true, length = 50)
    private String loginId;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(unique = true, nullable = false, length = 50)
    private String nickname;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false, length = 150)
    private String email;

    @Column(length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.USER;

    @Column(nullable = false)
    private boolean marketingConsent = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(length = 50, unique = true)
    private String provider;

    @Column(length = 255, unique = true)
    private String providerId;

    @OneToMany(mappedBy = "member",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PartyMember> joinedPartyMembers = new ArrayList<>();

    @Builder
    public User(String loginId, String username, String nickname, String password, String email, String phoneNumber, boolean marketingConsent, UserStatus status, UserRole role, String provider, String providerId) {
        this.loginId = loginId;
        this.username = username;
        this.nickname = nickname;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.marketingConsent = marketingConsent;
        this.provider = provider;
        this.providerId = providerId;
        this.status = status;
        this.role = role;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority(this.role.name()));
    }
}