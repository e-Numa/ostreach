package com.ostreach.utils.events;

import com.ostreach.entities.enums.Roles;
import com.ostreach.entities.model.UserEntity;
import com.ostreach.exceptions.EmailNotSentException;
import com.ostreach.securities.JWTGenerator;
import com.ostreach.services.email_service.EmailServices;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

@Log4j2
@Component
@RequiredArgsConstructor
public class RegistrationCompleteEventListener implements ApplicationListener<RegistrationCompleteEvent> {

    private final JWTGenerator jwtGenerator;
    private final EmailServices emailServices;
    private UserEntity theUser;

    @Value("${frontend.url}")
    private String frontEndUrl;

    @Override
    public void onApplicationEvent(RegistrationCompleteEvent event) {
        theUser = event.getUser();
        String verificationToken = jwtGenerator.generateSignupToken(theUser.getEmail(), 5L);
        String url = frontEndUrl+ "/registration/email_verification?token=" + verificationToken;

        new Thread(() -> {
            try {
                if (Roles.ADMIN.equals(theUser.getRoles())){
                    sendVerificationEmail(url, "ostreachlogistics.ng@gmail.com");
                    return;
                }
                sendVerificationEmail(url, theUser.getEmail());
            } catch (MessagingException | UnsupportedEncodingException e) {
                throw new EmailNotSentException("Email not sent!");
            }
        }).start();

        log.info("Click the link to verify your registration :  {}", url);
    }

    private void sendVerificationEmail(String url, String email) throws MessagingException,
            UnsupportedEncodingException {
        String subject = "Email Verification";
        String senderName = "Ostreach";
        String mailContent = "<p> Hi, " + theUser.getFirstName() + ", </p>" +
                "<p>Thank you for registering with us." + "\n" +
                "Please, follow the link below to complete your registration. " +
                "\nThis link <strong> expires in 5 minute</strong>.</p>" +
                "<a href=\"" + url + "\">Verify your email to activate your account</a>" +
                "<p> Thank you <br> Ostreach Portal Service";

        emailServices.sendSimpleMessage(email, subject, mailContent, senderName);
    }
}
