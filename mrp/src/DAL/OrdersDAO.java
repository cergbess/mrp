package DAL;

import Models.Order;
import javafx.scene.control.Alert;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrdersDAO {

    private void showAlert(Alert.AlertType alertType, String title, String headerText, String contentText) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    public void createOrder(int nodeId, LocalDate orderDate, int quantityOrdered,
                            String description, String supplier, Double price) {
        try (Connection connection = DatabaseConnector.getConnection()) {
            if (!nodeExists(nodeId)) {
                showAlert(Alert.AlertType.WARNING, "Предупреждение", "Узел не найден",
                        "Узел с id " + nodeId + " не существует.");
                return;
            }

            if (quantityOrdered <= 0 || price < 0) {
                showAlert(Alert.AlertType.WARNING, "Ошибка", "Отрицательные значения",
                        "Количество и цена не могут быть отрицательными.");
                return;
            }

            String query = "INSERT INTO Orders(nodeId, orderDate, quantityOrdered, description, supplier, price) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, nodeId);
                statement.setDate(2, Date.valueOf(orderDate));
                statement.setInt(3, quantityOrdered);
                statement.setString(4, description);
                statement.setString(5, supplier);
                statement.setDouble(6, price);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        try (Connection connection = DatabaseConnector.getConnection()) {
            String query = "SELECT * FROM Orders";
            try (PreparedStatement statement = connection.prepareStatement(query);
                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    orders.add(new Order(
                            resultSet.getInt("id"),
                            resultSet.getInt("nodeId"),
                            resultSet.getDate("orderDate").toLocalDate(),
                            resultSet.getInt("quantityOrdered"),
                            resultSet.getString("description"),
                            resultSet.getString("supplier"),
                            resultSet.getDouble("price")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public void updateOrder(Order order) {
        if (!nodeExists(order.getNodeId())) {
            showAlert(Alert.AlertType.WARNING, "Предупреждение", "Узел не найден",
                    "Узел с id " + order.getNodeId() + " не существует.");
            return;
        }
        if (order.getQuantityOrdered() <= 0 || order.getPrice() < 0) {
            showAlert(Alert.AlertType.WARNING, "Ошибка", "Отрицательные значения",
                    "Количество и цена не могут быть отрицательными.");
            return;
        }

        try (Connection connection = DatabaseConnector.getConnection()) {
            String query = "UPDATE Orders SET nodeId = ?, orderDate = ?, quantityOrdered = ?, " +
                    "description = ?, supplier = ?, price = ? WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, order.getNodeId());
                statement.setDate(2, Date.valueOf(order.getOrderDate()));
                statement.setInt(3, order.getQuantityOrdered());
                statement.setString(4, order.getDescription());
                statement.setString(5, order.getSupplier());
                statement.setDouble(6, order.getPrice());
                statement.setInt(7, order.getId());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteOrder(int orderId) {
        try (Connection connection = DatabaseConnector.getConnection()) {
            String query = "DELETE FROM Orders WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, orderId);
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
}