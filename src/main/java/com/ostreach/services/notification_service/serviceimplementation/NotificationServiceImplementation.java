package com.ostreach.services.notification_service.serviceimplementation;

import com.ostreach.entities.model.NotificationEntity;
import com.ostreach.entities.model.UserEntity;
import com.ostreach.exceptions.UserNotFoundException;
import com.ostreach.payload.response.NotificationResponse;
import com.ostreach.repositories.NotificationRepository;
import com.ostreach.repositories.UserRepository;
import com.ostreach.services.notification_service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationServiceImplementation implements NotificationService {
    private final NotificationRepository notificationRepo;
    private final UserRepository userRepo;
    private final ModelMapper modelMapper = new ModelMapper();


    @Override
    public void sendNotification(String message, Long userId) {
        UserEntity user = userRepo.findById(userId).orElseThrow(() -> new UserNotFoundException("User was not found!"));
        NotificationEntity notificationMessage = new NotificationEntity();
        notificationMessage.setMessage(message);
        user.addToNotification(notificationMessage);
        userRepo.save(user);
    }

    @Override
    public NotificationResponse mapToResponse(NotificationEntity entity) {
        return modelMapper.map(entity, NotificationResponse.class);
    }
    @Override
    public Page<NotificationResponse> findAllNotificationsSentToAUserUsingPagingAndSorting(String email, int offset, int pageSize, String field) {
        Page<NotificationEntity> response =  notificationRepo
                .findAllByUsersEmail(email, PageRequest.of(offset,pageSize).withSort(Sort.by(Sort.Direction.DESC,field)));
        return response.map(this::mapToResponse);
    }
}
