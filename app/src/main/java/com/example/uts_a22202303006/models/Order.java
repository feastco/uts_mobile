package com.example.uts_a22202303006.models;

import java.util.List;

public class Order {
    private int id;
    private String orderNumber;
    private String orderDate;
    private String updateDate;
    private Status status;
    private Shipping shipping;
    private Payment payment;
    private List<OrderItem> items;
    
    // Default constructor needed for manual population
    public Order() {}
    
    // Static nested classes to match API structure
    public static class Status {
        private String order;
        private String payment;
        
        public String getOrder() {
            return order;
        }
        
        public void setOrder(String order) {
            this.order = order;
        }
        
        public String getPayment() {
            return payment;
        }
        
        public void setPayment(String payment) {
            this.payment = payment;
        }
    }
    
    public static class Shipping {
        private String name;
        private String phone;
        private String address;
        private String province;
        private String city;
        private String postalCode;
        private String courier;
        private String service;
        private double cost;
        private int totalWeight; // Added total weight field
        
        public String getFormattedAddress() {
            return address + ", " + city + ", " + province + " " + postalCode;
        }
        
        // Getters and Setters
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getPhone() {
            return phone;
        }
        
        public void setPhone(String phone) {
            this.phone = phone;
        }
        
        public String getAddress() {
            return address;
        }
        
        public void setAddress(String address) {
            this.address = address;
        }
        
        public String getProvince() {
            return province;
        }
        
        public void setProvince(String province) {
            this.province = province;
        }
        
        public String getCity() {
            return city;
        }
        
        public void setCity(String city) {
            this.city = city;
        }
        
        public String getPostalCode() {
            return postalCode;
        }
        
        public void setPostalCode(String postalCode) {
            this.postalCode = postalCode;
        }
        
        public String getCourier() {
            return courier;
        }
        
        public void setCourier(String courier) {
            this.courier = courier;
        }
        
        public String getService() {
            return service;
        }
        
        public void setService(String service) {
            this.service = service;
        }
        
        public double getCost() {
            return cost;
        }
        
        public void setCost(double cost) {
            this.cost = cost;
        }
        
        public int getTotalWeight() {
            return totalWeight;
        }
        
        public void setTotalWeight(int totalWeight) {
            this.totalWeight = totalWeight;
        }
    }
    
    public static class Payment {
        private double subtotal;
        private double shipping;
        private double total;
        
        // Getters and Setters
        public double getSubtotal() {
            return subtotal;
        }
        
        public void setSubtotal(double subtotal) {
            this.subtotal = subtotal;
        }
        
        public double getShipping() {
            return shipping;
        }
        
        public void setShipping(double shipping) {
            this.shipping = shipping;
        }
        
        public double getTotal() {
            return total;
        }
        
        public void setTotal(double total) {
            this.total = total;
        }
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }
    
    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getOrderDate() {
        return orderDate;
    }
    
    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }
    
    public String getUpdateDate() {
        return updateDate;
    }
    
    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }

    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }

    public Shipping getShipping() {
        return shipping;
    }
    
    public void setShipping(Shipping shipping) {
        this.shipping = shipping;
    }

    public Payment getPayment() {
        return payment;
    }
    
    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public List<OrderItem> getItems() {
        return items;
    }
    
    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
}
