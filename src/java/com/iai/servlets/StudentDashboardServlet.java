package com.iai.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * StudentDashboardServlet servlet handles all student dashboard functionality
 * including courses, assignments, grades, schedule, and messages
 * 
 * @author Your Name
 */
@WebServlet("/student/studentDashboard")
public class StudentDashboardServlet extends HttpServlet {
    private static final String URL = "jdbc:mysql://localhost:3306/j2ee";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    
    /**
     * Handles the HTTP GET method to display appropriate page content
     * based on requested URL pattern
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        // Check if user is logged in and is student
        if (session.getAttribute("studentId") == null) {
            response.sendRedirect("../login.jsp");
            return;
        }
        
        request.getRequestDispatcher("studentMainPage.jsp").forward(request, response);
    }

    /**
     * Handles the HTTP POST method for various form submissions
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        String studentId = (String) session.getAttribute("studentId");
        
        // If not logged in, redirect to login page
        if (studentId == null) {
            response.sendRedirect("login.jsp");
            return;
        }
        
        String action = request.getParameter("action");
        
        try {
            if ("updateProfile".equals(action)) {
                updateStudentProfile(request, response, studentId);
            } else if ("submitAssignment".equals(action)) {
                submitAssignment(request, response, studentId);
            } else if ("sendMessage".equals(action)) {
                sendMessage(request, response, studentId);
            } else {
                // Default action - redirect to dashboard
                response.sendRedirect("dashboard");
            }
        } catch (SQLException e) {
            request.setAttribute("error", "Database error: " + e.getMessage());
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "System error: " + e.getMessage());
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    /**
     * Loads the student's basic information for the sidebar
     */
    private void loadStudentInfo(HttpServletRequest request, HttpServletResponse response, String studentId) 
            throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            String sql = "SELECT * FROM students WHERE student_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, studentId);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                Map<String, Object> studentInfo = new HashMap<>();
                studentInfo.put("id", rs.getString("student_id"));
                studentInfo.put("name", rs.getString("first_name") + " " + rs.getString("last_name"));
                studentInfo.put("email", rs.getString("email"));
                studentInfo.put("profileImage", rs.getString("profile_image"));
                
                request.setAttribute("studentInfo", studentInfo);
            }
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * Loads notification counts for messages and alerts
     */
    private void loadNotificationCounts(HttpServletRequest request, HttpServletResponse response, String studentId) 
            throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            
            // Get unread message count
            String msgSql = "SELECT COUNT(*) as message_count FROM messages WHERE recipient_id = ? AND is_read = 0";
            stmt = conn.prepareStatement(msgSql);
            stmt.setString(1, studentId);
            rs = stmt.executeQuery();
            
            int messageCount = 0;
            if (rs.next()) {
                messageCount = rs.getInt("message_count");
            }
            
            // Close and reuse for notifications
            rs.close();
            stmt.close();
            
            // Get unread notification count
            String notifSql = "SELECT COUNT(*) as notification_count FROM notifications WHERE student_id = ? AND is_read = 0";
            stmt = conn.prepareStatement(notifSql);
            stmt.setString(1, studentId);
            rs = stmt.executeQuery();
            
            int notificationCount = 0;
            if (rs.next()) {
                notificationCount = rs.getInt("notification_count");
            }
            
            request.setAttribute("messageCount", messageCount);
            request.setAttribute("notificationCount", notificationCount);
            
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * Loads dashboard data including stats, courses, and assignments
     */
    private void loadDashboardData(HttpServletRequest request, HttpServletResponse response, String studentId) 
            throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            
            // Get enrolled courses count
            String courseSql = "SELECT COUNT(*) as course_count FROM student_courses WHERE student_id = ?";
            stmt = conn.prepareStatement(courseSql);
            stmt.setString(1, studentId);
            rs = stmt.executeQuery();
            
            int courseCount = 0;
            if (rs.next()) {
                courseCount = rs.getInt("course_count");
            }
            
            // Close and reuse for pending assignments
            rs.close();
            stmt.close();
            
            // Get pending assignments count
            String assignmentSql = "SELECT COUNT(*) as assignment_count FROM assignments a " +
                                  "JOIN student_courses sc ON a.course_id = sc.course_id " +
                                  "WHERE sc.student_id = ? AND a.due_date >= CURDATE() AND a.id NOT IN " +
                                  "(SELECT assignment_id FROM assignment_submissions WHERE student_id = ?)";
            stmt = conn.prepareStatement(assignmentSql);
            stmt.setString(1, studentId);
            stmt.setString(2, studentId);
            rs = stmt.executeQuery();
            
            int pendingAssignments = 0;
            if (rs.next()) {
                pendingAssignments = rs.getInt("assignment_count");
            }
            
            // Close and reuse for average grade
            rs.close();
            stmt.close();
            
            // Get average grade
            String gradeSql = "SELECT AVG(grade) as avg_grade FROM student_grades WHERE student_id = ?";
            stmt = conn.prepareStatement(gradeSql);
            stmt.setString(1, studentId);
            rs = stmt.executeQuery();
            
            double avgGrade = 0;
            if (rs.next()) {
                avgGrade = rs.getDouble("avg_grade");
            }
            
            // Close and reuse for attendance
            rs.close();
            stmt.close();
            
            // Get attendance rate
            String attendanceSql = "SELECT " +
                                  "(SUM(CASE WHEN status = 'present' THEN 1 ELSE 0 END) * 100.0 / COUNT(*)) as attendance_rate " +
                                  "FROM attendance WHERE student_id = ?";
            stmt = conn.prepareStatement(attendanceSql);
            stmt.setString(1, studentId);
            rs = stmt.executeQuery();
            
            double attendanceRate = 0;
            if (rs.next()) {
                attendanceRate = rs.getDouble("attendance_rate");
            }
            
            // Close and reuse for current courses
            rs.close();
            stmt.close();
            
            // Get current courses with progress
            String currentCoursesSql = "SELECT c.id, c.title, c.icon, " +
                                      "(SELECT COUNT(*) FROM student_course_enrollments WHERE course_id = c.id) as student_count, " +
                                      "c.schedule, " +
                                      "(SELECT COUNT(*) FROM assignments WHERE course_id = c.id) as assignment_count, " +
                                      "sc.progress " +
                                      "FROM courses c " +
                                      "JOIN student_courses sc ON c.id = sc.course_id " +
                                      "WHERE sc.student_id = ? " +
                                      "ORDER BY sc.progress DESC " +
                                      "LIMIT 2";
            stmt = conn.prepareStatement(currentCoursesSql);
            stmt.setString(1, studentId);
            rs = stmt.executeQuery();
            
            List<Map<String, Object>> currentCourses = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> course = new HashMap<>();
                course.put("id", rs.getInt("id"));
                course.put("title", rs.getString("title"));
                course.put("icon", rs.getString("icon"));
                course.put("studentCount", rs.getInt("student_count"));
                course.put("schedule", rs.getString("schedule"));
                course.put("assignmentCount", rs.getInt("assignment_count"));
                course.put("progress", rs.getInt("progress"));
                currentCourses.add(course);
            }
            
            // Close and reuse for upcoming assignments
            rs.close();
            stmt.close();
            
            // Get upcoming assignments
            String upcomingAssignmentsSql = "SELECT a.id, a.title, a.description, a.due_date, c.title as course_title " +
                                           "FROM assignments a " +
                                           "JOIN courses c ON a.course_id = c.id " +
                                           "JOIN student_courses sc ON c.id = sc.course_id " +
                                           "WHERE sc.student_id = ? AND a.due_date >= CURDATE() " +
                                           "ORDER BY a.due_date ASC " +
                                           "LIMIT 2";
            stmt = conn.prepareStatement(upcomingAssignmentsSql);
            stmt.setString(1, studentId);
            rs = stmt.executeQuery();
            
            List<Map<String, Object>> upcomingAssignments = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> assignment = new HashMap<>();
                assignment.put("id", rs.getInt("id"));
                assignment.put("title", rs.getString("title"));
                assignment.put("description", rs.getString("description"));
                assignment.put("dueDate", rs.getDate("due_date"));
                assignment.put("courseTitle", rs.getString("course_title"));
                upcomingAssignments.add(assignment);
            }
            
            // Set attributes for dashboard stats
            Map<String, Object> dashboardStats = new HashMap<>();
            dashboardStats.put("courseCount", courseCount);
            dashboardStats.put("pendingAssignments", pendingAssignments);
            dashboardStats.put("averageGrade", Math.round(avgGrade));
            dashboardStats.put("attendanceRate", Math.round(attendanceRate));
            
            request.setAttribute("dashboardStats", dashboardStats);
            request.setAttribute("currentCourses", currentCourses);
            request.setAttribute("upcomingAssignments", upcomingAssignments);
            
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * Loads courses data for the student
     */
    private void loadCoursesData(HttpServletRequest request, HttpServletResponse response, String studentId) 
            throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            
            // Get all enrolled courses
            String sql = "SELECT c.id, c.title, c.description, c.icon, " +
                        "(SELECT COUNT(*) FROM student_course_enrollments WHERE course_id = c.id) as student_count, " +
                        "c.schedule, " +
                        "(SELECT COUNT(*) FROM assignments WHERE course_id = c.id) as assignment_count, " +
                        "sc.progress, " +
                        "i.name as instructor_name " +
                        "FROM courses c " +
                        "JOIN student_courses sc ON c.id = sc.course_id " +
                        "JOIN instructors i ON c.instructor_id = i.id " +
                        "WHERE sc.student_id = ? " +
                        "ORDER BY c.title ASC";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, studentId);
            rs = stmt.executeQuery();
            
            List<Map<String, Object>> courses = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> course = new HashMap<>();
                course.put("id", rs.getInt("id"));
                course.put("title", rs.getString("title"));
                course.put("description", rs.getString("description"));
                course.put("icon", rs.getString("icon"));
                course.put("studentCount", rs.getInt("student_count"));
                course.put("schedule", rs.getString("schedule"));
                course.put("assignmentCount", rs.getInt("assignment_count"));
                course.put("progress", rs.getInt("progress"));
                course.put("instructorName", rs.getString("instructor_name"));
                courses.add(course);
            }
            
            request.setAttribute("courses", courses);
            
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * Loads assignments data for the student
     */
    private void loadAssignmentsData(HttpServletRequest request, HttpServletResponse response, String studentId) 
            throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            
            // Get upcoming assignments
            String upcomingSql = "SELECT a.id, a.title, a.description, a.due_date, c.title as course_title " +
                               "FROM assignments a " +
                               "JOIN courses c ON a.course_id = c.id " +
                               "JOIN student_courses sc ON c.id = sc.course_id " +
                               "WHERE sc.student_id = ? AND a.due_date >= CURDATE() " +
                               "ORDER BY a.due_date ASC";
            stmt = conn.prepareStatement(upcomingSql);
            stmt.setString(1, studentId);
            rs = stmt.executeQuery();
            
            List<Map<String, Object>> upcomingAssignments = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> assignment = new HashMap<>();
                assignment.put("id", rs.getInt("id"));
                assignment.put("title", rs.getString("title"));
                assignment.put("description", rs.getString("description"));
                assignment.put("dueDate", rs.getDate("due_date"));
                assignment.put("courseTitle", rs.getString("course_title"));
                upcomingAssignments.add(assignment);
            }
            
            // Close and reuse for completed assignments
            rs.close();
            stmt.close();
            
            // Get completed assignments
            String completedSql = "SELECT a.id, a.title, a.description, as.submission_date, " +
                                "c.title as course_title, as.grade " +
                                "FROM assignments a " +
                                "JOIN assignment_submissions as ON a.id = as.assignment_id " +
                                "JOIN courses c ON a.course_id = c.id " +
                                "WHERE as.student_id = ? " +
                                "ORDER BY as.submission_date DESC";
            stmt = conn.prepareStatement(completedSql);
            stmt.setString(1, studentId);
            rs = stmt.executeQuery();
            
            List<Map<String, Object>> completedAssignments = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> assignment = new HashMap<>();
                assignment.put("id", rs.getInt("id"));
                assignment.put("title", rs.getString("title"));
                assignment.put("description", rs.getString("description"));
                assignment.put("submissionDate", rs.getDate("submission_date"));
                assignment.put("courseTitle", rs.getString("course_title"));
                assignment.put("grade", rs.getInt("grade"));
                completedAssignments.add(assignment);
            }
            
            request.setAttribute("upcomingAssignments", upcomingAssignments);
            request.setAttribute("completedAssignments", completedAssignments);
            
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * Loads grades data for the student
     */
    private void loadGradesData(HttpServletRequest request, HttpServletResponse response, String studentId) 
            throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            
            // Get student grades by course
            String sql = "SELECT c.title as course_title, sg.grade, sg.letter_grade " +
                        "FROM student_grades sg " +
                        "JOIN courses c ON sg.course_id = c.id " +
                        "WHERE sg.student_id = ? " +
                        "ORDER BY c.title ASC";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, studentId);
            rs = stmt.executeQuery();
            
            List<Map<String, Object>> grades = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> grade = new HashMap<>();
                grade.put("courseTitle", rs.getString("course_title"));
                grade.put("grade", rs.getDouble("grade"));
                grade.put("letterGrade", rs.getString("letter_grade"));
                grades.add(grade);
            }
            
            // Calculate GPA
            String gpaSql = "SELECT AVG(grade) as gpa FROM student_grades WHERE student_id = ?";
            stmt = conn.prepareStatement(gpaSql);
            stmt.setString(1, studentId);
            rs = stmt.executeQuery();
            
            double gpa = 0;
            if (rs.next()) {
                gpa = rs.getDouble("gpa");
            }
            
            request.setAttribute("grades", grades);
            request.setAttribute("gpa", String.format("%.2f", gpa));
            
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * Loads schedule data for the student
     */
    private void loadScheduleData(HttpServletRequest request, HttpServletResponse response, String studentId) 
            throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            
            // Get course schedule
            String courseSql = "SELECT c.title, c.schedule, c.location " +
                             "FROM courses c " +
                             "JOIN student_courses sc ON c.id = sc.course_id " +
                             "WHERE sc.student_id = ?";
            stmt = conn.prepareStatement(courseSql);
            stmt.setString(1, studentId);
            rs = stmt.executeQuery();
            
            List<Map<String, Object>> courseSchedule = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> schedule = new HashMap<>();
                schedule.put("title", rs.getString("title"));
                schedule.put("schedule", rs.getString("schedule"));
                schedule.put("location", rs.getString("location"));
                courseSchedule.add(schedule);
            }
            
            // Close and reuse for events
            rs.close();
            stmt.close();
            
            // Get events
            String eventSql = "SELECT e.title, e.event_date, e.description, e.location " +
                            "FROM events e " +
                            "JOIN student_events se ON e.id = se.event_id " +
                            "WHERE se.student_id = ? AND e.event_date >= CURDATE() " +
                            "ORDER BY e.event_date ASC";
            stmt = conn.prepareStatement(eventSql);
            stmt.setString(1, studentId);
            rs = stmt.executeQuery();
            
            List<Map<String, Object>> events = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> event = new HashMap<>();
                event.put("title", rs.getString("title"));
                event.put("date", rs.getDate("event_date"));
                event.put("description", rs.getString("description"));
                event.put("location", rs.getString("location"));
                events.add(event);
            }
            
            // Get current date information for calendar
            SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM");
            SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
            Date currentDate = new Date();
            String currentMonth = monthFormat.format(currentDate);
            String currentYear = yearFormat.format(currentDate);
            
            request.setAttribute("courseSchedule", courseSchedule);
            request.setAttribute("events", events);
            request.setAttribute("currentMonth", currentMonth);
            request.setAttribute("currentYear", currentYear);
            
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * Loads messages data for the student
     */
    private void loadMessagesData(HttpServletRequest request, HttpServletResponse response, String studentId) 
            throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            
            // Get received messages
            String inboxSql = "SELECT m.id, m.subject, m.content, m.sent_date, m.is_read, " +
                            "CONCAT(s.first_name, ' ', s.last_name) as sender_name, s.role " +
                            "FROM messages m " +
                            "JOIN users s ON m.sender_id = s.id " +
                            "WHERE m.recipient_id = ? " +
                            "ORDER BY m.sent_date DESC";
            stmt = conn.prepareStatement(inboxSql);
            stmt.setString(1, studentId);
            rs = stmt.executeQuery();
            
            List<Map<String, Object>> inboxMessages = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> message = new HashMap<>();
                message.put("id", rs.getInt("id"));
                message.put("subject", rs.getString("subject"));
                message.put("content", rs.getString("content"));
                message.put("sentDate", rs.getTimestamp("sent_date"));
                message.put("isRead", rs.getBoolean("is_read"));
                message.put("senderName", rs.getString("sender_name"));
                message.put("senderRole", rs.getString("role"));
                inboxMessages.add(message);
            }
            
            // Close and reuse for sent messages
            rs.close();
            stmt.close();
            
            // Get sent messages
            String sentSql = "SELECT m.id, m.subject, m.content, m.sent_date, " +
                           "CONCAT(r.first_name, ' ', r.last_name) as recipient_name, r.role " +
                           "FROM messages m " +
                           "JOIN users r ON m.recipient_id = r.id " +
                           "WHERE m.sender_id = ? " +
                           "ORDER BY m.sent_date DESC";
            stmt = conn.prepareStatement(sentSql);
            stmt.setString(1, studentId);
            rs = stmt.executeQuery();
            
            List<Map<String, Object>> sentMessages = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> message = new HashMap<>();
                message.put("id", rs.getInt("id"));
                message.put("subject", rs.getString("subject"));
                message.put("content", rs.getString("content"));
                message.put("sentDate", rs.getTimestamp("sent_date"));
                message.put("recipientName", rs.getString("recipient_name"));
                message.put("recipientRole", rs.getString("role"));
                sentMessages.add(message);
            }
            
            // Close and reuse for contacts
            rs.close();
            stmt.close();
            
            // Get contacts (instructors and classmates)
            String contactsSql = "SELECT u.id, CONCAT(u.first_name, ' ', u.last_name) as name, u.role " +
                              "FROM users u " +
                              "WHERE u.id != ? AND (u.role = 'instructor' OR u.id IN " +
                              "(SELECT sc2.student_id FROM student_courses sc1 " +
                              "JOIN student_courses sc2 ON sc1.course_id = sc2.course_id " +
                              "WHERE sc1.student_id = ?)) " +
                              "ORDER BY u.role DESC, name ASC";
            stmt = conn.prepareStatement(contactsSql);
            stmt.setString(1, studentId);
            stmt.setString(2, studentId);
            rs = stmt.executeQuery();
            
            List<Map<String, Object>> contacts = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> contact = new HashMap<>();
                contact.put("id", rs.getString("id"));
                contact.put("name", rs.getString("name"));
                contact.put("role", rs.getString("role"));
                contacts.add(contact);
            }
            
            request.setAttribute("inboxMessages", inboxMessages);
            request.setAttribute("sentMessages", sentMessages);
            request.setAttribute("contacts", contacts);
            
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * Loads resources data for the student
     */
    private void loadResourcesData(HttpServletRequest request, HttpServletResponse response, String studentId) 
            throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            
            // Get course resources
            String sql = "SELECT r.id, r.title, r.description, r.file_path, r.upload_date, " +
                       "c.title as course_title " +
                       "FROM resources r " +
                       "JOIN courses c ON r.course_id = c.id " +
                       "JOIN student_courses sc ON c.id = sc.course_id " +
                       "WHERE sc.student_id = ? " +
                       "ORDER BY r.upload_date DESC";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, studentId);
            rs = stmt.executeQuery();
            
            List<Map<String, Object>> resources = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> resource = new HashMap<>();
                resource.put("id", rs.getInt("id"));
                resource.put("title", rs.getString("title"));
                resource.put("description", rs.getString("description"));
                resource.put("filePath", rs.getString("file_path"));
                resource.put("uploadDate", rs.getDate("upload_date"));
                resource.put("courseTitle", rs.getString("course_title"));
                resources.add(resource);
            }
            
            request.setAttribute("resources", resources);
            
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * Loads settings data for the student
     */
    private void loadSettingsData(HttpServletRequest request, HttpServletResponse response, String studentId) 
            throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            
            // Get student profile data
            String sql = "SELECT s.first_name, s.last_name, s.email, s.phone, " +
                       "s.profile_image, s.address, s.city, s.state, s.postal_code, " +
                       "s.country, s.date_of_birth, s.major " +
                       "FROM students s " +
                       "WHERE s.student_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, studentId);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                Map<String, Object> profile = new HashMap<>();
                profile.put("firstName", rs.getString("first_name"));
                profile.put("lastName", rs.getString("last_name"));
                profile.put("email", rs.getString("email"));
                profile.put("phone", rs.getString("phone"));
                profile.put("profileImage", rs.getString("profile_image"));
                profile.put("address", rs.getString("address"));
                profile.put("city", rs.getString("city"));
                profile.put("state", rs.getString("state"));
                profile.put("postalCode", rs.getString("postal_code"));
                profile.put("country", rs.getString("country"));
                profile.put("dateOfBirth", rs.getDate("date_of_birth"));
                profile.put("major", rs.getString("major"));
                
                request.setAttribute("profile", profile);
            }
            
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    private Map<String, Object> loadStudentProfile(Connection conn, String studentId) throws SQLException {
        Map<String, Object> profile = new HashMap<>();
        String sql = "SELECT s.first_name, s.last_name, s.email, s.phone, s.profile_image, " +
                    "s.date_of_birth, s.gender, s.address, s.major, s.enrollment_date " +
                    "FROM students s WHERE s.student_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                profile.put("firstName", rs.getString("first_name"));
                profile.put("lastName", rs.getString("last_name"));
                profile.put("email", rs.getString("email"));
                profile.put("phone", rs.getString("phone"));
                profile.put("profileImage", rs.getString("profile_image"));
                profile.put("dateOfBirth", rs.getDate("date_of_birth"));
                profile.put("gender", rs.getString("gender"));
                profile.put("address", rs.getString("address"));
                profile.put("major", rs.getString("major"));
                profile.put("enrollmentDate", rs.getDate("enrollment_date"));
            }
        }
        return profile;
    }

    private void updateProfile(HttpServletRequest request, HttpServletResponse response, String studentId) 
            throws SQLException, IOException {
        String sql = "UPDATE students SET email = ?, phone = ?, address = ? WHERE student_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, request.getParameter("email"));
            stmt.setString(2, request.getParameter("phone"));
            stmt.setString(3, request.getParameter("address"));
            stmt.setString(4, studentId);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                response.sendRedirect("dashboard?action=settings&success=true");
            } else {
                handleError(response, "Failed to update profile");
            }
        }
    }

    private void loadClassSchedule(HttpServletRequest request, HttpServletResponse response, String studentId) 
            throws SQLException {
        String sql = "SELECT c.course_name, cs.day_of_week, cs.start_time, cs.end_time, cs.room_number " +
                    "FROM class_schedule cs " +
                    "JOIN courses c ON cs.course_id = c.course_id " +
                    "JOIN student_enrollments se ON c.course_id = se.course_id " +
                    "WHERE se.student_id = ? " +
                    "ORDER BY cs.day_of_week, cs.start_time";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentId);
            ResultSet rs = stmt.executeQuery();
            
            List<Map<String, Object>> schedule = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> classInfo = new HashMap<>();
                classInfo.put("courseName", rs.getString("course_name"));
                classInfo.put("day", rs.getString("day_of_week"));
                classInfo.put("startTime", rs.getTime("start_time"));
                classInfo.put("endTime", rs.getTime("end_time"));
                classInfo.put("room", rs.getString("room_number"));
                schedule.add(classInfo);
            }
            request.setAttribute("classSchedule", schedule);
        }
    }

    private void loadCourseResources(HttpServletRequest request, HttpServletResponse response, String studentId) 
            throws SQLException {
        String sql = "SELECT r.resource_id, r.title, r.description, r.file_path, r.upload_date, " +
                    "c.course_name, t.teacher_name " +
                    "FROM course_resources r " +
                    "JOIN courses c ON r.course_id = c.course_id " +
                    "JOIN teachers t ON c.teacher_id = t.teacher_id " +
                    "JOIN student_enrollments se ON c.course_id = se.course_id " +
                    "WHERE se.student_id = ? " +
                    "ORDER BY r.upload_date DESC";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentId);
            ResultSet rs = stmt.executeQuery();
            
            List<Map<String, Object>> resources = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> resource = new HashMap<>();
                resource.put("id", rs.getInt("resource_id"));
                resource.put("title", rs.getString("title"));
                resource.put("description", rs.getString("description"));
                resource.put("filePath", rs.getString("file_path"));
                resource.put("uploadDate", rs.getTimestamp("upload_date"));
                resource.put("courseName", rs.getString("course_name"));
                resource.put("teacherName", rs.getString("teacher_name"));
                resources.add(resource);
            }
            request.setAttribute("courseResources", resources);
        }
    }

    private void closeResources(Connection conn, PreparedStatement stmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            // Log the error but don't throw it
            e.printStackTrace();
        }
    }

    private void handleError(HttpServletResponse response, String message) throws IOException {
        response.setContentType("text/plain");
        response.getWriter().write(message);
    }

    /**
     * Updates the student's profile information
     */
    private void updateStudentProfile(HttpServletRequest request, HttpServletResponse response, String studentId) 
            throws SQLException, IOException {
        String sql = "UPDATE students SET first_name = ?, last_name = ?, email = ?, phone = ?, " +
                    "profile_image = ?, address = ?, city = ?, state = ?, postal_code = ?, " +
                    "country = ?, date_of_birth = ?, major = ? WHERE student_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, request.getParameter("firstName"));
            stmt.setString(2, request.getParameter("lastName"));
            stmt.setString(3, request.getParameter("email"));
            stmt.setString(4, request.getParameter("phone"));
            stmt.setString(5, request.getParameter("profileImage"));
            stmt.setString(6, request.getParameter("address"));
            stmt.setString(7, request.getParameter("city"));
            stmt.setString(8, request.getParameter("state"));
            stmt.setString(9, request.getParameter("postalCode"));
            stmt.setString(10, request.getParameter("country"));
            stmt.setDate(11, java.sql.Date.valueOf(request.getParameter("dateOfBirth")));
            stmt.setString(12, request.getParameter("major"));
            stmt.setString(13, studentId);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                response.sendRedirect("settings?success=true");
            } else {
                handleError(response, "Failed to update profile");
            }
        }
    }

    /**
     * Submits an assignment for the student
     */
    private void submitAssignment(HttpServletRequest request, HttpServletResponse response, String studentId) 
            throws SQLException, IOException {
        String sql = "INSERT INTO assignment_submissions (assignment_id, student_id, submission_file, submission_date) " +
                    "VALUES (?, ?, ?, NOW())";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(request.getParameter("assignmentId")));
            stmt.setString(2, studentId);
            stmt.setString(3, request.getParameter("submissionFile"));
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                response.sendRedirect("assignments?success=true");
            } else {
                handleError(response, "Failed to submit assignment");
            }
        }
    }

    /**
     * Sends a message from the student
     */
    private void sendMessage(HttpServletRequest request, HttpServletResponse response, String studentId) 
            throws SQLException, IOException {
        String sql = "INSERT INTO messages (sender_id, recipient_id, subject, content, sent_date) " +
                    "VALUES (?, ?, ?, ?, NOW())";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentId);
            stmt.setString(2, request.getParameter("recipientId"));
            stmt.setString(3, request.getParameter("subject"));
            stmt.setString(4, request.getParameter("content"));
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                response.sendRedirect("messages?success=true");
            } else {
                handleError(response, "Failed to send message");
            }
        }
    }

    /**
     * Gets a connection to the database
     */
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}