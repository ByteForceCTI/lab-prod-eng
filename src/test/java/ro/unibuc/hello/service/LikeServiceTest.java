package ro.unibuc.hello.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ro.unibuc.hello.data.LikeEntity;
import ro.unibuc.hello.data.repository.LikeRepository;
import ro.unibuc.hello.exception.EntityNotFoundException;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class LikeServiceTest {

    @Mock
    private LikeRepository likeRepository;

    @InjectMocks
    private LikeService likeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // test adding a like for a post (success)
    @Test
    void testCreatePostLike_Success() {
        // arrange: no existing like found
        when(likeRepository.findByUserIdAndPostId("post1", "user1")).thenReturn(null);
        LikeEntity savedLike = new LikeEntity();
        savedLike.setId("like1");
        savedLike.setPostId("post1");
        savedLike.setUserId("user1");
        savedLike.setCreatedAt(new Date());
        when(likeRepository.save(any(LikeEntity.class))).thenReturn(savedLike);

        LikeEntity result = likeService.createPostLike("post1", "user1");

        // assert
        assertNotNull(result);
        assertEquals("like1", result.getId());
        assertEquals("post1", result.getPostId());
        assertEquals("user1", result.getUserId());
    }

    // test adding a like for a post when the user has already liked it
    @Test
    void testCreatePostLike_AlreadyLiked() {
        // arrange: existing like is found
        LikeEntity existingLike = new LikeEntity();
        when(likeRepository.findByUserIdAndPostId("user1", "post1")).thenReturn(existingLike);

        // assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                likeService.createPostLike("post1", "user1")
        );
        assertTrue(exception.getMessage().contains("User has already liked this post."));
    }

    // test adding a like for a comment (success)
    @Test
    void testCreateCommentLike_Success() {
        // arrange: no existing like found
        when(likeRepository.findByUserIdAndCommentId("comment1", "user1")).thenReturn(null);
        LikeEntity savedLike = new LikeEntity();
        savedLike.setId("like2");
        savedLike.setCommentId("comment1");
        savedLike.setUserId("user1");
        savedLike.setCreatedAt(new Date());
        when(likeRepository.save(any(LikeEntity.class))).thenReturn(savedLike);

        LikeEntity result = likeService.createCommentLike("comment1", "user1");

        // assert
        assertNotNull(result);
        assertEquals("like2", result.getId());
        assertEquals("comment1", result.getCommentId());
        assertEquals("user1", result.getUserId());
    }

    // test adding a like for a comment when the user has already liked it
    @Test
    void testCreateCommentLike_AlreadyLiked() {
        // arrange: existing like exists
        LikeEntity existingLike = new LikeEntity();
        when(likeRepository.findByUserIdAndCommentId("user1", "comment1")).thenReturn(existingLike);

        // assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                likeService.createCommentLike("comment1", "user1")
        );
        assertTrue(exception.getMessage().contains("User has already liked this comment."));
    }

    // test for counting likes on a post
    @Test
    void testCountLikesForPost() {
        // arrange: return a list with three likes
        List<LikeEntity> likes = Arrays.asList(new LikeEntity(), new LikeEntity(), new LikeEntity());
        when(likeRepository.findByPostId("post1")).thenReturn(likes);

        long count = likeService.countLikesForPost("post1");

        // assert
        assertEquals(3L, count);
    }

    // test counting likes on a comment
    @Test
    void testCountLikesForComment() {
        // arrange: return a list with two likes
        List<LikeEntity> likes = Arrays.asList(new LikeEntity(), new LikeEntity());
        when(likeRepository.findByCommentId("comment1")).thenReturn(likes);

        long count = likeService.countLikesForComment("comment1");

        // assert
        assertEquals(2L, count);
    }

    // test deleting all likes for a post (success)
    @Test
    void testDeletePostLikes_Success() throws EntityNotFoundException {
        // arrange: for a given post, the service finds two likes
        LikeEntity like1 = new LikeEntity();
        like1.setId("like1");
        LikeEntity like2 = new LikeEntity();
        like2.setId("like2");
        List<LikeEntity> likes = Arrays.asList(like1, like2);
        when(likeRepository.findByPostId("post1")).thenReturn(likes);
        when(likeRepository.findById("like1")).thenReturn(Optional.of(like1));
        when(likeRepository.findById("like2")).thenReturn(Optional.of(like2));

        likeService.deletePostLikes("post1");

        // assert: check that delete is called for each like (once per like)
        verify(likeRepository, times(1)).delete(like1);
        verify(likeRepository, times(1)).delete(like2);
    }

    // test deleting likes for a post when one like is not found
    @Test
    void testDeletePostLikes_LikeNotFound() {
        // arrange: return one like but then simulates not finding it by id
        LikeEntity like1 = new LikeEntity();
        like1.setId("like1");
        List<LikeEntity> likes = Arrays.asList(like1);
        when(likeRepository.findByPostId("post1")).thenReturn(likes);
        when(likeRepository.findById("like1")).thenReturn(Optional.empty());

        // assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                likeService.deletePostLikes("post1")
        );
        assertTrue(exception.getMessage().contains("Like not found"));
    }

    // test deleting all likes for a comment (success)
    @Test
    void testDeleteCommentLikes_Success() throws EntityNotFoundException {
        // arrange: for a given comment, find one like
        LikeEntity like1 = new LikeEntity();
        like1.setId("like1");
        List<LikeEntity> likes = Arrays.asList(like1);
        when(likeRepository.findByCommentId("comment1")).thenReturn(likes);
        when(likeRepository.findById("like1")).thenReturn(Optional.of(like1));

        likeService.deleteCommentLikes("comment1");

        // assert: verify delete is called once
        verify(likeRepository, times(1)).delete(like1);
    }

    // test deleting likes for a comment when a like is not found
    @Test
    void testDeleteCommentLikes_LikeNotFound() {
        // arrange: return one like, but simulates not finding it by id
        LikeEntity like1 = new LikeEntity();
        like1.setId("like1");
        List<LikeEntity> likes = Arrays.asList(like1);
        when(likeRepository.findByCommentId("comment1")).thenReturn(likes);
        when(likeRepository.findById("like1")).thenReturn(Optional.empty());

        // assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                likeService.deleteCommentLikes("comment1")
        );
        assertTrue(exception.getMessage().contains("Like not found"));
    }
}
