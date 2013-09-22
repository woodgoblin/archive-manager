package com.myzone.archivemanager.model;

import com.google.common.collect.ImmutableSortedSet;
import org.jetbrains.annotations.NotNull;

import java.time.Clock;
import java.util.SortedSet;

import static com.myzone.archivemanager.model.User.AuthorizedSession;

/**
 * @author myzone
 * @date 9/6/13 10:23 AM
 */
public interface Document<C> {

    @NotNull
    String getName();

    @NotNull
    ContentType getContentType();

    @NotNull
    DocumentType getDocumentType();

    @NotNull
    SortedSet<Revision<C>> getRevisions(@NotNull AuthorizedSession authorizedSession);

    void updateBy(@NotNull AuthorizedSession authorizedSession, C content);

    boolean isReadableBy(@NotNull User user);

    boolean isWritableBy(@NotNull User user);

    boolean isCommentableBy(@NotNull User user);

    interface Revision<C> {

        @NotNull
        User getAuthor();

        @NotNull
        Clock getCreationTime();

        @NotNull
        SortedSet<Comment> getComments();

        void comment(@NotNull AuthorizedSession authorizedSession, @NotNull String commentText);

        void commentFor(@NotNull AuthorizedSession authorizedSession, @NotNull Comment cause, @NotNull String commentText);

        void removeComment(@NotNull AuthorizedSession authorizedSession, @NotNull String commentText);

        C getContent();

    }

    interface Comment {

        @NotNull
        User getAuthor();

        @NotNull
        Clock getCreationTime();

        @NotNull
        String getText();

        @NotNull
        SortedSet<Comment> getAnswers();

    }

    interface DocumentType {

        @NotNull
        String getName();

    }

    enum ContentType {

        FILE,
        UNKNOWN;

    }

}
