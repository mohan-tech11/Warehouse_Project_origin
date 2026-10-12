package com.warehouse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.json.JSONArray;
import org.json.JSONObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/addProduct")
public class ProductServlet extends HttpServlet {


// =========================
// ADD PRODUCT
// =========================

@Override
protected void doPost(HttpServletRequest request,
                      HttpServletResponse response)
        throws ServletException, IOException {

    request.setCharacterEncoding("UTF-8");

    String productName = request.getParameter("productName");
    String sku = request.getParameter("sku");
    String category = request.getParameter("category");
    String price = request.getParameter("price");
    String quantity = request.getParameter("quantity");

    String sql = "INSERT INTO products " +
                 "(product_name, sku, category, price, quantity) " +
                 "VALUES (?, ?, ?, ?, ?)";

    try (Connection connection = DBConnection.getConnection();
         PreparedStatement statement =
                 connection.prepareStatement(sql)) {

        statement.setString(1, productName);
        statement.setString(2, sku);
        statement.setString(3, category);
        statement.setDouble(4, Double.parseDouble(price));
        statement.setInt(5, Integer.parseInt(quantity));

        statement.executeUpdate();

        response.sendRedirect("products.html");

    } catch (Exception e) {

        e.printStackTrace();

        response.setStatus(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().println(
                "Unable to add product. Check the server logs.");
    }
}

// =========================
// VIEW ALL PRODUCTS
// =========================

@Override
protected void doGet(HttpServletRequest request,
                     HttpServletResponse response)
        throws ServletException, IOException {

    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    String sql = "SELECT product_id, product_name, sku, " +
                 "category, price, quantity FROM products";

    JSONArray products = new JSONArray();

    try (Connection connection = DBConnection.getConnection();
         PreparedStatement statement =
                 connection.prepareStatement(sql);
         ResultSet result = statement.executeQuery()) {

        while (result.next()) {

            JSONObject product = new JSONObject();

            product.put("id", result.getInt("product_id"));
            product.put("name", result.getString("product_name"));
            product.put("sku", result.getString("sku"));
            product.put("category", result.getString("category"));
            product.put("price", result.getDouble("price"));
            product.put("quantity", result.getInt("quantity"));

            products.put(product);
        }

        response.getWriter().print(products.toString());

    } catch (Exception e) {

        e.printStackTrace();

        response.setStatus(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        response.getWriter().print(
                "{\"error\":\"Unable to load products\"}");
    }
}

// =========================
// UPDATE PRODUCT
// =========================

@Override
protected void doPut(HttpServletRequest request,
                     HttpServletResponse response)
        throws ServletException, IOException {

    request.setCharacterEncoding("UTF-8");
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    try {

        JSONObject data = new JSONObject(
                request.getReader().lines()
                        .reduce("", (a, b) -> a + b));

        int id = data.getInt("id");
        String productName = data.getString("name");
        String sku = data.getString("sku");
        String category = data.getString("category");
        double price = data.getDouble("price");
        int quantity = data.getInt("quantity");

        String sql = "UPDATE products SET " +
                     "product_name = ?, sku = ?, category = ?, " +
                     "price = ?, quantity = ? WHERE product_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, productName);
            statement.setString(2, sku);
            statement.setString(3, category);
            statement.setDouble(4, price);
            statement.setInt(5, quantity);
            statement.setInt(6, id);

            int rows = statement.executeUpdate();

            if (rows > 0) {
                response.getWriter().print(
                        "{\"message\":\"Product updated successfully\"}");
            } else {
                response.setStatus(
                        HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().print(
                        "{\"error\":\"Product not found\"}");
            }
        }

    } catch (Exception e) {

        e.printStackTrace();

        response.setStatus(
                HttpServletResponse.SC_BAD_REQUEST);

        response.getWriter().print(
                "{\"error\":\"Unable to update product\"}");
    }
}

// =========================
// DELETE PRODUCT
// =========================

@Override
protected void doDelete(HttpServletRequest request,
                        HttpServletResponse response)
        throws ServletException, IOException {

    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    try {

        String idValue = request.getParameter("id");

        if (idValue == null || idValue.isBlank()) {
            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print(
                    "{\"error\":\"Product ID is required\"}");
            return;
        }

        int id = Integer.parseInt(idValue);

        String sql = "DELETE FROM products WHERE product_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            int rows = statement.executeUpdate();

            if (rows > 0) {
                response.getWriter().print(
                        "{\"message\":\"Product deleted successfully\"}");
            } else {
                response.setStatus(
                        HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().print(
                        "{\"error\":\"Product not found\"}");
            }
        }

           } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST);

            response.getWriter().print(
                    "{\"error\":\"Unable to delete product\"}");
        }
    }
}