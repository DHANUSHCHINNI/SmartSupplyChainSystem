package com.supplychain;

import com.supplychain.services.AuthService;
import com.supplychain.models.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.supplychain.services.DataVisualizationService;

import javax.xml.crypto.Data;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Smart Supply Chain System Starting...");
//
//        AuthService auth = new AuthService();
//        try {
//            auth.register("alice@example.com", "myp@ssw0rd");
//            boolean ok = auth.authenticate("alice@example.com", "myp@ssw0rd");
//            System.out.println("Login success? " + ok);  // prints true
//        } catch (AuthService.AuthenticationException e) {
//            System.err.println("Auth failed: " + e.getMessage());
//        }

        Product p1=new Product("P1", "Harpic", "Hygiene");
        Product p2=new Product("P2", "Toothpaste", "Hygiene");


        List<Product> productList1=new ArrayList<>();
        productList1.add(p1);
        productList1.add(p2);


        List<Product> productList2=new ArrayList<>();
        productList2.add(p1);


        Supplier s1= new Supplier("Supplier 1", 10, productList1);
        Supplier s2=new Supplier("Supplier 2", 12, productList2);

        s1.setProductPricing("P1", 35.0, 5);
        s1.setProductPricing("P2", 32.00, 7);

        s2.setProductPricing("P1", 37.0, 9);

        List<Supplier> suppliers=new ArrayList<>();
        suppliers.add(s1);
        suppliers.add(s2);
        WarehouseManager w1= new WarehouseManager(suppliers, 2, productList1);

        List<WarehouseManager> warehouseManagers=new ArrayList<>();
        warehouseManagers.add(w1);


        Thread.sleep(20000);
        Retailer r1= new Retailer(warehouseManagers, 6, productList1);


        r1.buyStock(p1, 2);
        r1.buyStock(p1, 2);

        r1.buyStock(p2, 4);
        r1.buyStock(p2,20);

        Thread.sleep(20000);
        r1.buyStock(p2,13);
        w1.getInventoryService().stopThread();
        HashMap<String, List<Integer>> dh=w1.getDemandHistory();
        System.out.println("Demand History for w1 = " + dh);

        DataVisualizationService d=new DataVisualizationService(warehouseManagers);

        d.visualizeDemand();

    }
}
