package com.vehiclerental.services;

import com.vehiclerental.models.User;
import com.vehiclerental.dao.UserDAO;
import java.sql.SQLException;
import java.util.*;

/*
 * Handles user authentication and management for the Vehicle Rental System.
 * Supports login, registration, user lookup, and user updates.
 * Maintains an in-memory map of users synced with the database.
 */
public class AuthService {
    private Map<String, User> users = new HashMap<>();
    private UserDAO userDAO;

    //Initialises the AuthService by loading user data from the database into an in-memory map for quick access.
    public AuthService() {
        userDAO = new UserDAO();
        loadUsersFromDatabase();
    }

    //Loads all users from the database into the internal user map. Used during initialisation to populate in-memory user records
    private void loadUsersFromDatabase() {
        try {
            List<User> dbUsers = userDAO.getAllUsers();
            for (User user : dbUsers) {
                users.put(user.getUsername(), user);
            }
        } catch (SQLException e) {
            System.out.println("Error loading users from DB: " + e.getMessage());
        }
    }

    //Registers a new user with full contact details. Saves the user to the database and adds them to the internal map.
    public boolean register(String username, String password, String role, String name, String phone, String email, String address) {
        if (users.containsKey(username)) return false;
        User user = new User(username, password, role, name, phone, email, address);
        try {
            boolean success = userDAO.saveUser(user);
            if (success) {
                users.put(username, user);
            }
            return success;
        } catch (SQLException e) {
            System.out.println("Error saving user: " + e.getMessage());
            return false;
        }
    }

    //Registers a new user with only basic credentials and role. Fills remaining fields with default placeholder values.
    public boolean register(String username, String password, String role) {
        return register(username, password, role, "Unknown", "N/A", "N/A", "N/A");
    }

    //Authenticates a user based on username and password.
    public User login(String username, String password) {
        User user = users.get(username);
        return (user != null && user.getPassword().equals(password)) ? user : null;
    }

    //Retrieves a user by username from the internal map.
    public User getUser(String username) {
        return users.get(username);
    }

    //Retrieves all users currently loaded in memory.
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    //Updates the contact details of an existing user. Applies changes to both the in-memory user map and the database.
    public boolean updateUser(String username, String newName, String newPhone, String newEmail, String newAddress) {
        User user = users.get(username);
        if (user != null) {
            user.setName(newName);
            user.setPhone(newPhone);
            user.setEmail(newEmail);
            user.setAddress(newAddress);
            try {
                boolean updated = userDAO.updateUser(user);
                if (updated) {
                    users.put(username, user);
                }
                return updated;
            } catch (SQLException e) {
                System.out.println("Error updating user: " + e.getMessage());
                return false;
            }
        }
        return false;
    }

    //Deletes a user from the system by username. 
    public boolean deleteUser(String username) {
        if (users.containsKey(username)) {
            try {
                boolean deleted = userDAO.deleteUser(username);
                if (deleted) {
                    users.remove(username);
                }
                return deleted;
            } catch (SQLException e) {
                System.out.println("Error deleting user: " + e.getMessage());
                return false;
            }
        }
        return false;
    }

    //Removes the user from both the database and the internal map if present.
    public boolean removeUser(String username) {
        return deleteUser(username);
    }
}
