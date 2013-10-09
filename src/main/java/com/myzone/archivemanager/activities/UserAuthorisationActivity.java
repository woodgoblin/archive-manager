package com.myzone.archivemanager.activities;

import com.myzone.archivemanager.core.Core;
import com.myzone.archivemanager.model.Document;
import com.myzone.archivemanager.model.User;
import com.myzone.archivemanager.services.UserAuthorizationService;
import com.myzone.utils.LastStackFramePane;
import javafx.beans.InvalidationListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.myzone.archivemanager.core.JavaFxBasedCore.binder;
import static com.myzone.archivemanager.model.User.CloseableSession;
import static com.myzone.utils.TaskScheduler.schedule;
import static javafx.application.Platform.runLater;

/**
 * @author myzone
 * @date 9/6/13 11:29 AM
 */
public class UserAuthorisationActivity extends StatusActivity<Node> {

    private final LastStackFramePane rootNode;

    private final Core<Node, Core.DataProvider<User, Core.DataProvider<Document, Core.DataProvider.DataProviderEnd>>> core;

    private final Node authorizationForm;
    private final UserAuthorizationService userAuthorizationService;
    private CloseableSession session;

    public UserAuthorisationActivity(Core<? super Node, Core.DataProvider<User, Core.DataProvider<Document, Core.DataProvider.DataProviderEnd>>> core) {
        // todo: remove this hack
        this.core = (Core<Node, Core.DataProvider<User, Core.DataProvider<Document, Core.DataProvider.DataProviderEnd>>>) core;
        this.rootNode = new LastStackFramePane();

        this.authorizationForm = constructForm();
        this.userAuthorizationService = new UserAuthorizationService();

        this.session = null;

        rootNode.getChildren().add(authorizationForm);
    }

    @Override
    public void onLoad(@NotNull Core.ApplicationGraphicsContext<? super Node> graphicsContext) {
        graphicsContext.bind(rootNode);

        core.loadService(userAuthorizationService);
    }

    @Override
    public void onUnload(@NotNull Core.ApplicationGraphicsContext<? super Node> graphicsContext) {
        graphicsContext.unbind(rootNode);

        core.unloadService(userAuthorizationService);
    }

    public CloseableSession getSession() {
        return session;
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

        Button registerBtn = new Button("Register");
        registerBtn.setOnAction((e) -> {
            passwordTextField.setText("");

            UserRegistrationActivity userRegistrationActivity = new UserRegistrationActivity(core);
            userRegistrationActivity.getStatus().addListener((observableValue, oldStatus, newStatus) -> {
                if (newStatus == Status.DONE) {
                    core.unloadActivity(userRegistrationActivity, rootNode, binder());
                    passwordTextField.setText("");
                }
            });

            core.loadActivity(userRegistrationActivity, rootNode, binder());
        });

        Button loginBtn = new Button("Login");
        loginBtn.setDisable(true);
        loginBtn.setOnAction((e) -> {
            actionText.setText("");
            login(
                    userTextField.getText(),
                    passwordTextField.getText(),
                    (userRegistrationResult) -> {
                        Optional<CloseableSession> sessionOptional = userRegistrationResult.getSession();
                        if (sessionOptional.isPresent()) {
                            session = sessionOptional.get();

                            System.out.println("Logined " + session.getSessionOwner());
                            registerBtn.setDisable(true);
                            schedule(() -> runLater(() -> status.set(Status.DONE)), 1, TimeUnit.SECONDS);
                        }

                        return null;
                    }
            );
        });

        InvalidationListener invalidationListener = unused -> {
            loginBtn.setDisable(
                    !userTextField.getText().matches("[A-Za-z][A-Za-z0-9]+")
                            || passwordTextField.getText().length() < 6
                            || !passwordTextField.getText().matches(".*[0-9].*")
                            || !passwordTextField.getText().matches(".*[A-Z].*")
                            || !passwordTextField.getText().matches(".*[a-z].*")
            );
        };

        userTextField.textProperty().addListener(invalidationListener);
        passwordTextField.textProperty().addListener(invalidationListener);

        HBox buttonWrapper = new HBox(10);
        buttonWrapper.setAlignment(Pos.BOTTOM_RIGHT);
        buttonWrapper.getChildren().add(loginBtn);
        buttonWrapper.getChildren().add(registerBtn);
        grid.add(buttonWrapper, 1, 4);

        return grid;
    }

    protected void login(String username, String password, Function<UserAuthorizationService.UserAuthorizationResponse, Void> callback) {
        core.processRequest(userAuthorizationService, new UserAuthorizationService.UserAuthorizationRequest() {

            @Override
            public String getUsername() {
                return username;
            }

            @Override
            public String getPassword() {
                return password;
            }

        }, callback);
    }

}
