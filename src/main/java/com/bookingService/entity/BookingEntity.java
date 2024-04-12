package com.bookingService.entity;

//import com.bookingService.model.PassengerList;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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

    private LocalDate bookingDate;

    private UUID busId;

    @Column(nullable = true)
    private Boolean status;

    private String  userMail;

    private String authorityEmail;
    private String paymentMode;
    private boolean couponApplied;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "booking")
    @JsonIgnore
    private List<PassengerEntity> passengers;

    @Override
    public String toString() {
        return "BookingEntity{" +
                "uuid=" + uuid +
                ", totalFare=" + totalFare +
                ", selectedSeats=" + selectedSeats +
                ", email='" + email + '\'' +
                ", contact='" + contact + '\'' +
                ", arrivaldate='" + arrivaldate + '\'' +
                ", departuredate='" + departuredate + '\'' +
                ", fromLocations='" + fromLocations + '\'' +
                ", toLocations='" + toLocations + '\'' +
                ", busNumber='" + busNumber + '\'' +
                ", busId=" + busId +'\'' +
                ", status=" + status +'\'' +
                ", userMail=" + userMail +
                '}';
    }

    }

