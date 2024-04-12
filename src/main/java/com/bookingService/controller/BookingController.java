package com.bookingService.controller;

import com.bookingService.entity.BlockSeat;
import com.bookingService.entity.BookingEntity;
import com.bookingService.entity.TransactionDetails;
import com.bookingService.model.BlockSeatRequest;
import com.bookingService.model.BookingAllResponse;
import com.bookingService.model.BookingRequest;
import com.bookingService.model.BookingResponse;
import com.bookingService.service.BookingService;
import jakarta.ws.rs.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/booking")
public class BookingController {

    @Autowired
    BookingService bookingService;

    @PostMapping("/transaction")
    public  TransactionDetails bookBus(@RequestBody double cash){
        System.out.println("transaction cash  "+  cash );
        TransactionDetails transaction  =createTransaction(cash);
        System.out.println("transaction details in controller  CURRENCY  "+transaction.getCurrency()+" AMOUNT  : "+transaction.getAmount() +"  KEY :   "+  transaction.getKey() +"ORDER ID : "+transaction.getOrderId());
        return transaction;
    }

    @PostMapping("/bookBus")
    public  BookingResponse  bookBus(@RequestBody BookingRequest bookingRequest,
                                     @RequestParam String userMail){
        System.out.println("bookingRequest "+ bookingRequest);
        System.out.println("booking busId  "+ bookingRequest.getBusId());

          BookingResponse bookingEntity  =bookingService.bookSeats(bookingRequest,userMail);
            System.out.println("BOOKING DATA online  : " +bookingEntity);

        return bookingEntity;
    }

    @PostMapping("/bookBusWithWallet")
    public  BookingResponse  bookBusWithWallet(@RequestBody BookingRequest bookingRequest,
                                     @RequestParam String userMail){
        System.out.println("bookingRequest "+ bookingRequest);
        System.out.println("booking busId  "+ bookingRequest.getBusId());

        BookingResponse bookingEntity  =bookingService.bookSeats(bookingRequest,userMail);
        System.out.println("BOOKING DATA wallet  : " +bookingEntity);

        return bookingEntity;
    }



    @PostMapping("/createTransaction/{amount}")
    public TransactionDetails createTransaction(@PathVariable(name = "amount") Double amount) {
        System.out.println("Test 1");
        return bookingService.createTransaction(amount);
    }

    @GetMapping("/bookingInvoice")
    public ResponseEntity<byte[]> getInvoice(@RequestParam(name = "bookingId") String bookingId) throws Exception {
        System.out.println("Selected Format:2 "+bookingId);
        UUID  bookingUuid = UUID.fromString(bookingId);
        System.out.println("Selected Format:2 "+bookingId);
        Optional<BookingEntity> booking =bookingService.findBookingById(bookingUuid);
        BookingEntity bookingInfo =booking.get();
        System.out.println("Selected Format:2 " + bookingInfo);
        byte[] reportBytes;
        String contentType;
        String filename;
        reportBytes = bookingService.getBookingInvoice(bookingInfo);

        contentType = MediaType.APPLICATION_PDF_VALUE;
        filename = "Invoice.pdf";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentDispositionFormData("attachment", filename);
        System.out.println("Header *  *  *  "+headers);
        return new ResponseEntity<>(reportBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/updatedSeatAfterBooking")
    public int[] updatedSeatAfterBooking(@RequestParam("busId") String busId) {
        UUID uuid = UUID.fromString(busId);
        System.out.println("bus id in update sesats" + uuid);
        return bookingService.updateSeatsAfterBooking(uuid);
    }
    @GetMapping("/updateSeatsAfterBlockSeat")
    public int[] updateSeatsAfterBlockSeat(@RequestParam("busId") String busId) {
        UUID uuid = UUID.fromString(busId);
        System.out.println("bus id in update  Block BusSeat UPDATE " + uuid);
        return bookingService.updateSeatsAfterBlockSeat(uuid);
    }

    @GetMapping("/getAllBooking")
    public List<BookingEntity> getAllBooking(){
        return bookingService.getALlBooking();
    }

    @GetMapping("/getBookingByUserMail")
    public List<BookingAllResponse> getBookingByUserMail(@RequestParam("userMail") String userMail) {
        List<BookingAllResponse> bookingResponse = bookingService.getBookingByUserMail(userMail);
        System.out.println("booking details " + bookingResponse);
        return bookingResponse;
    }
    @GetMapping("/getBookingByBusId")
    public List<BookingAllResponse> getBookingByBusId(@RequestParam("busId") String busId) {
        List<BookingAllResponse> bookingResponse = bookingService.getBookingByBusId(busId);
        System.out.println("booking details " + bookingResponse);
        return bookingResponse;
    }

    @DeleteMapping("/cancelBooking/{bookingId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String bookingId) {
        UUID bookingUuid = UUID.fromString(bookingId);
        BookingEntity bookingEntity = bookingService.findBookingById(bookingUuid)
                .orElseThrow(() -> new NotFoundException("Booking not found"));
        bookingEntity.setStatus(true);
        System.out.println("CANCELLED BOOKING  TRUE : "+bookingEntity);
        bookingEntity.setStatus(false);
        bookingService.updateBookingAfterCancel(bookingEntity);
        System.out.println("CANCELLED BOOKING   FALSE : "+bookingEntity);
        bookingService.cancelBooking(bookingUuid);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/blockSeat")
    public BlockSeat blockSeat(@RequestBody BlockSeatRequest bookingRequest,
                               @RequestParam String userMail) {
        System.out.println("blockSeat req " + bookingRequest);
        System.out.println("blockSeat busId  " + bookingRequest.getBusId());


        BlockSeat bookingEntity = bookingService.blockSeats(bookingRequest, userMail);
        System.out.println("BLOCK seat DATA : " + bookingEntity);

        return bookingEntity;
    }



    @PostMapping("/sendTicket")
    public ResponseEntity<?> sendTicketToEmail(@RequestParam("bookingId") String bookingId)throws Exception {
        try {
            System.out.println("Send booking ticket to email " + bookingId);
            BookingEntity booking = bookingService.sendTicketToEmail(bookingId);
            System.out.println("Controller success: " + booking);
            return ResponseEntity.ok(booking);
        } catch (RuntimeException ex) {
            System.out.println("Failed to send ticket email: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send ticket email: " + ex.getMessage());
        }
    }

    @GetMapping("/getBookingById")
    public BookingEntity getBookingById(@RequestParam String bookingId) {
        System.out.println("Get booking from booking success ");
        UUID bookingUuid = UUID.fromString(bookingId);
        return bookingService.findBookingById(bookingUuid).orElseThrow(()-> new RuntimeException("Booking not found"));
    }
}
