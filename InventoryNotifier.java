import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InventoryNotifier {
    private DBManager dbManager;

    public InventoryNotifier() {
        this.dbManager = DBManager.getInstance();
    }

    public void checkAndNotify() {
        try (Connection conn = dbManager.getConnection()) {
            String sql = """
                SELECT s.stock_id, s.branch_id, s.drink_id, s.quantity, s.min_threshold,
                       b.name as branch_name, dn.name as drink_name, ds.size, d.brand
                FROM stock s
                JOIN branches b ON s.branch_id = b.branch_id
                JOIN drinks d ON s.drink_id = d.drink_id
                JOIN drink_names dn ON d.drink_name_id = dn.drink_name_id
                JOIN drink_sizes ds ON d.size_id = ds.size_id
                WHERE s.quantity < s.min_threshold
                ORDER BY b.name, dn.name
            """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();

                int alertCount = 0;
                while (rs.next()) {
                    int stockId = rs.getInt("stock_id");
                    int quantity = rs.getInt("quantity");
                    int minThreshold = rs.getInt("min_threshold");
                    String branchName = rs.getString("branch_name");
                    String drinkName = rs.getString("drink_name");
                    String size = rs.getString("size");
                    String brand = rs.getString("brand");

                    String message = String.format("Low stock alert: %s %s %s at %s - Current: %d, Minimum: %d",
                            brand, drinkName, size, branchName, quantity, minThreshold);

                    System.out.println("ALERT: " + message);
                    logAlert(conn, stockId, message);
                    alertCount++;
                }

                if (alertCount == 0) {
                    System.out.println("No low stock alerts at this time.");
                } else {
                    System.out.println("Total alerts generated: " + alertCount);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking inventory: " + e.getMessage());
        }
    }

    public void checkBranchInventory(int branchId) {
        try (Connection conn = dbManager.getConnection()) {
            String sql = """
                SELECT s.stock_id, s.drink_id, s.quantity, s.min_threshold,
                       b.name as branch_name, dn.name as drink_name, ds.size, d.brand
                FROM stock s
                JOIN branches b ON s.branch_id = b.branch_id
                JOIN drinks d ON s.drink_id = d.drink_id
                JOIN drink_names dn ON d.drink_name_id = dn.drink_name_id
                JOIN drink_sizes ds ON d.size_id = ds.size_id
                WHERE s.branch_id = ? AND s.quantity < s.min_threshold
                ORDER BY dn.name
            """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, branchId);
                ResultSet rs = stmt.executeQuery();

                int alertCount = 0;
                while (rs.next()) {
                    int stockId = rs.getInt("stock_id");
                    int quantity = rs.getInt("quantity");
                    int minThreshold = rs.getInt("min_threshold");
                    String branchName = rs.getString("branch_name");
                    String drinkName = rs.getString("drink_name");
                    String size = rs.getString("size");
                    String brand = rs.getString("brand");

                    String message = String.format("Low stock at %s: %s %s %s - Current: %d, Minimum: %d",
                            branchName, brand, drinkName, size, quantity, minThreshold);

                    System.out.println("BRANCH ALERT: " + message);
                    logAlert(conn, stockId, message);
                    alertCount++;
                }

                if (alertCount == 0) {
                    System.out.println("No low stock alerts for branch ID: " + branchId);
                } else {
                    System.out.println("Branch alerts generated: " + alertCount);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking branch inventory: " + e.getMessage());
        }
    }

    private void logAlert(Connection conn, int stockId, String message) throws SQLException {
        String sql = "INSERT INTO stock_alerts (stock_id, alert_date, message) VALUES (?, NOW(), ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, stockId);
            stmt.setString(2, message);
            stmt.executeUpdate();
        }
    }

    public void viewRecentAlerts(int limit) {
        try (Connection conn = dbManager.getConnection()) {
            String sql = """
                SELECT sa.alert_date, sa.message,
                       b.name as branch_name, dn.name as drink_name, ds.size, d.brand
                FROM stock_alerts sa
                JOIN stock s ON sa.stock_id = s.stock_id
                JOIN branches b ON s.branch_id = b.branch_id
                JOIN drinks d ON s.drink_id = d.drink_id
                JOIN drink_names dn ON d.drink_name_id = dn.drink_name_id
                JOIN drink_sizes ds ON d.size_id = ds.size_id
                ORDER BY sa.alert_date DESC
                LIMIT ?
            """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, limit);
                ResultSet rs = stmt.executeQuery();

                System.out.println("Recent Stock Alerts:");
                System.out.println("==================");

                while (rs.next()) {
                    System.out.printf("[%s] %s%n",
                            rs.getTimestamp("alert_date"),
                            rs.getString("message"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error viewing recent alerts: " + e.getMessage());
        }
    }

    // Method to clear old alerts (housekeeping)
    public void clearOldAlerts(int daysOld) {
        try (Connection conn = dbManager.getConnection()) {
            String sql = "DELETE FROM stock_alerts WHERE alert_date < DATE_SUB(NOW(), INTERVAL ? DAY)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, daysOld);
                int deletedCount = stmt.executeUpdate();
                System.out.println("Cleared " + deletedCount + " old alerts (older than " + daysOld + " days)");
            }
        } catch (SQLException e) {
            System.err.println("Error clearing old alerts: " + e.getMessage());
        }
    }
}