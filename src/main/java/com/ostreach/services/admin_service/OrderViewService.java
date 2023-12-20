package com.ostreach.services.admin_service;

import com.ostreach.payload.response.AboutOrder;
import com.ostreach.payload.response.AllOrderResponse;
import com.ostreach.payload.response.GeneralOrderResponse;
import com.ostreach.payload.response.PaginatedResponse;

import java.util.List;

public interface OrderViewService {
    List<AllOrderResponse> searchByEmail(String searchMethod, int pageNo, int pageSize);
    List<AllOrderResponse> searchByTrackingNum(String trackingNum);
    GeneralOrderResponse getAllOrders(int pageNo, int pageSize);
    PaginatedResponse<AboutOrder> getAllUnAssignedOrder(Integer pageNo, Integer pageSize);
}
