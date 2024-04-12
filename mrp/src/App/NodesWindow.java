package App;

import DAL.NodesDAO;
import Models.Node;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY;

public class NodesWindow {

    private NodesDAO nodesDAO = new NodesDAO();
    private TableView<Node> tableView;

    public void openNodesWindow(List<Node> nodes) {
        Stage newStage = new Stage();
        newStage.setTitle("Панель узлов");
        BorderPane root = new BorderPane();
        tableView = createTableView();
        populateTableView(nodes);

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

    private TableView<Node> createTableView() {
        TableView<Node> tableView = new TableView<>();
        tableView.setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);

        TableColumn<Node, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Node, String> nodeNameColumn = new TableColumn<>("Имя узла");
        nodeNameColumn.setCellValueFactory(new PropertyValueFactory<>("nodeName"));

        TableColumn<Node, String> nodeDescriptionColumn = new TableColumn<>("Описание узла");
        nodeDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("nodeDescription"));

        tableView.getColumns().addAll(idColumn, nodeNameColumn, nodeDescriptionColumn);
        return tableView;
    }

    private void populateTableView(List<Node> nodes) {
        ObservableList<Node> nodeData = FXCollections.observableArrayList(nodes);
        tableView.setItems(nodeData);
    }

    private HBox createButtonsBox() {
        HBox buttonsBox = new HBox(50);
        buttonsBox.setAlignment(Pos.CENTER);

        Button create = createCreateButton();
        Button getByName = createGetByNameButton();
        Button update = createUpdateButton();
        Button delete = createDeleteButton();
        Button closeButton = createCloseButton();

        buttonsBox.getChildren().addAll(create, getByName, update, delete, closeButton);
        return buttonsBox;
    }

    private Button createCreateButton() {
        Button create = new Button("Create");
        create.setOnAction(e -> createNode());
        return create;
    }

    private void createNode() {
        Dialog<List<String>> dialog = new Dialog<>();
        dialog.setTitle("Create");
        dialog.setHeaderText("Введите данные для нового узла");

        ButtonType buttonTypeOK = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(buttonTypeOK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField nodeName = new TextField();
        TextField nodeDescription = new TextField();

        grid.add(new Label("Имя узла:"), 0, 0);
        grid.add(nodeName, 1, 0);
        grid.add(new Label("Описание узла:"), 0, 1);
        grid.add(nodeDescription, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == buttonTypeOK) {
                if(nodeName.getText().isEmpty()) {
                    showErrorAlert("Ошибка", "Пустые значения", "Пожалуйста, заполните все обязательные поля.");
                    return null;
                }
                List<String> results = new ArrayList<>();
                results.add(nodeName.getText());
                results.add(nodeDescription.getText());
                return results;
            }
            return null;
        });

        Optional<List<String>> result = dialog.showAndWait();

        result.ifPresent(results -> {
            nodesDAO.createNode(results.get(0), results.get(1));
            refreshTableView();
        });
    }

    private Button createGetByNameButton() {
        Button getByName = new Button("Get By Name");
        getByName.setOnAction(e -> filterByName());
        return getByName;
    }

    private void filterByName() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Поиск узла");
        dialog.setHeaderText("Введите имя узла для поиска");
        dialog.setContentText("Имя узла:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(this::searchByName);
    }

    private void searchByName(String nodeName) {
        if (nodeName.isEmpty()) {
            List<Node> allNodes = nodesDAO.getAllNodes();
            tableView.setItems(FXCollections.observableArrayList(allNodes));
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Получение всех узлов");
            alert.setHeaderText("Все узлы");
            alert.setContentText("Все узлы были успешно получены.");
            alert.showAndWait();
        } else {
            List<Node> filteredNodes = nodesDAO.getAllNodes().stream()
                    .filter(node -> node.getNodeName().equalsIgnoreCase(nodeName))
                    .collect(Collectors.toList());

            if (!filteredNodes.isEmpty()) {
                tableView.setItems(FXCollections.observableArrayList(filteredNodes));
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Результат поиска");
                alert.setHeaderText("Найдены узлы:");
                alert.setContentText("По запросу '" + nodeName + "' найдены следующие узлы.");
                alert.showAndWait();
            } else {
                showErrorAlert("Результат поиска", "Узлы не найдены", "По запросу '" + nodeName + "' узлы не найдены.");
            }
        }
    }

    private Button createUpdateButton() {
        Button update = new Button("Update");
        update.setOnAction(e -> updateNode());
        return update;
    }

    private void updateNode() {
        Node selectedNode = tableView.getSelectionModel().getSelectedItem();

        if (selectedNode != null) {
            Dialog<List<String>> dialog = new Dialog<>();
            dialog.setTitle("Update");
            dialog.setHeaderText("Измените данные узла");

            ButtonType buttonTypeOK = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(buttonTypeOK, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);

            TextField nodeName = new TextField(selectedNode.getNodeName());
            TextField nodeDescription = new TextField(selectedNode.getNodeDescription());

            grid.add(new Label("Имя узла:"), 0, 0);
            grid.add(nodeName, 1, 0);
            grid.add(new Label("Описание узла:"), 0, 1);
            grid.add(nodeDescription, 1, 1);

            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == buttonTypeOK) {
                    if(nodeName.getText().isEmpty()){
                        showErrorAlert("Ошибка", "Пустые значения", "Пожалуйста, заполните все обязательные поля.");
                        return null;
                    }
                    List<String> results = new ArrayList<>();
                    results.add(nodeName.getText());
                    results.add(nodeDescription.getText());
                    return results;
                }
                return null;
            });

            Optional<List<String>> result = dialog.showAndWait();

            result.ifPresent(results -> {
                selectedNode.setNodeName(results.get(0));
                selectedNode.setNodeDescription(results.get(1));
                nodesDAO.updateNode(selectedNode);
                refreshTableView();
            });
        }
    }

    private Button createDeleteButton() {
        Button delete = new Button("Delete");
        delete.setOnAction(e -> deleteNode());
        return delete;
    }

    private void deleteNode() {
        Node selectedNode = tableView.getSelectionModel().getSelectedItem();

        if (selectedNode != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Подтверждение удаления");
            alert.setHeaderText("Вы уверены, что хотите удалить этот узел?");
            alert.setContentText("ID: " + selectedNode.getId() +
                    "\nИмя узла: " + selectedNode.getNodeName() +
                    "\nОписание узла: " + selectedNode.getNodeDescription());

            ButtonType buttonTypeOK = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(buttonTypeOK, buttonTypeCancel);

            alert.showAndWait().ifPresent(result -> {
                if (result == buttonTypeOK) {
                    nodesDAO.deleteNode(selectedNode.getId());
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
        List<Node> updatedNodes = nodesDAO.getAllNodes();
        tableView.setItems(FXCollections.observableArrayList(updatedNodes));
    }

    private void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}