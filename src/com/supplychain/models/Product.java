package com.supplychain.models;

public class Product {
    private  String code;
    private  String name;
    private  String category="";

    public Product(String code, String name, String category) {
        this.code = code;
        this.name = name;
        this.category = category;
    }

    public Product(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public Product(String barcodeData){
        //with the assumption that barcodeData will be of the form "CODE-NAME-CATEGORY"
        try {
            String[] parts = barcodeData.split("-");
            if (parts.length != 3) throw new IllegalArgumentException("Invalid barcode format");
            this.code = parts[0]; 
            this.name = parts[1]; 
            this.category = parts[2]; 
    } catch (Exception e) {
        throw new IllegalArgumentException("Failed to parse barcode: " + barcodeData, e);
    }
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }
}
