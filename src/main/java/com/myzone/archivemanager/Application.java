package com.myzone.archivemanager;

import com.myzone.archivemanager.activities.UserRegistrationActivity;
import com.myzone.archivemanager.core.JavaFxBasedCore;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import static javafx.application.Platform.runLater;

/**
 * @author myzone
 * @date 9/5/13 5:14 PM
 */
public class Application extends javafx.application.Application {

    @Override
    public void start(Stage stage) throws Exception {
        Pane rootPane = new Pane();
        stage.setScene(new Scene(rootPane));
        stage.setResizable(false);

        JavaFxBasedCore core = new JavaFxBasedCore(rootPane);
        UserRegistrationActivity userRegistrationActivity = new UserRegistrationActivity(core);
        core.loadActivity(userRegistrationActivity);

        stage.setOnCloseRequest(e -> System.exit(0));
        runLater(stage::show);
    }

    public static void main(String[] args) {
        javafx.application.Application.launch(args);
    }

}
