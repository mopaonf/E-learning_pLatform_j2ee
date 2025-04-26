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

-- Update administrators table to include profile image
ALTER TABLE administrators 
ADD COLUMN profile_image VARCHAR(255),
ALTER TABLE administrators 
ADD COLUMN department_id INT,
ADD FOREIGN KEY (department_id) REFERENCES departments(id);

-- Insert default admin
INSERT INTO administrators (username, name, email, password_hash, password_salt, status, profile_image)
VALUES ('admin', 'System Administrator', 'admin@eduteach.com', 
        SHA2('admin123salt', 256), 'salt', 'active', '/assets/images/admin-profile.jpg')
ON DUPLICATE KEY UPDATE username=username;

-- Update admin credentials to match the login attempt
UPDATE administrators 
SET password_hash = SHA2('admin123salt', 256),
    password_salt = 'salt',
    email = 'admin@eduteach.com',
    name = 'System Administrator',
    status = 'active'
WHERE email = 'admin@eduteach.com';

-- If no admin exists, insert one
INSERT INTO administrators (username, name, email, password_hash, password_salt, status)
SELECT 'admin', 'System Administrator', 'admin@eduteach.com', 
       SHA2('admin123salt', 256), 'salt', 'active'
WHERE NOT EXISTS (
    SELECT 1 FROM administrators WHERE email = 'admin@eduteach.com'
);

-- Verify admin credentials
SELECT admin_id, email, password_hash, password_salt 
FROM administrators 
WHERE email = 'admin@eduteach.com';

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
(2, 'Arts Department', 'active'),
(3, 'Commercial  Department', 'active'),
(4, 'sports Department', 'active')
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

INSERT INTO teachers (name, email, contact, department_id, password_hash, password_salt, status, profile_image)
VALUES ('Dr. Sarah Johnson', 'sarah.johnson@eduteach.com', '123-456-7890', 1, 
        SHA2('password123salt', 256), 'salt', 'active', '/assets/images/sarah-johnson.jpg')
ON DUPLICATE KEY UPDATE email=email;

-- Update the teacher record with department information
UPDATE teachers 
SET name = 'Dr. Sarah Johnson',
    department_id = 1  -- Science Department
WHERE email = 'sarah.johnson@eduteach.com';

-- Update Sarah Johnson's profile image path
UPDATE teachers 
SET profile_image = '/assets/images/sarah-johnson.jpg'
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
    full_name VARCHAR(150) NOT NULL,
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

-- Insert test student with properly hashed password (password: student123)
INSERT INTO students (
    full_name, 
    email, 
    tel,
    gender,
    level, 
    password_hash, 
    password_salt,
    profile_image,
    date_of_birth,
    enrollment_date, 
    status
) VALUES (
    'Rico Simons', 
    'rico@gmail.com',
    '+237 677 011 361',
    'male',
    1,
    SHA2(CONCAT('rico123', 'salt'), 256),
    'salt',
    '/assets/images/rico-simons.jpg',
    '2000-01-01',
    '2023-01-01',
    'active'
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

INSERT INTO teachers (name, email, contact, department_id, password_hash, password_salt, status, profile_image)
VALUES 
('Mr. David Smith', 'david.smith@eduteach.com', '237 677 018 362', 2, SHA2('teachpasssalt2', 256), 'salt2', 'active', '/assets/images/david-smith.jpg'),
('Ms. Linda Okoro', 'linda.okoro@eduteach.com', '237 677 018 363', 3, SHA2('abc123salt3', 256), 'salt3', 'active', '/assets/images/linda-okoro.jpg'),
('Prof. James Kim', 'james.kim@eduteach.com', '237 677 018 364', 4, SHA2('securepasssalt4', 256), 'salt4', 'active', '/assets/images/james-kim.jpg'),
('Mrs. Fatima Bello', 'fatima.bello@eduteach.com', '237 677 018 365', 1, SHA2('mypasswordsalt5', 256), 'salt5', 'active', '/assets/images/fatima-bello.jpg'),
('Mr. Michael Chen', 'michael.chen@eduteach.com', '237 677 018 366', 2, SHA2('letmeinSalt6', 256), 'salt6', 'active', '/assets/images/michael-chen.jpg'),
('Dr. Anita Rao', 'anita.rao@eduteach.com', '237 677 018 367', 3, SHA2('pass123salt7', 256), 'salt7', 'active', '/assets/images/anita-rao.jpg'),
('Mr. Henry Ndlovu', 'henry.ndlovu@eduteach.com', '237 677 018 368', 4, SHA2('adminsalt8', 256), 'salt8', 'active', '/assets/images/henry-ndlovu.jpg'),
('Ms. Chloe Dubois', 'chloe.dubois@eduteach.com', '237 677 018 369', 1, SHA2('edupasssalt9', 256), 'salt9', 'active', '/assets/images/chloe-dubois.jpg'),
('Mr. Samuel Koffi', 'samuel.koffi@eduteach.com', '237 677 018 370', 2, SHA2('randomsalt10', 256), 'salt10', 'active', '/assets/images/samuel-koffi.jpg'),
('Dr. Naomi Hassan', 'naomi.hassan@eduteach.com', '237 677 018 371', 3, SHA2('naomipasssalt11', 256), 'salt11', 'active', '/assets/images/naomi-hassan.jpg'),
('Mr. Alex Gomez', 'alex.gomez@eduteach.com', '237 677 018 372', 4, SHA2('alexpasssalt12', 256), 'salt12', 'active', '/assets/images/alex-gomez.jpg'),
('Mrs. Amina Yusuf', 'amina.yusuf@eduteach.com', '237 677 018 373', 1, SHA2('amina123salt13', 256), 'salt13', 'active', '/assets/images/amina-yusuf.jpg'),
('Mr. Victor Lee', 'victor.lee@eduteach.com', '237 677 018 374', 2, SHA2('teachersalt14', 256), 'salt14', 'active', '/assets/images/victor-lee.jpg'),
('Dr. Brenda Owusu', 'brenda.owusu@eduteach.com', '237 677 018 375', 3, SHA2('owusupasssalt15', 256), 'salt15', 'active', '/assets/images/brenda-owusu.jpg'),
('Mr. Leo Fernandez', 'leo.fernandez@eduteach.com', '237 677 018 376', 4, SHA2('fernandezsalt16', 256), 'salt16', 'active', '/assets/images/leo-fernandez.jpg'),
('Ms. Zainab Musa', 'zainab.musa@eduteach.com', '237 677 018 377', 1, SHA2('zainabsalt17', 256), 'salt17', 'active', '/assets/images/zainab-musa.jpg'),
('Mr. Ethan Brown', 'ethan.brown@eduteach.com', '237 677 018 378', 2, SHA2('ethansalt18', 256), 'salt18', 'active', '/assets/images/ethan-brown.jpg'),
('Dr. Laila Farouk', 'laila.farouk@eduteach.com', '237 677 018 379', 3, SHA2('lailasalt19', 256), 'salt19', 'active', '/assets/images/laila-farouk.jpg'),
('Mr. Peter Mensah', 'peter.mensah@eduteach.com', '237 677 018 380', 4, SHA2('petersalt20', 256), 'salt20', 'active', '/assets/images/peter-mensah.jpg');

UPDATE teachers SET 
    password_salt = 'salt',
    password_hash = SHA2('password123salt', 256)
WHERE email = 'sarah.johnson@eduteach.com';

UPDATE teachers SET 
    password_salt = 'salt',
    password_hash = SHA2('teachpasssalt', 256)
WHERE email = 'david.smith@eduteach.com';

UPDATE teachers SET 
    password_salt = 'salt',
    password_hash = SHA2('abc123salt', 256)
WHERE email = 'linda.okoro@eduteach.com';

UPDATE teachers SET 
    password_salt = 'salt',
    password_hash = SHA2('securepasssalt', 256)
WHERE email = 'james.kim@eduteach.com';

UPDATE teachers SET 
    password_salt = 'salt',
    password_hash = SHA2('mypasswordsalt', 256)
WHERE email = 'fatima.bello@eduteach.com';

UPDATE teachers SET 
    password_salt = 'salt',
    password_hash = SHA2('letmeinSalt', 256)
WHERE email = 'michael.chen@eduteach.com';

UPDATE teachers SET 
    password_salt = 'salt',
    password_hash = SHA2('pass123salt', 256)
WHERE email = 'anita.rao@eduteach.com';

UPDATE teachers SET 
    password_salt = 'salt',
    password_hash = SHA2('adminsalt', 256)
WHERE email = 'henry.ndlovu@eduteach.com';

UPDATE teachers SET 
    password_salt = 'salt',
    password_hash = SHA2('edupasssalt', 256)
WHERE email = 'chloe.dubois@eduteach.com';

UPDATE teachers SET 
    password_salt = 'salt',
    password_hash = SHA2('randomsalt', 256)
WHERE email = 'samuel.koffi@eduteach.com';

UPDATE teachers SET 
    password_salt = 'salt',
    password_hash = SHA2('naomipasssalt', 256)
WHERE email = 'naomi.hassan@eduteach.com';

UPDATE teachers SET 
    password_salt = 'salt',
    password_hash = SHA2('alexpasssalt', 256)
WHERE email = 'alex.gomez@eduteach.com';

UPDATE teachers SET 
    password_salt = 'salt',
    password_hash = SHA2('amina123salt', 256)
WHERE email = 'amina.yusuf@eduteach.com';

UPDATE teachers SET 
    password_salt = 'salt',
    password_hash = SHA2('teachersalt', 256)
WHERE email = 'victor.lee@eduteach.com';

UPDATE teachers SET 
    password_salt = 'salt',
    password_hash = SHA2('owusupasssalt', 256)
WHERE email = 'brenda.owusu@eduteach.com';

UPDATE teachers SET 
    password_salt = 'salt',
    password_hash = SHA2('fernandezsalt', 256)
WHERE email = 'leo.fernandez@eduteach.com';

UPDATE teachers SET 
    password_salt = 'salt',
    password_hash = SHA2('zainabsalt', 256)
WHERE email = 'zainab.musa@eduteach.com';

UPDATE teachers SET 
    password_salt = 'salt',
    password_hash = SHA2('ethansalt', 256)
WHERE email = 'ethan.brown@eduteach.com';

UPDATE teachers SET 
    password_salt = 'salt',
    password_hash = SHA2('lailasalt', 256)
WHERE email = 'laila.farouk@eduteach.com';

UPDATE teachers SET 
    password_salt = 'salt',
    password_hash = SHA2('petersalt', 256)
WHERE email = 'peter.mensah@eduteach.com';


INSERT INTO students (
    full_name, email, tel, gender, level, password_hash, password_salt,
    profile_image, date_of_birth, enrollment_date, status
) VALUES
('Emily Carter', 'emily.carter@example.com', '+237 677 011 362', 'female', 2, SHA2(CONCAT('emily123', 'salt'), 256), 'salt', '/assets/images/emily-carter.jpg', '2001-05-15', '2023-01-01', 'active'),
('Liam Johnson', 'liam.johnson@example.com', '+237 677 011 363', 'male', 1, SHA2(CONCAT('liam123', 'salt'), 256), 'salt', '/assets/images/liam-johnson.jpg', '2002-03-22', '2023-01-01', 'active'),
('Sophia Brown', 'sophia.brown@example.com', '+237 677 011 364', 'female', 3, SHA2(CONCAT('sophia123', 'salt'), 256), 'salt', '/assets/images/sophia-brown.jpg', '2000-09-10', '2023-01-01', 'active'),
('Noah Smith', 'noah.smith@example.com', '+237 677 011 365', 'male', 2, SHA2(CONCAT('noah123', 'salt'), 256), 'salt', '/assets/images/noah-smith.jpg', '1999-12-01', '2023-01-01', 'active'),
('Olivia Davis', 'olivia.davis@example.com', '+237 677 011 366', 'female', 4, SHA2(CONCAT('olivia123', 'salt'), 256), 'salt', '/assets/images/olivia-davis.jpg', '2003-07-17', '2023-01-01', 'active'),
('Benjamin Wilson', 'benjamin.wilson@example.com', '+237 677 011 367', 'male', 3, SHA2(CONCAT('benjamin123', 'salt'), 256), 'salt', '/assets/images/benjamin-wilson.jpg', '2001-04-10', '2023-01-01', 'active'),
('Ava Moore', 'ava.moore@example.com', '+237 677 011 368', 'female', 5, SHA2(CONCAT('ava123', 'salt'), 256), 'salt', '/assets/images/ava-moore.jpg', '2002-02-21', '2023-01-01', 'active'),
('Lucas Taylor', 'lucas.taylor@example.com', '+237 677 011 369', 'male', 2, SHA2(CONCAT('lucas123', 'salt'), 256), 'salt', '/assets/images/lucas-taylor.jpg', '2000-11-09', '2023-01-01', 'active'),
('Mia Anderson', 'mia.anderson@example.com', '+237 677 011 370', 'female', 1, SHA2(CONCAT('mia123', 'salt'), 256), 'salt', '/assets/images/mia-anderson.jpg', '2001-08-23', '2023-01-01', 'active'),
('Ethan Thomas', 'ethan.thomas@example.com', '+237 677 011 371', 'male', 4, SHA2(CONCAT('ethan123', 'salt'), 256), 'salt', '/assets/images/ethan-thomas.jpg', '1998-07-13', '2023-01-01', 'active'),
('Isabella Martin', 'isabella.martin@example.com', '+237 677 011 372', 'female', 5, SHA2(CONCAT('isabella123', 'salt'), 256), 'salt', '/assets/images/isabella-martin.jpg', '2001-12-05', '2023-01-01', 'active'),
('Logan Jackson', 'logan.jackson@example.com', '+237 677 011 373', 'male', 6, SHA2(CONCAT('logan123', 'salt'), 256), 'salt', '/assets/images/logan-jackson.jpg', '2003-06-03', '2023-01-01', 'active'),
('Amelia White', 'amelia.white@example.com', '+237 677 011 374', 'female', 2, SHA2(CONCAT('amelia123', 'salt'), 256), 'salt', '/assets/images/amelia-white.jpg', '2002-10-20', '2023-01-01', 'active'),
('Elijah Harris', 'elijah.harris@example.com', '+237 677 011 375', 'male', 1, SHA2(CONCAT('elijah123', 'salt'), 256), 'salt', '/assets/images/elijah-harris.jpg', '2000-04-14', '2023-01-01', 'active'),
('Harper Lewis', 'harper.lewis@example.com', '+237 677 011 376', 'female', 3, SHA2(CONCAT('harper123', 'salt'), 256), 'salt', '/assets/images/harper-lewis.jpg', '1999-01-26', '2023-01-01', 'active'),
('William Clark', 'william.clark@example.com', '+237 677 011 377', 'male', 5, SHA2(CONCAT('william123', 'salt'), 256), 'salt', '/assets/images/william-clark.jpg', '2002-09-02', '2023-01-01', 'active'),
('Abigail Young', 'abigail.young@example.com', '+237 677 011 378', 'female', 4, SHA2(CONCAT('abigail123', 'salt'), 256), 'salt', '/assets/images/abigail-young.jpg', '2001-06-30', '2023-01-01', 'active'),
('James King', 'james.king@example.com', '+237 677 011 379', 'male', 3, SHA2(CONCAT('james123', 'salt'), 256), 'salt', '/assets/images/james-king.jpg', '2000-05-12', '2023-01-01', 'active'),
('Ella Scott', 'ella.scott@example.com', '+237 677 011 380', 'female', 2, SHA2(CONCAT('ella123', 'salt'), 256), 'salt', '/assets/images/ella-scott.jpg', '2001-07-21', '2023-01-01', 'active'),
('Daniel Green', 'daniel.green@example.com', '+237 677 011 381', 'male', 1, SHA2(CONCAT('daniel123', 'salt'), 256), 'salt', '/assets/images/daniel-green.jpg', '1998-10-30', '2023-01-01', 'active'),
('Scarlett Williams', 'scarlett.w@example.com', '+237 677 011 382', 'female', 3, SHA2(CONCAT('scarlett123', 'salt'), 256), 'salt', '/assets/images/scarlett-williams.jpg', '2001-03-15', '2023-01-01', 'active'),
('Mason Thompson', 'mason.t@example.com', '+237 677 011 383', 'male', 2, SHA2(CONCAT('mason123', 'salt'), 256), 'salt', '/assets/images/mason-thompson.jpg', '2002-08-22', '2023-01-01', 'active'),
('Zoe Parker', 'zoe.p@example.com', '+237 677 011 479', 'female', 1, SHA2(CONCAT('zoe123', 'salt'), 256), 'salt', '/assets/images/zoe-parker.jpg', '2003-12-05', '2023-01-01', 'active');