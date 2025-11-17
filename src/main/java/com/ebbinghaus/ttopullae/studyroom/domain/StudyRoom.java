package com.ebbinghaus.ttopullae.studyroom.domain;

import com.ebbinghaus.ttopullae.global.BaseTimeEntity;
import com.ebbinghaus.ttopullae.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

@Builder
@Entity @Getter
@Table(name = "study_rooms")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class StudyRoom extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long studyRoomId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomType roomType;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Column
    private String category;

    @Column(unique = true)
    private String joinCode;

    public boolean isGroupRoom() {
        return this.roomType == RoomType.GROUP;
    }
}
