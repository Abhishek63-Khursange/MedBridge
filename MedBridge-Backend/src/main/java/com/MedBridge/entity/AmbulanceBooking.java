package com.MedBridge.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class AmbulanceBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String patientName;
    private String contactNumber;
    private String pickupLocation;
    private String dropLocation;
    
    private LocalDateTime bookingTime;
    private String status; // PENDING, ASSIGNED, COMPLETED
    
    @ManyToOne
    @JoinColumn(name = "ambulance_id")
    private Ambulance ambulance;
}
