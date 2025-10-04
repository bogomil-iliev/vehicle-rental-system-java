CREATE DATABASE IF NOT EXISTS vehiclerental;
USE vehiclerental;

CREATE TABLE IF NOT EXISTS users (
    username VARCHAR(50) PRIMARY KEY,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    name VARCHAR(100),
    phone VARCHAR(50),
    email VARCHAR(100),
    address VARCHAR(255)
);

CREATE TABLE vehicles (
    id VARCHAR(50) PRIMARY KEY,
    brand VARCHAR(50),
    model VARCHAR(50),
    price_per_day DOUBLE,
    available BOOLEAN,
    rented BOOLEAN,
    rent_start DATETIME,
    rent_end DATETIME,
    paid BOOLEAN,
    rented_by VARCHAR(50),
    type VARCHAR(20),  -- NEW: required by VehicleDAO
    FOREIGN KEY (rented_by) REFERENCES users(username)
);


CREATE TABLE IF NOT EXISTS rentals (
    id INT AUTO_INCREMENT PRIMARY KEY,
    vehicle_id VARCHAR(50),
    rented_by VARCHAR(50),
    start_time DATETIME,
    end_time DATETIME,
    total_price DOUBLE,
    paid BOOLEAN,
    vehicle_type VARCHAR(50),
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id),
    FOREIGN KEY (rented_by) REFERENCES users(username)
);


INSERT INTO users (username, password, role, name, phone, email, address) VALUES
('admin1', 'adminpass', 'ADMIN', 'Alice Admin', '111-222-3333', 'admin1@example.com', 'Admin St'),
('user1', 'userpass', 'CUSTOMER', 'Bob User', '444-555-6666', 'user1@example.com', 'User Rd');


INSERT INTO vehicles (id, brand, model, price_per_day, available, rented, paid, type)
VALUES
('KM62STU', 'Toyota', 'Corolla', 50, true, false, false, 'Car'),
('RV59RRC', 'Ford', 'Transit', 80, true, false, false, 'Van'),
('YR99JEP', 'Honda', 'CBR500R', 40, true, false, false, 'Motorcycle');
