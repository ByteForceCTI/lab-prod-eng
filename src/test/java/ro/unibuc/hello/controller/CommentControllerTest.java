package ro.unibuc.hello.controller;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ro.unibuc.hello.data.CommentEntity;
import ro.unibuc.hello.exception.EntityNotFoundException;
import ro.unibuc.hello.service.CommentService;

class CommentControllerTest {

    @Mock
    private CommentService commentService;

    @InjectMocks
    private CommentController commentController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(commentController).build();
    }

    @Test
    void test_createComment() throws Exception {
        // Arrange
        CommentEntity inputComment = new CommentEntity();
        inputComment.setPostId("post1");
        inputComment.setUserId("user1");
        inputComment.setContent("This is a comment");

        CommentEntity savedComment = new CommentEntity();
        savedComment.setId("c1");
        savedComment.setPostId("post1");
        savedComment.setUserId("user1");
        savedComment.setContent("This is a comment");

        when(commentService.createComment(eq("post1"), eq("user1"), eq("This is a comment")))
                .thenReturn(savedComment);

        // Act & Assert
        mockMvc.perform(post("/comments")
                .content("{\"postId\":\"post1\", \"userId\":\"user1\", \"content\":\"This is a comment\"}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("c1"))
                .andExpect(jsonPath("$.postId").value("post1"))
                .andExpect(jsonPath("$.userId").value("user1"))
                .andExpect(jsonPath("$.content").value("This is a comment"));
    }

    @Test
    void test_createNestedComment() throws Exception {
        // Arrange
        CommentEntity inputComment = new CommentEntity();
        inputComment.setPostId("post1");
        inputComment.setUserId("user2");
        inputComment.setContent("This is a nested comment");
        inputComment.setParentCommentId("parent1");

        CommentEntity savedComment = new CommentEntity();
        savedComment.setId("c2");
        savedComment.setPostId("post1");
        savedComment.setUserId("user2");
        savedComment.setContent("This is a nested comment");
        savedComment.setParentCommentId("parent1");

        when(commentService.createNestedComment(eq("post1"), eq("user2"), eq("This is a nested comment"), eq("parent1")))
                .thenReturn(savedComment);

        // Act & Assert
        mockMvc.perform(post("/comments/nested")
                .content("{\"postId\":\"post1\", \"userId\":\"user2\", \"content\":\"This is a nested comment\", \"parentCommentId\":\"parent1\"}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("c2"))
                .andExpect(jsonPath("$.postId").value("post1"))
                .andExpect(jsonPath("$.userId").value("user2"))
                .andExpect(jsonPath("$.content").value("This is a nested comment"))
                .andExpect(jsonPath("$.parentCommentId").value("parent1"));
    }

    @Test
    void test_createNestedComment_EntityNotFound() throws Exception {
        // Arrange
        String parentId = "nonExistingParent";
        when(commentService.createNestedComment(any(), any(), any(), eq(parentId)))
                .thenThrow(new EntityNotFoundException("Parent comment not found: " + parentId));

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> commentController.createNestedComment(new CommentEntity() {{
                    setPostId("post1");
                    setUserId("user2");
                    setContent("Nested comment");
                    setParentCommentId(parentId);
                }}));
        assertTrue(exception.getMessage().contains(parentId));
    }

    @Test
    void test_countCommentsForPost() throws Exception {
        // Arrange
        String postId = "post1";
        when(commentService.countCommentsForPost(postId)).thenReturn(5L);

        // Act & Assert
        mockMvc.perform(get("/comments/count")
                .param("postId", postId))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }

    @Test
    void test_editComment() throws Exception {
        // Arrange
        String commentId = "c1";
        String newContent = "Edited comment";
        CommentEntity editedComment = new CommentEntity();
        editedComment.setId(commentId);
        editedComment.setContent(newContent);

        when(commentService.editComment(eq(commentId), eq(newContent))).thenReturn(editedComment);

        // Act & Assert
        mockMvc.perform(put("/comments/{id}", commentId)
                .content("{\"content\":\"" + newContent + "\"}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(commentId))
                .andExpect(jsonPath("$.content").value(newContent));
    }

    @Test
    void test_deleteComment() throws Exception {
        // Arrange
        String commentId = "c1";

        // Act & Assert
        mockMvc.perform(delete("/comments/{id}", commentId))
                .andExpect(status().isOk());

        verify(commentService, times(1)).deleteComment(commentId);
    }
}
