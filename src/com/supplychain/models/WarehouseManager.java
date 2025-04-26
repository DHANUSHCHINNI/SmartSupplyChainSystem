package com.supplychain.models;

import com.supplychain.services.InventoryService;

import java.io.IOException;
import java.util.ArrayList;
import com.supplychain.services.ShipmentTrackerThread;
import com.supplychain.services.Trackable;
import com.supplychain.models.Order;
import com.supplychain.forecasts.DemandForecastService;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import com.supplychain.services.ShipmentTrackerThread;
import java.util.Map;
public class WarehouseManager extends User{
    private String whCode;
    private InventoryService inventoryService;
    private List<Supplier> suppliers;
    private int minOrderQty;
    private int MAX_QUANTITY;
    private List<Product> products;
    public HashMap<String, Integer> quantityMap; //code, quantity
    private Map<String, Double> priceMap;
    private DemandForecastService predictionModel;
    private HashMap<String, List<Integer>> demandHistory;
    public WarehouseManager(List<Supplier> suppliers, int minOrderQty, List<Product> products){
        this.predictionModel=new DemandForecastService(this);
        this.suppliers=suppliers;
        this.products=products;
        this.minOrderQty=minOrderQty;
        this.quantityMap = new HashMap<>();
        this.priceMap = new HashMap<>();
        Random r=new Random();
        this.demandHistory=new HashMap<String, List<Integer>>();
        for (Product p : products) {
            quantityMap.put(p.getCode(), 0); // Initialize each product with 0 quantity
            priceMap.put(p.getCode(), Double.MAX_VALUE); // Default price if not set
        }

        inventoryService=new InventoryService(this);
        inventoryService.start();
    }
    public List<Product> getProducts() {
        return products;
    }

    public InventoryService getInventoryService(){
        return this.inventoryService;
    }
    public void addQty(Product p, int q) {
        System.out.println("Adding quantity " + q + " to product " + p.getName());
        // Check if the product code already exists in the map
        if (this.quantityMap.containsKey(p.getCode())) {
            // If it exists, increment the existing quantity by q
            this.quantityMap.put(p.getCode(), this.quantityMap.get(p.getCode()) + q);
        } else {
            // If not, add the product code with the specified quantity
            this.quantityMap.put(p.getCode(), q);
        }
    }

    public void updateMinOrderQty() {
        List<Integer> allDemands = new ArrayList<>();

        for (List<Integer> demands : demandHistory.values()) {
            allDemands.addAll(demands);
        }

        if (allDemands.isEmpty()) {
            return; // or some default value if there's no demand history
        }

        int sum = 0;
        for (int demand : allDemands) {
            sum += demand;
        }

        this.minOrderQty = sum / allDemands.size(); // Integer division: average
        System.out.println("Updated Minimum Order quantity to: " + this.minOrderQty);
    }
    public void buyStock(Product product, int desiredQty) {
        String code = product.getCode();
        Integer current = quantityMap.getOrDefault(code, 0);

        if (current >= minOrderQty) {
            System.out.println("Stock for " + code + " (" + current +
                    ") above threshold (" + minOrderQty + "). No reorder.");
            return;
        }

        Supplier bestSupplier = null;
        double bestCost = Double.MAX_VALUE;
        int bestQty = predictionModel.predict(product);
        System.out.println("Placing order for predicted value: ");

        for (Supplier s : suppliers) {
            int supplierMax = s.getMaxOrderQuantity(code);
            int orderQty = Math.min(desiredQty, supplierMax);
            double price = s.getPrice(code);
            double cost = price * orderQty;
            if (cost < bestCost && orderQty > 0) {
                bestCost = cost;
                bestSupplier = s;
                bestQty = orderQty;
            }
        }

        if (bestSupplier == null) {
            System.err.println("No supplier available for " + code);
            return;
        }

        System.out.println("Ordering " + bestQty + " units of " + code + " from supplier " + bestSupplier.getName());

        try {

            // Create the order (this also handles sleep and shipment.log updates)
            Order order = new Order(bestSupplier,this, product, bestQty);

            ShipmentTrackerThread shipmentTracker= new ShipmentTrackerThread(order.getOrderID(), 2, order);
            shipmentTracker.start();
//            order.process();
            new Thread(() -> {
                try {
                    System.out.println("[ORDER THREAD] Order of " + product.getName() + " placed with Order ID:" +  order.getOrderID());
                    order.process();
                } catch (InterruptedException e) {
                    System.err.println("Order for " + product.getCode() + " interrupted.");
                }
            }).start();


        } catch (InterruptedException e) {
            System.err.println("Order for " + code + " interrupted.");
            e.printStackTrace();  // helpful for debugging
            return;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Update the stock after successful delivery
        quantityMap.put(code, current + bestQty);
    }

    public void updateDemandHistory(Product product, int qty){
        this.demandHistory.computeIfAbsent(product.getCode(), k -> new ArrayList<>()).add(qty);
        updateMinOrderQty();
    }
    public void sellToRetailer(Product product, int qty, Retailer retailer) throws InsufficientStockException {
        String code = product.getCode();
        this.updateDemandHistory(product, qty);

        int currentQty = quantityMap.getOrDefault(code, 0);


        if (currentQty < qty) {
            throw new InsufficientStockException("Not enough stock for product: " + code);
        }

        List<Integer> productHistory = demandHistory.get(code);


        quantityMap.put(code, currentQty - qty);
    }

    public String getWarehouseCode() {
        return this.whCode;
    }

    public static class InsufficientStockException extends Exception {
        public InsufficientStockException(String message) {
            super(message);
        }
    }

    public int getMinOrderQty() {
        return minOrderQty;
    }

    public int getMaxQuantity() {
        return MAX_QUANTITY;
    }

    public double getPrice(String productCode) {
        return priceMap.getOrDefault(productCode, Double.MAX_VALUE);
    }

    public HashMap<String, List<Integer>> getDemandHistory(){
        return this.demandHistory;
    }

    public List<Integer> getProductDemandHistory(Product p){
        List<Integer> productHistory=this.demandHistory.get(p.getCode());
        return productHistory;
    }
   

}
