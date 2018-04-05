package postaurant;


import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import postaurant.context.FXMLoaderService;
import postaurant.model.Order;
import postaurant.model.User;
import postaurant.service.UserService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DubScreenController {
    private ArrayList<Button> tableButtonList = new ArrayList<>();
    private int page;

    private final UserService userService;
    private final FXMLoaderService loaderService;

    @Value("/FXML/TableWindow.fxml")
    private Resource tableWindowForm;

    @Value("/FXML/CreateOrder.fxml")
    private Resource createOrderForm;

    private  User user;
    @FXML
    private TextField timeField;
    @FXML
    private TextField userID;
    @FXML
    private GridPane tablesGrid;

    //time preparations
    private final Integer startTime = 1;
    private Integer seconds = startTime;

    public DubScreenController(UserService userService, FXMLoaderService loaderService) {
        this.userService =userService;
        this.loaderService = loaderService;
    }

    public void initialize() {
    }


    @FXML
    private void handleExitButton(ActionEvent event) {
        ((Node) event.getSource()).getScene().getWindow().hide();
    }

    @FXML
    private void handleTimeButton() {
        doTime();
    }

    @FXML
    private void handleUpButton() {
        setTables(false);
    }

    @FXML
    private void handleDownButton() {
        setTables(true);
    }

    private boolean isNextPage() {
        try {
            System.out.println(tableButtonList.get((this.page * 16)));
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
        return true;

    }


        @FXML
        private void handleNewTableButton(ActionEvent event) {
            try {
                FXMLLoader loader = loaderService.getLoader(createOrderForm.getURL());
                Parent root = loader.load();
                Scene scene = new Scene(root);
                scene.getStylesheets().add("mainScreen.css");
                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.initStyle(StageStyle.UNDECORATED);
                stage.setScene(scene);
                stage.showAndWait();
                CreateOrderController controller = loader.getController();
                int tableNum = controller.getTableNum();
                if (tableNum != -1) {
                    loader=loaderService.getLoader(tableWindowForm.getURL());
                    Parent parent= loader.load();
                    TableWindowController tableWindowController=loader.getController();
                    tableWindowController.setTableNo(tableNum);
                    scene=new Scene(parent);
                    scene.getStylesheets().add("POStaurant.css");
                    stage=(Stage) ((Node)event.getSource()).getScene().getWindow();
                    stage.setScene(scene);
                    stage.show();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }

        }


    public void doTime() {
        timeField.setText(createTime());
        Timeline time = new Timeline();
        KeyFrame frame = new KeyFrame(Duration.seconds(3), event -> {
            seconds--;
            if (seconds <= 0) {
                timeField.setText("");
                time.stop();
            }
        });
        time.getKeyFrames().add(frame);
        time.setCycleCount(Timeline.INDEFINITE);
        time.play();
    }

    public String createTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String formatDateTime = now.format(formatter);
        return formatDateTime;

    }

    public void setUser(User user) {
        this.user=user;
        this.userID.setText(user.getFirst_name());
        this.page = 0;
        createTableButtons(user);
        setTables(true);

    }

    public void setTables(boolean forward) {
        System.out.println("BEFORE: " + this.page);
        int start;
        int x = 0;
        int y = 0;
        if (forward) {
            System.out.println("forward");
            if (this.page == 0) {
                start = 0;
            } else {
                start = this.page * 16;
            }
            if (isNextPage()) {
                for (int i = 0; i < (tablesGrid.getChildren()).size(); ) {
                    tablesGrid.getChildren().remove(tablesGrid.getChildren().get(i));
                }
                System.out.println(start);
                if (tableButtonList.size() - start > 15) {
                    for (int i = start; i < (start + 16); i++) {
                        tablesGrid.add(tableButtonList.get(i), x, y);
                        GridPane.setMargin(tableButtonList.get(i), new Insets(2, 2, 2, 2));
                        if (x == 3) {
                            x = 0;
                            y++;
                        } else {
                            x++;
                        }

                    }
                } else {
                    for (int i = start; i < tableButtonList.size(); i++) {
                        tablesGrid.add(tableButtonList.get(i), x, y);
                        GridPane.setMargin(tableButtonList.get(i), new Insets(2, 2, 2, 2));
                        if (x == 3) {
                            x = 0;
                            y++;
                        } else {
                            x++;
                        }

                    }
                }
                page++;
            }
        } else {
            System.out.println("backwards");
            if (this.page > 1) {
                if (this.page == 2) {
                    start = 0;
                } else {
                    start = (this.page - 2) * 16;
                }
                for (int i = start; i < (start + 16); i++) {
                    tablesGrid.add(tableButtonList.get(i), x, y);
                    GridPane.setMargin(tableButtonList.get(i), new Insets(2, 2, 2, 2));
                    if (x == 3) {
                        x = 0;
                        y++;
                    } else {
                        x++;
                    }

                }
                page--;
            }

        }
        System.out.println("AFTER: " + this.page);
    }


    public void createTableButtons(User user) {
        try {
            List<Order> tables = userService.getUserOrders(user);
            for (Order o: tables) {
                String text =""+o.getTableNo();
                Button button = new Button(text);
                button.setPrefHeight(70.0);
                button.setPrefWidth(95.0);
                button.setMnemonicParsing(false);
                tableButtonList.add(button);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
