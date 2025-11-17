package com.ebbinghaus.ttopullae.user.domain;

import com.ebbinghaus.ttopullae.global.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Builder
@Entity @Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private Boolean receiveNotifications;

    public void updateUsername(String username) {
        this.username = username;
    }

    public void updateNotificationSetting(boolean receive) {
        this.receiveNotifications = receive;
    }
}
