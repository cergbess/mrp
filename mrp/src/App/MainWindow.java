package App;

import DAL.EdgesDAO;
import DAL.NodesDAO;
import DAL.OrdersDAO;
import DAL.WarehouseDAO;
import Models.Edge;
import Models.Node;
import Models.Order;
import Models.Warehouse;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.util.List;

public class MainWindow extends Application {

    private EdgesDAO edgesDAO = new EdgesDAO();
    private NodesDAO nodesDAO = new NodesDAO();
    private OrdersDAO ordersDAO = new OrdersDAO();
    private WarehouseDAO warehouseDAO = new WarehouseDAO();
    private EdgesWindow edgesWindow = new EdgesWindow();
    private NodesWindow nodesWindow = new NodesWindow();
    private OrdersWindow ordersWindow = new OrdersWindow();
    private WarehouseWindow warehouseWindow = new WarehouseWindow();
    private ComponentsWindow componentsWindow = new ComponentsWindow();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("MRP");

        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(20);

        vbox.setBackground(new Background(new BackgroundFill(Color.ORANGE,
                CornerRadii.EMPTY, javafx.geometry.Insets.EMPTY)));
        primaryStage.setScene(new Scene(vbox, 300, 400));

        Label label = new Label("Меню");
        label.setStyle("-fx-font-size: 18;");
        vbox.getChildren().add(label);

        Button openEdgesButton = createButton("Открыть панель рёбер");
        openEdgesButton.setOnAction(e -> {
            List<Edge> edges = edgesDAO.getAllEdges();
            edgesWindow.openEdgesWindow(edges);
        });
        vbox.getChildren().add(openEdgesButton);

        Button openNodesButton = createButton("Открыть панель узлов");
        openNodesButton.setOnAction(e -> {
            List<Node> nodes = nodesDAO.getAllNodes();
            nodesWindow.openNodesWindow(nodes);
        });
        vbox.getChildren().add(openNodesButton);

        Button openOrdersButton = createButton("Открыть панель заказов");
        openOrdersButton.setOnAction(e -> {
            List<Order> orders = ordersDAO.getAllOrders();
            ordersWindow.openOrdersWindow(orders);
        });
        vbox.getChildren().add(openOrdersButton);

        Button openWarehouseButton = createButton("Открыть панель склада");
        openWarehouseButton.setOnAction(e -> {
            List<Warehouse> warehouseEntries = warehouseDAO.getAllWarehouseEntries();
            warehouseWindow.openWarehouseWindow(warehouseEntries);
        });
        vbox.getChildren().add(openWarehouseButton);

        Button openComponentsButton = createButton("Открыть окно компонентов");
        openComponentsButton.setOnAction(e -> {
            componentsWindow.openComponentWindow();
        });
        vbox.getChildren().add(openComponentsButton);

        Button exitButton = new Button("Выход");
        exitButton.setOnAction(e -> primaryStage.close());
        exitButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        vbox.getChildren().add(exitButton);

        primaryStage.show();
    }
    private Button createButton(String text) {
        Button button = new Button(text);
        button.setMinWidth(200);
        button.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white;");
        return button;
    }
}
