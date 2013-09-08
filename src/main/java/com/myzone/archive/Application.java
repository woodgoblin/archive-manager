package com.myzone.archive;

import com.myzone.archive.activities.UserRegistrationActivity;
import com.myzone.archive.core.Activity;
import com.myzone.archive.core.Core;
import com.myzone.archive.core.JavaFxBasedCore;
import com.myzone.archive.model.Document;
import com.myzone.archive.model.User;
import com.myzone.utils.ImmutableTuple;
import javafx.application.Platform;
import javafx.scene.Node;
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

        runLater(stage::show);
    }

    public static void main(String[] args) {
        javafx.application.Application.launch(args);
    }

}
