package com.ostreach.services.driver_service.serviceImplementation;

import com.ostreach.entities.enums.OrderStatus;
import com.ostreach.entities.model.OrderEntity;
import com.ostreach.entities.model.TaskEntity;
import com.ostreach.entities.model.TrackingEntity;
import com.ostreach.entities.model.UserEntity;
import com.ostreach.exceptions.OrderNotFoundException;
import com.ostreach.exceptions.TaskNotFoundException;
import com.ostreach.exceptions.UserNotFoundException;
import com.ostreach.kafka.KafkaProducer;
import com.ostreach.payload.response.*;
import com.ostreach.repositories.DescriptionRepository;
import com.ostreach.repositories.OrderRepository;
import com.ostreach.repositories.TaskRepository;
import com.ostreach.repositories.UserRepository;
import com.ostreach.services.driver_service.DriverService;
import com.ostreach.services.driver_service.DriverTaskChoice;
import com.ostreach.services.notification_service.serviceimplementation.NotificationServiceImplementation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

import static com.ostreach.entities.enums.DriverStatus.AVAILABLE;
import static com.ostreach.entities.enums.DriverStatus.UNAVAILABLE;
import static com.ostreach.entities.enums.OrderStatus.ORDER_CONFIRMED;
import static com.ostreach.entities.enums.Roles.ADMIN;
import static com.ostreach.entities.enums.Roles.DRIVER;
import static com.ostreach.entities.enums.TaskStatus.REJECTED;

@Service
@Slf4j
@RequiredArgsConstructor
public class DriverServiceImplementation implements DriverService {
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final TaskRepository taskRepository;
    private final KafkaProducer kafkaProducer;
    private final DriverTaskChoice driverTaskChoice;
    private final DescriptionRepository descriptionRepository;
    private final NotificationServiceImplementation notificationService;

    @Transactional
    @Override
    public void generateRandomOrder() {
        List<OrderEntity> allConfirmedOrders =
                orderRepository.findAllByStatusAndTaskEntityIsNull(ORDER_CONFIRMED);
        List<UserEntity> drivers = userRepository.findByRolesAndDriverStatus(DRIVER, AVAILABLE);
        Random random = new Random();
        OrderEntity customerOrder = (allConfirmedOrders.size() == 1) ? allConfirmedOrders.get(0) :
                allConfirmedOrders.get(random.nextInt(0, allConfirmedOrders.size() - 1));

        if (!drivers.isEmpty()) {
            assignTaskToDriver(drivers, customerOrder);
        } else {
            String customerMessage = kafkaProducer.sendUnavailableMessage();
            notificationService.sendNotification(customerMessage, customerOrder.getUserEntity().getId());

        }
    }

    @Transactional
    @Override
    public void assignTaskToDriver(List<UserEntity> driverList, OrderEntity order) {
        new Thread(() -> {
            Random random = new Random();
            long startTime = System.currentTimeMillis();
            long timeLimit = 5 * 60 * 1000;


            UserEntity driver = (driverList.size() == 1) ? driverList.get(0) :
                    driverList.get(random.nextInt(0, driverList.size() - 1));

            driverList.remove(driver);

            TaskEntity task = new TaskEntity();
            task.addOrder(order);
            driver.addToDriverTask(task);
            driver.setDriverStatus(UNAVAILABLE);
            UserEntity savedDriver = userRepository.save(driver);

            savedDriver.setDriverTaskEntities(List.of(task));

            String driverMessage = kafkaProducer.sendTaskDetailsMessageToAssignedDriver(savedDriver.getEmail(),
                    savedDriver.getDriverTaskEntities().get(0).getId());

            notificationService.sendNotification(driverMessage, savedDriver.getId());

            while (System.currentTimeMillis() - startTime <= timeLimit) {
            }

            Long taskId = savedDriver.getDriverTaskEntities().get(0).getId();
            TaskEntity driverTask = taskRepository.findById(taskId)
                    .orElseThrow(() -> new TaskNotFoundException("Task not found!"));
            if (driverTask.getTaskStatus() == null) {
                driverTask.setTaskStatus(REJECTED);
                taskRepository.save(driverTask);

                if (!driverList.isEmpty()) {
                    assignTaskToDriver(driverList, order);
                }
            }
            if (driverTask.getTaskStatus().equals(REJECTED) && !driverList.isEmpty()) {
                assignTaskToDriver(driverList, order);
                List<UserEntity> admins = userRepository.findAllByRoles(ADMIN);
                admins
                        .forEach(admin -> notificationService
                                .sendNotification("A driver with name " + savedDriver
                                        .getFirstName() + " rejected a task assigned to him", admin
                                        .getId()));
            } else if (driverTask.getTaskStatus().equals(REJECTED)) {
                List<UserEntity> admins = userRepository.findAllByRoles(ADMIN);
                admins
                        .forEach(admin -> notificationService
                                .sendNotification("A driver with name " + savedDriver
                                        .getFirstName() + " rejected a task assigned to him", admin
                                        .getId()));
                String customerMessage = kafkaProducer.sendUnavailableMessage();
                notificationService.sendNotification(customerMessage, order.getUserEntity().getId());
                // TODO: tell the user that no driver is available at the moment . And notify the management via email
            } else {
                OrderEntity orderEntity = orderRepository.findOrderEntityById(order.getId())
                        .orElseThrow(() -> new OrderNotFoundException("Order not found!"));
                orderEntity.addTrackingLocation(TrackingEntity.builder().location(order.getDescriptionEntity().getPickUpLocation()).build());

                List<UserEntity> admins = userRepository.findAllByRoles(ADMIN);
                admins
                        .forEach(admin -> notificationService
                                .sendNotification("A driver with name " + savedDriver
                                        .getFirstName() + " has accepted a task assigned to him", admin
                                        .getId()));
                String customerMessage = kafkaProducer.sendAvailableMessage();
                String message = savedDriver.getFirstName() + " is en route to pick up your order ";
                notificationService.sendNotification(customerMessage, order.getUserEntity().getId());
                notificationService.sendNotification(message, order.getUserEntity().getId());

                log.info("Task id:------------->" + savedDriver.getDriverTaskEntities().get(0).getId());
                log.info("Driver email:----------->" + driver.getEmail());
            }
        }).start();
    }

    @Override
    public List<TaskEntity> viewAllOrdersInDriversTask() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        UserEntity driver = userRepository.findUserEntityByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Driver not found!"));

        return driver.getDriverTaskEntities();
    }

    @Transactional
    @Override
    public UserOrderPage pageOrders(Integer pageNo, Integer pageSize) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("dateCreated").ascending());

        Slice<TaskEntity> result = taskRepository.findAllByDriverEntityEmailAndTaskStatusIsNull(email, pageable);

        return result == null ? new UserOrderPage() :
                UserOrderPage.builder()
                        .pageNo(result.getNumber())
                        .pageSize(result.getSize())
                        .lastPage(result.isLast())
                        .orderResponseList(result.get().map((task) -> {
                                    if (task.getOrderEntity().isEmpty()) {
                                        return null;
                                    }
                                    return UserOrderResponse.builder()
                                            .orderId(task.getId())
                                            .status(task.getOrderEntity().get(0).getStatus())
                                            .receiver(task.getOrderEntity().get(0).getDescriptionEntity().getReceiverName())
                                            .dropOffLocation(task.getOrderEntity().get(0).getDescriptionEntity().getDropOffLocation())
                                            .sender(task.getOrderEntity().get(0).getDescriptionEntity().getSenderName())
                                            .pickUpLocation(task.getOrderEntity().get(0).getDescriptionEntity().getPickUpLocation())
                                            .trackingNum(task.getOrderEntity().get(0).getTrackingNumber())
                                            .imageURL(task.getOrderEntity().get(0).getUserEntity().getPictureUrl())
                                            .build();
                                })
                                .toList())
                        .build();
    }

    @Override
    public OrderHistory getDriverOrderHistory(Integer pageNo, Integer pageSize) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("dateCreated").ascending());

        Slice<TaskEntity> taskEntities = taskRepository.findByDriverEntityEmailAndTaskStatusIsNotNullOrderByIdDesc(email, pageable);


        return OrderHistory.builder()
                .pageSize(taskEntities.getSize())
                .pageNo(taskEntities.getNumber())
                .lastPage(taskEntities.isLast())
                .aboutOrderList(taskEntities.stream().map((task) -> {
                            if (task.getOrderEntity().isEmpty() || task.getOrderEntity().get(0).getDescriptionEntity() == null) {
                                return null;
                            }
                            return AboutOrder.builder()
                                    .orderId(task.getOrderEntity().get(0).getId())
                                    .orderStatus(String.valueOf(task.getOrderEntity().get(0).getStatus()))
                                    .itemCategory(String.valueOf(task.getOrderEntity().get(0).getDescriptionEntity().getItemCategory()))
                                    .email(task.getOrderEntity().get(0).getUserEntity().getEmail())
                                    .address(task.getOrderEntity().get(0).getUserEntity().getAddress())
                                    .deliveryPrice(task.getOrderEntity().get(0).getDeliveryCost())
                                    .firstName(task.getOrderEntity().get(0).getUserEntity().getFirstName())
                                    .lastName(task.getOrderEntity().get(0).getUserEntity().getLastName())
                                    .phoneNum(task.getOrderEntity().get(0).getUserEntity().getPhoneNumber())
                                    .imageURL(task.getOrderEntity().get(0).getUserEntity().getPictureUrl())
                                    .itemName(task.getOrderEntity().get(0).getDescriptionEntity().getItemName())
                                    .dropOffLocation(task.getOrderEntity().get(0).getDescriptionEntity().getDropOffLocation())
                                    .pickUpLocation(task.getOrderEntity().get(0).getDescriptionEntity().getPickUpLocation())
                                    .senderName(task.getOrderEntity().get(0).getDescriptionEntity().getSenderName())
                                    .receiverName(task.getOrderEntity().get(0).getDescriptionEntity().getReceiverName())
                                    .receiverPhone(task.getOrderEntity().get(0).getDescriptionEntity().getReceiverPhone())
                                    .trackingNum(task.getOrderEntity().get(0).getTrackingNumber())
                                    .locationList(task.getOrderEntity().get(0).getTrackingLocationEntities().stream()
                                            .map((location) -> TrackingResponse.builder()
                                                    .locationId(location.getId())
                                                    .dateTime(String.valueOf(location.getDateCreated()))
                                                    .location(location.getLocation())
                                                    .build())
                                            .toList())
                                    .build();
                        })

                        .toList())
                .build();
    }

    @Override
    public AboutOrder findTrack(String trackingNum) {
        OrderEntity order = orderRepository.findByTrackingNumber(trackingNum)
                .orElseThrow(() -> new OrderNotFoundException("Order not found!"));

        return AboutOrder.builder()
                .orderId(order.getId())
                .orderStatus(String.valueOf(order.getStatus()))
                .itemCategory(String.valueOf(order.getDescriptionEntity().getItemCategory()))
                .email(order.getUserEntity().getEmail())
                .address(order.getUserEntity().getAddress())
                .deliveryPrice(order.getDeliveryCost())
                .firstName(order.getUserEntity().getFirstName())
                .lastName(order.getUserEntity().getLastName())
                .phoneNum(order.getUserEntity().getPhoneNumber())
                .imageURL(order.getUserEntity().getPictureUrl())
                .itemName(order.getDescriptionEntity().getItemName())
                .dropOffLocation(order.getDescriptionEntity().getDropOffLocation())
                .pickUpLocation(order.getDescriptionEntity().getPickUpLocation())
                .senderName(order.getDescriptionEntity().getSenderName())
                .receiverName(order.getDescriptionEntity().getReceiverName())
                .receiverPhone(order.getDescriptionEntity().getReceiverPhone())
                .trackingNum(order.getTrackingNumber())
                .locationList(order.getTrackingLocationEntities().stream()
                        .map((location) -> TrackingResponse.builder()
                                .locationId(location.getId())
                                .dateTime(String.valueOf(location.getDateCreated()))
                                .location(location.getLocation())
                                .build())
                        .toList())
                .build();
    }

    @Transactional
    @Override
    public String updateOrderStatus(String trackingNum, String status) {
        OrderEntity order = orderRepository.findByTrackingNumber(trackingNum)
                .orElseThrow(() -> new OrderNotFoundException("Order not found!"));

        order.setStatus(OrderStatus.valueOf(status.toUpperCase()));

        orderRepository.save(order);

        return "Order status updated!";
    }
}
