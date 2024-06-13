package App;

import DAL.OrdersDAO;
import DAL.WarehouseDAO;
import Models.ExtendedEdge;
import Models.Order;
import Models.Warehouse;
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

public class WarehouseWindow {

    private WarehouseDAO warehouseDAO = new WarehouseDAO();

    private OrdersDAO orderDAO = new OrdersDAO();

    private TableView<Warehouse> tableView;
    ComponentsWindow componentsWindow = new ComponentsWindow();

    public void openWarehouseWindow(List<Warehouse> warehouseEntries) {
        Stage newStage = new Stage();
        newStage.setTitle("Панель склада");
        BorderPane root = new BorderPane();
        tableView = createTableView();
        populateTableView(warehouseEntries);

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

    private TableView<Warehouse> createTableView() {
        TableView<Warehouse> tableView = new TableView<>();
        tableView.setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);

        TableColumn<Warehouse, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Warehouse, Integer> nodeIdColumn = new TableColumn<>("ID узла");
        nodeIdColumn.setCellValueFactory(new PropertyValueFactory<>("nodeId"));

        TableColumn<Warehouse, LocalDate> dateColumn = new TableColumn<>("Дата");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<Warehouse, Integer> receivedQuantityColumn = new TableColumn<>("Получено");
        receivedQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("receivedQuantity"));

        TableColumn<Warehouse, Integer> shippedQuantityColumn = new TableColumn<>("Отгружено");
        shippedQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("shippedQuantity"));

        tableView.getColumns().addAll(idColumn, nodeIdColumn, dateColumn, receivedQuantityColumn, shippedQuantityColumn);
        return tableView;
    }

    private void populateTableView(List<Warehouse> warehouseEntries) {
        ObservableList<Warehouse> warehouseData = FXCollections.observableArrayList(warehouseEntries);
        tableView.setItems(warehouseData);
    }

    private HBox createButtonsBox() {
        HBox buttonsBox = new HBox(50);
        buttonsBox.setAlignment(Pos.CENTER);

        Button create = createCreateButton();
        Button getByNodeId = createGetByNodeIdButton();
        Button update = createUpdateButton();
        Button delete = createDeleteButton();
        Button closeButton = createCloseButton();
        Button getCount = createChooseDateButton();
        Button differenceButton = createDifferenceButton();

        buttonsBox.getChildren().addAll(create, getByNodeId, update, delete, getCount, differenceButton, closeButton);
        return buttonsBox;
    }

    private Button createCreateButton() {
        Button create = new Button("Create");
        create.setOnAction(e -> createWarehouseEntry());
        return create;
    }

    private void createWarehouseEntry() {
        Dialog<List<String>> dialog = new Dialog<>();
        dialog.setTitle("Create");
        dialog.setHeaderText("Введите данные для новой записи на складе");

        ButtonType buttonTypeOK = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(buttonTypeOK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField nodeId = new TextField();
        DatePicker date = new DatePicker();
        TextField receivedQuantity = new TextField();
        TextField shippedQuantity = new TextField();

        grid.add(new Label("ID узла:"), 0, 0);
        grid.add(nodeId, 1, 0);
        grid.add(new Label("Дата:"), 0, 1);
        grid.add(date, 1, 1);
        grid.add(new Label("Получено:"), 0, 2);
        grid.add(receivedQuantity, 1, 2);
        grid.add(new Label("Отгружено:"), 0, 3);
        grid.add(shippedQuantity, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == buttonTypeOK) {
                if (nodeId.getText().isEmpty() || date.getValue() == null ||
                        receivedQuantity.getText().isEmpty() || shippedQuantity.getText().isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Ошибка", "Пустые значения", "Пожалуйста, заполните все обязательные поля.");
                    return null;
                }
                List<String> results = new ArrayList<>();
                results.add(nodeId.getText());
                results.add(date.getValue().toString());
                results.add(receivedQuantity.getText());
                results.add(shippedQuantity.getText());
                return results;
            }
            return null;
        });

        Optional<List<String>> result = dialog.showAndWait();

        result.ifPresent(results -> {
            warehouseDAO.createWarehouseEntry(
                    Integer.parseInt(results.get(0)),
                    LocalDate.parse(results.get(1)),
                    Integer.parseInt(results.get(2)),
                    Integer.parseInt(results.get(3))
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
            List<Warehouse> allWarehouseEntries = warehouseDAO.getAllWarehouseEntries();
            tableView.setItems(FXCollections.observableArrayList(allWarehouseEntries));
            showAlert(Alert.AlertType.INFORMATION, "Получение всех товаров", "Все товары", "Все товары были успешно получены.");
        } else {
            try {
                int nodeIdValue = Integer.parseInt(nodeId);

                List<Warehouse> filteredWarehouse = warehouseDAO.getAllWarehouseEntries().stream()
                        .filter(warehouse -> warehouse.getNodeId() == nodeIdValue)
                        .collect(Collectors.toList());

                if (!filteredWarehouse.isEmpty()) {
                    tableView.setItems(FXCollections.observableArrayList(filteredWarehouse));
                    showAlert(Alert.AlertType.INFORMATION, "Результат поиска", "Найдены товары для узла с ID " + nodeIdValue, "Найдены следующие товары для узла с ID " + nodeIdValue + ".");
                } else {
                    showAlert(Alert.AlertType.WARNING, "Результат поиска", "Товары не найдены", "Для узла с ID " + nodeIdValue + " товары не найдены.");
                }
            } catch (NumberFormatException ex) {
                showErrorAlert("Ошибка", "Некорректный NodeId", "Пожалуйста, введите корректное числовое значение для NodeId.");
            }
        }
    }

    private Button createUpdateButton() {
        Button update = new Button("Update");
        update.setOnAction(e -> updateWarehouseEntry());
        return update;
    }

    private void updateWarehouseEntry() {
        Warehouse selectedEntry = tableView.getSelectionModel().getSelectedItem();

        if (selectedEntry != null) {
            Dialog<List<String>> dialog = new Dialog<>();
            dialog.setTitle("Update");
            dialog.setHeaderText("Измените данные записи на складе");

            ButtonType buttonTypeOK = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(buttonTypeOK, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);

            TextField nodeId = new TextField(String.valueOf(selectedEntry.getNodeId()));
            DatePicker date = new DatePicker(selectedEntry.getDate());
            TextField receivedQuantity = new TextField(String.valueOf(selectedEntry.getReceivedQuantity()));
            TextField shippedQuantity = new TextField(String.valueOf(selectedEntry.getShippedQuantity()));

            grid.add(new Label("ID узла:"), 0, 0);
            grid.add(nodeId, 1, 0);
            grid.add(new Label("Дата:"), 0, 1);
            grid.add(date, 1, 1);
            grid.add(new Label("Получено:"), 0, 2);
            grid.add(receivedQuantity, 1, 2);
            grid.add(new Label("Отгружено:"), 0, 3);
            grid.add(shippedQuantity, 1, 3);

            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == buttonTypeOK) {
                    if (nodeId.getText().isEmpty() || date.getValue() == null ||
                            receivedQuantity.getText().isEmpty() || shippedQuantity.getText().isEmpty()) {
                        showAlert(Alert.AlertType.WARNING, "Ошибка", "Пустые значения", "Пожалуйста, заполните все обязательные поля.");
                        return null;
                    }
                    List<String> results = new ArrayList<>();
                    results.add(nodeId.getText());
                    results.add(date.getValue().toString());
                    results.add(receivedQuantity.getText());
                    results.add(shippedQuantity.getText());
                    return results;
                }
                return null;
            });

            Optional<List<String>> result = dialog.showAndWait();

            result.ifPresent(results -> {
                selectedEntry.setNodeId(Integer.parseInt(results.get(0)));
                selectedEntry.setDate(LocalDate.parse(results.get(1)));
                selectedEntry.setReceivedQuantity(Integer.parseInt(results.get(2)));
                selectedEntry.setShippedQuantity(Integer.parseInt(results.get(3)));

                warehouseDAO.updateWarehouseEntry(selectedEntry);
                refreshTableView();
            });
        }
    }

    private Button createDeleteButton() {
        Button delete = new Button("Delete");
        delete.setOnAction(e -> deleteWarehouseEntry());
        return delete;
    }

    private void deleteWarehouseEntry() {
        Warehouse selectedEntry = tableView.getSelectionModel().getSelectedItem();

        if (selectedEntry != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Подтверждение удаления");
            alert.setHeaderText("Вы уверены, что хотите удалить эту запись на складе?");
            alert.setContentText("ID: " + selectedEntry.getId() +
                    "\nID узла: " + selectedEntry.getNodeId() +
                    "\nДата: " + selectedEntry.getDate() +
                    "\nПолучено: " + selectedEntry.getReceivedQuantity() +
                    "\nОтгружено: " + selectedEntry.getShippedQuantity());

            ButtonType buttonTypeOK = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(buttonTypeOK, buttonTypeCancel);

            alert.showAndWait().ifPresent(result -> {
                if (result == buttonTypeOK) {
                    warehouseDAO.deleteWarehouseEntry(selectedEntry.getId());
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
        List<Warehouse> updatedEntries = warehouseDAO.getAllWarehouseEntries();
        tableView.setItems(FXCollections.observableArrayList(updatedEntries));
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
                List<Warehouse> warehouses = warehouseDAO.getCountsByDate(selectedDate);
                List<ExtendedEdge> edges = new ArrayList<>();

                for (Warehouse warehouse : warehouses) {
                    int receivedQuantity = warehouse.getReceivedQuantity();
                    int shippedQuantity = warehouse.getShippedQuantity();

                    List<ExtendedEdge> lowerNodes = componentsWindow.getAllLowerNodes(warehouse.getNodeId());

                    for (ExtendedEdge edge : lowerNodes) {
                        edge.setWeight(edge.getWeight() * (receivedQuantity - shippedQuantity));
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

    private Button createDifferenceButton() {
        Button chooseDate = new Button("Difference");
        chooseDate.setOnAction(e -> openDidderenceWindow());
        return chooseDate;
    }

    private void openDidderenceWindow() {
        DatePicker datePicker = new DatePicker();

        Button confirmButton = new Button("Подтвердить");
        confirmButton.setOnAction(event -> {
            LocalDate selectedDate = datePicker.getValue();

            if (selectedDate != null) {
                List<Warehouse> warehouses = warehouseDAO.getCountsByDate(selectedDate);

                Map<Integer, ExtendedEdge> mergedMap1 = new HashMap<>();

                for (Warehouse warehouse : warehouses) {
                    int receivedQuantity = warehouse.getReceivedQuantity();
                    int shippedQuantity = warehouse.getShippedQuantity();

                    List<ExtendedEdge> lowerNodes = componentsWindow.getAllLowerNodes(warehouse.getNodeId());

                    for (ExtendedEdge edge : lowerNodes) {
                        edge.setWeight(edge.getWeight() * (receivedQuantity - shippedQuantity));
                    }

                    for (ExtendedEdge edge : lowerNodes) {
                        int lowerNodeName = edge.getLowerNodeName();
                        if (mergedMap1.containsKey(lowerNodeName)) {
                            ExtendedEdge existingEdge = mergedMap1.get(lowerNodeName);
                            int newWeight = existingEdge.getWeight() + edge.getWeight();
                            existingEdge.setWeight(newWeight);
                        } else {
                            mergedMap1.put(lowerNodeName, edge);
                        }
                    }
                }

                List<Order> orders = orderDAO.getOrdersByDate(selectedDate);

                Map<Integer, ExtendedEdge> mergedMap2 = new HashMap<>();

                for (Order order : orders) {
                    int quantityOrdered = order.getQuantityOrdered();

                    List<ExtendedEdge> lowerNodes = componentsWindow.getAllLowerNodes(order.getNodeId());

                    for (ExtendedEdge edge : lowerNodes) {
                        edge.setWeight(edge.getWeight() * quantityOrdered);
                    }

                    for (ExtendedEdge edge : lowerNodes) {
                        int lowerNodeName = edge.getLowerNodeName();
                        if (mergedMap2.containsKey(lowerNodeName)) {
                            ExtendedEdge existingEdge = mergedMap2.get(lowerNodeName);
                            int newWeight = existingEdge.getWeight() + edge.getWeight();
                            existingEdge.setWeight(newWeight);
                        } else {
                            mergedMap2.put(lowerNodeName, edge);
                        }
                    }
                }

                Map<Integer, ExtendedEdge> differenceMap = new HashMap<>();

                for (ExtendedEdge edge : mergedMap1.values()) {
                    int nodeId = edge.getLowerNodeName();
                    if (mergedMap2.containsKey(nodeId)) {
                        ExtendedEdge demandEdge = mergedMap2.get(nodeId);
                        int newWeight = edge.getWeight() - demandEdge.getWeight();
                        edge.setWeight(newWeight);
                    }
                    if (edge.getWeight() < 0) {
                        edge.setWeight(-edge.getWeight());
                    }
                    differenceMap.put(nodeId, edge);
                }

                List<ExtendedEdge> differences = new ArrayList<>(differenceMap.values());

                List<ExtendedEdge> negativeDifferences = differences.stream()
                        .filter(edge -> edge.getWeight() > 0)
                        .collect(Collectors.toList());

                showDifferenceWindow(negativeDifferences);

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

    private void showDifferenceWindow(List<ExtendedEdge> edges) {
        TableView<ExtendedEdge> tableView = new TableView<>();
        TableColumn<ExtendedEdge, String> nameColumn = new TableColumn<>("Номер компонента");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("lowerNodeName"));
        TableColumn<ExtendedEdge, Double> weightColumn = new TableColumn<>("Недостает");
        weightColumn.setCellValueFactory(new PropertyValueFactory<>("weight"));
        TableColumn<ExtendedEdge, String> nodeNameColumn = new TableColumn<>("Имя компонента");
        nodeNameColumn.setCellValueFactory(new PropertyValueFactory<>("nodeName"));
        TableColumn<ExtendedEdge, String> nodeDescColumn = new TableColumn<>("Описание компонента");
        nodeDescColumn.setCellValueFactory(new PropertyValueFactory<>("nodeDescription"));
        tableView.getColumns().addAll(nameColumn, weightColumn, nodeNameColumn, nodeDescColumn);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        tableView.setItems(FXCollections.observableArrayList(edges));

        Button closeButton = new Button("Выход");
        closeButton.setOnAction(event -> {
            Stage stage = (Stage) closeButton.getScene().getWindow();
            stage.close();
        });

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(closeButton);
        buttonBox.setAlignment(Pos.CENTER);

        VBox layout = new VBox(10);
        layout.getChildren().addAll(tableView, buttonBox);
        layout.setAlignment(Pos.CENTER);

        Stage stage = new Stage();
        stage.setTitle("Результаты");
        stage.setScene(new Scene(layout, 600, 400));
        stage.show();

    }
}