import java.io.Serializable;
public class Drink implements Serializable {
    private static final long serialVersionUID=1L;
    private String brand;
    private String name;
    private double price;
    private int stockQuantity;
    private String drinkId;

    public Drink(){
    }
    public Drink(String drinkId,String brand,String name,double price,int stockQuantity){
        this.drinkId=drinkId;
        this.brand=brand;
        this.name=name;
        this.price=price;
        this.stockQuantity=stockQuantity;
    }

    public String getDrinkId(){
        return drinkId;
    }
    public String getBrand(){
        return brand;
    }
    public String getName(){
        return name;
    }
    public double getPrice(){
        return price;
    }
    public int getStockQuantity(){
        return stockQuantity;
    }

    public void setDrinkId(String drinkId){
        this.drinkId=drinkId;
    }
    public void setBrand(String brand){
        this.brand=brand;
    }
    public void setName(String name){
        this.name=name;
    }
    public void setPrice(double price){
        this.price=price;
    }
    public void setStockQuantity(int stockQuantity){
        this.stockQuantity=stockQuantity;
    }

    //Business logic
   public boolean reduceStock(int quantity){
        if(stockQuantity>=quantity){
            stockQuantity-=quantity;
            return true;
        }
        return false;
   }
   public void addStock(int quantity){
        if(quantity>0){
            stockQuantity+=quantity;
        }
   }
   public boolean isStockBelowThreshold(int minThreshold){
        return stockQuantity<minThreshold;
   }
   public boolean hasEnoughstock(int requestedQuantity){
        return stockQuantity>=requestedQuantity;
   }
   public double Calculatecost(int quantity){
        return price*quantity;
   }

   public String toString(){
        return String.format("Drink{ID='%s',Brand='%s',Name='%s',Price=%.2f,Stock=%d}",
                            drinkId,brand,name,price,stockQuantity);
    }

   public boolean equals(Object obj){
       if(this==obj)return true;
       if(obj==null||getClass()!=obj.getClass())return false;

       Drink drink=(Drink) obj;
       return drinkId!=null?drinkId.equals(drink.drinkId):drink.drinkId==null;
       }

       public int hashCode(){
       return drinkId!=null?drinkId.hashCode():0;
       }
   }

