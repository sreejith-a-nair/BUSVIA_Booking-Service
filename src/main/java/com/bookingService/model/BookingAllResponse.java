package com.bookingService.model;

import com.bookingService.entity.PassengerEntity;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class BookingAllResponse {
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

    private  boolean status;

    private LocalDate bookingDate;

    private List<PassengerEntity> passengers;
}
