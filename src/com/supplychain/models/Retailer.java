package com.supplychain.models;
import java.util.ArrayList;

import com.supplychain.services.InventoryService;
import com.supplychain.services.ShipmentTrackerThread;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Retailer extends User{
    private String retailerId;
    private List<WarehouseManager> warehouseManagers;
    private List<Product> products;
    public HashMap<String, Integer> quantityMap;
    private int minOrderQty;

     public Retailer(List<WarehouseManager> warehouseManagers, int minOrderQty, List<Product> products) {
        this.warehouseManagers = warehouseManagers;
        this.minOrderQty = minOrderQty;
        this.products = products;
        this.quantityMap = new HashMap<>();

    }

     


    public void buyStock(Product product, int desiredQty){
        String code = product.getCode();
        Integer current = quantityMap.getOrDefault(code, 0);

        if (current > minOrderQty) {
            System.out.println("Retailer stock for " + code + " (" + current +
                    ") above threshold (" + minOrderQty + "). No reorder.");
            return;
        }


         

        

        WarehouseManager bestWarehouseManager = null;
        double bestCost = Double.MAX_VALUE;
        int bestQty = 0;

        for (WarehouseManager wm : warehouseManagers) {
            int stockAvailable = wm.quantityMap.getOrDefault(code, 0);
            int orderQty = Math.min(desiredQty, stockAvailable);

            if (orderQty == 0) continue;

            double price = wm.getPrice(code);
            double cost = price * orderQty;

            if (cost < bestCost && orderQty > 0) {
                bestCost = cost;
                bestWarehouse = wm;
                bestQty = orderQty;
            }
        }

        if (bestWarehouse == null) {
            System.err.println("No warehouse has available stock for " + code);
            return;
        }

        try{
            bestWarehouse.sellToRetailer(product, bestQty, this);
            quantityMap.put(code, current + bestQty);
            System.out.println("Retailer ordering " + bestQty + " units of " + code + " from warehouse.");
        }catch(WarehouseManager.InsufficientStockException e){
            System.err.println("Failed to buy from warehouse: " + e.getMessage());
        }
    }


    public void sellToCustomer(Product product, int qty) throws InsufficientStockException {
        String code = product.getCode();
        int currentQty = quantityMap.getOrDefault(code, 0);

        if (currentQty < qty) {
            throw new InsufficientStockException("Retailer: Not enough stock for product: " + code);
        }

        quantityMap.put(code, currentQty - qty);
        System.out.println("Sold " + qty + " units of " + code + " to customer.");
    }


    public List<Product> getProducts() {
        return products;
    }


    public static class InsufficientStockException extends Exception {
        public InsufficientStockException(String message) {
            super(message);
        }
    }

    public int getMinOrderQty() {
        return minOrderQty;
    }

    public List<WarehouseManager> getWarehouseManagers() {
        return warehouseManagers;
    }

    //more flexible than array, can dynamically add/remove items
    private ArrayList<Product> scannerProducts = new ArrayList<>();

    public class BarcodeScanner{
        //barCodeData is received a string. The sensor of the barcode scanner converts the dashes, into analog data, which is then converted to barCode data in a string format.
        public void scanBarcode(String barcodeData){
            //Simulating a scan
            System.out.println("Scanning barcode: " + barcodeData);
            Product newScan = new Product(barcodeData);
            scannerProducts.add(newScan);
            System.out.println("Product [" + barcodeData + "] added to retailer's stock.");
        }

        public void scanBarcode(String... barcodes) {
            for (String code : barcodes) {
                scanBarcode(code); // Calling the single barcode method
            }
        }
    }

    public ArrayList<Product> getScannedProducts() {
        return scannerProducts;
    }

}
