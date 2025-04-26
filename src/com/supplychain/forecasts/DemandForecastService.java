package com.supplychain.forecasts;

import com.supplychain.models.Product;
import com.supplychain.models.WarehouseManager;

import java.util.List;

public class DemandForecastService implements ForecastModel {
    WarehouseManager whm;

    public DemandForecastService(WarehouseManager whm) {
        this.whm = whm;
    }

    @Override
    public int predict(Product p) {
        List<Integer> productHistory = this.whm.getProductDemandHistory(p);

        // Ensure there is enough data to perform regression
        if (productHistory == null || productHistory.size() < 2) {
            return 0; // Not enough data to make a prediction
        }

        // Apply simple linear regression: y = a + b*x
        int n = productHistory.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;

        for (int i = 0; i < n; i++) {
            double x = i + 1; // time index starting from 1
            double y = productHistory.get(i);
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumXX += x * x;
        }

        double meanX = sumX / n;
        double meanY = sumY / n;

        double numerator = sumXY - n * meanX * meanY;
        double denominator = sumXX - n * meanX * meanX;

        double slope = numerator / denominator;
        double intercept = meanY - slope * meanX;

        // Predict next value (i.e., for x = n + 1)
        double nextX = n + 1;
        double prediction = intercept + slope * nextX;

        return (int) Math.round(prediction);
    }
}
