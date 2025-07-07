package server.inventory;
import java.sql.*;
import java.util.*;

import server.database.DatabaseManager;



public class StockManager {
    private DatabaseManager dbManager;

    public StockManager() {
        this.dbManager = new DatabaseManager();
    }


    public List<Map<String, Object>> getStockByBranch(int branchId) {
        try {
            List<Map<String, Object>> list = dbManager.getStockByBranch(branchId);
            return list;
        } catch (Exception e) {
            System.err.println("Error getting stock: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // Add new stock entry
    public boolean addStock(int branchId, int drinkId, int quantity, int minThreshold) {
        if (quantity < 0 || minThreshold < 0) {
            System.err.println("Invalid values");
            return false;
        }

        try {
            if (stockExists(branchId, drinkId)) {
                System.err.println("Stock already exists");
                return false;
            }

            dbManager.addStock(branchId, drinkId, quantity, minThreshold);
            return true;
        } catch (SQLException e) {
            System.err.println("Error adding stock: " + e.getMessage());
            return false;
        }
    }

    // Update quantity of an existing stock
    public boolean updateStock(int stockId, int newQuantity) {
        if (newQuantity < 0) {
            System.err.println("Invalid quantity");
            return false;
        }

        try {
            dbManager.updateStock(stockId, newQuantity);
            return true;
        } catch (SQLException e) {
            System.err.println("Error updating stock: " + e.getMessage());
            return false;
        }
    }

    // Reduce stock after a sale or order
    public boolean reduceStock(int branchId, int drinkId, int amount) {
        if (amount <= 0) return false;

        try {
            Map<String, Object> stock = getStockItem(branchId, drinkId);
            if (stock == null) return false;

            int current = (Integer) stock.get("quantity");
            if (current < amount) return false;

            int stockId = (Integer) stock.get("stock_id");
            dbManager.updateStock(stockId, current - amount);
            return true;
        } catch (SQLException e) {
            System.err.println("Error reducing stock: " + e.getMessage());
            return false;
        }
    }

    // Check if stock is available (used by other classes)
    public boolean isStockAvailable(int branchId, int drinkId, int amount) {
        try {
            Map<String, Object> stock = getStockItem(branchId, drinkId);
            if (stock == null) return false;

            int quantity = (Integer) stock.get("quantity");
            return quantity >= amount;
        } catch (SQLException e) {
            System.err.println("Error checking availability: " + e.getMessage());
            return false;
        }
    }

    // Check if a stock entry already exists
    private boolean stockExists(int branchId, int drinkId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM stock WHERE branch_id = ? AND drink_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, branchId);
            stmt.setInt(2, drinkId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    // Get a single stock item by branch and drink
    private Map<String, Object> getStockItem(int branchId, int drinkId) throws SQLException {
        String sql = "SELECT * FROM stock WHERE branch_id = ? AND drink_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, branchId);
            stmt.setInt(2, drinkId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Map<String, Object> stock = new HashMap<>();
                stock.put("stock_id", rs.getInt("stock_id"));
                stock.put("quantity", rs.getInt("quantity"));
                return stock;
            } else {
                return null;
            }
        }
    }
}
