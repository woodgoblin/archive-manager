package com.myzone.archivemanager.model;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.time.Instant;
import java.util.Comparator;
import java.util.NavigableSet;
import java.util.Optional;

import static com.myzone.archivemanager.model.User.AuthorizedSession;

/**
 * @author myzone
 * @date 9/6/13 10:23 AM
 */
public interface Document<C> {

    @NotNull
    String getName();

    @NotNull
    ContentType<C> getContentType();

    @NotNull
    DocumentType getDocumentType();

    @NotNull
    NavigableSet<Revision<C>> getRevisions(@NotNull AuthorizedSession authorizedSession);

    void updateBy(@NotNull AuthorizedSession authorizedSession, C content);

    boolean isReadableBy(@NotNull User user);

    boolean isWritableBy(@NotNull User user);

    boolean isCommentableBy(@NotNull User user);

    interface Revision<C> extends Comparable<Revision<C>> {

        @NotNull
        Document<C> getDocument();

        @NotNull
        User getAuthor();

        @NotNull
        Instant getCreationTime();

        @NotNull
        NavigableSet<Comment> getComments();

        void comment(@NotNull AuthorizedSession authorizedSession, @NotNull String commentText);

        void commentFor(@NotNull AuthorizedSession authorizedSession, @NotNull Comment cause, @NotNull String commentText);

        void removeComment(@NotNull AuthorizedSession authorizedSession, @NotNull Comment comment);

        C getContent();

        default int compareTo(Revision<C> other) {
            return Comparators.BY_CREATION_TIME.compare(this, other);
        }

        enum Comparators implements Comparator<Revision<?>> {

            BY_CREATION_TIME;

            @Override
            public int compare(Revision<?> left, Revision<?> right) {
                return left.getCreationTime().compareTo(right.getCreationTime());
            }

        }

    }

    interface Comment extends Comparable<Comment> {

        @NotNull
        User getAuthor();

        @NotNull
        Instant getCreationTime();

        @NotNull
        String getText();

        @NotNull
        Optional<Comment> getCause();

        @NotNull
        NavigableSet<Comment> getAnswers();

        default int compareTo(Comment other) {
            return Comparators.BY_CREATION_TIME.compare(this, other);
        }

        enum Comparators implements Comparator<Comment> {

            BY_CREATION_TIME;

            @Override
            public int compare(Comment left, Comment right) {
                return left.getCreationTime().compareTo(right.getCreationTime());
            }

        }

    }

    interface DocumentType {

        @NotNull
        String getName();

    }

    interface ContentType<T> {

        @NotNull
        String getName();

        @NotNull
        Class<T> contentsClass();

        enum StringContentType implements ContentType<String> {

            INSTANCE;

            @NotNull
            @Override
            public String getName() {
                return "Text";
            }

            @NotNull
            @Override
            public Class<String> contentsClass() {
                return String.class;
            }

        }

        enum FileContentType implements ContentType<File> {

            INSTANCE;

            @NotNull
            @Override
            public String getName() {
                return "File";
            }

            @NotNull
            @Override
            public Class<File> contentsClass() {
                return File.class;
            }

        }

    }

}
