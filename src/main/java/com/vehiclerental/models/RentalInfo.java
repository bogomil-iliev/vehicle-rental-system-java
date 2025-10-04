//This class represents rental transaction details for a specific vehicle booking. 
package com.vehiclerental.models;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

//It constructs a RentalInfo object for a completed or ongoing rental.
public class RentalInfo {
    private LocalDate startDate;
    private LocalDate endDate;

    public RentalInfo(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }
    
    //assigns the startdate
    public LocalDate getStartDate() {
        return startDate;
    }

     //assigns the end date.
    public LocalDate getEndDate() {
        return endDate;
    }

    //the period of the rent.
    public long getDaysRented() {
        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }
}
