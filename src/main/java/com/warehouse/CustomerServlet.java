package com.warehouse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/addCustomer")
public class CustomerServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {

        String name = request.getParameter("customerName");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String address = request.getParameter("address");

        String sql = "INSERT INTO customers " +
                     "(customer_name, email, phone, address) " +
                     "VALUES (?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, phone);
            statement.setString(4, address);

            int result = statement.executeUpdate();

            if (result > 0) {

                response.sendRedirect("customers.html");

            } else {

                response.getWriter().println(
                        "Customer was not added."
                );
            }

        } catch (Exception e) {

            e.printStackTrace();

            response.setContentType("text/html");

            response.getWriter().println(
                    "<h2>Customer Error</h2>"
            );

            response.getWriter().println(
                    "<p>" + e.getMessage() + "</p>"
            );

            response.getWriter().println(
                    "<a href='customers.html'>Back to Customers</a>"
            );
        }
    }


    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

       String sql = "SELECT customer_id, customer_name, " +
             "email, phone, address, created_at " +
             "FROM customers " +
             "ORDER BY customer_id ASC";

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            StringBuilder json = new StringBuilder();

            json.append("[");

            boolean first = true;

            while (result.next()) {

                if (!first) {
                    json.append(",");
                }

                json.append("{");

                json.append("\"id\":")
                    .append(result.getInt("customer_id"))
                    .append(",");

                json.append("\"name\":\"")
                    .append(result.getString("customer_name"))
                    .append("\",");

                json.append("\"email\":\"")
                    .append(result.getString("email"))
                    .append("\",");

                json.append("\"phone\":\"")
                    .append(result.getString("phone"))
                    .append("\",");

                json.append("\"address\":\"")
                    .append(result.getString("address"))
                    .append("\",");

                json.append("\"createdAt\":\"")
                    .append(result.getString("created_at"))
                    .append("\"");

                json.append("}");

                first = false;
            }

            json.append("]");

            response.getWriter().print(json.toString());

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            response.getWriter().print(
                    "{\"error\":\"" + e.getMessage() + "\"}"
            );
        }
    }
}

