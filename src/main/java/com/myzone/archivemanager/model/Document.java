package com.myzone.archivemanager.model;

import java.time.Clock;
import java.util.SortedSet;

import static com.myzone.archivemanager.model.User.AuthorizedSession;

/**
 * @author myzone
 * @date 9/6/13 10:23 AM
 */
public interface Document<C> {

    String getName();

    ContentType getContentType();

    SortedSet<Revision<C>> getRevisions();

    void updateBy(AuthorizedSession authorizedSession, C content);

    interface Revision<C> {

        User getAuthor();

        Clock getCreationTime();

        C getContent();

        SortedSet<Comment> getComments();

    }

    interface Comment {

        User getAuthor();

        Clock getCreationTime();

        String getText();

        SortedSet<Comment> getAnswers();

    }

    enum ContentType {
        FILE;
    }

}
