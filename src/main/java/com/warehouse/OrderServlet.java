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

@WebServlet("/addOrder")
public class OrderServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {

        String customerId = request.getParameter("customerId");
        String totalAmount = request.getParameter("totalAmount");
        String status = request.getParameter("status");

        String sql = "INSERT INTO orders " +
                     "(customer_id, status, total_amount) " +
                     "VALUES (?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, Integer.parseInt(customerId));
            statement.setString(2, status);
            statement.setDouble(3, Double.parseDouble(totalAmount));

            int result = statement.executeUpdate();

            if (result > 0) {
                response.sendRedirect("orders.html");
            } else {
                response.getWriter().println(
                        "Order was not created."
                );
            }

        } catch (Exception e) {

            e.printStackTrace();

            response.setContentType("text/html");

            response.getWriter().println(
                    "<h2>Order Error</h2>"
            );

            response.getWriter().println(
                    "<p>" + e.getMessage() + "</p>"
            );

            response.getWriter().println(
                    "<a href='orders.html'>Back to Orders</a>"
            );
        }
    }


    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        String sql = "SELECT o.order_id, o.customer_id, " +
             "c.customer_name, o.order_date, " +
             "o.status, o.total_amount " +
             "FROM orders o " +
             "INNER JOIN customers c " +
             "ON o.customer_id = c.customer_id " +
             "ORDER BY o.order_id DESC";

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
                    .append(result.getInt("order_id"))
                    .append(",");

               json.append("\"customerId\":")
    .append(result.getInt("customer_id"))
    .append(",");

json.append("\"customerName\":\"")
    .append(
        result.getString("customer_name")
              .replace("\\", "\\\\")
              .replace("\"", "\\\"")
    )
    .append("\",");

                json.append("\"orderDate\":\"")
                    .append(result.getString("order_date"))
                    .append("\",");

                json.append("\"status\":\"")
                    .append(result.getString("status"))
                    .append("\",");

                json.append("\"amount\":")
                    .append(result.getDouble("total_amount"));

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


