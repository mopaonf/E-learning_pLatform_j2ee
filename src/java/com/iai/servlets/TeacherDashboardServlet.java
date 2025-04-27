package com.iai.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * TeacherDashboardServlet handles all teacher dashboard functionality
 * including courses, students, assignments, grades, tasks, and calendar events.
 * 
 * @author Your Name
 */
@WebServlet("/teacher/dashboard")
public class TeacherDashboardServlet extends HttpServlet {
    
    private static final String DB_URL = "jdbc:mysql://localhost:3306/j2ee";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    
    /**
     * Handles GET requests to load dashboard data or specific page data
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(); // Ensure session is declared only once
        Integer teacherId = (Integer) session.getAttribute("teacherId");

        if (teacherId == null) {
            response.sendRedirect("../login.jsp");
            return;
        }

        String action = request.getParameter("action");
        if ("loadGrades".equals(action)) {
            loadGrades(request, response, teacherId);
            return;
        }
        if ("loadCourseStudents".equals(action)) {
            int courseId = Integer.parseInt(request.getParameter("courseId"));

            String sql = "SELECT s.id, s.full_name, s.email, s.tel, s.gender, s.level " +
                         "FROM students s " +
                         "JOIN course_enrollments ce ON s.id = ce.student_id " +
                         "WHERE ce.course_id = ?";

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, courseId);
                try (ResultSet rs = stmt.executeQuery()) {
                    List<Map<String, Object>> students = new ArrayList<>();
                    while (rs.next()) {
                        Map<String, Object> student = new HashMap<>();
                        student.put("id", rs.getInt("id"));
                        student.put("fullName", rs.getString("full_name"));
                        student.put("email", rs.getString("email"));
                        student.put("tel", rs.getString("tel"));
                        student.put("gender", rs.getString("gender"));
                        student.put("level", rs.getString("level"));
                        students.add(student);
                    }
                    response.setContentType("application/json");
                    response.getWriter().write(new JSONArray(students).toString());
                }
            } catch (SQLException e) {
                e.printStackTrace();
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error loading course students");
            }
            return;
        }

        try (Connection conn = getConnection()) {
            // Fetch total students
            int totalStudents = getTotalStudents(conn, teacherId);
            request.setAttribute("totalStudents", totalStudents);

            // Fetch active courses
            int activeCourses = getActiveCourses(conn, teacherId);
            request.setAttribute("activeCourses", activeCourses);

            // Fetch courses taught by the teacher
            List<Map<String, Object>> courses = getCoursesByTeacher(conn, teacherId);
            request.setAttribute("courses", courses);

            // Fetch students associated with the teacher's courses
            List<Map<String, Object>> students = getStudentsByTeacher(conn, teacherId);
            request.setAttribute("students", students);

            // Fetch assignments
            List<Map<String, Object>> assignments = getAssignmentsByTeacher(conn, teacherId);
            request.setAttribute("assignments", assignments);

            // Fetch grades
            List<Map<String, Object>> grades = getGradesByTeacher(conn, teacherId);
            request.setAttribute("grades", grades);

            // Forward to teacher main page
            request.getRequestDispatcher("/teacher/teacherMainPage.jsp").forward(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect("../error.jsp");
        }
    }
    
    /**
     * Handles all POST requests for creating or updating data
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        // Check if user is logged in
        if (session.getAttribute("teacherId") == null) {
            response.sendRedirect("login.jsp");
            return;
        }
        
        int teacherId = (int) session.getAttribute("teacherId");
        String action = request.getParameter("action");
        
        if (action == null) {
            response.sendRedirect("teacherDashboard");
            return;
        }
        
        switch (action) {
            case "addCourse":
                addCourse(request, response, teacherId);
                break;
            case "addStudent":
                addStudent(request, response);
                break;
            case "addAssignment":
                addAssignment(request, response, teacherId);
                break;
            case "updateGrade":
                updateGrade(request, response, teacherId);
                break;
            case "addTask":
                addTask(request, response, teacherId);
                break;
            case "updateTask":
                updateTask(request, response, teacherId);
                break;
            case "addCalendarEvent":
                addCalendarEvent(request, response, teacherId);
                break;
            case "sendMessage":
                sendMessage(request, response, teacherId);
                break;
            case "uploadResource":
                uploadResource(request, response, teacherId);
                break;
            case "updateSettings":
                updateSettings(request, response, teacherId);
                break;
            case "addGrade":
                addGrade(request, response, teacherId);
                break;
            case "editGrade":
                editGrade(request, response);
                break;
            case "deleteGrade":
                deleteGrade(request, response);
                break;
            case "editAssignment":
                editAssignment(request, response);
                break;
            case "deleteAssignment":
                deleteAssignment(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action: " + action);
                break;
        }
    }
    
    /**
     * Loads all data needed for the main dashboard
     */
    private void loadDashboardData(HttpServletRequest request, int teacherId) {
        try {
            Connection conn = getConnection();
            
            // Get total students count
            int totalStudents = getStudentCount(conn, teacherId);
            request.setAttribute("totalStudents", totalStudents);
            
            // Get active courses count
            int activeCourses = getActiveCourseCount(conn, teacherId);
            request.setAttribute("activeCourses", activeCourses);
            
            // Get pending assignments count
            int pendingAssignments = getPendingAssignmentCount(conn, teacherId);
            request.setAttribute("pendingAssignments", pendingAssignments);
            
            // Get average completion rate
            double avgCompletion = getAverageCompletionRate(conn, teacherId);
            request.setAttribute("avgCompletion", avgCompletion);
            
            // Get recent classes
            List<Map<String, Object>> recentClasses = getRecentClasses(conn, teacherId);
            request.setAttribute("recentClasses", recentClasses);
            
            // Get today's tasks
            List<Map<String, Object>> todaysTasks = getTodaysTasks(conn, teacherId);
            request.setAttribute("todaysTasks", todaysTasks);
            
            // Get calendar events for current month
            List<Map<String, Object>> calendarEvents = getCalendarEvents(conn, teacherId);
            request.setAttribute("calendarEvents", calendarEvents);
            
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("error", "Error loading dashboard data: " + e.getMessage());
        }
    }
    
    /**
     * Gets connection to the database
     */
    private Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found: " + e.getMessage());
        }
    }
    
    /**
     * Gets total number of students across all teacher's courses
     */
    private int getStudentCount(Connection conn, int teacherId) throws SQLException {
        String sql = "SELECT COUNT(DISTINCT s.student_id) AS total FROM students s " +
                    "JOIN course_enrollments ce ON s.student_id = ce.student_id " +
                    "JOIN courses c ON ce.course_id = c.course_id " +
                    "WHERE c.teacher_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teacherId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
            return 0;
        }
    }
    
    /**
     * Gets number of active courses for teacher
     */
    private int getActiveCourseCount(Connection conn, int teacherId) throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM courses WHERE teacher_id = ? AND status = 'active'";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teacherId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
            return 0;
        }
    }
    
    /**
     * Gets number of pending assignments
     */
    private int getPendingAssignmentCount(Connection conn, int teacherId) throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM assignments a " +
                    "JOIN courses c ON a.course_id = c.course_id " +
                    "WHERE c.teacher_id = ? AND a.due_date >= CURRENT_DATE()";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teacherId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
            return 0;
        }
    }
    
    /**
     * Gets average completion rate for teacher's assignments
     */
    private double getAverageCompletionRate(Connection conn, int teacherId) throws SQLException {
        String sql = "SELECT AVG(completion_rate) AS avg_rate FROM " +
                    "(SELECT a.assignment_id, " +
                    "COUNT(sa.submission_id) * 100.0 / COUNT(ce.student_id) AS completion_rate " +
                    "FROM assignments a " +
                    "JOIN courses c ON a.course_id = c.course_id " +
                    "JOIN course_enrollments ce ON c.course_id = ce.course_id " +
                    "LEFT JOIN student_assignments sa ON a.assignment_id = sa.assignment_id AND sa.student_id = ce.student_id " +
                    "WHERE c.teacher_id = ? " +
                    "AND a.due_date < CURRENT_DATE() " +
                    "GROUP BY a.assignment_id) AS completion_data";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teacherId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double avgRate = rs.getDouble("avg_rate");
                return rs.wasNull() ? 0 : avgRate;
            }
            return 0;
        }
    }
    
    /**
     * Gets recent classes for the teacher
     */
    private List<Map<String, Object>> getRecentClasses(Connection conn, int teacherId) throws SQLException {
        String sql = "SELECT c.course_name, cl.class_date, cl.start_time, cl.end_time, " +
                    "COUNT(ce.student_id) AS student_count, " +
                    "CASE WHEN cl.class_date < CURRENT_DATE() OR " +
                    "(cl.class_date = CURRENT_DATE() AND cl.end_time < CURRENT_TIME()) " +
                    "THEN 'Completed' ELSE 'Upcoming' END AS status " +
                    "FROM classes cl " +
                    "JOIN courses c ON cl.course_id = c.course_id " +
                    "LEFT JOIN course_enrollments ce ON c.course_id = ce.course_id " +
                    "WHERE c.teacher_id = ? " +
                    "GROUP BY c.course_name, cl.class_date, cl.start_time, cl.end_time " +
                    "ORDER BY cl.class_date DESC, cl.start_time DESC " +
                    "LIMIT 5";
        
        List<Map<String, Object>> classes = new ArrayList<>();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teacherId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> classInfo = new HashMap<>();
                classInfo.put("courseName", rs.getString("course_name"));
                classInfo.put("date", rs.getDate("class_date").toString());
                classInfo.put("time", rs.getString("start_time") + " - " + rs.getString("end_time"));
                classInfo.put("students", rs.getInt("student_count"));
                classInfo.put("status", rs.getString("status"));
                
                classes.add(classInfo);
            }
        }
        
        return classes;
    }
    
    /**
     * Gets today's tasks for the teacher
     */
    private List<Map<String, Object>> getTodaysTasks(Connection conn, int teacherId) throws SQLException {
        String sql = "SELECT task_id, task_title, scheduled_time, is_completed " +
                    "FROM teacher_tasks " +
                    "WHERE teacher_id = ? AND task_date = CURRENT_DATE() " +
                    "ORDER BY scheduled_time";
        
        List<Map<String, Object>> tasks = new ArrayList<>();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teacherId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> task = new HashMap<>();
                task.put("id", rs.getInt("task_id"));
                task.put("title", rs.getString("task_title"));
                task.put("time", rs.getTime("scheduled_time").toString());
                task.put("completed", rs.getBoolean("is_completed"));
                
                tasks.add(task);
            }
        }
        
        return tasks;
    }
    
    /**
     * Gets calendar events for the current month
     */
    private List<Map<String, Object>> getCalendarEvents(Connection conn, int teacherId) throws SQLException {
        String sql = "SELECT event_date FROM calendar_events " +
                    "WHERE teacher_id = ? AND MONTH(event_date) = MONTH(CURRENT_DATE()) " +
                    "AND YEAR(event_date) = YEAR(CURRENT_DATE())";
        
        List<Map<String, Object>> events = new ArrayList<>();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teacherId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> event = new HashMap<>();
                event.put("date", rs.getDate("event_date").toString());
                events.add(event);
            }
        }
        
        return events;
    }
    
    /**
     * Loads courses data for the teacher
     */
    private void loadCourses(HttpServletRequest request, HttpServletResponse response, int teacherId) 
            throws ServletException, IOException {
        try {
            Connection conn = getConnection();
            
            String sql = "SELECT c.course_id, c.course_name, c.course_code, c.schedule, " +
                        "COUNT(DISTINCT ce.student_id) AS student_count, " +
                        "COUNT(DISTINCT a.assignment_id) AS assignment_count " +
                        "FROM courses c " +
                        "LEFT JOIN course_enrollments ce ON c.course_id = ce.course_id " +
                        "LEFT JOIN assignments a ON c.course_id = a.course_id " +
                        "WHERE c.teacher_id = ? AND c.status = 'active' " +
                        "GROUP BY c.course_id, c.course_name, c.course_code, c.schedule";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, teacherId);
            
            ResultSet rs = stmt.executeQuery();
            List<Map<String, Object>> courses = new ArrayList<>();
            
            while (rs.next()) {
                Map<String, Object> course = new HashMap<>();
                course.put("id", rs.getInt("course_id"));
                course.put("name", rs.getString("course_name"));
                course.put("code", rs.getString("course_code"));
                course.put("schedule", rs.getString("schedule"));
                course.put("studentCount", rs.getInt("student_count"));
                course.put("assignmentCount", rs.getInt("assignment_count"));
                
                courses.add(course);
            }
            
            request.setAttribute("courses", courses);
            conn.close();
            
            // Forward to courses page or return JSON
            if (request.getParameter("format") != null && request.getParameter("format").equals("json")) {
                response.setContentType("application/json");
                response.getWriter().write(new JSONArray(courses).toString());
            } else {
                request.getRequestDispatcher("/teacher/teacherMainPage.jsp").forward(request, response);
            }
        } catch (SQLException e) {
            handleError(response, "Error loading courses: " + e.getMessage());
        }
    }
    
    /**
     * Loads students data for the teacher's courses
     */
    private void loadStudents(HttpServletRequest request, HttpServletResponse response, int teacherId) 
            throws ServletException, IOException {
        try {
            Connection conn = getConnection();
            
            String sql = "SELECT DISTINCT s.student_id, s.surname, s.level, s.email, s.tel, s.gender " +
                        "FROM students s " +
                        "JOIN course_enrollments ce ON s.student_id = ce.student_id " +
                        "JOIN courses c ON ce.course_id = c.course_id " +
                        "WHERE c.teacher_id = ? " +
                        "ORDER BY s.surname";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, teacherId);
            
            ResultSet rs = stmt.executeQuery();
            List<Map<String, Object>> students = new ArrayList<>();
            
            while (rs.next()) {
                Map<String, Object> student = new HashMap<>();
                student.put("id", rs.getInt("student_id"));
                student.put("name", rs.getString("surname"));
                student.put("level", rs.getString("level"));
                student.put("email", rs.getString("email"));
                student.put("tel", rs.getString("tel"));
                student.put("gender", rs.getString("gender"));
                
                students.add(student);
            }
            
            request.setAttribute("students", students);
            conn.close();
            
            // Forward to students page or return JSON
            if (request.getParameter("format") != null && request.getParameter("format").equals("json")) {
                response.setContentType("application/json");
                response.getWriter().write(new JSONArray(students).toString());
            } else {
                request.getRequestDispatcher("teacherMainPage.jsp").forward(request, response);
            }
        } catch (SQLException e) {
            handleError(response, "Error loading students: " + e.getMessage());
        }
    }
    
    /**
     * Loads assignments data
     */
    private void loadAssignments(HttpServletRequest request, HttpServletResponse response, int teacherId) 
            throws ServletException, IOException {
        try {
            Connection conn = getConnection();
            
            String sql = "SELECT a.assignment_id, a.title, c.course_name, a.due_date, " +
                        "CASE WHEN a.due_date < CURRENT_DATE() THEN 'Closed' " +
                        "WHEN a.due_date = CURRENT_DATE() THEN 'Due Today' " +
                        "ELSE 'Pending' END AS status " +
                        "FROM assignments a " +
                        "JOIN courses c ON a.course_id = c.course_id " +
                        "WHERE c.teacher_id = ? " +
                        "ORDER BY a.due_date ASC";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, teacherId);
            
            ResultSet rs = stmt.executeQuery();
            List<Map<String, Object>> assignments = new ArrayList<>();
            
            while (rs.next()) {
                Map<String, Object> assignment = new HashMap<>();
                assignment.put("id", rs.getInt("assignment_id"));
                assignment.put("title", rs.getString("title"));
                assignment.put("course", rs.getString("course_name"));
                assignment.put("dueDate", rs.getDate("due_date").toString());
                assignment.put("status", rs.getString("status"));
                
                assignments.add(assignment);
            }
            
            request.setAttribute("assignments", assignments);
            conn.close();
            
            // Forward to assignments page or return JSON
            if (request.getParameter("format") != null && request.getParameter("format").equals("json")) {
                response.setContentType("application/json");
                response.getWriter().write(new JSONArray(assignments).toString());
            } else {
                request.getRequestDispatcher("teacherMainPage.jsp").forward(request, response);
            }
        } catch (SQLException e) {
            handleError(response, "Error loading assignments: " + e.getMessage());
        }
    }
    
    /**
     * Loads grades data
     */
    private void loadGrades(HttpServletRequest request, HttpServletResponse response, int teacherId) 
            throws ServletException, IOException {
        try (Connection conn = getConnection()) {
            List<Map<String, Object>> grades = getGradesByTeacher(conn, teacherId);
            request.setAttribute("grades", grades);

            // Forward to grades page or return JSON
            if ("json".equals(request.getParameter("format"))) {
                response.setContentType("application/json");
                response.getWriter().write(new JSONArray(grades).toString());
            } else {
                request.getRequestDispatcher("/teacher/teacherMainPage.jsp").forward(request, response);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect("../error.jsp");
        }
    }
    
    /**
     * Loads calendar events
     */
    private void loadCalendarEvents(HttpServletRequest request, HttpServletResponse response, int teacherId) 
            throws ServletException, IOException {
        try {
            Connection conn = getConnection();
            
            String month = request.getParameter("month");
            String year = request.getParameter("year");
            
            // If month and year are not provided, use current month and year
            if (month == null || year == null) {
                month = java.time.LocalDate.now().getMonthValue() + "";
                year = java.time.LocalDate.now().getYear() + "";
            }
            
            String sql = "SELECT event_id, event_title, event_date, event_time, event_type " +
                        "FROM calendar_events " +
                        "WHERE teacher_id = ? AND MONTH(event_date) = ? AND YEAR(event_date) = ?";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, teacherId);
            stmt.setString(2, month);
            stmt.setString(3, year);
            
            ResultSet rs = stmt.executeQuery();
            List<Map<String, Object>> events = new ArrayList<>();
            
            while (rs.next()) {
                Map<String, Object> event = new HashMap<>();
                event.put("id", rs.getInt("event_id"));
                event.put("title", rs.getString("event_title"));
                event.put("date", rs.getDate("event_date").toString());
                event.put("time", rs.getString("event_time"));
                event.put("type", rs.getString("event_type"));
                
                events.add(event);
            }
            
            request.setAttribute("events", events);
            request.setAttribute("month", month);
            request.setAttribute("year", year);
            conn.close();
            
            // Forward to calendar page or return JSON
            if (request.getParameter("format") != null && request.getParameter("format").equals("json")) {
                response.setContentType("application/json");
                response.getWriter().write(new JSONArray(events).toString());
            } else {
                request.getRequestDispatcher("teacherMainPage.jsp").forward(request, response);
            }
        } catch (SQLException e) {
            handleError(response, "Error loading calendar events: " + e.getMessage());
        }
    }
    
    /**
     * Loads messages
     */
    private void loadMessages(HttpServletRequest request, HttpServletResponse response, int teacherId) 
            throws ServletException, IOException {
        try {
            Connection conn = getConnection();
            
            String sql = "SELECT m.message_id, s.surname, m.message_content, m.date_sent, m.is_read " +
                        "FROM messages m " +
                        "JOIN students s ON m.student_id = s.student_id " +
                        "WHERE m.teacher_id = ? " +
                        "ORDER BY m.date_sent DESC";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, teacherId);
            
            ResultSet rs = stmt.executeQuery();
            List<Map<String, Object>> messages = new ArrayList<>();
            
            while (rs.next()) {
                Map<String, Object> message = new HashMap<>();
                message.put("id", rs.getInt("message_id"));
                message.put("student", rs.getString("surname"));
                message.put("content", rs.getString("message_content"));
                message.put("date", rs.getTimestamp("date_sent").toString());
                message.put("isRead", rs.getBoolean("is_read"));
                
                messages.add(message);
            }
            
            request.setAttribute("messages", messages);
            conn.close();
            
            // Forward to messages page or return JSON
            if (request.getParameter("format") != null && request.getParameter("format").equals("json")) {
                response.setContentType("application/json");
                response.getWriter().write(new JSONArray(messages).toString());
            } else {
                request.getRequestDispatcher("teacherMainPage.jsp").forward(request, response);
            }
        } catch (SQLException e) {
            handleError(response, "Error loading messages: " + e.getMessage());
        }
    }
    
    /**
     * Loads resources
     */
    private void loadResources(HttpServletRequest request, HttpServletResponse response, int teacherId) 
            throws ServletException, IOException {
        try {
            Connection conn = getConnection();
            
            String sql = "SELECT resource_id, resource_name, resource_type, upload_date, file_path " +
                        "FROM resources " +
                        "WHERE teacher_id = ? " +
                        "ORDER BY upload_date DESC";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, teacherId);
            
            ResultSet rs = stmt.executeQuery();
            List<Map<String, Object>> resources = new ArrayList<>();
            
            while (rs.next()) {
                Map<String, Object> resource = new HashMap<>();
                resource.put("id", rs.getInt("resource_id"));
                resource.put("name", rs.getString("resource_name"));
                resource.put("type", rs.getString("resource_type"));
                resource.put("date", rs.getDate("upload_date").toString());
                resource.put("path", rs.getString("file_path"));
                
                resources.add(resource);
            }
            
            request.setAttribute("resources", resources);
            conn.close();
            
            // Forward to resources page or return JSON
            if (request.getParameter("format") != null && request.getParameter("format").equals("json")) {
                response.setContentType("application/json");
                response.getWriter().write(new JSONArray(resources).toString());
            } else {
                request.getRequestDispatcher("teacherMainPage.jsp").forward(request, response);
            }
        } catch (SQLException e) {
            handleError(response, "Error loading resources: " + e.getMessage());
        }
    }
    
    /**
     * Loads tasks
     */
    private void loadTasks(HttpServletRequest request, HttpServletResponse response, int teacherId) 
            throws ServletException, IOException {
        try {
            Connection conn = getConnection();
            
            String date = request.getParameter("date");
            if (date == null) {
                date = java.time.LocalDate.now().toString();
            }
            
            String sql = "SELECT task_id, task_title, scheduled_time, is_completed " +
                        "FROM teacher_tasks " +
                        "WHERE teacher_id = ? AND task_date = ? " +
                        "ORDER BY scheduled_time";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, teacherId);
            stmt.setString(2, date);
            
            ResultSet rs = stmt.executeQuery();
            List<Map<String, Object>> tasks = new ArrayList<>();
            
            while (rs.next()) {
                Map<String, Object> task = new HashMap<>();
                task.put("id", rs.getInt("task_id"));
                task.put("title", rs.getString("task_title"));
                task.put("time", rs.getTime("scheduled_time").toString());
                task.put("completed", rs.getBoolean("is_completed"));
                
                tasks.add(task);
            }
            
            request.setAttribute("tasks", tasks);
            request.setAttribute("selectedDate", date);
            conn.close();
            
            // Forward to tasks page or return JSON
            if (request.getParameter("format") != null && request.getParameter("format").equals("json")) {
                response.setContentType("application/json");
                response.getWriter().write(new JSONArray(tasks).toString());
            } else {
                request.getRequestDispatcher("teacherMainPage.jsp").forward(request, response);
            }
        } catch (SQLException e) {
            handleError(response, "Error loading tasks: " + e.getMessage());
        }
    }
    
    /**
     * Adds a new course
     */
    private void addCourse(HttpServletRequest request, HttpServletResponse response, int teacherId) 
            throws ServletException, IOException {
        try {
            Connection conn = getConnection();
            
            String courseName = request.getParameter("courseName");
            String courseCode = request.getParameter("courseCode");
            String schedule = request.getParameter("schedule");
            String description = request.getParameter("description");
            
            String sql = "INSERT INTO courses (course_name, course_code, schedule, description, teacher_id, status) " +
                        "VALUES (?, ?, ?, ?, ?, 'active')";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, courseName);
            stmt.setString(2, courseCode);
            stmt.setString(3, schedule);
            stmt.setString(4, description);
            stmt.setInt(5, teacherId);
            
            int rowsAffected = stmt.executeUpdate();
            conn.close();
            
            if (rowsAffected > 0) {
                response.sendRedirect("teacherDashboard?action=loadCourses&success=true");
            } else {
                handleError(response, "Failed to add course");
            }
        } catch (SQLException e) {
            handleError(response, "Error adding course: " + e.getMessage());
        }
    }
    
    /**
     * Adds a new student
     */
    private void addStudent(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            Connection conn = getConnection();
            
            String studentName = request.getParameter("studentName");
            String studentEmail = request.getParameter("studentEmail");
            String studentTel = request.getParameter("studentTel");
            String studentGender = request.getParameter("studentGender");
            String studentLevel = request.getParameter("studentLevel");
            
            String sql = "INSERT INTO students (surname, email, tel, gender, level) " +
                        "VALUES (?, ?, ?, ?, ?)";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, studentName);
            stmt.setString(2, studentEmail);
            stmt.setString(3, studentTel);
            stmt.setString(4, studentGender);
            stmt.setString(5, studentLevel);
            
            int rowsAffected = stmt.executeUpdate();
            conn.close();
            
            if (rowsAffected > 0) {
                response.sendRedirect("teacherDashboard?action=loadStudents&success=true");
            } else {
                handleError(response, "Failed to add student");
            }
        } catch (SQLException e) {
            handleError(response, "Error adding student: " + e.getMessage());
        }
    }
    
    /**
     * Adds a new assignment
     */
    private void addAssignment(HttpServletRequest request, HttpServletResponse response, int teacherId) 
            throws IOException {
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        int courseId = Integer.parseInt(request.getParameter("courseId"));
        String dueDate = request.getParameter("dueDate");

        String sql = "INSERT INTO assignments (title, description, course_id, due_date) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, title);
            stmt.setString(2, description);
            stmt.setInt(3, courseId);
            stmt.setString(4, dueDate);
            stmt.executeUpdate();

            // Redirect back to the same page to avoid duplicate submissions
            response.sendRedirect(request.getContextPath() + "/teacher/dashboard");
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect("../error.jsp");
        }
    }
    
    /**
     * Updates a grade
     */
    private void updateGrade(HttpServletRequest request, HttpServletResponse response, int teacherId) 
            throws IOException {
        String gradeId = request.getParameter("gradeId");
        String gradeValue = request.getParameter("gradeValue");

        String sql = "UPDATE grades SET grade_value = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, gradeValue);
            stmt.setInt(2, Integer.parseInt(gradeId));
            stmt.executeUpdate();

            response.sendRedirect(request.getContextPath() + "/teacher/dashboard?action=loadGrades");
        } catch (SQLException e) {
            handleError(response, "Error updating grade: " + e.getMessage());
        }
    }
    
    /**
     * Adds a new task for the teacher
     */
    private void addTask(HttpServletRequest request, HttpServletResponse response, int teacherId) 
            throws ServletException, IOException {
        try (Connection conn = getConnection()) {
            String sql = "INSERT INTO teacher_tasks (teacher_id, task_title, task_date, scheduled_time, is_completed) " +
                        "VALUES (?, ?, ?, ?, false)";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, teacherId);
            stmt.setString(2, request.getParameter("taskTitle"));
            stmt.setString(3, request.getParameter("taskDate"));
            stmt.setString(4, request.getParameter("scheduledTime"));
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                response.sendRedirect("teacherDashboard?action=loadTasks&success=true");
            } else {
                handleError(response, "Failed to add task");
            }
        } catch (SQLException e) {
            handleError(response, "Error adding task: " + e.getMessage());
        }
    }

    /**
     * Updates task completion status
     */
    private void updateTask(HttpServletRequest request, HttpServletResponse response, int teacherId) 
            throws ServletException, IOException {
        try (Connection conn = getConnection()) {
            String sql = "UPDATE teacher_tasks SET is_completed = ? WHERE task_id = ? AND teacher_id = ?";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setBoolean(1, Boolean.parseBoolean(request.getParameter("completed")));
            stmt.setInt(2, Integer.parseInt(request.getParameter("taskId")));
            stmt.setInt(3, teacherId);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                response.sendRedirect("teacherDashboard?action=loadTasks&success=true");
            } else {
                handleError(response, "Failed to update task");
            }
        } catch (SQLException e) {
            handleError(response, "Error updating task: " + e.getMessage());
        }
    }

    /**
     * Sends a message to a student
     */
    private void sendMessage(HttpServletRequest request, HttpServletResponse response, int teacherId) 
            throws ServletException, IOException {
        try (Connection conn = getConnection()) {
            String sql = "INSERT INTO messages (teacher_id, student_id, message_content, date_sent, is_read) " +
                        "VALUES (?, ?, ?, NOW(), false)";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, teacherId);
            stmt.setInt(2, Integer.parseInt(request.getParameter("studentId")));
            stmt.setString(3, request.getParameter("messageContent"));
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                response.sendRedirect("teacherDashboard?action=loadMessages&success=true");
            } else {
                handleError(response, "Failed to send message");
            }
        } catch (SQLException e) {
            handleError(response, "Error sending message: " + e.getMessage());
        }
    }

    /**
     * Uploads a resource file
     */
    private void uploadResource(HttpServletRequest request, HttpServletResponse response, int teacherId) 
            throws ServletException, IOException {
        try (Connection conn = getConnection()) {
            String sql = "INSERT INTO resources (teacher_id, resource_name, resource_type, file_path, upload_date) " +
                        "VALUES (?, ?, ?, ?, NOW())";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, teacherId);
            stmt.setString(2, request.getParameter("resourceName"));
            stmt.setString(3, request.getParameter("resourceType"));
            stmt.setString(4, request.getParameter("filePath"));
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                response.sendRedirect("teacherDashboard?action=loadResources&success=true");
            } else {
                handleError(response, "Failed to upload resource");
            }
        } catch (SQLException e) {
            handleError(response, "Error uploading resource: " + e.getMessage());
        }
    }

    /**
     * Updates teacher settings
     */
    private void updateSettings(HttpServletRequest request, HttpServletResponse response, int teacherId) 
            throws ServletException, IOException {
        try (Connection conn = getConnection()) {
            String sql = "UPDATE teacher_settings SET notification_preference = ?, display_name = ?, " +
                        "theme_preference = ?, language_preference = ? WHERE teacher_id = ?";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, request.getParameter("notificationPreference"));
            stmt.setString(2, request.getParameter("displayName"));
            stmt.setString(3, request.getParameter("themePreference"));
            stmt.setString(4, request.getParameter("languagePreference"));
            stmt.setInt(5, teacherId);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                response.sendRedirect("teacherDashboard?action=loadSettings&success=true");
            } else {
                handleError(response, "Failed to update settings");
            }
        } catch (SQLException e) {
            handleError(response, "Error updating settings: " + e.getMessage());
        }
    }

    /**
     * Adds a new calendar event
     */
    private void addCalendarEvent(HttpServletRequest request, HttpServletResponse response, int teacherId) 
            throws ServletException, IOException {
        try (Connection conn = getConnection()) {
            String sql = "INSERT INTO calendar_events (teacher_id, event_title, event_date, event_time, event_type) " +
                        "VALUES (?, ?, ?, ?, ?)";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, teacherId);
            stmt.setString(2, request.getParameter("eventTitle"));
            stmt.setString(3, request.getParameter("eventDate")); 
            stmt.setString(4, request.getParameter("eventTime"));
            stmt.setString(5, request.getParameter("eventType"));
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                response.sendRedirect("teacherDashboard?action=loadCalendar&success=true");
            } else {
                handleError(response, "Failed to add calendar event");
            }
        } catch (SQLException e) {
            handleError(response, "Error adding calendar event: " + e.getMessage());
        }
    }

    /**
     * Adds a new grade
     */
    private void addGrade(HttpServletRequest request, HttpServletResponse response, int teacherId)
            throws IOException {
        String studentId = request.getParameter("studentId");
        String courseId = request.getParameter("courseId");
        String gradeValue = request.getParameter("gradeValue");

        String sql = "INSERT INTO grades (student_id, course_id, grade_value, date_recorded) VALUES (?, ?, ?, current_timestamp())";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(studentId));
            stmt.setInt(2, Integer.parseInt(courseId));
            stmt.setString(3, gradeValue);
            stmt.executeUpdate();

            // Redirect back to the grades page
            response.sendRedirect(request.getContextPath() + "/teacher/dashboard?action=loadGrades&success=true");
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/teacher/dashboard?action=loadGrades&error=true");
        }
    }

    /**
     * Edits a grade
     */
    private void editGrade(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int gradeId = Integer.parseInt(request.getParameter("id"));
        String gradeValue = request.getParameter("gradeValue");

        String sql = "UPDATE grades SET grade_value = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, gradeValue);
            stmt.setInt(2, gradeId);
            stmt.executeUpdate();

            response.sendRedirect(request.getContextPath() + "/teacher/dashboard?action=loadGrades");
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect("../error.jsp");
        }
    }

    /**
     * Deletes a grade
     */
    private void deleteGrade(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int gradeId = Integer.parseInt(request.getParameter("id"));

        String sql = "DELETE FROM grades WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, gradeId);
            stmt.executeUpdate();

            response.sendRedirect(request.getContextPath() + "/teacher/dashboard?action=loadGrades");
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect("../error.jsp");
        }
    }

    /**
     * Edits an assignment
     */
    private void editAssignment(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int assignmentId = Integer.parseInt(request.getParameter("id"));
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String dueDate = request.getParameter("dueDate");

        String sql = "UPDATE assignments SET title = ?, description = ?, due_date = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, title);
            stmt.setString(2, description);
            stmt.setString(3, dueDate);
            stmt.setInt(4, assignmentId);
            stmt.executeUpdate();

            response.sendRedirect(request.getContextPath() + "/teacher/dashboard?action=loadAssignments");
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect("../error.jsp");
        }
    }

    /**
     * Deletes an assignment
     */
    private void deleteAssignment(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int assignmentId = Integer.parseInt(request.getParameter("id"));

        String sql = "DELETE FROM assignments WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, assignmentId);
            stmt.executeUpdate();

            response.sendRedirect(request.getContextPath() + "/teacher/dashboard?action=loadAssignments");
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect("../error.jsp");
        }
    }

    /**
     * Handles errors by sending an error message to the response
     */
    private void handleError(HttpServletResponse response, String errorMessage) throws IOException {
        response.setContentType("text/plain");
        response.getWriter().write(errorMessage);
    }

    private List<Map<String, Object>> getCoursesByTeacher(Connection conn, int teacherId) throws SQLException {
        List<Map<String, Object>> courses = new ArrayList<>();
        String sql = "SELECT c.id, c.title, c.description, c.status, COUNT(ce.student_id) AS student_count " +
                     "FROM courses c " +
                     "LEFT JOIN course_enrollments ce ON c.id = ce.course_id " +
                     "WHERE c.teacher_id = ? " +
                     "GROUP BY c.id, c.title, c.description, c.status";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teacherId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> course = new HashMap<>();
                    course.put("id", rs.getInt("id"));
                    course.put("title", rs.getString("title"));
                    course.put("description", rs.getString("description"));
                    course.put("status", rs.getString("status"));
                    course.put("studentCount", rs.getInt("student_count"));
                    courses.add(course);
                }
            }
        }
        return courses;
    }

    private List<Map<String, Object>> getStudentsByTeacher(Connection conn, int teacherId) throws SQLException {
        List<Map<String, Object>> students = new ArrayList<>();
        String sql = "SELECT DISTINCT s.id, s.full_name, s.email, s.tel, s.gender, s.level " +
                     "FROM students s " +
                     "JOIN course_enrollments ce ON s.id = ce.student_id " +
                     "JOIN courses c ON ce.course_id = c.id " +
                     "WHERE c.teacher_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teacherId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> student = new HashMap<>();
                    student.put("id", rs.getInt("id"));
                    student.put("fullName", rs.getString("full_name"));
                    student.put("email", rs.getString("email"));
                    student.put("tel", rs.getString("tel"));
                    student.put("gender", rs.getString("gender"));
                    student.put("level", rs.getString("level"));
                    students.add(student);
                }
            }
        }
        return students;
    }

    private List<Map<String, Object>> getAssignmentsByTeacher(Connection conn, int teacherId) throws SQLException {
        List<Map<String, Object>> assignments = new ArrayList<>();
        String sql = "SELECT a.id, a.title, a.description, a.due_date, a.created_at, c.title AS course_title " +
                     "FROM assignments a " +
                     "JOIN courses c ON a.course_id = c.id " +
                     "WHERE c.teacher_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teacherId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> assignment = new HashMap<>();
                    assignment.put("id", rs.getInt("id"));
                    assignment.put("title", rs.getString("title"));
                    assignment.put("description", rs.getString("description"));
                    assignment.put("courseTitle", rs.getString("course_title"));
                    assignment.put("dueDate", rs.getTimestamp("due_date").toString());
                    assignment.put("createdAt", rs.getTimestamp("created_at").toString());
                    assignments.add(assignment);
                }
            }
        }
        return assignments;
    }

    private List<Map<String, Object>> getGradesByTeacher(Connection conn, int teacherId) throws SQLException {
        List<Map<String, Object>> grades = new ArrayList<>();
        String sql = "SELECT g.id, s.full_name AS student_name, c.title AS course_title, g.grade_value, g.date_recorded " +
                     "FROM grades g " +
                     "JOIN students s ON g.student_id = s.id " +
                     "JOIN courses c ON g.course_id = c.id " +
                     "WHERE c.teacher_id = ? " +
                     "ORDER BY g.date_recorded DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teacherId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> grade = new HashMap<>();
                    grade.put("id", rs.getInt("id"));
                    grade.put("studentName", rs.getString("student_name"));
                    grade.put("courseTitle", rs.getString("course_title"));
                    grade.put("gradeValue", rs.getString("grade_value"));
                    grade.put("dateRecorded", rs.getTimestamp("date_recorded").toString());
                    grades.add(grade);
                }
            }
        }
        return grades;
    }

    private int getTotalStudents(Connection conn, int teacherId) throws SQLException {
        String sql = "SELECT COUNT(DISTINCT ce.student_id) AS total_students " +
                     "FROM course_enrollments ce " +
                     "JOIN courses c ON ce.course_id = c.id " +
                     "WHERE c.teacher_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teacherId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total_students");
                }
            }
        }
        return 0;
    }

    private int getActiveCourses(Connection conn, int teacherId) throws SQLException {
        String sql = "SELECT COUNT(*) AS active_courses " +
                     "FROM courses " +
                     "WHERE teacher_id = ? AND status = 'active'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teacherId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("active_courses");
                }
            }
        }
        return 0;
    }

    private void loadCourseStudents(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int courseId = Integer.parseInt(request.getParameter("courseId"));

        String sql = "SELECT s.id, s.full_name, s.email, s.tel, s.gender, s.level " +
                     "FROM students s " +
                     "JOIN course_enrollments ce ON s.id = ce.student_id " +
                     "WHERE ce.course_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<Map<String, Object>> students = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> student = new HashMap<>();
                    student.put("id", rs.getInt("id"));
                    student.put("fullName", rs.getString("full_name"));
                    student.put("email", rs.getString("email"));
                    student.put("tel", rs.getString("tel"));
                    student.put("gender", rs.getString("gender"));
                    student.put("level", rs.getString("level"));
                    students.add(student);
                }
                response.setContentType("application/json");
                response.getWriter().write(new JSONArray(students).toString());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error loading course students");
        }
    }
}