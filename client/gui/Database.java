import java.sql.*;
public class Database {
    private static final String URL = "jdbc:postgresql://localhost:5433/db_Drinks";
    private static final String USER = "postgres";
    private static final String PASSWORD = "101121";
    private Connection connection;
    
    public Database() {
        try {

            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to PostgreSQL database!");
        } catch (SQLException e) {
            System.out.println("Error " + e + "Occurred");
        }
    }
    public Connection getConnection() {
      return connection;
    }
    public void addinventory(String Item,int Quantity,int Price){

    }
    public boolean checkinventory(int Threshold){
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try{
            String sql = "SELECT COUNT(*) as low_stock_count FROM drinks WHERE qty < ?";
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, Threshold);
            
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                int lowStockCount = rs.getInt("low_stock_count");
                return lowStockCount == 0; // Returns true if no items below threshold
            }
            
            return false;

        }catch (SQLException e){
            System.err.println("Database error: " + e.getMessage());
            return false;
        }finally{
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        } 
    }
}
