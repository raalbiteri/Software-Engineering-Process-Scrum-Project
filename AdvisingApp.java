import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AdvisingApp extends Application {
    @Override
    public void start(Stage stage) throws Exception{
        FXMLLoader mainLoader = new FXMLLoader(getClass().getResource("gui.fxml"));
        Parent root = mainLoader.load();
        stage.setScene(new Scene(root, 600, 400));
        stage.setTitle("Advising App");
        stage.sizeToScene();

        Stage majorStage = new Stage();
        FXMLLoader majorLoader = new FXMLLoader(getClass().getResource("major.fxml"));
        Parent majorRoot = majorLoader.load();
        majorStage.setScene(new Scene(majorRoot, 900, 810));
        majorStage.setTitle("Major Selection");
        majorStage.sizeToScene();

        Controller controller = mainLoader.getController();
        MajorController majorController = majorLoader.getController();
        controller.setMajorController(majorController);
        controller.setMajorStage(majorStage);

        majorController.setController(controller);
        majorController.setStage(stage);

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
