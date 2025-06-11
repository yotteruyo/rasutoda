package com.pelosa.rasutoda.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "party")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Party extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ott_id")
    private Ott ott;

    @Column(nullable = false)
    private String title;

    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL)
    private List<PartyMember> members = new ArrayList<>();

    @Column(nullable = false)
    private Integer maxMembers;

    @Column(nullable = false)
    private Integer currentMembers = 0;

    @Column(nullable = false)
    private Integer monthlyPrice;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @Column(name = "ott_account_id", nullable = false, length = 150)
    private String ottAccountId;

    @Column(name = "ott_account_password", nullable = false, length = 255)
    private String ottAccountPassword;


    @Column(nullable = false)
    private int viewCount = 0;

    @Builder
    public Party(User creator, Ott ott, String title, Integer maxMembers, Integer monthlyPrice,LocalDate startDate, LocalDate endDate, String ottAccountId, String ottAccountPassword, Integer currentMembers) {
        this.creator = creator;
        this.ott = ott;
        this.title = title;
        this.maxMembers = maxMembers;
        this.monthlyPrice = monthlyPrice;
        this.startDate = startDate;
        this.endDate = endDate;
        this.viewCount = 0;
        this.ottAccountId = ottAccountId;
        this.ottAccountPassword = ottAccountPassword;
        this.currentMembers = currentMembers;
    }
}