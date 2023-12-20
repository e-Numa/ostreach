package com.ostreach.services.location_service.serviceImplementation;

import com.ostreach.entities.enums.OrderStatus;
import com.ostreach.entities.model.LocationEntity;
import com.ostreach.entities.model.OrderEntity;
import com.ostreach.entities.model.TrackingEntity;
import com.ostreach.exceptions.LocationNotFoundException;
import com.ostreach.exceptions.OrderNotFoundException;
import com.ostreach.payload.response.LocationResponse;
import com.ostreach.repositories.DescriptionRepository;
import com.ostreach.repositories.LocationRepository;
import com.ostreach.repositories.OrderRepository;
import com.ostreach.services.location_service.LocationService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {
    private final ModelMapper modelMapper = new ModelMapper();
    private final LocationRepository locationRepository;
    private final OrderRepository orderRepository;
    private final DescriptionRepository descriptionRepository;

    @Override
    public LocationResponse getLocationById(Long orderId) {
        OrderEntity order = orderRepository.findOrderEntityById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found!"));

        return LocationResponse.builder()
                .orderId(order.getId())
                .dropOffLocation(order.getDescriptionEntity().getDropOffLocation())
                .pickUpLocation(order.getDescriptionEntity().getPickUpLocation())
                .build();
    }

    @Override
    public void deleteLocation(Long locationId) {
        LocationEntity existingLocation = locationRepository.findById(locationId)
                .orElseThrow(() -> new LocationNotFoundException("The location with id: " +locationId+ " was not " +
                        "found"));

        locationRepository.delete(existingLocation);
    }

    @Override
    public String updateLocation(String location, Long locationId) {
        OrderEntity order = orderRepository.findById(locationId)
                .orElseThrow(()-> new OrderNotFoundException("Order not found!"));

        order.addTrackingLocation(TrackingEntity.builder()
                        .location(location)
                .build());

        if (order.getStatus() == OrderStatus.ORDER_CONFIRMED){
            order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
        }

        orderRepository.save(order);
        return "Location updated successfully!";
    }
}
