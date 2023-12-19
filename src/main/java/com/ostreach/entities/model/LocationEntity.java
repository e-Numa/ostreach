package com.ostreach.entities.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "location_tb")
public class LocationEntity extends BaseEntity{
    @Column(nullable = false)
    private String pickUpLocation;

    @Column(nullable = false)
    private String dropOffLocation;

    @OneToOne(cascade = {CascadeType.DETACH,CascadeType.MERGE
            ,CascadeType.PERSIST,CascadeType.REFRESH})
    @JoinColumn(name = "order_id")
    private OrderEntity orderEntity;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.DETACH,CascadeType.MERGE
            ,CascadeType.PERSIST,CascadeType.REFRESH})
    @JoinColumn(name = "driverTask_id")
    private TaskEntity taskEntity;
}