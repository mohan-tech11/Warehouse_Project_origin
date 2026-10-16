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

    String action = request.getParameter("action");

    // UPDATE EXISTING ORDER STATUS
    if ("updateStatus".equals(action)) {
    updateOrderStatus(request, response);
    return;
}

if ("delete".equals(action)) {
    deleteOrder(request, response);
    return;
}

    // CREATE A NEW ORDER
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

        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");

        if (result > 0) {
            response.sendRedirect("orders.html");
        } else {
            response.setStatus(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );
            response.getWriter().print("Order was not created.");
        }

    } catch (Exception e) {
        e.printStackTrace();

        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(
            HttpServletResponse.SC_INTERNAL_SERVER_ERROR
        );
        response.getWriter().print("Unable to create order.");
    }
}

// UPDATE ORDER STATUS
private void updateOrderStatus(HttpServletRequest request,
                               HttpServletResponse response)
        throws IOException {

    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    String orderIdText = request.getParameter("orderId");
    String status = request.getParameter("status");

    if (orderIdText == null || status == null ||
        !status.matches("Pending|Processing|Completed")) {

        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().print(
            "{\"success\":false,\"message\":\"Invalid order ID or status.\"}"
        );
        return;
    }

    try {
        int orderId = Integer.parseInt(orderIdText);

        if (orderId <= 0) {
            throw new NumberFormatException();
        }

        String sql =
            "UPDATE orders SET status = ? WHERE order_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                 connection.prepareStatement(sql)) {

            statement.setString(1, status);
            statement.setInt(2, orderId);

            int rowsUpdated = statement.executeUpdate();

            if (rowsUpdated > 0) {
                response.getWriter().print(
                    "{\"success\":true,\"message\":\"Order status updated successfully.\"}"
                );
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().print(
                    "{\"success\":false,\"message\":\"Order not found.\"}"
                );
            }
        }

    } catch (NumberFormatException e) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().print(
            "{\"success\":false,\"message\":\"Invalid order ID.\"}"
        );

    } catch (Exception e) {
        e.printStackTrace();

        response.setStatus(
            HttpServletResponse.SC_INTERNAL_SERVER_ERROR
        );

        response.getWriter().print(
            "{\"success\":false,\"message\":\"Unable to update order status.\"}"
        );
    }
}

// DELETE ORDER
private void deleteOrder(HttpServletRequest request,
                         HttpServletResponse response)
        throws IOException {

    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    String orderIdText = request.getParameter("orderId");

    if (orderIdText == null || orderIdText.trim().isEmpty()) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

        response.getWriter().print(
            "{\"success\":false,\"message\":\"Order ID is required.\"}"
        );

        return;
    }

    try {
        int orderId = Integer.parseInt(orderIdText);

        if (orderId <= 0) {
            throw new NumberFormatException();
        }

        String sql = "DELETE FROM orders WHERE order_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, orderId);

            int rowsDeleted = statement.executeUpdate();

            if (rowsDeleted > 0) {
                response.getWriter().print(
                    "{\"success\":true,\"message\":\"Order deleted successfully.\"}"
                );
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);

                response.getWriter().print(
                    "{\"success\":false,\"message\":\"Order not found.\"}"
                );
            }
        }

    } catch (NumberFormatException e) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

        response.getWriter().print(
            "{\"success\":false,\"message\":\"Invalid order ID.\"}"
        );

    } catch (Exception e) {
        e.printStackTrace();

        response.setStatus(
            HttpServletResponse.SC_INTERNAL_SERVER_ERROR
        );

        response.getWriter().print(
            "{\"success\":false,\"message\":\"Unable to delete order. It may be referenced by another record.\"}"
        );
    }
}

// GET ALL ORDERS
@Override
protected void doGet(HttpServletRequest request,
                     HttpServletResponse response)
        throws ServletException, IOException {

    String sql =
        "SELECT o.order_id, o.customer_id, " +
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

        StringBuilder json = new StringBuilder("[");
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
                .append(escapeJson(result.getString("customer_name")))
                .append("\",");

            json.append("\"orderDate\":\"")
                .append(escapeJson(result.getString("order_date")))
                .append("\",");

            json.append("\"status\":\"")
                .append(escapeJson(result.getString("status")))
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
            "{\"error\":\"Unable to load orders.\"}"
        );
    }
}

// ESCAPE SPECIAL CHARACTERS IN JSON STRINGS
private String escapeJson(String value) {

    if (value == null) {
        return "";
    }

    return value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t");
}


}
