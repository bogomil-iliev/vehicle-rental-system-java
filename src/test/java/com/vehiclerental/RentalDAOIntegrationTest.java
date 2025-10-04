package com.vehiclerental;

import com.vehiclerental.dao.RentalDAO;
import com.vehiclerental.models.Car;
import com.vehiclerental.models.Vehicle;
import com.vehiclerental.utils.DatabaseConnection;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.MySQLContainer;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test 1 (IT-1): verifies RentalDAO talks to a live MySQL database.
 * DOCKER NEEDS TO BE INSTALLED, OTHERWISE THE TESTS WILL FAIL!!!
 * Requires Testcontainers in pom.xml (mysql 1.19.6).
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RentalDAOIntegrationTest {

    private final MySQLContainer<?> mysql =
            new MySQLContainer<>("mysql:8.0")
                    .withDatabaseName("vehiclerental")
                    .withUsername("root")
                    .withPassword("root");

    private RentalDAO rentalDAO;

    @BeforeAll
    void startContainer() throws Exception {
        mysql.start();
        // Redirect DatabaseConnection to the container 
        DatabaseConnection.overrideJdbcUrl(
                mysql.getJdbcUrl(),
                mysql.getUsername(),
                mysql.getPassword());

            //Bootstrap schema 
        try (Connection c = DatabaseConnection.getConnection();
             Statement  s = c.createStatement()) {

            s.execute("""
                CREATE TABLE rentals (
                  id           INT AUTO_INCREMENT PRIMARY KEY,
                  vehicle_id   VARCHAR(20),
                  rented_by    VARCHAR(40),
                  start_time   DATETIME,
                  end_time     DATETIME,
                  total_price  DOUBLE,
                  paid         BOOLEAN,
                  vehicle_type VARCHAR(20)
                );
            """);
        }
        rentalDAO = new RentalDAO();
    }

    @AfterAll
    void stopContainer() {
        mysql.stop();
    }

    @Test
    void logRental_and_fetchHistory() {

        // Arrange
        Vehicle car = new Car("A1", "Ford", "Focus", 30.0);
        car.setRentedBy("alice");
        car.setRentStartDateTime(LocalDateTime.now());
        car.setRentEndDateTime  (LocalDateTime.now().plusHours(2));
        car.setPaid(false);

        // Act: log row + read it back 
        rentalDAO.logRental(car);
        List<Vehicle> all = rentalDAO.getAllRentalHistory();

        // Assert 
        assertEquals(1, all.size());
        assertEquals("A1", all.get(0).getId());
        assertFalse(all.get(0).isPaid());
    }
}
