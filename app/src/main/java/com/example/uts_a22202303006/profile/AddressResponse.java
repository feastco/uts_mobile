package com.example.uts_a22202303006.profile;

import java.util.List;

public class AddressResponse {
    private int result;
    private String message;
    private List<ShippingAddress> addresses;

    // Getters and setters
    public int getResult() { return result; }
    public String getMessage() { return message; }
    public List<ShippingAddress> getAddresses() { return addresses; }
}
