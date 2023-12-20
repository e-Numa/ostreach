package com.ostreach.services.user_service;

import com.ostreach.payload.response.LocationResponse;

public interface LocationServices {
   LocationResponse getTrackingLocation(String trackingNum);
}
