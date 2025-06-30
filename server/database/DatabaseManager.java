package server.database;

import common.models.*;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/beveragedb";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "password";
    
    // Connection pool could be implemented here for production use
    
    public DatabaseManager() {
        // Initialize database connection
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
        }
    }
    
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
    
    // Stock operations (used by StockManager)
    public List<Map<String, Object>> getStockByBranch(int branchId) throws SQLException {
        List<Map<String, Object>> stockList = new ArrayList<>();
        String sql = """
            SELECT s.stock_id, s.branch_id, s.drink_id, s.quantity, s.min_threshold,
                   b.name as branch_name, dn.name as drink_name, ds.size, d.brand, d.price
            FROM stock s
            JOIN branches b ON s.branch_id = b.branch_id
            JOIN drinks d ON s.drink_id = d.drink_id
            JOIN drink_names dn ON d.drink_name_id = dn.drink_name_id
            JOIN drink_sizes ds ON d.size_id = ds.size_id
            WHERE s.branch_id = ?
            ORDER BY dn.name
        """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, branchId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> stock = new HashMap<>();
                stock.put("stock_id", rs.getInt("stock_id"));
                stock.put("branch_id", rs.getInt("branch_id"));
                stock.put("drink_id", rs.getInt("drink_id"));
                stock.put("quantity", rs.getInt("quantity"));
                stock.put("min_threshold", rs.getInt("min_threshold"));
                stock.put("branch_name", rs.getString("branch_name"));
                stock.put("drink_name", rs.getString("drink_name"));
                stock.put("size", rs.getString("size"));
                stock.put("brand", rs.getString("brand"));
                stock.put("price", rs.getBigDecimal("price"));
                stockList.add(stock);
            }
        }
        return stockList;
    }
    
    public void addStock(int branchId, int drinkId, int quantity, int minThreshold) throws SQLException {
        String sql = "INSERT INTO stock (branch_id, drink_id, quantity, min_threshold) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, branchId);
            stmt.setInt(2, drinkId);
            stmt.setInt(3, quantity);
            stmt.setInt(4, minThreshold);
            stmt.executeUpdate();
        }
    }
    
    public void updateStock(int stockId, int newQuantity) throws SQLException {
        String sql = "UPDATE stock SET quantity = ? WHERE stock_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newQuantity);
            stmt.setInt(2, stockId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No stock record found with ID: " + stockId);
            }
        }
    }
    
    // Drink operations
    public Drink addDrink(String name, String brand, String size, BigDecimal price) throws SQLException {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            
            // First, ensure drink_names entry exists
            int drinkNameId = getOrCreateDrinkName(conn, name);
            
            // Then, ensure drink_sizes entry exists
            int sizeId = getOrCreateDrinkSize(conn, size);
            
            // Finally, create the drink
            String sql = "INSERT INTO drinks (drink_name_id, size_id, brand, price) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, drinkNameId);
                stmt.setInt(2, sizeId);
                stmt.setString(3, brand);
                stmt.setBigDecimal(4, price);
                stmt.executeUpdate();
                
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int drinkId = generatedKeys.getInt(1);
                    conn.commit();
                    return new Drink(drinkId, name, brand, size, price);
                } else {
                    throw new SQLException("Creating drink failed, no ID obtained");
                }
            }
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Error rolling back transaction: " + rollbackEx.getMessage());
                }
            }
            throw e;
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
    
    private int getOrCreateDrinkName(Connection conn, String name) throws SQLException {
        // Check if name exists
        String checkSql = "SELECT drink_name_id FROM drink_names WHERE name = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, name);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("drink_name_id");
            }
        }
        
        // Create new name
        String insertSql = "INSERT INTO drink_names (name) VALUES (?)";
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            insertStmt.setString(1, name);
            insertStmt.executeUpdate();
            ResultSet generatedKeys = insertStmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            } else {
                throw new SQLException("Creating drink name failed, no ID obtained");
            }
        }
    }
    
    private int getOrCreateDrinkSize(Connection conn, String size) throws SQLException {
        // Check if size exists
        String checkSql = "SELECT size_id FROM drink_sizes WHERE size = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, size);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("size_id");
            }
        }
        
        // Create new size
        String insertSql = "INSERT INTO drink_sizes (size) VALUES (?)";
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            insertStmt.setString(1, size);
            insertStmt.executeUpdate();
            ResultSet generatedKeys = insertStmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            } else {
                throw new SQLException("Creating drink size failed, no ID obtained");
            }
        }
    }
    
    public Drink getDrink(int id) throws SQLException {
        String sql = """
            SELECT d.drink_id, dn.name as drink_name, d.brand, ds.size, d.price
            FROM drinks d
            JOIN drink_names dn ON d.drink_name_id = dn.drink_name_id
            JOIN drink_sizes ds ON d.size_id = ds.size_id
            WHERE d.drink_id = ?
        """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return new Drink(
                    rs.getInt("drink_id"),
                    rs.getString("drink_name"),
                    rs.getString("brand"),
                    rs.getString("size"),
                    rs.getBigDecimal("price")
                );
            }
            return null;
        }
    }
    
    public List<Drink> getAllDrinks() throws SQLException {
        List<Drink> drinks = new ArrayList<>();
        String sql = """
            SELECT d.drink_id, dn.name as drink_name, d.brand, ds.size, d.price
            FROM drinks d
            JOIN drink_names dn ON d.drink_name_id = dn.drink_name_id
            JOIN drink_sizes ds ON d.size_id = ds.size_id
            ORDER BY dn.name, d.brand, ds.size
        """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                drinks.add(new Drink(
                    rs.getInt("drink_id"),
                    rs.getString("drink_name"),
                    rs.getString("brand"),
                    rs.getString("size"),
                    rs.getBigDecimal("price")
                ));
            }
        }
        return drinks;
    }
    
    public void updateDrink(Drink drink) throws SQLException {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            
            // Get or create drink name and size IDs
            int drinkNameId = getOrCreateDrinkName(conn, drink.getName());
            int sizeId = getOrCreateDrinkSize(conn, drink.getSize());
            
            String sql = "UPDATE drinks SET drink_name_id = ?, size_id = ?, brand = ?, price = ? WHERE drink_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, drinkNameId);
                stmt.setInt(2, sizeId);
                stmt.setString(3, drink.getBrand());
                stmt.setBigDecimal(4, drink.getPrice());
                stmt.setInt(5, drink.getId());
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("No drink found with ID: " + drink.getId());
                }
                conn.commit();
            }
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Error rolling back transaction: " + rollbackEx.getMessage());
                }
            }
            throw e;
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
    
    public void deleteDrink(int id) throws SQLException {
        String sql = "DELETE FROM drinks WHERE drink_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No drink found with ID: " + id);
            }
        }
    }
    
    // Customer operations
    public Customer addCustomer(Customer customer) throws SQLException {
        String sql = "INSERT INTO customers (full_name, phone, email) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getPhone());
            stmt.setString(3, customer.getEmail());
            stmt.executeUpdate();
            
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                customer.setId(generatedKeys.getInt(1));
                return customer;
            } else {
                throw new SQLException("Creating customer failed, no ID obtained");
            }
        }
    }
    
    public Customer getCustomer(int id) throws SQLException {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return new Customer(
                    rs.getInt("customer_id"),
                    rs.getString("full_name"),
                    rs.getString("phone"),
                    rs.getString("email")
                );
            }
            return null;
        }
    }
    
    public List<Customer> getAllCustomers() throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers ORDER BY full_name";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                customers.add(new Customer(
                    rs.getInt("customer_id"),
                    rs.getString("full_name"),
                    rs.getString("phone"),
                    rs.getString("email")
                ));
            }
        }
        return customers;
    }
    
    public void updateCustomer(Customer customer) throws SQLException {
        String sql = "UPDATE customers SET full_name = ?, phone = ?, email = ? WHERE customer_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getPhone());
            stmt.setString(3, customer.getEmail());
            stmt.setInt(4, customer.getId());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No customer found with ID: " + customer.getId());
            }
        }
    }
    
    public void deleteCustomer(int id) throws SQLException {
        String sql = "DELETE FROM customers WHERE customer_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No customer found with ID: " + id);
            }
        }
    }
    
    // Branch operations
    public Branch addBranch(Branch branch) throws SQLException {
        String sql = "INSERT INTO branches (name, location, phone) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, branch.getName());
            stmt.setString(2, branch.getLocation());
            stmt.setString(3, branch.getPhone());
            stmt.executeUpdate();
            
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                branch.setId(generatedKeys.getInt(1));
                return branch;
            } else {
                throw new SQLException("Creating branch failed, no ID obtained");
            }
        }
    }
    
    public Branch getBranch(int id) throws SQLException {
        String sql = "SELECT * FROM branches WHERE branch_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return new Branch(
                    rs.getInt("branch_id"),
                    rs.getString("name"),
                    rs.getString("location"),
                    rs.getString("phone")
                );
            }
            return null;
        }
    }
    
    public Branch getBranchByName(String name) throws SQLException {
        String sql = "SELECT * FROM branches WHERE name = ? OR location = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, name);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return new Branch(
                    rs.getInt("branch_id"),
                    rs.getString("name"),
                    rs.getString("location"),
                    rs.getString("phone")
                );
            }
            return null;
        }
    }
    
    public List<Branch> getAllBranches() throws SQLException {
        List<Branch> branches = new ArrayList<>();
        String sql = "SELECT * FROM branches ORDER BY name";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                branches.add(new Branch(
                    rs.getInt("branch_id"),
                    rs.getString("name"),
                    rs.getString("location"),
                    rs.getString("phone")
                ));
            }
        }
        return branches;
    }
    
    public void updateBranch(Branch branch) throws SQLException {
        String sql = "UPDATE branches SET name = ?, location = ?, phone = ? WHERE branch_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, branch.getName());
            stmt.setString(2, branch.getLocation());
            stmt.setString(3, branch.getPhone());
            stmt.setInt(4, branch.getId());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No branch found with ID: " + branch.getId());
            }
        }
    }
    
    public void deleteBranch(int id) throws SQLException {
        String sql = "DELETE FROM branches WHERE branch_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No branch found with ID: " + id);
            }
        }
    }
    
    // Sales analytics methods
    public BigDecimal getTotalSalesByBranch(int branchId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE branch_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, branchId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getBigDecimal(1);
            }
            return BigDecimal.ZERO;
        }
    }
    
    public BigDecimal getTotalSales() throws SQLException {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM orders";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getBigDecimal(1);
            }
            return BigDecimal.ZERO;
        }
    }
    
    // Database initialization methods
    public void initializeSampleData() throws SQLException {
        initializeSampleCustomers();
        initializeSampleBranches();
        initializeSampleDrinks();
    }
    
    public void initializeSampleCustomers() throws SQLException {
        try (Connection conn = getConnection()) {
            // Check if customers already exist
            String checkSql = "SELECT COUNT(*) FROM customers";
            try (PreparedStatement stmt = conn.prepareStatement(checkSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return; // Customers already exist
                }
            }
            
            // Insert sample customers
            String insertSql = "INSERT INTO customers (full_name, phone, email) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setString(1, "John Doe");
                stmt.setString(2, "0722123456");
                stmt.setString(3, "john@example.com");
                stmt.addBatch();
                
                stmt.setString(1, "Jane Smith");
                stmt.setString(2, "0733987654");
                stmt.setString(3, "jane@example.com");
                stmt.addBatch();
                
                stmt.setString(1, "Alice Johnson");
                stmt.setString(2, "0711555666");
                stmt.setString(3, "alice@example.com");
                stmt.addBatch();
                
                stmt.executeBatch();
            }
        }
    }
    
    private void initializeSampleBranches() throws SQLException {
        try (Connection conn = getConnection()) {
            // Check if branches already exist
            String checkSql = "SELECT COUNT(*) FROM branches";
            try (PreparedStatement stmt = conn.prepareStatement(checkSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return; // Branches already exist
                }
            }
            
            // Insert sample branches
            String insertSql = "INSERT INTO branches (name, location, phone) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setString(1, "Main Branch");
                stmt.setString(2, "Downtown Nairobi");
                stmt.setString(3, "0700123456");
                stmt.addBatch();
                
                stmt.setString(1, "Westlands Branch");
                stmt.setString(2, "Westlands");
                stmt.setString(3, "0700123457");
                stmt.addBatch();
                
                stmt.setString(1, "Karen Branch");
                stmt.setString(2, "Karen");
                stmt.setString(3, "0700123458");
                stmt.addBatch();
                
                stmt.executeBatch();
            }
        }
    }
    
    private void initializeSampleDrinks() throws SQLException {
        // This would initialize sample drink names, sizes, and drinks
        // Implementation depends on your specific drink structure
    }
}