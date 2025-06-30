package common.utils;
public class StockThresholdChecker {
    private static final int THRESHOLD = 10; // Example threshold

    public static boolean isStockLow(int stockLevel) {
        return stockLevel < THRESHOLD;
    }
}
