<%@page contentType="text/html" pageEncoding="UTF-8"%> <%@ taglib prefix="c"
uri="http://java.sun.com/jsp/jstl/core" %> <% if
(request.getAttribute("teachers") == null) {
response.sendRedirect(request.getContextPath() + "/admin/dashboard"); return; }
if (request.getAttribute("students") == null) {
response.sendRedirect(request.getContextPath() + "/admin/dashboard"); return; }
if (request.getAttribute("courses") == null) {
response.sendRedirect(request.getContextPath() + "/admin/dashboard"); return; }
%>
<!DOCTYPE html>
<html lang="en">
   <head>
      <meta charset="UTF-8" />
      <meta name="viewport" content="width=device-width, initial-scale=1.0" />
      <title>EduTeach - Admin Dashboard</title>
      <link
         rel="stylesheet"
         href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css"
      />
      <style>
         * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
         }

         :root {
            --primary-color: #4361ee;
            --secondary-color: #3f37c9;
            --accent-color: #4895ef;
            --light-color: #f8f9fa;
            --dark-color: #212529;
            --success-color: #4cc9f0;
            --danger-color: #f72585;
            --warning-color: #f8961e;
            --sidebar-width: 280px;
            --header-height: 70px;
            --shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
            --transition: all 0.3s ease;
         }

         body {
            background-color: #f5f7fa;
            color: var(--dark-color);
            min-height: 100vh;
            overflow-x: hidden;
         }

         /* Sidebar Styles */
         .sidebar {
            position: fixed;
            left: 0;
            top: 0;
            width: var(--sidebar-width);
            height: 100vh;
            background-color: white;
            box-shadow: var(--shadow);
            transition: var(--transition);
            z-index: 1000;
            overflow-y: auto;
         }

         .sidebar-collapsed {
            left: calc(-1 * var(--sidebar-width) + 60px);
         }

         .sidebar-header {
            padding: 20px;
            text-align: center;
            border-bottom: 1px solid rgba(0, 0, 0, 0.1);
         }

         .user-profile {
            position: relative;
            margin-bottom: 10px;
         }

         .profile-pic {
            width: 80px;
            height: 80px;
            border-radius: 50%;
            object-fit: cover;
            border: 3px solid var(--primary-color);
            margin: 0 auto 10px;
            display: block;
         }

         .profile-info h4 {
            color: var(--dark-color);
            margin-bottom: 5px;
         }

         .profile-info p {
            color: #6c757d;
            font-size: 14px;
         }

         .online-status {
            position: absolute;
            width: 12px;
            height: 12px;
            background-color: #10b981;
            border-radius: 50%;
            bottom: 35px;
            right: 95px;
            border: 2px solid white;
         }

         .nav-menu {
            padding: 20px 0;
         }

         .nav-item {
            padding: 12px 25px;
            display: flex;
            align-items: center;
            color: #6c757d;
            transition: var(--transition);
            cursor: pointer;
         }

         .nav-item:hover {
            background-color: rgba(67, 97, 238, 0.1);
            color: var(--primary-color);
         }

         .nav-item.active {
            background-color: rgba(67, 97, 238, 0.1);
            border-left: 4px solid var(--primary-color);
            color: var(--primary-color);
            font-weight: 600;
         }

         .nav-item i {
            margin-right: 10px;
            font-size: 18px;
         }

         .toggle-sidebar {
            position: absolute;
            top: 20px;
            right: 7px;
            width: 30px;
            height: 30px;
            background-color: var(--primary-color);
            color: white;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            box-shadow: var(--shadow);
            z-index: 10;
         }

         /* Header Styles */
         .header {
            position: fixed;
            top: 0;
            left: var(--sidebar-width);
            right: 0;
            height: var(--header-height);
            background-color: white;
            box-shadow: var(--shadow);
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 0 25px;
            transition: var(--transition);
            z-index: 900;
         }

         .header.expanded {
            left: 60px;
            width: calc(100% - 60px);
         }

         .search-container {
            display: flex;
            align-items: center;
            background-color: #f5f7fa;
            border-radius: 50px;
            padding: 0 15px;
            width: 300px;
         }

         .search-container input {
            background: transparent;
            border: none;
            outline: none;
            padding: 10px;
            width: 100%;
            font-size: 14px;
         }

         .search-container i {
            color: #6c757d;
         }

         .header-actions {
            display: flex;
            align-items: center;
            gap: 20px;
         }

         .action-icon {
            position: relative;
            cursor: pointer;
            color: #6c757d;
            transition: var(--transition);
         }

         .action-icon:hover {
            color: var(--primary-color);
         }

         .badge {
            position: absolute;
            top: -5px;
            right: -5px;
            background-color: var(--danger-color);
            color: white;
            border-radius: 50%;
            width: 18px;
            height: 18px;
            font-size: 10px;
            display: flex;
            align-items: center;
            justify-content: center;
         }

         /* Main Content Styles */
         .main-content {
            margin-left: var(--sidebar-width);
            padding-top: var(--header-height);
            min-height: 100vh;
            transition: var(--transition);
            padding: calc(var(--header-height) + 20px) 25px 25px;
         }

         .main-content.expanded {
            margin-left: 60px;
         }

         .page-title {
            margin-bottom: 20px;
            font-weight: 600;
            color: var(--dark-color);
         }

         .stats-overview {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
         }

         .stat-box {
            background: white;
            padding: 20px;
            border-radius: 10px;
            box-shadow: var(--shadow);
            text-align: center;
         }

         .stat-number {
            font-size: 28px;
            font-weight: bold;
            color: var(--primary-color);
            margin: 10px 0;
         }

         .stat-label {
            color: #6c757d;
            font-size: 14px;
         }

         .user-management {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 20px;
         }

         .action-card {
            background: white;
            padding: 20px;
            border-radius: 10px;
            box-shadow: var(--shadow);
            margin-bottom: 20px;
         }

         .action-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 15px;
         }

         .action-title {
            font-size: 18px;
            font-weight: 600;
         }

         .user-list {
            list-style: none;
            padding: 0;
         }

         .user-item {
            display: flex;
            align-items: center;
            padding: 10px 0;
            border-bottom: 1px solid #eee;
         }

         .user-avatar {
            width: 40px;
            height: 40px;
            border-radius: 50%;
            margin-right: 10px;
         }

         .user-info {
            flex: 1;
         }

         .user-name {
            font-weight: 500;
            margin-bottom: 2px;
         }

         .user-role {
            font-size: 12px;
            color: #6c757d;
         }

         .action-buttons {
            display: flex;
            gap: 10px;
         }

         .btn-edit,
         .btn-delete {
            padding: 5px 10px;
            border-radius: 4px;
            font-size: 12px;
            margin: 0 5px;
            padding: 5px 10px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
         }

         .btn-edit {
            background: var(--primary-color);
            color: white;
         }

         .btn-delete {
            background: var(--danger-color);
            color: white;
         }

         /* Responsive Design */
         @media (max-width: 1000px) {
            .sidebar {
               left: calc(-1 * var(--sidebar-width));
            }

            .sidebar.active {
               left: 0;
            }

            .header,
            .main-content {
               left: 0;
               width: 100%;
            }

            .header {
               left: 0;
            }

            .main-content {
               margin-left: 0;
            }

            .data-grid {
               grid-template-columns: 1fr;
            }
         }

         /* Pages Container */
         .page-container {
            display: none;
            margin-top: 20px;
         }

         .page-container.active {
            display: block;
         }

         /* Loading Animation */
         .loader {
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100px;
         }

         .spinner {
            width: 40px;
            height: 40px;
            border: 4px solid rgba(67, 97, 238, 0.3);
            border-radius: 50%;
            border-top-color: var(--primary-color);
            animation: spin 1s ease-in-out infinite;
         }

         @keyframes spin {
            to {
               transform: rotate(360deg);
            }
         }

         /* Calendar Styles */
         .calendar {
            width: 100%;
         }

         .calendar-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 15px;
         }

         .calendar-grid {
            display: grid;
            grid-template-columns: repeat(7, 1fr);
            gap: 5px;
         }

         .calendar-day {
            text-align: center;
            padding: 10px;
            font-weight: 600;
            color: #6c757d;
         }

         .calendar-date {
            text-align: center;
            padding: 10px;
            border-radius: 5px;
            cursor: pointer;
            transition: var(--transition);
         }

         .calendar-date:hover {
            background-color: rgba(67, 97, 238, 0.1);
         }

         .calendar-date.today {
            background-color: var(--primary-color);
            color: white;
         }

         .calendar-date.has-event::after {
            content: '';
            display: block;
            width: 6px;
            height: 6px;
            background-color: var(--danger-color);
            border-radius: 50%;
            margin: 2px auto 0;
         }

         /* Course List Styles */
         .course-item {
            display: flex;
            align-items: center;
            gap: 15px;
            padding: 15px 0;
            border-bottom: 1px solid rgba(0, 0, 0, 0.05);
         }

         .course-item:last-child {
            border-bottom: none;
         }

         .course-thumbnail {
            width: 60px;
            height: 60px;
            border-radius: 8px;
            background-color: #f5f7fa;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 24px;
            color: var(--primary-color);
         }

         .course-info {
            flex: 1;
         }

         .course-title {
            font-weight: 600;
            margin-bottom: 5px;
         }

         .course-meta {
            display: flex;
            gap: 15px;
            font-size: 13px;
            color: #6c757d;
         }

         .course-meta span {
            display: flex;
            align-items: center;
            gap: 5px;
         }

         /* Custom Scrollbar */
         ::-webkit-scrollbar {
            width: 5px;
         }

         ::-webkit-scrollbar-track {
            background: #f1f1f1;
         }

         ::-webkit-scrollbar-thumb {
            background: #c1c1c1;
            border-radius: 5px;
         }

         ::-webkit-scrollbar-thumb:hover {
            background: #a8a8a8;
         }

         /* Additional Styles for New Sections */
         .settings-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 20px;
         }

         .form-group {
            margin-bottom: 20px;
         }

         .form-group label {
            display: block;
            margin-bottom: 5px;
            font-weight: 500;
         }

         .form-control {
            width: 100%;
            padding: 8px;
            border: 1px solid #ddd;
            border-radius: 4px;
         }

         .reports-grid {
            display: grid;
            gap: 20px;
         }

         .performance-stats {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 15px;
            padding: 15px;
         }

         .stat-item {
            background: #f8f9fa;
            padding: 15px;
            border-radius: 8px;
         }

         .stat-value {
            font-size: 24px;
            font-weight: bold;
            color: var(--primary-color);
            margin: 10px 0;
         }

         .progress-bar {
            height: 6px;
            background: var(--primary-color);
            border-radius: 3px;
         }

         /* Fix for overlapping content */
         .page-container {
            margin-top: 20px;
         }

         /* Fix table styles */
         .action-card table {
            width: 100%;
            margin-top: 15px;
         }

         .action-card th,
         .action-card td {
            padding: 12px;
            text-align: left;
         }

         /* Fix button spacing */
         .btn-edit,
         .btn-delete {
            margin: 0 5px;
            padding: 5px 10px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
         }

         /* Fix card spacing */
         .action-card {
            margin-bottom: 20px;
         }

         /* Fix header position */
         .header.expanded {
            left: 60px;
            width: calc(100% - 60px);
         }

         /* Add professional table styling */
         .styled-table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
            font-size: 16px;
            text-align: left;
         }

         .styled-table th,
         .styled-table td {
            padding: 12px 15px;
            border: 1px solid #ddd;
         }

         .styled-table th {
            background-color: var(--primary-color);
            color: white;
            font-weight: bold;
         }

         .styled-table tr:nth-child(even) {
            background-color: #f9f9f9;
         }

         .styled-table tr:hover {
            background-color: #f1f1f1;
         }

         .btn-edit,
         .btn-delete {
            padding: 5px 10px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 14px;
         }

         .btn-edit {
            background-color: var(--primary-color);
            color: white;
         }

         .btn-delete {
            background-color: var(--danger-color);
            color: white;
         }

         .btn-edit:hover {
            background-color: var(--secondary-color);
         }

         .btn-delete:hover {
            background-color: #d9534f;
         }

         .status.active {
            color: #10b981;
            font-weight: bold;
         }

         /* Enhanced Course Grid Layout */
         .course-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
            gap: 20px;
            padding: 20px;
         }

         .course-card {
            background: white;
            border-radius: 15px;
            box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
            transition: transform 0.3s ease, box-shadow 0.3s ease;
            overflow: hidden;
         }

         .course-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 8px 25px rgba(67, 97, 238, 0.2);
         }

         .course-header {
            padding: 20px;
            background: linear-gradient(
               135deg,
               var(--primary-color),
               var(--secondary-color)
            );
            color: white;
            position: relative;
            overflow: hidden;
         }

         .course-header::before {
            content: '';
            position: absolute;
            top: 0;
            right: 0;
            width: 100px;
            height: 100%;
            background: rgba(255, 255, 255, 0.1);
            transform: skewX(-25deg);
         }

         .course-icon {
            position: absolute;
            top: 20px;
            right: 20px;
            font-size: 24px;
            color: rgba(255, 255, 255, 0.9);
         }

         .course-title {
            font-size: 1.5rem;
            font-weight: 600;
            margin-bottom: 5px;
         }

         .course-department {
            font-size: 0.9rem;
            opacity: 0.9;
         }

         .course-body {
            padding: 20px;
         }

         .course-stats {
            display: grid;
            grid-template-columns: repeat(2, 1fr);
            gap: 15px;
            margin-bottom: 20px;
         }

         .stat {
            text-align: center;
            padding: 10px;
            background: rgba(67, 97, 238, 0.05);
            border-radius: 10px;
         }

         .stat-value {
            font-size: 1.2rem;
            font-weight: 600;
            color: var(--primary-color);
         }

         .stat-label {
            font-size: 0.8rem;
            color: #6c757d;
            margin-top: 5px;
         }

         .course-footer {
            padding: 15px 20px;
            border-top: 1px solid rgba(0, 0, 0, 0.05);
            display: flex;
            justify-content: space-between;
            align-items: center;
         }

         .course-status {
            padding: 5px 12px;
            border-radius: 20px;
            font-size: 0.8rem;
            font-weight: 500;
         }

         .status-active {
            background: rgba(16, 185, 129, 0.1);
            color: #10b981;
         }

         .course-actions {
            display: flex;
            gap: 10px;
         }

         .action-btn {
            padding: 8px 15px;
            border: none;
            border-radius: 8px;
            font-size: 0.9rem;
            cursor: pointer;
            transition: all 0.3s ease;
         }

         .btn-edit {
            background: var(--primary-color);
            color: white;
         }

         .btn-edit:hover {
            background: var(--secondary-color);
         }

         .btn-delete {
            background: rgba(247, 37, 133, 0.1);
            color: var(--danger-color);
         }

         .btn-delete:hover {
            background: var(--danger-color);
            color: white;
         }
      </style>
   </head>
   <body>
      <!-- Sidebar -->
      <div class="sidebar" id="sidebar">
         <div class="toggle-sidebar" id="toggleSidebar">
            <i class="fas fa-chevron-left" id="sidebarIcon"></i>
         </div>
         <div class="sidebar-header">
            <div class="user-profile">
               <c:choose>
                  <c:when test="${not empty adminImage}">
                     <img
                        src="${pageContext.request.contextPath}${adminImage}"
                        alt="Admin Profile"
                        class="profile-pic"
                     />
                  </c:when>
                  <c:otherwise>
                     <img
                        src="${pageContext.request.contextPath}/assets/images/default-avatar.png"
                        alt="Default Profile"
                        class="profile-pic"
                     />
                  </c:otherwise>
               </c:choose>
               <span class="online-status"></span>
               <div class="profile-info">
                  <h4>${adminName}</h4>
                  <p>${adminEmail}</p>
               </div>
            </div>
         </div>
         <div class="nav-menu">
            <div class="nav-item active" data-page="dashboard">
               <i class="fas fa-th-large"></i>
               <span class="nav-text">Dashboard</span>
            </div>
            <div class="nav-item" data-page="teachers">
               <i class="fas fa-chalkboard-teacher"></i>
               <span class="nav-text">Teachers</span>
            </div>
            <div class="nav-item" data-page="students">
               <i class="fas fa-user-graduate"></i>
               <span class="nav-text">Students</span>
            </div>
            <div class="nav-item" data-page="courses">
               <i class="fas fa-book"></i>
               <span class="nav-text">Courses</span>
            </div>
            <div class="nav-item" data-page="departments">
               <i class="fas fa-building"></i>
               <span class="nav-text">Departments</span>
            </div>
            <div class="nav-item" data-page="reports">
               <i class="fas fa-chart-bar"></i>
               <span class="nav-text">Reports</span>
            </div>
            <div class="nav-item" data-page="settings">
               <i class="fas fa-cog"></i>
               <span class="nav-text">Settings</span>
            </div>
         </div>
      </div>

      <!-- Header -->
      <div class="header" id="header">
         <div class="search-container">
            <i class="fas fa-search"></i>
            <input type="text" placeholder="Search..." />
         </div>
         <div class="header-actions">
            <div class="action-icon">
               <i class="fas fa-bell fa-lg"></i>
               <span class="badge">3</span>
            </div>
            <div class="action-icon">
               <i class="fas fa-envelope fa-lg"></i>
               <span class="badge">5</span>
            </div>
            <div class="action-icon">
               <i class="fas fa-question-circle fa-lg"></i>
            </div>
            <div class="action-icon">
               <i class="fas fa-sign-out-alt fa-lg"></i>
            </div>
         </div>
      </div>

      <!-- Main Content -->
      <div class="main-content" id="mainContent">
         <!-- Dashboard -->
         <div class="page-container active" id="dashboard">
            <h2 class="page-title">Admin Dashboard</h2>
            <div class="stats-overview">
               <div class="stat-box">
                  <i class="fas fa-chalkboard-teacher fa-2x"></i>
                  <div class="stat-number">${teacherCount}</div>
                  <div class="stat-label">Total Teachers</div>
               </div>
               <div class="stat-box">
                  <i class="fas fa-user-graduate fa-2x"></i>
                  <div class="stat-number">${studentCount}</div>
                  <div class="stat-label">Total Students</div>
               </div>
               <div class="stat-box">
                  <i class="fas fa-book fa-2x"></i>
                  <div class="stat-number">${courseCount}</div>
                  <div class="stat-label">Active Courses</div>
               </div>
               <div class="stat-box">
                  <i class="fas fa-building fa-2x"></i>
                  <div class="stat-number">${departmentCount}</div>
                  <div class="stat-label">Departments</div>
               </div>
            </div>
         </div>

         <!-- Teachers Page -->
         <div class="page-container" id="teachers">
            <h2 class="page-title">Teachers Management</h2>
            <div class="card">
               <div class="card-header">
                  <h3 class="card-title">All Teachers</h3>
                  <div class="card-actions">
                     <button class="btn btn-primary" onclick="showAddTeacherForm()">Add New Teacher</button>
                  </div>
               </div>
               <table class="styled-table">
                  <thead>
                     <tr>
                        <th>ID</th>
                        <th>Name</th>
                        <th>Email</th>
                        <th>Contact</th>
                        <th>Department</th>
                        <th>Status</th>
                        <th>Actions</th>
                     </tr>
                  </thead>
                  <tbody>
                     <c:forEach items="${teachers}" var="teacher">
                        <tr>
                           <td>${teacher.id}</td>
                           <td>${teacher.name}</td>
                           <td>${teacher.email}</td>
                           <td>${teacher.contact}</td>
                           <td>${teacher.department}</td>
                           <td>${teacher.status}</td>
                           <td>
                              <!-- Edit Teacher Button -->
                              <button class="btn-edit" onclick="showEditTeacherForm('${teacher.id}', '${teacher.name}', '${teacher.email}', '${teacher.contact}', '${teacher.departmentId}', '${teacher.status}')">
                                 Edit
                              </button>
                              <!-- Delete Teacher Form -->
                              <form action="${pageContext.request.contextPath}/admin/dashboard" method="POST" style="display:inline;">
                                 <input type="hidden" name="action" value="deleteTeacher">
                                 <input type="hidden" name="id" value="${teacher.id}">
                                 <button class="btn-delete" onclick="return confirm('Are you sure you want to delete this teacher?');">
                                    Delete
                                 </button>
                              </form>
                           </td>
                        </tr>
                     </c:forEach>
                  </tbody>
               </table>
            </div>
         </div>

         <!-- Add/Edit Teacher Modal -->
         <div id="teacherModal" style="display:none; position:fixed; top:50%; left:50%; transform:translate(-50%, -50%); background:white; padding:20px; box-shadow:0 4px 6px rgba(0,0,0,0.1); border-radius:8px;">
            <h3 id="teacherModalTitle">Add Teacher</h3>
            <form action="${pageContext.request.contextPath}/admin/dashboard" method="POST">
               <input type="hidden" id="teacherAction" name="action" value="addTeacher">
               <input type="hidden" id="teacherId" name="id">
               <div class="form-group">
                  <label for="teacherName">Name</label>
                  <input type="text" id="teacherName" name="name" class="form-control" required>
               </div>
               <div class="form-group">
                  <label for="teacherEmail">Email</label>
                  <input type="email" id="teacherEmail" name="email" class="form-control" required>
               </div>
               <div class="form-group">
                  <label for="teacherContact">Contact</label>
                  <input type="text" id="teacherContact" name="contact" class="form-control">
               </div>
               <div class="form-group">
                  <label for="teacherDepartment">Department</label>
                  <select id="teacherDepartment" name="departmentId" class="form-control" required>
                     <c:forEach items="${departments}" var="department">
                        <option value="${department.id}">${department.name}</option>
                     </c:forEach>
                  </select>
               </div>
               <div class="form-group">
                  <label for="teacherStatus">Status</label>
                  <select id="teacherStatus" name="status" class="form-control" required>
                     <option value="active">Active</option>
                     <option value="inactive">Inactive</option>
                  </select>
               </div>
               <div class="form-group">
                  <button type="submit" class="btn btn-primary">Save</button>
                  <button type="button" class="btn btn-secondary" onclick="closeTeacherModal()">Cancel</button>
               </div>
            </form>
         </div>

         <script>
            function showAddTeacherForm() {
               document.getElementById('teacherModalTitle').innerText = 'Add Teacher';
               document.getElementById('teacherAction').value = 'addTeacher';
               document.getElementById('teacherId').value = '';
               document.getElementById('teacherName').value = '';
               document.getElementById('teacherEmail').value = '';
               document.getElementById('teacherContact').value = '';
               document.getElementById('teacherDepartment').value = '';
               document.getElementById('teacherStatus').value = 'active';
               document.getElementById('teacherModal').style.display = 'block';
            }

            function showEditTeacherForm(id, name, email, contact, departmentId, status) {
               document.getElementById('teacherModalTitle').innerText = 'Edit Teacher';
               document.getElementById('teacherAction').value = 'editTeacher';
               document.getElementById('teacherId').value = id;
               document.getElementById('teacherName').value = name;
               document.getElementById('teacherEmail').value = email;
               document.getElementById('teacherContact').value = contact;
               document.getElementById('teacherDepartment').value = departmentId;
               document.getElementById('teacherStatus').value = status;
               document.getElementById('teacherModal').style.display = 'block';
            }

            function closeTeacherModal() {
               document.getElementById('teacherModal').style.display = 'none';
            }
         </script>

         <!-- Students Page -->
         <div class="page-container" id="students">
            <h2 class="page-title">Students Management</h2>
            <div class="card">
               <div class="card-header">
                  <h3 class="card-title">All Students</h3>
                  <div class="card-actions">
                     <button class="btn btn-primary" onclick="showAddStudentForm()">Add New Student</button>
                  </div>
               </div>
               <table class="styled-table">
                  <thead>
                     <tr>
                        <th>ID</th>
                        <th>Full Name</th>
                        <th>Email</th>
                        <th>Tel</th>
                        <th>Gender</th>
                        <th>Level</th>
                        <th>Status</th>
                        <th>Actions</th>
                     </tr>
                  </thead>
                  <tbody>
                     <c:forEach items="${students}" var="student">
                        <tr>
                           <td>${student.id}</td>
                           <td>${student.fullName}</td>
                           <td>${student.email}</td>
                           <td>${student.tel}</td>
                           <td>${student.gender}</td>
                           <td>${student.level}</td>
                           <td>${student.status}</td>
                           <td>
                              <!-- Edit Student Button -->
                              <button class="btn-edit" onclick="showEditStudentForm('${student.id}', '${student.fullName}', '${student.email}', '${student.tel}', '${student.gender}', '${student.level}', '${student.status}')">
                                 Edit
                              </button>
                              <!-- Delete Student Form -->
                              <form action="${pageContext.request.contextPath}/admin/dashboard" method="POST" style="display:inline;">
                                 <input type="hidden" name="action" value="deleteStudent">
                                 <input type="hidden" name="id" value="${student.id}">
                                 <button class="btn-delete" onclick="return confirm('Are you sure you want to delete this student?');">
                                    Delete
                                 </button>
                              </form>
                           </td>
                        </tr>
                     </c:forEach>
                  </tbody>
               </table>
            </div>
         </div>

         <!-- Add/Edit Student Modal -->
         <div id="studentModal" style="display:none; position:fixed; top:50%; left:50%; transform:translate(-50%, -50%); background:white; padding:20px; box-shadow:0 4px 6px rgba(0,0,0,0.1); border-radius:8px;">
            <h3 id="studentModalTitle">Add Student</h3>
            <form action="${pageContext.request.contextPath}/admin/dashboard" method="POST">
               <input type="hidden" id="studentAction" name="action" value="addStudent">
               <input type="hidden" id="studentId" name="id">
               <div class="form-group">
                  <label for="studentFullName">Full Name</label>
                  <input type="text" id="studentFullName" name="fullName" class="form-control" required>
               </div>
               <div class="form-group">
                  <label for="studentEmail">Email</label>
                  <input type="email" id="studentEmail" name="email" class="form-control" required>
               </div>
               <div class="form-group">
                  <label for="studentTel">Tel</label>
                  <input type="text" id="studentTel" name="tel" class="form-control">
               </div>
               <div class="form-group">
                  <label for="studentGender">Gender</label>
                  <select id="studentGender" name="gender" class="form-control" required>
                     <option value="male">Male</option>
                     <option value="female">Female</option>
                     <option value="other">Other</option>
                  </select>
               </div>
               <div class="form-group">
                  <label for="studentLevel">Level</label>
                  <input type="text" id="studentLevel" name="level" class="form-control" required>
               </div>
               <div class="form-group">
                  <label for="studentStatus">Status</label>
                  <select id="studentStatus" name="status" class="form-control" required>
                     <option value="active">Active</option>
                     <option value="inactive">Inactive</option>
                  </select>
               </div>
               <div class="form-group">
                  <button type="submit" class="btn btn-primary">Save</button>
                  <button type="button" class="btn btn-secondary" onclick="closeStudentModal()">Cancel</button>
               </div>
            </form>
         </div>

         <script>
            function showAddStudentForm() {
               document.getElementById('studentModalTitle').innerText = 'Add Student';
               document.getElementById('studentAction').value = 'addStudent';
               document.getElementById('studentId').value = '';
               document.getElementById('studentFullName').value = '';
               document.getElementById('studentEmail').value = '';
               document.getElementById('studentTel').value = '';
               document.getElementById('studentGender').value = 'male';
               document.getElementById('studentLevel').value = '';
               document.getElementById('studentStatus').value = 'active';
               document.getElementById('studentModal').style.display = 'block';
            }

            function showEditStudentForm(id, fullName, email, tel, gender, level, status) {
               document.getElementById('studentModalTitle').innerText = 'Edit Student';
               document.getElementById('studentAction').value = 'editStudent';
               document.getElementById('studentId').value = id;
               document.getElementById('studentFullName').value = fullName;
               document.getElementById('studentEmail').value = email;
               document.getElementById('studentTel').value = tel;
               document.getElementById('studentGender').value = gender;
               document.getElementById('studentLevel').value = level;
               document.getElementById('studentStatus').value = status;
               document.getElementById('studentModal').style.display = 'block';
            }

            function closeStudentModal() {
               document.getElementById('studentModal').style.display = 'none';
            }
         </script>

         <!-- Courses Page -->
         <div class="page-container" id="courses">
            <h2 class="page-title">Courses Management</h2>
            <div class="card">
               <div class="card-header">
                  <h3 class="card-title">All Courses</h3>
                  <div class="card-actions">
                     <button class="btn btn-primary" onclick="showAddCourseForm()">Add New Course</button>
                  </div>
               </div>
               <table class="styled-table">
                  <thead>
                     <tr>
                        <th>ID</th>
                        <th>Title</th>
                        <th>Course Code</th>
                        <th>Department</th>
                        <th>Teacher</th>
                        <th>Status</th>
                        <th>Actions</th>
                     </tr>
                  </thead>
                  <tbody>
                     <c:forEach items="${courses}" var="course">
                        <tr>
                           <td>${course.id}</td>
                           <td>${course.title}</td>
                           <td>${course.courseCode}</td>
                           <td>${course.department}</td>
                           <td>${course.teacher}</td>
                           <td>${course.status}</td>
                           <td>
                              <!-- Edit Course Button -->
                              <button class="btn-edit" onclick="showEditCourseForm('${course.id}', '${course.title}', '${course.courseCode}', '${course.description}', '${course.departmentId}', '${course.teacherId}', '${course.schedule}', '${course.status}')">
                                 Edit
                              </button>
                              <!-- Delete Course Form -->
                              <form action="${pageContext.request.contextPath}/admin/dashboard" method="POST" style="display:inline;">
                                 <input type="hidden" name="action" value="deleteCourse">
                                 <input type="hidden" name="id" value="${course.id}">
                                 <button class="btn-delete" onclick="return confirm('Are you sure you want to delete this course?');">
                                    Delete
                                 </button>
                              </form>
                           </td>
                        </tr>
                     </c:forEach>
                  </tbody>
               </table>
            </div>
         </div>

         <!-- Add/Edit Course Modal -->
         <div id="courseModal" style="display:none; position:fixed; top:50%; left:50%; transform:translate(-50%, -50%); background:white; padding:20px; box-shadow:0 4px 6px rgba(0,0,0,0.1); border-radius:8px;">
            <h3 id="courseModalTitle">Add Course</h3>
            <form action="${pageContext.request.contextPath}/admin/dashboard" method="POST">
               <input type="hidden" id="courseAction" name="action" value="addCourse">
               <input type="hidden" id="courseId" name="id">
               <div class="form-group">
                  <label for="courseTitle">Title</label>
                  <input type="text" id="courseTitle" name="title" class="form-control" required>
               </div>
               <div class="form-group">
                  <label for="courseCode">Course Code</label>
                  <input type="text" id="courseCode" name="courseCode" class="form-control" required>
               </div>
               <div class="form-group">
                  <label for="courseDescription">Description</label>
                  <textarea id="courseDescription" name="description" class="form-control"></textarea>
               </div>
               <div class="form-group">
                  <label for="courseDepartment">Department</label>
                  <select id="courseDepartment" name="departmentId" class="form-control" required>
                     <c:forEach items="${departments}" var="department">
                        <option value="${department.id}">${department.name}</option>
                     </c:forEach>
                  </select>
               </div>
               <div class="form-group">
                  <label for="courseTeacher">Teacher</label>
                  <select id="courseTeacher" name="teacherId" class="form-control" required>
                     <c:forEach items="${teachers}" var="teacher">
                        <option value="${teacher.id}">${teacher.name}</option>
                     </c:forEach>
                  </select>
               </div>
               <div class="form-group">
                  <label for="courseSchedule">Schedule</label>
                  <textarea id="courseSchedule" name="schedule" class="form-control"></textarea>
               </div>
               <div class="form-group">
                  <label for="courseStatus">Status</label>
                  <select id="courseStatus" name="status" class="form-control" required>
                     <option value="active">Active</option>
                     <option value="inactive">Inactive</option>
                  </select>
               </div>
               <div class="form-group">
                  <button type="submit" class="btn btn-primary">Save</button>
                  <button type="button" class="btn btn-secondary" onclick="closeCourseModal()">Cancel</button>
               </div>
            </form>
         </div>

         <script>
            function showAddCourseForm() {
               document.getElementById('courseModalTitle').innerText = 'Add Course';
               document.getElementById('courseAction').value = 'addCourse';
               document.getElementById('courseId').value = '';
               document.getElementById('courseTitle').value = '';
               document.getElementById('courseCode').value = '';
               document.getElementById('courseDescription').value = '';
               document.getElementById('courseDepartment').value = '';
               document.getElementById('courseTeacher').value = '';
               document.getElementById('courseSchedule').value = '';
               document.getElementById('courseStatus').value = 'active';
               document.getElementById('courseModal').style.display = 'block';
            }

            function showEditCourseForm(id, title, courseCode, description, departmentId, teacherId, schedule, status) {
               document.getElementById('courseModalTitle').innerText = 'Edit Course';
               document.getElementById('courseAction').value = 'editCourse';
               document.getElementById('courseId').value = id;
               document.getElementById('courseTitle').value = title;
               document.getElementById('courseCode').value = courseCode;
               document.getElementById('courseDescription').value = description;
               document.getElementById('courseDepartment').value = departmentId;
               document.getElementById('courseTeacher').value = teacherId;
               document.getElementById('courseSchedule').value = schedule;
               document.getElementById('courseStatus').value = status;
               document.getElementById('courseModal').style.display = 'block';
            }

            function closeCourseModal() {
               document.getElementById('courseModal').style.display = 'none';
            }
         </script>

         <!-- Departments Page -->
         <div class="page-container" id="departments">
            <h2 class="page-title">Departments Management</h2>
            <div class="card">
               <div class="card-header">
                  <h3 class="card-title">All Departments</h3>
                  <div class="card-actions">
                     <!-- Add Department Button -->
                     <button class="btn btn-primary" onclick="showAddDepartmentForm()">Add New Department</button>
                  </div>
               </div>
               <table class="styled-table">
                  <thead>
                     <tr>
                        <th>ID</th>
                        <th>Department Name</th>
                        <th>Actions</th>
                     </tr>
                  </thead>
                  <tbody>
                     <c:forEach items="${departments}" var="department">
                        <tr>
                           <td>${department.id}</td>
                           <td>${department.name}</td>
                           <td>
                              <!-- Edit Department Button -->
                              <button class="btn-edit" onclick="showEditDepartmentForm('${department.id}', '${department.name}')">
                                 Edit
                              </button>
                              <!-- Delete Department Form -->
                              <form action="${pageContext.request.contextPath}/admin/dashboard" method="POST" style="display:inline;">
                                 <input type="hidden" name="action" value="deleteDepartment">
                                 <input type="hidden" name="departmentId" value="${department.id}">
                                 <button class="btn-delete" onclick="return confirm('Are you sure you want to delete this department?');">
                                    Delete
                                 </button>
                              </form>
                           </td>
                        </tr>
                     </c:forEach>
                  </tbody>
               </table>
            </div>
         </div>

         <!-- Add/Edit Department Modal -->
         <div id="departmentModal" style="display:none; position:fixed; top:50%; left:50%; transform:translate(-50%, -50%); background:white; padding:20px; box-shadow:0 4px 6px rgba(0,0,0,0.1); border-radius:8px;">
            <h3 id="departmentModalTitle">Add Department</h3>
            <form action="${pageContext.request.contextPath}/admin/dashboard" method="POST">
               <input type="hidden" id="departmentAction" name="action" value="addDepartment">
               <input type="hidden" id="departmentId" name="departmentId">
               <div class="form-group">
                  <label for="departmentName">Department Name</label>
                  <input type="text" id="departmentName" name="departmentName" class="form-control" required>
               </div>
               <div class="form-group">
                  <button type="submit" class="btn btn-primary">Save</button>
                  <button type="button" class="btn btn-secondary" onclick="closeDepartmentModal()">Cancel</button>
               </div>
            </form>
         </div>

         <script>
            function showAddDepartmentForm() {
               document.getElementById('departmentModalTitle').innerText = 'Add Department';
               document.getElementById('departmentAction').value = 'addDepartment';
               document.getElementById('departmentId').value = '';
               document.getElementById('departmentName').value = '';
               document.getElementById('departmentModal').style.display = 'block';
            }

            function showEditDepartmentForm(id, name) {
               document.getElementById('departmentModalTitle').innerText = 'Edit Department';
               document.getElementById('departmentAction').value = 'editDepartment';
               document.getElementById('departmentId').value = id;
               document.getElementById('departmentName').value = name;
               document.getElementById('departmentModal').style.display = 'block';
            }

            function closeDepartmentModal() {
               document.getElementById('departmentModal').style.display = 'none';
            }
         </script>

         <!-- Reports Page -->
         <div class="page-container" id="reports">
            <h2 class="page-title">Analytics & Reports</h2>
            <div class="card">
               <div class="card-header"></div>
                  <h3 class="card-title">Performance Overview</h3>
                  <div class="card-actions">
                     <button class="btn btn-primary">Export Report</button>
                  </div>
               </div>
               <div class="reports-content">
                  <div class="performance-stats">
                     <div class="stat-item">
                        <h4>Average Attendance</h4>
                        <div class="stat-value">87%</div>
                        <div class="stat-chart">
                           <div class="progress-bar" style="width: 87%"></div>
                        </div>
                     </div>
                  </div>
               </div>
            </div>
         </div>

         <!-- Settings Page -->
         <div class="page-container" id="settings">
            <h2 class="page-title">System Settings</h2>
            <div class="card">
               <div class="card-header">
                  <h3 class="card-title">General Settings</h3>
               </div>
               <form>
                  <label>Institution Name</label>
                  <input type="text" value="EduTeach Institute" />
                  <label>Admin Email</label>
                  <input type="email" value="admin@eduteach.com" />
                  <button class="btn btn-primary">Save Changes</button>
               </form>
            </div>
         </div>
      </div>

      <script>
         // Sidebar toggle functionality
         const sidebar = document.getElementById('sidebar');
         const toggleSidebar = document.getElementById('toggleSidebar');
         const sidebarIcon = document.getElementById('sidebarIcon');
         const mainContent = document.getElementById('mainContent');

         toggleSidebar.addEventListener('click', () => {
            sidebar.classList.toggle('sidebar-collapsed');
            mainContent.classList.toggle('expanded');
            sidebarIcon.classList.toggle('fa-chevron-left');
            sidebarIcon.classList.toggle('fa-chevron-right');
         });

         // Page navigation functionality
         const navItems = document.querySelectorAll('.nav-item');
         const pageContainers = document.querySelectorAll('.page-container');

         navItems.forEach((item) => {
            item.addEventListener('click', () => {
               navItems.forEach((nav) => nav.classList.remove('active'));
               item.classList.add('active');
               pageContainers.forEach((page) =>
                  page.classList.remove('active')
               );
               const pageId = item.getAttribute('data-page');
               document.getElementById(pageId).classList.add('active');
            });
         });

         // Add logout functionality
         function logout() {
            if (confirm('Are you sure you want to logout?')) {
               window.location.href =
                  '${pageContext.request.contextPath}/AuthServlet?action=logout';
            }
         }

         // Add click handler to logout button
         document
            .querySelector('.action-icon:last-child')
            .addEventListener('click', logout);

         // Filter functionality
         const filterDepartment = document.getElementById('filterDepartment');
         const filterTeacher = document.getElementById('filterTeacher');
         const filterStatus = document.getElementById('filterStatus');
         const courseGrid = document.getElementById('courseGrid');

         function filterCourses() {
            const department = filterDepartment.value.toLowerCase();
            const teacher = filterTeacher.value.toLowerCase();
            const status = filterStatus.value.toLowerCase();

            const courses = courseGrid.querySelectorAll('.course-card');
            courses.forEach((course) => {
               const courseDepartment = course
                  .getAttribute('data-department')
                  .toLowerCase();
               const courseTeacher = course
                  .getAttribute('data-teacher')
                  .toLowerCase();
               const courseStatus = course
                  .getAttribute('data-status')
                  .toLowerCase();

               const matchesDepartment =
                  !department || courseDepartment === department;
               const matchesTeacher = !teacher || courseTeacher === teacher;
               const matchesStatus = !status || courseStatus === status;

               if (matchesDepartment && matchesTeacher && matchesStatus) {
                  course.style.display = 'block';
               } else {
                  course.style.display = 'none';
               }
            });
         }

         filterDepartment.addEventListener('change', filterCourses);
         filterTeacher.addEventListener('change', filterCourses);
         filterStatus.addEventListener('change', filterCourses);
      </script>
   </body>
</html>
