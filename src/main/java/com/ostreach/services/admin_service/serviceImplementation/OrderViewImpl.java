package com.ostreach.services.admin_service.serviceImplementation;

import com.ostreach.entities.enums.OrderStatus;
import com.ostreach.entities.enums.StatusConstant;
import com.ostreach.entities.model.OrderEntity;
import com.ostreach.exceptions.OrderNotFoundException;
import com.ostreach.payload.response.*;
import com.ostreach.repositories.OrderRepository;
import com.ostreach.services.admin_service.OrderViewService;
import com.ostreach.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderViewImpl implements OrderViewService {
    private final OrderRepository orderRepo;
    private final ModelMapper mapper = new ModelMapper();


    @Override
    public GeneralOrderResponse getAllOrders(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<OrderEntity> orders =
                orderRepo.findOrderEntitiesByRecordStatusOrderByDateCreatedDesc(StatusConstant.ACTIVE, pageable);

        List<OrderEntity> listOfOrders = orders.getContent();

        List<AllOrderResponse> responses = new ArrayList<>();
        for (OrderEntity order : listOfOrders) {
            DriverResponses.DriverResponsesBuilder driverResponseBuilder = createDriverResponseBuilder(order);

            AllOrderResponse allOrderResponse = AllOrderResponse.builder()
                    .trackingNum(order.getTrackingNumber())
                    .orderDate(DateUtils.toDateString(order.getDateCreated()))
                    .status(order.getStatus())
                    .pickUpLocation(order.getDescriptionEntity().getPickUpLocation())
                    .dropOffLocation(order.getDescriptionEntity().getDropOffLocation())
                    .user(mapper.map(order.getUserEntity(), UserResponse.class))
                    .driver(driverResponseBuilder.build())
                    .build();
            responses.add(allOrderResponse);
        }

        return GeneralOrderResponse.builder()
                .pageNo(orders.getNumber())
                .pageSize(orders.getSize())
                .totalPage(orders.getTotalPages())
                .lastPage(orders.isLast())
                .generalOrders(responses)
                .build();
    }

    @Override
    public List<AllOrderResponse> searchByEmail(String email, int pageNo, int pageSize) {
        if (email == null || email.trim().isEmpty()) {
            log.error("Invalid email input: {}", email);
            throw new OrderNotFoundException("No order with email address " + email);
        }

        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<OrderEntity> result = orderRepo.findAllByUserEntity_EmailOrderByDateCreated(email, pageable);

        return result.isEmpty() ? new ArrayList<>()
                : result.stream().map(this::createAllOrderResponse).toList();
    }

    private AllOrderResponse createAllOrderResponse(OrderEntity order) {
        DriverResponses.DriverResponsesBuilder driverResponseBuilder = createDriverResponseBuilder(order);

        return AllOrderResponse.builder()
                .trackingNum(order.getTrackingNumber())
                .orderDate(DateUtils.toDateString(order.getDateCreated()))
                .status(order.getStatus())
                .pickUpLocation(order.getDescriptionEntity().getPickUpLocation())
                .dropOffLocation(order.getDescriptionEntity().getDropOffLocation())
                .user(mapper.map(order.getUserEntity(), UserResponse.class))
                .driver(driverResponseBuilder.build())
                .build();
    }

    @Override
    public List<AllOrderResponse> searchByTrackingNum(String trackingNumber) {
        System.out.println("Debugging!");
        OrderEntity order = orderRepo.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new OrderNotFoundException("No order with tracking " + trackingNumber));

        System.out.println("Debugging 11!");

        DriverResponses.DriverResponsesBuilder driverResponseBuilder = createDriverResponseBuilder(order);

        return new ArrayList<>(List.of(AllOrderResponse.builder()
        .trackingNum(order.getTrackingNumber())
        .orderDate(DateUtils.toDateString(order.getDateCreated()))
        .status(order.getStatus())
        .pickUpLocation(order.getDescriptionEntity().getPickUpLocation())
        .dropOffLocation(order.getDescriptionEntity().getDropOffLocation())
        .user(mapper.map(order.getUserEntity(), UserResponse.class))
        .driver(driverResponseBuilder.build())
        .build()));
    }

    private DriverResponses.DriverResponsesBuilder createDriverResponseBuilder(OrderEntity order) {
        DriverResponses.DriverResponsesBuilder driverResponseBuilder = DriverResponses.builder();

        if (order.getTaskEntity() != null && order.getTaskEntity().getDriverEntity() != null) {
            driverResponseBuilder.id(order.getTaskEntity().getDriverEntity().getId())
                    .firstName(order.getTaskEntity().getDriverEntity().getFirstName())
                    .lastName(order.getTaskEntity().getDriverEntity().getLastName());
        }

        return driverResponseBuilder;
    }

    @Override
    public PaginatedResponse<AboutOrder> getAllUnAssignedOrder(Integer pageNo, Integer pageSize){
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Slice<OrderEntity> result = orderRepo.findAllByStatusOrStatusOrStatusOrderByIdAsc(OrderStatus.PENDING,
                                                                                            OrderStatus.ORDER_CONFIRMED,
                                                                                            OrderStatus.CANCELLED,
                                                                                            pageable);
        PaginatedResponse<AboutOrder> response = new PaginatedResponse<>();

        if (result.isEmpty()){
            return new PaginatedResponse<>();
        }

        response.setContent(result.stream().map((order)-> AboutOrder.builder()
                .orderId(order.getId())
                .orderStatus(String.valueOf(order.getStatus()))
                .senderName(order.getDescriptionEntity().getSenderName())
                .pickUpLocation(order.getDescriptionEntity().getPickUpLocation())
                .receiverName(order.getDescriptionEntity().getReceiverName())
                .dropOffLocation(order.getDescriptionEntity().getDropOffLocation())
                .imageURL(order.getUserEntity().getPictureUrl())
                .trackingNum(order.getTrackingNumber())
                .build()).toList());
        response.setPageNo(result.getNumber());
        response.setPageSize(result.getSize());
        response.setLast(result.isLast());

        return response;
    }
}
