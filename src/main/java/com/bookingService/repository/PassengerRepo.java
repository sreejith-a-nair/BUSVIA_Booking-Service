package com.bookingService.repository;


import com.bookingService.entity.PassengerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PassengerRepo extends JpaRepository<PassengerEntity, UUID> {
}
