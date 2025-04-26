<%@page contentType="text/html" pageEncoding="UTF-8"%> <%@ taglib prefix="c"
uri="http://java.sun.com/jsp/jstl/core" %> <% if
(request.getAttribute("courses") == null) {
response.sendRedirect(request.getContextPath() + "/teacher/dashboard"); return;
} if (request.getAttribute("courses") == null) {
response.sendRedirect(request.getContextPath() + "/teacher/dashboard"); return;
} %>
<!DOCTYPE html>
<html lang="en">
   <head>
      <meta charset="UTF-8" />
      <meta name="viewport" content="width=device-width, initial-scale=1.0" />
      <title>EduTeach - Teacher Dashboard</title>
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

         .dashboard {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
            gap: 20px;
            margin-bottom: 25px;
         }

         .stat-card {
            background-color: white;
            border-radius: 10px;
            padding: 20px;
            box-shadow: var(--shadow);
            display: flex;
            align-items: center;
            justify-content: space-between;
            transition: var(--transition);
         }

         .stat-card:hover {
            transform: translateY(-5px);
         }

         .stat-info h3 {
            font-size: 24px;
            margin-bottom: 5px;
         }

         .stat-info p {
            color: #6c757d;
            font-size: 14px;
         }

         .stat-icon {
            width: 50px;
            height: 50px;
            border-radius: 10px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 20px;
            color: white;
         }

         .stat-icon.blue {
            background-color: var(--primary-color);
         }

         .stat-icon.purple {
            background-color: var(--secondary-color);
         }

         .stat-icon.cyan {
            background-color: var(--success-color);
         }

         .stat-icon.red {
            background-color: var(--danger-color);
         }

         .data-grid {
            display: grid;
            grid-template-columns: 2fr 1fr;
            gap: 20px;
         }

         .card {
            background-color: white;
            border-radius: 10px;
            box-shadow: var(--shadow);
            padding: 20px;
            margin-bottom: 20px;
         }

         .card-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 20px;
            padding-bottom: 10px;
            border-bottom: 1px solid rgba(0, 0, 0, 0.1);
         }

         .card-title {
            font-size: 18px;
            font-weight: 600;
         }

         .card-actions {
            display: flex;
            gap: 10px;
         }

         .btn {
            padding: 8px 15px;
            border-radius: 5px;
            border: none;
            font-size: 14px;
            cursor: pointer;
            transition: var(--transition);
         }

         .btn-primary {
            background-color: var(--primary-color);
            color: white;
         }

         .btn-primary:hover {
            background-color: var(--secondary-color);
         }

         table {
            width: 100%;
            border-collapse: collapse;
         }

         th,
         td {
            padding: 12px 15px;
            text-align: left;
            border-bottom: 1px solid rgba(0, 0, 0, 0.05);
         }

         th {
            font-weight: 600;
            color: #6c757d;
         }

         tr:last-child td {
            border-bottom: none;
         }

         .status {
            padding: 5px 10px;
            border-radius: 50px;
            font-size: 12px;
            font-weight: 500;
         }

         .status.active {
            background-color: rgba(16, 185, 129, 0.1);
            color: #10b981;
         }

         .status.pending {
            background-color: rgba(248, 150, 30, 0.1);
            color: var(--warning-color);
         }

         .status.inactive {
            background-color: rgba(247, 37, 133, 0.1);
            color: var(--danger-color);
         }

         .tasks-list {
            display: flex;
            flex-direction: column;
            gap: 15px;
         }

         .task-item {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 10px 0;
            border-bottom: 1px solid rgba(0, 0, 0, 0.05);
         }

         .task-item:last-child {
            border-bottom: none;
         }

         .task-info {
            display: flex;
            align-items: center;
            gap: 10px;
         }

         .task-checkbox {
            appearance: none;
            width: 20px;
            height: 20px;
            border: 2px solid #e5e7eb;
            border-radius: 5px;
            cursor: pointer;
            position: relative;
         }

         .task-checkbox:checked {
            background-color: var(--primary-color);
            border-color: var(--primary-color);
         }

         .task-checkbox:checked::after {
            content: '✓';
            color: white;
            position: absolute;
            font-size: 12px;
            top: 1px;
            left: 5px;
         }

         .task-title {
            font-size: 15px;
         }

         .task-checkbox:checked + .task-title {
            text-decoration: line-through;
            color: #9ca3af;
         }

         .task-date {
            font-size: 12px;
            color: #6c757d;
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

         /* Form Styling */
         .form-container {
            max-width: 800px;
            margin: 0 auto;
            padding: 20px;
         }

         .form-group {
            margin-bottom: 20px;
         }

         .form-group label {
            display: block;
            margin-bottom: 8px;
            font-weight: 500;
            color: var(--dark-color);
         }

         .form-control {
            width: 100%;
            padding: 12px;
            border: 1px solid #e2e8f0;
            border-radius: 8px;
            font-size: 14px;
            transition: all 0.3s ease;
            background-color: #fff;
         }

         .form-control:focus {
            border-color: var(--primary-color);
            box-shadow: 0 0 0 3px rgba(67, 97, 238, 0.1);
            outline: none;
         }

         textarea.form-control {
            min-height: 120px;
            resize: vertical;
         }

         select.form-control {
            appearance: none;
            background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' fill='%236B7280' viewBox='0 0 16 16'%3E%3Cpath d='M8 10.5l-4-4h8l-4 4z'/%3E%3C/svg%3E");
            background-repeat: no-repeat;
            background-position: right 12px center;
            padding-right: 40px;
         }

         .form-submit {
            background-color: var(--primary-color);
            color: white;
            padding: 12px 24px;
            border: none;
            border-radius: 8px;
            font-size: 16px;
            font-weight: 500;
            cursor: pointer;
            transition: all 0.3s ease;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
         }

         .form-submit:hover {
            background-color: var(--secondary-color);
            transform: translateY(-1px);
         }

         .form-submit:active {
            transform: translateY(0);
         }

         .form-submit i {
            font-size: 18px;
         }

         /* Two-column form layout */
         .form-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 20px;
         }

         @media (max-width: 768px) {
            .form-grid {
               grid-template-columns: 1fr;
            }
         }

         /* Form header styling */
         .form-header {
            margin-bottom: 24px;
            padding-bottom: 16px;
            border-bottom: 1px solid #e2e8f0;
         }

         .form-header h3 {
            font-size: 20px;
            color: var(--dark-color);
            margin-bottom: 8px;
         }

         .form-header p {
            color: #6b7280;
            font-size: 14px;
         }
      </style>
      <link
         rel="stylesheet"
         href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css"
      />
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
                  <c:when test="${not empty teacherImage}">
                     <img
                        src="${pageContext.request.contextPath}${teacherImage}"
                        alt="Teacher Profile"
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
                  <h4>${teacherName}</h4>
                  <p>${departmentName}</p>
               </div>
            </div>
         </div>
         <div class="nav-menu">
            <div class="nav-item active" data-page="dashboard">
               <i class="fas fa-th-large"></i>
               <span class="nav-text">Dashboard</span>
            </div>
            <div class="nav-item" data-page="courses">
               <i class="fas fa-book"></i>
               <span class="nav-text">My Courses</span>
            </div>
            <div class="nav-item" data-page="students">
               <i class="fas fa-user-graduate"></i>
               <span class="nav-text">Students</span>
            </div>
            <div class="nav-item" data-page="assignments">
               <i class="fas fa-tasks"></i>
               <span class="nav-text">Assignments</span>
            </div>
            <div class="nav-item" data-page="grades">
               <i class="fas fa-chart-bar"></i>
               <span class="nav-text">Grades</span>
            </div>
            <div class="nav-item" data-page="calendar">
               <i class="fas fa-calendar-alt"></i>
               <span class="nav-text">Calendar</span>
            </div>
            <div class="nav-item" data-page="messages">
               <i class="fas fa-comment-alt"></i>
               <span class="nav-text">Messages</span>
            </div>
            <div class="nav-item" data-page="resources">
               <i class="fas fa-folder"></i>
               <span class="nav-text">Resources</span>
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
               <span class="badge" id="notificationCount">0</span>
            </div>
            <div class="action-icon">
               <i class="fas fa-envelope fa-lg"></i>
               <span class="badge" id="messageCount">0</span>
            </div>
            <div class="action-icon">
               <i class="fas fa-question-circle fa-lg"></i>
            </div>
            <div class="action-icon" onclick="logout()">
               <i class="fas fa-sign-out-alt fa-lg"></i>
            </div>
         </div>
      </div>

      <!-- Main Content -->
      <div class="main-content" id="mainContent">
         <!-- Dashboard Page -->
         <div class="page-container active" id="dashboard">
            <h2 class="page-title">Dashboard</h2>

            <!-- Stats Cards -->
            <div class="dashboard">
               <div class="stat-card">
                  <div class="stat-info">
                     <h3>${totalStudents}</h3>
                     <p>Total Students</p>
                  </div>
                  <div class="stat-icon blue">
                     <i class="fas fa-user-graduate"></i>
                  </div>
               </div>

               <div class="stat-card">
                  <div class="stat-info">
                     <h3>${activeCourses}</h3>
                     <p>Active Courses</p>
                  </div>
                  <div class="stat-icon purple">
                     <i class="fas fa-book"></i>
                  </div>
               </div>

               <div class="stat-card">
                  <div class="stat-info">
                     <h3>12</h3>
                     <p>Assignments Due</p>
                  </div>
                  <div class="stat-icon cyan">
                     <i class="fas fa-tasks"></i>
                  </div>
               </div>

               <div class="stat-card">
                  <div class="stat-info">
                     <h3>87%</h3>
                     <p>Avg. Completion</p>
                  </div>
                  <div class="stat-icon red">
                     <i class="fas fa-chart-line"></i>
                  </div>
               </div>
            </div>

            <!-- Data Grid -->
            <div class="data-grid">
               <!-- Recent Classes -->
               <div class="card">
                  <div class="card-header">
                     <h3 class="card-title">Recent Classes</h3>
                     <div class="card-actions">
                        <button class="btn btn-primary">View All</button>
                     </div>
                  </div>

                  <table>
                     <thead>
                        <tr>
                           <th>Class Name</th>
                           <th>Date</th>
                           <th>Time</th>
                           <th>Students</th>
                           <th>Status</th>
                        </tr>
                     </thead>
                     <tbody>
                        <tr>
                           <td>Advanced Physics</td>
                           <td>April 22, 2025</td>
                           <td>10:00 - 11:30 AM</td>
                           <td>32</td>
                           <td><span class="status active">Completed</span></td>
                        </tr>
                        <tr>
                           <td>Chemistry Lab</td>
                           <td>April 21, 2025</td>
                           <td>2:00 - 3:30 PM</td>
                           <td>28</td>
                           <td><span class="status active">Completed</span></td>
                        </tr>
                        <tr>
                           <td>Biology 101</td>
                           <td>April 21, 2025</td>
                           <td>9:00 - 10:30 AM</td>
                           <td>45</td>
                           <td><span class="status active">Completed</span></td>
                        </tr>
                        <tr>
                           <td>Environmental Science</td>
                           <td>April 20, 2025</td>
                           <td>1:00 - 2:30 PM</td>
                           <td>24</td>
                           <td><span class="status active">Completed</span></td>
                        </tr>
                        <tr>
                           <td>Physics Lab Session</td>
                           <td>April 23, 2025</td>
                           <td>11:00 - 12:30 PM</td>
                           <td>18</td>
                           <td><span class="status pending">Upcoming</span></td>
                        </tr>
                     </tbody>
                  </table>
               </div>

               <!-- Tasks and To-do -->
               <div>
                  <div class="card">
                     <div class="card-header">
                        <h3 class="card-title">Today's Tasks</h3>
                        <div class="card-actions">
                           <button class="btn btn-primary">Add Task</button>
                        </div>
                     </div>

                     <div class="tasks-list">
                        <div class="task-item">
                           <div class="task-info">
                              <input type="checkbox" class="task-checkbox" />
                              <span class="task-title"
                                 >Grade Biology midterm papers</span
                              >
                           </div>
                           <span class="task-date">10:00 AM</span>
                        </div>
                        <div class="task-item">
                           <div class="task-info">
                              <input
                                 type="checkbox"
                                 class="task-checkbox"
                                 checked
                              />
                              <span class="task-title"
                                 >Prepare Physics lab materials</span
                              >
                           </div>
                           <span class="task-date">11:30 AM</span>
                        </div>
                        <div class="task-item">
                           <div class="task-info">
                              <input type="checkbox" class="task-checkbox" />
                              <span class="task-title">Department meeting</span>
                           </div>
                           <span class="task-date">2:00 PM</span>
                        </div>
                        <div class="task-item">
                           <div class="task-info">
                              <input type="checkbox" class="task-checkbox" />
                              <span class="task-title"
                                 >Update course syllabus</span
                              >
                           </div>
                           <span class="task-date">4:30 PM</span>
                        </div>
                     </div>
                  </div>

                  <!-- Calendar -->
                  <div class="card">
                     <div class="card-header">
                        <h3 class="card-title">April 2025</h3>
                        <div class="card-actions">
                           <i class="fas fa-chevron-left"></i>
                           <i class="fas fa-chevron-right"></i>
                        </div>
                     </div>

                     <div class="calendar">
                        <div class="calendar-grid">
                           <div class="calendar-day">Sun</div>
                           <div class="calendar-day">Mon</div>
                           <div class="calendar-day">Tue</div>
                           <div class="calendar-day">Wed</div>
                           <div class="calendar-day">Thu</div>
                           <div class="calendar-day">Fri</div>
                           <div class="calendar-day">Sat</div>

                           <!-- Calendar dates - Adjust as needed -->
                           <div class="calendar-date"></div>
                           <div class="calendar-date">1</div>
                           <div class="calendar-date">2</div>
                           <div class="calendar-date">3</div>
                           <div class="calendar-date">4</div>
                           <div class="calendar-date">5</div>
                           <div class="calendar-date">6</div>

                           <div class="calendar-date">7</div>
                           <div class="calendar-date">8</div>
                           <div class="calendar-date">9</div>
                           <div class="calendar-date">10</div>
                           <div class="calendar-date">11</div>
                           <div class="calendar-date has-event">12</div>
                           <div class="calendar-date">13</div>

                           <div class="calendar-date">14</div>
                           <div class="calendar-date">15</div>
                           <div class="calendar-date has-event">16</div>
                           <div class="calendar-date">17</div>
                           <div class="calendar-date">18</div>
                           <div class="calendar-date">19</div>
                           <div class="calendar-date">20</div>

                           <div class="calendar-date">21</div>
                           <div class="calendar-date today">22</div>
                           <div class="calendar-date has-event">23</div>
                           <div class="calendar-date">24</div>
                           <div class="calendar-date">25</div>
                           <div class="calendar-date">26</div>
                           <div class="calendar-date">27</div>

                           <div class="calendar-date">28</div>
                           <div class="calendar-date">29</div>
                           <div class="calendar-date has-event">30</div>
                        </div>
                     </div>
                  </div>
               </div>
            </div>
         </div>

         <!-- Courses Page -->
         <div class="page-container" id="courses">
            <h2 class="page-title">My Courses</h2>
            <div class="card">
               <div class="card-header">
                  <h3 class="card-title">Active Courses</h3>
                  <div class="card-actions">
                     <button class="btn btn-primary">Add New Course</button>
                  </div>
               </div>
               <div class="course-list">
                  <c:forEach items="${courses}" var="course">
                     <div class="course-item">
                        <div class="course-thumbnail">
                           <i class="fas fa-book"></i>
                        </div>
                        <div class="course-info">
                           <h4 class="course-title">${course.title}</h4>
                           <div class="course-meta">
                              <span
                                 ><i class="fas fa-user-graduate"></i>
                                 ${course.studentCount} Students</span
                              >
                              <span
                                 ><i class="fas fa-building"></i>
                                 ${course.department}</span
                              >
                              <span
                                 ><i class="fas fa-clock"></i>
                                 ${course.schedule}</span
                              >
                           </div>
                        </div>
                        <button class="btn btn-primary">Manage</button>
                     </div>
                  </c:forEach>
               </div>
            </div>
         </div>

         <!-- Students Page -->
         <div class="page-container" id="students">
            <h2 class="page-title">My Students</h2>
            <div class="card">
               <div class="card-header">
                  <h3 class="card-title">Student List</h3>
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
                        </tr>
                     </c:forEach>
                  </tbody>
               </table>
            </div>
         </div>

         <!-- Assignments Page -->
         <div class="page-container" id="assignments">
            <h2 class="page-title">Assignments</h2>
            <div class="card">
               <div class="form-container">
                  <div class="form-header">
                     <h3>Create New Assignment</h3>
                     <p>
                        Fill in the details below to create a new assignment for
                        your students.
                     </p>
                  </div>
                  <form
                     action="${pageContext.request.contextPath}/teacher/dashboard"
                     method="post"
                  >
                     <input type="hidden" name="action" value="addAssignment" />
                     <div class="form-grid">
                        <div class="form-group">
                           <label for="title">Assignment Title</label>
                           <input
                              type="text"
                              id="title"
                              name="title"
                              class="form-control"
                              placeholder="Enter assignment title"
                              required
                           />
                        </div>
                        <div class="form-group">
                           <label for="courseId">Select Course</label>
                           <select
                              id="courseId"
                              name="courseId"
                              class="form-control"
                              required
                           >
                              <option value="">Choose a course</option>
                              <c:forEach items="${courses}" var="course">
                                 <option value="${course.id}">
                                    ${course.title}
                                 </option>
                              </c:forEach>
                           </select>
                        </div>
                     </div>
                     <div class="form-group">
                        <label for="description">Assignment Description</label>
                        <textarea
                           id="description"
                           name="description"
                           class="form-control"
                           placeholder="Enter detailed assignment description"
                        ></textarea>
                     </div>
                     <div class="form-grid">
                        <div class="form-group">
                           <label for="dueDate">Due Date</label>
                           <input
                              type="datetime-local"
                              id="dueDate"
                              name="dueDate"
                              class="form-control"
                              required
                           />
                        </div>
                        <div
                           class="form-group"
                           style="display: flex; align-items: flex-end"
                        >
                           <button type="submit" class="form-submit">
                              <i class="fas fa-plus"></i>
                              Create Assignment
                           </button>
                        </div>
                     </div>
                  </form>
               </div>
            </div>

            <div class="card">
               <div class="card-header">
                  <h3 class="card-title">Assignments</h3>
               </div>
               <table class="styled-table">
                  <thead>
                     <tr>
                        <th>Title</th>
                        <th>Description</th>
                        <th>Course</th>
                        <th>Due Date</th>
                        <th>Created At</th>
                     </tr>
                  </thead>
                  <tbody>
                     <c:forEach items="${assignments}" var="assignment">
                        <tr>
                           <td>${assignment.title}</td>
                           <td>${assignment.description}</td>
                           <td>${assignment.courseTitle}</td>
                           <td>${assignment.dueDate}</td>
                           <td>${assignment.createdAt}</td>
                        </tr>
                     </c:forEach>
                  </tbody>
               </table>
            </div>
         </div>

         <!-- Grades Page -->
         <div class="page-container" id="grades">
            <h2 class="page-title">Grades</h2>
            <div class="card">
               <div class="card-header">
                  <h3 class="card-title">Recent Grades</h3>
               </div>
               <table>
                  <thead>
                     <tr>
                        <th>Student</th>
                        <th>Course</th>
                        <th>Grade</th>
                     </tr>
                  </thead>
                  <tbody>
                     <tr>
                        <td>John Doe</td>
                        <td>Physics 101</td>
                        <td>A</td>
                     </tr>
                     <tr>
                        <td>Jane Smith</td>
                        <td>Chemistry 201</td>
                        <td>B+</td>
                     </tr>
                  </tbody>
               </table>
            </div>
         </div>

         <!-- Calendar Page -->
         <div class="page-container" id="calendar">
            <h2 class="page-title">Calendar</h2>
            <div class="card">
               <div class="card-header">
                  <h3 class="card-title">April 2025</h3>
               </div>
               <div class="calendar">
                  <div class="calendar-grid">
                     <div class="calendar-day">Sun</div>
                     <div class="calendar-day">Mon</div>
                     <div class="calendar-day">Tue</div>
                     <div class="calendar-day">Wed</div>
                     <div class="calendar-day">Thu</div>
                     <div class="calendar-day">Fri</div>
                     <div class="calendar-day">Sat</div>
                     <!-- Calendar dates -->
                     <div class="calendar-date"></div>
                     <div class="calendar-date">1</div>
                     <div class="calendar-date">2</div>
                     <div class="calendar-date">3</div>
                     <div class="calendar-date">4</div>
                     <div class="calendar-date">5</div>
                     <div class="calendar-date">6</div>
                     <div class="calendar-date">7</div>
                     <div class="calendar-date">8</div>
                     <div class="calendar-date">9</div>
                     <div class="calendar-date">10</div>
                     <div class="calendar-date">11</div>
                     <div class="calendar-date has-event">12</div>
                     <div class="calendar-date">13</div>
                     <div class="calendar-date">14</div>
                     <div class="calendar-date">15</div>
                     <div class="calendar-date has-event">16</div>
                     <div class="calendar-date">17</div>
                     <div class="calendar-date">18</div>
                     <div class="calendar-date">19</div>
                     <div class="calendar-date">20</div>
                     <div class="calendar-date">21</div>
                     <div class="calendar-date today">22</div>
                     <div class="calendar-date has-event">23</div>
                     <div class="calendar-date">24</div>
                     <div class="calendar-date">25</div>
                     <div class="calendar-date">26</div>
                     <div class="calendar-date">27</div>
                     <div class="calendar-date">28</div>
                     <div class="calendar-date">29</div>
                     <div class="calendar-date has-event">30</div>
                  </div>
               </div>
            </div>
         </div>

         <!-- Messages Page -->
         <div class="page-container" id="messages">
            <h2 class="page-title">Messages</h2>
            <div class="card">
               <div class="card-header">
                  <h3 class="card-title">Inbox</h3>
               </div>
               <ul>
                  <li>
                     <strong>John Doe:</strong> Can you clarify the assignment
                     requirements?
                  </li>
                  <li>
                     <strong>Jane Smith:</strong> Thank you for the feedback on
                     my project!
                  </li>
               </ul>
            </div>
         </div>

         <!-- Resources Page -->
         <div class="page-container" id="resources">
            <h2 class="page-title">Resources</h2>
            <div class="card">
               <div class="card-header">
                  <h3 class="card-title">Available Resources</h3>
               </div>
               <ul>
                  <li><a href="#">Physics Lab Manual</a></li>
                  <li><a href="#">Chemistry Experiment Guide</a></li>
               </ul>
            </div>
         </div>

         <!-- Settings Page -->
         <div class="page-container" id="settings">
            <h2 class="page-title">Settings</h2>
            <div class="card">
               <div class="card-header">
                  <h3 class="card-title">Account Settings</h3>
               </div>
               <form>
                  <label for="username">Username:</label>
                  <input type="text" id="username" value="Dr. Sarah Johnson" />
                  <label for="email">Email:</label>
                  <input
                     type="email"
                     id="email"
                     value="sarah.johnson@example.com"
                  />
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
               // Remove active class from all nav items
               navItems.forEach((nav) => nav.classList.remove('active'));
               // Add active class to the clicked nav item
               item.classList.add('active');

               // Hide all pages
               pageContainers.forEach((page) =>
                  page.classList.remove('active')
               );
               // Show the selected page
               const pageId = item.getAttribute('data-page');
               document.getElementById(pageId).classList.add('active');
            });
         });

         // Logout function
         function logout() {
            if (confirm('Are you sure you want to logout?')) {
               window.location.href =
                  '${pageContext.request.contextPath}/AuthServlet?action=logout';
            }
         }

         // Update notification counts
         document.addEventListener('DOMContentLoaded', function () {
            fetch(
               '${pageContext.request.contextPath}/teacher/teacherDashboard?action=getNotifications'
            )
               .then((response) => response.json())
               .then((data) => {
                  document.getElementById('notificationCount').textContent =
                     data.notifications || 0;
                  document.getElementById('messageCount').textContent =
                     data.messages || 0;
               })
               .catch((error) =>
                  console.error('Error fetching notifications:', error)
               );
         });
      </script>
   </body>
</html>
