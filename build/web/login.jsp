<%-- Document : login Created on : Apr 22, 2025, 10:59:15 p.m. Author : ASPIRE
I7 --%> <%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
   <head>
      <meta charset="UTF-8" />
      <meta name="viewport" content="width=device-width, initial-scale=1.0" />
      <title>EduTeach - Login</title>
      <link
         rel="stylesheet"
         href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css"
      />
      <style>
         :root {
            --primary-color: #4361ee;
            --secondary-color: #3f37c9;
            --success-color: #4cc9f0;
            --danger-color: #f72585;
            --warning-color: #ff9e00;
            --light-color: #f8f9fa;
            --dark-color: #212529;
            --box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
         }

         * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
         }

         body {
            background: linear-gradient(135deg, #f5f7fa, #e4e7eb);
            min-height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
         }

         .container {
            width: 100%;
            max-width: 1200px;
            padding: 0 15px;
            margin: 0 auto;
            display: flex;
            justify-content: center;
         }

         .auth-wrapper {
            display: flex;
            width: 900px;
            box-shadow: var(--box-shadow);
            border-radius: 10px;
            overflow: hidden;
            background: white;
         }

         .auth-banner {
            flex: 1;
            background: url('/api/placeholder/400/900') center/cover;
            position: relative;
            display: flex;
            flex-direction: column;
            justify-content: flex-end;
            padding: 40px;
            color: white;
         }

         .auth-banner::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: linear-gradient(
               to top,
               rgba(0, 0, 0, 0.8),
               rgba(0, 0, 0, 0.4)
            );
            z-index: 1;
         }

         .auth-banner-content {
            position: relative;
            z-index: 2;
         }

         .auth-banner h1 {
            font-size: 2.5rem;
            margin-bottom: 10px;
         }

         .auth-banner p {
            font-size: 1rem;
            opacity: 0.9;
            line-height: 1.6;
         }

         .auth-form {
            flex: 1;
            padding: 40px;
            display: flex;
            flex-direction: column;
         }

         .auth-form h2 {
            font-size: 1.8rem;
            margin-bottom: 30px;
            color: var(--dark-color);
            text-align: center;
         }

         .input-group {
            margin-bottom: 20px;
            position: relative;
         }

         .input-group label {
            display: block;
            margin-bottom: 8px;
            font-weight: 500;
            color: var(--dark-color);
         }

         .input-group input {
            width: 100%;
            padding: 15px 15px 15px 45px;
            border: 1px solid #ddd;
            border-radius: 5px;
            font-size: 1rem;
            transition: all 0.3s;
         }

         .input-group input:focus {
            border-color: var(--primary-color);
            outline: none;
            box-shadow: 0 0 0 2px rgba(67, 97, 238, 0.2);
         }

         .input-group i {
            position: absolute;
            left: 15px;
            top: 42px;
            color: #999;
         }

         .role-selection {
            display: flex;
            gap: 10px;
            margin-bottom: 20px;
            justify-content: center;
         }

         .role-option {
            flex: 1;
            text-align: center;
            padding: 15px 10px;
            border: 1px solid #ddd;
            border-radius: 5px;
            cursor: pointer;
            transition: all 0.3s;
         }

         .role-option:hover {
            border-color: var(--primary-color);
         }

         .role-option.active {
            border-color: var(--primary-color);
            background: rgba(67, 97, 238, 0.1);
         }

         .role-option i {
            font-size: 24px;
            margin-bottom: 8px;
            display: block;
            color: var(--primary-color);
         }

         .button {
            padding: 15px;
            border: none;
            border-radius: 5px;
            background: var(--primary-color);
            color: white;
            font-size: 1rem;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s;
            margin-top: 10px;
         }

         .button:hover {
            background: var(--secondary-color);
         }

         .form-footer {
            margin-top: 30px;
            text-align: center;
         }

         .form-footer a {
            color: var(--primary-color);
            text-decoration: none;
            font-weight: 500;
         }

         .form-footer a:hover {
            text-decoration: underline;
         }

         .alert {
            padding: 15px;
            margin-bottom: 20px;
            border-radius: 5px;
            font-weight: 500;
         }

         .alert-danger {
            background-color: rgba(247, 37, 133, 0.1);
            border: 1px solid rgba(247, 37, 133, 0.3);
            color: var(--danger-color);
         }

         .auth-tabs {
            display: flex;
            margin-bottom: 20px;
         }

         .auth-tab {
            flex: 1;
            text-align: center;
            padding: 15px;
            cursor: pointer;
            font-weight: 600;
            color: #999;
            border-bottom: 2px solid transparent;
            transition: all 0.3s;
         }

         .auth-tab.active {
            color: var(--primary-color);
            border-bottom-color: var(--primary-color);
         }

         .forgot-password {
            text-align: right;
            margin-bottom: 20px;
         }

         .forgot-password a {
            color: #999;
            text-decoration: none;
            font-size: 0.9rem;
         }

         .forgot-password a:hover {
            color: var(--primary-color);
         }

         @media (max-width: 768px) {
            .auth-wrapper {
               flex-direction: column;
               width: 100%;
            }

            .auth-banner {
               display: none;
            }
         }
      </style>
   </head>
   <body>
      <div class="container">
         <div class="auth-wrapper">
            <div class="auth-banner">
               <div class="auth-banner-content">
                  <h1>EduTeach</h1>
                  <p>
                     Leading educational platform connecting students and
                     teachers for a better learning experience.
                  </p>
               </div>
            </div>
            <div class="auth-form">
               <div class="auth-tabs">
                  <div
                     id="login-tab"
                     class="auth-tab active"
                     onclick="showLoginForm()"
                  >
                     Login
                  </div>
                  <div
                     id="signup-tab"
                     class="auth-tab"
                     onclick="showSignupForm()"
                  >
                     Sign Up
                  </div>
               </div>

               <% if (request.getAttribute("error") != null) { %>
               <div class="alert alert-danger">
                  <%= request.getAttribute("error") %>
               </div>
               <% } %>

               <!-- Login Form -->
               <form
                  id="login-form"
                  action="AuthServlet"
                  method="post"
                  style="display: block"
               >
                  <input type="hidden" name="action" value="login" />
                  <input type="hidden" name="csrfToken" value="${csrfToken}" />

                  <div class="role-selection">
                     <div
                        class="role-option active"
                        data-role="student"
                        onclick="selectRole(this, 'student')"
                     >
                        <i class="fas fa-user-graduate"></i>
                        Student
                     </div>
                     <div
                        class="role-option"
                        data-role="teacher"
                        onclick="selectRole(this, 'teacher')"
                     >
                        <i class="fas fa-chalkboard-teacher"></i>
                        Teacher
                     </div>
                     <div
                        class="role-option"
                        data-role="admin"
                        onclick="selectRole(this, 'admin')"
                     >
                        <i class="fas fa-user-shield"></i>
                        Admin
                     </div>
                  </div>
                  <input
                     type="hidden"
                     name="role"
                     id="selected-role"
                     value="student"
                  />

                  <div class="input-group">
                     <label for="login-email">Email Address</label>
                     <i class="fas fa-envelope"></i>
                     <input
                        type="email"
                        id="login-email"
                        name="email"
                        required
                        placeholder="Enter your email address"
                     />
                  </div>

                  <div class="input-group">
                     <label for="password">Password</label>
                     <i class="fas fa-lock"></i>
                     <input
                        type="password"
                        id="password"
                        name="password"
                        required
                     />
                  </div>

                  <div class="forgot-password">
                     <a href="forgotPassword.jsp">Forgot password?</a>
                  </div>

                  <button type="submit" class="button">Login</button>

                  <div class="form-footer">
                     <p>
                        New student?
                        <a href="javascript:void(0)" onclick="showSignupForm()"
                           >Create an account</a
                        >
                     </p>
                  </div>
               </form>

               <!-- Signup Form -->
               <form
                  id="signup-form"
                  action="AuthServlet"
                  method="post"
                  style="display: none"
               >
               <input type="hidden" name="action" value="signup" />
               <input type="hidden" name="csrfToken" value="${csrfToken}" />
                  

                  <div class="input-group">
                     <label for="fullname">Full Name</label>
                     <i class="fas fa-user"></i>
                     <input
                        type="text"
                        id="fullname"
                        name="fullname"
                        required
                     />
                  </div>

                  <div class="input-group">
                     <label for="signup-email">Email</label>
                     <i class="fas fa-envelope"></i>
                     <input
                        type="email"
                        id="signup-email"
                        name="email"
                        required
                     />
                  </div>

                  <div class="input-group">
                     <label for="phone">Phone Number</label>
                     <i class="fas fa-phone"></i>
                     <input type="tel" id="phone" name="phone" required />
                  </div>

                  <div class="input-group">
                     <label for="signup-password">Password</label>
                     <i class="fas fa-lock"></i>
                     <input
                        type="password"
                        id="signup-password"
                        name="password"
                        required
                     />
                  </div>

                  <div class="input-group">
                     <label for="confirm-password">Confirm Password</label>
                     <i class="fas fa-lock"></i>
                     <input
                        type="password"
                        id="confirm-password"
                        name="confirmPassword"
                        required
                     />
                  </div>

                  <button type="submit" class="button">Create Account</button>

                  <div class="form-footer">
                     <p>
                        Already have an account?
                        <a href="javascript:void(0)" onclick="showLoginForm()"
                           >Login</a
                        >
                     </p>
                  </div>
               </form>
            </div>
         </div>
      </div>

      <script>
         function selectRole(element, role) {
            // Remove active class from all options
            document.querySelectorAll('.role-option').forEach((option) => {
               option.classList.remove('active');
            });

            // Add active class to selected option
            element.classList.add('active');

            // Update hidden input
            document.getElementById('selected-role').value = role;
         }

         function showLoginForm() {
            document.getElementById('login-form').style.display = 'block';
            document.getElementById('signup-form').style.display = 'none';
            document.getElementById('login-tab').classList.add('active');
            document.getElementById('signup-tab').classList.remove('active');
         }

         function showSignupForm() {
            document.getElementById('login-form').style.display = 'none';
            document.getElementById('signup-form').style.display = 'block';
            document.getElementById('login-tab').classList.remove('active');
            document.getElementById('signup-tab').classList.add('active');
         }
      </script>
   </body>
</html>
