package com.vehiclerental.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/* Centralised JDBC connection helper.
     Production code uses the default URL, user, and password. <br>
    Integration tests can override those values at runtime via
    {@link #overrideJdbcUrl(String, String, String)}.
 */
public class DatabaseConnection {

    // default values (local MySQL)
    private static String url      = "jdbc:mysql://localhost:3306/vehiclerental";
    private static String username = "root";
    private static String password = "root";

    private static Connection connection;

    //Returns a lazily initialised, reusable Connection.
     
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(url, username, password);
        }
        return connection;
    }

    /* -----------------------------------------------------------------------
       TESTING HELPER
       -----------------------------------------------------------------------
       Allows Testcontainers (or other test setups) to redirect JDBC traffic
       without touching production configuration.
     */
    public static void overrideJdbcUrl(String newUrl,
                                       String newUser,
                                       String newPass) {
        url      = newUrl;
        username = newUser;
        password = newPass;
        // close previous connection so next getConnection() uses new settings
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException ignore) { /* ignore on test cleanup */ }
        connection = null;  // force re-initialisation
    }

    // prevent instantiation
    private DatabaseConnection() { }
}
