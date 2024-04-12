package Models;

public class Edge {
    private int id;
    private int upperNodeName;
    private int lowerNodeName;
    private int weight;
    private String unitOfMeasurement;

    public Edge(int id, int upperNodeName, int lowerNodeName, int weight, String unitOfMeasurement) {
        this.id = id;
        this.upperNodeName = upperNodeName;
        this.lowerNodeName = lowerNodeName;
        this.weight = weight;
        this.unitOfMeasurement = unitOfMeasurement;
    }

    public Edge(int upperNodeName, int lowerNodeName, double weight, String someProperty) {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUpperNodeName() {
        return upperNodeName;
    }

    public void setUpperNodeName(int upperNodeName) {
        this.upperNodeName = upperNodeName;
    }

    public int getLowerNodeName() {
        return lowerNodeName;
    }

    public void setLowerNodeName(int lowerNodeName) {
        this.lowerNodeName = lowerNodeName;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String getUnitOfMeasurement() {
        return unitOfMeasurement;
    }

    public void setUnitOfMeasurement(String unitOfMeasurement) {
        this.unitOfMeasurement = unitOfMeasurement;
    }
}
