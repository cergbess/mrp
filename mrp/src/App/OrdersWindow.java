package App;

import DAL.EdgesDAO;
import DAL.OrdersDAO;
import Models.Edge;
import Models.ExtendedEdge;
import Models.Order;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY;

public class OrdersWindow {

    private OrdersDAO ordersDAO = new OrdersDAO();
    ComponentsWindow componentsWindow = new ComponentsWindow();
    private TableView<Order> tableView;

    public void openOrdersWindow(List<Order> orders) {
        Stage newStage = new Stage();
        newStage.setTitle("Панель заказов");
        BorderPane root = new BorderPane();
        tableView = createTableView();
        populateTableView(orders);

        ScrollPane scrollPane = new ScrollPane(tableView);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        root.setTop(scrollPane);
        HBox buttonsBox = createButtonsBox();
        root.setBottom(buttonsBox);
        HBox.setHgrow(scrollPane, Priority.ALWAYS);
        Scene scene = new Scene(root, 800, 500);
        newStage.setScene(scene);
        newStage.show();
    }

    private TableView<Order> createTableView() {
        TableView<Order> tableView = new TableView<>();
        tableView.setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);

        TableColumn<Order, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Order, Integer> nodeIdColumn = new TableColumn<>("ID узла");
        nodeIdColumn.setCellValueFactory(new PropertyValueFactory<>("nodeId"));

        TableColumn<Order, String> orderDateColumn = new TableColumn<>("Дата заказа");
        orderDateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));

        TableColumn<Order, Integer> quantityOrderedColumn = new TableColumn<>("Заказанное количество");
        quantityOrderedColumn.setCellValueFactory(new PropertyValueFactory<>("quantityOrdered"));

        TableColumn<Order, String> descriptionColumn = new TableColumn<>("Описание");
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<Order, String> supplierColumn = new TableColumn<>("Поставщик");
        supplierColumn.setCellValueFactory(new PropertyValueFactory<>("supplier"));

        TableColumn<Order, Double> priceColumn = new TableColumn<>("Цена");
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

        tableView.getColumns().addAll(idColumn, nodeIdColumn, orderDateColumn, quantityOrderedColumn,
                descriptionColumn, supplierColumn, priceColumn);
        return tableView;
    }

    private void populateTableView(List<Order> orders) {
        ObservableList<Order> orderData = FXCollections.observableArrayList(orders);
        tableView.setItems(orderData);
    }

    private HBox createButtonsBox() {
        HBox buttonsBox = new HBox(50);
        buttonsBox.setAlignment(Pos.CENTER);

        Button create = createCreateButton();
        Button getByNodeId = createGetByNodeIdButton();
        Button update = createUpdateButton();
        Button delete = createDeleteButton();
        Button chooseDate = createChooseDateButton();
        Button closeButton = createCloseButton();

        buttonsBox.getChildren().addAll(create, getByNodeId, update, delete, chooseDate, closeButton);
        return buttonsBox;
    }

    private Button createCreateButton() {
        Button create = new Button("Create");
        create.setOnAction(e -> createOrder());
        return create;
    }

    private void createOrder() {
        Dialog<List<String>> dialog = new Dialog<>();
        dialog.setTitle("Create");
        dialog.setHeaderText("Введите данные для нового заказа");

        ButtonType buttonTypeOK = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(buttonTypeOK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField nodeId = new TextField();
        DatePicker orderDate = new DatePicker();
        TextField quantityOrdered = new TextField();
        TextField description = new TextField();
        TextField supplier = new TextField();
        TextField price = new TextField();

        grid.add(new Label("ID узла:"), 0, 0);
        grid.add(nodeId, 1, 0);
        grid.add(new Label("Дата заказа:"), 0, 1);
        grid.add(orderDate, 1, 1);
        grid.add(new Label("Заказанное количество:"), 0, 2);
        grid.add(quantityOrdered, 1, 2);
        grid.add(new Label("Описание:"), 0, 3);
        grid.add(description, 1, 3);
        grid.add(new Label("Поставщик:"), 0, 4);
        grid.add(supplier, 1, 4);
        grid.add(new Label("Цена:"), 0, 5);
        grid.add(price, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == buttonTypeOK) {
                if (nodeId.getText().isEmpty() || orderDate.getValue() == null ||
                        quantityOrdered.getText().isEmpty() || price.getText().isEmpty()) {
                    showErrorAlert("Ошибка", "Пустые значения", "Пожалуйста, заполните все обязательные поля.");
                    return null;
                }
                List<String> results = new ArrayList<>();
                results.add(nodeId.getText());
                results.add(orderDate.getValue().toString());
                results.add(quantityOrdered.getText());
                results.add(description.getText());
                results.add(supplier.getText());
                results.add(price.getText());
                return results;
            }
            return null;
        });

        Optional<List<String>> result = dialog.showAndWait();

        result.ifPresent(results -> {
            ordersDAO.createOrder(
                    Integer.parseInt(results.get(0)),
                    LocalDate.parse(results.get(1)),
                    Integer.parseInt(results.get(2)),
                    results.get(3),
                    results.get(4),
                    Double.parseDouble(results.get(5))
            );
            refreshTableView();
        });
    }

    private Button createGetByNodeIdButton() {
        Button getByNodeId = new Button("Get By NodeId");
        getByNodeId.setOnAction(e -> filterByNodeId());
        return getByNodeId;
    }

    private void filterByNodeId() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Поиск заказов по NodeId");
        dialog.setHeaderText("Введите NodeId для поиска заказов");
        dialog.setContentText("NodeId:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(this::searchByNodeId);
    }

    private void searchByNodeId(String nodeId) {
        if (nodeId.isEmpty()) {
            List<Order> allOrders = ordersDAO.getAllOrders();
            tableView.setItems(FXCollections.observableArrayList(allOrders));
            showAlert(Alert.AlertType.INFORMATION, "Получение всех заказов", "Все заказы", "Все заказы были успешно получены.");
        } else {
            try {
                int nodeIdValue = Integer.parseInt(nodeId);

                List<Order> filteredOrders = ordersDAO.getAllOrders().stream()
                        .filter(order -> order.getNodeId() == nodeIdValue)
                        .collect(Collectors.toList());

                if (!filteredOrders.isEmpty()) {
                    tableView.setItems(FXCollections.observableArrayList(filteredOrders));
                    showAlert(Alert.AlertType.INFORMATION, "Результат поиска", "Найдены заказы для узла с ID " + nodeIdValue, "Найдены следующие заказы для узла с ID " + nodeIdValue + ".");
                } else {
                    showAlert(Alert.AlertType.WARNING, "Результат поиска", "Заказы не найдены", "Для узла с ID " + nodeIdValue + " заказы не найдены.");
                }
            } catch (NumberFormatException ex) {
                showErrorAlert("Ошибка", "Некорректный NodeId", "Пожалуйста, введите корректное числовое значение для NodeId.");
            }
        }
    }

    private Button createUpdateButton() {
        Button update = new Button("Update");
        update.setOnAction(e -> updateOrder());
        return update;
    }

    private void updateOrder() {
        Order selectedOrder = tableView.getSelectionModel().getSelectedItem();

        if (selectedOrder != null) {
            Dialog<List<String>> dialog = new Dialog<>();
            dialog.setTitle("Update");
            dialog.setHeaderText("Измените данные заказа");

            ButtonType buttonTypeOK = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(buttonTypeOK, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);

            TextField nodeId = new TextField(String.valueOf(selectedOrder.getNodeId()));
            DatePicker orderDate = new DatePicker(selectedOrder.getOrderDate());
            TextField quantityOrdered = new TextField(String.valueOf(selectedOrder.getQuantityOrdered()));
            TextField description = new TextField(selectedOrder.getDescription());
            TextField supplier = new TextField(selectedOrder.getSupplier());
            TextField price = new TextField(String.valueOf(selectedOrder.getPrice()));

            grid.add(new Label("ID узла:"), 0, 0);
            grid.add(nodeId, 1, 0);
            grid.add(new Label("Дата заказа:"), 0, 1);
            grid.add(orderDate, 1, 1);
            grid.add(new Label("Заказанное количество:"), 0, 2);
            grid.add(quantityOrdered, 1, 2);
            grid.add(new Label("Описание:"), 0, 3);
            grid.add(description, 1, 3);
            grid.add(new Label("Поставщик:"), 0, 4);
            grid.add(supplier, 1, 4);
            grid.add(new Label("Цена:"), 0, 5);
            grid.add(price, 1, 5);

            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == buttonTypeOK) {
                    if (nodeId.getText().isEmpty() || orderDate.getValue() == null ||
                            quantityOrdered.getText().isEmpty() || price.getText().isEmpty()) {
                        showErrorAlert("Ошибка", "Пустые значения", "Пожалуйста, заполните все обязательные поля.");
                        return null;
                    }
                    List<String> results = new ArrayList<>();
                    results.add(nodeId.getText());
                    results.add(orderDate.getValue().toString());
                    results.add(quantityOrdered.getText());
                    results.add(description.getText());
                    results.add(supplier.getText());
                    results.add(price.getText());
                    return results;
                }
                return null;
            });

            Optional<List<String>> result = dialog.showAndWait();

            result.ifPresent(results -> {
                selectedOrder.setNodeId(Integer.parseInt(results.get(0)));
                selectedOrder.setOrderDate(LocalDate.parse(results.get(1)));
                selectedOrder.setQuantityOrdered(Integer.parseInt(results.get(2)));
                selectedOrder.setDescription(results.get(3));
                selectedOrder.setSupplier(results.get(4));
                selectedOrder.setPrice(Double.parseDouble(results.get(5)));

                ordersDAO.updateOrder(selectedOrder);
                refreshTableView();
            });
        }
    }

    private Button createDeleteButton() {
        Button delete = new Button("Delete");
        delete.setOnAction(e -> deleteOrder());
        return delete;
    }

    private void deleteOrder() {
        Order selectedOrder = tableView.getSelectionModel().getSelectedItem();

        if (selectedOrder != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Подтверждение удаления");
            alert.setHeaderText("Вы уверены, что хотите удалить этот заказ?");
            alert.setContentText("ID: " + selectedOrder.getId() +
                    "\nID узла: " + selectedOrder.getNodeId() +
                    "\nДата заказа: " + selectedOrder.getOrderDate() +
                    "\nЗаказанное количество: " + selectedOrder.getQuantityOrdered() +
                    "\nОписание: " + selectedOrder.getDescription() +
                    "\nПоставщик: " + selectedOrder.getSupplier() +
                    "\nЦена: " + selectedOrder.getPrice());

            ButtonType buttonTypeOK = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(buttonTypeOK, buttonTypeCancel);

            alert.showAndWait().ifPresent(result -> {
                if (result == buttonTypeOK) {
                    ordersDAO.deleteOrder(selectedOrder.getId());
                    refreshTableView();
                }
            });
        }
    }

    private Button createCloseButton() {
        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> closeButtonAction());
        return closeButton;
    }

    private void closeButtonAction() {
        Stage stage = (Stage) tableView.getScene().getWindow();
        stage.close();
    }

    private void refreshTableView() {
        List<Order> updatedOrders = ordersDAO.getAllOrders();
        tableView.setItems(FXCollections.observableArrayList(updatedOrders));
    }

    private void showErrorAlert(String title, String header, String content) {
        showAlert(Alert.AlertType.WARNING, title, header, content);
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private Button createChooseDateButton() {
        Button chooseDate = new Button("Components");
        chooseDate.setOnAction(e -> openDateSelectionWindow());
        return chooseDate;
    }
    private void openDateSelectionWindow() {
        DatePicker datePicker = new DatePicker();

        Button confirmButton = new Button("Подтвердить");
        confirmButton.setOnAction(event -> {
            LocalDate selectedDate = datePicker.getValue();

            if (selectedDate != null) {
                List<Order> orders = ordersDAO.getOrdersByDate(selectedDate);
                List<ExtendedEdge> edges = new ArrayList<>();

                for (Order order : orders) {
                    int quantityOrdered = order.getQuantityOrdered();

                    List<ExtendedEdge> lowerNodes = componentsWindow.getAllLowerNodes(order.getNodeId());

                    for (ExtendedEdge edge : lowerNodes) {
                        edge.setWeight(edge.getWeight() * quantityOrdered);
                    }
                    edges.addAll(lowerNodes);
                }
                showEdgesInTableView(edges);

                Stage stage = (Stage) confirmButton.getScene().getWindow();
                stage.close();
            } else {
                showAlert(Alert.AlertType.INFORMATION,"Ошибка", "Выберите дату", "Пожалуйста, выберите дату.");
            }
        });

        VBox layout = new VBox(10);
        layout.getChildren().addAll(new Label("Выберите дату:"), datePicker, confirmButton);
        layout.setAlignment(Pos.CENTER);

        Stage stage = new Stage();
        stage.setTitle("Выбор даты");
        stage.setScene(new Scene(layout));
        stage.show();
    }

    private void showEdgesInTableView(List<ExtendedEdge> edges) {
        TableView<ExtendedEdge> tableView = new TableView<>();
        TableColumn<ExtendedEdge, String> nameColumn = new TableColumn<>("Номер компонента");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("lowerNodeName"));
        TableColumn<ExtendedEdge, Double> weightColumn = new TableColumn<>("Количество");
        weightColumn.setCellValueFactory(new PropertyValueFactory<>("weight"));
        TableColumn<ExtendedEdge, String> nodeNameColumn = new TableColumn<>("Имя узла");
        nodeNameColumn.setCellValueFactory(new PropertyValueFactory<>("nodeName"));
        TableColumn<ExtendedEdge, String> nodeDescColumn = new TableColumn<>("Описание узла");
        nodeDescColumn.setCellValueFactory(new PropertyValueFactory<>("nodeDescription"));
        tableView.getColumns().addAll(nameColumn, weightColumn, nodeNameColumn, nodeDescColumn);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        tableView.setItems(FXCollections.observableArrayList(edges));

        Button mergeButton = new Button("Сложить веса");
        mergeButton.setOnAction(event -> mergeRecords(tableView));

        Button closeButton = new Button("Выход");
        closeButton.setOnAction(event -> {
            Stage stage = (Stage) closeButton.getScene().getWindow();
            stage.close();
        });

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(mergeButton, closeButton);
        buttonBox.setAlignment(Pos.CENTER);

        VBox layout = new VBox(10);
        layout.getChildren().addAll(tableView, buttonBox);
        layout.setAlignment(Pos.CENTER);

        Stage stage = new Stage();
        stage.setTitle("Результаты");
        stage.setScene(new Scene(layout, 600, 400));
        stage.show();

    }

    private void mergeRecords(TableView<ExtendedEdge> tableView) {
        ObservableList<ExtendedEdge> items = tableView.getItems();

        Map<Integer, ExtendedEdge> mergedMap = new HashMap<>();
        for (ExtendedEdge edge : items) {
            int lowerNodeName = edge.getLowerNodeName();
            if (mergedMap.containsKey(lowerNodeName)) {
                ExtendedEdge existingEdge = mergedMap.get(lowerNodeName);
                int newWeight = existingEdge.getWeight() + edge.getWeight();
                existingEdge.setWeight(newWeight);
            } else {
                mergedMap.put(lowerNodeName, edge);
            }
        }

        tableView.getItems().setAll(mergedMap.values());
        showAlert(Alert.AlertType.INFORMATION,"Информация",null,"Записи объединены и веса сложены.");
    }
}