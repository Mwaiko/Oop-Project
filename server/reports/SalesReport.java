package server.reports;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import DBManager;

import java.math.BigDecimal;

public class SalesReport {
    private DBManager dbManager;

    public SalesReport() {
        this.dbManager = DBManager.getInstance();
    }

    public void generateDailyReport(String date) {
        try (Connection conn = dbManager.getConnection()) {
            String sql = """
                SELECT d.drink_id, dn.name as drink_name, ds.size, d.brand,
                       SUM(oi.quantity) AS total_sold,
                       SUM(oi.total_price) AS total_revenue,
                       AVG(oi.unit_price) AS avg_price
                FROM orders o
                JOIN order_items oi ON o.order_id = oi.order_id
                JOIN drinks d ON oi.drink_id = d.drink_id
                JOIN drink_names dn ON d.drink_name_id = dn.drink_name_id
                JOIN drink_sizes ds ON d.size_id = ds.size_id
                WHERE DATE(o.order_date) = ?
                GROUP BY d.drink_id, dn.name, ds.size, d.brand
                ORDER BY total_sold DESC
            """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, date);
                ResultSet rs = stmt.executeQuery();

                System.out.println("Daily Sales Report for: " + date);
                System.out.println("=====================================");
                System.out.printf("%-30s %-10s %-15s %-10s%n", "Product", "Sold", "Revenue", "Avg Price");
                System.out.println("--------------------------------------------------------------");

                BigDecimal totalRevenue = BigDecimal.ZERO;
                int totalItemsSold = 0;

                while (rs.next()) {
                    String product = String.format("%s %s %s",
                            rs.getString("brand"),
                            rs.getString("drink_name"),
                            rs.getString("size"));

                    int sold = rs.getInt("total_sold");
                    BigDecimal revenue = rs.getBigDecimal("total_revenue");
                    BigDecimal avgPrice = rs.getBigDecimal("avg_price");

                    System.out.printf("%-30s %-10d $%-14.2f $%-9.2f%n",
                            product, sold, revenue, avgPrice);

                    totalRevenue = totalRevenue.add(revenue);
                    totalItemsSold += sold;
                }

                System.out.println("--------------------------------------------------------------");
                System.out.printf("TOTALS: %-19s %-10d $%-14.2f%n", "", totalItemsSold, totalRevenue);

                if (totalItemsSold == 0) {
                    System.out.println("No sales recorded for " + date);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error generating daily report: " + e.getMessage());
        }
    }

    public void generateBranchReport(int branchId, String fromDate, String toDate) {
        try (Connection conn = dbManager.getConnection()) {
            String sql = """
                SELECT b.name as branch_name, b.location,
                       COUNT(DISTINCT o.order_id) as total_orders,
                       SUM(oi.quantity) as total_items_sold,
                       SUM(oi.total_price) as total_revenue,
                       AVG(o.total_amount) as avg_order_value
                FROM orders o
                JOIN order_items oi ON o.order_id = oi.order_id
                JOIN branches b ON o.branch_id = b.branch_id
                WHERE o.branch_id = ? AND DATE(o.order_date) BETWEEN ? AND ?
                GROUP BY b.branch_id, b.name, b.location
            """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, branchId);
                stmt.setString(2, fromDate);
                stmt.setString(3, toDate);
                ResultSet rs = stmt.executeQuery();

                System.out.println("Branch Sales Report");
                System.out.println("Branch ID: " + branchId);
                System.out.println("Period: " + fromDate + " to " + toDate);
                System.out.println("==================================");

                if (rs.next()) {
                    System.out.println("Branch: " + rs.getString("branch_name"));
                    System.out.println("Location: " + rs.getString("location"));
                    System.out.println("Total Orders: " + rs.getInt("total_orders"));
                    System.out.println("Total Items Sold: " + rs.getInt("total_items_sold"));
                    System.out.println("Total Revenue: $" + rs.getBigDecimal("total_revenue"));
                    System.out.println("Average Order Value: $" + rs.getBigDecimal("avg_order_value"));
                } else {
                    System.out.println("No sales data found for the specified period");
                }
            }

            // Get top selling products for this branch
            getTopSellingProducts(conn, branchId, fromDate, toDate);

        } catch (SQLException e) {
            System.err.println("Error generating branch report: " + e.getMessage());
        }
    }

    public void generateProductReport(int drinkId, String fromDate, String toDate) {
        try (Connection conn = dbManager.getConnection()) {
            String sql = """
                SELECT dn.name as drink_name, ds.size, d.brand,
                       COUNT(DISTINCT o.order_id) as orders_count,
                       SUM(oi.quantity) as total_sold,
                       SUM(oi.total_price) as total_revenue,
                       AVG(oi.unit_price) as avg_price,
                       MIN(oi.unit_price) as min_price,
                       MAX(oi.unit_price) as max_price
                FROM orders o
                JOIN order_items oi ON o.order_id = oi.order_id
                JOIN drinks d ON oi.drink_id = d.drink_id
                JOIN drink_names dn ON d.drink_name_id = dn.drink_name_id
                JOIN drink_sizes ds ON d.size_id = ds.size_id
                WHERE oi.drink_id = ? AND DATE(o.order_date) BETWEEN ? AND ?
                GROUP BY d.drink_id, dn.name, ds.size, d.brand
            """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, drinkId);
                stmt.setString(2, fromDate);
                stmt.setString(3, toDate);
                ResultSet rs = stmt.executeQuery();

                System.out.println("Product Sales Report");
                System.out.println("Drink ID: " + drinkId);
                System.out.println("Period: " + fromDate + " to " + toDate);
                System.out.println("==================================");

                if (rs.next()) {
                    String product = String.format("%s %s %s",
                            rs.getString("brand"),
                            rs.getString("drink_name"),
                            rs.getString("size"));

                    System.out.println("Product: " + product);
                    System.out.println("Orders Count: " + rs.getInt("orders_count"));
                    System.out.println("Total Sold: " + rs.getInt("total_sold"));
                    System.out.println("Total Revenue: $" + rs.getBigDecimal("total_revenue"));
                    System.out.println("Average Price: $" + rs.getBigDecimal("avg_price"));
                    System.out.println("Price Range: $" + rs.getBigDecimal("min_price") +
                            " - $" + rs.getBigDecimal("max_price"));
                } else {
                    System.out.println("No sales data found for this product in the specified period");
                }
            }

            // Get branch-wise sales for this product
            getProductSalesByBranch(conn, drinkId, fromDate, toDate);

        } catch (SQLException e) {
            System.err.println("Error generating product report: " + e.getMessage());
        }
    }

    public void generateMonthlySummary(String year, String month) {
        try (Connection conn = dbManager.getConnection()) {
            String sql = """
                SELECT DATE(o.order_date) as sale_date,
                       COUNT(DISTINCT o.order_id) as orders_count,
                       SUM(oi.quantity) as items_sold,
                       SUM(oi.total_price) as daily_revenue
                FROM orders o
                JOIN order_items oi ON o.order_id = oi.order_id
                WHERE YEAR(o.order_date) = ? AND MONTH(o.order_date) = ?
                GROUP BY DATE(o.order_date)
                ORDER BY sale_date
            """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, year);
                stmt.setString(2, month);
                ResultSet rs = stmt.executeQuery();

                System.out.println("Monthly Sales Summary - " + year + "/" + month);
                System.out.println("==========================================");
                System.out.printf("%-12s %-8s %-10s %-12s%n", "Date", "Orders", "Items", "Revenue");
                System.out.println("----------------------------------------------");

                BigDecimal monthlyRevenue = BigDecimal.ZERO;
                int monthlyOrders = 0;
                int monthlyItems = 0;

                while (rs.next()) {
                    String date = rs.getString("sale_date");
                    int orders = rs.getInt("orders_count");
                    int items = rs.getInt("items_sold");
                    BigDecimal revenue = rs.getBigDecimal("daily_revenue");

                    System.out.printf("%-12s %-8d %-10d $%-11.2f%n", date, orders, items, revenue);

                    monthlyRevenue = monthlyRevenue.add(revenue);
                    monthlyOrders += orders;
                    monthlyItems += items;
                }

                System.out.println("----------------------------------------------");
                System.out.printf("%-12s %-8d %-10d $%-11.2f%n", "MONTHLY", monthlyOrders, monthlyItems, monthlyRevenue);

                if (monthlyOrders == 0) {
                    System.out.println("No sales recorded for " + year + "/" + month);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error generating monthly summary: " + e.getMessage());
        }
    }

    private void getTopSellingProducts(Connection conn, int branchId, String fromDate, String toDate) throws SQLException {
        String sql = """
            SELECT dn.name as drink_name, ds.size, d.brand,
                   SUM(oi.quantity) as total_sold,
                   SUM(oi.total_price) as revenue
            FROM orders o
            JOIN order_items oi ON o.order_id = oi.order_id
            JOIN drinks d ON oi.drink_id = d.drink_id
            JOIN drink_names dn ON d.drink_name_id = dn.drink_name_id
            JOIN drink_sizes ds ON d.size_id = ds.size_id
            WHERE o.branch_id = ? AND DATE(o.order_date) BETWEEN ? AND ?
            GROUP BY d.drink_id, dn.name, ds.size, d.brand
            ORDER BY total_sold DESC
            LIMIT 5
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, branchId);
            stmt.setString(2, fromDate);
            stmt.setString(3, toDate);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\nTop 5 Products:");
            System.out.println("===============");
            int rank = 1;
            while (rs.next()) {
                String product = String.format("%s %s %s",
                        rs.getString("brand"),
                        rs.getString("drink_name"),
                        rs.getString("size"));

                System.out.printf("%d. %s - Sold: %d, Revenue: $%.2f%n",
                        rank++, product, rs.getInt("total_sold"), rs.getBigDecimal("revenue"));
            }
        }
    }

    private void getProductSalesByBranch(Connection conn, int drinkId, String fromDate, String toDate) throws SQLException {
        String sql = """
            SELECT b.name as branch_name, b.location,
                   SUM(oi.quantity) as total_sold,
                   SUM(oi.total_price) as revenue
            FROM orders o
            JOIN order_items oi ON o.order_id = oi.order_id
            JOIN branches b ON o.branch_id = b.branch_id
            WHERE oi.drink_id = ? AND DATE(o.order_date) BETWEEN ? AND ?
            GROUP BY b.branch_id, b.name, b.location
            ORDER BY total_sold DESC
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, drinkId);
            stmt.setString(2, fromDate);
            stmt.setString(3, toDate);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\nSales by Branch:");
            System.out.println("================");
            while (rs.next()) {
                System.out.printf("%s (%s) - Sold: %d, Revenue: $%.2f%n",
                        rs.getString("branch_name"),
                        rs.getString("location"),
                        rs.getInt("total_sold"),
                        rs.getBigDecimal("revenue"));
            }
        }
    }
}