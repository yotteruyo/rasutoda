package com.pelosa.rasutoda.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
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

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private int viewCount = 0;
}