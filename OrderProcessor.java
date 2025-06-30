import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import server.inventory.StockManager;

import java.math.BigDecimal;

public class OrderProcessor {
    private DBManager dbManager;
    private StockManager stockManager;

    public OrderProcessor() {
        this.dbManager = DBManager.getInstance();
        this.stockManager = new StockManager();
    }

    public OrderProcessor(StockManager stockManager) {
        this.dbManager = DBManager.getInstance();
        this.stockManager = stockManager;
    }

    public boolean placeOrder(int customerId, int branchId, int[] drinkIds, int[] quantities) {
        if (drinkIds.length != quantities.length) {
            System.err.println("Drink IDs and quantities arrays must have the same length");
            return false;
        }

        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            conn.setAutoCommit(false);

            // First validate customer exists
            if (!customerExists(conn, customerId)) {
                System.err.println("Customer ID " + customerId + " does not exist");
                conn.rollback();
                return false;
            }

            // Check stock availability for all items first
            for (int i = 0; i < drinkIds.length; i++) {
                if (!stockManager.isStockAvailable(branchId, drinkIds[i], quantities[i])) {
                    System.err.println("Insufficient stock for drink ID " + drinkIds[i] +
                            " at branch ID " + branchId + ". Required: " + quantities[i]);
                    conn.rollback();
                    return false;
                }
            }

            // Create order
            String orderSql = "INSERT INTO orders (customer_id, branch_id, order_date, total_amount) VALUES (?, ?, NOW(), ?)";
            BigDecimal totalAmount = calculateOrderTotal(conn, drinkIds, quantities);

            int orderId;
            try (PreparedStatement orderStmt = conn.prepareStatement(orderSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                orderStmt.setInt(1, customerId);
                orderStmt.setInt(2, branchId);
                orderStmt.setBigDecimal(3, totalAmount);
                orderStmt.executeUpdate();

                ResultSet generatedKeys = orderStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    orderId = generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Order creation failed, no ID obtained");
                }
            }

            // Add order items and reduce stock
            String itemSql = "INSERT INTO order_items (order_id, drink_id, quantity, unit_price, total_price) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement itemStmt = conn.prepareStatement(itemSql)) {
                for (int i = 0; i < drinkIds.length; i++) {
                    BigDecimal unitPrice = getDrinkPrice(conn, drinkIds[i]);
                    BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(quantities[i]));

                    itemStmt.setInt(1, orderId);
                    itemStmt.setInt(2, drinkIds[i]);
                    itemStmt.setInt(3, quantities[i]);
                    itemStmt.setBigDecimal(4, unitPrice);
                    itemStmt.setBigDecimal(5, itemTotal);
                    itemStmt.addBatch();

                    // Reduce stock for this item
                    if (!stockManager.reduceStock(branchId, drinkIds[i], quantities[i])) {
                        throw new SQLException("Failed to reduce stock for drink ID " + drinkIds[i]);
                    }
                }
                itemStmt.executeBatch();
            }

            conn.commit();
            System.out.println("Order " + orderId + " placed successfully. Total: $" + totalAmount);
            return true;

        } catch (SQLException e) {
            System.err.println("Error placing order: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Error rolling back transaction: " + rollbackEx.getMessage());
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
    }

    private boolean customerExists(Connection conn, int customerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM customers WHERE customer_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        }
    }

    private BigDecimal calculateOrderTotal(Connection conn, int[] drinkIds, int[] quantities) throws SQLException {
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < drinkIds.length; i++) {
            BigDecimal unitPrice = getDrinkPrice(conn, drinkIds[i]);
            BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(quantities[i]));
            total = total.add(itemTotal);
        }
        return total;
    }

    private BigDecimal getDrinkPrice(Connection conn, int drinkId) throws SQLException {
        String sql = "SELECT price FROM drinks WHERE drink_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, drinkId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getBigDecimal("price");
            }
            throw new SQLException("Drink not found: " + drinkId);
        }
    }

    // Method to view order details
    public void viewOrder(int orderId) {
        try (Connection conn = dbManager.getConnection()) {
            String orderSql = """
                SELECT o.*, c.full_name, b.name as branch_name
                FROM orders o
                JOIN customers c ON o.customer_id = c.customer_id
                JOIN branches b ON o.branch_id = b.branch_id
                WHERE o.order_id = ?
            """;

            try (PreparedStatement orderStmt = conn.prepareStatement(orderSql)) {
                orderStmt.setInt(1, orderId);
                ResultSet orderRs = orderStmt.executeQuery();

                if (orderRs.next()) {
                    System.out.println("Order Details:");
                    System.out.println("==============");
                    System.out.println("Order ID: " + orderRs.getInt("order_id"));
                    System.out.println("Customer: " + orderRs.getString("full_name"));
                    System.out.println("Branch: " + orderRs.getString("branch_name"));
                    System.out.println("Date: " + orderRs.getTimestamp("order_date"));
                    System.out.println("Total: $" + orderRs.getBigDecimal("total_amount"));
                    System.out.println();

                    // Get order items
                    String itemsSql = """
                        SELECT oi.*, dn.name as drink_name, ds.size, d.brand
                        FROM order_items oi
                        JOIN drinks d ON oi.drink_id = d.drink_id
                        JOIN drink_names dn ON d.drink_name_id = dn.drink_name_id
                        JOIN drink_sizes ds ON d.size_id = ds.size_id
                        WHERE oi.order_id = ?
                    """;

                    try (PreparedStatement itemsStmt = conn.prepareStatement(itemsSql)) {
                        itemsStmt.setInt(1, orderId);
                        ResultSet itemsRs = itemsStmt.executeQuery();

                        System.out.println("Items:");
                        while (itemsRs.next()) {
                            System.out.printf("- %s %s %s | Qty: %d | Unit: $%.2f | Total: $%.2f%n",
                                    itemsRs.getString("brand"),
                                    itemsRs.getString("drink_name"),
                                    itemsRs.getString("size"),
                                    itemsRs.getInt("quantity"),
                                    itemsRs.getBigDecimal("unit_price"),
                                    itemsRs.getBigDecimal("total_price"));
                        }
                    }
                } else {
                    System.out.println("Order not found: " + orderId);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error viewing order: " + e.getMessage());
        }
    }

    // Method to get recent orders for a customer
    public void getCustomerOrderHistory(int customerId, int limit) {
        try (Connection conn = dbManager.getConnection()) {
            String sql = """
                SELECT o.order_id, o.order_date, o.total_amount, b.name as branch_name
                FROM orders o
                JOIN branches b ON o.branch_id = b.branch_id
                WHERE o.customer_id = ?
                ORDER BY o.order_date DESC
                LIMIT ?
            """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, customerId);
                stmt.setInt(2, limit);
                ResultSet rs = stmt.executeQuery();

                System.out.println("Order History for Customer ID: " + customerId);
                System.out.println("=============================================");

                while (rs.next()) {
                    System.out.printf("Order #%d | %s | Branch: %s | Total: $%.2f%n",
                            rs.getInt("order_id"),
                            rs.getTimestamp("order_date"),
                            rs.getString("branch_name"),
                            rs.getBigDecimal("total_amount"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting customer order history: " + e.getMessage());
        }
    }
}