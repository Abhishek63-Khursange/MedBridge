package com.MedBridge.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AmbulanceBookingDto {
    private String patientName;
    private String contactNumber;
    private String pickupLocation;
    private String dropLocation;
}
