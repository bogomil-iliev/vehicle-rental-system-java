//This class represents a Motorcycle. It inherits common properties from Vehicle.
package com.vehiclerental.models;

//It constucts a Motorcycle object with its attributes similar with the Car class.
public class Motorcycle extends Vehicle {
    public Motorcycle(String registrationNumber, String make, String model, double rentalPrice) {
        super(registrationNumber, make, model, rentalPrice);
    }

    @Override
    public String getVehicleType() {
        return "Motorcycle";
    }
}
