package com.bookingService.entity;//package com.bookingService.entity;
//
//import jakarta.persistence.Entity;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
//import jakarta.persistence.Id;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;
@Data
@AllArgsConstructor

@NoArgsConstructor

@Entity
public class PassengerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    private String name;
    private int age;
    private String gender;
    private int seatNumber;
    @ManyToOne
    @JoinColumn(name = "booking_id")
    private BookingEntity booking;


    @ManyToOne
    @JoinColumn(name = "block_id")
    private BlockSeat blockSeat;

}
