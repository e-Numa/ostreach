package com.ostreach.services.user_service.serviceImplementation;

import com.ostreach.entities.model.OrderEntity;
import com.ostreach.exceptions.OrderNotFoundException;
import com.ostreach.payload.response.LocationResponse;
import com.ostreach.repositories.OrderRepository;
import com.ostreach.repositories.TrackingRepository;
import com.ostreach.services.user_service.LocationServices;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class LocationServiceImpl implements LocationServices {
    private final TrackingRepository trackingRepository;
    private final OrderRepository orderRepo;

    @Override
    public LocationResponse getTrackingLocation(String trackingNum){
        OrderEntity order = orderRepo.findByTrackingNumber(trackingNum)
                .orElseThrow(()-> new OrderNotFoundException("No other with tracking number: "+trackingNum));

        System.out.println("Order id is: "+order.getId());

        return LocationResponse.builder()
                .orderId(order.getId())
                .pickUpLocation(order.getDescriptionEntity().getPickUpLocation())
                .dropOffLocation(order.getDescriptionEntity().getDropOffLocation())
                .build();
    }
}
