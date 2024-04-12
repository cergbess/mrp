package App;

import DAL.EdgesDAO;
import Models.Edge;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.control.TableView;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY;

public class EdgesWindow {

    private EdgesDAO edgesDAO = new EdgesDAO();
    private TableView<Edge> tableView;

    public void openEdgesWindow(List<Edge> edges) {
        Stage newStage = new Stage();
        newStage.setTitle("Панель ребер");
        BorderPane root = new BorderPane();
        tableView = createTableView();
        populateTableView(edges);

        ScrollPane scrollPane = new ScrollPane(tableView);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        root.setTop(scrollPane);
        HBox buttonsBox = createButtonsBox();
        root.setBottom(buttonsBox);
        HBox.setHgrow(scrollPane, Priority.ALWAYS);
        Scene scene = new Scene(root, 500, 500);
        newStage.setScene(scene);
        newStage.show();
    }

    private TableView<Edge> createTableView() {
        TableView<Edge> tableView = new TableView<>();
        tableView.setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);

        TableColumn<Edge, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Edge, Integer> upperNodeColumn = new TableColumn<>("Верхний узел");
        upperNodeColumn.setCellValueFactory(new PropertyValueFactory<>("upperNodeName"));

        TableColumn<Edge, Integer> lowerNodeColumn = new TableColumn<>("Нижний узел");
        lowerNodeColumn.setCellValueFactory(new PropertyValueFactory<>("lowerNodeName"));

        TableColumn<Edge, Integer> weightColumn = new TableColumn<>("Количество");
        weightColumn.setCellValueFactory(new PropertyValueFactory<>("weight"));

        TableColumn<Edge, String> unitOfMeasurementColumn = new TableColumn<>("Единица измерения");
        unitOfMeasurementColumn.setCellValueFactory(new PropertyValueFactory<>("unitOfMeasurement"));

        tableView.getColumns().addAll(idColumn, upperNodeColumn, lowerNodeColumn, weightColumn, unitOfMeasurementColumn);
        return tableView;
    }

    private void populateTableView(List<Edge> edges) {
        ObservableList<Edge> edgeData = FXCollections.observableArrayList(edges);
        tableView.setItems(edgeData);
    }

    private HBox createButtonsBox() {
        HBox buttonsBox = new HBox(50);
        buttonsBox.setAlignment(Pos.CENTER);

        Button create = createCreateButton();
        Button getById = createGetByIdButton();
        Button update = createUpdateButton();
        Button delete = createDeleteButton();
        Button closeButton = createCloseButton();

        buttonsBox.getChildren().addAll(create, getById, update, delete, closeButton);
        return buttonsBox;
    }

    private Button createCreateButton() {
        Button create = new Button("Create");
        create.setOnAction(e -> createEdge());
        return create;
    }

    private void createEdge() {
        Dialog<List<String>> dialog = new Dialog<>();
        dialog.setTitle("Create");
        dialog.setHeaderText("Введите данные для нового ребра");

        ButtonType buttonTypeOK = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(buttonTypeOK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField upperNodeField = new TextField();
        TextField lowerNodeField = new TextField();
        TextField weightField = new TextField();
        TextField unitOfMeasurementField = new TextField();

        grid.add(new Label("Верхний узел:"), 0, 0);
        grid.add(upperNodeField, 1, 0);
        grid.add(new Label("Нижний узел:"), 0, 1);
        grid.add(lowerNodeField, 1, 1);
        grid.add(new Label("Количество:"), 0, 2);
        grid.add(weightField, 1, 2);
        grid.add(new Label("Единица измерения:"), 0, 3);
        grid.add(unitOfMeasurementField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == buttonTypeOK) {
                if (upperNodeField.getText().isEmpty() || lowerNodeField.getText().isEmpty() || weightField.getText().isEmpty()) {
                    showErrorAlert("Ошибка", "Пустые значения", "Пожалуйста, заполните все обязательные поля.");
                    return null;
                }

                List<String> results = new ArrayList<>();
                results.add(upperNodeField.getText());
                results.add(lowerNodeField.getText());
                results.add(weightField.getText());
                results.add(unitOfMeasurementField.getText());
                return results;
            }
            return null;
        });

        Optional<List<String>> result = dialog.showAndWait();

        result.ifPresent(results -> {
            edgesDAO.createEdge(Integer.parseInt(results.get(0)), Integer.parseInt(results.get(1)),
                    Integer.parseInt(results.get(2)), results.get(3));

            refreshTableView();
        });
    }

    private Button createGetByIdButton() {
        Button getById = new Button("Get By Id");
        getById.setOnAction(e -> filterById());
        return getById;
    }

    private void filterById() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Поиск ребра");
        dialog.setHeaderText("Введите id ребра для поиска");
        dialog.setContentText("Id ребра:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(this::searchById);
    }

    private void searchById(String edgeId) {
        if (edgeId.isEmpty()) {
            List<Edge> allEdges = edgesDAO.getAllEdges();
            tableView.setItems(FXCollections.observableArrayList(allEdges));
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Получение всех рёбер");
            alert.setHeaderText("Все рёбра");
            alert.setContentText("Все рёбра были успешно получены.");
            alert.showAndWait();
        } else {
            List<Edge> filteredEdges = edgesDAO.getAllEdges().stream()
                    .filter(edge -> String.valueOf(edge.getId()).equalsIgnoreCase(edgeId))
                    .collect(Collectors.toList());

            if (!filteredEdges.isEmpty()) {
                tableView.setItems(FXCollections.observableArrayList(filteredEdges));
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Результат поиска");
                alert.setHeaderText("Найдены рёбра:");
                alert.setContentText("По запросу '" + edgeId + "' найдены следующие рёбра.");
                alert.showAndWait();
            } else {
                showErrorAlert("Результат поиска", "Рёбра не найдены", "По запросу '" + edgeId + "' рёбра не найдены.");
            }
        }
    }

    private Button createUpdateButton() {
        Button update = new Button("Update");
        update.setOnAction(e -> updateEdge());
        return update;
    }

    private void updateEdge() {
        Edge selectedEdge = tableView.getSelectionModel().getSelectedItem();

        if (selectedEdge != null) {
            Dialog<List<String>> dialog = new Dialog<>();
            dialog.setTitle("Update");
            dialog.setHeaderText("Измените данные ребра");

            ButtonType buttonTypeOK = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(buttonTypeOK, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);

            TextField upperNodeName = new TextField(String.valueOf(selectedEdge.getUpperNodeName()));
            TextField lowerNodeName = new TextField(String.valueOf(selectedEdge.getLowerNodeName()));
            TextField weight = new TextField(String.valueOf(selectedEdge.getWeight()));
            TextField unitOfMeasurement = new TextField(selectedEdge.getUnitOfMeasurement());

            grid.add(new Label("Верхнее имя узла:"), 0, 0);
            grid.add(upperNodeName, 1, 0);
            grid.add(new Label("Нижнее имя узла:"), 0, 1);
            grid.add(lowerNodeName, 1, 1);
            grid.add(new Label("Количество:"), 0, 2);
            grid.add(weight, 1, 2);
            grid.add(new Label("Единица измерения:"), 0, 3);
            grid.add(unitOfMeasurement, 1, 3);

            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == buttonTypeOK) {
                    if (upperNodeName.getText().isEmpty() || lowerNodeName.getText().isEmpty() || weight.getText().isEmpty()) {
                        showErrorAlert("Ошибка", "Пустые значения", "Пожалуйста, заполните все обязательные поля.");
                        return null;
                    }

                    List<String> results = new ArrayList<>();
                    results.add(upperNodeName.getText());
                    results.add(lowerNodeName.getText());
                    results.add(weight.getText());
                    results.add(unitOfMeasurement.getText());
                    return results;
                }
                return null;
            });

            Optional<List<String>> result = dialog.showAndWait();

            result.ifPresent(results -> {
                selectedEdge.setUpperNodeName(Integer.parseInt(results.get(0)));
                selectedEdge.setLowerNodeName(Integer.parseInt(results.get(1)));
                selectedEdge.setWeight(Integer.parseInt(results.get(2)));
                selectedEdge.setUnitOfMeasurement(results.get(3));

                edgesDAO.updateEdge(selectedEdge);

                refreshTableView();
            });
        }
    }

    private Button createDeleteButton() {
        Button delete = new Button("Delete");
        delete.setOnAction(e -> deleteEdge());
        return delete;
    }

    private void deleteEdge() {
        Edge selectedEdge = tableView.getSelectionModel().getSelectedItem();

        if (selectedEdge != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Подтверждение удаления");
            alert.setHeaderText("Вы уверены, что хотите удалить это ребро?");
            alert.setContentText("ID: " + selectedEdge.getId() +
                    "\nВерхний узел: " + selectedEdge.getUpperNodeName() +
                    "\nНижний узел: " + selectedEdge.getLowerNodeName());

            ButtonType buttonTypeOK = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(buttonTypeOK, buttonTypeCancel);

            alert.showAndWait().ifPresent(result -> {
                if (result == buttonTypeOK) {
                    edgesDAO.deleteEdge(selectedEdge.getId());

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
        List<Edge> updatedEdges = edgesDAO.getAllEdges();
        tableView.setItems(FXCollections.observableArrayList(updatedEdges));
    }

    private void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
