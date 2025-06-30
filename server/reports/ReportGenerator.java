package server.reports;

import common.models.Branch;
import common.models.Order;
import server.database.DatabaseManager;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class ReportGenerator {
    private DatabaseManager dbManager;
    
    public ReportGenerator(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }
    
    public SalesReport generateBranchSalesReport(String branchName) throws SQLException {
        List<Order> branchOrders = dbManager.getOrdersByBranch(branchName);
        BigDecimal totalSales = dbManager.getTotalSalesByBranch(branchName);
        
        SalesReport report = new SalesReport();
        report.setBranchName(branchName);
        report.setTotalSales(totalSales);
        report.setOrders(branchOrders);
        
        return report;
    }
    
    public CustomerOrdersReport generateCustomerOrdersReport() throws SQLException{
        List<Order> allOrders = dbManager.getAllOrders();
        Map<Integer, List<Order>> ordersByCustomer = allOrders.stream()
                .collect(Collectors.groupingBy(o -> o.getCustomer().getId()));
        
        CustomerOrdersReport report = new CustomerOrdersReport();
        report.setOrdersByCustomer(ordersByCustomer);
        
        return report;
    }
    
    public TotalSalesReport generateTotalSalesReport() throws SQLException  {
        TotalSalesReport report = new TotalSalesReport();
        
        // Get all branches
        List<Branch> branches = dbManager.getAllBranches();
        
        // Calculate sales for each branch
        Map<String, BigDecimal> salesByBranch = new HashMap<>();
        for (Branch branch : branches) {
            BigDecimal branchSales = dbManager.getTotalSalesByBranch(branch.getLocation());
            salesByBranch.put(branch.getLocation(), branchSales);
        }
        
        report.setSalesByBranch(salesByBranch);
        report.setTotalSales(dbManager.getTotalSales());
        
        return report;
    }
    
    // Report data classes
    public static class SalesReport implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        
        private String branchName;
        private BigDecimal totalSales;
        private List<Order> orders;
        
        // Getters and setters
        public String getBranchName() { return branchName; }
        public void setBranchName(String branchName) { this.branchName = branchName; }
        
        public BigDecimal getTotalSales() { return totalSales; }
        public void setTotalSales(BigDecimal totalSales) { this.totalSales = totalSales; }
        
        public List<Order> getOrders() { return orders; }
        public void setOrders(List<Order> orders) { this.orders = orders; }
    }
    
    public static class CustomerOrdersReport implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        
        private Map<Integer, List<Order>> ordersByCustomer;
        
        // Getters and setters
        public Map<Integer, List<Order>> getOrdersByCustomer() { return ordersByCustomer; }
        public void setOrdersByCustomer(Map<Integer, List<Order>> ordersByCustomer) { 
            this.ordersByCustomer = ordersByCustomer; 
        }
    }
    
    public static class TotalSalesReport implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        
        private Map<String, BigDecimal> salesByBranch;
        private BigDecimal totalSales;
        
        // Getters and setters
        public Map<String, BigDecimal> getSalesByBranch() { return salesByBranch; }
        public void setSalesByBranch(Map<String, BigDecimal> salesByBranch) { 
            this.salesByBranch = salesByBranch; 
        }
        
        public BigDecimal getTotalSales() { return totalSales; }
        public void setTotalSales(BigDecimal totalSales) { this.totalSales = totalSales; }
    }
}