package com.ostreach.services.notification_service;

import com.ostreach.entities.model.NotificationEntity;
import com.ostreach.payload.response.NotificationResponse;
import org.springframework.data.domain.Page;

public interface NotificationService {
    void sendNotification(String message, Long userId);
    Page<NotificationResponse> findAllNotificationsSentToAUserUsingPagingAndSorting(String email, int offset,
                                                                                    int pageSize, String field);
    NotificationResponse mapToResponse(NotificationEntity entity);
}
