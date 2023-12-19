package com.ostreach.entities.model;

import com.ostreach.entities.enums.ItemCategory;
import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table
public class DescriptionEntity extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String itemName;

    @Column(nullable = false, length = 1000)
    private String itemDescription;

    @Column(nullable = false)
    private Double itemWeight;

    private Double length;
    private Double width;
    private Double height;

    @Column(nullable = false)
    private Double declaredPrice;

    @Enumerated(EnumType.STRING)
    private ItemCategory itemCategory;

    @Column(nullable = false, length = 50)
    private String senderName;

    @Column(nullable = false, length = 17)
    private String senderPhone;

    @Column(nullable = false, length = 500)
    private String pickUpLocation;

    @Column(nullable = false, length = 500)
    private String dropOffLocation;

    @Column(nullable = false, length = 50)
    private String receiverName;

    @Column(nullable = false, length = 17)
    private String receiverPhone;

    private Double distance;

    @OneToOne(cascade = {CascadeType.DETACH,CascadeType.MERGE
            ,CascadeType.PERSIST,CascadeType.REFRESH})
    @JoinColumn(name = "orders_id")
    private OrderEntity orderEntity;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.DETACH,CascadeType.MERGE
            ,CascadeType.PERSIST,CascadeType.REFRESH})
    @JoinColumn(name = "driverTask_id")
    private TaskEntity taskEntity;
}
