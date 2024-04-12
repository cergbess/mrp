package Models;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Node {
    private final IntegerProperty id;
    private final StringProperty nodeName;
    private final StringProperty nodeDescription;

    public Node(int id, String nodeName, String nodeDescription) {
        this.id = new SimpleIntegerProperty(id);
        this.nodeName = new SimpleStringProperty(nodeName);
        this.nodeDescription = new SimpleStringProperty(nodeDescription);
    }

    public int getId() {
        return id.get();
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public String getNodeName() {
        return nodeName.get();
    }

    public StringProperty nodeNameProperty() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName.set(nodeName);
    }

    public String getNodeDescription() {
        return nodeDescription.get();
    }

    public StringProperty nodeDescriptionProperty() {
        return nodeDescription;
    }

    public void setNodeDescription(String nodeDescription) {
        this.nodeDescription.set(nodeDescription);
    }
}
