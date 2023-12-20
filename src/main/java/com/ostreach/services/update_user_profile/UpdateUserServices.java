package com.ostreach.services.update_user_profile;

import com.ostreach.payload.request.ChangeAddressRequest;
import com.ostreach.payload.request.ChangePasswordRequest;
import com.ostreach.payload.response.UserResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UpdateUserServices {
    String uploadPicture(MultipartFile multipartFile) throws IOException;
    String updateAddress(ChangeAddressRequest request);
    String updatePassword(ChangePasswordRequest request);
    UserResponse getUser();
}
