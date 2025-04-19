package com.example.uts_a22202303006.product;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ProductResponse {
    @SerializedName("result") // Sesuaikan dengan nama JSON
    private List<Product> result;

    public List<Product> getResult() {
        return result;
    }
}
