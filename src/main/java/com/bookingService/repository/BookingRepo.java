package com.bookingService.repository;

import com.bookingService.entity.BlockSeat;
import com.bookingService.entity.BookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
@Repository
public interface BookingRepo extends JpaRepository<BookingEntity , UUID> {


    List<BookingEntity> findAllByBusId(UUID busUuid);



    List<BookingEntity> findAllByUserMail(String userMail);

//    List<BookingEntity> findAllByUserMail(String userMail);
    List<BookingEntity> getBookingByUserMail(String userMail);

}
