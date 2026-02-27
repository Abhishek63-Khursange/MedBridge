package com.MedBridge.service;

import com.MedBridge.dao.AmbulanceBookingRepository;
import com.MedBridge.dao.AmbulanceRepository;
import com.MedBridge.dto.AmbulanceBookingDto;
import com.MedBridge.entity.Ambulance;
import com.MedBridge.entity.AmbulanceBooking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class AmbulanceService {

    @Autowired
    private AmbulanceBookingRepository bookingRepository;

    @Autowired
    private AmbulanceRepository ambulanceRepository;

    public AmbulanceBooking bookAmbulance(AmbulanceBookingDto bookingDto) {
        // Get available ambulance (for demo, just pick first available)
        List<Ambulance> ambulances = ambulanceRepository.findAll();
        if (ambulances.isEmpty()) {
            // Create a default ambulance if none exists
            Ambulance ambulance = new Ambulance();
            ambulance.setDriverName("Driver " + new Random().nextInt(100));
            ambulance.setPlateNumber("AMB-" + new Random().nextInt(10000));
            ambulance.setCurrentLatitude(28.6139); // Default Delhi coordinates
            ambulance.setCurrentLongitude(77.2090);
            ambulance = ambulanceRepository.save(ambulance);
            
            return createBooking(bookingDto, ambulance);
        } else {
            // Assign first available ambulance
            Ambulance ambulance = ambulances.get(0);
            return createBooking(bookingDto, ambulance);
        }
    }

    private AmbulanceBooking createBooking(AmbulanceBookingDto bookingDto, Ambulance ambulance) {
        AmbulanceBooking booking = new AmbulanceBooking();
        booking.setPatientName(bookingDto.getPatientName());
        booking.setContactNumber(bookingDto.getContactNumber());
        booking.setPickupLocation(bookingDto.getPickupLocation());
        booking.setDropLocation(bookingDto.getDropLocation());
        booking.setBookingTime(LocalDateTime.now());
        booking.setStatus("ASSIGNED");
        booking.setAmbulance(ambulance);
        
        return bookingRepository.save(booking);
    }

    public List<AmbulanceBooking> getAllBookings() {
        return bookingRepository.findAll();
    }
}
