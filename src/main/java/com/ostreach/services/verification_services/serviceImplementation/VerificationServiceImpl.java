package com.ostreach.services.verification_services.serviceImplementation;

import com.ostreach.entities.model.UserEntity;
import com.ostreach.exceptions.EmailNotSentException;
import com.ostreach.exceptions.TokenExpirationException;
import com.ostreach.exceptions.UserNotFoundException;
import com.ostreach.repositories.UserRepository;
import com.ostreach.securities.JWTGenerator;
import com.ostreach.services.email_service.EmailServices;
import com.ostreach.services.verification_services.VerificationServices;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@RequiredArgsConstructor
@Service
public class VerificationServiceImpl implements VerificationServices {
    private final JWTGenerator jwtGenerator;
    private final UserRepository userRepository;
    private final EmailServices emailServices;

    @Override
    public String verifyUserEmail(String token) {
        if (!jwtGenerator.validateToken(token)){
            throw new TokenExpirationException("Token has expired!");
        }

        String email = jwtGenerator.getEmailFromJWT(token);
        UserEntity user = userRepository.findUserEntityByEmail(email)
                .orElseThrow(()-> new UserNotFoundException("User not found!"));

        user.setIsVerified(true);

        userRepository.save(user); // TODO: Add a custom class containing username, email and successful verification message.

        return email;
    }

    @Override
    public void re_sendVerificationEmail(String email) {

        String verificationToken = jwtGenerator.generateSignupToken(email, 5L);
        String url = "http://localhost:2006/registration/email_verification?token="+verificationToken;

        String subject = "Email Verification";
        String senderName = "Ostreach";
        String mailContent = "<p>Please, follow the link below to complete your registration. \nThis link <strong> expires in 5 minute</strong>.</p>"+
                "<a href=\"" +url+ "\">Verify your email to activate your account</a>"+
                "<p> Thank you <br> Users Registration Portal Service.";
        new Thread(() -> {
            try {
                emailServices.sendSimpleMessage(email, subject, mailContent, senderName);
            } catch (MessagingException | UnsupportedEncodingException e) {
                throw new EmailNotSentException("Email not sent!");
            }
        }).start();
    }
}
