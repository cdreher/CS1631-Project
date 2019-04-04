import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class voteGUI extends Application {


    Stage window;

    @Override
    public void start(Stage primaryStage) throws Exception {

        Parent root = FXMLLoader.load(getClass().getResource("voting.fxml"));

        Scene logScene, userScene, adminScene;
        window = primaryStage;
        window.setTitle("Voting System");   //voting system title

        //Log in Scene
        logScene = new Scene(root,800,500);
        window.setScene(logScene);

        //we display the GUI
        window.show();

    }

    public static void main(String[] args) {
        launch(args);
    }



}
