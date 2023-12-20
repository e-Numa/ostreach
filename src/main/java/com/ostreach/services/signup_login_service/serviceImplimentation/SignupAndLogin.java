package com.ostreach.services.signup_login_service.serviceImplimentation;

import com.ostreach.entities.enums.DriverStatus;
import com.ostreach.entities.enums.Gender;
import com.ostreach.entities.enums.Roles;
import com.ostreach.entities.model.UserEntity;
import com.ostreach.exceptions.DuplicateEmailException;
import com.ostreach.exceptions.PasswordMismatchException;
import com.ostreach.payload.request.DriverRequest;
import com.ostreach.payload.request.UserRequest;
import com.ostreach.repositories.UserRepository;
import com.ostreach.services.notification_service.NotificationService;
import com.ostreach.services.signup_login_service.SignupAndLoginServices;
import com.ostreach.utils.events.RegistrationCompleteEvent;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.ostreach.entities.enums.Roles.ADMIN;

@RequiredArgsConstructor
@Service
public class SignupAndLogin implements SignupAndLoginServices {
    private final UserRepository userRepos;
    private final PasswordEncoder encoder;
    private final ApplicationEventPublisher publisher;
    private final NotificationService notificationService;

    @Override
    public String register(UserRequest userRequest, HttpServletRequest request) {
        if (userRepos.existsByEmail(userRequest.getEmail().toLowerCase())) {
            throw new DuplicateEmailException("Email already exist!");
        }

        if (!userRequest.getPassword().equals(userRequest.getConfirmPassword())) {
            throw new PasswordMismatchException("Password mismatch!");
        }

        String path = request.getServletPath();

        UserEntity user = UserEntity.builder()
                .firstName(userRequest.getFirstName())
                .lastName(userRequest.getLastName())
                .email(userRequest.getEmail().toLowerCase())
                .password(encoder.encode(userRequest.getPassword()))
                .confirmPassword(userRequest.getConfirmPassword())
                .phoneNumber(userRequest.getPhoneNumber())
                .address(userRequest.getAddress())
                .gender(Gender.valueOf(userRequest.getGender().toUpperCase()))
                .dob(userRequest.getDob())
                .roles(
                        path.contains("users") ? Roles.USER : ADMIN
                )
                .isVerified(false)
                .build();
        userRepos.save(user);
        publisher.publishEvent(new RegistrationCompleteEvent(user, applicationUrl(request)));

        return "Verification link sent to Email. Check email and verify your account.";
    }

    @Override
    public String driverRegister(DriverRequest driverRequest, HttpServletRequest request) {
        if (userRepos.existsByEmail(driverRequest.getEmail().toLowerCase())) {
            throw new DuplicateEmailException("Email already exist!");
        }

        UserEntity user = UserEntity.builder()
                .firstName(driverRequest.getFirstName())
                .lastName(driverRequest.getLastName())
                .email(driverRequest.getEmail().toLowerCase())
                .phoneNumber(driverRequest.getPhoneNumber())
                .address(driverRequest.getAddress())
                .dob(driverRequest.getDob())
                .password(encoder.encode("111111"))
                .confirmPassword("111111")
                .gender(Gender.valueOf(driverRequest.getGender()))
                .roles(Roles.DRIVER)
                .isVerified(false)
                .driverStatus(DriverStatus.AVAILABLE)
                .build();

        userRepos.save(user);
        publisher.publishEvent(new RegistrationCompleteEvent(user, applicationUrl(request)));
        List<UserEntity> admins = userRepos.findAllByRoles(ADMIN);
        admins
                .forEach(admin -> notificationService
                        .sendNotification("A driver with name " + driverRequest
                                .getFirstName() + " was registered", admin
                                .getId()));
        return "Verification link sent to Email. Check email and verify your account.";
    }

    private String applicationUrl(HttpServletRequest request) {
        return "https://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    }
}
