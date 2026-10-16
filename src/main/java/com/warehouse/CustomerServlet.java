package com.warehouse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        if (action == null || action.isBlank()) {
            action = "add";
        }

        try (Connection connection = DBConnection.getConnection()) {
            if ("add".equalsIgnoreCase(action)
                    || "edit".equalsIgnoreCase(action)) {
                String name = request.getParameter("customerName");
                String email = request.getParameter("email");
                String phone = request.getParameter("phone");
                String address = request.getParameter("address");

                if (name == null || name.isBlank()
                        || email == null || email.isBlank()
                        || phone == null || phone.isBlank()
                        || address == null || address.isBlank()) {
                    writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                            "Name, email, phone, and address are required.");
                    return;
                }

                boolean editing = "edit".equalsIgnoreCase(action);
                String sql = editing
                        ? "UPDATE customers SET customer_name = ?, email = ?, "
                            + "phone = ?, address = ? WHERE customer_id = ?"
                        : "INSERT INTO customers "
                            + "(customer_name, email, phone, address) "
                            + "VALUES (?, ?, ?, ?)";

                try (PreparedStatement statement =
                             connection.prepareStatement(sql)) {
                    statement.setString(1, name.trim());
                    statement.setString(2, email.trim());
                    statement.setString(3, phone.trim());
                    statement.setString(4, address.trim());

                    int customerId = 0;
                    if (editing) {
                        customerId = parseCustomerId(
                                request.getParameter("customerId"));
                        statement.setInt(5, customerId);
                    }

                    int rows = statement.executeUpdate();
                    if (editing && rows == 0
                            && !customerExists(connection, customerId)) {
                        writeError(response, HttpServletResponse.SC_NOT_FOUND,
                                "Customer not found.");
                        return;
                    }

                    writeMessage(response, editing
                            ? "Customer updated successfully."
                            : "Customer added successfully.");
                }
            } else if ("delete".equalsIgnoreCase(action)) {
                int customerId = parseCustomerId(
                        request.getParameter("customerId"));
                try (PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM customers WHERE customer_id = ?")) {
                    statement.setInt(1, customerId);
                    if (statement.executeUpdate() == 0) {
                        writeError(response, HttpServletResponse.SC_NOT_FOUND,
                                "Customer not found.");
                        return;
                    }
                }

                writeMessage(response, "Customer deleted successfully.");
            } else {
                writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid customer action.");
            }
        } catch (NumberFormatException e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid customer ID.");
        } catch (SQLException e) {
            e.printStackTrace();
            if ("delete".equalsIgnoreCase(action)
                    && "23000".equals(e.getSQLState())) {
                writeError(response, HttpServletResponse.SC_CONFLICT,
                        "This customer cannot be deleted while orders reference them.");
            } else if ("23000".equals(e.getSQLState())) {
                writeError(response, HttpServletResponse.SC_CONFLICT,
                        "Customer data conflicts with an existing record.");
            } else {
                writeError(response,
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Database operation failed.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            writeError(response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to save customer.");
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
                    .append(escapeJson(result.getString("customer_name")))
                    .append("\",");

                json.append("\"email\":\"")
                    .append(escapeJson(result.getString("email")))
                    .append("\",");

                json.append("\"phone\":\"")
                    .append(escapeJson(result.getString("phone")))
                    .append("\",");

                json.append("\"address\":\"")
                    .append(escapeJson(result.getString("address")))
                    .append("\",");

                json.append("\"createdAt\":\"")
                    .append(escapeJson(result.getString("created_at")))
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
                    "{\"error\":\"Unable to load customers.\"}"
            );
        }
    }

    private boolean customerExists(Connection connection, int customerId)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM customers WHERE customer_id = ?")) {
            statement.setInt(1, customerId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    private int parseCustomerId(String value) {
        if (value == null || value.isBlank()) {
            throw new NumberFormatException("Customer ID is required.");
        }
        int customerId = Integer.parseInt(value);
        if (customerId <= 0) {
            throw new NumberFormatException("Customer ID must be positive.");
        }
        return customerId;
    }

    private void writeMessage(HttpServletResponse response, String message)
            throws IOException {
        response.getWriter().print(
                "{\"message\":\"" + escapeJson(message) + "\"}");
    }

    private void writeError(HttpServletResponse response, int status,
                            String message) throws IOException {
        response.setStatus(status);
        response.getWriter().print(
                "{\"error\":\"" + escapeJson(message) + "\"}");
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }
}
