package com.myzone.archivemanager.activities;

import com.myzone.archivemanager.core.Core;
import com.myzone.archivemanager.model.User;
import javafx.scene.Node;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.TextFieldBuilder;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * @author myzone
 * @date 9/21/13 1:13 AM
 */
public class MainMenuActivity extends StatusActivity<Node> {

    private final Core<? super Node, ?> core;
    private final User.CloseableSession session;
    private final Pane rootPane;

    public MainMenuActivity(Core<? super Node, ?> core, User.CloseableSession session) {
        this.core = core;
        this.session = session;

        Text nicknameText = TextBuilder
                .create()
                .build();

        rootPane = BorderPaneBuilder
                .create()
                .left(VBoxBuilder
                        .create()
                        .children(
                                HBoxBuilder
                                        .create()
                                        .children(
                                                nicknameText,
                                                ButtonBuilder
                                                        .create()
                                                        .text("Logout")
                                                        .onAction(System.out::println)
                                                        .build()
                                        )
                                        .build(),
                                TextFieldBuilder
                                        .create()
                                        .promptText("Document search")
                                        .build()

                                // nickname (logout)
                                // search
                                // my docs list
                        )
                        .build())
                .build();

        nicknameText.setText(session.getSessionOwner().getUsername());
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

}
