package com.myzone.archivemanager.model.simple;

import com.google.common.base.Objects;
import com.myzone.archivemanager.model.Document;
import com.myzone.archivemanager.model.User;
import com.myzone.utils.bindings.ObservableNavigableSet;
import com.myzone.utils.bindings.ObservableNavigableSetWrapper;
import com.myzone.utils.bindings.ReadOnlyObservableNavigableSetWrapper;
import org.jetbrains.annotations.NotNull;

import java.time.Clock;
import java.time.Instant;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

import static java.util.Collections.emptyNavigableSet;
import static java.util.Collections.unmodifiableNavigableSet;

/**
 * @author myzone
 * @date 10/11/13 2:34 AM
 */
public class SimpleDocument<C> implements Document<C> {

    private final String name;
    private volatile ContentType contentType;
    private volatile DocumentType documentType;
    private final ObservableNavigableSet<Revision<C>> revisions;

    public SimpleDocument(String name, User creator, C content, ContentType contentType, DocumentType documentType) {
        this.name = name;
        this.contentType = contentType;
        this.documentType = documentType;
        this.revisions = new ObservableNavigableSetWrapper<>(new TreeSet<>());

        revisions.add(new SimpleRevision(
                creator,
                Clock.systemDefaultZone().instant(),
                content
        ));
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @NotNull
    @Override
    public ContentType<C> getContentType() {
        return contentType;
    }

    @NotNull
    @Override
    public DocumentType getDocumentType() {
        return documentType;
    }

    @NotNull
    @Override
    public ObservableNavigableSet<Revision<C>> getRevisions(@NotNull User.AuthorizedSession authorizedSession) {
        if (!isReadableBy(authorizedSession.getOwner())) {
            return new ObservableNavigableSetWrapper<>(emptyNavigableSet());
        }

        return new ReadOnlyObservableNavigableSetWrapper<>(revisions);
    }

    @Override
    public void updateBy(@NotNull User.AuthorizedSession authorizedSession, C content) {
        if (isWritableBy(authorizedSession.getOwner())) {
            revisions.add(new SimpleRevision(
                    authorizedSession.getOwner(),
                    Clock.systemDefaultZone().instant(),
                    content
            ));
        }
    }

    @Override
    public boolean isReadableBy(@NotNull User user) {
        return true; // temporary @fixme
    }

    @Override
    public boolean isWritableBy(@NotNull User user) {
        return true; // temporary @fixme
    }

    @Override
    public boolean isCommentableBy(@NotNull User user) {
        return true; // temporary @fixme
    }

    public class SimpleRevision implements Revision<C> {

        protected final User author;
        protected final Instant creationTime;
        protected final ObservableNavigableSet<Comment> comments;
        protected final C content;

        public SimpleRevision(User author, Instant creationTime, C content) {
            this.author = author;
            this.creationTime = creationTime;
            this.comments = new ObservableNavigableSetWrapper<>(new TreeSet<>());
            this.content = content;
        }

        @NotNull
        @Override
        public Document<C> getDocument() {
            return SimpleDocument.this;
        }

        @NotNull
        @Override
        public User getAuthor() {
            return author;
        }

        @NotNull
        @Override
        public Instant getCreationTime() {
            return creationTime;
        }

        @NotNull
        @Override
        public ObservableNavigableSet<Comment> getComments() {
            return new ReadOnlyObservableNavigableSetWrapper<>(comments);
        }

        @Override
        public void comment(@NotNull User.AuthorizedSession authorizedSession, @NotNull String commentText) {
            if (isCommentableBy(authorizedSession.getOwner())) {
                comments.add(new SimpleComment(
                        authorizedSession.getOwner(),
                        Clock.systemDefaultZone().instant(),
                        commentText
                ));
            }
        }

        @Override
        public void commentFor(@NotNull User.AuthorizedSession authorizedSession, @NotNull Comment cause, @NotNull String commentText) {
            if (isOurComment(cause) && isCommentableBy(authorizedSession.getOwner())) {
                ((SimpleComment) cause).addAnswer(new SimpleComment(
                        authorizedSession.getOwner(),
                        Clock.systemDefaultZone().instant(),
                        commentText,
                        cause
                ));
            }
        }

        @Override
        public void removeComment(@NotNull User.AuthorizedSession authorizedSession, @NotNull Comment comment) {
            if (isOurComment(comment) && isCommentableBy(authorizedSession.getOwner()) && comment.getAuthor().equals(authorizedSession.getOwner())) {
                if (!comment.getCause().isPresent()) {
                    if (isLastOf(comments, comment)) {
                        comments.remove(comment);
                    }
                } else {
                    if (isLastOf(comment.getCause().get().getAnswers(), comment)) {
                        ((SimpleComment) comment.getCause().get()).removeAnswer(comment);
                    }
                }
            }
        }

        @Override
        public C getContent() {
            return content;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(SimpleRevision.class.isInstance(o))) return false;

            SimpleRevision that = (SimpleRevision) o;

            if (!author.equals(that.author)) return false;
            if (!comments.equals(that.comments)) return false;
            if (content != null ? !content.equals(that.content) : that.content != null) return false;
            if (!creationTime.equals(that.creationTime)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = author.hashCode();
            result = 31 * result + creationTime.hashCode();
            result = 31 * result + comments.hashCode();
            result = 31 * result + (content != null ? content.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("author", author)
                    .add("creationTime", creationTime)
                    .add("comments", comments)
                    .add("content", content)
                    .toString();
        }

        private <T> boolean isLastOf(@NotNull SortedSet<T> sortedSet, @NotNull T o) {
            return o.equals(sortedSet.last());
        }

        private boolean isOurComment(Comment comment) {
            return SimpleComment.class.isInstance(comment) && ((SimpleComment) comment).getRevision() == this;
        }

        public class SimpleComment implements Comment {

            protected final User author;
            protected final Instant creationTime;
            protected final String text;
            protected final Optional<Comment> cause;
            protected final NavigableSet<Comment> answers;

            public SimpleComment(@NotNull User author, @NotNull Instant creationTime, @NotNull String text) {
                this.author = author;
                this.creationTime = creationTime;
                this.text = text;
                this.cause = Optional.empty();
                this.answers = new TreeSet<>();
            }

            public SimpleComment(@NotNull User author, @NotNull Instant creationTime, @NotNull String text, @NotNull Comment cause) {
                this.author = author;
                this.creationTime = creationTime;
                this.text = text;
                this.cause = Optional.of(cause);
                this.answers = new TreeSet<>();
            }

            public Revision<C> getRevision() {
                return SimpleRevision.this;
            }

            @NotNull
            @Override
            public User getAuthor() {
                return author;
            }

            @NotNull
            @Override
            public Instant getCreationTime() {
                return creationTime;
            }

            @NotNull
            @Override
            public String getText() {
                return text;
            }

            @NotNull
            @Override
            public Optional<Comment> getCause() {
                return cause;
            }

            public void addAnswer(@NotNull Comment comment) {
                answers.add(comment);
            }

            public void removeAnswer(@NotNull Comment comment) {
                answers.add(comment);
            }

            @NotNull
            @Override
            public ObservableNavigableSet<Comment> getAnswers() {
                return new ObservableNavigableSetWrapper<>(unmodifiableNavigableSet(answers));
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (!(SimpleComment.class.isInstance(o))) return false;

                SimpleComment that = (SimpleComment) o;

                if (!answers.equals(that.answers)) return false;
                if (!author.equals(that.author)) return false;
                if (!cause.equals(that.cause)) return false;
                if (!creationTime.equals(that.creationTime)) return false;
                if (!text.equals(that.text)) return false;

                return true;
            }

            @Override
            public int hashCode() {
                int result = author.hashCode();
                result = 31 * result + creationTime.hashCode();
                result = 31 * result + text.hashCode();
                result = 31 * result + cause.hashCode();
                result = 31 * result + answers.hashCode();
                return result;
            }

            @Override
            public String toString() {
                return Objects.toStringHelper(this)
                        .add("author", author)
                        .add("creationTime", creationTime)
                        .add("text", text)
                        .add("cause", cause)
                        .add("answers", answers)
                        .toString();
            }

        }

    }

}
