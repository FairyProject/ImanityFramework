package org.imanity.framework.config;

import org.hamcrest.Matchers;
import org.imanity.framework.config.annotation.Comment;
import org.imanity.framework.config.util.CollectionFactory;
import org.junit.jupiter.api.Test;

import static org.imanity.framework.config.util.CollectionFactory.mapOf;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class CommentsTest {

    @Test
    void classCommentsAdded() {
        class A {}

        @Comment("B")
        class B {}

        @Comment({"C", "D"})
        class C {}

        Comments comments = Comments.ofClass(A.class);
        assertThat(comments.getClassComments(), empty());
        assertThat(comments.getFieldComments().entrySet(), empty());

        comments = Comments.ofClass(B.class);
        assertThat(comments.getClassComments(), Matchers.is(CollectionFactory.listOf("B")));
        assertThat(comments.getFieldComments().entrySet(), empty());

        comments = Comments.ofClass(C.class);
        assertThat(comments.getClassComments(), Matchers.is(CollectionFactory.listOf("C", "D")));
        assertThat(comments.getFieldComments().entrySet(), empty());
    }

    @Test
    void fieldCommentsAdded() {
        class A {
            int a;
            @Comment("b")
            int b;
            @Comment({"c", "d"})
            int c;
        }

        Comments comments = Comments.ofClass(A.class);
        assertThat(comments.getClassComments(), empty());
        assertThat(comments.getFieldComments(), Matchers.is(CollectionFactory.mapOf(
                "b", CollectionFactory.listOf("b"),
                "c", CollectionFactory.listOf("c", "d")
        )));
    }
}
