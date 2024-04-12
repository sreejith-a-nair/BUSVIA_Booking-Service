package com.bookingService.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class BookingResponse {
    private UUID uuid;


    private double totalFare;


    private int selectedSeats;


    private String email;


    private String contact;


    private String arrivaldate;


    private String departuredate;


    private String fromLocations;


    private String toLocations;


    private String busNumber;

    private UUID busId;

    private String userMail;

    private LocalDate bookingDate;

    private String authorityMail;


}
