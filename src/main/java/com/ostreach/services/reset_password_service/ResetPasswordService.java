package com.ostreach.services.reset_password_service;

import com.ostreach.payload.request.ResetPasswordRequest;

public interface ResetPasswordService {
    String resetPasswordLink(String email);
    String validateResetPasswordEmail(String token);
    String updatePassword(ResetPasswordRequest resetPasswordRequest, String email);
}
