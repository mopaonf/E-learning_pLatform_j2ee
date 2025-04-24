/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.iai.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 *
 * @author AE DERICK
 */
public class AddStudent extends HttpServlet {
   
    private static final String URL = "jdbc:mysql://localhost:3306/j2ee";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/addStudentForm.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String surname = request.getParameter("surname");
        String level = request.getParameter("level");
        String tel = request.getParameter("tel");
        String email = request.getParameter("email");
        String gender = request.getParameter("gender");

        PrintWriter out = response.getWriter();
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            // Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish Connection
            conn = DriverManager.getConnection(URL, USER, PASSWORD);

            // SQL query
        String sql = "INSERT INTO students (surname, level, tel, email, gender) VALUES (?, ?, ?, ?, ?)";
        
        stmt = conn.prepareStatement(sql);
        stmt.setString(1, surname);
        stmt.setString(2, level);
        stmt.setString(3, tel);
        stmt.setString(4, email);
        stmt.setString(5, gender);

        int rowsAffected = stmt.executeUpdate();
        
        if(rowsAffected > 0) {
            
            request.getRequestDispatcher("/displayStudent.jsp").forward(request, response);
        } else {
            
            request.getRequestDispatcher("/displayStudent.jsp").forward(request, response);
        }
        } catch (ClassNotFoundException | SQLException e) {
            out.println("Database error: " + e.getMessage());
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
            }
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
