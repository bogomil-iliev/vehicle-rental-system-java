//This class represents a Van, a larger vehicle type suitable for transporting goods or multiple passengers. It inherits common properties from Vehicle.
package com.vehiclerental.models;

//It constucts a Van object with its attributes similar with the Car class.
public class Van extends Vehicle {
    public Van(String registrationNumber, String make, String model, double rentalPrice) {
        super(registrationNumber, make, model, rentalPrice);
    }

    @Override
    public String getVehicleType() {
        return "Van";
    }
}
