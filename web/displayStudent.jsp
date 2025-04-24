<%-- 
    Document   : displayStudent
    Created on : Mar 24, 2025, 1:45:36 p.m.
    Author     : ASPIRE I7
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
   <head>
      <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
      <title>JSP Page</title>
      <style>
         body {
            font-family: Arial, sans-serif;
            margin: 0;
            padding: 0;
            background-color: #e9ecef;
         }
         h1 {
            text-align: center;
            color: #343a40;
         }
         .container {
            width: 97%;
            margin: 0 auto;
            padding: 20px;
            margin-top: 200px;
            background-color: #fff;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
         }
         .add-student-btn {
            display: block;
            margin: 20px 0;
            text-align: right;
         }
         .add-student-btn button {
            padding: 10px 20px;
            background-color: #40ac64;
            color: #fff;
            border: none;
            border-radius: 5px;
            cursor: pointer;
         }
         .add-student-btn button:hover {
            background-color: #5a3a3a;
         }
         table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
            margin-left: auto;
            margin-right: auto;
         }
         table,
         th,
         td {
            border: 1px solid #dee2e6;
         }
         th,
         td {
            padding: 10px;
            text-align: center;
         }
         th {
            background-color: #343a40;
            color: #fff;
            border: 3px solid #dee2e6;
         }
         tr:nth-child(even) {
            background-color: #f8f9fa;
         }
         tr:hover {
            background-color: #e9ecef;
         }
         .action-buttons button {
            padding: 10px 20px;

            margin-right: 5px;
            border: none;
            border-radius: 5px;
            cursor: pointer;
         }
         .action-buttons .edit-btn {
            background-color: #40ac64;
            color: #fff;
            width: 100px;
         }
         .action-buttons .delete-btn {
            background-color: #dc3545;
            width: 100px;
            color: #fff;
         }
         .action-buttons button:hover {
            opacity: 0.8;
         }
      </style>
   </head>
   <body>
      <div class="container">
         <h1>Welcome to student Management System</h1>
         <div class="add-student-btn">
            <a href="http://localhost:8080/firstProject/addstudent.jsp"><button>Add student</button></a>
         </div>
         <table>
            <thead>
               <tr style="border: 3px solid #161718">
                  <td>ID</td>
                  <td>Surname</td>
                  <td>Level</td>
                  <td>Tel</td>
                  <td>Email</td>
                  <td>Gender</td>
                  <td>Action</td>
               </tr>
            </thead>
            <tbody>
               <tr>
                  <td>1</td>
                  <td>John</td>
                  <td>2</td>
                  <td>674016435</td>
                  <td>John@gmail.com</td>
                  <td>male</td>
                  <td class="action-buttons">
                     <button class="edit-btn">Edit</button>
                     <button class="delete-btn">Delete</button>
                  </td>
               </tr>
            </tbody>
         </table>
      </div>
   </body>
</html>
