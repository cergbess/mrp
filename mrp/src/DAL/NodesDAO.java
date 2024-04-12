package DAL;

import Models.Node;
import javafx.scene.control.Alert;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NodesDAO {

    private void showWarningAlert(String title, String headerText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    public void createNode(String nodeName, String nodeDescription) {
        if (nodeExists(nodeName)) {
            showWarningAlert("Предупреждение", "Дубликат узла", "Узел с именем " + nodeName + " уже существует.");
            return;
        }

        try (Connection connection = DatabaseConnector.getConnection()) {
            String query = "INSERT INTO Nodes(nodeName, nodeDescription) VALUES (?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, nodeName);
                statement.setString(2, nodeDescription);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean nodeExists(String nodeName) {
        try (Connection connection = DatabaseConnector.getConnection()) {
            String query = "SELECT COUNT(*) FROM Nodes WHERE nodeName = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, nodeName);
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

    public void updateNode(Node node) {
        try (Connection connection = DatabaseConnector.getConnection()) {
            String query = "SELECT nodeName FROM Nodes WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, node.getId());
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    String oldNodeName = resultSet.getString("nodeName");
                    if (!oldNodeName.equals(node.getNodeName()) && nodeExists(node.getNodeName())) {
                        showWarningAlert("Предупреждение", "Дубликат узла", "Узел с именем " + node.getNodeName() + " уже существует.");
                        return;
                    }
                }
            }
            query = "UPDATE Nodes SET nodeName = ?, nodeDescription = ? WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, node.getNodeName());
                statement.setString(2, node.getNodeDescription());
                statement.setInt(3, node.getId());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteNode(int nodeId) {
        try (Connection connection = DatabaseConnector.getConnection()) {
            String query = "DELETE FROM Nodes WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, nodeId);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Node> getAllNodes() {
        List<Node> nodes = new ArrayList<>();
        try (Connection connection = DatabaseConnector.getConnection()) {
            String query = "SELECT * FROM Nodes";
            try (PreparedStatement statement = connection.prepareStatement(query);
                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    nodes.add(new Node(
                            resultSet.getInt("id"),
                            resultSet.getString("nodeName"),
                            resultSet.getString("nodeDescription")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nodes;
    }

    public Node getNodeById(int id) {
        Node node = null;
        try (Connection connection = DatabaseConnector.getConnection()) {
            String query = "SELECT * FROM Nodes WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, id);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        node = new Node(
                                resultSet.getInt("id"),
                                resultSet.getString("nodeName"),
                                resultSet.getString("nodeDescription")
                        );
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return node;
    }
}
