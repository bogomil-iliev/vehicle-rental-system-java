package com.vehiclerental.models;

import java.time.LocalDateTime;

/*
 * The Vehicle class represents a general vehicle entity.
 * It serves as a superclass for specific vehicle types like Car, Van, and Motorcycle.
 * Includes details such as ID, brand, model, daily rental price, availability,
 * rental status, assigned user, rental period, payment status, and total rental cost.
 */


public abstract class Vehicle {
    private String id;
    private String brand;
    private String model;
    private double pricePerDay;
    private boolean available = true;
    private boolean rented = false;
    private LocalDateTime rentStartDateTime;
    private LocalDateTime rentEndDateTime;
    private boolean paid = false;
    private String rentedBy;

    public Vehicle(String id, String brand, String model, double pricePerDay) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.pricePerDay = pricePerDay;
    }

    //Gets the vehicle ID.
    public String getId() {
        return id;
    }

    //Gets the brand of the vehicle.
    public String getBrand() {
        return brand;
    }

    //Gets the model of the vehicle.
    public String getModel() {
        return model;
    }

    //Gets the rental price per day.
    public double getPricePerDay() {
        return pricePerDay;
    }

    //Checks if the vehicle is currently available.
    public boolean isAvailable() {
        return available;
    }

    //Sets the availability status of the vehicle.
    public void setAvailable(boolean available) {
        this.available = available;
    }

    //Checks if the vehicle is currently rented.
    public boolean isRented() {
        return rented;
    }

    //Sets the rental status of the vehicle.
    public void setRented(boolean rented) {
        this.rented = rented;
    }

    //Gets the username of the person who rented the vehicle.
    public void setRentedBy(String rentedBy) {
        this.rentedBy = rentedBy;
    }

    //Assigns the vehicle to a user.
    public String getRentedBy() {
        return rentedBy;
    }

    //Gets the rental start time.
    public LocalDateTime getRentStartDateTime() {
        return rentStartDateTime;
    }

    //Sets the rental start time.
    public void setRentStartDateTime(LocalDateTime rentStartDateTime) {
        this.rentStartDateTime = rentStartDateTime;
    }

    //Gets the rental end time.
    public LocalDateTime getRentEndDateTime() {
        return rentEndDateTime;
    }

    //Sets the rental end time..
    public void setRentEndDateTime(LocalDateTime rentEndDateTime) {
        this.rentEndDateTime = rentEndDateTime;
    }

    //Checks if the rental has been paid.
    public boolean isPaid() {
        return paid;
    }

    //Sets the payment status of the rental.
    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    //Gets the duration of the rent in days
    public long getRentalDurationDays() {
        if (rentStartDateTime != null && rentEndDateTime != null) {
            return java.time.Duration.between(rentStartDateTime, rentEndDateTime).toDays();
        }
        return 0;
    }


    //calculates the price of the rent
    public double calculateRentalPrice() {
        return getRentalDurationDays() * pricePerDay;
    }

    public abstract String getVehicleType();

    @Override
    public String toString() {
        return getVehicleType() + " - " + brand + " " + model + " (ID: " + id + "), €" + pricePerDay + "/day";
    }


    public void setBrand(String brand) {
        this.brand = brand;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public void setPricePerDay(double pricePerDay) {
        this.pricePerDay = pricePerDay;
    }
    
}
