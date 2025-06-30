package server.services;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import server.database.DatabaseManager;



public class BranchSyncService {
    private DatabaseManager dbManager;

    public BranchSyncService() {
        this.dbManager = new DatabaseManager();
    }

    public boolean transferStock(int fromBranchId, int toBranchId, int drinkId, int quantity) {
        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            conn.setAutoCommit(false);

            // First check if source has enough stock
            if (!hasEnoughStock(conn, fromBranchId, drinkId, quantity)) {
                System.err.println("Insufficient stock at source branch");
                conn.rollback();
                return false;
            }

            // Check if destination has stock record, if not create it
            ensureStockRecord(conn, toBranchId, drinkId);

            // Reduce stock at source
            String reduceSql = "UPDATE stock SET quantity = quantity - ? WHERE branch_id = ? AND drink_id = ?";
            try (PreparedStatement reduceStmt = conn.prepareStatement(reduceSql)) {
                reduceStmt.setInt(1, quantity);
                reduceStmt.setInt(2, fromBranchId);
                reduceStmt.setInt(3, drinkId);
                int rowsAffected = reduceStmt.executeUpdate();

                if (rowsAffected == 0) {
                    throw new SQLException("No stock record found at source branch");
                }
            }

            // Increase stock at destination
            String increaseSql = "UPDATE stock SET quantity = quantity + ? WHERE branch_id = ? AND drink_id = ?";
            try (PreparedStatement increaseStmt = conn.prepareStatement(increaseSql)) {
                increaseStmt.setInt(1, quantity);
                increaseStmt.setInt(2, toBranchId);
                increaseStmt.setInt(3, drinkId);
                increaseStmt.executeUpdate();
            }

            // Log transfer
            String logSql = "INSERT INTO stock_transfers (from_branch_id, to_branch_id, drink_id, quantity, transfer_date) VALUES (?, ?, ?, ?, NOW())";
            try (PreparedStatement logStmt = conn.prepareStatement(logSql)) {
                logStmt.setInt(1, fromBranchId);
                logStmt.setInt(2, toBranchId);
                logStmt.setInt(3, drinkId);
                logStmt.setInt(4, quantity);
                logStmt.executeUpdate();
            }

            conn.commit();
            System.out.println("Stock transfer completed successfully");
            return true;

        } catch (SQLException e) {
            System.err.println("Error during stock transfer: " + e.getMessage());
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

    private boolean hasEnoughStock(Connection conn, int branchId, int drinkId, int requiredQuantity) throws SQLException {
        String sql = "SELECT quantity FROM stock WHERE branch_id = ? AND drink_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, branchId);
            pstmt.setInt(2, drinkId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int availableQuantity = rs.getInt("quantity");
                return availableQuantity >= requiredQuantity;
            }
            return false;
        }
    }

    private void ensureStockRecord(Connection conn, int branchId, int drinkId) throws SQLException {
        // Check if stock record exists
        String checkSql = "SELECT COUNT(*) FROM stock WHERE branch_id = ? AND drink_id = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, branchId);
            checkStmt.setInt(2, drinkId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getInt(1) == 0) {
                // Create stock record with 0 quantity and default threshold
                String insertSql = "INSERT INTO stock (branch_id, drink_id, quantity, min_threshold) VALUES (?, ?, 0, 5)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setInt(1, branchId);
                    insertStmt.setInt(2, drinkId);
                    insertStmt.executeUpdate();
                }
            }
        }
    }

    // Method to get transfer history
    public void getTransferHistory(int branchId) {
        try (Connection conn = dbManager.getConnection()) {
            String sql = """
                SELECT st.*, 
                       fb.name as from_branch_name, tb.name as to_branch_name,
                       dn.name as drink_name, ds.size, d.brand
                FROM stock_transfers st
                JOIN branches fb ON st.from_branch_id = fb.branch_id
                JOIN branches tb ON st.to_branch_id = tb.branch_id
                JOIN drinks d ON st.drink_id = d.drink_id
                JOIN drink_names dn ON d.drink_name_id = dn.drink_name_id
                JOIN drink_sizes ds ON d.size_id = ds.size_id
                WHERE st.from_branch_id = ? OR st.to_branch_id = ?
                ORDER BY st.transfer_date DESC
                LIMIT 50
            """;

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, branchId);
                pstmt.setInt(2, branchId);
                ResultSet rs = pstmt.executeQuery();

                System.out.println("Transfer History for Branch ID: " + branchId);
                System.out.println("================================================");

                while (rs.next()) {
                    System.out.printf("Date: %s | From: %s | To: %s | Drink: %s %s %s | Quantity: %d%n",
                            rs.getTimestamp("transfer_date"),
                            rs.getString("from_branch_name"),
                            rs.getString("to_branch_name"),
                            rs.getString("brand"),
                            rs.getString("drink_name"),
                            rs.getString("size"),
                            rs.getInt("quantity"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving transfer history: " + e.getMessage());
        }
    }
}