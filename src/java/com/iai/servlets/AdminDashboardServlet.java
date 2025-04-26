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

/**
 * Main servlet to handle all admin dashboard operations
 * Routes requests based on action parameter to appropriate handlers
 */
@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {
    private static final String URL = "jdbc:mysql://localhost:3306/j2ee";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    
    // Dashboard stats cache to reduce database load
    private Map<String, Integer> dashboardStats = new HashMap<>();
    private long statsLastUpdated = 0;
    private static final long STATS_CACHE_TTL = 5 * 60 * 1000; // 5 minutes
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
            
        HttpSession session = request.getSession();
        // Check if user is logged in and is admin
        if (session.getAttribute("adminId") == null) {
            response.sendRedirect("../login.jsp");
            return;
        }

        int adminId = (int) session.getAttribute("adminId");
        String action = request.getParameter("action");

        try {
            Connection conn = getConnection();
            // Load admin profile information
            loadAdminProfile(request, adminId, conn);

            if (action == null) {
                // Load main dashboard data
                loadDashboardData(request, adminId);
                request.getRequestDispatcher("/admin/adminMainPage.jsp").forward(request, response);
            } else {
                switch (action) {
                    case "loadTeachers":
                        loadTeachers(request, response, adminId, conn);
                        break;
                    case "loadStudents":
                        loadStudents(request, response, adminId, conn);
                        break;
                    case "loadCourses":
                        loadCourses(request, response, adminId, conn);
                        break;
                    case "loadDepartments":
                        loadDepartments(request, response, adminId, conn);
                        break;
                    case "loadReports":
                        loadReports(request, response, adminId, conn);
                        break;
                    case "loadSettings":
                        loadSettings(request, response, adminId, conn);
                        break;
                    default:
                        loadDashboardData(request, adminId);
                        request.getRequestDispatcher("adminMainPage.jsp").forward(request, response);
                }
            }
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect("../error.jsp");
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        switch (pathInfo) {
            case "/addTeacher":
                addTeacher(request, response);
                break;
            case "/updateTeacher":
                updateTeacher(request, response);
                break;
            case "/deleteTeacher":
                deleteTeacher(request, response);
                break;
            case "/addStudent":
                addStudent(request, response);
                break;
            case "/updateStudent":
                updateStudent(request, response);
                break;
            case "/deleteStudent":
                deleteStudent(request, response);
                break;
            case "/addCourse":
                addCourse(request, response);
                break;
            case "/updateCourse":
                updateCourse(request, response);
                break;
            case "/deleteCourse":
                deleteCourse(request, response);
                break;
            case "/addDepartment":
                addDepartment(request, response);
                break;
            case "/updateDepartment":
                updateDepartment(request, response);
                break;
            case "/deleteDepartment":
                deleteDepartment(request, response);
                break;
            case "/saveSettings":
                saveSettings(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                break;
        }
    }   
    
    /**
     * Show the main dashboard with statistics
     */
    private void showDashboard(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Check if stats cache is valid or needs refresh
        long currentTime = System.currentTimeMillis();
        if (dashboardStats.isEmpty() || (currentTime - statsLastUpdated) > STATS_CACHE_TTL) {
            updateDashboardStats();
            statsLastUpdated = currentTime;
        }
        
        // Set attributes for dashboard
        request.setAttribute("teacherCount", dashboardStats.get("teachers"));
        request.setAttribute("studentCount", dashboardStats.get("students"));
        request.setAttribute("courseCount", dashboardStats.get("courses"));
        request.setAttribute("departmentCount", dashboardStats.get("departments"));
        
        // Forward to main admin page instead of dashboard.jsp
        request.getRequestDispatcher("/admin/adminMainPage.jsp").forward(request, response);
    }
    
    /**
     * Fetch and update dashboard statistics
     */
    private void updateDashboardStats() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            
            // Count teachers
            stmt = conn.prepareStatement("SELECT COUNT(*) FROM teachers");
            rs = stmt.executeQuery();
            if (rs.next()) {
                dashboardStats.put("teachers", rs.getInt(1));
            }
            rs.close();
            stmt.close();
            
            // Count students
            stmt = conn.prepareStatement("SELECT COUNT(*) FROM students");
            rs = stmt.executeQuery();
            if (rs.next()) {
                dashboardStats.put("students", rs.getInt(1));
            }
            rs.close();
            stmt.close();
            
            // Count courses
            stmt = conn.prepareStatement("SELECT COUNT(*) FROM courses WHERE status = 'Active'");
            rs = stmt.executeQuery();
            if (rs.next()) {
                dashboardStats.put("courses", rs.getInt(1));
            }
            rs.close();
            stmt.close();
            
            // Count departments
            stmt = conn.prepareStatement("SELECT COUNT(*) FROM departments");
            rs = stmt.executeQuery();
            if (rs.next()) {
                dashboardStats.put("departments", rs.getInt(1));
            }
            
        } catch (SQLException e) {
            // Log error
            e.printStackTrace();
        } finally {
            closeResources(conn, stmt, rs);
        }
    }
    
    /**
     * List all teachers
     */
    private void listTeachers(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Map<String, String>> teachers = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(
                "SELECT t.id, t.name, d.name as department, t.email, t.contact, t.status " +
                "FROM teachers t JOIN departments d ON t.department_id = d.id"
            );
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, String> teacher = new HashMap<>();
                teacher.put("id", rs.getString("id"));
                teacher.put("name", rs.getString("name"));
                teacher.put("department", rs.getString("department"));
                teacher.put("email", rs.getString("email"));
                teacher.put("contact", rs.getString("contact"));
                teacher.put("status", rs.getString("status"));
                teachers.add(teacher);
            }
            
            request.setAttribute("teachers", teachers);
            request.getRequestDispatcher("/admin/teachers.jsp").forward(request, response);
            
        } catch (SQLException e) {
            handleSQLException(response, e);
        } finally {
            closeResources(conn, stmt, rs);
        }
    }
    
    /**
     * List all students
     */
    private void listStudents(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Map<String, String>> students = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(
                "SELECT id, name, level, email, contact, gender, status FROM students"
            );
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, String> student = new HashMap<>();
                student.put("id", rs.getString("id"));
                student.put("name", rs.getString("name"));
                student.put("level", rs.getString("level"));
                student.put("email", rs.getString("email"));
                student.put("contact", rs.getString("contact"));
                student.put("gender", rs.getString("gender"));
                student.put("status", rs.getString("status"));
                students.add(student);
            }
            
            request.setAttribute("students", students);
            request.getRequestDispatcher("/admin/students.jsp").forward(request, response);
            
        } catch (SQLException e) {
            handleSQLException(response, e);
        } finally {
            closeResources(conn, stmt, rs);
        }
    }
    
    /**
     * List all courses
     */
    private void listCourses(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Map<String, String>> courses = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(
                "SELECT c.id, c.title, d.name as department, " +
                "(SELECT COUNT(*) FROM course_enrollments WHERE course_id = c.id) as student_count, " +
                "t.name as teacher_name " +
                "FROM courses c " +
                "JOIN departments d ON c.department_id = d.id " +
                "JOIN teachers t ON c.teacher_id = t.id " +
                "WHERE c.status = 'Active'"
            );
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, String> course = new HashMap<>();
                course.put("id", rs.getString("id"));
                course.put("title", rs.getString("title"));
                course.put("department", rs.getString("department"));
                course.put("studentCount", rs.getString("student_count"));
                course.put("teacher", rs.getString("teacher_name"));
                courses.add(course);
            }
            
            request.setAttribute("courses", courses);
            request.getRequestDispatcher("/admin/courses.jsp").forward(request, response);
            
        } catch (SQLException e) {
            handleSQLException(response, e);
        } finally {
            closeResources(conn, stmt, rs);
        }
    }
    
    /**
     * List all departments
     */
    private void listDepartments(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Map<String, String>> departments = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(
                "SELECT d.id, d.name, t.name as head, " +
                "(SELECT COUNT(*) FROM teachers WHERE department_id = d.id) as staff_count, " +
                "(SELECT COUNT(*) FROM students s JOIN student_departments sd ON s.id = sd.student_id " +
                "WHERE sd.department_id = d.id) as student_count " +
                "FROM departments d " +
                "LEFT JOIN teachers t ON d.head_id = t.id"
            );
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, String> department = new HashMap<>();
                department.put("id", rs.getString("id"));
                department.put("name", rs.getString("name"));
                department.put("head", rs.getString("head"));
                department.put("staffCount", rs.getString("staff_count"));
                department.put("studentCount", rs.getString("student_count"));
                departments.add(department);
            }
            
            request.setAttribute("departments", departments);
            request.getRequestDispatcher("/admin/departments.jsp").forward(request, response);
            
        } catch (SQLException e) {
            handleSQLException(response, e);
        } finally {
            closeResources(conn, stmt, rs);
        }
    }
    
    /**
     * Generate reports and analytics
     */
    private void generateReports(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Map<String, Object> reportData = new HashMap<>();
        
        try {
            conn = getConnection();
            
            // Get attendance stats
            stmt = conn.prepareStatement(
                "SELECT AVG(attendance_percentage) as avg_attendance FROM attendance_records"
            );
            rs = stmt.executeQuery();
            if (rs.next()) {
                reportData.put("averageAttendance", rs.getDouble("avg_attendance"));
            }
            
            // Add more report metrics here
            
            request.setAttribute("reportData", reportData);
            request.getRequestDispatcher("/admin/reports.jsp").forward(request, response);
            
        } catch (SQLException e) {
            handleSQLException(response, e);
        } finally {
            closeResources(conn, stmt, rs);
        }
    }
    
    /**
     * Show system settings
     */
    private void showSettings(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Map<String, String> settings = new HashMap<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement("SELECT setting_key, setting_value FROM system_settings");
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                settings.put(rs.getString("setting_key"), rs.getString("setting_value"));
            }
            
            request.setAttribute("settings", settings);
            request.getRequestDispatcher("/admin/settings.jsp").forward(request, response);
            
        } catch (SQLException e) {
            handleSQLException(response, e);
        } finally {
            closeResources(conn, stmt, rs);
        }
    }
    
    /**
     * Show form to add a new teacher
     */
    private void showAddTeacherForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Get departments for dropdown
        List<Map<String, String>> departments = getDepartments();
        request.setAttribute("departments", departments);
        request.getRequestDispatcher("/admin/addTeacherForm.jsp").forward(request, response);
    }
    
    /**
     * Add a new teacher to database
     */
    private void addTeacher(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String name = request.getParameter("name");
        String departmentId = request.getParameter("department");
        String email = request.getParameter("email");
        String contact = request.getParameter("contact");
        String status = request.getParameter("status");
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            String sql = "INSERT INTO teachers (name, department_id, email, contact, status) VALUES (?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, departmentId);
            stmt.setString(3, email);
            stmt.setString(4, contact);
            stmt.setString(5, status);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                // Invalidate stats cache
                statsLastUpdated = 0;
                response.sendRedirect(request.getContextPath() + "/admin/teachers");
            } else {
                request.setAttribute("error", "Failed to add teacher");
                showAddTeacherForm(request, response);
            }
        } catch (SQLException e) {
            handleSQLException(response, e);
        } finally {
            closeResources(conn, stmt, null);
        }
    }
    
    /**
     * Show form to edit an existing teacher
     */
    private void showEditTeacherForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String teacherId = request.getParameter("id");
        if (teacherId == null || teacherId.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Teacher ID is required");
            return;
        }
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(
                "SELECT id, name, department_id, email, contact, status FROM teachers WHERE id = ?"
            );
            stmt.setString(1, teacherId);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                Map<String, String> teacher = new HashMap<>();
                teacher.put("id", rs.getString("id"));
                teacher.put("name", rs.getString("name"));
                teacher.put("departmentId", rs.getString("department_id"));
                teacher.put("email", rs.getString("email"));
                teacher.put("contact", rs.getString("contact"));
                teacher.put("status", rs.getString("status"));
                
                request.setAttribute("teacher", teacher);
                
                // Get departments for dropdown
                List<Map<String, String>> departments = getDepartments();
                request.setAttribute("departments", departments);
                
                request.getRequestDispatcher("/admin/editTeacherForm.jsp").forward(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Teacher not found");
            }
        } catch (SQLException e) {
            handleSQLException(response, e);
        } finally {
            closeResources(conn, stmt, rs);
        }
    }
    
    /**
     * Update an existing teacher
     */
    private void updateTeacher(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String teacherId = request.getParameter("id");
        String name = request.getParameter("name");
        String departmentId = request.getParameter("department");
        String email = request.getParameter("email");
        String contact = request.getParameter("contact");
        String status = request.getParameter("status");
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            String sql = "UPDATE teachers SET name = ?, department_id = ?, email = ?, contact = ?, status = ? WHERE id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, departmentId);
            stmt.setString(3, email);
            stmt.setString(4, contact);
            stmt.setString(5, status);
            stmt.setString(6, teacherId);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                response.sendRedirect(request.getContextPath() + "/admin/teachers");
            } else {
                request.setAttribute("error", "Failed to update teacher");
                showEditTeacherForm(request, response);
            }
        } catch (SQLException e) {
            handleSQLException(response, e);
        } finally {
            closeResources(conn, stmt, null);
        }
    }
    
    /**
     * Delete a teacher
     */
    private void deleteTeacher(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String teacherId = request.getParameter("id");
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            String sql = "DELETE FROM teachers WHERE id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, teacherId);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                // Invalidate stats cache
                statsLastUpdated = 0;
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": true}");
            } else {
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": false, \"message\": \"Teacher not found\"}");
            }
        } catch (SQLException e) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"" + e.getMessage() + "\"}");
        } finally {
            closeResources(conn, stmt, null);
        }
    }
    
    // Similar methods for Students
    private void showAddStudentForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/admin/addStudentForm.jsp").forward(request, response);
    }
    
    private void addStudent(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String name = request.getParameter("name");
        String level = request.getParameter("level");
        String email = request.getParameter("email");
        String contact = request.getParameter("contact");
        String gender = request.getParameter("gender");
        String status = request.getParameter("status");
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            String sql = "INSERT INTO students (name, level, email, contact, gender, status) VALUES (?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, level);
            stmt.setString(3, email);
            stmt.setString(4, contact);
            stmt.setString(5, gender);
            stmt.setString(6, status);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                // Invalidate stats cache
                statsLastUpdated = 0;
                response.sendRedirect(request.getContextPath() + "/admin/students");
            } else {
                request.setAttribute("error", "Failed to add student");
                showAddStudentForm(request, response);
            }
        } catch (SQLException e) {
            handleSQLException(response, e);
        } finally {
            closeResources(conn, stmt, null);
        }
    }
    
    private void showEditStudentForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String studentId = request.getParameter("id");
        if (studentId == null || studentId.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Student ID is required");
            return;
        }
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(
                "SELECT id, name, level, email, contact, gender, status FROM students WHERE id = ?"
            );
            stmt.setString(1, studentId);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                Map<String, String> student = new HashMap<>();
                student.put("id", rs.getString("id"));
                student.put("name", rs.getString("name"));
                student.put("level", rs.getString("level"));
                student.put("email", rs.getString("email"));
                student.put("contact", rs.getString("contact"));
                student.put("gender", rs.getString("gender"));
                student.put("status", rs.getString("status"));
                
                request.setAttribute("student", student);
                request.getRequestDispatcher("/admin/editStudentForm.jsp").forward(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Student not found");
            }
        } catch (SQLException e) {
            handleSQLException(response, e);
        } finally {
            closeResources(conn, stmt, rs);
        }
    }
    
    private void updateStudent(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String studentId = request.getParameter("id");
        String name = request.getParameter("name");
        String level = request.getParameter("level");
        String email = request.getParameter("email");
        String contact = request.getParameter("contact");
        String gender = request.getParameter("gender");
        String status = request.getParameter("status");
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            String sql = "UPDATE students SET name = ?, level = ?, email = ?, contact = ?, gender = ?, status = ? WHERE id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, level);
            stmt.setString(3, email);
            stmt.setString(4, contact);
            stmt.setString(5, gender);
            stmt.setString(6, status);
            stmt.setString(7, studentId);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                response.sendRedirect(request.getContextPath() + "/admin/students");
            } else {
                request.setAttribute("error", "Failed to update student");
                showEditStudentForm(request, response);
            }
        } catch (SQLException e) {
            handleSQLException(response, e);
        } finally {
            closeResources(conn, stmt, null);
        }
    }
    
    private void deleteStudent(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String studentId = request.getParameter("id");
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            String sql = "DELETE FROM students WHERE id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, studentId);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                // Invalidate stats cache
                statsLastUpdated = 0;
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": true}");
            } else {
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": false, \"message\": \"Student not found\"}");
            }
        } catch (SQLException e) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"" + e.getMessage() + "\"}");
        } finally {
            closeResources(conn, stmt, null);
        }
    }
    
    // Similar methods for Courses
    private void showAddCourseForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Get departments and teachers for dropdowns
        List<Map<String, String>> departments = getDepartments();
        List<Map<String, String>> teachers = getTeachers();
        
        request.setAttribute("departments", departments);
        request.setAttribute("teachers", teachers);
        request.getRequestDispatcher("/admin/addCourseForm.jsp").forward(request, response);
    }
    
    private void addCourse(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String title = request.getParameter("title");
        String departmentId = request.getParameter("department");
        String teacherId = request.getParameter("teacher");
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            String sql = "INSERT INTO courses (title, department_id, teacher_id, status) VALUES (?, ?, ?, 'Active')";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, title);
            stmt.setString(2, departmentId);
            stmt.setString(3, teacherId);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                // Invalidate stats cache
                statsLastUpdated = 0;
                response.sendRedirect(request.getContextPath() + "/admin/courses");
            } else {
                request.setAttribute("error", "Failed to add course");
                showAddCourseForm(request, response);
            }
        } catch (SQLException e) {
            handleSQLException(response, e);
        } finally {
            closeResources(conn, stmt, null);
        }
    }
    
    private void showEditCourseForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String courseId = request.getParameter("id");
        if (courseId == null || courseId.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Course ID is required");
            return;
        }
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(
                "SELECT id, title, department_id, teacher_id, status FROM courses WHERE id = ?"
            );
            stmt.setString(1, courseId);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                Map<String, String> course = new HashMap<>();
                course.put("id", rs.getString("id"));
                course.put("title", rs.getString("title"));
                course.put("departmentId", rs.getString("department_id"));
                course.put("teacherId", rs.getString("teacher_id"));
                course.put("status", rs.getString("status"));
                
                request.setAttribute("course", course);
                
                // Get departments and teachers for dropdowns
                List<Map<String, String>> departments = getDepartments();
                List<Map<String, String>> teachers = getTeachers();
                
                request.setAttribute("departments", departments);
                request.setAttribute("teachers", teachers);
                
                request.getRequestDispatcher("/admin/editCourseForm.jsp").forward(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Course not found");
            }
        } catch (SQLException e) {
            handleSQLException(response, e);
        } finally {
            closeResources(conn, stmt, rs);
        }
    }
    
    private void updateCourse(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String courseId = request.getParameter("id");
        String title = request.getParameter("title");
        String departmentId = request.getParameter("department");
        String teacherId = request.getParameter("teacher");
        String status = request.getParameter("status");
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            String sql = "UPDATE courses SET title = ?, department_id = ?, teacher_id = ?, status = ? WHERE id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, title);
            stmt.setString(2, departmentId);
            stmt.setString(3, teacherId);
            stmt.setString(4, status);
            stmt.setString(5, courseId);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                response.sendRedirect(request.getContextPath() + "/admin/courses");
            } else {
                request.setAttribute("error", "Failed to update course");
                showEditCourseForm(request, response);
            }
        } catch (SQLException e) {
            handleSQLException(response, e);
        } finally {
            closeResources(conn, stmt, null);
        }
    }
    
    private void deleteCourse(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String courseId = request.getParameter("id");
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            String sql = "DELETE FROM courses WHERE id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, courseId);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                // Invalidate stats cache
                statsLastUpdated = 0;
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": true}");
            } else {
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": false, \"message\": \"Course not found\"}");
            }
        } catch (SQLException e) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"" + e.getMessage() + "\"}");
        } finally {
            closeResources(conn, stmt, null);
        }
    }
    
    // Similar methods for Departments
    private void showAddDepartmentForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Get teachers for dropdown
        List<Map<String, String>> teachers = getTeachers();
        request.setAttribute("teachers", teachers);
        request.getRequestDispatcher("/admin/addDepartmentForm.jsp").forward(request, response);
    }
    
    private void addDepartment(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handleDepartmentOperations(request, response, "add");
    }
    
    private void showEditDepartmentForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String departmentId = request.getParameter("id");
        if (departmentId == null || departmentId.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Department ID is required");
            return;
        }
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(
                "SELECT id, name, head_id FROM departments WHERE id = ?"
            );
            stmt.setString(1, departmentId);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                Map<String, String> department = new HashMap<>();
                department.put("id", rs.getString("id"));
                department.put("name", rs.getString("name"));
                department.put("headId", rs.getString("head_id"));
                
                request.setAttribute("department", department);
                
                // Get teachers for dropdown
                List<Map<String, String>> teachers = getTeachers();
                request.setAttribute("teachers", teachers);
                
                request.getRequestDispatcher("/admin/editDepartmentForm.jsp").forward(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Department not found");
            }
        } catch (SQLException e) {
            handleSQLException(response, e);
        } finally {
            closeResources(conn, stmt, rs);
        }
    }
    
    private void updateDepartment(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handleDepartmentOperations(request, response, "update");
    }
    
    private void deleteDepartment(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handleDepartmentOperations(request, response, "delete");
    }
    
    private void saveSettings(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            String sql = "UPDATE system_settings SET setting_value = ? WHERE setting_key = ?";
            
            // Update institution name
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, request.getParameter("institutionName"));
            stmt.setString(2, "institution_name");
            stmt.executeUpdate();
            
            // Update admin email
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, request.getParameter("adminEmail"));
            stmt.setString(2, "admin_email");
            stmt.executeUpdate();
            
            response.sendRedirect(request.getContextPath() + "/admin/settings?success=true");
        } catch (SQLException e) {
            handleSQLException(response, e);
        } finally {
            closeResources(conn, stmt, null);
        }
    }

    private void handleDepartmentOperations(HttpServletRequest request, HttpServletResponse response, String operation) 
            throws ServletException, IOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            String sql = "";
            
            switch (operation) {
                case "add":
                    sql = "INSERT INTO departments (name, head_id) VALUES (?, ?)";
                    stmt = conn.prepareStatement(sql);
                    stmt.setString(1, request.getParameter("departmentName"));
                    stmt.setString(2, request.getParameter("headId"));
                    break;
                    
                case "update":
                    sql = "UPDATE departments SET name = ?, head_id = ? WHERE id = ?";
                    stmt = conn.prepareStatement(sql);
                    stmt.setString(1, request.getParameter("departmentName"));
                    stmt.setString(2, request.getParameter("headId"));
                    stmt.setString(3, request.getParameter("departmentId"));
                    break;
                    
                case "delete":
                    sql = "DELETE FROM departments WHERE id = ?";
                    stmt = conn.prepareStatement(sql);
                    stmt.setString(1, request.getParameter("departmentId"));
                    break;
            }
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                statsLastUpdated = 0; // Invalidate cache
                response.sendRedirect(request.getContextPath() + "/admin/departments?success=true");
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/departments?error=true");
            }
        } catch (SQLException e) {
            handleSQLException(response, e);
        } finally {
            closeResources(conn, stmt, null);
        }
    }

    private void generateAnalytics(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        Map<String, Object> analytics = new HashMap<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            
            // Get department performance
            String sql = "SELECT d.name, COUNT(s.id) as student_count, " +
                        "AVG(g.grade_value) as avg_grade " +
                        "FROM departments d " +
                        "LEFT JOIN students s ON d.id = s.department_id " +
                        "LEFT JOIN grades g ON s.id = g.student_id " +
                        "GROUP BY d.id";
            
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            
            List<Map<String, Object>> departmentStats = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> stat = new HashMap<>();
                stat.put("name", rs.getString("name"));
                stat.put("studentCount", rs.getInt("student_count"));
                stat.put("avgGrade", rs.getDouble("avg_grade"));
                departmentStats.add(stat);
            }
            
            analytics.put("departmentStats", departmentStats);
            request.setAttribute("analytics", analytics);
            request.getRequestDispatcher("/admin/analytics.jsp").forward(request, response);
            
        } catch (SQLException e) {
            handleSQLException(response, e);
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    private void handleError(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json");
        response.getWriter().write("{\"success\": false, \"message\": \"" + message + "\"}");
    }

    /**
     * Get a list of all departments
     */
    private List<Map<String, String>> getDepartments() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Map<String, String>> departments = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement("SELECT id, name FROM departments");
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, String> department = new HashMap<>();
                department.put("id", rs.getString("id"));
                department.put("name", rs.getString("name"));
                departments.add(department);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return departments;
    }
    
    /**
     * Get a list of all teachers
     */
    private List<Map<String, String>> getTeachers() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Map<String, String>> teachers = new ArrayList<>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement("SELECT id, name FROM teachers");
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, String> teacher = new HashMap<>();
                teacher.put("id", rs.getString("id"));
                teacher.put("name", rs.getString("name"));
                teachers.add(teacher);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, stmt, rs);
        }
        
        return teachers;
    }
    
    /**
     * Handle SQL exceptions
     */
    private void handleSQLException(HttpServletResponse response, SQLException e) throws IOException {
        e.printStackTrace();
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
    }
    
    /**
     * Get a database connection
     */
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
    
    /**
     * Close database resources
     */
    private void closeResources(Connection conn, PreparedStatement stmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadAdminProfile(HttpServletRequest request, int adminId, Connection conn) {
        // Stub implementation for loading admin profile
        request.setAttribute("adminName", "Admin Name");
        request.setAttribute("adminEmail", "admin@eduteach.com");
    }

    private void loadDashboardData(HttpServletRequest request, int adminId) {
        // Stub implementation for loading dashboard data
        request.setAttribute("teacherCount", 10);
        request.setAttribute("studentCount", 100);
        request.setAttribute("courseCount", 20);
        request.setAttribute("departmentCount", 5);
    }

    private void loadTeachers(HttpServletRequest request, HttpServletResponse response, int adminId, Connection conn) {
        // Stub implementation for loading teachers
        request.setAttribute("teachers", new ArrayList<>());
    }

    private void loadStudents(HttpServletRequest request, HttpServletResponse response, int adminId, Connection conn) {
        // Stub implementation for loading students
        request.setAttribute("students", new ArrayList<>());
    }

    private void loadCourses(HttpServletRequest request, HttpServletResponse response, int adminId, Connection conn) {
        // Stub implementation for loading courses
        request.setAttribute("courses", new ArrayList<>());
    }

    private void loadDepartments(HttpServletRequest request, HttpServletResponse response, int adminId, Connection conn) {
        // Stub implementation for loading departments
        request.setAttribute("departments", new ArrayList<>());
    }

    private void loadReports(HttpServletRequest request, HttpServletResponse response, int adminId, Connection conn) {
        // Stub implementation for loading reports
        request.setAttribute("reports", new ArrayList<>());
    }

    private void loadSettings(HttpServletRequest request, HttpServletResponse response, int adminId, Connection conn) {
        // Stub implementation for loading settings
        request.setAttribute("settings", new HashMap<>());
    }
}