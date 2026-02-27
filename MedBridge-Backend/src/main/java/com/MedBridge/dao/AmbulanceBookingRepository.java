package com.MedBridge.dao;

import com.MedBridge.entity.AmbulanceBooking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AmbulanceBookingRepository extends JpaRepository<AmbulanceBooking, Long> {
}
