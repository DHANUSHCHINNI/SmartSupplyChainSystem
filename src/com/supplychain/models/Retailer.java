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
    private int minOrderQty;

     public Retailer(List<WarehouseManager> warehouseManagers, int minOrderQty, List<Product> products) {
        this.warehouseManagers = warehouseManagers;
        this.minOrderQty = minOrderQty;


    }

     


    public boolean buyStock(Product product, int desiredQty){
        String code = product.getCode();
        System.out.println("INSIDE RETAILER BUY STOCK");
//        Integer current = quantityMap.get(code);

//        if (current > minOrderQty) {
//            System.out.println("Retailer stock for " + code + " (" + current +
//                    ") above threshold (" + minOrderQty + "). No reorder.");
//            return false;
//        }

        WarehouseManager bestWarehouse = this.warehouseManagers.get(0);
        double bestCost = Double.MAX_VALUE;



        for (WarehouseManager wm : warehouseManagers) {
            int stockAvailable = wm.quantityMap.getOrDefault(code, 0);
            if(stockAvailable < desiredQty){
                wm.updateDemandHistory(product, desiredQty);
                return false;
            }
            desiredQty = Math.min(desiredQty, stockAvailable);

            if (desiredQty == 0) continue;

            double price = wm.getPrice(code);
            double cost = price * desiredQty;

            if (cost <= bestCost && desiredQty > 0) {
                bestCost = cost;
                bestWarehouse = wm;

            }
        }

        if (bestWarehouse == null) {
            System.err.println("No warehouse has available stock for " + code);
            return false;
        }

        try{
            bestWarehouse.sellToRetailer(product, desiredQty, this);
//            quantityMap.put(code, current + bestQty);
            System.out.println("Retailer ordering " + desiredQty + " units of " + code + " from warehouse.");
            return true;
        }catch(WarehouseManager.InsufficientStockException e){
            System.err.println("Failed to buy from warehouse: " + e.getMessage());
        }
        return true;
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
