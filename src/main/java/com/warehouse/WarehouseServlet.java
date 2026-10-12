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

    String sql = "SELECT warehouse_id, warehouse_name, location, phone "
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
                .append("\",\"phone\":\"")
                .append(escapeJson(rs.getString("phone")))
                .append("\"}");

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

// POST: Save a new warehouse
@Override
protected void doPost(HttpServletRequest request,
                     HttpServletResponse response)
        throws IOException {

    String warehouseName = request.getParameter("warehouseName");
    String location = request.getParameter("location");

    response.setContentType("text/plain");
    response.setCharacterEncoding("UTF-8");

    if (warehouseName == null || warehouseName.trim().isEmpty()
            || location == null || location.trim().isEmpty()) {

        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().println("Error: Warehouse name and location are required.");
        return;
    }

    String sql = "INSERT INTO warehouses (warehouse_name, location, phone) VALUES (?, ?, ?)";

    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, warehouseName.trim());
        ps.setString(2, location.trim());
        ps.setString(3, "");

        int rows = ps.executeUpdate();

        if (rows > 0) {
            response.getWriter().println("SUCCESS: Warehouse saved successfully!");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("ERROR: Warehouse was not inserted.");
        }

    } catch (Exception e) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        e.printStackTrace();
        response.getWriter().println("DATABASE ERROR: " + e.getMessage());
    }
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

