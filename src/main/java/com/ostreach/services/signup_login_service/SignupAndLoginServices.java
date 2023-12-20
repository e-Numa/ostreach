package com.ostreach.services.signup_login_service;

import com.ostreach.payload.request.DriverRequest;
import com.ostreach.payload.request.UserRequest;
import jakarta.servlet.http.HttpServletRequest;

public interface SignupAndLoginServices {
    String register(UserRequest userRequest, HttpServletRequest request);
    String driverRegister(DriverRequest driverRequest, HttpServletRequest request);
}
