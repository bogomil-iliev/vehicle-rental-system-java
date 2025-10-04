package com.vehiclerental.services;

import com.vehiclerental.dao.RentalDAO;
import com.vehiclerental.dao.VehicleDAO;
import com.vehiclerental.models.Vehicle;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Handles business logic related to vehicle management and rentals.
 * Provides methods for adding vehicles, renting, returning, checking availability,
 * calculating prices, and tracking rental history.
 */

public class VehicleManager {
    private List<Vehicle> vehicles = new ArrayList<>();
    private List<Vehicle> rentalHistory = new ArrayList<>();
    private VehicleDAO vehicleDAO = new VehicleDAO();
    private RentalDAO rentalDAO = new RentalDAO();

    //Initialises the VehicleManager by loading vehicle and rental history data from the database.
     public VehicleManager() {
        try {
            vehicles = vehicleDAO.getAllVehicles();
        } catch (SQLException e) {
            System.out.println("Failed to load vehicles from database: " + e.getMessage());
        }

        try {
            rentalHistory = rentalDAO.getAllRentalHistory();
        } catch (Exception e) {
            System.out.println("Failed to load rental history: " + e.getMessage());
        }
    }

    //Adds a new vehicle to the system and saves it to the database. Prevents duplicate entries based on vehicle ID.
    public void addVehicle(Vehicle vehicle) {
        if (findVehicleById(vehicle.getId()) != null) {
            System.out.println("A vehicle with this ID already exists.");
            return;
        }
        vehicles.add(vehicle);
        try {
            vehicleDAO.saveVehicle(vehicle);
        } catch (SQLException e) {
            System.out.println("Failed to save vehicle to database: " + e.getMessage());
        }
    }

    //Rents a vehicle for a specified time period if it is available. Updates rental details and logs the transaction.
    public boolean rentVehicle(String vehicleId, String username, LocalDateTime startDateTime, LocalDateTime endDateTime, boolean isPaid) {
        Vehicle vehicle = findVehicleById(vehicleId);
        if (vehicle != null && isAvailableForPeriod(vehicle, startDateTime, endDateTime)) {
            vehicle.setRented(true);
            vehicle.setAvailable(false);
            vehicle.setRentStartDateTime(startDateTime);
            vehicle.setRentEndDateTime(endDateTime);
            vehicle.setRentedBy(username);
            vehicle.setPaid(isPaid);
            
            try {
                vehicleDAO.updateVehicle(vehicle);
                RentalDAO rentalDAO = new RentalDAO();
                rentalDAO.logRental(vehicle);
                return true;
                
            } catch (SQLException e) {
                System.out.println("Failed to update rental in database: " + e.getMessage());
            }
        }
        return false;
    }

    //Marks a rented vehicle as returned.Updates status, resets rental fields, logs the return in the database.
    public boolean returnVehicle(String vehicleId) {
        Vehicle vehicle = findVehicleById(vehicleId);
        if (vehicle != null && vehicle.isRented()) {
            rentalHistory.add(cloneVehicle(vehicle));
            rentalDAO.logRental(vehicle); // log to DB

            vehicle.setRented(false);
            vehicle.setAvailable(true);
            vehicle.setRentStartDateTime(null);
            vehicle.setRentEndDateTime(null);
            vehicle.setRentedBy(null);
            vehicle.setPaid(false);

            try {
                vehicleDAO.updateVehicle(vehicle);
                return true;
            } catch (SQLException e) {
                System.out.println("Failed to update return in database: " + e.getMessage());
            }
        }
        return false;
    }

    //Cancels upcoming bookings of vehicles.
    public boolean cancelUpcomingBooking(String vehicleId, String username) {
        Vehicle vehicle = findVehicleById(vehicleId);
        if (vehicle != null && username.equals(vehicle.getRentedBy())) {
            if (vehicle.getRentStartDateTime() != null && vehicle.getRentStartDateTime().isAfter(LocalDateTime.now())) {
                vehicle.setRented(false);
                vehicle.setAvailable(true);
                vehicle.setRentStartDateTime(null);
                vehicle.setRentEndDateTime(null);
                vehicle.setRentedBy(null);
                vehicle.setPaid(false);
                try {
                    vehicleDAO.updateVehicle(vehicle);
                    return true;
                } catch (SQLException e) {
                    System.out.println("Failed to cancel booking in database: " + e.getMessage());
                }
            }
        }
        return false;
    }

    //Finds a vehicle by its ID.
    public Vehicle findVehicleById(String id) {
        return vehicles.stream()
                .filter(v -> v.getId().equalsIgnoreCase(id))
                .findFirst()
                .orElse(null);
    }

    //Checks whether a vehicle is available for a new rental within the specified date/time range.
    public boolean isAvailableForPeriod(Vehicle vehicle, LocalDateTime start, LocalDateTime end) {
        if (!vehicle.isAvailable()) return false;
        if (vehicle.getRentStartDateTime() == null || vehicle.getRentEndDateTime() == null) return true;
        return end.isBefore(vehicle.getRentStartDateTime()) || start.isAfter(vehicle.getRentEndDateTime());
    }

    //Checks if a specific vehicle is availale for rental during a given time range.
    public boolean isAvailableDuring(String vehicleId, LocalDateTime start, LocalDateTime end) {
        Vehicle vehicle = findVehicleById(vehicleId);
        return vehicle != null && isAvailableForPeriod(vehicle, start, end);
    }

    //Returns a list of vehicles that are currently available for rent.
    public List<Vehicle> getAvailableVehicles() {
        return vehicles.stream().filter(Vehicle::isAvailable).collect(Collectors.toList());
    }

    //Returns a list of vehicles that are currently rented out.
    public List<Vehicle> getRentedVehicles() {
        return vehicles.stream().filter(Vehicle::isRented).collect(Collectors.toList());
    }

    //Retreives all vehicles from the database.
    public List<Vehicle> getAllVehicles() {
        return new ArrayList<>(vehicles);
    }

    //Retrieves all vehicles currently rented by a specific user.
    public List<Vehicle> getVehiclesRentedByUser(String username) {
        return vehicles.stream()
                .filter(v -> v.isRented() && username.equals(v.getRentedBy()))
                .collect(Collectors.toList());
    }

    //Retrieves the complete rental history.
    public List<Vehicle> getRentalHistory() {
        return new ArrayList<>(rentalHistory);
    }

    //Retrieves rental history for a specific user.
    public List<Vehicle> getRentalHistoryByUser(String username) {
        return rentalHistory.stream()
                .filter(v -> username.equals(v.getRentedBy()))
                .collect(Collectors.toList());
    }

    //Retreives upcoming bookings for a specific user passed.
    public List<Vehicle> getUpcomingBookings(String username) {
        LocalDateTime now = LocalDateTime.now();
        return vehicles.stream()
                .filter(v -> v.isRented()
                        && username.equals(v.getRentedBy())
                        && v.getRentStartDateTime() != null
                        && v.getRentStartDateTime().isAfter(now))
                .collect(Collectors.toList());
    }

    //Updates the details of an existing vehicle and saves the changes to the database.
    public boolean updateVehicleDetails(String id, String newBrand, String newModel, double newPricePerDay) {
        Vehicle vehicle = findVehicleById(id);
        if (vehicle != null) {
            vehicle.setBrand(newBrand);
            vehicle.setModel(newModel);
            vehicle.setPricePerDay(newPricePerDay);
            try {
                return vehicleDAO.updateVehicle(vehicle);
            } catch (SQLException e) {
                System.out.println("Failed to update vehicle in database: " + e.getMessage());
            }
        }
        return false;
    }

    //Removes a vehicle by its ID
    public void removeVehicle(String id) {
        Vehicle vehicle = findVehicleById(id);
        if (vehicle != null && !vehicle.isRented()) {
            vehicles.remove(vehicle);
            try {
                if (vehicleDAO.deleteVehicle(id)) {
                    System.out.println("Vehicle removed.");
                }
            } catch (SQLException e) {
                System.out.println("Failed to delete vehicle from database: " + e.getMessage());
            }
        } else {
            System.out.println("Cannot remove a rented vehicle or vehicle not found.");
        }
    }


   //Confirms the payment for a rented vehicle. Updates both the vehicle record and the rental log in the database.
    public boolean confirmPayment(String vehicleId) {
        Vehicle vehicle = findVehicleById(vehicleId);
        if (vehicle != null && !vehicle.isPaid()) {
            vehicle.setPaid(true);
            try {
                vehicleDAO.updateVehicle(vehicle); // update vehicle paid status
                return rentalDAO.markRentalAsPaid(vehicle.getId(), vehicle.getRentedBy()); // update rentals table
            } catch (SQLException e) {
                System.out.println("Failed to confirm payment in database: " + e.getMessage());
            }
        }
        return false;
    }
    
    
    //Creates a copy of a given Vehicle object, including its rental details. The clone retains the same type, ID, brand, model, price, and rental state.
    private Vehicle cloneVehicle(Vehicle original) {
        Vehicle clone = switch (original.getVehicleType()) {
            case "Car" -> new com.vehiclerental.models.Car(original.getId(), original.getBrand(), original.getModel(), original.getPricePerDay());
            case "Van" -> new com.vehiclerental.models.Van(original.getId(), original.getBrand(), original.getModel(), original.getPricePerDay());
            case "Motorcycle" -> new com.vehiclerental.models.Motorcycle(original.getId(), original.getBrand(), original.getModel(), original.getPricePerDay());
            default -> null;
        };

        if (clone != null) {
            clone.setRentStartDateTime(original.getRentStartDateTime());
            clone.setRentEndDateTime(original.getRentEndDateTime());
            clone.setRentedBy(original.getRentedBy());
            clone.setPaid(original.isPaid());
        }
        return clone;
    }

    //Adds an injectable constructor for testing purposes
    public VehicleManager(VehicleDAO vehicleDAO, RentalDAO rentalDAO) {
        this.vehicleDAO = vehicleDAO;
        this.rentalDAO  = rentalDAO;
    }
    

    

}
