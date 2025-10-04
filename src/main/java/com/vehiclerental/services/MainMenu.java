package com.vehiclerental.services;

import com.vehiclerental.dao.RentalDAO;
import com.vehiclerental.models.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/**
 * Provides the main console interface for the Vehicle Rental System.
 * Handles user interaction, authentication, and access to vehicle and rental features based on role.
 */
public class MainMenu {
    private static final VehicleManager vehicleManager = new VehicleManager();
    private static final AuthService authService = new AuthService();
    private static final NotificationService notificationService = new NotificationService(vehicleManager, authService);
    private static final RentalDAO rentalDAO = new RentalDAO();
    private static final Scanner scanner = new Scanner(System.in);

    //Launches the main menu loop for the system. Handles login, registration, and routes users to role-specific menus.
    public static void start() {
        System.out.println("=== Welcome to the Vehicle Rental System ===");
        User currentUser = null;

        while (currentUser == null) {
            System.out.println("\n1. Login\n2. Register\n3. Exit");
            int choice = getIntInput("Choose an option: ");
            switch (choice) {
                case 1 -> {
                    String username = getStringInput("Username: ");
                    String password = getStringInput("Password: ");
                    currentUser = authService.login(username, password);
                    if (currentUser == null) System.out.println("Invalid credentials. Try again.");
                }
                case 2 -> {
                    String username = getStringInput("Choose a username: ");
                    if (authService.getUser(username) != null) {
                        System.out.println("Username already exists.");
                        break;
                    }
                    String password = getStringInput("Choose a password: ");
                    String name = getStringInput("Full name: ");
                    String phone = getStringInput("Phone number: ");
                    String email = getStringInput("Email address: ");
                    String address = getStringInput("Home address: ");
                    authService.register(username, password, "CUSTOMER", name, phone, email, address);
                    currentUser = authService.login(username, password);
                }
                case 3 -> {
                    System.out.println("Exiting application.");
                    return;
                }
                default -> System.out.println("Invalid option. Try again.");
            }
        }

        if ("ADMIN".equalsIgnoreCase(currentUser.getRole())) showAdminMenu(currentUser);
        else showCustomerMenu(currentUser);
    }

    //Displays the admin menu and processes admin-specific operations such as managing vehicles, users, and viewing system-wide rentals.
    private static void showAdminMenu(User admin) {
        int choice;
        do {
            System.out.println("\n=== Admin Menu ===");
            System.out.println("""
                1. Add Vehicle
                2. Rent Vehicle
                3. Return Vehicle
                4. View Available Vehicles
                5. Add Regular User
                6. Add Admin User
                7. View Rented Vehicles
                8. View Notifications
                9. Update Vehicle Details
                10. Manage Users
                11. View Rental History
                12. Cancel Booking
                13. Confirm Payment on Pickup
                14. Logout
            """);
            choice = getIntInput("Choose an option: ");
            switch (choice) {
                case 1 -> addVehicle();
                case 2 -> rentVehicle(admin);
                case 3 -> returnVehicle();
                case 4 -> showAvailableVehicles();
                case 5 -> registerUser("CUSTOMER");
                case 6 -> registerUser("ADMIN");
                case 7 -> showRentedVehicles();
                case 8 -> notificationService.printNotifications(admin);
                case 9 -> updateVehicleDetails();
                case 10 -> manageUsers();
                case 11 -> showAllRentalHistory();
                case 12 -> cancelAnyBooking();
                case 13 -> confirmPaymentOnPickup();
                case 14 -> System.out.println("Logging out...");
                default -> System.out.println("Invalid choice.");
            }
        } while (choice != 14);
    }

    //Displays the user menu and allows customers to rent, return, view rentals, and see notifications.
    private static void showCustomerMenu(User customer) {
        int choice;
        do {
            System.out.println("\n=== Customer Menu ===");
            System.out.println("""
                1. Rent Vehicle
                2. Return Vehicle
                3. View Available Vehicles
                4. View Notifications
                5. View Booking History
                6. Cancel Upcoming Booking
                7. Logout
            """);
            choice = getIntInput("Choose an option: ");
            switch (choice) {
                case 1 -> rentVehicle(customer);
                case 2 -> returnVehicle();
                case 3 -> showAvailableVehicles();
                case 4 -> notificationService.printNotifications(customer);
                case 5 -> showUserBookingHistory(customer);
                case 6 -> cancelUpcomingBooking(customer);
                case 7 -> System.out.println("Logging out...");
                default -> System.out.println("Invalid choice.");
            }
        } while (choice != 7);
    }

    /*Prompts the admin to add a new vehicle to the system. Allows selection of vehicle type and entry of all required details. 
    Prevents duplicate vehicle IDs and stores the new vehicle via VehicleManager.*/
    private static void addVehicle() {
        System.out.println("Select vehicle type:\n1. Car\n2. Van\n3. Motorcycle");
        int type = getIntInput("Enter choice: ");
        String id = getStringInput("Enter vehicle ID: ");
        if (vehicleManager.findVehicleById(id) != null) {
            System.out.println("Vehicle ID already exists.");
            return;
        }
        String brand = getStringInput("Enter brand: ");
        String model = getStringInput("Enter model: ");
        double price = getDoubleInput("Enter price per day: ");
        Vehicle vehicle = switch (type) {
            case 1 -> new Car(id, brand, model, price);
            case 2 -> new Van(id, brand, model, price);
            case 3 -> new Motorcycle(id, brand, model, price);
            default -> null;
        };
        if (vehicle != null) {
            vehicleManager.addVehicle(vehicle);
            System.out.println("Vehicle added successfully.");
        } else System.out.println("Invalid vehicle type.");
    }

    /*Handles the vehicle rental process for a user.Prompts for vehicle ID, rental duration, and start date,
     checks availability, and attempts to process the booking.*/
    private static void rentVehicle(User user) {
        String id = getStringInput("Enter vehicle ID to rent: ");
        Vehicle vehicle = vehicleManager.findVehicleById(id);
        if (vehicle == null) {
            System.out.println("Vehicle not found.");
            return;
        }
        int days = getIntInput("Enter number of rental days: ");
        if (days <= 0) {
            System.out.println("Rental days must be positive.");
            return;
        }
        LocalDateTime startDate = getDateTimeInput("Enter rental start date and time (yyyy-MM-dd HH:mm): ");
        LocalDateTime endDate = startDate.plusDays(days);
        if (!vehicleManager.isAvailableDuring(id, startDate, endDate)) {
            System.out.println("Vehicle is not available during this period.");
            return;
        }
    
        boolean paid = false; // Default to unpaid for customer booking
    
        if (vehicleManager.rentVehicle(id, user.getUsername(), startDate, endDate, paid)) {
            System.out.printf("Rental confirmed. Total: £%.2f%nReturn due: %s%n", days * vehicle.getPricePerDay(),
                    endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        } else {
            System.out.println("Failed to process rental.");
        }
    }
    
    //Handles the return process for a rented vehicle. Prompts for the vehicle ID and updates its status if found and rented.
    private static void returnVehicle() {
        String id = getStringInput("Enter vehicle ID to return: ");
        if (vehicleManager.returnVehicle(id)) {
            System.out.println("Vehicle returned successfully.");
        } else {
            System.out.println("Vehicle not found or already returned.");
        }
    }

    // * Displays a list of all currently available vehicles for rent. Informs the user if no vehicles are available.
    private static void showAvailableVehicles() {
        List<Vehicle> available = vehicleManager.getAvailableVehicles();
        if (available.isEmpty()) System.out.println("No available vehicles.");
        else available.forEach(System.out::println);
    }

    /*Displays a list of all currently rented vehicles, including renter information and return due date/time.
    Informs the user if no vehicles are currently rented.*/
    private static void showRentedVehicles() {
        List<Vehicle> rented = vehicleManager.getRentedVehicles();
        if (rented.isEmpty()) System.out.println("No vehicles currently rented.");
        else rented.forEach(v -> System.out.printf("%s rented by %s, return by %s%n", v, v.getRentedBy(),
                v.getRentEndDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
    }

    //Allows an admin to update the details of an existing vehicle. Prompts for new brand, model, and daily price, and updates the database.
    private static void updateVehicleDetails() {
        String id = getStringInput("Enter vehicle ID to update: ");
        Vehicle vehicle = vehicleManager.findVehicleById(id);
        if (vehicle == null) {
            System.out.println("Vehicle not found.");
            return;
        }
        System.out.println("Current details: " + vehicle);
        String brand = getStringInput("Enter new brand: ");
        String model = getStringInput("Enter new model: ");
        double price = getDoubleInput("Enter new price per day: ");
        if (vehicleManager.updateVehicleDetails(id, brand, model, price)) {
            System.out.println("Vehicle details updated.");
        } else {
            System.out.println("Failed to update.");
        }
    }

    //Displays the complete rental history across all users and vehicles. Includes rental period, user, and payment status. Informs the admin if no past rentals are found
    private static void showAllRentalHistory() {
        List<Vehicle> history = rentalDAO.getAllRentalHistory();
        if (history.isEmpty()) System.out.println("No past rentals found.");
        else history.forEach(v -> System.out.printf("Vehicle %s | User: %s | From: %s To: %s | Paid: %s%n",
                v.getId(), v.getRentedBy(), v.getRentStartDateTime(), v.getRentEndDateTime(),
                v.isPaid() ? "Yes" : "No"));
    }

    // Displays the rental history for the currently logged-in user. Shows vehicle ID, rental period, and payment status.
    private static void showUserBookingHistory(User user) {
        List<Vehicle> bookings = rentalDAO.getRentalHistoryByUser(user.getUsername());
        if (bookings.isEmpty()) System.out.println("You have no past bookings.");
        else bookings.forEach(v -> System.out.printf("Vehicle %s | From: %s To: %s | Paid: %s%n",
                v.getId(), v.getRentStartDateTime(), v.getRentEndDateTime(), v.isPaid() ? "Yes" : "No"));
    }

    //Allows a user to view and cancel one of their upcoming bookings. Displays a numbered list of future rentals and processes the selected cancellation.
    private static void cancelUpcomingBooking(User user) {
        List<Vehicle> upcoming = vehicleManager.getUpcomingBookings(user.getUsername());
        if (upcoming.isEmpty()) {
            System.out.println("No upcoming bookings.");
            return;
        }
        for (int i = 0; i < upcoming.size(); i++) {
            Vehicle v = upcoming.get(i);
            System.out.printf("%d. %s (Return: %s)%n", i + 1, v, v.getRentEndDateTime());
        }
        int choice = getIntInput("Enter booking number to cancel: ") - 1;
        if (choice >= 0 && choice < upcoming.size()) {
            if (vehicleManager.cancelUpcomingBooking(upcoming.get(choice).getId(), user.getUsername())) {
                System.out.println("Booking cancelled.");
            } else System.out.println("Cancellation failed.");
        } else System.out.println("Invalid choice.");
    }

    //Registers a new user or admin with full contact details. Validates username uniqueness and delegates registration to AuthService.
    private static void registerUser(String role) {
        String username = getStringInput("Enter username: ");
        if (authService.getUser(username) != null) {
            System.out.println("Username exists.");
            return;
        }
        String password = getStringInput("Password: ");
        String name = getStringInput("Full name: ");
        String phone = getStringInput("Phone: ");
        String email = getStringInput("Email: ");
        String address = getStringInput("Address: ");
        authService.register(username, password, role, name, phone, email, address);
        System.out.println(role + " added.");
    }

    // Displays the user management menu for admins. Provides options to view, update, or delete user accounts. Loops until the admin chooses to go back.
    private static void manageUsers() {
        int choice;
        do {
            System.out.println("\n=== User Management ===");
            System.out.println("1. View All\n2. Update\n3. Delete\n4. Back");
            choice = getIntInput("Choose: ");
            switch (choice) {
                case 1 -> viewAllUsers();
                case 2 -> updateUserDetails();
                case 3 -> deleteUser();
                case 4 -> System.out.println("Returning...");
                default -> System.out.println("Invalid.");
            }
        } while (choice != 4);
    }

    //Displays a list of all registered users in the system. Shows username, role, name, and email for each user.
    private static void viewAllUsers() {
        List<User> users = authService.getAllUsers();
        if (users.isEmpty()) System.out.println("No users.");
        else users.forEach(u -> System.out.printf("Username: %s | Role: %s | Name: %s | Email: %s%n",
                u.getUsername(), u.getRole(), u.getName(), u.getEmail()));
    }

    //Allows the admin to update contact details of an existing user.
    private static void updateUserDetails() {
        String username = getStringInput("Username: ");
        User u = authService.getUser(username);
        if (u == null) {
            System.out.println("User not found.");
            return;
        }
        String name = getStringInput("New full name: ");
        String phone = getStringInput("New phone: ");
        String email = getStringInput("New email: ");
        String address = getStringInput("New address: ");
        authService.updateUser(username, name, phone, email, address);
        System.out.println("User updated.");
    }

    //Allows the admin to delete user accounts. Confirms deletion or reports if the user doesn't exist.
    private static void deleteUser() {
        String username = getStringInput("Username: ");
        if (authService.deleteUser(username)) System.out.println("Deleted.");
        else System.out.println("User not found.");
    }

    /*Allows an admin to view and cancel any upcoming booking in the system. Displays a numbered list of all future rentals across users,
    prompts for selection, and processes the cancellation.*/
    private static void cancelAnyBooking() {
        List<Vehicle> upcoming = vehicleManager.getAllVehicles().stream()
            .filter(v -> v.isRented()
                && v.getRentStartDateTime() != null
                && v.getRentStartDateTime().isAfter(LocalDateTime.now()))
            .toList();
    
        if (upcoming.isEmpty()) {
            System.out.println("No upcoming bookings found.");
            return;
        }
    
        System.out.println("Upcoming Bookings:");
        for (int i = 0; i < upcoming.size(); i++) {
            Vehicle v = upcoming.get(i);
            System.out.printf("%d. %s | User: %s | Start: %s | End: %s%n", i + 1,
                    v.getId(), v.getRentedBy(),
                    v.getRentStartDateTime(), v.getRentEndDateTime());
        }
    
        int choice = getIntInput("Select booking number to cancel: ") - 1;
        if (choice >= 0 && choice < upcoming.size()) {
            Vehicle v = upcoming.get(choice);
            if (vehicleManager.cancelUpcomingBooking(v.getId(), v.getRentedBy())) {
                System.out.println("Booking cancelled successfully.");
            } else {
                System.out.println("Failed to cancel booking.");
            }
        } else {
            System.out.println("Invalid selection.");
        }
    }
    
    /* Allows an admin to confirm payment for rentals that were not prepaid.
     Displays all unpaid current rentals and processes the selected payment confirmation.*/
    private static void confirmPaymentOnPickup() {
        List<Vehicle> unpaid = vehicleManager.getRentedVehicles()
            .stream().filter(v -> !v.isPaid()).toList();
    
        if (unpaid.isEmpty()) {
            System.out.println("No unpaid rentals.");
            return;
        }
    
        System.out.println("Unpaid Rentals:");
        for (int i = 0; i < unpaid.size(); i++) {
            Vehicle v = unpaid.get(i);
            System.out.printf("%d. %s | Rented by: %s | Due: %s%n",
                i + 1, v.getId(), v.getRentedBy(),
                v.getRentEndDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        }
    
        int choice = getIntInput("Select number to confirm payment: ") - 1;
        if (choice >= 0 && choice < unpaid.size()) {
            Vehicle selected = unpaid.get(choice);
            boolean success = vehicleManager.confirmPayment(selected.getId());
            System.out.println(success ? "Payment confirmed." : "Failed to confirm payment.");
        } else {
            System.out.println("Invalid selection.");
        }
    }
    
    //Prompts the user for an integer input.
    private static int getIntInput(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextInt()) {
            System.out.print("Invalid. " + prompt);
            scanner.next(); // Skip invalid input
        }
        int value = scanner.nextInt();
        scanner.nextLine(); 
        return value;
    }
    
    //Prompts the user for a double input.
    private static double getDoubleInput(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextDouble()) {
            System.out.print("Invalid. " + prompt);
            scanner.next(); // Skip invalid input
        }
        double value = scanner.nextDouble();
        scanner.nextLine(); 
        return value;
    }

    //Prompts the user for a string input.
    private static String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    //Prompts the user for a Yes or No input.
    private static boolean getYesNoInput(String prompt) {
        System.out.print(prompt);
        return scanner.next().trim().toLowerCase().startsWith("y");
    }

    private static LocalDateTime getDateTimeInput(String prompt) {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        while (true) {
            try {
                System.out.print(prompt);
                return LocalDateTime.parse(scanner.next() + " " + scanner.next(), f);
            } catch (Exception e) {
                System.out.println("Invalid format. Use yyyy-MM-dd HH:mm.");
            }
        }
    }
}
