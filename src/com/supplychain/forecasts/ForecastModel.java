package com.supplychain.forecasts;

import com.supplychain.models.Product;

import java.util.List;
public interface ForecastModel {
    public int predict(Product p);
}

