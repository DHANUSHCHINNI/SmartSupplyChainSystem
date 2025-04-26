package com.supplychain.models;

import com.supplychain.services.InventoryService;

import java.io.IOException;
import java.util.ArrayList;
import com.supplychain.services.ShipmentTrackerThread;
import com.supplychain.services.Trackable;
import com.supplychain.models.Order;
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
    public WarehouseManager(List<Supplier> suppliers, int minOrderQty, List<Product> products){
        inventoryService=new InventoryService(this);
        inventoryService.start();
        this.suppliers=suppliers;
        this.products=products;
        this.minOrderQty=minOrderQty;
        this.quantityMap = new HashMap<>();
        this.priceMap = new HashMap<>();
        Random r=new Random();
    }
    public List<Product> getProducts() {
        return products;
    }

    public InventoryService getInventoryService(){
        return this.inventoryService;
    }
    public void buyStock(Product product, int desiredQty) {
        String code = product.getCode();
        Integer current = quantityMap.getOrDefault(code, 0);

        if (current > minOrderQty) {
            System.out.println("Stock for " + code + " (" + current +
                    ") above threshold (" + minOrderQty + "). No reorder.");
            return;
        }

        Supplier bestSupplier = null;
        double bestCost = Double.MAX_VALUE;
        int bestQty = 0;

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
            Order order = new Order(bestSupplier,this);
            ShipmentTrackerThread shipmentTracker= new ShipmentTrackerThread(order.getOrderID(), 2, order);
            shipmentTracker.start();
            order.process();

        } catch (InterruptedException e) {
            System.err.println("Order for " + code + " interrupted.");
            return;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Update the stock after successful delivery
        quantityMap.put(code, current + bestQty);
    }

    public void sellToRetailer(Product product, int qty, Retailer retailer) throws InsufficientStockException {
        String code = product.getCode();
        int currentQty = quantityMap.getOrDefault(code, 0);

        if (currentQty < qty) {
            throw new InsufficientStockException("Not enough stock for product: " + code);
        }

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

   

}
