package com.bookingService.service;

import com.bookingService.entity.BlockSeat;
import com.bookingService.entity.BookingEntity;
import com.bookingService.entity.PassengerEntity;
import com.bookingService.entity.TransactionDetails;
import com.bookingService.enums.PaymentOptions;
import com.bookingService.model.*;
import com.bookingService.repository.BlockSeatRepo;
import com.bookingService.repository.BookingRepo;
import com.bookingService.repository.PassengerRepo;
import com.bookingService.util.EmailUtil;
import com.razorpay.RazorpayClient;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.json.JSONObject;
import com.razorpay.RazorpayException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;


import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.rtf.RTFEditorKit;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.bookingService.enums.PaymentOptions.WALLET_PAYMENT;

@Service
public class BookingServiceImp implements BookingService {


    @Autowired
    JavaMailSender mailSender;
    @Autowired
    EmailUtil emailUtils;
    @Autowired
    PassengerRepo passengerRepo;
    @Autowired
    BookingRepo bookingRepo;

    @Autowired
    BlockSeatRepo blockSeatRepo;

    private static final String KEY = "rzp_test_krZbRdGwanRtve";

    private static final String KEY_SECRET = "vPwSnArNx1RcrRK9z4KfmJi9";

    private static final String CURRENCY = "INR";

    @Override
    public TransactionDetails createTransaction(Double amount) {
        try {
            int amountRazorpay = (int) (amount * 100);
            // Construct the JSON request
            JSONObject jsonObject = new JSONObject();
//            jsonObject.put("amount", amount * 100);
            jsonObject.put("amount", amountRazorpay);
//            jsonObject.put("currency", "INR");
            jsonObject.put("currency", CURRENCY);


            RazorpayClient razorpayClient = new RazorpayClient(KEY, KEY_SECRET);


            com.razorpay.Order order = razorpayClient.orders.create(jsonObject);


            TransactionDetails transactionDetails = prepareTransactionDetails(order);
            return transactionDetails;
        } catch (RazorpayException e) {

            e.printStackTrace();
            System.err.println("Razorpay error: " + e.getMessage());

        } catch (Exception e) {

            e.printStackTrace();

        }

        return null;
    }

    @Override
    public TransactionDetails prepareTransactionDetails(com.razorpay.Order order) {
        String orderId = order.get("id");
        String currency = order.get("currency");
        Integer amount = order.get("amount");

        TransactionDetails transactionDetails = new TransactionDetails(orderId, currency, amount, KEY);
        System.out.println("transactionDetails>>>>>>>>>in service..." + transactionDetails);
        return transactionDetails;
    }

        @Override
        public BookingResponse bookSeats(BookingRequest bookingReq,String userMail) {
            System.out.println("AUTHORITY EMAIL  "+bookingReq.getAuthorityEmail());
            System.out.println("Bus number   "+bookingReq.getBusNumber());

            String paymentModeString = bookingReq.getPaymentMode();
//            Optional<PaymentOptions> paymentMode = PaymentOptions.fromString(paymentModeString);
//
            if (PaymentOptions.WALLET_PAYMENT.equals(PaymentOptions.valueOf(bookingReq.getPaymentMode()))){
                System.out.println("WALLET_PAYMENT  *   *   *");

                        if (bookingReq != null && !bookingReq.getPassengers().isEmpty()) {
                            System.out.println("wallet amount "+bookingReq.getWalletAmount());
                            System.out.println("Booking  amount "+bookingReq.getWalletAmount());

                            if(bookingReq.getTotalFare()<bookingReq.getWalletAmount()) {
                                System.out.println("Wallet has balance payment success--------------------------");

                                LocalDate currentDate = LocalDate.now();
                                BookingEntity bookingInfo = new BookingEntity();
                                UUID busUuid = UUID.fromString(bookingReq.getBusId());
                                bookingInfo.setTotalFare(bookingReq.getTotalFare());
                                bookingInfo.setBusId(busUuid);
                                bookingInfo.setContact(bookingReq.getContact());
                                bookingInfo.setEmail(bookingReq.getEmail());
                                bookingInfo.setDeparturedate(bookingReq.getDeparturedate());
                                bookingInfo.setArrivaldate(bookingReq.getArrivaldate());
                                bookingInfo.setFromLocations(bookingReq.getFrom());
                                bookingInfo.setToLocations(bookingReq.getTo());
                                bookingInfo.setBusNumber(bookingReq.getBusNumber());
                                bookingInfo.setBookingDate(currentDate);
                                bookingInfo.setAuthorityEmail(bookingReq.getAuthorityEmail());
                                bookingInfo.setPaymentMode(bookingReq.getPaymentMode());
                                bookingInfo.setStatus(true);
                                bookingInfo.setUserMail(userMail);

                                List<PassengerEntity> passengers = new ArrayList<>();
                                int bookedSeatCount = 0;
                                for (PassengerList passengerData : bookingReq.getPassengers()) {
                                    PassengerEntity passengerEntity = new PassengerEntity();
                                    passengerEntity.setName(passengerData.getName());
                                    passengerEntity.setAge(passengerData.getAge());
                                    passengerEntity.setGender(passengerData.getGender());
                                    passengerEntity.setSeatNumber(passengerData.getSeatNumber());
                                    passengerEntity.setBooking(bookingInfo); // Set the reference to the booking
                                    passengers.add(passengerEntity);
                                    bookedSeatCount++;
                                }
                                System.out.println("Booked seatCount  * * *" + bookedSeatCount);


                                bookingInfo.setPassengers(passengers);
                                bookingInfo.setSelectedSeats(bookedSeatCount);

                                BookingEntity bookingData = bookingRepo.save(bookingInfo);

                                BookingResponse bookingResponse = new BookingResponse();
                                bookingResponse.setUuid(bookingData.getUuid());
                                bookingResponse.setTotalFare(bookingData.getTotalFare());
                                bookingResponse.setSelectedSeats(bookedSeatCount);
                                bookingResponse.setEmail(bookingData.getEmail());
                                bookingResponse.setContact(bookingData.getContact());
                                bookingResponse.setArrivaldate(bookingData.getArrivaldate());
                                bookingResponse.setDeparturedate(bookingData.getDeparturedate());
                                bookingResponse.setFromLocations(bookingData.getFromLocations());
                                bookingResponse.setToLocations(bookingData.getToLocations());
                                bookingResponse.setBusNumber(bookingData.getBusNumber());
                                bookingResponse.setBusId(bookingData.getBusId());
                                bookingResponse.setBookingDate(bookingData.getBookingDate());
                                bookingResponse.setAuthorityMail(bookingData.getAuthorityEmail());

                                return bookingResponse;
                            }else{
                                throw  new RuntimeException("Insufficient wallet balance ");
                            }
                        }
                        else {
                            throw new RuntimeException("booking request not valid or empty ");
                        }

            } else {

                System.out.println("ONLINE  PAYMENT *  *  * ");
                if (bookingReq != null && !bookingReq.getPassengers().isEmpty()) {


                    LocalDate currentDate = LocalDate.now();
                    BookingEntity bookingInfo = new BookingEntity();
                    UUID busUuid = UUID.fromString(bookingReq.getBusId());
                    bookingInfo.setTotalFare(bookingReq.getTotalFare());
                    bookingInfo.setBusId(busUuid);
                    bookingInfo.setContact(bookingReq.getContact());
                    bookingInfo.setEmail(bookingReq.getEmail());
                    bookingInfo.setDeparturedate(bookingReq.getDeparturedate());
                    bookingInfo.setArrivaldate(bookingReq.getArrivaldate());
                    bookingInfo.setFromLocations(bookingReq.getFrom());
                    bookingInfo.setToLocations(bookingReq.getTo());
                    bookingInfo.setBusNumber(bookingReq.getBusNumber());
                    bookingInfo.setBookingDate(currentDate);
                    bookingInfo.setAuthorityEmail(bookingReq.getAuthorityEmail());
                    bookingInfo.setPaymentMode(bookingReq.getPaymentMode());
                    bookingInfo.setStatus(true);
                    bookingInfo.setUserMail(userMail);

                    List<PassengerEntity> passengers = new ArrayList<>();
                    int bookedSeatCount = 0;
                    for (PassengerList passengerData : bookingReq.getPassengers()) {
                        PassengerEntity passengerEntity = new PassengerEntity();
                        passengerEntity.setName(passengerData.getName());
                        passengerEntity.setAge(passengerData.getAge());
                        passengerEntity.setGender(passengerData.getGender());
                        passengerEntity.setSeatNumber(passengerData.getSeatNumber());
                        passengerEntity.setBooking(bookingInfo); // Set the reference to the booking
                        passengers.add(passengerEntity);
                        bookedSeatCount++;
                    }
                    System.out.println("Booked seatCount  * * *" + bookedSeatCount);


                    bookingInfo.setPassengers(passengers);
                    bookingInfo.setSelectedSeats(bookedSeatCount);

                    BookingEntity bookingData = bookingRepo.save(bookingInfo);

                    BookingResponse bookingResponse = new BookingResponse();
                    bookingResponse.setUuid(bookingData.getUuid());
                    bookingResponse.setTotalFare(bookingData.getTotalFare());
                    bookingResponse.setSelectedSeats(bookedSeatCount);
                    bookingResponse.setEmail(bookingData.getEmail());
                    bookingResponse.setContact(bookingData.getContact());
                    bookingResponse.setArrivaldate(bookingData.getArrivaldate());
                    bookingResponse.setDeparturedate(bookingData.getDeparturedate());
                    bookingResponse.setFromLocations(bookingData.getFromLocations());
                    bookingResponse.setToLocations(bookingData.getToLocations());
                    bookingResponse.setBusNumber(bookingData.getBusNumber());
                    bookingResponse.setBusId(bookingData.getBusId());
                    bookingResponse.setBookingDate(bookingData.getBookingDate());
                    bookingResponse.setAuthorityMail(bookingData.getAuthorityEmail());


                    return bookingResponse;
                } else {
                    throw new RuntimeException("booking request not valid or empty ");
                }

            }
    }



    @Override
    public BlockSeat blockSeats(BlockSeatRequest blockReq, String userMail) {
        if (blockReq != null) {
            System.out.println("if");

            BlockSeat blockSeat = new BlockSeat();
            UUID busUuid = UUID.fromString(blockReq.getBusId());
            blockSeat.setBusId(busUuid);
            blockSeat.setStatus(true);


            List<PassengerEntity> passengers = new ArrayList<>();
            int bookedSeatCount = 0;
            System.out.println("size "+blockReq.getSelectedSeats().size());

            for (int i = 0; i < blockReq.getSelectedSeats().size(); i++) {
                PassengerEntity  passengerEntity = new PassengerEntity();
                passengerEntity.setName(userMail);
                passengerEntity.setSeatNumber(blockReq.getSelectedSeats().get(i));
                passengerEntity.setBlockSeat(blockSeat);
                System.out.println("value seat number "+blockReq.getSelectedSeats().get(i));
                passengers.add(passengerEntity);
                bookedSeatCount++;
//             PassengerEntity passenger =  passengerRepo.save(passengerEntity);
            }


            blockSeat.setPassengers(passengers);
            blockSeat.setSelectedSeats(bookedSeatCount);

            BlockSeat blockInfo = blockSeatRepo.save(blockSeat);
            System.out.println("BlockSeat data after set passenger"+blockInfo);

            return blockInfo;
        }
        throw new RuntimeException("Block seat  request not valid or empty ");
    }


    @Override
    public Optional<BookingEntity> findBookingById(UUID savedBookingUuid) {

        return bookingRepo.findById(savedBookingUuid);
    }

    @Override
    public byte[] getBookingInvoice(BookingEntity bookingInfo) throws Exception {
        List<PassengerEntity> passengers= bookingInfo.getPassengers();
            double grandTotal = 0.0;
            String email =bookingInfo.getEmail();
            String booking = "<html><body>";


        booking += "<h2 style='background-color:#2967D5; padding: 36px; color: #E2CDE4;font-weight:600; text-align: center;'>BusVia.com</h2>";



        booking += "<h4 style='background-color: #8893E4;color:white;text-align:center;width:100%:padding:20px;'>BOOKING INVOICE</h4>";
                    if (email != null) {

                        booking += "<div style='background-color: #2967D5;text-align:center;width:100%;height:40px;'>";
                            booking += "<h5 style='color:#E0D2EC;background-color: #2967D5;'> Contact Details </h5>";
                        booking += "<p style='background-color: #f9f9f9; border-radius: 5px; padding: 5px;'>Email : " + bookingInfo.getEmail() + "  "+" Contact : " + bookingInfo.getContact() + "</p>";
                        booking += "</div>";

                        int passengerCount = 0;


                        booking += "<div style='background-color: #2967D5;color:#E0D2EC;text-align:center;width:100%;height:20px;margin-top:20px;margin-bottom:20px;'>";
                        booking += "</div>";

                        booking += "<h5 style='padding:18px;margin-bottom:10px;text-align:center;'> INVOICE DETAILS </h5>";
                        booking += "<h5>Booking Details</h5>";
                        booking += "<table width='100%' border='1' cellpadding='8'>";
                        booking+="<tr style='background-color: #E9CE51; background-image: linear-gradient(180deg, #E9CE51 0%, #E9CE51 100%);'>";
                        booking+="<th style='color:#15694E; text-align: left;'>Passenger</th>";
                        booking+="<th style='color:#15694E; text-align: left;'>Age</th>";
                        booking+="<th style='color:#15694E; text-align: left;'>Gender</th>";
                        booking+="<th style='color:#15694E; text-align: left;'>Seat Number</th>";
                        booking+="<th style='color:#15694E; text-align: left;'>Date</th>";
                        booking+="</tr>";


                            for (PassengerEntity passenger : passengers) {
                                booking += "<tr  style='background-color: #EAFAEB;color:#2E270B;'>";

                                booking += "<td>" + passenger.getName() + "</td>";

                                booking += "<td>" + passenger.getAge() + "</td>";

                                booking += "<td>" + passenger.getGender() + "</td>";

                                booking += "<td>" + (passenger.getSeatNumber() + 1)+ "</td>";

                                booking += "<td>" + passenger.getBooking().getDeparturedate() + "</td>";

                                booking += "</tr>";
                            }
                        booking += "</table>";

                        booking += "<div style='background-color: #EAFAEB; width: 100%; padding: 14px; border-radius: 10px;color:#8893E4;margin-top:20px;'>";
                         booking += "<div style='background-color: #8893E4; color: white;font-weight:600px; padding: 9px; border-radius: 10px 10px 0 0; width:100%;'>TRIP DETAILS INVOICE DETAILS</div>";
                        booking += "<div style='padding: 10px;'>";
                        booking += "<h5 style='background-color: #f0f0f0; border-radius: 5px; padding: 5px;'>Bus Number:</h5>";
                        booking += "<p style='background-color: #f9f9f9; border-radius: 5px; padding: 5px;'>Bus Number : " + bookingInfo.getBusNumber() + "</p>";
                        booking += "<p style='background-color: #f9f9f9; border-radius: 5px; padding: 5px;'>From : " + bookingInfo.getFromLocations() + ", " + bookingInfo.getDeparturedate() + "</p>";
                        booking += "<p style='background-color: #f9f9f9; border-radius: 5px; padding: 5px;'>To : " + bookingInfo.getToLocations() + ", " + bookingInfo.getArrivaldate() + "</p>";
                        booking += "<p style='background-color: #f9f9f9; border-radius: 5px; padding: 5px;'>Total Fare: " + bookingInfo.getTotalFare() + "</p>";
                        booking += "</div>";
                        booking += "</div>";
                    }
        booking += "<h3>"+"Total Fare " + " :  &#8377;"+bookingInfo.getTotalFare()+"</h3>";
        booking += "</body></html>";
            ITextRenderer renderer = new ITextRenderer();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            renderer.setDocumentFromString(booking);
            renderer.layout();
            renderer.createPDF(outputStream);
        System.out.println("renderer *  *  *  "+renderer);
        return outputStream.toByteArray();
    }


    public byte[] getBookingTicketPdf(BookingEntity bookingInfo) throws Exception {
        List<PassengerEntity> passengers= bookingInfo.getPassengers();
        double grandTotal = 0.0;
        String email =bookingInfo.getEmail();
        String booking = "<html><head><style>"
                + "body { font-family: 'Arial', sans-serif; margin: 0; padding: 0; background-color: #f4f4f4; }"
                + ".header { background-color: #FFA500; color: white; padding: 20px; text-align: center; }"
                + ".content { background-color: white; color: black; padding: 10px; margin: 15px; box-shadow: 0 4px 8px 0 rgba(0,0,0,0.2); }"
                + ".details, .total { background-color: #f0f0f0; padding: 5px; margin: 15px; }"
                + ".details strong, .total strong { font-weight: bold; }"
                + ".footer { background-color: #FFA500; color: white; padding: 10px; text-align: center; position: fixed; bottom: 0; width: 100%; }"
                + "table { width: 100%; border-collapse: collapse; }"
                + "th, td { padding: 8px; text-align: left; border-bottom: 1px solid #ddd; }"
                + "th { background-color: #FFA500; color: white; }"
                + "tr:hover { background-color: #f5f5f5; }"
                + "</style></head><body>";

        booking += "<h2 class='header'>BusVia.com</h2>";

        booking += "<h4 class='content'>BOOKING INVOICE</h4>";

        if (email != null) {
            booking += "<div class='content'>";
            booking += "<h5 class='details'>Contact Details</h5>";
            booking += "<p>Email: " + bookingInfo.getEmail() + " Contact: " + bookingInfo.getContact() + "</p>";
            booking += "</div>";

            booking += "<h5 class='details'>INVOICE DETAILS</h5>";
            booking += "<table>";
            booking += "<tr><th>Passenger</th><th>Age</th><th>Gender</th><th>Seat Number</th><th>Date</th></tr>";

            for (PassengerEntity passenger : passengers) {
                booking += "<tr>";
                booking += "<td>" + passenger.getName() + "</td>";
                booking += "<td>" + passenger.getAge() + "</td>";
                booking += "<td>" + passenger.getGender() + "</td>";
                booking += "<td>" + (passenger.getSeatNumber() + 1) + "</td>";
                booking += "<td>" + passenger.getBooking().getDeparturedate() + "</td>";
                booking += "</tr>";
            }
            booking += "</table>";

            booking += "<div class='details'>";
            booking += "<h5>TRIP DETAILS INVOICE DETAILS</h5>";
            booking += "<p>Bus Number: " + bookingInfo.getBusNumber() + "</p>";
            booking += "<p>From: " + bookingInfo.getFromLocations() + ", " + bookingInfo.getDeparturedate() + "</p>";
            booking += "<p>To: " + bookingInfo.getToLocations() + ", " + bookingInfo.getArrivaldate() + "</p>";
            booking += "<p>Total Fare: " + bookingInfo.getTotalFare() + "</p>";
            booking += "</div>";
        }

        booking += "<h3 class='footer'>Total Fare: &#8377;" + bookingInfo.getTotalFare() + "</h3>";
        booking += "</body></html>";
        ITextRenderer renderer = new ITextRenderer();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        renderer.setDocumentFromString(booking);
        renderer.layout();
        renderer.createPDF(outputStream);
        System.out.println("renderer *  *  *  "+renderer);
        return outputStream.toByteArray();
    }

    @Override
    public int[] updateSeatsAfterBooking(UUID busUuid) {
        List<BookingEntity> bookingEntities =  bookingRepo.findAllByBusId(busUuid);
        System.out.println("bookingEntities : " + bookingEntities);

        int totalBookedSeats = bookingEntities.stream()
                .mapToInt(bookingEntity -> bookingEntity.getPassengers().size())
                .sum();
        System.out.println("totalBookedSeats : " + totalBookedSeats);
        int[] bookedSeats = new int[totalBookedSeats];
        int index = 0;

        for (BookingEntity bookingEntity : bookingEntities) {
            List<PassengerEntity> passengers = bookingEntity.getPassengers();
            for (PassengerEntity passenger : passengers) {
                System.out.println("passenger : " + passenger);
                bookedSeats[index] = passenger.getSeatNumber();
                System.out.println("bookedSeats[index] : " + bookedSeats[index]);
                index++;
            }
        }
        for (int i = 0; i < bookedSeats.length; i++) {
            System.out.println(bookedSeats[i]);
        }

        System.out.println("bookedSeats  : "+bookedSeats);
        return  bookedSeats;
    }

    @Override
    public int[] updateSeatsAfterBlockSeat(UUID busUuid) {

        List<BlockSeat> blockSeats =blockSeatRepo.findAllByBusId(busUuid);
        System.out.println("Block seat operator : " + blockSeats);

        int totalBookedSeats = blockSeats.stream()
                .mapToInt(bookingEntity -> bookingEntity.getPassengers().size())
                .sum();

        System.out.println("totalBlockedSeats : " + totalBookedSeats);
        int[] bookedSeats = new int[totalBookedSeats];
        int index = 0;

        for (BlockSeat bookingEntity :blockSeats ) {
            List<PassengerEntity> passengers = bookingEntity.getPassengers();
            for (PassengerEntity passenger : passengers) {
                System.out.println("passenger : " + passenger);
                bookedSeats[index] = passenger.getSeatNumber();
                System.out.println("bookedSeats[index] : " + bookedSeats[index]);
                index++;
            }
        }
        for (int i = 0; i < bookedSeats.length; i++) {
            System.out.println(bookedSeats[i]);
        }

        System.out.println("bookedSeats  : "+bookedSeats);
        return  bookedSeats;

    }

    @Override
    public List<BookingEntity> getALlBooking() {
        List<BookingEntity> booking = bookingRepo.findAll();
        System.out.println("Booking list "+booking);
        return booking;
    }

    @Override
    public List<BookingAllResponse> getBookingByUserMail(String userMail) {
       List<BookingEntity> bookingInfo = bookingRepo.findAllByUserMail(userMail);

        if (bookingInfo == null) {
            // Handle case where booking is not found for the user
            return null;
        }

        List<BookingAllResponse> bookingResponses = new ArrayList<>();

        for (BookingEntity bookingEntity : bookingInfo) {
            BookingAllResponse bookingResponse = new BookingAllResponse();
            System.out.println("Booking id "+bookingEntity.getUuid());

            bookingResponse.setUuid(bookingEntity.getUuid());
            bookingResponse.setTotalFare(bookingEntity.getTotalFare());
            bookingResponse.setSelectedSeats(bookingEntity.getSelectedSeats());
            bookingResponse.setEmail(bookingEntity.getEmail());
            bookingResponse.setContact(bookingEntity.getContact());
            bookingResponse.setArrivaldate(bookingEntity.getArrivaldate());
            bookingResponse.setDeparturedate(bookingEntity.getDeparturedate());
            bookingResponse.setFromLocations(bookingEntity.getFromLocations());
            bookingResponse.setToLocations(bookingEntity.getToLocations());
            bookingResponse.setBusNumber(bookingEntity.getBusNumber());
            bookingResponse.setBusId(bookingEntity.getBusId());
            bookingResponse.setStatus(bookingEntity.getStatus());
            bookingResponse.setUserMail(bookingEntity.getUserMail());
            bookingResponse.setPassengers(bookingEntity.getPassengers());
            bookingResponse.setBookingDate(bookingEntity.getBookingDate());


            bookingResponses.add(bookingResponse);
        }
        System.out.println("bookiign all rsponse "+bookingResponses);
        return bookingResponses;
    }

    @Override
    public List<BookingAllResponse> getBookingByBusId(String busId) {
        UUID busUuid = UUID.fromString(busId);
        List<BookingEntity> bookingInfo = bookingRepo.findAllByBusId(busUuid);

        if (bookingInfo == null) {

            return null;
        }
        List<PassengerEntity> allPassengers = new ArrayList<>();
        List<BookingAllResponse> bookingResponses = new ArrayList<>();
        for (BookingEntity bookingEntity : bookingInfo) {

            for (  PassengerEntity passenger : bookingEntity.getPassengers()) {

                allPassengers.add(passenger);

            }

            System.out.println("Passenegr list by busIdf from bookingEntity mapped passeneger "+allPassengers);
            BookingAllResponse bookingResponse = new BookingAllResponse();

            bookingResponse.setUuid(bookingEntity.getUuid());
            bookingResponse.setTotalFare(bookingEntity.getTotalFare());
            bookingResponse.setSelectedSeats(bookingEntity.getSelectedSeats());
            bookingResponse.setEmail(bookingEntity.getEmail());
            bookingResponse.setContact(bookingEntity.getContact());
            bookingResponse.setArrivaldate(bookingEntity.getArrivaldate());
            bookingResponse.setDeparturedate(bookingEntity.getDeparturedate());
            bookingResponse.setFromLocations(bookingEntity.getFromLocations());
            bookingResponse.setToLocations(bookingEntity.getToLocations());
            bookingResponse.setBusNumber(bookingEntity.getBusNumber());
            bookingResponse.setBusId(bookingEntity.getBusId());
            bookingResponse.setStatus(bookingEntity.getStatus());
            bookingResponse.setUserMail(bookingEntity.getUserMail());
            bookingResponse.setPassengers(bookingEntity.getPassengers());
            bookingResponse.setBookingDate(bookingEntity.getBookingDate());


            bookingResponses.add(bookingResponse);
        }
        System.out.println("BOOKING RESPONSE BUS BUS ID  :  "+bookingResponses);
        return bookingResponses;
    }

    @Override
    public void cancelBooking(UUID bookingId) {
        BookingEntity bookingEntity = bookingRepo.findById(bookingId).orElseThrow();

        List<PassengerEntity> passengers = bookingEntity.getPassengers();

        for (PassengerEntity passenger : passengers) {
            passenger.setSeatNumber(-1);
        }
        passengerRepo.saveAll(passengers);
        System.out.println("seat update success ");

    }




    @Override
    public void updateBookingAfterCancel(BookingEntity bookingEntity) {
        bookingRepo.save(bookingEntity);
    }

    @Override
    public BookingEntity sendTicketToEmail(String bookingId) throws Exception {
       UUID bookingUuid = UUID.fromString(bookingId);
       BookingEntity booking =bookingRepo .findById(bookingUuid).orElseThrow(()->new RuntimeException("booking not found"));
       if(booking!=null){
            BookingEntity bookingEntity   =  mailSenWithHtml(bookingId);

           if(bookingEntity!=null){
               System.out.println("email send successful");
               return booking;
           }else{
               System.out.println("email not send ,failed!");
               throw new RuntimeException("Mail not send ");
           }

       }
       throw new RuntimeException("Booking not found");
    }



    public  BookingEntity mailSenWithHtml(String bookingId) throws Exception {

        UUID bookingUuid = UUID.fromString(bookingId);
        BookingEntity booking = bookingRepo.findById(bookingUuid).orElseThrow(() -> new RuntimeException("Booking not found"));
        if (booking != null) {

            byte[] pdfContent = getBookingTicketPdf(booking);


            try {
                MimeMessage message = mailSender .createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true);
                helper.setTo(booking.getEmail());
                helper.setSubject("Your ticket details");
                helper.setText("Please find your booking invoice attached.");
                helper.addAttachment("booking_invoice.pdf", new ByteArrayResource(pdfContent));

                mailSender.send(message);
                System.out.println("Email sent successfully");
            } catch (MessagingException | MailException ex) {
                System.out.println("Failed to send email: " + ex.getMessage());
                throw new RuntimeException("Failed to send email");
            }
            return booking;
        } else {
            throw new RuntimeException("Booking not found");
        }
    }
}
