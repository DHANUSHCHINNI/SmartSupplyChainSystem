package com.supplychain.services;

import com.supplychain.models.Order;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ShipmentTrackerThread extends Thread {
    private final String shipmentId;
    private volatile boolean running = true;
    private int checkingFrequency;
    private Order order;

    public ShipmentTrackerThread(String shipmentId, int checkingFrequency, Order order) {
        super("Tracker-" + shipmentId);
        this.shipmentId = shipmentId;
        this.checkingFrequency = checkingFrequency;
        this.order = order;
        System.out.println("[ShipmentTracking] ShipmentTrackerThread created for shipmentId: " + shipmentId + " with checking frequency: " + checkingFrequency + " seconds");
    }

    @Override
    public void run() {
        System.out.println("[ShipmentTracking] Started tracker for " + shipmentId);
        while (running && !isInterrupted()) {
            try {
                // fetch without writing
                String status = fetchStatusFromCarrier(shipmentId);

                // write exactly once
                writeLogEntry(status);


                if ("Delivered".equals(status)) {
                    shutdown();
                    break;
                }

                System.out.println("[ShipmentTracking] Sleeping for "
                        + checkingFrequency + "s before next check.");
                Thread.sleep(checkingFrequency * 1000);
            } catch (InterruptedException ie) {
                System.out.println("[ShipmentTracking] Interrupted; stopping tracker.");
                running = false;
            }
        }
    }


    private String fetchStatusFromCarrier(String shipmentId) {
        return order.getOrderStatus();
    }

    /**
     * Stop tracking this shipment.
     */
    public void shutdown() {
        System.out.println("[ShipmentTracking] Shutting down ShipmentTrackerThread for shipmentId: " + shipmentId);
        running = false;
        interrupt();
    }

    private void writeLogEntry(String status) {
        String timestamp = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS"));
        String line = String.format("%s | %s | %s%n", timestamp, order.getOrderID(), status);

        try (BufferedWriter w = new BufferedWriter(new FileWriter("shipments.log", true))) {
            w.write(line);
            System.out.println("[ShipmentTracking] Logged: " + line.trim());
        } catch (IOException e) {
            System.err.println("[ShipmentTracking] Failed to write log: " + e.getMessage());
        }
    }
}
