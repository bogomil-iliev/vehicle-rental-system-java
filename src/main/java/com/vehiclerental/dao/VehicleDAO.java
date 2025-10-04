package com.vehiclerental.dao;

import com.vehiclerental.models.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for managing vehicle records in the MySQL database.
 * Provides methods for saving, retrieving, updating, and deleting vehicles.
 * Uses JDBC for database operations.
 */

public class VehicleDAO {
    private static final String URL = "jdbc:mysql://localhost:3306/vehiclerental";
    private static final String USER = "root";
    private static final String PASSWORD = "root";


    //Saves a new vehicle to the database.
     public void saveVehicle(Vehicle vehicle) throws SQLException {
        String sql = "INSERT INTO vehicles (id, brand, model, price_per_day, available, rented, rent_start, rent_end, paid, rented_by, type) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, vehicle.getId());
            stmt.setString(2, vehicle.getBrand());
            stmt.setString(3, vehicle.getModel());
            stmt.setDouble(4, vehicle.getPricePerDay());
            stmt.setBoolean(5, vehicle.isAvailable());
            stmt.setBoolean(6, vehicle.isRented());
            stmt.setObject(7, vehicle.getRentStartDateTime());
            stmt.setObject(8, vehicle.getRentEndDateTime());
            stmt.setBoolean(9, vehicle.isPaid());
            stmt.setString(10, vehicle.getRentedBy());
            stmt.setString(11, vehicle.getVehicleType());
            stmt.executeUpdate();
        }
    }

    //Retrieves vehicles from the database by ID.
    public Vehicle getVehicleById(String id) throws SQLException {
        String sql = "SELECT * FROM vehicles WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return buildVehicleFromResultSet(rs);
            }
        }
        return null;
    }

    //Gets all vehicles from the database.
    public List<Vehicle> getAllVehicles() throws SQLException {
        String sql = "SELECT * FROM vehicles";
        List<Vehicle> vehicles = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                vehicles.add(buildVehicleFromResultSet(rs));
            }
        }
        return vehicles;
    }


    //Updates vehicle details in the database.
    public boolean updateVehicle(Vehicle vehicle) throws SQLException {
        String sql = "UPDATE vehicles SET brand=?, model=?, price_per_day=?, available=?, rented=?, rent_start=?, rent_end=?, paid=?, rented_by=?, type=? WHERE id=?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, vehicle.getBrand());
            stmt.setString(2, vehicle.getModel());
            stmt.setDouble(3, vehicle.getPricePerDay());
            stmt.setBoolean(4, vehicle.isAvailable());
            stmt.setBoolean(5, vehicle.isRented());
            stmt.setObject(6, vehicle.getRentStartDateTime());
            stmt.setObject(7, vehicle.getRentEndDateTime());
            stmt.setBoolean(8, vehicle.isPaid());
            stmt.setString(9, vehicle.getRentedBy());
            stmt.setString(10, vehicle.getVehicleType());
            stmt.setString(11, vehicle.getId());

            return stmt.executeUpdate() > 0;
        }
    }


    //Deletes a vehicle from the database using its ID.
    public boolean deleteVehicle(String id) throws SQLException {
        String sql = "DELETE FROM vehicles WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    private Vehicle buildVehicleFromResultSet(ResultSet rs) throws SQLException {
        String type = rs.getString("type");
        String id = rs.getString("id");
        String brand = rs.getString("brand");
        String model = rs.getString("model");
        double price = rs.getDouble("price_per_day");

        Vehicle vehicle;
        switch (type) {
            case "Car" -> vehicle = new Car(id, brand, model, price);
            case "Van" -> vehicle = new Van(id, brand, model, price);
            case "Motorcycle" -> vehicle = new Motorcycle(id, brand, model, price);
            default -> throw new SQLException("Unknown vehicle type: " + type);
        }

        vehicle.setAvailable(rs.getBoolean("available"));
        vehicle.setRented(rs.getBoolean("rented"));
        vehicle.setRentStartDateTime(rs.getTimestamp("rent_start") != null ? rs.getTimestamp("rent_start").toLocalDateTime() : null);
        vehicle.setRentEndDateTime(rs.getTimestamp("rent_end") != null ? rs.getTimestamp("rent_end").toLocalDateTime() : null);
        vehicle.setPaid(rs.getBoolean("paid"));
        vehicle.setRentedBy(rs.getString("rented_by"));

        return vehicle;
    }
}
