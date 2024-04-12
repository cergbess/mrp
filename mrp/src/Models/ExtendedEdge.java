package Models;

public class ExtendedEdge extends Edge {
    private String nodeName;
    private String nodeDescription;

    public ExtendedEdge(int upperNodeName, int lowerNodeName, int weight, String nodeName, String nodeDescription) {
        super(0, upperNodeName, lowerNodeName, weight, "");
        this.nodeName = nodeName;
        this.nodeDescription = nodeDescription;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getNodeDescription() {
        return nodeDescription;
    }

    public void setNodeDescription(String nodeDescription) {
        this.nodeDescription = nodeDescription;
    }
}
