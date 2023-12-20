package com.ostreach.services.driver_service.serviceImplementation;

import com.ostreach.entities.enums.TaskStatus;
import com.ostreach.entities.model.TaskEntity;
import com.ostreach.entities.model.UserEntity;
import com.ostreach.exceptions.TaskNotFoundException;
import com.ostreach.exceptions.UserNotFoundException;
import com.ostreach.repositories.TaskRepository;
import com.ostreach.repositories.UserRepository;
import com.ostreach.services.driver_service.DriverTaskChoice;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class DriverTaskChoiceImplementation implements DriverTaskChoice {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Override
    public String taskResponse(Long taskId, String status) {

        TaskEntity driverTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Driver task not found!"));

        driverTask.setTaskStatus(TaskStatus.valueOf(status.toUpperCase()));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        UserEntity driver = userRepository.findUserEntityByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Driver not found!"));
        driver.setDriverTaskEntities(new ArrayList<>(List.of(driverTask)));

        userRepository.save(driver);


        if (status.equalsIgnoreCase("ACCEPTED")) {
            return "Task has been accepted!";
        }
        return "Task has been rejected!";
    }
}
