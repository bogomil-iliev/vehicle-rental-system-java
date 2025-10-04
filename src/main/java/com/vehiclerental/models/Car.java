//This class represents a car, which is a specific type of vehicle. It inherits general properties from the Vehicle Class.
package com.vehiclerental.models;


//It constructs a Car object with the specified ID, brand, model, and daily rental price.
public class Car extends Vehicle {
    public Car(String registrationNumber, String make, String model, double rentalPrice) {
        super(registrationNumber, make, model, rentalPrice);
    }

    @Override
    public String getVehicleType() {
        return "Car";
    }
}
