package com.ostreach.entities.model;

import com.ostreach.entities.enums.DriverStatus;
import com.ostreach.entities.enums.Gender;
import com.ostreach.entities.enums.Roles;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "users_tb")
public class UserEntity extends BaseEntity {

    @Column(nullable = false,length = 25)
    private String firstName;

    @Column(length = 25)
    private String lastName;

    @Column(nullable = false, length = 35)
    private String email;

    @Column(nullable = false, length = 60)
    private String password;

    @Transient
    private String confirmPassword;

    @Column(nullable = false,length = 15)
    private String phoneNumber;

    @Column(nullable = false, length = 100)
    private String address;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(length = 11)
    private String dob;

    private String pictureUrl;

    @Enumerated(EnumType.STRING)
    private Roles roles;

    private Boolean isVerified;

    @Enumerated(EnumType.STRING)
    private DriverStatus driverStatus;

    @OneToMany(mappedBy = "userEntity", cascade = CascadeType.ALL)
    private List<OrderEntity> orderEntities = new ArrayList<>();

    @OneToOne( cascade = CascadeType.ALL)
    private KYDEntity kydEntity;

    @OneToMany(mappedBy = "driverEntity", cascade = {CascadeType.DETACH,CascadeType.MERGE
            ,CascadeType.PERSIST,CascadeType.REFRESH},fetch = FetchType.LAZY)
    private List<TaskEntity> driverTaskEntities = new ArrayList<>();

    @OneToMany(mappedBy = "users", cascade = {CascadeType.DETACH,CascadeType.MERGE
            ,CascadeType.PERSIST,CascadeType.REFRESH}, fetch = FetchType.EAGER)
    private List<NotificationEntity> notifications = new ArrayList<>();


    public void addOrder(OrderEntity order){
        orderEntities.add(order);
        order.setUserEntity(this);
    }

    public void addToDriverTask(TaskEntity driverTask){
        driverTaskEntities.add(driverTask);
        driverTask.setDriverEntity(this);
    }

    public void addToNotification(NotificationEntity notificationMessage){
        notifications.add(notificationMessage);
        notificationMessage.setUsers(this);
    }

}
