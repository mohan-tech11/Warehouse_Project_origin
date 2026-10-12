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

@WebServlet("/dispatches")
public class DispatchServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String sql = """
            SELECT dispatch_id, order_id, dispatch_date,
                   courier_name, tracking_number, status
            FROM dispatches
            ORDER BY dispatch_id DESC
            """;

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
                    .append("\"dispatchId\":")
                    .append(rs.getInt("dispatch_id"))
                    .append(",\"orderId\":")
                    .append(rs.getInt("order_id"))
                    .append(",\"dispatchDate\":\"")
                    .append(escape(rs.getString("dispatch_date")))
                    .append("\",\"carrier\":\"")
                    .append(escape(rs.getString("courier_name")))
                    .append("\",\"trackingNumber\":\"")
                    .append(escape(rs.getString("tracking_number")))
                    .append("\",\"status\":\"")
                    .append(escape(rs.getString("status")))
                    .append("\"}");

                first = false;
            }

            json.append("]");
            response.getWriter().write(json.toString());

        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(
                "{\"error\":\"Unable to load dispatches\"}"
            );
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");

        if (action == null) {
            action = "add";
        }

        try (Connection con = DBConnection.getConnection()) {

            if ("add".equalsIgnoreCase(action)) {

                int orderId = Integer.parseInt(
                    request.getParameter("orderId")
                );

                String carrier = request.getParameter("carrier");
                String tracking = request.getParameter("tracking");
                String status = request.getParameter("status");

                String sql = """
                    INSERT INTO dispatches
                    (order_id, dispatch_date, courier_name,
                     tracking_number, status)
                    VALUES (?, CURRENT_DATE, ?, ?, ?)
                    """;

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, orderId);
                    ps.setString(2, carrier);
                    ps.setString(3, tracking);
                    ps.setString(4, status);

                    ps.executeUpdate();
                }

                response.getWriter().write(
                    "{\"message\":\"Dispatch added successfully\"}"
                );

            } else if ("edit".equalsIgnoreCase(action)) {

                int dispatchId = Integer.parseInt(
                    request.getParameter("dispatchId")
                );

                int orderId = Integer.parseInt(
                    request.getParameter("orderId")
                );

                String carrier = request.getParameter("carrier");
                String tracking = request.getParameter("tracking");
                String status = request.getParameter("status");

                String sql = """
                    UPDATE dispatches
                    SET order_id = ?, courier_name = ?,
                        tracking_number = ?, status = ?
                    WHERE dispatch_id = ?
                    """;

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, orderId);
                    ps.setString(2, carrier);
                    ps.setString(3, tracking);
                    ps.setString(4, status);
                    ps.setInt(5, dispatchId);

                    int rows = ps.executeUpdate();

                    if (rows == 0) {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        response.getWriter().write(
                            "{\"error\":\"Dispatch not found\"}"
                        );
                        return;
                    }
                }

                response.getWriter().write(
                    "{\"message\":\"Dispatch updated successfully\"}"
                );

            } else if ("delete".equalsIgnoreCase(action)) {

                int dispatchId = Integer.parseInt(
                    request.getParameter("dispatchId")
                );

                String sql =
                    "DELETE FROM dispatches WHERE dispatch_id = ?";

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, dispatchId);

                    int rows = ps.executeUpdate();

                    if (rows == 0) {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        response.getWriter().write(
                            "{\"error\":\"Dispatch not found\"}"
                        );
                        return;
                    }
                }

                response.getWriter().write(
                    "{\"message\":\"Dispatch deleted successfully\"}"
                );

            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(
                    "{\"error\":\"Invalid action\"}"
                );
            }

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(
                "{\"error\":\"Invalid numeric value\"}"
            );

        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(
                "{\"error\":\"Database operation failed\"}"
            );
            e.printStackTrace();
        }
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r");
    }
}

