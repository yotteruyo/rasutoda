package com.pelosa.rasutoda.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "party")
@Getter
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
    private Integer monthlyPrice;

    private LocalDate endDate;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private int viewCount = 0;

    @Builder
    public Party(User creator, Ott ott, String title, String content, Integer maxMembers, Integer monthlyPrice, LocalDate endDate) {
        this.creator = creator;
        this.ott = ott;
        this.title = title;
        this.content = content;
        this.maxMembers = maxMembers;
        this.monthlyPrice = monthlyPrice;
        this.endDate = endDate;
        this.viewCount = 0;
    }
}