package com.warehouse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONObject;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/dashboardStats")
public class DashboardServlet extends HttpServlet {

private int getCount(Connection con, String tableName)
        throws SQLException {

    // Only allow known table names in this query.
    switch (tableName) {
        case "products":
        case "customers":
        case "orders":
        case "warehouses":
        case "dispatches":
            break;

        default:
            throw new IllegalArgumentException("Invalid table name");
    }

    String sql = "SELECT COUNT(*) FROM " + tableName;

    try (PreparedStatement ps = con.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        if (rs.next()) {
            return rs.getInt(1);
        }
    }

    return 0;
}

@Override
protected void doGet(
        HttpServletRequest request,
        HttpServletResponse response)
        throws IOException {

    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    JSONObject stats = new JSONObject();

    try (Connection con = DBConnection.getConnection()) {

        stats.put("products", getCount(con, "products"));
        stats.put("customers", getCount(con, "customers"));
        stats.put("orders", getCount(con, "orders"));
        stats.put("warehouses", getCount(con, "warehouses"));
        stats.put("dispatches", getCount(con, "dispatches"));

        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(stats.toString());

    } catch (Exception e) {

        e.printStackTrace();

        response.setStatus(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        JSONObject error = new JSONObject();
        error.put("error", "Unable to load dashboard statistics");

        response.getWriter().write(error.toString());
    }
}


}
