package com.myzone.archivemanager.services;

import com.google.common.collect.ImmutableMap;
import com.myzone.archivemanager.core.Core;
import com.myzone.archivemanager.core.ProcessingService;
import com.myzone.archivemanager.model.Document;
import javafx.scene.Node;
import javafx.scene.control.Label;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author myzone
 * @date 10/11/13 11:49 AM
 */
public class ContentRenderingService implements ProcessingService<ContentRenderingService.Request<?>, ContentRenderingService.Response, Object> {

    protected static final Map<Document.ContentType<?>, Function<?, Node>> RENDERERS = ImmutableMap
            .<Document.ContentType<?>, Function<?, Node>>builder()
            .put(Document.ContentType.FileContentType.INSTANCE, file -> new Label(file.toString()))
            .put(Document.ContentType.StringContentType.INSTANCE, (Function<String, Node>) Label::new)
            .build();

    @Override
    public void process(Request<?> request, @NotNull Consumer<? super Response> callback, @NotNull Core.ApplicationProcessingContext<? super Request<?>, ? extends Response, Object> processingContext) throws YieldException {
        Function<Object, Node> renderer = (Function<Object, Node>) RENDERERS.get(request.getContentType()); // @todo fix this

        if (renderer == null) {
            callback.accept((Response) Optional::empty);
        } else {
            callback.accept((Response) () -> Optional.ofNullable(renderer.apply(request.getContent())));
        }
    }

    @Override
    public void onLoad(@NotNull Core<?, ?> core) {
    }

    @Override
    public void onUnload(@NotNull Core<?, ?> core) {
    }

    public interface Request<T> {

        @NotNull
        Document.ContentType<T> getContentType();

        @NotNull
        T getContent();

    }

    public interface Response {

        Optional<Node> getResult();

    }

}
