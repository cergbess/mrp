package DAL;

import Models.Edge;
import javafx.scene.control.Alert;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EdgesDAO {

    private void showAlert(Alert.AlertType alertType, String title, String headerText, String contentText) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    public void createEdge(int upperNodeName, int lowerNodeName, int weight, String unitOfMeasurement) {

        if (edgeExists(upperNodeName, lowerNodeName) || edgeExists(lowerNodeName, upperNodeName)) {
            showAlert(Alert.AlertType.WARNING, "Предупреждение", "Дубликат ребра",
                    "Ребро между вершинами " + upperNodeName + " и " + lowerNodeName + " уже существует.");
            return;
        }
        if (!nodeExists(upperNodeName) || !nodeExists(lowerNodeName)) {
            showAlert(Alert.AlertType.WARNING, "Ошибка", "Отсутствует вершина",
                    "Одна из вершин (" + upperNodeName + " или " + lowerNodeName + ") отсутствует");
            return;
        }
        if (weight <= 0) {
            showAlert(Alert.AlertType.WARNING, "Ошибка", "Ошибочное количество",
                    "Количество должно быть больше 0");
            return;
        }
        if (upperNodeName == lowerNodeName) {
            showAlert(Alert.AlertType.WARNING, "Предупреждение", "Цикл",
                    "Ребро не может быть петлей");
            return;
        }

        try (Connection connection = DatabaseConnector.getConnection()) {
            String query = "INSERT INTO Edges(upperNodeName, lowerNodeName, weight, unitOfMeasurement) VALUES (?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, upperNodeName);
                statement.setInt(2, lowerNodeName);
                statement.setInt(3, weight);
                statement.setString(4, unitOfMeasurement);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateEdge(Edge edge) {
        if (!edgeNodesUnchanged(edge) && (edgeExists(edge.getUpperNodeName(), edge.getLowerNodeName()) ||
                edgeExists(edge.getLowerNodeName(), edge.getUpperNodeName()))) {
            showAlert(Alert.AlertType.WARNING, "Предупреждение", "Дубликат ребра",
                    "Ребро между вершинами " + edge.getUpperNodeName() + " и " +
                            edge.getLowerNodeName() + " уже существует.");
            return;
        }

        if (!nodeExists(edge.getUpperNodeName()) || !nodeExists(edge.getLowerNodeName())) {
            showAlert(Alert.AlertType.WARNING, "Ошибка", "Отсутствует вершина",
                    "Одна из вершин (" + edge.getUpperNodeName() + " или " + edge.getLowerNodeName() + ") отсутствует");
            return;
        }
        if (edge.getWeight() <= 0) {
            showAlert(Alert.AlertType.WARNING, "Ошибка", "Ошибочное количество",
                    "Количество должно быть больше 0");
            return;
        }
        if (edge.getUpperNodeName() == edge.getLowerNodeName()) {
            showAlert(Alert.AlertType.WARNING, "Предупреждение", "Цикл",
                    "Ребро не может быть петлей");
            return;
        }

        try (Connection connection = DatabaseConnector.getConnection()) {
            String query = "UPDATE Edges SET upperNodeName = ?, lowerNodeName = ?, weight = ?, unitOfMeasurement = ? WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, edge.getUpperNodeName());
                statement.setInt(2, edge.getLowerNodeName());
                statement.setInt(3, edge.getWeight());
                statement.setString(4, edge.getUnitOfMeasurement());
                statement.setInt(5, edge.getId());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean edgeNodesUnchanged(Edge edge) {
        try (Connection connection = DatabaseConnector.getConnection()) {
            String query = "SELECT upperNodeName, lowerNodeName FROM Edges WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, edge.getId());
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        int storedUpperNode = resultSet.getInt("upperNodeName");
                        int storedLowerNode = resultSet.getInt("lowerNodeName");
                        return storedUpperNode == edge.getUpperNodeName() && storedLowerNode == edge.getLowerNodeName();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean edgeExists(int upperNodeName, int lowerNodeName) {
        try (Connection connection = DatabaseConnector.getConnection()) {
            String query = "SELECT COUNT(*) FROM Edges WHERE upperNodeName = ? AND lowerNodeName = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, upperNodeName);
                statement.setInt(2, lowerNodeName);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getInt(1) > 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean nodeExists(int id) {
        try (Connection connection = DatabaseConnector.getConnection()) {
            String query = "SELECT COUNT(*) FROM Nodes WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, id);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getInt(1) > 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void deleteEdge(int edgeId) {
        try (Connection connection = DatabaseConnector.getConnection()) {
            String query = "DELETE FROM Edges WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, edgeId);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Edge> getAllEdges() {
        List<Edge> edges = new ArrayList<>();
        try (Connection connection = DatabaseConnector.getConnection()) {
            String query = "SELECT * FROM Edges";
            try (PreparedStatement statement = connection.prepareStatement(query);
                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    edges.add(new Edge(
                            resultSet.getInt("id"),
                            resultSet.getInt("upperNodeName"),
                            resultSet.getInt("lowerNodeName"),
                            resultSet.getInt("weight"),
                            resultSet.getString("unitOfMeasurement")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return edges;
    }
    public List<Edge> getAllEdgesForNode(int node) {
        List<Edge> edges = new ArrayList<>();
        Set<SimplePair<Integer, Integer>> visitedEdges = new HashSet<>();
        getAllEdgesRecursive(node, edges, visitedEdges);
        return edges;
    }

    private PathInfo getAllEdgesRecursive(int currentNode, List<Edge> edges, Set<SimplePair<Integer, Integer>> visitedEdges) {
        PathInfo pathInfo = new PathInfo();
        String query = "SELECT * FROM Edges WHERE upperNodeName = ? OR lowerNodeName = ?";
        try (Connection connection = DatabaseConnector.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, currentNode);
            statement.setInt(2, currentNode);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int upperNodeName = resultSet.getInt("upperNodeName");
                    int lowerNodeName = resultSet.getInt("lowerNodeName");
                    SimplePair<Integer, Integer> edgePair = new SimplePair<>(upperNodeName, lowerNodeName);
                    if (!visitedEdges.contains(edgePair)) {
                        visitedEdges.add(edgePair);
                        Edge edge = new Edge(
                                resultSet.getInt("id"),
                                upperNodeName,
                                lowerNodeName,
                                resultSet.getInt("weight"),
                                resultSet.getString("unitOfMeasurement")
                        );
                        edges.add(edge);
                        pathInfo.addEdge(edge);
                        int nextNode;
                        if (upperNodeName == currentNode) {
                            nextNode = lowerNodeName;
                        } else {
                            nextNode = upperNodeName;
                        }
                        PathInfo nextPath = getAllEdgesRecursive(nextNode, edges, visitedEdges);
                        pathInfo.getEdges().addAll(nextPath.getEdges());
                        pathInfo.totalWeight += nextPath.getTotalWeight();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pathInfo;
    }
}