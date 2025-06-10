import java.sql.*;
import java.util.*;
import java.math.BigDecimal;

public class DBManager {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/drinksalesdb";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "password";
    private static DBManager instance;

    private DBManager() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            initializeDatabase();
        } catch (ClassNotFoundException e) {
            System.err.println("Driver not found: " + e.getMessage());
        }
    }

    public static synchronized DBManager getInstance() {
        if (instance == null) {
            instance = new DBManager();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private void initializeDatabase() {
        try (Connection conn = getConnection()) {
            insertSampleDrinks(conn);
            insertSampleCustomers(conn);
        } catch (SQLException e) {
            System.err.println("Initialization error: " + e.getMessage());
        }
    }

    private void insertSampleDrinks(Connection conn) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM drinks";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(checkSql)) {
            if (rs.next() && rs.getInt(1) > 0) return;
        }

        String sql = "INSERT INTO drinks (drink_name_id, size_id, brand, price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            Object[][] drinks = {
                    {1, 2, "Coca Cola", 50.0},
                    {4, 2, "Pepsi", 50.0},
                    {2, 2, "Fanta", 45.0},
                    {3, 2, "Sprite", 45.0},
                    {1, 1, "Coca Cola", 35.0},
                    {2, 3, "Fanta", 80.0},
                    {3, 4, "Sprite", 120.0}
            };

            for (Object[] drink : drinks) {
                pstmt.setInt(1, (Integer) drink[0]);
                pstmt.setInt(2, (Integer) drink[1]);
                pstmt.setString(3, (String) drink[2]);
                pstmt.setBigDecimal(4, BigDecimal.valueOf((Double) drink[3]));
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    private void insertSampleCustomers(Connection conn) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM customers";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(checkSql)) {
            if (rs.next() && rs.getInt(1) > 0) return;
        }

        String sql = "INSERT INTO customers (full_name, email, phone_number) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String[][] customers = {
                    {"John Doe", "john.doe@email.com", "0712345678"},
                    {"Jane Smith", "jane.smith@email.com", "0723456789"},
                    {"Bob Johnson", "bob.johnson@email.com", "0734567890"},
                    {"Alice Brown", "alice.brown@email.com", "0745678901"}
            };

            for (String[] cust : customers) {
                pstmt.setString(1, cust[0]);
                pstmt.setString(2, cust[1]);
                pstmt.setString(3, cust[2]);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    public int createCustomer(String fullName, String email, String phoneNumber) throws SQLException {
        String sql = "INSERT INTO customers (full_name, email, phone_number) VALUES (?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, fullName);
            pstmt.setString(2, email);
            pstmt.setString(3, phoneNumber);
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            throw new SQLException("Customer creation failed");
        }
    }

    public List<Map<String, Object>> getAllCustomers() throws SQLException {
        String sql = "SELECT * FROM customers ORDER BY full_name";
        List<Map<String, Object>> customers = new ArrayList<>();
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> cust = new HashMap<>();
                cust.put("customer_id", rs.getInt("customer_id"));
                cust.put("full_name", rs.getString("full_name"));
                cust.put("email", rs.getString("email"));
                cust.put("phone_number", rs.getString("phone_number"));
                customers.add(cust);
            }
        }
        return customers;
    }

    public List<Map<String, Object>> getAllBranches() throws SQLException {
        String sql = "SELECT * FROM branches ORDER BY name";
        List<Map<String, Object>> branches = new ArrayList<>();
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> branch = new HashMap<>();
                branch.put("branch_id", rs.getInt("branch_id"));
                branch.put("name", rs.getString("name"));
                branch.put("location", rs.getString("location"));
                branch.put("is_headquarter", rs.getBoolean("is_headquarter"));
                branches.add(branch);
            }
        }
        return branches;
    }

    // Added missing methods that StockManager expects
    public List<Map<String, Object>> getStockByBranch(int branchId) throws SQLException {
        String sql = """
            SELECT s.stock_id, s.quantity, s.min_threshold, s.branch_id, s.drink_id,
                   b.name as branch_name, b.location,
                   dn.name as drink_name, ds.size, d.brand, d.price
            FROM stock s
            JOIN branches b ON s.branch_id = b.branch_id
            JOIN drinks d ON s.drink_id = d.drink_id
            JOIN drink_names dn ON d.drink_name_id = dn.drink_name_id
            JOIN drink_sizes ds ON d.size_id = ds.size_id
            WHERE s.branch_id = ?
            ORDER BY dn.name, ds.size
        """;

        List<Map<String, Object>> stockItems = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, branchId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> stock = new HashMap<>();
                stock.put("stock_id", rs.getInt("stock_id"));
                stock.put("quantity", rs.getInt("quantity"));
                stock.put("min_threshold", rs.getInt("min_threshold"));
                stock.put("branch_id", rs.getInt("branch_id"));
                stock.put("drink_id", rs.getInt("drink_id"));
                stock.put("branch_name", rs.getString("branch_name"));
                stock.put("location", rs.getString("location"));
                stock.put("drink_name", rs.getString("drink_name"));
                stock.put("size", rs.getString("size"));
                stock.put("brand", rs.getString("brand"));
                stock.put("price", rs.getBigDecimal("price"));
                stockItems.add(stock);
            }
        }
        return stockItems;
    }

    public void addStock(int branchId, int drinkId, int initialQuantity, int minThreshold) throws SQLException {
        String sql = "INSERT INTO stock (branch_id, drink_id, quantity, min_threshold) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, branchId);
            pstmt.setInt(2, drinkId);
            pstmt.setInt(3, initialQuantity);
            pstmt.setInt(4, minThreshold);
            pstmt.executeUpdate();
        }
    }

    public void updateStock(int stockId, int newQuantity) throws SQLException {
        String sql = "UPDATE stock SET quantity = ? WHERE stock_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, newQuantity);
            pstmt.setInt(2, stockId);
            pstmt.executeUpdate();
        }
    }

    public List<Map<String, Object>> getLowStockAlerts() throws SQLException {
        String sql = """
            SELECT s.stock_id, s.quantity, s.min_threshold, s.branch_id, s.drink_id,
                   b.name as branch_name, dn.name as drink_name, ds.size, d.brand
            FROM stock s
            JOIN branches b ON s.branch_id = b.branch_id
            JOIN drinks d ON s.drink_id = d.drink_id
            JOIN drink_names dn ON d.drink_name_id = dn.drink_name_id
            JOIN drink_sizes ds ON d.size_id = ds.size_id
            WHERE s.quantity < s.min_threshold
            ORDER BY b.name, dn.name
        """;

        List<Map<String, Object>> lowStockItems = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Map<String, Object> item = new HashMap<>();
                item.put("stock_id", rs.getInt("stock_id"));
                item.put("quantity", rs.getInt("quantity"));
                item.put("min_threshold", rs.getInt("min_threshold"));
                item.put("branch_id", rs.getInt("branch_id"));
                item.put("drink_id", rs.getInt("drink_id"));
                item.put("branch_name", rs.getString("branch_name"));
                item.put("drink_name", rs.getString("drink_name"));
                item.put("size", rs.getString("size"));
                item.put("brand", rs.getString("brand"));
                lowStockItems.add(item);
            }
        }
        return lowStockItems;
    }

    // Helper method for getting drinks with details
    public List<Map<String, Object>> getAllDrinks() throws SQLException {
        String sql = """
            SELECT d.drink_id, d.brand, d.price,
                   dn.name as drink_name, ds.size
            FROM drinks d
            JOIN drink_names dn ON d.drink_name_id = dn.drink_name_id
            JOIN drink_sizes ds ON d.size_id = ds.size_id
            ORDER BY dn.name, ds.size
        """;

        List<Map<String, Object>> drinks = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Map<String, Object> drink = new HashMap<>();
                drink.put("drink_id", rs.getInt("drink_id"));
                drink.put("brand", rs.getString("brand"));
                drink.put("price", rs.getBigDecimal("price"));
                drink.put("drink_name", rs.getString("drink_name"));
                drink.put("size", rs.getString("size"));
                drinks.add(drink);
            }
        }
        return drinks;
    }
}