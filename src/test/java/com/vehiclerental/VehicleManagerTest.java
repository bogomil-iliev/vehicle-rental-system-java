package com.vehiclerental;

import com.vehiclerental.dao.RentalDAO;
import com.vehiclerental.dao.VehicleDAO;
import com.vehiclerental.models.Car;
import com.vehiclerental.models.Vehicle;
import com.vehiclerental.services.VehicleManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.SQLException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;

//Unit test 1 (UT-1): VehicleManager must reject overlapping rentals.
class VehicleManagerTest {

    private VehicleDAO vehicleDAO;
    private RentalDAO  rentalDAO;
    private VehicleManager manager;

    @BeforeEach
    void setUp() throws SQLException {
        vehicleDAO = Mockito.mock(VehicleDAO.class);
        rentalDAO  = Mockito.mock(RentalDAO.class);
        manager    = new VehicleManager(vehicleDAO, rentalDAO);
        
        // --- Arrange existing booking 20 May 2025 10:00-12:00 -----------------
        Vehicle busy = new Car("1", "BMW", "M3", 50.0); // sample vehicle
        busy.setAvailable(false); // mark as rented
        busy.setRented(true);
        busy.setRentStartDateTime(LocalDateTime.of(2025, 5, 20, 10, 0));
        busy.setRentEndDateTime  (LocalDateTime.of(2025, 5, 20, 12, 0));
        Mockito.doNothing().when(vehicleDAO).saveVehicle(any(Vehicle.class)); // stub DAO
        manager.addVehicle(busy); // preload list
    }

    @Test
    void rentVehicle_rejectsOverlap() throws SQLException {
        // --- Act: request a conflicting booking 11:00-13:00 ------------------
        boolean booked = manager.rentVehicle(
                "1", "alice",
                LocalDateTime.of(2025, 5, 20, 11, 0),
                LocalDateTime.of(2025, 5, 20, 13, 0),
                false);

        // --- Assert ----------------------------------------------------------
        assertFalse(booked);  // should be rejected
        Mockito.verify(rentalDAO, Mockito.never()).logRental(any(Vehicle.class)); // DAO not called
    }
}
