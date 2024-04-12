package DAL;

import Models.Edge;

import java.util.ArrayList;
import java.util.List;

public class PathInfo {
    private List<Edge> edges;
    public int totalWeight;

    public PathInfo() {
        this.edges = new ArrayList<>();
        this.totalWeight = 0;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public void addEdge(Edge edge) {
        this.edges.add(edge);
        this.totalWeight += edge.getWeight();
    }

    public int getTotalWeight() {
        return totalWeight;
    }
}
