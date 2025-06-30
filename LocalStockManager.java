import java.util.HashMap;
import java.util.Map;

public class LocalStockManager {
    private Map<String, Integer> localStock;

    public LocalStockManager() {
        localStock = new HashMap<>();
    }

    public void updateStock(String drinkName, int quantity) {
        localStock.put(drinkName, localStock.getOrDefault(drinkName, 0) + quantity);
    }

    public int getStockLevel(String drinkName) {
        return localStock.getOrDefault(drinkName, 0);
    }

    public Map<String, Integer> getAllStockLevels() {
        return localStock;
    }
}
