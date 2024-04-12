package App;

import DAL.EdgesDAO;
import DAL.NodesDAO;
import DAL.PathInfo;
import Models.ExtendedEdge;
import Models.Edge;
import Models.Node;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.*;

public class ComponentsWindow {

    private EdgesDAO edgesDAO = new EdgesDAO();
    private List<ExtendedEdge> allNodesList;

    public void openComponentWindow() {
        Stage newStage = new Stage();
        newStage.setTitle("Панель компонентов");
        BorderPane root = new BorderPane();

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

        VBox bottomPane = new VBox();
        TextField nodeNumberField = new TextField();
        nodeNumberField.setPromptText("Введите номер узла");
        Button getComponentsButton = new Button("Получить компоненты");
        getComponentsButton.setOnAction(event -> {
            int startingNode = Integer.parseInt(nodeNumberField.getText());
            List<ExtendedEdge> allLowerNodes = getAllLowerNodes(startingNode);
            Set<ExtendedEdge> uniqueEdges = new HashSet<>(allLowerNodes);
            ObservableList<ExtendedEdge> uniqueData = FXCollections.observableArrayList(uniqueEdges);
            tableView.setItems(uniqueData);
        });
        Button mergeButton = new Button("Объединить записи");
        mergeButton.setOnAction(event -> mergeRecords(tableView));

        HBox buttonBox = new HBox(getComponentsButton, mergeButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setSpacing(10);

        bottomPane.getChildren().addAll(new Label("Номер узла:"), nodeNumberField, buttonBox);


        root.setCenter(tableView);
        root.setBottom(bottomPane);


        Scene scene = new Scene(root, 800, 500);
        newStage.setScene(scene);
        newStage.show();
    }

    private List<ExtendedEdge> getAllLowerNodes(int startingNode) {
        allNodesList = new ArrayList<>();
        Set<ExtendedEdge> outerEdgesSet = new HashSet<>();
        List<ExtendedEdge> outerEdges = getOuterEdgesForNode(startingNode);
        NodesDAO nodesDAO = new NodesDAO();

        for (ExtendedEdge edge : outerEdges) {
            int nodeId = edge.getLowerNodeName();
            Node node = nodesDAO.getNodeById(nodeId);
            if (node != null) {
                edge.setNodeName(node.getNodeName());
                edge.setNodeDescription(node.getNodeDescription());
            }
            outerEdgesSet.add(edge);
        }

        return new ArrayList<>(outerEdgesSet);
    }

    private List<ExtendedEdge> getOuterEdgesForNode(int node) {
        List<Edge> allEdges = edgesDAO.getAllEdgesForNode(node);
        List<ExtendedEdge> outerEdges = new ArrayList<>();
        Set<Integer> visitedNodes = new HashSet<>();

        for (Edge edge : allEdges) {
            if (edge.getUpperNodeName() == node && !visitedNodes.contains(edge.getLowerNodeName())) {
                visitedNodes.add(node);
                List<PathInfo> paths = getAllOuterEdgesRecursive(edge.getLowerNodeName(), allEdges, new HashSet<>(visitedNodes), edge.getWeight());
                for (PathInfo pathInfo : paths) {
                    if (!pathInfo.getEdges().isEmpty()) {
                        ExtendedEdge newEdge = new ExtendedEdge(pathInfo.getEdges().get(0).getUpperNodeName(), pathInfo.getEdges().get(pathInfo.getEdges().size() - 1).getLowerNodeName(), pathInfo.getTotalWeight(), "", "");
                        outerEdges.add(newEdge);
                    }
                }
            }
        }

        return outerEdges;
    }

    private List<PathInfo> getAllOuterEdgesRecursive(int currentNode, List<Edge> allEdges, Set<Integer> visitedNodes, int prevWeight) {
        List<PathInfo> paths = new ArrayList<>();
        visitedNodes.add(currentNode);
        boolean isOuterNode = true;

        for (Edge edge : allEdges) {
            if (edge.getUpperNodeName() == currentNode) {
                isOuterNode = false;
                if (!visitedNodes.contains(edge.getLowerNodeName())) {
                    int newWeight = prevWeight * edge.getWeight();
                    List<PathInfo> nextPaths = getAllOuterEdgesRecursive(edge.getLowerNodeName(), allEdges, new HashSet<>(visitedNodes), newWeight);
                    paths.addAll(nextPaths);
                }
            }
        }

        if (isOuterNode) {
            PathInfo pathInfo = new PathInfo();
            ExtendedEdge newEdge = new ExtendedEdge(currentNode, currentNode, prevWeight, "", "");
            pathInfo.addEdge(newEdge);
            paths.add(pathInfo);
        }

        return paths;
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
        showAlert("Записи объединены и веса сложены.");
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle("Информация");
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}

