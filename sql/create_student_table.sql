CREATE TABLE students (
    id INT AUTO_INCREMENT PRIMARY KEY,
    surname VARCHAR(255) NOT NULL,
    level INT NOT NULL,
    tel VARCHAR(15) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    gender ENUM('male', 'female', 'other') NOT NULL
);
