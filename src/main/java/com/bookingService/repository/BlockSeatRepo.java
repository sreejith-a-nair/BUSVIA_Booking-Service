package com.bookingService.repository;

import com.bookingService.entity.BlockSeat;
import com.bookingService.entity.BookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BlockSeatRepo extends JpaRepository<BlockSeat, UUID> {

    List<BlockSeat> findAllByBusId(UUID busUuid);
}
