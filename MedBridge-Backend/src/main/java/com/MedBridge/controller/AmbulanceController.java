package com.MedBridge.controller;

import com.MedBridge.dto.AmbulanceBookingDto;
import com.MedBridge.entity.AmbulanceBooking;
import com.MedBridge.service.AmbulanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ambulance")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class AmbulanceController {

    private static final Logger LOG = LoggerFactory.getLogger(AmbulanceController.class);

    @Autowired
    private AmbulanceService ambulanceService;

    @PostMapping("/book")
    public ResponseEntity<?> bookAmbulance(@RequestBody AmbulanceBookingDto bookingDto) {
        LOG.info("Received request for ambulance booking");
        
        try {
            AmbulanceBooking booking = ambulanceService.bookAmbulance(bookingDto);
            LOG.info("Ambulance booked successfully with booking ID: " + booking.getId());
            
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            LOG.error("Error booking ambulance: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to book ambulance: " + e.getMessage());
        }
    }

    @GetMapping("/patient/bookings")
    public ResponseEntity<?> getPatientBookings() {
        LOG.info("Received request to get patient ambulance bookings");
        
        try {
            List<AmbulanceBooking> bookings = ambulanceService.getAllBookings();
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            LOG.error("Error getting patient bookings: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to get bookings: " + e.getMessage());
        }
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<?> getBookingById(@PathVariable Long bookingId) {
        LOG.info("Received request to get booking details for ID: " + bookingId);
        
        try {
            // For now, return a simple response since we don't have getBookingById method
            return ResponseEntity.ok("Booking details for ID: " + bookingId);
        } catch (Exception e) {
            LOG.error("Error getting booking details: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to get booking details");
        }
    }
}
