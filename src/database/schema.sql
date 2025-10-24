CREATE TABLE admin_users (
admin_id INT AUTO_INCREMENT PRIMARY KEY,
username VARCHAR(255) NOT NULL UNIQUE,
password_hash VARCHAR(255) NOT NULL, 
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE airports (
airport_id INT AUTO_INCREMENT PRIMARY KEY,
airport_code VARCHAR(10) NOT NULL,  
airport_name VARCHAR(255) NOT NULL,
city VARCHAR(255) NOT NULL,
country VARCHAR(255) NOT NULL,
UNIQUE (airport_code)  
);

CREATE TABLE flight_owners (
owner_id INT AUTO_INCREMENT PRIMARY KEY,
company_name VARCHAR(255) NOT NULL,
company_code VARCHAR(50) NOT NULL UNIQUE,  
contact_info VARCHAR(255),
flight_count INT DEFAULT 0,
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
password VARCHAR(255) NOT NULL
);

CREATE TABLE flight_data (
flight_id INT AUTO_INCREMENT PRIMARY KEY,
company_id INT NOT NULL,  
flight_code VARCHAR(50) NOT NULL UNIQUE,  
flight_name VARCHAR(255) NOT NULL,
capacity INT NOT NULL, 
departure_airport_id INT NOT NULL,  
destination_airport_id INT NOT NULL,
departure_time DATETIME NOT NULL,  
destination_time DATETIME NOT NULL,
price DECIMAL(10, 2) NOT NULL,  
FOREIGN KEY (company_id) REFERENCES flight_owners(owner_id),
FOREIGN KEY (departure_airport_id) REFERENCES airports(airport_id),
FOREIGN KEY (destination_airport_id) REFERENCES airports(airport_id)
);

CREATE TABLE users (
user_id INT AUTO_INCREMENT PRIMARY KEY,
first_name VARCHAR(255) NOT NULL,
last_name VARCHAR(255) NOT NULL,
email VARCHAR(255) NOT NULL UNIQUE,
phone VARCHAR(20),
DOB date,
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
password VARCHAR(255) NOT NULL
);

CREATE TABLE bookings (
booking_id INT AUTO_INCREMENT PRIMARY KEY,
user_id INT NOT NULL,  
flight_id INT NOT NULL,
departure_airport_id INT NOT NULL,
destination_airport_id INT NOT NULL,
departure_time DATETIME NOT NULL,  
destination_time DATETIME NOT NULL,
PNR VARCHAR(20) UNIQUE NOT NULL,  
date_of_departure DATE NOT NULL,
date_of_destination DATE NOT NULL,
date_of_booking TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
amount DECIMAL(10, 2) NOT NULL, 
payment_status ENUM('Pending', 'Completed', 'Failed') NOT NULL,  
booking_status ENUM('Pending', 'Confirmed', 'Cancelled') NOT NULL,
FOREIGN KEY (flight_id) REFERENCES flight_data(flight_id),
FOREIGN KEY (departure_airport_id) REFERENCES airports(airport_id),
FOREIGN KEY (destination_airport_id) REFERENCES airports(airport_id),
FOREIGN KEY (user_id) REFERENCES users(user_id)  
);

CREATE INDEX idx_flight_id ON bookings(flight_id);
CREATE INDEX idx_user_id ON bookings(user_id);
CREATE INDEX idx_departure_airport_id ON flight_data(departure_airport_id);
CREATE INDEX idx_destination_airport_id ON flight_data(destination_airport_id);
