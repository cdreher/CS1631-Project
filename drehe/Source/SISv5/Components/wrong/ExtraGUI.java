import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ExtraGUI extends Application {

	@Override
	public void start(Stage primaryStage) {
		try {
			ExtraGUIContent root = new ExtraGUIContent(primaryStage);

			Scene scene = new Scene(root, 900, 600);

			// scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);

			primaryStage.setMinHeight(600);
			primaryStage.setMinWidth(900);

			primaryStage.setMaxHeight(600);
			primaryStage.setMaxWidth(900);

			primaryStage.setResizable(false);
			primaryStage.setTitle("Extra GUI Component");

			primaryStage.setOnCloseRequest(e -> System.exit(0));

			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
