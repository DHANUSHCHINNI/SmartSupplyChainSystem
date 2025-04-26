package com.supplychain.models;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.Random;

public class Order {
    Supplier supplier;
    WarehouseManager warehouseManager;
    Product product;
    int quantity;
    Random r=new Random();
    private final String orderID = String.valueOf(r.nextInt(51407 - 32104 + 1) + 32104);
    String orderStatus;
    public Order(Supplier supplier, WarehouseManager warehouseManager, Product product, int quantity) throws InterruptedException, IOException {
        this.supplier = supplier;
        this.orderStatus = "Created";
        this.warehouseManager = warehouseManager;
        this.product=product;
        this.quantity=quantity;

    }
    public void process() throws InterruptedException {
        this.orderStatus = "In Progress";  // Set to "In Progress" first


        int acceptanceTime = supplier.getAcceptanceTime();
        System.out.println("[Order] Sleep for acceptanceTime: " + acceptanceTime);
        Thread.sleep(acceptanceTime);  // Sleep for acceptanceTime
        this.orderStatus = "In Transit";  // Update to "In Transit"


        int deliveryTime = supplier.getDeliveryTime();
        System.out.println("[Order] Sleep for deliveryTime: " + deliveryTime);
        Thread.sleep(deliveryTime);  // Sleep for deliveryTime
        this.orderStatus = "Delivered";  // Update to "Delivered"
        warehouseManager.addQty(this.product, this.quantity);

    }
    public String getOrderID() {
        return this.orderID;
    }

    public String getOrderStatus() {
        return this.orderStatus;
    }
}
