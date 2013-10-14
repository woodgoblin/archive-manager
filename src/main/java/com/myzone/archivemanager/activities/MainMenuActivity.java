package com.myzone.archivemanager.activities;

import com.myzone.archivemanager.core.Core;
import com.myzone.archivemanager.model.Document;
import com.myzone.archivemanager.model.User;
import com.myzone.archivemanager.model.simple.SimpleDocument;
import com.myzone.archivemanager.services.ContentRenderingService;
import com.myzone.archivemanager.services.GlobalsService;
import com.myzone.utils.SynchronizedConsumer;
import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

/**
 * @author myzone
 * @date 9/21/13 1:13 AM
 */
public class MainMenuActivity extends StatusActivity<Node> {

    private static final Color INVERSE_BACKGROUND_COLOR = Color.DARKGRAY;
    private static final Color INVERSE_FOREGROUND_COLOR = Color.GHOSTWHITE;

    private final Core<? super Node, ?> core;
    private final ContentRenderingService contentRenderingService;

    private final GlobalsService.Globals globals;
    private final ScheduledExecutorService scheduler;

    private SplitPane rootPane;

    private Label nicknameLabel;
    private Button logoutButton;
    private TextField documentsSearchTextField;
    private Button createNewDocumentButton;
    private TreeView<DocumentTreeNode> documentTreeView;
    private TabPane tabsView;

    public MainMenuActivity(Core<? super Node, ?> core, GlobalsService globalsService, ContentRenderingService contentRenderingService) {
        this.core = core;
        this.contentRenderingService = contentRenderingService;

        SynchronizedConsumer<GlobalsService.Globals> globalsConsumer = new SynchronizedConsumer<>();
        core.processRequest(globalsService, null, globalsConsumer);
        globals = globalsConsumer.get();
        scheduler = newSingleThreadScheduledExecutor();

        initUi();
    }

    @Override
    public void onLoad(@NotNull Core.ApplicationGraphicsContext<? super Node> graphicsContext) {
        super.onLoad(graphicsContext);

        graphicsContext.bind(rootPane);

        fillWithData(globals.getCurrentSession().getValue().get());
    }

    @Override
    public void onUnload(@NotNull Core.ApplicationGraphicsContext<? super Node> graphicsContext) {
        graphicsContext.unbind(rootPane);

        super.onUnload(graphicsContext);
    }

    private void initUi() {
        User.CloseableSession currentSession = globals.getCurrentSession().getValue().get();

        nicknameLabel = new Label();
        nicknameLabel.setText(currentSession.getOwner().getUsername());
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
        documentTreeView.setCellFactory(new Callback<TreeView<DocumentTreeNode>, TreeCell<DocumentTreeNode>>() {
            @Override
            public TreeCell<DocumentTreeNode> call(TreeView<DocumentTreeNode> documentTreeNodeTreeView) {
                return new TextFieldTreeCell<>(new StringConverter<DocumentTreeNode>() {
                    @Override
                    public String toString(DocumentTreeNode node) {
                        if (node.isDocument()) {
                            return node.getDocument().getName();
                        } else if (node.isRevision()) {
                            Document.Revision revision = node.getRevision();

                            return revision.getAuthor().getUsername() + " @ " + revision.getCreationTime().toString();
                        } else {
                            return "root";
                        }
                    }

                    @Override
                    public DocumentTreeNode fromString(String s) {
                        return null;
                    }

                });
            }
        });
        documentTreeView.setRoot(new TreeItem<DocumentTreeNode>(new DocumentTreeNode<>()));
        documentTreeView.setOnMouseClicked((event) -> {
            if (event.getClickCount() > 1) {
                DocumentTreeNode currentNode = documentTreeView.getSelectionModel().getSelectedItems().get(0).getValue();

                if (currentNode.isDocument()) {
                    SortedSet<? extends Document.Revision<?>> revisions = currentNode.getDocument().getRevisions(globals.getCurrentSession().getValue().get());

                    if (!revisions.isEmpty()) {
                        tabsView.getTabs().add(createTab(revisions.last()));
                    }
                } else if (currentNode.isRevision()) {
                    tabsView.getTabs().add(createTab(currentNode.getRevision()));
                }
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

    private void fillWithData(User.AuthorizedSession currentSession) {
        /* data hardcode fixme */
        SimpleDocument simpleDocument = new SimpleDocument<>(
                "u suck",
                currentSession.getOwner(),
                "u suck, bitch!!!",
                Document.ContentType.StringContentType.INSTANCE,
                () -> "bullshit"
        );

        Document.Revision r = (Document.Revision) simpleDocument.getRevisions(currentSession).last();
        r.comment(currentSession, "U2, motherfucker!!!");
        r.comment(currentSession, "Когда я рассматривал покупку двухколесной техники, я достаточно долгое время сравнивал по дизайну и характеристикам несколько разных мотоциклов, чтобы решить для себя, на какой аппарат ориентироваться в дальнейшем, и с какого мотосалона начинать более пристальное изучение.");

        Set<SimpleDocument<?>> documents = new HashSet<>();
        documents.add(simpleDocument);

        List<TreeItem<DocumentTreeNode>> documentsTreeItems = documents
                .stream()
                .map(document -> {
                    TreeItem<DocumentTreeNode> treeItem = new TreeItem<>(new DocumentTreeNode(document));

                    // todo fix this holy shit about generics
                    List<TreeItem<DocumentTreeNode>> revisionTreeItems = document
                            .getRevisions(currentSession)
                            .stream()
                            .map((revision) -> new DocumentTreeNode(revision))
                            .map(TreeItem::new)
                            .collect(Collectors.toList());

                    treeItem.getChildren().setAll(revisionTreeItems);

                    return treeItem;
                })
                .collect(Collectors.toList());

        documentTreeView.getRoot().getChildren().setAll(documentsTreeItems);
    }

    private <T> Tab createTab(@NotNull Document.Revision<T> revision) {
        Tab result = new Tab();

        StackPane documentContentViewWrapper = new StackPane();
        documentContentViewWrapper.setPadding(new Insets(5));
        documentContentViewWrapper.setMaxHeight(Double.MAX_VALUE);
        documentContentViewWrapper.setMaxWidth(Double.MAX_VALUE);
        documentContentViewWrapper.getChildren().add(
                StackPaneBuilder
                        .create()
                        .prefHeight(120)
                        .alignment(Pos.CENTER)
                        .children(new Label("Loading..."))
                        .build()
        );

        core.processRequest(contentRenderingService, new ContentRenderingService.Request<T>() {

            @NotNull
            @Override
            public Document.ContentType<T> getContentType() {
                return revision.getDocument().getContentType();
            }

            @NotNull
            @Override
            public T getContent() {
                return revision.getContent();
            }

        }, (response) -> {
            response.getResult().ifPresent(documentContentViewWrapper.getChildren()::setAll);
        });

        GridPane documentMetadataView = new GridPane();
        documentMetadataView.setPadding(new Insets(5));
        documentMetadataView.setHgap(5);
        documentMetadataView.setVgap(5);
        documentMetadataView.setBackground(new Background(new BackgroundFill(INVERSE_BACKGROUND_COLOR, new CornerRadii(0), new Insets(0))));

        Label authorLabel = new Label();
        authorLabel.setText(revision.getAuthor().getUsername());
        authorLabel.setTextFill(INVERSE_FOREGROUND_COLOR);

        Label authorTextLabel = new Label();
        authorTextLabel.setText("Author");
        authorTextLabel.setLabelFor(authorLabel);
        authorTextLabel.setTextFill(INVERSE_FOREGROUND_COLOR);

        Label creationTimeLabel = new Label();
        creationTimeLabel.setText(revision.getCreationTime().toString());
        creationTimeLabel.setTextFill(INVERSE_FOREGROUND_COLOR);

        Label creationTimeTextLabel = new Label();
        creationTimeTextLabel.setText("Creation time");
        creationTimeTextLabel.setLabelFor(creationTimeLabel);
        creationTimeTextLabel.setTextFill(INVERSE_FOREGROUND_COLOR);

        documentMetadataView.addRow(0, authorTextLabel, authorLabel);
        documentMetadataView.addRow(1, creationTimeTextLabel, creationTimeLabel);

        ListView<Document.Comment> commentsView = new ListView<>();
        commentsView.setPadding(new Insets(0, 5, 0, 5));
        commentsView.setCellFactory(new Callback<ListView<Document.Comment>, ListCell<Document.Comment>>() {
            @Override
            public ListCell<Document.Comment> call(ListView<Document.Comment> commentListView) {
                return new ListCell<Document.Comment>() {

                    @Override
                    protected void updateItem(Document.Comment comment, boolean empty) {
                        if (empty) {
                            setGraphic(null);
                        } else {
                            BorderPane commentRoot = new BorderPane();

                            TextArea commentTextArea = new TextArea();
                            commentTextArea.setText(comment.getText());
                            commentTextArea.setEditable(false);
                            commentTextArea.setWrapText(true);
                            commentTextArea.setPrefHeight(52);
                            commentTextArea.setMaxHeight(Double.MAX_VALUE);

                            Label usernameLabel = new Label();
                            usernameLabel.setPadding(new Insets(5));
                            usernameLabel.setText(comment.getAuthor().getUsername());
                            usernameLabel.setLabelFor(commentTextArea);

                            Label dateAndTimeLabel = new Label();
                            dateAndTimeLabel.setPadding(new Insets(5));
                            dateAndTimeLabel.setText(comment.getCreationTime().toString());
                            dateAndTimeLabel.setLabelFor(commentTextArea);

                            commentRoot.setCenter(commentTextArea);
                            commentRoot.setRight(
                                    VBoxBuilder
                                            .create()
                                            .fillWidth(true)
                                            .children(usernameLabel, dateAndTimeLabel)
                                            .build()
                            );

                            setGraphic(commentRoot);
                        }

                        super.updateItem(comment, empty);
                    }

                };
            }
        });
        commentsView.getItems().setAll(revision.getComments());

        BorderPane commentInputView = new BorderPane();

        TextArea commentInputArea = new TextArea();
        commentInputArea.setPromptText("Enter your comment");
        commentInputArea.setEditable(false);
        commentInputArea.setWrapText(true);
        commentInputArea.setPrefHeight(52);
        commentInputArea.setMaxHeight(Double.MAX_VALUE);

        Label usernameLabel = new Label();
        usernameLabel.setPadding(new Insets(5));
        usernameLabel.setText(globals.getCurrentSession().getValue().get().getOwner().getUsername());
        usernameLabel.setLabelFor(commentInputArea);

        Label dateAndTimeLabel = new Label();
        dateAndTimeLabel.setPadding(new Insets(5));
        dateAndTimeLabel.setLabelFor(commentInputArea);
        scheduler.scheduleAtFixedRate(() -> {
                Platform.runLater(() -> dateAndTimeLabel.setText(globals.getCurrentClock().getValue().instant().toString()));
        }, 0, 50, TimeUnit.MILLISECONDS);

        commentInputView.setCenter(commentInputArea);
        commentInputView.setRight(
                VBoxBuilder
                        .create()
                        .fillWidth(true)
                        .children(usernameLabel, dateAndTimeLabel)
                        .build()
        );

        VBox tabRoot = new VBox();
        tabRoot.setFillWidth(true);
        tabRoot.setMaxHeight(Double.MAX_VALUE);
        tabRoot.getChildren().add(documentContentViewWrapper);
        tabRoot.getChildren().add(horizontalSeparator());
        tabRoot.getChildren().add(documentMetadataView);
        tabRoot.getChildren().add(horizontalSeparator());
        tabRoot.getChildren().add(commentsView);
        tabRoot.getChildren().add(commentInputView);

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

        private DocumentTreeNode() {
            this.document = Optional.empty();
            this.revision = Optional.empty();
        }

        private DocumentTreeNode(@NotNull Document<T> document) {
            this.document = Optional.of(document);
            this.revision = Optional.empty();
        }

        private DocumentTreeNode(@NotNull Document.Revision<T> revision) {
            this.document = Optional.empty();
            this.revision = Optional.of(revision);
        }

        public boolean isRoot() {
            return !isDocument() && !isRevision();
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
