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

@WebServlet("/warehouses")
public class WarehouseServlet extends HttpServlet {


// GET: Display all warehouses
@Override
protected void doGet(HttpServletRequest request,
                     HttpServletResponse response)
        throws ServletException, IOException {

    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    String sql = "SELECT warehouse_id, warehouse_name, location, capacity "
               + "FROM warehouses ORDER BY warehouse_id ASC";

    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        StringBuilder json = new StringBuilder("[");
        boolean first = true;

        while (rs.next()) {

            if (!first) {
                json.append(",");
            }

            json.append("{")
                .append("\"warehouseId\":")
                .append(rs.getInt("warehouse_id"))
                .append(",\"warehouseName\":\"")
                .append(escapeJson(rs.getString("warehouse_name")))
                .append("\",\"location\":\"")
                .append(escapeJson(rs.getString("location")))
                .append("\",\"capacity\":")
                .append(rs.getInt("capacity"))
                .append("}");

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
            "{\"error\":\"Unable to load warehouses\"}"
        );
    }
}

// POST: Add, edit, or delete a warehouse
@Override
protected void doPost(HttpServletRequest request,
                     HttpServletResponse response)
        throws IOException {

    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    String action = request.getParameter("action");
    if (action == null || action.isBlank()) {
        action = "add";
    }

    String warehouseName = request.getParameter("warehouseName");
    String location = request.getParameter("location");
    String capacityParameter = request.getParameter("capacity");

    try (Connection con = DBConnection.getConnection()) {

        if ("add".equalsIgnoreCase(action)
                || "edit".equalsIgnoreCase(action)) {

            if (warehouseName == null || warehouseName.trim().isEmpty()
                    || location == null || location.trim().isEmpty()
                    || capacityParameter == null
                    || capacityParameter.isBlank()) {
                writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Warehouse name, location, and capacity are required.");
                return;
            }

            int capacity;
            try {
                capacity = Integer.parseInt(capacityParameter);
            } catch (NumberFormatException e) {
                writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Capacity must be a whole number.");
                return;
            }

            if (capacity < 0) {
                writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Capacity cannot be negative.");
                return;
            }

            boolean editing = "edit".equalsIgnoreCase(action);
            String sql = editing
                    ? "UPDATE warehouses SET warehouse_name = ?, location = ?, "
                        + "capacity = ? WHERE warehouse_id = ?"
                    : "INSERT INTO warehouses "
                        + "(warehouse_name, location, capacity) VALUES (?, ?, ?)";

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, warehouseName.trim());
                ps.setString(2, location.trim());
                ps.setInt(3, capacity);

                if (editing) {
                    int warehouseId = parseWarehouseId(
                            request.getParameter("warehouseId"));
                    ps.setInt(4, warehouseId);
                }

                int rows = ps.executeUpdate();
                if (editing && rows == 0
                        && !warehouseExists(con, parseWarehouseId(
                                request.getParameter("warehouseId")))) {
                    writeError(response, HttpServletResponse.SC_NOT_FOUND,
                            "Warehouse not found.");
                    return;
                }

                writeMessage(response, editing
                        ? "Warehouse updated successfully."
                        : "Warehouse added successfully.");
            }
        } else if ("delete".equalsIgnoreCase(action)) {
            int warehouseId = parseWarehouseId(
                    request.getParameter("warehouseId"));

            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM warehouses WHERE warehouse_id = ?")) {
                ps.setInt(1, warehouseId);
                if (ps.executeUpdate() == 0) {
                    writeError(response, HttpServletResponse.SC_NOT_FOUND,
                            "Warehouse not found.");
                    return;
                }
            }

            writeMessage(response, "Warehouse deleted successfully.");
        } else {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid warehouse action.");
        }
    } catch (NumberFormatException e) {
        writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                "Invalid warehouse ID.");
    } catch (java.sql.SQLException e) {
        e.printStackTrace();
        if ("23000".equals(e.getSQLState())) {
            writeError(response, HttpServletResponse.SC_CONFLICT,
                    "This warehouse cannot be deleted while inventory records reference it.");
        } else {
            writeError(response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Database operation failed.");
        }
    } catch (Exception e) {
        e.printStackTrace();
        writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Unable to save warehouse.");
    }
}

private boolean warehouseExists(Connection connection, int warehouseId)
        throws java.sql.SQLException {
    try (PreparedStatement statement = connection.prepareStatement(
            "SELECT 1 FROM warehouses WHERE warehouse_id = ?")) {
        statement.setInt(1, warehouseId);
        try (ResultSet result = statement.executeQuery()) {
            return result.next();
        }
    }
}

private int parseWarehouseId(String value) {
    if (value == null || value.isBlank()) {
        throw new NumberFormatException("Warehouse ID is required.");
    }

    int warehouseId = Integer.parseInt(value);
    if (warehouseId <= 0) {
        throw new NumberFormatException("Warehouse ID must be positive.");
    }
    return warehouseId;
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
// Escape special characters before placing text in JSON.
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
