import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MinimalJavaFXApp extends Application {

    @Override
    public void start(Stage stage) {
        // Create a label with some text
        Label label = new Label("Hello, JavaFX with GraalVM!");

        // Create a layout pane and add the label to it
        StackPane root = new StackPane();
        root.getChildren().add(label);

        // Create a scene with the layout pane and set its size
        Scene scene = new Scene(root, 320, 240);

        // Set the stage (main window) title
        stage.setTitle("Minimal JavaFX App");

        // Set the scene on the stage
        stage.setScene(scene);

        // Show the stage
        stage.show();
    }

    public static void main(String[] args) {
        // Launch the JavaFX application
        launch();
    }
}
