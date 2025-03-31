package ro.unibuc.hello.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ro.unibuc.hello.data.FriendshipEntity;
import ro.unibuc.hello.data.FriendshipEntity.FriendshipStatus;
import ro.unibuc.hello.data.PostEntity;
import ro.unibuc.hello.data.PostEntity.PostVisibility;
import ro.unibuc.hello.data.UserEntity;
import ro.unibuc.hello.data.repository.PostRepository;
import ro.unibuc.hello.data.repository.UserRepository;
import ro.unibuc.hello.dto.PostDto;
import ro.unibuc.hello.exception.EntityNotFoundException;
import ro.unibuc.hello.exception.ForbiddenAccessException;
import ro.unibuc.hello.service.CommentService;
import ro.unibuc.hello.service.FriendshipService;
import ro.unibuc.hello.service.LikeService;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Answers.valueOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentService commentService;

    @Mock
    private LikeService likeService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FriendshipService friendshipService;

    @InjectMocks
    private PostService postService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreatePost() {
        // arrange: create a sample post request
        PostEntity postRequest = new PostEntity();
        postRequest.setUserId("user1");
        postRequest.setContent("Test content");
        postRequest.setMediaUrl("http://example.com/media");
        postRequest.setVisibility(PostVisibility.PUBLIC);

        // simulate the repository saving the post
        PostEntity createdPost = new PostEntity();
        createdPost.setId("1");
        createdPost.setUserId("user1");
        createdPost.setContent("Test content");
        createdPost.setMediaUrl("http://example.com/media");
        createdPost.setVisibility(PostVisibility.PUBLIC);
        createdPost.setCreatedAt(new Date());
        createdPost.setUpdatedAt(new Date());

        when(postRepository.save(any(PostEntity.class))).thenReturn(createdPost);

        PostEntity result = postService.createPost(postRequest);

        // assert
        assertNotNull(result);
        assertEquals("1", result.getId());
        assertEquals("Test content", result.getContent());
    }

    @Test
    void testGetVisiblePosts() {
        // arrange: create two posts: one visible and one not visible
        PostEntity publicPost = new PostEntity();
        publicPost.setId("1");
        publicPost.setUserId("user1");
        publicPost.setContent("Visible post");
        publicPost.setMediaUrl("http://example.com/media1");
        publicPost.setVisibility(PostVisibility.PUBLIC);
        publicPost.setCreatedAt(new Date());
        publicPost.setUpdatedAt(new Date());

        PostEntity friendsOnlyPost = new PostEntity();
        friendsOnlyPost.setId("2");
        friendsOnlyPost.setUserId("user2");
        friendsOnlyPost.setContent("Not visible post");
        friendsOnlyPost.setMediaUrl("http://example.com/media2");
        friendsOnlyPost.setVisibility(PostVisibility.FRIENDS_ONLY);
        friendsOnlyPost.setCreatedAt(new Date());
        friendsOnlyPost.setUpdatedAt(new Date());

        // for a PUBLIC post, friendship check returns null (can be viewed if you are not friends with the user)
        when(friendshipService.getFriendshipBetween("user1", null)).thenReturn(null);
        // for a FRIENDS_ONLY post, simulate that there isn't a friendship between the users (post should not be visible to that user)
        when(friendshipService.getFriendshipBetween("user2", null)).thenReturn(null);

        // simulated comment and like counts for public post
        when(commentService.countCommentsForPost("1")).thenReturn(5L);
        when(likeService.countLikesForPost("1")).thenReturn(3L);

        List<PostEntity> posts = Arrays.asList(publicPost, friendsOnlyPost);
        when(postRepository.findAll()).thenReturn(posts);

        List<PostDto> visiblePosts = postService.getVisiblePosts(null);

        // assert: only the public post should be seen by the user
        assertNotNull(visiblePosts);
        assertEquals(1, visiblePosts.size());
        PostDto dto = visiblePosts.get(0);
        assertEquals("1", dto.getId());
        assertEquals("Visible post", dto.getContent());
        assertEquals(5, dto.getCommentCount());
        assertEquals(3, dto.getLikeCount());
    }

    @Test
    void testGetPostById_Visible() throws EntityNotFoundException {
        // arrange: create a public visible post
        PostEntity post = new PostEntity();
        post.setId("1");
        post.setUserId("user1");
        post.setContent("Detailed content");
        post.setMediaUrl("http://example.com/media");
        post.setVisibility(PostVisibility.PUBLIC);
        post.setCreatedAt(new Date());
        post.setUpdatedAt(new Date());

        // for the PUBLIC post, friendship check returns null
        when(friendshipService.getFriendshipBetween("user1", null)).thenReturn(null);
        when(postRepository.findById("1")).thenReturn(Optional.of(post));
        // simulated comment and like counts
        when(commentService.countCommentsForPost("1")).thenReturn(2L);
        when(likeService.countLikesForPost("1")).thenReturn(4L);

        PostDto result = postService.getPostById("1", null);

        // assert
        assertNotNull(result);
        assertEquals("1", result.getId());
        assertEquals("Detailed content", result.getContent());
        assertEquals(2, result.getCommentCount());
        assertEquals(4, result.getLikeCount());
    }

    @Test
    void testGetPostById_NotVisible() {
        // arrange: create a FRIENDS_ONLY post that is not visible because of missing friendship between the users
        PostEntity post = new PostEntity();
        post.setId("1");
        post.setUserId("user2"); // post owner
        post.setContent("Private post");
        post.setMediaUrl("http://example.com/media");
        post.setVisibility(PostVisibility.FRIENDS_ONLY);
        post.setCreatedAt(new Date());
        post.setUpdatedAt(new Date());

        // simulate no friendship between the users
        when(friendshipService.getFriendshipBetween("user2", null)).thenReturn(null);
        when(postRepository.findById("1")).thenReturn(Optional.of(post));

        // assert: should throw exception because post is not visible
        assertThrows(EntityNotFoundException.class, () -> postService.getPostById("1", null));
    }

    @Test
    void testGetPostById_FriendsOnly_Visible() throws EntityNotFoundException {
        // arrange: create a FRIENDS_ONLY post
        PostEntity post = new PostEntity();
        post.setId("1");
        post.setUserId("user2"); // post owner
        post.setContent("Friends-only post");
        post.setMediaUrl("http://example.com/media");
        post.setVisibility(PostVisibility.FRIENDS_ONLY);
        post.setCreatedAt(new Date());
        post.setUpdatedAt(new Date());
        
        // create a FriendshipEntity with status ACCEPTED
        FriendshipEntity friendship = new FriendshipEntity();
        friendship.setStatus(FriendshipEntity.FriendshipStatus.ACCEPTED);
        
        // simulate post owner ("user2") and viewer ("user1") are friends
        when(friendshipService.getFriendshipBetween("user2", "user1")).thenReturn(friendship);
        
        // repository finds post
        when(postRepository.findById("1")).thenReturn(Optional.of(post));
        
        // simulated comment and like count
        when(commentService.countCommentsForPost("1")).thenReturn(2L);
        when(likeService.countLikesForPost("1")).thenReturn(3L);
        
        // call service method with "user1" as the current user (logged in)
        PostDto dto = postService.getPostById("1", "user1");
        
        // assert: the post should be visible and returned with its data
        assertNotNull(dto);
        assertEquals("1", dto.getId());
        assertEquals("Friends-only post", dto.getContent());
        assertEquals(2L, dto.getCommentCount());
        assertEquals(3L, dto.getLikeCount());
    }


    @Test
    void testUpdatePost_Existing() throws EntityNotFoundException {
        // arrange: existing post
        PostEntity existingPost = new PostEntity();
        existingPost.setId("1");
        existingPost.setUserId("user1");
        existingPost.setContent("Old content");
        existingPost.setMediaUrl("http://example.com/old");
        existingPost.setVisibility(PostVisibility.PUBLIC);
        existingPost.setCreatedAt(new Date());
        existingPost.setUpdatedAt(new Date());

        // update post details
        PostEntity updateRequest = new PostEntity();
        updateRequest.setContent("Updated content");
        updateRequest.setMediaUrl("http://example.com/updated");
        updateRequest.setVisibility(PostVisibility.PUBLIC);

        // simulate saving in repository
        when(postRepository.findById("1")).thenReturn(Optional.of(existingPost));
        PostEntity updatedPost = new PostEntity();
        updatedPost.setId("1");
        updatedPost.setUserId("user1");
        updatedPost.setContent("Updated content");
        updatedPost.setMediaUrl("http://example.com/updated");
        updatedPost.setVisibility(PostVisibility.PUBLIC);
        updatedPost.setUpdatedAt(new Date());
        when(postRepository.save(any(PostEntity.class))).thenReturn(updatedPost);

        PostEntity result = postService.updatePost("1", updateRequest);

        // assert
        assertNotNull(result);
        assertEquals("1", result.getId());
        assertEquals("Updated content", result.getContent());
    }

    @Test
    void testUpdatePost_NonExisting() {
        // arrange: update request for non-existent post
        PostEntity updateRequest = new PostEntity();
        updateRequest.setContent("Updated content");

        when(postRepository.findById("1")).thenReturn(Optional.empty());

        // assert: expect exception
        assertThrows(EntityNotFoundException.class, () -> postService.updatePost("1", updateRequest));
    }

    @Test
    void testDeletePost_Existing() throws EntityNotFoundException {
        // arrange: simulate existing post to be deleted
        when(postRepository.existsById("1")).thenReturn(true);

        postService.deletePost("1");

        // assert: check that delete methods are called for the necessary components
        verify(postRepository, times(1)).deleteById("1");
        verify(commentService, times(1)).deletePostComments("1");
        verify(likeService, times(1)).deletePostLikes("1");
    }

    @Test
    void testDeletePost_NonExisting() {
        // arrange: simulate post to be deleted does not exist
        when(postRepository.existsById("1")).thenReturn(false);

        // assert: expect exception
        assertThrows(EntityNotFoundException.class, () -> postService.deletePost("1"));
    }

    @Test
    void testGetPostById_PostNotFound() {
        // arrange: simulate requested post is not found
        when(postRepository.findById("1")).thenReturn(Optional.empty());

        // assert: expect exception when calling get method
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> postService.getPostById("1", null)
        );
        assertTrue(exception.getMessage().contains("Post not found"));
    }

    @Test
    void testGetPostById_Blocked() {
        // arrange: create a post that would normally be visible if the user is not blocked
        PostEntity post = new PostEntity();
        post.setId("1");
        post.setUserId("user2"); // post creator
        post.setContent("Some post content");
        post.setVisibility(PostVisibility.PUBLIC);

        // simulate user currentUser is blocked by the post creator
        FriendshipEntity friendship = new FriendshipEntity();
        friendship.setStatus(FriendshipEntity.FriendshipStatus.BLOCKED);
        when(friendshipService.getFriendshipBetween("user2", "currentUser")).thenReturn(friendship);

        // repository finds post
        when(postRepository.findById("1")).thenReturn(Optional.of(post));

        // assert: expect an exception since the viewer is blocked
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> 
            postService.getPostById("1", "currentUser")
        );
        assertTrue(exception.getMessage().contains("Post"));
    }

    @Test
    void testGetPostById_FriendsOnly_NonAccepted() {
        // arrange: create a FRIENDS_ONLY post
        PostEntity post = new PostEntity();
        post.setId("1");
        post.setUserId("user2"); // post creator
        post.setContent("Friends-only post");
        post.setVisibility(PostVisibility.FRIENDS_ONLY);

        // simulate existing friendship not accepted (pending)
        FriendshipEntity friendship = new FriendshipEntity();
        friendship.setStatus(FriendshipEntity.FriendshipStatus.PENDING);
        when(friendshipService.getFriendshipBetween("user2", "currentUser")).thenReturn(friendship);

        // repository finds post
        when(postRepository.findById("1")).thenReturn(Optional.of(post));

        // assert: expect an exception since the friendship is not accepted
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> 
            postService.getPostById("1", "currentUser")
        );
        assertTrue(exception.getMessage().contains("Post"));
    }
 // TODO: check comments
    @Test
    void testUpdatePostAuth_Success() throws EntityNotFoundException {
        // Arrange: Create an existing post owned by user1.
        PostEntity existingPost = new PostEntity();
        existingPost.setId("1");
        existingPost.setUserId("user1");
        existingPost.setContent("Old content");
        existingPost.setMediaUrl("http://example.com/old");
        existingPost.setVisibility(PostVisibility.PUBLIC);
        existingPost.setCreatedAt(new Date());
        existingPost.setUpdatedAt(new Date());
        
        // Create an update request.
        PostEntity updateRequest = new PostEntity();
        updateRequest.setContent("New content");
        updateRequest.setMediaUrl("http://example.com/new");
        
        // Arrange mocks: post exists and user exists.
        when(postRepository.findById("1")).thenReturn(Optional.of(existingPost));
        UserEntity user = new UserEntity();
        user.setId("user1");
        when(userRepository.findById("user1")).thenReturn(Optional.of(user));
        
        // Simulate saving the updated post.
        PostEntity savedPost = new PostEntity();
        savedPost.setId("1");
        savedPost.setUserId("user1");
        savedPost.setContent("New content");
        savedPost.setMediaUrl("http://example.com/new");
        savedPost.setUpdatedAt(new Date());
        when(postRepository.save(any(PostEntity.class))).thenReturn(savedPost);
        
        // Act
        PostEntity result = postService.updatePostAuth("1", "user1", updateRequest);
        
        // Assert
        assertNotNull(result);
        assertEquals("New content", result.getContent());
        assertEquals("http://example.com/new", result.getMediaUrl());
    }

    @Test
    void testUpdatePostAuth_PostNotFound() {
        // Arrange: Post does not exist.
        when(postRepository.findById("1")).thenReturn(Optional.empty());
        UserEntity user = new UserEntity();
        user.setId("user1");
        when(userRepository.findById("user1")).thenReturn(Optional.of(user));
        
        PostEntity updateRequest = new PostEntity();
        updateRequest.setContent("New content");
        
        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> 
            postService.updatePostAuth("1", "user1", updateRequest)
        );
    }
    
    @Test
    void testUpdatePostAuth_UserNotFound() {
        // Arrange: Post exists but user is not found.
        PostEntity existingPost = new PostEntity();
        existingPost.setId("1");
        existingPost.setUserId("user1");
        when(postRepository.findById("1")).thenReturn(Optional.of(existingPost));
        when(userRepository.findById("user1")).thenReturn(Optional.empty());
        
        PostEntity updateRequest = new PostEntity();
        updateRequest.setContent("New content");
        
        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> 
            postService.updatePostAuth("1", "user1", updateRequest)
        );
    }
    
    @Test
    void testUpdatePostAuth_Forbidden() {
        // Arrange: Post exists with owner "user2", but current user is "user1".
        PostEntity existingPost = new PostEntity();
        existingPost.setId("1");
        existingPost.setUserId("user2");
        when(postRepository.findById("1")).thenReturn(Optional.of(existingPost));
        
        UserEntity user = new UserEntity();
        user.setId("user1");
        when(userRepository.findById("user1")).thenReturn(Optional.of(user));
        
        PostEntity updateRequest = new PostEntity();
        updateRequest.setContent("New content");
        
        // Act & Assert
        assertThrows(ForbiddenAccessException.class, () -> 
            postService.updatePostAuth("1", "user1", updateRequest)
        );
    }
    
    // --------------------------
    // New tests for deletePostAuth
    // --------------------------
    
    @Test
    void testDeletePostAuth_Success() throws EntityNotFoundException {
        // Arrange: Post exists and is owned by user1.
        PostEntity existingPost = new PostEntity();
        existingPost.setId("1");
        existingPost.setUserId("user1");
        when(postRepository.findById("1")).thenReturn(Optional.of(existingPost));
        
        // Arrange: user exists.
        UserEntity user = new UserEntity();
        user.setId("user1");
        when(userRepository.findById("user1")).thenReturn(Optional.of(user));
        
        // Setup delete operations (simulate successful deletion).
        doNothing().when(postRepository).deleteById("1");
        doNothing().when(commentService).deletePostComments("1");
        doNothing().when(likeService).deletePostLikes("1");
        
        // Act
        postService.deletePostAuth("1", "user1");
        
        // Assert: Verify delete methods are called.
        verify(postRepository, times(1)).deleteById("1");
        verify(commentService, times(1)).deletePostComments("1");
        verify(likeService, times(1)).deletePostLikes("1");
    }
    
    @Test
    void testDeletePostAuth_PostNotFound() {
        // Arrange: Post does not exist.
        when(postRepository.findById("1")).thenReturn(Optional.empty());
        UserEntity user = new UserEntity();
        user.setId("user1");
        when(userRepository.findById("user1")).thenReturn(Optional.of(user));
        
        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> 
            postService.deletePostAuth("1", "user1")
        );
    }
    
    @Test
    void testDeletePostAuth_UserNotFound() {
        // Arrange: Post exists.
        PostEntity existingPost = new PostEntity();
        existingPost.setId("1");
        existingPost.setUserId("user1");
        when(postRepository.findById("1")).thenReturn(Optional.of(existingPost));
        // User not found.
        when(userRepository.findById("user1")).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> 
            postService.deletePostAuth("1", "user1")
        );
    }
    
    @Test
    void testDeletePostAuth_Forbidden() {
        // Arrange: Post exists with owner "user2" while current user is "user1".
        PostEntity existingPost = new PostEntity();
        existingPost.setId("1");
        existingPost.setUserId("user2");
        when(postRepository.findById("1")).thenReturn(Optional.of(existingPost));
        
        UserEntity user = new UserEntity();
        user.setId("user1");
        when(userRepository.findById("user1")).thenReturn(Optional.of(user));
        
        // Act & Assert
        assertThrows(ForbiddenAccessException.class, () -> 
            postService.deletePostAuth("1", "user1")
        );
    }
}
