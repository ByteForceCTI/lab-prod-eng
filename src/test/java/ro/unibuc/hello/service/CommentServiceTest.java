package ro.unibuc.hello.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ro.unibuc.hello.data.CommentEntity;
import ro.unibuc.hello.data.repository.CommentRepository;
import ro.unibuc.hello.exception.EntityNotFoundException;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private LikeService likeService;

    @InjectMocks
    private CommentService commentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateComment() {
        // Arrange
        String postId = "post1";
        String userId = "user1";
        String content = "This is a comment";
        CommentEntity savedComment = new CommentEntity();
        savedComment.setPostId(postId);
        savedComment.setUserId(userId);
        savedComment.setContent(content);
        savedComment.setParentCommentId(null);
        savedComment.setCreatedAt(new Date());
        savedComment.setUpdatedAt(null);
        when(commentRepository.save(any(CommentEntity.class))).thenReturn(savedComment);

        // Act
        CommentEntity result = commentService.createComment(postId, userId, content);

        // Assert
        assertNotNull(result);
        assertEquals(postId, result.getPostId());
        assertEquals(userId, result.getUserId());
        assertEquals(content, result.getContent());
        assertNull(result.getParentCommentId());
        verify(commentRepository, times(1)).save(any(CommentEntity.class));
    }

    @Test
    void testCreateNestedComment_ExistingParent() throws EntityNotFoundException {
        // Arrange
        String postId = "post1";
        String userId = "user2";
        String content = "This is a nested comment";
        String parentCommentId = "parent1";

        CommentEntity parentComment = new CommentEntity();
        parentComment.setId(parentCommentId);
        parentComment.setContent("Parent comment");

        when(commentRepository.findById(parentCommentId)).thenReturn(Optional.of(parentComment));

        CommentEntity savedComment = new CommentEntity();
        savedComment.setPostId(postId);
        savedComment.setUserId(userId);
        savedComment.setContent(content);
        savedComment.setParentCommentId(parentCommentId);
        savedComment.setCreatedAt(new Date());
        savedComment.setUpdatedAt(null);

        when(commentRepository.save(any(CommentEntity.class))).thenReturn(savedComment);

        // Act
        CommentEntity result = commentService.createNestedComment(postId, userId, content, parentCommentId);

        // Assert
        assertNotNull(result);
        assertEquals(postId, result.getPostId());
        assertEquals(userId, result.getUserId());
        assertEquals(content, result.getContent());
        assertEquals(parentCommentId, result.getParentCommentId());
        verify(commentRepository, times(1)).findById(parentCommentId);
        verify(commentRepository, times(1)).save(any(CommentEntity.class));
    }

    @Test
    void testCreateNestedComment_NonExistingParent() {
        // Arrange
        String postId = "post1";
        String userId = "user2";
        String content = "This is a nested comment";
        String parentCommentId = "nonExistingParent";

        when(commentRepository.findById(parentCommentId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, 
            () -> commentService.createNestedComment(postId, userId, content, parentCommentId));
        assertTrue(exception.getMessage().contains(parentCommentId));
        verify(commentRepository, times(1)).findById(parentCommentId);
    }

    @Test
    void testCountCommentsForPost() {
        // Arrange
        String postId = "post1";
        List<CommentEntity> comments = Arrays.asList(
                new CommentEntity(), new CommentEntity(), new CommentEntity()
        );
        when(commentRepository.findByPostId(postId)).thenReturn(comments);

        // Act
        long count = commentService.countCommentsForPost(postId);

        // Assert
        assertEquals(3, count);
        verify(commentRepository, times(1)).findByPostId(postId);
    }

    @Test
    void testEditComment_ExistingEntity() throws EntityNotFoundException {
        // Arrange
        String commentId = "comment1";
        String newContent = "Edited comment content";
        CommentEntity existingComment = new CommentEntity();
        existingComment.setId(commentId);
        existingComment.setContent("Original content");
        existingComment.setCreatedAt(new Date());
        existingComment.setUpdatedAt(null);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(existingComment));

        CommentEntity updatedComment = new CommentEntity();
        updatedComment.setId(commentId);
        updatedComment.setContent(newContent);
        updatedComment.setCreatedAt(existingComment.getCreatedAt());
        updatedComment.setUpdatedAt(new Date());

        when(commentRepository.save(any(CommentEntity.class))).thenReturn(updatedComment);

        // Act
        CommentEntity result = commentService.editComment(commentId, newContent);

        // Assert
        assertNotNull(result);
        assertEquals(commentId, result.getId());
        assertEquals(newContent, result.getContent());
        assertNotNull(result.getUpdatedAt());
        verify(commentRepository, times(1)).findById(commentId);
        verify(commentRepository, times(1)).save(any(CommentEntity.class));
    }

    @Test
    void testEditComment_NonExistingEntity() {
        // Arrange
        String commentId = "nonExistingComment";
        String newContent = "Edited comment content";
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> commentService.editComment(commentId, newContent));
        verify(commentRepository, times(1)).findById(commentId);
    }

    @Test
    void testDeleteComment_ExistingEntity() throws EntityNotFoundException {
        // Arrange
        String commentId = "comment1";
        CommentEntity comment = new CommentEntity();
        comment.setId(commentId);
        comment.setContent("Comment to delete");

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // Act
        commentService.deleteComment(commentId);

        // Assert
        verify(commentRepository, times(1)).findById(commentId);
        verify(commentRepository, times(1)).delete(comment);
    }

    @Test
    void testDeleteComment_NonExistingEntity() {
        // Arrange
        String commentId = "nonExistingComment";
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> commentService.deleteComment(commentId));
        verify(commentRepository, times(1)).findById(commentId);
    }

    @Test
    void testDeletePostComments() throws EntityNotFoundException {
        // Arrange
        String postId = "post1";
        CommentEntity comment1 = new CommentEntity();
        comment1.setId("c1");
        comment1.setPostId(postId);

        CommentEntity comment2 = new CommentEntity();
        comment2.setId("c2");
        comment2.setPostId(postId);

        List<CommentEntity> comments = Arrays.asList(comment1, comment2);
        when(commentRepository.findByPostId(postId)).thenReturn(comments);

        // For each deleteComment call, stub findById to return the comment
        when(commentRepository.findById("c1")).thenReturn(Optional.of(comment1));
        when(commentRepository.findById("c2")).thenReturn(Optional.of(comment2));

        // Act
        commentService.deletePostComments(postId);

        // Assert
        verify(commentRepository, times(1)).findByPostId(postId);
        verify(likeService, times(1)).deleteCommentLikes("c1");
        verify(likeService, times(1)).deleteCommentLikes("c2");
        verify(commentRepository, times(1)).delete(comment1);
        verify(commentRepository, times(1)).delete(comment2);
    }
}
