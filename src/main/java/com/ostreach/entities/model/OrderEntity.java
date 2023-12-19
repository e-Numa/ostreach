package com.ostreach.entities.model;

import com.ostreach.entities.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "order_tb")
public class OrderEntity extends BaseEntity {

    @Column(name = "tracking_num", length = 22)
    private String trackingNumber;

    private String deliveryCost;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private DescriptionEntity descriptionEntity;

    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.DETACH,CascadeType.MERGE
            ,CascadeType.PERSIST,CascadeType.REFRESH})
    @JoinColumn(name = "user_id")
    private UserEntity userEntity;

    @OneToMany(mappedBy = "orderEntity", cascade = CascadeType.ALL)
    private List<TrackingEntity> trackingLocationEntities = new ArrayList<>();

    @OneToOne       /* Cascade omitted to avoid deleting stored amount from transaction entity.*/
    @JoinColumn(name = "transaction_id")
    private TransactionEntity transactionEntity;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "driverTask_id")
    private TaskEntity taskEntity;

    public void addTrackingLocation(TrackingEntity location){
        trackingLocationEntities.add(location);
        location.setOrderEntity(this);
    }
}
