package com.ostreach.services.reset_password_service.serviceImplementation;

import com.ostreach.entities.model.UserEntity;
import com.ostreach.exceptions.*;
import com.ostreach.payload.request.ResetPasswordRequest;
import com.ostreach.repositories.UserRepository;
import com.ostreach.securities.JWTGenerator;
import com.ostreach.services.email_service.EmailServices;
import com.ostreach.services.notification_service.NotificationService;
import com.ostreach.services.reset_password_service.ResetPasswordService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@RequiredArgsConstructor
@Service
public class ResetPasswordImpl implements ResetPasswordService {
    private final UserRepository userRepo;
    private final JWTGenerator jwtGenerator;
    private final PasswordEncoder encoder;
    private final EmailServices emailServices;
    private final NotificationService notificationService;
    @Override
    public String resetPasswordLink(String email) {
        String token = jwtGenerator.generateSignupToken(email, 5L);

        String url = "http://localhost:2006/reset_password/email_verification?token="+token;

        String subject = "Reset Password";
        String senderName = "Ostreach";
        String mailContent =
                "<p>Click on the link below to reset password." +
                        "\nThis link <strong> expires in 5 minutes</strong>.</p>"+
                "<a href=\"" +url+ "\">Reset Password</a>" +
                        "<p> Thank you <br> Users Registration Portal Service</p>";
        new Thread(() -> {
            try {
                emailServices.sendSimpleMessage(email, subject, mailContent, senderName);
            } catch (MessagingException | UnsupportedEncodingException e) {
                throw new EmailNotSentException("Email not sent!");
            }
        }).start();

        return "Check email for password reset link.";
    }

    @Override
    public String validateResetPasswordEmail(String token) {
        if (!jwtGenerator.validateToken(token)){
            throw new TokenExpirationException("Token has expired!");
        }

        return jwtGenerator.getEmailFromJWT(token);
    }

    @Override
    public String updatePassword(ResetPasswordRequest resetPasswordRequest, String email) {

        UserEntity user = userRepo.findUserEntityByEmail(email)
                .orElseThrow(()-> new UserNotFoundException("User not found!"));

        if (!resetPasswordRequest.getNewPassword().equals(resetPasswordRequest.getConfirmPassword())){
            throw new PasswordMismatchException("Password mismatched!");
        }

        if(encoder.matches(resetPasswordRequest.getNewPassword(), user.getPassword())){
            throw new NewAndOldPasswordException("The new and old password should not be same!");
        }

        user.setPassword(encoder.encode(resetPasswordRequest.getNewPassword()));

        UserEntity savedUser =  userRepo.save(user);
        notificationService.sendNotification("Your password was changed successfully",savedUser.getId());;
        return "Password reset successfully!";
    }
}