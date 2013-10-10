package com.myzone.archivemanager.activities;

import com.myzone.archivemanager.core.Core;
import com.myzone.archivemanager.core.Service;
import com.myzone.archivemanager.model.Document;
import com.myzone.archivemanager.model.User;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.SortedSet;
import java.util.stream.Collectors;

/**
 * @author myzone
 * @date 9/21/13 1:13 AM
 */
public class MainMenuActivity extends StatusActivity<Node> {

    private final Core<? super Node, ?> core;
    private final User.CloseableSession session;
    private final SplitPane rootPane;

    private Label nicknameLabel;
    private Button logoutButton;
    private TextField documentsSearchTextField;
    private Button createNewDocumentButton;
    private TreeView<DocumentTreeNode<?>> documentTreeView;
    private TabPane tabsView;

    public MainMenuActivity(Core<? super Node, ?> core, User.CloseableSession session) {
        this.core = core;
        this.session = session;

        nicknameLabel = new Label();
        nicknameLabel.setText(session.getOwner().getUsername());
        nicknameLabel.setMinHeight(26);
        nicknameLabel.setMaxHeight(26);
        nicknameLabel.setMaxWidth(Double.MAX_VALUE);

        logoutButton = new Button();
        logoutButton.setText("Logout");
        logoutButton.setMinHeight(26);
        logoutButton.setMaxHeight(26);
        logoutButton.setMaxWidth(Double.MAX_VALUE);

        documentsSearchTextField = new TextField();
        documentsSearchTextField.setPromptText("Document search");
        documentsSearchTextField.setMinHeight(26);
        documentsSearchTextField.setMaxHeight(26);
        documentsSearchTextField.setMaxWidth(Double.MAX_VALUE);

        createNewDocumentButton = new Button();
        createNewDocumentButton.setText("Create new document");
        createNewDocumentButton.setMinHeight(26);
        createNewDocumentButton.setMaxHeight(26);
        createNewDocumentButton.setMaxWidth(Double.MAX_VALUE);

        documentTreeView = new TreeView<>();
        documentTreeView.setMaxHeight(Double.MAX_VALUE);
        documentTreeView.setShowRoot(false);
        documentTreeView.setCellFactory(new Callback<TreeView<DocumentTreeNode<?>>, TreeCell<DocumentTreeNode<?>>>() {
            @Override
            public TreeCell<DocumentTreeNode<?>> call(TreeView<DocumentTreeNode<?>> documentTreeNodeTreeView) {
                return new TreeCell<DocumentTreeNode<?>>() {

                    @Override
                    protected void updateItem(DocumentTreeNode<?> node, boolean empty) {
                        super.updateItem(node, empty);

                        if (empty) {
                            setText(null);
                        } else {
                            if (node.isDocument()) {
                                Document<?> document = node.getDocument();

                                setText(document.getName());
                            } else if (node.isRevision()) {
                                Document.Revision<?> revision = node.getRevision();

                                setText(revision.getCreationTime().toString());
                                setTextFill(revision.getAuthor().equals(session.getOwner()) ? Color.DARKGRAY : Color.BLACK);
                            }
                        }
                    }

                    @Override
                    public void startEdit() {
                        // do nothing
                    }

                };
            }
        });
        documentTreeView.setOnEditStart(event -> {
            DocumentTreeNode<?> currentNode = event.getTreeItem().getValue();

            if (currentNode.isDocument()) {
                SortedSet<? extends Document.Revision<?>> revisions = currentNode.getDocument().getRevisions(session);

                if (!revisions.isEmpty()) {
                    createTab(revisions.last());
                }
            } else if (currentNode.isRevision()) {
                createTab(currentNode.getRevision());
            }
        });

        tabsView = new TabPane();
        tabsView.setSide(Side.TOP);
        tabsView.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        tabsView.setTabMaxHeight(Double.MAX_VALUE);
        tabsView.setTabMaxWidth(Double.MAX_VALUE);

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
        rootPane.getItems().add(tabsView);
        rootPane.setDividerPositions(0.20);
    }

    @Override
    public void onLoad(@NotNull Core.ApplicationGraphicsContext<? super Node> graphicsContext) {
        super.onLoad(graphicsContext);

        graphicsContext.bind(rootPane);

//        tabsView.getTabs().add(createTab());
    }

    @Override
    public void onUnload(@NotNull Core.ApplicationGraphicsContext<? super Node> graphicsContext) {
        graphicsContext.unbind(rootPane);

        super.onUnload(graphicsContext);
    }

    private Tab createTab(@NotNull Document.Revision<?> revision) {
        return new Tab();
    }

    private <T> Tab createTab(@NotNull Document.Revision<T> revision,@NotNull Service<T, Node> rendererService) {
        Tab result = new Tab();

        VBox tabRoot = new VBox();

        StackPane documentContentViewWrapper = new StackPane();
        tabRoot.setMaxHeight(Double.MAX_VALUE);
        tabRoot.setMaxWidth(Double.MAX_VALUE);
        tabRoot.getChildren().add(
                StackPaneBuilder
                        .create()
                        .prefHeight(120)
                        .alignment(Pos.CENTER)
                        .children(new Label("Loading..."))
                        .build()
        );

//        core.processRequest(rendererService, revision.getContent(), (documentContentView) -> {
//            tabRoot.getChildren().clear();
//            tabRoot.getChildren().add(documentContentView);
//        });

        GridPane documentMetadataView = new GridPane();
        documentMetadataView.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY,new CornerRadii(1), new Insets(-50, -10, 100, 20))));

        VBox commentsView = new VBox();
        commentsView.setFillWidth(true);
        commentsView.getChildren().addAll(
                revision
                        .getComments()
                        .stream()
                        .map((comment) -> {
                            BorderPane commentRoot = new BorderPane();

                            Label commentTextLabel = new Label();
                            commentTextLabel.setText(comment.getText());

                            Label usernameLabel = new Label();
                            usernameLabel.setText(comment.getAuthor().getUsername());
                            usernameLabel.setLabelFor(commentTextLabel);

                            Label dateAndTimeLabel = new Label();
                            dateAndTimeLabel.setText(comment.getCreationTime().toString());
                            dateAndTimeLabel.setLabelFor(commentTextLabel);

                            commentRoot.setCenter(commentTextLabel);
                            commentRoot.setRight(
                                    VBoxBuilder
                                            .create()
                                            .fillWidth(true)
                                            .children(usernameLabel, dateAndTimeLabel)
                                            .build()
                            );

                            return commentRoot;
                        })
                        .collect(Collectors.<Node>toList())
        );

        tabRoot.setFillWidth(true);
        tabRoot.setMaxHeight(Double.MAX_VALUE);
        tabRoot.getChildren().add(documentContentViewWrapper);
        tabRoot.getChildren().add(documentMetadataView);
        tabRoot.getChildren().add(commentsView);

        result.setText(revision.getDocument().getName());
        result.setDisable(false);
        result.setClosable(true);
        result.setContent(tabRoot);
        
        return result;
    }

    private static Separator horizontalSeparator() {
        return SeparatorBuilder
                .create()
                .orientation(Orientation.HORIZONTAL)
                .build();
    }

    private static class DocumentTreeNode<T> {

        private final Optional<Document<T>> document;

        private final Optional<Document.Revision<T>> revision;

        private DocumentTreeNode(@NotNull Document<T> document) {
            this.document = Optional.of(document);
            this.revision = Optional.empty();
        }

        private DocumentTreeNode(@NotNull Document.Revision<T> revision) {
            this.document = Optional.empty();
            this.revision = Optional.of(revision);
        }

        public boolean isDocument() {
            return document.isPresent();
        }

        boolean isRevision() {
            return revision.isPresent();
        }

        @NotNull
        private Document<T> getDocument() {
            return document.get();
        }

        @NotNull
        private Document.Revision<T> getRevision() {
            return revision.get();
        }

    }

}
