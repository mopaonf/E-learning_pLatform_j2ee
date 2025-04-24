<%-- Document : newjsp Created on : Apr 1, 2025, 9:44:36 a.m. Author : ASPIRE I7
--%> <%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
   <head>
      <meta charset="UTF-8" />
      <meta name="viewport" content="width=device-width, initial-scale=1.0" />
      <title>LogIn Form</title>
      <link rel="stylesheet" href="style.css" />
      <script
         src="https://kit.fontawesome.com/333b565e70.js"
         crossorigin="anonymous"
      ></script>
      <style>
         .container {
            height: 800px;
         }
         body {
            background-color: rgb(82, 81, 81);
         }
         .form-box {
            width: 90%;
            height: 550px;
            max-width: 450px;
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            background: #fff;
            padding: 50px 60px 70px;
            text-align: center;
         }
         .form-box h1 {
            font-size: 30px;
            margin-bottom: 60px;
            color: #3c00a0;
            position: relative;
         }
         .form-box h1::after {
            content: '';
            width: 30px;
            height: 4px;
            border-radius: 3px;
            background: #3c00a0;
            position: absolute;
            bottom: -12px;
            left: 50%;
            transform: translate(-50%);
         }

         .input-field {
            background: #eaeaea;
            margin: 15px 0;
            border-radius: 3px;
            display: flex;
            align-items: center;
            max-height: 65px;
            transition: max-height 0.5s;
            overflow: hidden;
         }

         input {
            width: 100%;
            background: transparent;
            border: 0;
            outline: 0;
            padding: 18px 15px;
         }
         .input-field i {
            margin-left: 15px;
            color: #999;
         }
         form p {
            text-align: left;
            font-size: 13px;
         }
         form p a {
            text-decoration: none;
            color: #3c00a0;
         }
         .btn-field {
            width: 100%;
            display: flex;
            justify-content: space-between;
            padding-top: 150px;
            padding-left: 110px;
         }
         .btn-field button {
            flex-basis: 48%;
            background-color: #3c00a0;
            color: #fff;
            height: 40px;
            border-radius: 20px;
            border: 0;
            outline: 0;
            cursor: pointer;
            transition: background 1s;
         }
         .input-group {
            height: 280px;
         }
         .btn-field button.disable {
            background: #eaeaea;
            color: #555;
         }
      </style>
   </head>
   <body>
      <div class="container">
         <div class="form-box">
            <h1 id="title">Add Student</h1>
            <form action="AddStudent" method="post">
               <div class="input-group">
                  <div class="input-field">
                     <i class="fa-solid fa-envelope"></i>
                     <input
                        type="text"
                        name="surname"
                        placeholder="Surname"
                        required
                     />
                  </div>
                  <div class="input-field">
                     <i class="fa-solid fa-envelope"></i>
                     <input
                        type="text"
                        name="level"
                        placeholder="Level"
                        required
                     />
                  </div>
                  <div class="input-field">
                     <i class="fa-solid fa-envelope"></i>
                     <input type="text" name="tel" placeholder="Tel" required />
                  </div>
                  <div class="input-field">
                     <i class="fa-solid fa-envelope"></i>
                     <input
                        type="email"
                        name="email"
                        placeholder="Email"
                        required
                     />
                  </div>
                  <div class="input-field">
                     <i class="fa-solid fa-envelope"></i>
                     <input
                        type="text"
                        name="gender"
                        placeholder="Gender"
                        required
                     />
                  </div>
               </div>
               <div class="btn-field">
                  <button type="submit" id="signupbtn">Add Student</button>
               </div>
            </form>
         </div>
      </div>

      <script>
         let signupbtn = document.getElementById('signupbtn');
         let signinbtn = document.getElementById('signinbtn');
         let namefield = document.getElementById('namefield');
         let title = document.getElementById('title');

         signinbtn.onclick = function () {
            namefield.style.maxHeight = '0';
            title.innerHTML = 'Sign In';
            signupbtn.classList.add('disable');
            signinbtn.classList.remove('disable');
         };

         signupbtn.onclick = function () {
            namefield.style.maxHeight = '60px';
            title.innerHTML = 'Sign In';
            signupbtn.classList.remove('disable');
            signinbtn.classList.add('disable');
         };
      </script>
   </body>
</html>
