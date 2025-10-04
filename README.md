# Vehicle Rental System (Java, MySQL, CLI)

![Java](https://img.shields.io/badge/Java-23-blue)
![Maven](https://img.shields.io/badge/Maven-3.9+-blue)
![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)

A layered **CLI** Vehicle Rental System in **Java (Maven)** with **MySQL/JDBC**:
- **RBAC** (Admin/Customer), registration/login
- Vehicle/user **CRUD**, **conflict-free booking** (time-window overlap checks), **payment status**
- **Notifications**: upcoming/overdue returns, upcoming bookings
- **DAO/Service/CLI** architecture, unit tests (**JUnit 5/Mockito**) + **MySQL Testcontainers** integration test

> Detailed design, OOP principles & patterns, and testing strategy are documented in `docs/ain7302_report.pdf`. 

---

## Quickstart

### 0) Prereqs
- **JDK 23** (matches `pom.xml` `maven.compiler.release=23`).  
  *(If you prefer JDK 17, set `<maven.compiler.release>17</maven.compiler.release>` and ensure dependencies work.)*
- **MySQL 8.x** (for local run)
- **Docker** (if you want to run the integration test with Testcontainers)

### 1) Database setup (local MySQL)
```bash
# Create schema + seed users/vehicles
mysql -u root -p < sql/vehiclerental_mysql.sql
```
The Schema and seed mirror the app’s expectations (users, vehicles, rentals).

**Configure credentials: By default the code uses jdbc:mysql://localhost:3306/vehiclerental and root:root.**

Update either:
  - src/main/java/com/vehiclerental/utils/DatabaseConnection.java, and the duplicated constants in src/main/java/com/vehiclerental/dao/UserDAO.java and .../VehicleDAO.java

### 2) Build & test
```bash
# run unit tests
mvn -q -Dtest='*Test' test

# include the integration test (requires Docker running)
mvn -q test
```

### 3) Run the CLI

**Simplest (IDE):** run com.vehiclerental.Main.
**From CLI (two options):**
 
 A) Exec plugin (if you add it):
 
  ```bash
    mvn -q exec:java -Dexec.mainClass="com.vehiclerental.Main"
  ```

B) Uber-jar (if you add shade plugin):
  
  ```bash
    mvn -q -DskipTests package
    java -jar target/vehiclerentalapp-1.0-SNAPSHOT-shaded.jar
  ```

 If you don’t add an exec/shade plugin yet, you can also run:
   
   ```bash
    mvn -q compile && java -cp target/classes com.vehiclerental.Main
   ```

## Architecture & patterns (high level)
   - **Three-tier:** CLI (presentation) → Services (business logic) → DAOs (persistence)
   - **Patterns:** DAO, Service/Façade, GRASP Controller; SRP, DIP, OCP in practice (see report).
   - **Testing:** JUnit + Mockito; **Testcontainers** spins MySQL for RentalDAOIntegrationTest (Docker required).

## Screens & diagrams
 - ERD / Class / Sequence / Collaboration diagrams → docs/figures/*.png
 - Manual tests: docs/figures/MT1..MT5 screenshots (registration, booking, overlap rejection, payment, notifications)    

## Repo map
```bash
src/
  main/java/com/vehiclerental/...
    services/ (MainMenu, AuthService, VehicleManager, NotificationService)
    dao/      (UserDAO, VehicleDAO, RentalDAO)
    models/   (Vehicle, Car, Van, Motorcycle, User, RentalInfo)
    utils/    (DatabaseConnection)
  test/java/com/vehiclerental/...
    AuthServiceTest, VehicleManagerTest, NotificationServiceTest
    RentalDAOIntegrationTest  # requires Docker
sql/
  vehiclerental_mysql.sql
docs/
  ain7302_report.pdf
  figures/ (ERD, ClassDiagram, Sequence_*, Collaboration_*, MT*_*.png)
pom.xml
```

## Notes & roadmap
- **Config:** consolidate DB config via DatabaseConnection (remove duplicate URL/user/pass in DAOs).
- **Security:** move passwords to env or application.properties; hash user passwords.
- **Packaging:** add maven-shade-plugin or exec-maven-plugin for one-command run.
- **Logging:** add slf4j + logback (replace System.out).
- **CI:** add GitHub Actions “Maven” workflow; optionally enable services for Testcontainers.

## License 
**MIT**

## Citation
[➡️ Cite this repository](./CITATION.cff)

