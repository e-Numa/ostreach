package com.ostreach.services.verification_services;

public interface VerificationServices {
    String verifyUserEmail(String token);
    void re_sendVerificationEmail(String email);
}
