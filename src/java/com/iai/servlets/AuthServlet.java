package com.iai.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.DriverManager; // Add this import
import java.nio.charset.StandardCharsets; // Add this import

/**
 * AuthServlet handles all authentication operations including login, signup, and logout
 * for different user roles: students, teachers, and administrators.
 * 
 * @author EduTeach
 * @version 1.0
 */
@WebServlet("/AuthServlet")
public class AuthServlet extends HttpServlet {
    
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(AuthServlet.class.getName());
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOCKOUT_TIME_MINUTES = 30;
    
    // Database connection parameters
    private static final String DB_URL = "jdbc:mysql://localhost:3306/j2ee";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    
    // CSRF token name
    private static final String CSRF_TOKEN_NAME = "csrfToken";
    
    /**
     * Handles GET requests for authentication pages
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        if (action == null) {
            action = "showLogin";
        }
        
        switch (action) {
            case "logout":
                logout(request, response);
                break;
            case "showLogin":
                showLoginPage(request, response);
                break;
            case "showSignup":
                showSignupPage(request, response);
                break;
            default:
                showLoginPage(request, response);
        }
    }
    
    /**
     * Handles POST requests for authentication actions
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        // Validate CSRF token for all POST requests
        if (!validateCsrfToken(request)) {
            request.setAttribute("error", "Invalid request. Please try again.");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }
        
        if (action == null) {
            showLoginPage(request, response);
            return;
        }
        
        switch (action) {
            case "login":
                login(request, response);
                break;
            case "signup":
                signup(request, response);
                break;
            default:
                showLoginPage(request, response);
        }
    }
    
    /**
     * Display login page with new CSRF token
     */
    private void showLoginPage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Generate and set CSRF token
        String csrfToken = generateCsrfToken(request);
        request.setAttribute("csrfToken", csrfToken);
        
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }
    
    /**
     * Display signup page with new CSRF token
     */
    private void showSignupPage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Generate and set CSRF token
        String csrfToken = generateCsrfToken(request);
        request.setAttribute("csrfToken", csrfToken);
        
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }
    
    /**
     * Process login request
     */
    private void login(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String email = request.getParameter("email"); // Changed from username
        String password = request.getParameter("password");
        String role = request.getParameter("role");
        
        if (email == null || password == null || role == null) { // Updated error message
            request.setAttribute("error", "Email, password, and role are required");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }
        
        try {
            // Check for account lockout
            if (isAccountLocked(email, role)) {
                request.setAttribute("error", "Account is temporarily locked. Please try again later.");
                request.getRequestDispatcher("/login.jsp").forward(request, response);
                return;
            }
            
            // Attempt database connection and authentication
            try (Connection conn = DatabaseUtil.getConnection()) {
                boolean authenticated = authenticateUser(conn, email, password, role);
                
                if (authenticated) {
                    // Reset login attempts on successful login
                    resetLoginAttempts(conn, email, role);
                    
                    // Create session and store user information
                    HttpSession session = request.getSession(true);
                    storeUserInSession(conn, session, email, role);
                    
                    // Generate new CSRF token for the session
                    generateCsrfToken(request);
                    
                    // Log the successful login
                    LOGGER.info("User " + email + " logged in successfully as " + role);
                    
                    // Redirect based on role
                    redirectByRole(response, role);
                } else {
                    // Increment failed login attempts
                    incrementLoginAttempts(conn, email, role);
                    
                    request.setAttribute("error", "Invalid email or password");
                    request.getRequestDispatcher("/login.jsp").forward(request, response);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during login", e);
            request.setAttribute("error", "System error. Please try again later.");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }
    
    /**
     * Process signup request (for students only)
     */
    private void signup(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String fullname = request.getParameter("fullname");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        
        // Validate inputs
        if (fullname == null || email == null || phone == null || password == null || confirmPassword == null) {
            request.setAttribute("error", "All fields are required");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            request.setAttribute("error", "Passwords do not match");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }
        
        // Password strength validation
        if (password.length() < 8) {
            request.setAttribute("error", "Password must be at least 8 characters long");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }
        
        try (Connection conn = DatabaseUtil.getConnection()) {
            // Check if email already exists
            if (emailExists(conn, email)) {
                request.setAttribute("error", "Email already registered");
                request.getRequestDispatcher("/login.jsp").forward(request, response);
                return;
            }
            
            // Create salt for password hashing
            byte[] salt = generateSalt();
            // Hash the password
            String hashedPassword = hashPassword(password, salt);
            
            // Insert new student record
            if (createStudentAccount(conn, fullname, email, phone, hashedPassword, Base64.getEncoder().encodeToString(salt))) {
                // Show success message and redirect to login
                request.setAttribute("success", "Account created successfully. Please login.");
                request.getRequestDispatcher("/login.jsp").forward(request, response);
            } else {
                request.setAttribute("error", "Failed to create account. Please try again.");
                request.getRequestDispatcher("/login.jsp").forward(request, response);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during signup", e);
            request.setAttribute("error", "System error. Please try again later.");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.log(Level.SEVERE, "Error generating password hash", e);
            request.setAttribute("error", "System error. Please try again later.");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }
    
    /**
     * Process logout request
     */
    private void logout(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session != null) {
            // Log the logout
            String username = (String) session.getAttribute("username");
            String role = (String) session.getAttribute("role");
            if (username != null && role != null) {
                LOGGER.info("User " + username + " logged out as " + role);
            }
            
            // Invalidate session
            session.invalidate();
        }
        
        // Redirect to login page
        response.sendRedirect("AuthServlet?action=showLogin");
    }
    
    /**
     * Authenticate user based on credentials and role
     */
    private boolean authenticateUser(Connection conn, String username, String password, String role)
            throws SQLException {
        
        LOGGER.info("Attempting to authenticate user: " + username + " with role: " + role);
        
        String tableName, idColumn, emailColumn, passwordColumn, saltColumn;
        
        // Determine table and column names based on role
        switch (role) {
            case "teacher":
                tableName = "teachers";
                idColumn = "id";
                emailColumn = "email";  // Always use email for teachers
                passwordColumn = "password_hash";
                saltColumn = "password_salt";
                break;
            case "student":
                tableName = "students";
                idColumn = "student_id";
                emailColumn = "email";
                passwordColumn = "password_hash";
                saltColumn = "password_salt";
                break;
            case "admin":
                tableName = "administrators";
                idColumn = "admin_id";
                emailColumn = "username";
                passwordColumn = "password_hash";
                saltColumn = "password_salt";
                break;
            default:
                return false;
        }
        
        String sql = "SELECT " + idColumn + ", " + passwordColumn + ", " + saltColumn + 
                    " FROM " + tableName + " WHERE " + emailColumn + " = ?";
        
        LOGGER.info("Executing SQL: " + sql + " with username: " + username);
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String storedPassword = rs.getString(passwordColumn);
                String salt = rs.getString(saltColumn);
                
                LOGGER.info("Found user record. Attempting password verification");
                
                // For teachers specifically, use SHA2
                if (role.equals("teacher")) {
                    String hashedAttempt = hashWithSHA2(password + salt);
                    LOGGER.info("Teacher auth - Password with salt: " + (password + salt));
                    LOGGER.info("Teacher auth - Generated hash: " + hashedAttempt);
                    LOGGER.info("Teacher auth - Stored hash: " + storedPassword);
                    return storedPassword.equals(hashedAttempt);
                } else {
                    // Original authentication for other roles
                    try {
                        byte[] saltBytes = Base64.getDecoder().decode(salt);
                        String hashedPassword = hashPassword(password, saltBytes);
                        return storedPassword.equals(hashedPassword);
                    } catch (NoSuchAlgorithmException e) {
                        LOGGER.log(Level.SEVERE, "Error verifying password", e);
                        return false;
                    }
                }
            } else {
                LOGGER.warning("No user found with username: " + username);
            }
        }
        
        return false;
    }
    
    /**
     * Hashes a string using SHA2-256
     */
    private String hashWithSHA2(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            
            // Convert to hexadecimal
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            LOGGER.log(Level.SEVERE, "Error hashing with SHA2", e);
            return null;
        }
    }
    
    /**
     * Store user information in session after successful authentication
     */
    private void storeUserInSession(Connection conn, HttpSession session, String username, String role)
            throws SQLException {
        
        if (role.equals("teacher")) {
            String sql = "SELECT t.id, t.name, t.email, d.name as department_name " +
                        "FROM teachers t " +
                        "LEFT JOIN departments d ON t.department_id = d.id " +
                        "WHERE t.email = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    int userId = rs.getInt("id");
                    String name = rs.getString("name");
                    String departmentName = rs.getString("department_name");
                    
                    // Store user information in session
                    session.setAttribute("userId", userId);
                    session.setAttribute("username", username);
                    session.setAttribute("teacherName", name);
                    session.setAttribute("departmentName", departmentName);
                    session.setAttribute("role", role);
                    session.setAttribute("teacherId", userId);
                    
                    // Set session timeout (30 minutes)
                    session.setMaxInactiveInterval(30 * 60);
                    
                    // Log access time
                    logUserAccess(conn, userId, role);
                }
            }
        } else {
            String tableName, idColumn, nameColumn, usernameColumn;
        
            // Determine table and column names based on role
            switch (role) {
                case "teacher":
                    tableName = "teachers";
                    idColumn = "id";           // Changed from teacher_id to id
                    nameColumn = "name";       // Changed from surname to name
                    usernameColumn = "email";
                    break;
                case "student":
                    tableName = "students";
                    idColumn = "student_id";
                    nameColumn = "surname";
                    usernameColumn = "email";
                    break;
                case "admin":
                    tableName = "administrators";
                    idColumn = "admin_id";
                    nameColumn = "name";
                    usernameColumn = "username";
                    break;
                default:
                    return;
            }
            
            String sql = "SELECT " + idColumn + ", " + nameColumn + 
                        " FROM " + tableName + " WHERE " + usernameColumn + " = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    int userId = rs.getInt(idColumn);
                    String name = rs.getString(nameColumn);
                    
                    // Store user information in session
                    session.setAttribute("userId", userId);
                    session.setAttribute("username", username);
                    session.setAttribute("name", name);
                    session.setAttribute("role", role);
                    
                    // Set role-specific attributes
                    switch (role) {
                        case "student":
                            session.setAttribute("studentId", userId);
                            break;
                        case "teacher":
                            session.setAttribute("teacherId", userId);
                            break;
                        case "admin":
                            session.setAttribute("adminId", userId);
                            break;
                    }
                    
                    // Set session timeout (30 minutes)
                    session.setMaxInactiveInterval(30 * 60);
                    
                    // Log access time
                    logUserAccess(conn, userId, role);
                }
            }
        }
    }
    
    /**
     * Log user access time for audit purposes
     */
    private void logUserAccess(Connection conn, int userId, String role) throws SQLException {
        String sql = "INSERT INTO user_access_log (user_id, role, access_time, ip_address) VALUES (?, ?, NOW(), ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, role);
            stmt.setString(3, ""); // We would get the IP address here
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Redirect user to appropriate page based on role
     */
    private void redirectByRole(HttpServletResponse response, String role) throws IOException {
        switch (role) {
            case "student":
                response.sendRedirect("student/studentsMainPage.jsp");
                break;
            case "teacher":
                response.sendRedirect("teacher/teacherMainPage.jsp");
                break;
            case "admin":
                response.sendRedirect("admin/adminMainPage.jsp");
                break;
            default:
                response.sendRedirect("AuthServlet?action=showLogin");
        }
    }
    
    /**
     * Check if email already exists for student signup
     */
    private boolean emailExists(Connection conn, String email) throws SQLException {
        String sql = "SELECT student_id FROM students WHERE email = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            
            return rs.next();
        }
    }
    
    /**
     * Create a new student account
     */
    private boolean createStudentAccount(Connection conn, String fullname, String email, String phone, 
                                        String hashedPassword, String salt) throws SQLException {
        
        String sql = "INSERT INTO students (surname, email, tel, password_hash, password_salt, registration_date) " +
                    "VALUES (?, ?, ?, ?, ?, NOW())";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, fullname);
            stmt.setString(2, email);
            stmt.setString(3, phone);
            stmt.setString(4, hashedPassword);
            stmt.setString(5, salt);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    /**
     * Generate a random salt for password hashing
     */
    private byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }
    
    /**
     * Hash a password with a given salt using SHA-256
     */
    private String hashPassword(String password, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(salt);
        byte[] hashedPassword = md.digest(password.getBytes());
        
        // Convert to hexadecimal string
        StringBuilder sb = new StringBuilder();
        for (byte b : hashedPassword) {
            sb.append(String.format("%02x", b));
        }
        
        return sb.toString();
    }
    
    /**
     * Generate a CSRF token and store it in session
     */
    private String generateCsrfToken(HttpServletRequest request) {
    String token = UUID.randomUUID().toString();
    HttpSession session = request.getSession(true);
    session.setAttribute(CSRF_TOKEN_NAME, token);
    LOGGER.info("Generated new CSRF token: " + token);
    return token;
}
    
    /**
     * Validate CSRF token from request
     */
    private boolean validateCsrfToken(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session == null) {
        LOGGER.warning("No session found during CSRF validation");
        return true;
    }
    
    String sessionToken = (String) session.getAttribute(CSRF_TOKEN_NAME);
    String requestToken = request.getParameter(CSRF_TOKEN_NAME);
    
    LOGGER.info("CSRF Validation - Session Token: " + sessionToken);
    LOGGER.info("CSRF Validation - Request Token: " + requestToken);
    
    if (sessionToken == null || requestToken == null) {
        LOGGER.warning("Missing CSRF token - Session: " + (sessionToken == null) + ", Request: " + (requestToken == null));
        return false;
    }
    
    boolean isValid = sessionToken.equals(requestToken);
    LOGGER.info("CSRF Validation Result: " + isValid);
    return isValid;
}
    
    /**
     * Check if account is locked due to too many failed attempts
     */
    private boolean isAccountLocked(String username, String role) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "SELECT failed_attempts, last_attempt_time FROM login_attempts " +
                        "WHERE username = ? AND role = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                stmt.setString(2, role);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    int attempts = rs.getInt("failed_attempts");
                    LocalDateTime lastAttempt = rs.getTimestamp("last_attempt_time").toLocalDateTime();
                    
                    if (attempts >= MAX_LOGIN_ATTEMPTS) {
                        LocalDateTime lockoutExpiry = lastAttempt.plusMinutes(LOCKOUT_TIME_MINUTES);
                        return LocalDateTime.now().isBefore(lockoutExpiry);
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * Increment failed login attempts
     */
    private void incrementLoginAttempts(Connection conn, String username, String role) throws SQLException {
        String sql = "INSERT INTO login_attempts (username, role, failed_attempts, last_attempt_time) " +
                    "VALUES (?, ?, 1, NOW()) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "failed_attempts = failed_attempts + 1, " +
                    "last_attempt_time = NOW()";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, role);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Reset login attempts after successful login
     */
    private void resetLoginAttempts(Connection conn, String username, String role) throws SQLException {
        String sql = "DELETE FROM login_attempts WHERE username = ? AND role = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, role);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Utility class for database operations
     */
    private static class DatabaseUtil {
        public static Connection getConnection() throws SQLException {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            } catch (ClassNotFoundException e) {
                throw new SQLException("Database driver not found", e);
            }
        }
    }
}