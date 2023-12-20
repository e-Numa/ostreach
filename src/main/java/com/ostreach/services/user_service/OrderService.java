package com.ostreach.services.user_service;

import com.ostreach.payload.request.DescriptionRequest;
import com.ostreach.payload.response.PaymentRequest;
import com.ostreach.payload.response.TransactionResponse;
import com.ostreach.payload.response.UserOrderPage;
import com.ostreach.payload.response.UserOverView;

public interface OrderService {
    TransactionResponse getQuotation(DescriptionRequest descriptionRequest);
    TransactionResponse createOrder(DescriptionRequest descriptionRequest);
    String cancelOder(Long orderId);
    String validatePayment(PaymentRequest paymentResponse);
    UserOrderPage getOrderHistory(Integer pageNo, Integer pageSize);
    UserOverView getUserOverView();
}
