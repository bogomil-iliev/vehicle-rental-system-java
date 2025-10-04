package com.vehiclerental.services;

import com.vehiclerental.models.User;
import com.vehiclerental.models.Vehicle;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


/*
 * Provides notification services related to vehicle rentals.
 * Generates alerts for overdue returns, upcoming returns, and scheduled rentals.
 */
public class NotificationService {

    private final VehicleManager vehicleManager;
    private final AuthService authService;

    //Constructs a NotificationService with access to vehicle and user data.
    public NotificationService(VehicleManager vehicleManager, AuthService authService) {
        this.vehicleManager = vehicleManager;
        this.authService = authService;
    }

    //Generates a list of overdue rental notifications. Identifies vehicles whose return times have passed but are still marked as rented.
    public List<String> getOverdueNotifications() {
        List<String> notifications = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (Vehicle vehicle : vehicleManager.getAllVehicles()) {
            if (vehicle.getRentEndDateTime() != null && now.isAfter(vehicle.getRentEndDateTime()) && vehicle.isRented()) {
                notifications.add("Overdue: " + vehicle + " was due on " + vehicle.getRentEndDateTime());
            }
        }
        return notifications;
    }

    //Generates warnings for vehicles due to be returned within the next 24 hours.
    public List<String> getUpcomingReturnWarnings() {
        List<String> warnings = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (Vehicle vehicle : vehicleManager.getAllVehicles()) {
            if (vehicle.getRentEndDateTime() != null && vehicle.isRented()) {
                long hoursToReturn = java.time.Duration.between(now, vehicle.getRentEndDateTime()).toHours();
                if (hoursToReturn > 0 && hoursToReturn <= 24) {
                    warnings.add("Reminder: " + vehicle + " is due within 24h on " + vehicle.getRentEndDateTime());
                }
            }
        }
        return warnings;
    }


    // Generates reminders for vehicles scheduled to be rented soon. Includes rentals that are booked to start within the next 24 hours.
    public List<String> getUpcomingRentalReminders() {
        List<String> reminders = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (Vehicle vehicle : vehicleManager.getAllVehicles()) {
            if (vehicle.getRentStartDateTime() != null && !vehicle.isRented()) {
                long hoursUntilStart = java.time.Duration.between(now, vehicle.getRentStartDateTime()).toHours();
                if (hoursUntilStart > 0 && hoursUntilStart <= 24) {
                    reminders.add("Upcoming rental: " + vehicle + " is scheduled to start on " + vehicle.getRentStartDateTime());
                }
            }
        }
        return reminders;
    }

    /*Generates personalized rental notifications for a specific user. Includes upcoming rental reminders, overdue alerts, and return warnings
     for vehicles currently or soon-to-be rented by the user. */
    public List<String> getUserNotifications(User user) {
        List<String> messages = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Vehicle vehicle : vehicleManager.getVehiclesRentedByUser(user.getUsername())) {
            LocalDateTime start = vehicle.getRentStartDateTime();
            LocalDateTime end = vehicle.getRentEndDateTime();

            if (start != null && now.isBefore(start)) {
                long hoursUntilStart = java.time.Duration.between(now, start).toHours();
                if (hoursUntilStart <= 24) {
                    messages.add("Upcoming rental: " + vehicle + " starts on " + start);
                }
            }

            if (end != null) {
                if (now.isAfter(end) && vehicle.isRented()) {
                    messages.add("Overdue: " + vehicle + " was due on " + end);
                } else {
                    long hoursToReturn = java.time.Duration.between(now, end).toHours();
                    if (hoursToReturn > 0 && hoursToReturn <= 24) {
                        messages.add("Reminder: " + vehicle + " is due within 24h on " + end);
                    }
                }
            }
        }

        return messages;
    }

    //Displays all current rental notifications for a given user. Fetches upcoming, overdue, and return-related messages and prints them to the console.
    public void printNotifications(User user) {
        List<String> messages = getUserNotifications(user);
        if (messages.isEmpty()) {
            System.out.println("No notifications.");
        } else {
            System.out.println("Notifications:");
            messages.forEach(System.out::println);
        }
    }
}
