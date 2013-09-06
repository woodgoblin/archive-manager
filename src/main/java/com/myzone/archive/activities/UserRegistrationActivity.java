package com.myzone.archive.activities;

import com.myzone.archive.core.Activity;
import com.myzone.archive.core.Core;
import com.myzone.archive.model.User;
import com.myzone.archive.services.UserRegistrationService;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.function.Function;

/**
 * @author myzone
 * @date 9/6/13 11:29 AM
 */
public class UserRegistrationActivity implements Activity<Node> {

    private final Core<Node> core;
    private final Node rootNode;

    private UserRegistrationService registrationService;

    public UserRegistrationActivity(Core<Node> core) {
        this.core = core;
        this.rootNode = constructForm();
    }

    @Override
    public void onLoad(Core.ApplicationGraphicsContext<? super Node> graphicsContext) {
        graphicsContext.bind(rootNode);

        registrationService = new UserRegistrationService();
        core.loadService(registrationService);
    }

    @Override
    public void onUnload(Core.ApplicationGraphicsContext<? super Node> graphicsContext) {
        graphicsContext.unbind(rootNode);
        core.unloadService(registrationService);
    }

    protected Node constructForm() {
        GridPane grid = new GridPane();
        grid.setPrefSize(450, 400);
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Text sceneTitle = new Text("Welcome");
        sceneTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(sceneTitle, 0, 0, 2, 1);

        Label userName = new Label("User Name:");
        grid.add(userName, 0, 1);

        TextField userTextField = new TextField();
        grid.add(userTextField, 1, 1);

        Label passwordLabel = new Label("Password:");
        grid.add(passwordLabel, 0, 2);

        PasswordField passwordTextField = new PasswordField();
        grid.add(passwordTextField, 1, 2);

        Text actionText = new Text();
        actionText.setTextAlignment(TextAlignment.RIGHT);
        grid.add(actionText, 1, 6, 6, 1);

        Button btn = new Button("Register");
        btn.setDisable(true);
        btn.setOnAction(actionEvent -> {
            actionText.setText("");
            register(
                    userTextField.getText(),
                    passwordTextField.getText(),
                    (userRegistrationResult) -> {
                        User registeredUser = userRegistrationResult.getRegisteredUser();

                        if (registeredUser != null) {
                            actionText.setFill(Color.GREEN);
                            actionText.setText("Registration has been succeeded");
                        } else {
                            actionText.setFill(Color.FIREBRICK);
                            actionText.setText("Registration has been failed");
                        }

                        return null;
                    }
            );
        });

        EventHandler<KeyEvent> eventHandler = actionEvent -> {
            btn.setDisable(
                    !userTextField.getText().matches("[A-Za-z][A-Za-z0-9]+")
                            || passwordTextField.getText().length() < 6
                            || !passwordTextField.getText().matches(".*[0-9].*")
                            || !passwordTextField.getText().matches(".*[A-Z].*")
                            || !passwordTextField.getText().matches(".*[a-z].*")
            );
        };

        userTextField.setOnKeyReleased(eventHandler);
        passwordTextField.setOnKeyReleased(eventHandler);

        HBox buttonWrapper = new HBox(10);
        buttonWrapper.setAlignment(Pos.BOTTOM_RIGHT);
        buttonWrapper.getChildren().add(btn);
        grid.add(buttonWrapper, 1, 4);

        return grid;
    }

    protected void register(String username, String password, Function<UserRegistrationService.UserRegistrationResponse, Void> callback) {
        callback.apply(core.processRequest(registrationService, new UserRegistrationService.UserRegistrationRequest() {
            @Override
            public String getPreferredUsername() {
                return username;
            }

            @Override
            public String getPreferredPassword() {
                return password;
            }
        }));
    }

}