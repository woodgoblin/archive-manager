package com.myzone.archive.core;

import com.myzone.archive.data.DataAccessor;
import com.myzone.archive.model.Document;
import com.myzone.archive.model.User;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author myzone
 * @date 9/6/13 11:51 AM
 */
public class JavaFxBasedCore implements Core<Node> {

    private final Pane rootNode;
    private final ApplicationGraphicsContext<Node> applicationGraphicsContext;

    public JavaFxBasedCore(Pane rootNode) {
        this.rootNode = rootNode;
        this.applicationGraphicsContext = new ApplicationGraphicsContext<Node>() {

            @Override
            public void bind(Node node) {
                rootNode.getChildren().add(node);
            }

            @Override
            public void unbind(Node node) {
                rootNode.getChildren().remove(node);
            }

        };
    }

    Set<User> users = new HashSet<>();

    @Override
    public <A, R> R processRequest(Service<? super A, ? extends R> service, A request) {
        return service.process(request, new ApplicationDataContext() {

            @Override
            public DataAccessor<User> getUserDataAccessor() {
                return () -> new DataAccessor.Transaction<User>() {

                    Set<User> tmp = new HashSet<>(users);

                    @Override
                    public Stream<User> getAll() throws DataAccessor.DataAccessException {
                        return tmp.stream();
                    }

                    @Override
                    public void save(User o) throws DataAccessor.DataAccessException {
                        tmp.add(o);
                    }

                    @Override
                    public void update(User o) throws DataAccessor.DataAccessException {
                    }

                    @Override
                    public void delete(User o) throws DataAccessor.DataAccessException {
                        tmp.remove(o);
                    }

                    @Override
                    public void commit() throws DataAccessor.DataModificationException {
                        users = tmp;
                    }

                    @Override
                    public void rollback() {
                    }
                };
            }

            @Override
            public DataAccessor<Document> getDocumentDataAccessor() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        });
    }

    @Override
    public void loadActivity(Activity<? extends Node> activity) {
        activity.onLoad(applicationGraphicsContext);
    }

    @Override
    public void unloadActivity(Activity<? extends Node> activity) {
        activity.onUnload(applicationGraphicsContext);
    }

    @Override
    public void loadService(Service<?, ?> service) {

    }

    @Override
    public void unloadService(Service<?, ?> service) {

    }

}
