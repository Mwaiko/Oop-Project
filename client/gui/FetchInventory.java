import java.util.*;

public class FetchInventory {
    ArrayList<Map<String, String>> inventory = new ArrayList<Map<String, String>>();
    
    public ArrayList<Map<String, String>> get(){
        System.out.println("The Inventory was fetched");
        return inventory;
    }
    
    
    
}