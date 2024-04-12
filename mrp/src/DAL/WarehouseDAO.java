package DAL;

import Models.Warehouse;
import javafx.scene.control.Alert;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class WarehouseDAO {

    private void showAlert(Alert.AlertType alertType, String title, String headerText, String contentText) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    public void createWarehouseEntry(int nodeId, LocalDate date, int receivedQuantity,
                                     int shippedQuantity) {
        try (Connection connection = DatabaseConnector.getConnection()) {
            if (!nodeExists(nodeId)) {
                showAlert(Alert.AlertType.WARNING, "Предупреждение", "Узел не найден",
                        "Узел с id " + nodeId + " не существует.");
                return;
            }

            String query = "INSERT INTO Warehouse(nodeId, date, receivedQuantity, shippedQuantity) " +
                    "VALUES (?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, nodeId);
                statement.setDate(2, Date.valueOf(date));
                statement.setInt(3, receivedQuantity);
                statement.setInt(4, shippedQuantity);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Warehouse> getAllWarehouseEntries() {
        List<Warehouse> warehouseEntries = new ArrayList<>();
        try (Connection connection = DatabaseConnector.getConnection()) {
            String query = "SELECT * FROM Warehouse";
            try (PreparedStatement statement = connection.prepareStatement(query);
                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    warehouseEntries.add(new Warehouse(
                            resultSet.getInt("id"),
                            resultSet.getInt("nodeId"),
                            resultSet.getInt("receivedQuantity"),
                            resultSet.getInt("shippedQuantity"),
                            resultSet.getDate("date").toLocalDate()
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return warehouseEntries;
    }

    public void updateWarehouseEntry(Warehouse warehouseEntry) {
        if (!nodeExists(warehouseEntry.getNodeId())) {
            showAlert(Alert.AlertType.WARNING, "Предупреждение", "Узел не найден",
                    "Узел с id " + warehouseEntry.getNodeId() + " не существует.");
            return;
        }

        try (Connection connection = DatabaseConnector.getConnection()) {
            String query = "UPDATE Warehouse SET nodeId = ?, date = ?, " +
                    "receivedQuantity = ?, shippedQuantity = ? WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, warehouseEntry.getNodeId());
                statement.setDate(2, Date.valueOf(warehouseEntry.getDate()));
                statement.setInt(3, warehouseEntry.getReceivedQuantity());
                statement.setInt(4, warehouseEntry.getShippedQuantity());
                statement.setInt(5, warehouseEntry.getId());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteWarehouseEntry(int warehouseEntryId) {
        try (Connection connection = DatabaseConnector.getConnection()) {
            String query = "DELETE FROM Warehouse WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, warehouseEntryId);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean nodeExists(int nodeId) {
        try (Connection connection = DatabaseConnector.getConnection()) {
            String query = "SELECT COUNT(*) FROM Nodes WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, nodeId);
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

    public int getCountByParameters(int nodeId, LocalDate date) {
        int count = 0;
        try (Connection connection = DatabaseConnector.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT SUM(receivedQuantity) - SUM(shippedQuantity)" +
                     " FROM warehouse WHERE nodeId = ? AND date <= ?");
        ) {
            statement.setInt(1, nodeId);
            statement.setDate(2, Date.valueOf(date));

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    count = resultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }
}
