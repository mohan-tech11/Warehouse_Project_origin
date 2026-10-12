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

@WebServlet("/inventory")
public class InventoryServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        String sql =
            "SELECT i.inventory_id, p.product_name, " +
            "i.quantity, w.location " +
            "FROM inventory i " +
            "JOIN products p ON i.product_id = p.product_id " +
            "JOIN warehouses w ON i.warehouse_id = w.warehouse_id " +
            "ORDER BY i.inventory_id ASC";

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

                json.append("{")
                    .append("\"inventoryId\":")
                    .append(result.getInt("inventory_id"))
                    .append(",")
                    .append("\"productName\":\"")
                    .append(escapeJson(
                        result.getString("product_name")))
                    .append("\",")
                    .append("\"quantity\":")
                    .append(result.getInt("quantity"))
                    .append(",")
                    .append("\"location\":\"")
                    .append(escapeJson(
                        result.getString("location")))
                    .append("\"")
                    .append("}");

                first = false;
            }

            json.append("]");

            response.getWriter().print(json.toString());

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            response.getWriter().print(
                "{\"error\":\"Unable to load inventory\"}");
        }
    }



  @Override
protected void doPost(HttpServletRequest request,
                      HttpServletResponse response)
        throws ServletException, IOException {

    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    String action = request.getParameter("action");

    if (action == null || action.trim().isEmpty()) {
        action = "add";
    }

    try (Connection connection = DBConnection.getConnection()) {

        if ("add".equals(action)) {

            int productId = Integer.parseInt(
                    request.getParameter("productId"));

            int warehouseId = Integer.parseInt(
                    request.getParameter("warehouseId"));

            int quantity = Integer.parseInt(
                    request.getParameter("quantity"));

            if (productId <= 0 || warehouseId <= 0 || quantity <= 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().print(
                        "{\"message\":\"Product, warehouse and positive quantity are required.\"}");
                return;
            }

            String sql = "INSERT INTO inventory "
                       + "(product_id, warehouse_id, quantity) "
                       + "VALUES (?, ?, ?)";

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setInt(1, productId);
                statement.setInt(2, warehouseId);
                statement.setInt(3, quantity);

                statement.executeUpdate();
            }

            response.getWriter().print(
                    "{\"message\":\"Stock added successfully.\"}");

        } else if ("edit".equals(action)) {

            int inventoryId = Integer.parseInt(
                    request.getParameter("inventoryId"));

            int quantity = Integer.parseInt(
                    request.getParameter("quantity"));

            if (inventoryId <= 0 || quantity <= 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().print(
                        "{\"message\":\"Enter a valid inventory ID and positive quantity.\"}");
                return;
            }

            String sql = "UPDATE inventory "
                       + "SET quantity = ? "
                       + "WHERE inventory_id = ?";

            int rows;

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setInt(1, quantity);
                statement.setInt(2, inventoryId);

                rows = statement.executeUpdate();
            }

            if (rows == 0) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().print(
                        "{\"message\":\"Inventory record not found.\"}");
                return;
            }

            response.getWriter().print(
                    "{\"message\":\"Stock updated successfully.\"}");

        } else if ("delete".equals(action)) {

            int inventoryId = Integer.parseInt(
                    request.getParameter("inventoryId"));

            if (inventoryId <= 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().print(
                        "{\"message\":\"Invalid inventory ID.\"}");
                return;
            }

            String sql = "DELETE FROM inventory WHERE inventory_id = ?";

            int rows;

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setInt(1, inventoryId);
                rows = statement.executeUpdate();
            }

            if (rows == 0) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().print(
                        "{\"message\":\"Inventory record not found.\"}");
                return;
            }

            response.getWriter().print(
                    "{\"message\":\"Stock deleted successfully.\"}");

        } else {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print(
                    "{\"message\":\"Invalid action.\"}");
        }

    } catch (NumberFormatException e) {

        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().print(
                "{\"message\":\"Please enter valid numeric values.\"}");

    } catch (Exception e) {

        e.printStackTrace();

        response.setStatus(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        response.getWriter().print(
                "{\"message\":\"Database operation failed.\"}");
    }
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

