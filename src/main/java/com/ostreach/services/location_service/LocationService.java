package com.ostreach.services.location_service;

import com.ostreach.payload.response.LocationResponse;

public interface LocationService {
    LocationResponse getLocationById(Long orderId);
    void deleteLocation(Long locationId);

    String updateLocation(String location, Long orderId);
}
