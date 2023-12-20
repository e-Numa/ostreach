package com.ostreach.services.driver_service;

import com.ostreach.entities.model.OrderEntity;
import com.ostreach.entities.model.TaskEntity;
import com.ostreach.entities.model.UserEntity;
import com.ostreach.payload.response.AboutOrder;
import com.ostreach.payload.response.OrderHistory;
import com.ostreach.payload.response.UserOrderPage;

import java.util.List;

public interface DriverService {
    void generateRandomOrder();
    void assignTaskToDriver(List<UserEntity> driverList, OrderEntity order);
    List<TaskEntity> viewAllOrdersInDriversTask();
    UserOrderPage pageOrders(Integer pageNo, Integer pageSize);
    OrderHistory getDriverOrderHistory(Integer pageNo, Integer pageSize);
    AboutOrder findTrack(String trackingNum);
    String updateOrderStatus(String trackingNum, String status);
}
