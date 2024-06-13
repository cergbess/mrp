package Models;

import java.time.LocalDate;

public class Warehouse {
    private int id;
    private int nodeId;
    private int receivedQuantity;
    private int shippedQuantity;
    private LocalDate date;

    public Warehouse(int id, int nodeId, int receivedQuantity, int shippedQuantity, LocalDate date) {
        this.id = id;
        this.nodeId = nodeId;
        this.receivedQuantity = receivedQuantity;
        this.shippedQuantity = shippedQuantity;
        this.date = date;
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

    public int getReceivedQuantity() {
        return receivedQuantity;
    }

    public void setReceivedQuantity(int receivedQuantity) {
        this.receivedQuantity = receivedQuantity;
    }

    public int getShippedQuantity() {
        return shippedQuantity;
    }

    public void setShippedQuantity(int shippedQuantity) {
        this.shippedQuantity = shippedQuantity;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
