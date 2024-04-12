package com.bookingService.service;

import com.bookingService.entity.BlockSeat;
import com.bookingService.entity.BookingEntity;
import com.bookingService.entity.TransactionDetails;
import com.bookingService.model.BlockSeatRequest;
import com.bookingService.model.BookingAllResponse;
import com.bookingService.model.BookingRequest;
import com.bookingService.model.BookingResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public interface BookingService {

    TransactionDetails createTransaction(Double amount);
    TransactionDetails prepareTransactionDetails(com.razorpay.Order order);

    BookingResponse bookSeats(BookingRequest bookingRequest,String userMail);

    BlockSeat blockSeats(BlockSeatRequest bookingRequest, String userMail);

    Optional<BookingEntity> findBookingById(UUID savedBookingUuid);

    byte[] getBookingInvoice(BookingEntity bookingInfo) throws Exception;

    int[] updateSeatsAfterBooking(UUID uuid);

    int[] updateSeatsAfterBlockSeat(UUID busUuid);

    List<BookingEntity> getALlBooking();

    List<BookingAllResponse> getBookingByUserMail(String userMail);

    List<BookingAllResponse> getBookingByBusId(String busId);

    void cancelBooking(UUID bookingId);

    void updateBookingAfterCancel(BookingEntity bookingEntity);

    BookingEntity sendTicketToEmail(String bookingId) throws Exception;


//    boolean sendMail(String toEmail, String subject, String body);




//    List<BookingEntity> getBookingByUserMail(String userMail);

}
