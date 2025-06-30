package common.utils;
import java.util.List;

public class ReportFormatter {
    public static String formatSalesReport(List<String> salesData) {
        StringBuilder report = new StringBuilder();
        report.append("Sales Report:\n");
        for (String data : salesData) {
            report.append(data).append("\n");
        }
        return report.toString();
    }

    public static String formatOrderReport(List<String> orderData) {
        StringBuilder report = new StringBuilder();
        report.append("Order Report:\n");
        for (String data : orderData) {
            report.append(data).append("\n");
        }
        return report.toString();
    }
}
