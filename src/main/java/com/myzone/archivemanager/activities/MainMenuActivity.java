package com.myzone.archivemanager.activities;

import com.myzone.archivemanager.core.Core;
import com.myzone.archivemanager.model.Document;
import com.myzone.archivemanager.model.User;
import com.myzone.archivemanager.model.simple.SimpleDocument;
import com.myzone.archivemanager.services.ContentRenderingService;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.layout.*;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author myzone
 * @date 9/21/13 1:13 AM
 */
public class MainMenuActivity extends StatusActivity<Node> {

    private final Core<? super Node, ?> core;
    private final User.CloseableSession session;
    private final ContentRenderingService contentRenderingService;

    private final SplitPane rootPane;

    private Label nicknameLabel;
    private Button logoutButton;
    private TextField documentsSearchTextField;
    private Button createNewDocumentButton;
    private TreeView<DocumentTreeNode> documentTreeView;
    private TabPane tabsView;

    public MainMenuActivity(Core<? super Node, ?> core, User.CloseableSession session, ContentRenderingService contentRenderingService) {
        this.core = core;
        this.session = session;
        this.contentRenderingService = contentRenderingService;

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
                    SortedSet<? extends Document.Revision<?>> revisions = currentNode.getDocument().getRevisions(session);

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

    @Override
    public void onLoad(@NotNull Core.ApplicationGraphicsContext<? super Node> graphicsContext) {
        super.onLoad(graphicsContext);

        graphicsContext.bind(rootPane);

        SimpleDocument simpleDocument = new SimpleDocument<String>(
                "u suck",
                session.getOwner(),
                "u suck, bitch!!!",
                Document.ContentType.StringContentType.INSTANCE,
                () -> "bullshit"
        );

        Document.Revision r = (Document.Revision) simpleDocument.getRevisions(session).last();
        r.comment(session, "U2, motherfucker!!!");


        Set<SimpleDocument<?>> documents = new HashSet<>();
        documents.add(simpleDocument);

        List<TreeItem<DocumentTreeNode>> documentsTreeItems = documents
                .stream()
                .map(document -> {
                    TreeItem<DocumentTreeNode> treeItem = new TreeItem<>(new DocumentTreeNode(document));

                    // todo fix this holy shit about generics
                    List<TreeItem<DocumentTreeNode>> revisionTreeItems = document
                            .getRevisions(session)
                            .stream()
                            .map((revision) -> new DocumentTreeNode(revision))
                            .map(TreeItem::new)
                            .collect(Collectors.toList());

                    treeItem.getChildren().setAll(revisionTreeItems);

                    return treeItem;
                })
                .collect(Collectors.toList());

        documentTreeView.getRoot().getChildren().setAll(documentsTreeItems);

//        tabsView.getTabs().add();
    }

    @Override
    public void onUnload(@NotNull Core.ApplicationGraphicsContext<? super Node> graphicsContext) {
        graphicsContext.unbind(rootPane);

        super.onUnload(graphicsContext);
    }

    private <T> Tab createTab(@NotNull Document.Revision<T> revision) {
        Tab result = new Tab();

        StackPane documentContentViewWrapper = new StackPane();
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
        Label authorLabel = new Label();
        authorLabel.setText(revision.getAuthor().getUsername());

        Label authorTextLabel = new Label();
        authorTextLabel.setText("Author");
        authorTextLabel.setLabelFor(authorLabel);

        Label creationTimeLabel = new Label();
        creationTimeLabel.setText(revision.getCreationTime().toString());

        Label creationTimeTextLabel = new Label();
        creationTimeTextLabel.setText("Creation time");
        creationTimeTextLabel.setLabelFor(creationTimeLabel);

        documentMetadataView.addRow(0, authorTextLabel, authorLabel);
        documentMetadataView.addRow(1, creationTimeTextLabel, creationTimeLabel);

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

        VBox tabRoot = new VBox();
        tabRoot.setFillWidth(true);
        tabRoot.setMaxHeight(Double.MAX_VALUE);
        tabRoot.getChildren().add(documentContentViewWrapper);
        tabRoot.getChildren().add(horizontalSeparator());
        tabRoot.getChildren().add(documentMetadataView);
        tabRoot.getChildren().add(horizontalSeparator());
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
