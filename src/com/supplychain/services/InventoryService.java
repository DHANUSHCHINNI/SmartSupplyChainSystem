package com.supplychain.services;

import com.supplychain.models.Product;
import com.supplychain.models.WarehouseManager;

import java.util.List;
import java.util.HashMap;


public class InventoryService extends Thread {
    private final WarehouseManager warehouseManager;
    private final int checkIntervalMillis = 5000; // check every 5 seconds
    private volatile boolean running = true;

    public InventoryService(WarehouseManager whm) {
        this.warehouseManager = whm;
    }

    public void stopThread(){
        this.running = false;
        interrupt();  // Interrupt the thread if it's sleeping or waiting
    }

    @Override
    public void run() {
        System.out.println("[InventoryService] Started inventory monitoring service.");

        // Loop to monitor inventory
        while (this.running) {
            try {
                // Check if products list is null or empty
                List<Product> products = warehouseManager.getProducts();
                System.out.println("Quantity for products "+ warehouseManager.quantityMap);

                if (products == null || products.isEmpty()) {
                    System.out.println("[InventoryService] No products to monitor. Waiting for products...");
                    Thread.sleep(10000);  // Wait for 10 seconds before checking again
                    continue;  // Continue the loop instead of returning
                }

                // Iterate through products and check stock levels
                for (Product product : products) {
                    String productCode = product.getCode();
                    Integer currentQty = warehouseManager.quantityMap.getOrDefault(productCode, 0);

                    // Log product check and stock levels
                    System.out.println("[InventoryService] Checking Product: " + product.getName() +
                            " | Code: " + productCode + " | Current Stock: " + currentQty);

                    if (currentQty < warehouseManager.getMinOrderQty()) {
                        // Log reordering action
                        System.out.println("[InventoryService] Stock for product " + product.getName() + " (" + productCode +
                                ") is below minimum threshold (" + warehouseManager.getMinOrderQty() + "). Reordering...");

                        // Place an order for the product
                        warehouseManager.buyStock(product, warehouseManager.getMinOrderQty());
                    }
                }

                // Sleep for the defined interval before re-checking
                Thread.sleep(checkIntervalMillis);  // 5 seconds

            } catch (InterruptedException e) {
                System.err.println("[InventoryService] Inventory monitoring interrupted.");
                break;  // Exit the loop if interrupted
            } catch (Exception e) {
                System.err.println("[InventoryService] Unexpected error: " + e.getMessage());
            }
        }
        System.out.println("[InventoryService] Inventory monitoring service stopped.");
    }
}
