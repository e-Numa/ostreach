package com.ostreach.entities.model;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "notification_tb")
public class NotificationEntity extends BaseEntity {
    @Column(name = "notification", columnDefinition = "TEXT")
    private String message;

    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE
            , CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name = "users_id")
    private UserEntity users;
}