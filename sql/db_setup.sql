-- Create database if it doesn't exist
CREATE DATABASE IF NOT EXISTS j2ee;
USE j2ee;

-- Create administrators table
CREATE TABLE IF NOT EXISTS administrators (
    admin_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(256) NOT NULL,
    password_salt VARCHAR(64) NOT NULL,
    last_login DATETIME,
    status ENUM('active', 'inactive') DEFAULT 'active'
);

-- Create departments table
CREATE TABLE IF NOT EXISTS departments (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    head_id INT,
    status ENUM('active', 'inactive') DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert default departments
INSERT INTO departments (id, name, status) VALUES 
(1, 'Science Department', 'active'),
(2, 'Mathematics Department', 'active'),
(3, 'Computer Science Department', 'active'),
(4, 'Literature Department', 'active')
ON DUPLICATE KEY UPDATE name=name;

-- Check if table exists and create if not
CREATE TABLE IF NOT EXISTS teachers (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    contact VARCHAR(20),
    department_id INT,
    password_hash VARCHAR(256) NOT NULL,
    password_salt VARCHAR(64) NOT NULL,
    status ENUM('active', 'inactive') DEFAULT 'active',
    profile_image VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (department_id) REFERENCES departments(id)
);

-- Delete and re-insert teacher with email login
DELETE FROM teachers WHERE email = 'sarah.johnson@eduteach.com';

INSERT INTO teachers (name, email, contact, department_id, password_hash, password_salt, status)
VALUES ('Dr. Sarah Johnson', 'sarah.johnson@eduteach.com', '123-456-7890', 1, 
        SHA2('password123salt', 256), 'salt', 'active')
ON DUPLICATE KEY UPDATE email=email;

-- Update the teacher record with department information
UPDATE teachers 
SET name = 'Dr. Sarah Johnson',
    department_id = 1  -- Science Department
WHERE email = 'sarah.johnson@eduteach.com';

-- Verify the teacher information
SELECT t.id, t.name, t.email, d.name as department_name 
FROM teachers t 
LEFT JOIN departments d ON t.department_id = d.id 
WHERE t.email = 'sarah.johnson@eduteach.com';

-- Verify the insertion
SELECT id, name, email, password_hash, password_salt FROM teachers 
WHERE email = 'sarah.johnson@eduteach.com';

-- Add foreign key to departments for head_id
ALTER TABLE departments
ADD FOREIGN KEY (head_id) REFERENCES teachers(id);

-- Create students table
CREATE TABLE IF NOT EXISTS students (
    id INT PRIMARY KEY AUTO_INCREMENT,
    surname VARCHAR(100) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    tel VARCHAR(20),
    gender ENUM('male', 'female', 'other'),
    level VARCHAR(20),
    password_hash VARCHAR(256) NOT NULL,
    password_salt VARCHAR(64) NOT NULL,
    profile_image VARCHAR(255),
    date_of_birth DATE,
    address TEXT,
    city VARCHAR(50),
    state VARCHAR(50),
    postal_code VARCHAR(20),
    country VARCHAR(50),
    major VARCHAR(100),
    enrollment_date DATE,
    status ENUM('active', 'inactive') DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create courses table
CREATE TABLE IF NOT EXISTS courses (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    course_code VARCHAR(20) UNIQUE NOT NULL,
    description TEXT,
    department_id INT,
    teacher_id INT,
    schedule TEXT,
    status ENUM('active', 'inactive') DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (department_id) REFERENCES departments(id),
    FOREIGN KEY (teacher_id) REFERENCES teachers(id)
);

-- Create course_enrollments table
CREATE TABLE IF NOT EXISTS course_enrollments (
    id INT PRIMARY KEY AUTO_INCREMENT,
    course_id INT,
    student_id INT,
    enrollment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('active', 'completed', 'dropped') DEFAULT 'active',
    FOREIGN KEY (course_id) REFERENCES courses(id),
    FOREIGN KEY (student_id) REFERENCES students(id)
);

-- Create assignments table
CREATE TABLE IF NOT EXISTS assignments (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    course_id INT,
    due_date DATETIME NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES courses(id)
);

-- Create assignment_submissions table
CREATE TABLE IF NOT EXISTS assignment_submissions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    assignment_id INT,
    student_id INT,
    submission_file VARCHAR(255),
    submission_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    grade DECIMAL(5,2),
    feedback TEXT,
    FOREIGN KEY (assignment_id) REFERENCES assignments(id),
    FOREIGN KEY (student_id) REFERENCES students(id)
);

-- Create attendance_records table
CREATE TABLE IF NOT EXISTS attendance_records (
    id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT,
    course_id INT,
    date DATE,
    status ENUM('present', 'absent', 'late') NOT NULL,
    attendance_percentage DECIMAL(5,2),
    FOREIGN KEY (student_id) REFERENCES students(id),
    FOREIGN KEY (course_id) REFERENCES courses(id)
);

-- Create messages table
CREATE TABLE IF NOT EXISTS messages (
    id INT PRIMARY KEY AUTO_INCREMENT,
    sender_id INT,
    recipient_id INT,
    subject VARCHAR(255),
    content TEXT,
    sent_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_read BOOLEAN DEFAULT FALSE,
    sender_role ENUM('student', 'teacher', 'admin'),
    recipient_role ENUM('student', 'teacher', 'admin')
);

-- Create calendar_events table
CREATE TABLE IF NOT EXISTS calendar_events (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    event_date DATE NOT NULL,
    event_time TIME,
    event_type VARCHAR(50),
    teacher_id INT,
    FOREIGN KEY (teacher_id) REFERENCES teachers(id)
);

-- Create resources table
CREATE TABLE IF NOT EXISTS resources (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    file_path VARCHAR(255) NOT NULL,
    resource_type VARCHAR(50),
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    teacher_id INT,
    course_id INT,
    FOREIGN KEY (teacher_id) REFERENCES teachers(id),
    FOREIGN KEY (course_id) REFERENCES courses(id)
);

-- Create user_access_log table
CREATE TABLE IF NOT EXISTS user_access_log (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    role ENUM('student', 'teacher', 'admin') NOT NULL,
    access_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45)
);

-- Create login_attempts table
CREATE TABLE IF NOT EXISTS login_attempts (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL,
    role ENUM('student', 'teacher', 'admin') NOT NULL,
    failed_attempts INT DEFAULT 0,
    last_attempt_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_user_role (username, role)
);

-- Create system_settings table
CREATE TABLE IF NOT EXISTS system_settings (
    setting_key VARCHAR(50) PRIMARY KEY,
    setting_value TEXT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insert default system settings
INSERT INTO system_settings (setting_key, setting_value) VALUES
('institution_name', 'Default Institution Name'),
('admin_email', 'admin@example.com'),
('system_theme', 'default'),
('maintenance_mode', 'false');

-- Create teacher_tasks table
CREATE TABLE IF NOT EXISTS teacher_tasks (
    id INT PRIMARY KEY AUTO_INCREMENT,
    teacher_id INT,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    task_date DATE NOT NULL,
    scheduled_time TIME,
    is_completed BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (teacher_id) REFERENCES teachers(id)
);

-- Create student_grades table
CREATE TABLE IF NOT EXISTS student_grades (
    id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT,
    course_id INT,
    grade DECIMAL(5,2),
    letter_grade VARCHAR(2),
    grade_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(id),
    FOREIGN KEY (course_id) REFERENCES courses(id)
);

-- Add indexes for better performance
CREATE INDEX idx_course_enrollments ON course_enrollments(course_id, student_id);
CREATE INDEX idx_assignments ON assignments(course_id, due_date);
CREATE INDEX idx_attendance ON attendance_records(student_id, course_id, date);
CREATE INDEX idx_messages ON messages(recipient_id, is_read);
CREATE INDEX idx_calendar_events ON calendar_events(teacher_id, event_date);
