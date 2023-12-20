package com.ostreach.services.user_service.serviceImplementation;

import com.ostreach.entities.enums.ItemCategory;
import com.ostreach.entities.enums.OrderStatus;
import com.ostreach.entities.enums.PaymentStatus;
import com.ostreach.entities.model.DescriptionEntity;
import com.ostreach.entities.model.OrderEntity;
import com.ostreach.entities.model.TransactionEntity;
import com.ostreach.entities.model.UserEntity;
import com.ostreach.exceptions.OrderCannotBeCanceledException;
import com.ostreach.exceptions.OrderNotFoundException;
import com.ostreach.exceptions.TransactionErrorException;
import com.ostreach.exceptions.UserNotFoundException;
import com.ostreach.payload.request.DescriptionRequest;
import com.ostreach.payload.response.*;
import com.ostreach.repositories.DescriptionRepository;
import com.ostreach.repositories.OrderRepository;
import com.ostreach.repositories.TransactionRepository;
import com.ostreach.repositories.UserRepository;
import com.ostreach.services.driver_service.DriverService;
import com.ostreach.services.notification_service.NotificationService;
import com.ostreach.services.user_service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

import static com.ostreach.commons.CostConstant.*;
import static com.ostreach.utils.CustomIdGenerator.trackingIdGenerator;

@RequiredArgsConstructor
@Service
public class OrderServiceImpl implements OrderService  {

    private final OrderRepository orderRepository;
    private final UserRepository userRepo;
    private final TransactionRepository transactionRepository;
    private final DriverService driverService;
    private final DescriptionRepository descriptionRepository;
    private final NotificationService notificationService;

    /**
     * formula =>Total cost = COST_PER_KM * KM + COST_PER_KG * KG + COST_PER_50K *(declaredPrice/50,000)
     * +COST_FRAGILE + COST_PERISHABLE + COST_DOCUMENT
     */

    @Override
    public TransactionResponse getQuotation(DescriptionRequest orderDesc) {

        BigDecimal totalPrice = calcTotalCost(orderDesc.getDistance(),
                orderDesc.getItemWeight(),
                orderDesc.getDeclaredPrice(),
                orderDesc.getItemCategory(), orderDesc.getWidth(),
                orderDesc.getLength(), orderDesc.getHeight());

        totalPrice = totalPrice.setScale(2, RoundingMode.HALF_EVEN);

        BigDecimal weightCost = BigDecimal.valueOf(orderDesc.getItemWeight() <= 5.0 ? 0 : orderDesc.getItemWeight() * COST_PER_KG);

        return TransactionResponse.builder()
                .totalAmount(totalPrice)
                .weightCost(weightCost)
                .build();
    }

    @Transactional
    @Override
    public TransactionResponse createOrder(DescriptionRequest descriptionRequest) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        UserEntity user = userRepo.findUserEntityByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found!"));

        DescriptionEntity description = DescriptionEntity.builder()
                .itemName(descriptionRequest.getItemName())
                .itemDescription(descriptionRequest.getItemDescription())
                .senderName(descriptionRequest.getSenderName())
                .senderPhone(descriptionRequest.getSenderPhone())
                .receiverName(descriptionRequest.getReceiverName())
                .receiverPhone(descriptionRequest.getReceiverPhone())
                .width(descriptionRequest.getWidth())
                .length(descriptionRequest.getLength())
                .height(descriptionRequest.getHeight())
                .declaredPrice(descriptionRequest.getDeclaredPrice())
                .itemWeight(descriptionRequest.getItemWeight())
                .itemCategory(ItemCategory.valueOf(descriptionRequest.getItemCategory().toUpperCase()))
                .pickUpLocation(descriptionRequest.getPickUpLocation())
                .dropOffLocation(descriptionRequest.getDropOffLocation())
                .distance(descriptionRequest.getDistance())
                .build();

        DescriptionEntity savedDescription = descriptionRepository.save(description);


        BigDecimal weightCost = BigDecimal.valueOf(descriptionRequest.getItemWeight() <= 5.0 ? 0 : descriptionRequest.getItemWeight() * COST_PER_KG);

        BigDecimal totalPrice = calcTotalCost(descriptionRequest.getDistance(),
                descriptionRequest.getItemWeight(),
                descriptionRequest.getDeclaredPrice(),
                descriptionRequest.getItemCategory(), descriptionRequest.getWidth(),
                descriptionRequest.getLength(), descriptionRequest.getHeight());

        totalPrice = totalPrice.setScale(2, RoundingMode.HALF_UP);

        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.getDefault());
        String price = formatter.format(totalPrice);

        String priceDeclared = formatter.format(descriptionRequest.getDeclaredPrice());

        OrderEntity order = OrderEntity.builder()
                .status(OrderStatus.PENDING)
                .trackingNumber("KG" + trackingIdGenerator())
                .userEntity(user)
                .descriptionEntity(savedDescription)
                .deliveryCost(price)
                .build();

        OrderEntity savedOder = orderRepository.save(order);

        String receipt = generateReceipt(savedOder.getTrackingNumber(), descriptionRequest.getItemName(),
                descriptionRequest.getItemDescription(), descriptionRequest.getItemWeight(),
                priceDeclared, descriptionRequest.getItemCategory(),
                descriptionRequest.getPickUpLocation(), descriptionRequest.getDropOffLocation(),
                price, descriptionRequest.getLength(), descriptionRequest.getHeight(), descriptionRequest.getWidth());

        TransactionEntity transaction = TransactionEntity.builder()
                .amount(totalPrice)
                .receipt(receipt)
                .orderEntity(savedOder)
                .status(PaymentStatus.PENDING)
                .build();

        TransactionEntity savedTransaction = transactionRepository.save(transaction);

        return TransactionResponse.builder()
                .transactionId(savedTransaction.getId())
                .totalAmount(totalPrice)
                .weightCost(weightCost)
                .build();
    }


    @Transactional
    @Override
    public String cancelOder(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found!"));

        if (order.getStatus().equals(OrderStatus.ORDER_CONFIRMED)) {
            throw new OrderCannotBeCanceledException("Order can no longer be cancelled!");
        }

        order.setStatus(OrderStatus.CANCELLED);

        orderRepository.save(order);
        return "Order cancelled successfully.";
    }

    @Transactional
    @Override
    public String validatePayment(PaymentRequest paymentRequest) {
        if (paymentRequest.getReferenceId().length() < 10) {
            throw new TransactionErrorException("Reference ID is invalid!");
        }
        TransactionEntity transaction = transactionRepository.findById(paymentRequest.getTransactionId())
                .orElseThrow(() -> new TransactionErrorException("Transaction not found!"));

        transaction.setReferenceId(paymentRequest.getReferenceId());
        transaction.setStatus(PaymentStatus.CONFIRMED);

        transactionRepository.save(transaction);

        checkOrderStatus(transaction.getOrderEntity().getId());

        return "Payment successful!";
    }

    @Transactional
    @Override
    public UserOrderPage getOrderHistory(Integer pageNo, Integer pageSize) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (pageSize < 1) {
            pageSize = 1;
        }

        Pageable pageable = PageRequest.of(pageNo, pageSize);

        Slice<OrderEntity> orderList = orderRepository.findAllByUserEntityEmailOrderByIdDesc(email, pageable);

        return UserOrderPage.builder()
                .pageNo(orderList.getNumber())
                .pageSize(orderList.getSize())
                .lastPage(orderList.isLast())
                .orderResponseList(orderList.stream().map(order -> UserOrderResponse.builder()
                        .orderId(order.getId())
                        .receiver(order.getDescriptionEntity().getReceiverName())
                        .date(String.valueOf(order.getDateCreated()))
                        .amount(order.getDeliveryCost())
                        .dropOffLocation(order.getDescriptionEntity().getDropOffLocation())
                        .pickUpLocation(order.getDescriptionEntity().getPickUpLocation())
                        .status(order.getStatus())
                        .trackingNum(order.getTrackingNumber())
                        .locationList(order.getTrackingLocationEntities().stream()
                                .map((location)-> TrackingResponse.builder()
                                        .locationId(location.getId())
                                        .dateTime(String.valueOf(location.getDateCreated()))
                                        .location(location.getLocation())
                                        .build()).toList())
                        .build()).toList())
                .build();
    }

    @Override
    public UserOverView getUserOverView(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        OrderEntity order = orderRepository.findFirstByUserEntityEmailOrderByDateCreatedDesc(email)
                .orElseThrow(()-> new OrderNotFoundException("No order found!"));

        return UserOverView.builder()
                .senderName(order.getDescriptionEntity().getSenderName())
                .senderAddress(order.getDescriptionEntity().getPickUpLocation())
                .receiverAddress(order.getDescriptionEntity().getDropOffLocation())
                .receiverName(order.getDescriptionEntity().getReceiverName())
                .deliveryStatus(order.getTaskEntity() == null ? new DeliveryStatus() : DeliveryStatus.builder()
                        .taskStatusDate(String.valueOf(order.getTaskEntity().getDateCreated()))
                        .pickUpDate(String.valueOf(order.getDateCreated()))
                        .deliveryStatusDate(order.getStatus() == OrderStatus.OUT_FOR_DELIVERY ?
                                String.valueOf(order.getDateModified()) : null)
                        .deliveryDate(order.getStatus() == OrderStatus.DELIVERED ?
                                String.valueOf(order.getDateModified()) : null)
                        .build())
                .driverInfo(order.getTaskEntity() == null ? new DriverInfo() :  DriverInfo.builder()
                        .driverId(order.getTaskEntity().getDriverEntity().getId())
                        .firstName(order.getTaskEntity().getDriverEntity().getFirstName())
                        .lastName(order.getTaskEntity().getDriverEntity().getLastName())
                        .phoneNo(order.getTaskEntity().getDriverEntity().getPhoneNumber())
                        .build())
                .locationList(order.getTrackingLocationEntities().stream()
                        .map((location)-> TrackingResponse.builder()
                                .locationId(location.getId())
                                .dateTime(String.valueOf(location.getDateCreated()))
                                .location(location.getLocation())
                                .build()).toList())
                .build();
    }


    private BigDecimal calcTotalCost(Double distance, Double weight, Double declaredPrice, String itemCategory,
                                     Double width, Double length, Double height) {
        double area = (width * length * height) / 1000.0;
        return BigDecimal.valueOf(COST_PER_KM * distance)
                .add(BigDecimal.valueOf(weight <= 5.0 ? 0 : COST_PER_KG * weight))
                .add(BigDecimal.valueOf(COST_PER_50K * (declaredPrice / 50_000.0)))
                .add(BigDecimal.valueOf(itemCategory.equalsIgnoreCase("FRAGILE") ? COST_FRAGILE
                        : itemCategory.equalsIgnoreCase("PERISHABLES") ? COST_PERISHABLE
                        : itemCategory.equalsIgnoreCase("DOCUMENTS") ? COST_DOCUMENT : 0))
                .add(BigDecimal.valueOf(area < 10.0 ? 0 : area * COST_PER_AREA));
    }

    private String generateReceipt(String trackingId, String itemName, String description,
                                   Double weight, String declaredPrice, String category,
                                   String pickupLocation, String dropOffLocation, String deliveryCost,
                                   Double length, Double height, Double width) {

        return String.format("""
                        Tracking Number: %s
                        Item Name: %s
                        Description: %s
                        Weight: %s kg
                        Dimension: L: %smm  H: %smm  W: %smm
                        Declared price: %s
                        Item category: %s
                        Pickup location: %s
                        Drop-off location: %s
                        Cost of delivery: %s
                        """, trackingId, itemName, description, weight, length, height, width, declaredPrice,
                category, pickupLocation, dropOffLocation, deliveryCost);
    }

    @Transactional
    public void checkOrderStatus(Long orderId) {
        new Thread(() -> {
            long minutes = 1L;
            Date expireDate = new Date(System.currentTimeMillis() + 1000 * 60 * minutes);

            while (!new Date().after(expireDate)) {
            }

            OrderEntity orderEntity = orderRepository.findById(orderId)
                    .orElseThrow(() -> new OrderNotFoundException("Order not found!"));

            if (!orderEntity.getStatus().equals(OrderStatus.CANCELLED)) {
                orderEntity.setStatus(OrderStatus.ORDER_CONFIRMED);
                OrderEntity savedOrder = orderRepository.save(orderEntity);
                notificationService.sendNotification("Your order has been created successfully",
                        savedOrder.getUserEntity().getId());
                driverService.generateRandomOrder();
            }
        }).start();
    }
}
