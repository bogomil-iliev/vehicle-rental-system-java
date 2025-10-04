package com.vehiclerental.dao;

import com.vehiclerental.models.*;
import com.vehiclerental.utils.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Data Access Object (DAO) for handling rental transaction records.
 * Logs new rentals and retrieves rental history from the database.
 */

public class RentalDAO {

    //Logs a new rental into the database.
    public void logRental(Vehicle vehicle) {
        String sql = "INSERT INTO rentals (vehicle_id, rented_by, start_time, end_time, total_price, paid, vehicle_type) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, vehicle.getId());
            stmt.setString(2, vehicle.getRentedBy());
            stmt.setTimestamp(3, Timestamp.valueOf(vehicle.getRentStartDateTime()));
            stmt.setTimestamp(4, Timestamp.valueOf(vehicle.getRentEndDateTime()));
            stmt.setDouble(5, vehicle.calculateRentalPrice());
            stmt.setBoolean(6, vehicle.isPaid());
            stmt.setString(7, vehicle.getVehicleType());

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Failed to log rental: " + e.getMessage());
        }
    }

    //Retrieves rental history for a specific user.
    public List<Vehicle> getRentalHistoryByUser(String username) {
        List<Vehicle> rentals = new ArrayList<>();
        String sql = "SELECT * FROM rentals WHERE rented_by = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Vehicle vehicle = mapResultToVehicle(rs);
                rentals.add(vehicle);
            }

        } catch (SQLException e) {
            System.out.println("Failed to fetch rental history: " + e.getMessage());
        }

        return rentals;
    }


    //Retrieves all rental history records in the system.
    public List<Vehicle> getAllRentalHistory() {
        List<Vehicle> rentals = new ArrayList<>();
        String sql = "SELECT * FROM rentals";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Vehicle vehicle = mapResultToVehicle(rs);
                rentals.add(vehicle);
            }

        } catch (SQLException e) {
            System.out.println("Failed to fetch all rental history: " + e.getMessage());
        }

        return rentals;
    }

    //Maps a result set row to a corresponding Vehicle object. Constructs appropriate subclass (Car, Van, Motorcycle) based on vehicle type.
    private Vehicle mapResultToVehicle(ResultSet rs) throws SQLException {
        String type = rs.getString("vehicle_type");
        Vehicle vehicle = switch (type) {
            case "Car" -> new Car(rs.getString("vehicle_id"), "Unknown", "Unknown", rs.getDouble("total_price")); // Fallback values
            case "Van" -> new Van(rs.getString("vehicle_id"), "Unknown", "Unknown", rs.getDouble("total_price"));
            case "Motorcycle" -> new Motorcycle(rs.getString("vehicle_id"), "Unknown", "Unknown", rs.getDouble("total_price"));
            default -> null;
        };

        if (vehicle != null) {
            vehicle.setRentedBy(rs.getString("rented_by"));
            vehicle.setRentStartDateTime(rs.getTimestamp("start_time").toLocalDateTime());
            vehicle.setRentEndDateTime(rs.getTimestamp("end_time").toLocalDateTime());
            vehicle.setPaid(rs.getBoolean("paid"));
        }

        return vehicle;
    }

    //Marks a specific rental as paid based on vehicle ID and user.
    public boolean markRentalAsPaid(String vehicleId, String username) {
        String sql = "UPDATE rentals SET paid = TRUE WHERE vehicle_id = ? AND rented_by = ? AND paid = FALSE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, vehicleId);
            stmt.setString(2, username);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Failed to mark rental as paid: " + e.getMessage());
            return false;
        }
    }
    
}
