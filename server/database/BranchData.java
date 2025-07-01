package server.database;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class BranchData {
    

    List<Map<String, Object>> listOfMaps = new ArrayList<>();
    
    BranchData(int ipAddress) {
        Map<String, Object> map1 = new HashMap<>();
        map1.put("id", 1);
        map1.put("Name", "Nairobi_Branch");
        map1.put("location","Nairobi");
        map1.put("Port", 5001);
        map1.put("IpAddress",ipAddress );
        
        Map<String, Object> map2 = new HashMap<>();
        map1.put("id", 2);
        map1.put("Name", "Mombasa_Branch");
        map1.put("location","Mombasa");
        map1.put("Port", 5002);
        map1.put("IpAddress",ipAddress );

        Map<String, Object> map3 = new HashMap<>();
        map1.put("id", 3);
        map1.put("Name", "Kisumu_Branch");
        map1.put("location","Kisumu");
        map1.put("Port", 5003);
        map1.put("IpAddress",ipAddress );
        
        Map<String, Object> map4 = new HashMap<>();
        map1.put("id", 4);
        map1.put("Name", "Nakuru_Branch");
        map1.put("location","Nakuru");
        map1.put("Port", 5004);
        map1.put("IpAddress",ipAddress);
        
        listOfMaps.add(map1);
        listOfMaps.add(map2);
        listOfMaps.add(map3);
        listOfMaps.add(map4);
    }
    public List<Map<String, Object>> getBranchData(){
        return listOfMaps;
    }

    
}


