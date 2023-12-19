package com.ostreach.entities.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "tracking_tb")
public class TrackingEntity extends BaseEntity{

    @Column(nullable = false)
    private String location;

    @ManyToOne(fetch = FetchType.LAZY,cascade = {CascadeType.DETACH,CascadeType.MERGE,
            CascadeType.PERSIST,CascadeType.REFRESH})
    @JoinColumn(name = "order_id")
    private OrderEntity orderEntity;
}
