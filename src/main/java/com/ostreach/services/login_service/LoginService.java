package com.ostreach.services.login_service;

import com.ostreach.payload.request.LoginRequest;
import com.ostreach.payload.response.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface LoginService {
    AuthResponse login(LoginRequest loginRequest, HttpServletRequest request);
    void logout();
}
