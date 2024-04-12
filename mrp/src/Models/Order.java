package Models;

import java.time.LocalDate;

public class Order {
    private int id;
    private int nodeId;
    private LocalDate orderDate;
    private int quantityOrdered;
    private String description;
    private String supplier;
    private double price;

    public Order(int id, int nodeId, LocalDate orderDate, int quantityOrdered, String description, String supplier, double price) {
        this.id = id;
        this.nodeId = nodeId;
        this.orderDate = orderDate;
        this.quantityOrdered = quantityOrdered;
        this.description = description;
        this.supplier = supplier;
        this.price = price;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public int getQuantityOrdered() {
        return quantityOrdered;
    }

    public void setQuantityOrdered(int quantityOrdered) {
        this.quantityOrdered = quantityOrdered;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}

