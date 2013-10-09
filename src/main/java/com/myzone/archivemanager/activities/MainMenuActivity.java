package com.myzone.archivemanager.activities;

import com.myzone.archivemanager.core.Core;
import com.myzone.archivemanager.model.User;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPaneBuilder;
import javafx.scene.layout.VBoxBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * @author myzone
 * @date 9/21/13 1:13 AM
 */
public class MainMenuActivity extends StatusActivity<Node> {

    private final Core<? super Node, ?> core;
    private final User.CloseableSession session;
    private final SplitPane rootPane;

    public MainMenuActivity(Core<? super Node, ?> core, User.CloseableSession session) {
        this.core = core;
        this.session = session;

        Label nicknameLabel = new Label();
        nicknameLabel.setText(session.getSessionOwner().getUsername());
        nicknameLabel.setMinHeight(26);
        nicknameLabel.setMaxHeight(26);
        nicknameLabel.setMaxWidth(Double.MAX_VALUE);

        Button logoutButton = new Button();
        logoutButton.setText("Logout");
        logoutButton.setMinHeight(26);
        logoutButton.setMaxHeight(26);
        logoutButton.setMaxWidth(Double.MAX_VALUE);

        TextField documentsSearchTextField = new TextField();
        documentsSearchTextField.setPromptText("Document search");
        documentsSearchTextField.setMinHeight(26);
        documentsSearchTextField.setMaxHeight(26);
        documentsSearchTextField.setMaxWidth(Double.MAX_VALUE);

        Button createNewDocumentButton = new Button();
        createNewDocumentButton.setText("Create new document");
        createNewDocumentButton.setMinHeight(26);
        createNewDocumentButton.setMaxHeight(26);
        createNewDocumentButton.setMaxWidth(Double.MAX_VALUE);

        TreeView<String> documentTreeView = new TreeView<>();
        documentTreeView.setMaxHeight(Double.MAX_VALUE);
        documentTreeView.setShowRoot(false);

        rootPane = new SplitPane();
        rootPane.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        rootPane.setMaxHeight(Double.MAX_VALUE);
        rootPane.setMaxWidth(Double.MAX_VALUE);
        rootPane.getItems().add(
                VBoxBuilder
                        .create()
                        .fillWidth(true)
                        .alignment(Pos.TOP_CENTER)
                        .maxHeight(Double.MAX_VALUE)
                        .spacing(10)
                        .padding(new Insets(10))
                        .children(
                                BorderPaneBuilder
                                        .create()
                                        .maxHeight(nicknameLabel.getMaxHeight())
                                        .maxWidth(Double.MAX_VALUE)
                                        .center(nicknameLabel)
                                        .right(logoutButton)
                                        .build(),
                                horizontalSeparator(),
                                documentsSearchTextField,
                                horizontalSeparator(),
                                createNewDocumentButton,
                                horizontalSeparator(),
                                documentTreeView
                        )
                        .build()
        );
        rootPane.getItems().add(TabPaneBuilder
                .create()
                .tabs(
                        TabBuilder
                                .create()
                                .text("Tab")
                                .closable(true)
                                .build()
                )
                .build()
        );
        rootPane.setDividerPositions(0.20);
    }

    @Override
    public void onLoad(@NotNull Core.ApplicationGraphicsContext<? super Node> graphicsContext) {
        graphicsContext.bind(rootPane);

        super.onLoad(graphicsContext);
    }

    @Override
    public void onUnload(@NotNull Core.ApplicationGraphicsContext<? super Node> graphicsContext) {
        graphicsContext.bind(rootPane);

        super.onUnload(graphicsContext);
    }

    private static Separator horizontalSeparator() {
        return SeparatorBuilder
                .create()
                .orientation(Orientation.HORIZONTAL)
                .build();
    }

}
