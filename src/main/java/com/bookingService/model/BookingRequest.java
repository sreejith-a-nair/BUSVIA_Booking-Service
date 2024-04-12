package com.bookingService.model;


import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class BookingRequest {
    private String busId;
    private double totalFare;
    private String selectedSeats;
    private String email;
    private String contact;
    private String arrivaldate;
    private String departuredate;
    private String from;
    private String to;
    private String authorityEmail;
    private String busNumber;
    private String paymentMode;
    private double walletAmount;
    private boolean couponApplied;
    private List<PassengerList> passengers;
}
