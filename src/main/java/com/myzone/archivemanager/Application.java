package com.myzone.archivemanager;

import com.myzone.archivemanager.activities.MainMenuActivity;
import com.myzone.archivemanager.activities.StatusActivity;
import com.myzone.archivemanager.activities.UserAuthorisationActivity;
import com.myzone.archivemanager.core.DataService;
import com.myzone.archivemanager.core.GreenThreadCoreFactory;
import com.myzone.archivemanager.core.JavaFxBasedCore;
import com.myzone.archivemanager.data.DataAccessor;
import com.myzone.archivemanager.data.InMemoryDataAccessor;
import com.myzone.archivemanager.model.Document;
import com.myzone.archivemanager.model.User;
import com.myzone.utils.RecursiveImmutableTuple;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import static com.myzone.archivemanager.core.Core.ApplicationDataContext;
import static com.myzone.archivemanager.core.Core.DataProvider;
import static com.myzone.archivemanager.core.Core.DataProvider.DataProviderEnd;
import static com.myzone.archivemanager.core.JavaFxBasedCore.binder;
import static com.myzone.archivemanager.core.ScheduledCore.DataContextProvider;
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

        RecursiveDataProvider dataProvider = new RecursiveDataProvider<User, DataProvider<Document, DataProviderEnd>>(
                new InMemoryDataAccessor<>(User.class),
                new RecursiveDataProvider<>(
                        new InMemoryDataAccessor<>(Document.class),
                        DataProviderEnd.END
                )
        );

        JavaFxBasedCore<DataProvider<User, DataProvider<Document, DataProviderEnd>>> core = new JavaFxBasedCore<DataProvider<User, DataProvider<Document, DataProviderEnd>>>(
                new GreenThreadCoreFactory<>(),
                new DataContextProvider<DataProvider<User, DataProvider<Document, DataProviderEnd>>>() {
                    @NotNull
                    @Override
                    public <A, R> ApplicationDataContext<? extends DataProvider<User, DataProvider<Document, DataProviderEnd>>> provide(@NotNull DataService<A, R, ? super DataProvider<User, DataProvider<Document, DataProviderEnd>>> dataService) {
                        return  new ApplicationDataContext<DataProvider<User, DataProvider<Document, DataProviderEnd>>>() {
                            @NotNull
                            @Override
                            public DataProvider<User, DataProvider<Document, DataProviderEnd>> getDataProvider() {
                                return dataProvider;
                            }
                        };
                    }
                }
        );

        UserAuthorisationActivity userAuthorisationActivity = new UserAuthorisationActivity(core);
        userAuthorisationActivity.getStatus().addListener((observableValue, oldStatus, newStatus) -> {
            if (newStatus == StatusActivity.Status.DONE) {
                try {
                    core.unloadActivity(userAuthorisationActivity, rootPane, binder());

                    Pane mainMenuPane = new Pane();
                    Stage mainMenuStage = new Stage();
                    mainMenuStage.setScene(new Scene(mainMenuPane, 1200, 800));

                    MainMenuActivity menuActivity = new MainMenuActivity(core, userAuthorisationActivity.getSession());
                    core.loadActivity(menuActivity, mainMenuPane, binder());

                    mainMenuStage.show();
                    stage.hide();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        });
        core.loadActivity(userAuthorisationActivity, rootPane, binder());

        stage.setOnCloseRequest(e -> System.exit(0));
        runLater(stage::show);
    }

    public static void main(String[] args) {
        javafx.application.Application.launch(args);
    }

    public static class RecursiveDataProvider<D, T extends DataProvider> extends RecursiveImmutableTuple<DataAccessor<D>, T> implements DataProvider<D, T> {

        public RecursiveDataProvider(DataAccessor<D> data, T next) {
            super(data, next);
        }

    }

}