package com.supplychain.services;

import com.supplychain.models.Product;
import com.supplychain.models.WarehouseManager;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import java.util.HashMap;
import java.util.List;

public class DataVisualizationService {
    private List<WarehouseManager> warehouseManagers;

    public DataVisualizationService(List<WarehouseManager> warehouseManagers) {
        this.warehouseManagers = warehouseManagers;
    }

    public void visualizeDemand() {
        for (WarehouseManager whm : warehouseManagers) {
            HashMap<String, List<Integer>> demandHistory = whm.getDemandHistory();
            if (demandHistory == null || demandHistory.isEmpty()) {
                System.out.println("[DataVisualizationService] No demand history available for warehouse manager.");
                continue;
            }

            for (String productCode : demandHistory.keySet()) {
                List<Integer> history = demandHistory.get(productCode);
                if (history == null || history.isEmpty()) {
                    System.out.println("[DataVisualizationService] No demand data for product: " + productCode);
                    continue;
                }

                // Create dataset
                DefaultCategoryDataset dataset = new DefaultCategoryDataset();
                for (int i = 0; i < history.size(); i++) {
                    dataset.addValue(history.get(i), "Demand", "Order " + (i + 1));
                }

                // Create chart
                JFreeChart chart = ChartFactory.createLineChart(
                        "Demand History for " + productCode,  // Chart Title
                        "Day",                                // X-axis Label
                        "Quantity",                           // Y-axis Label
                        dataset                               // Dataset
                );

                // Create and show frame
                ChartFrame frame = new ChartFrame("Demand Chart - " + productCode, chart);
                frame.setSize(800, 600); // Better size for viewing
                frame.setVisible(true);
            }
        }
    }
}
