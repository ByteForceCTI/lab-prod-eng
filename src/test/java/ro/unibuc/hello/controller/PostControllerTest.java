package ro.unibuc.hello.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ro.unibuc.hello.data.PostEntity;
import ro.unibuc.hello.data.PostEntity.PostVisibility;
import ro.unibuc.hello.dto.PostDto;
import ro.unibuc.hello.dto.UserDto;
import ro.unibuc.hello.exception.EntityNotFoundException;
import ro.unibuc.hello.exception.GlobalExceptionHandler;
import ro.unibuc.hello.exception.ForbiddenAccessException;
import ro.unibuc.hello.service.PostService;
import ro.unibuc.hello.service.implementation.UserServiceImpl;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PostControllerTest {

    @Mock
    private PostService postService;

    @Mock
    private UserServiceImpl userService;

    @InjectMocks
    private PostController postController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    // TODO: Rewrite comments
    
    // Define a dummy valid token and a valid user to be returned for that token.
    private final String validToken = "validToken";
    private UserDto validUser;
    private UserDto forbiddenUser;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        // Build the MockMvc with the GlobalExceptionHandler for error mapping.
        mockMvc = MockMvcBuilders.standaloneSetup(postController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        validUser = new UserDto();
        validUser.setId("user1");
        validUser.setUsername("testUser");

        forbiddenUser = new UserDto();
        forbiddenUser.setId("user2");
        forbiddenUser.setUsername("forbiddenUser");
    }

    @Test
    void test_createPost() throws Exception {
        // Arrange: simulate that the provided valid token returns a valid user.
        when(userService.getUserFromToken(validToken)).thenReturn(validUser);

        // Prepare a sample post request. Note: we don't set userId because the controller sets it from the token.
        PostEntity postRequest = new PostEntity();
        postRequest.setContent("Test content");
        postRequest.setMediaUrl("http://example.com/media");
        postRequest.setVisibility(PostVisibility.PUBLIC);

        // Simulate the service returning the created post.
        PostEntity createdPost = new PostEntity();
        createdPost.setId("1");
        createdPost.setUserId("user1");
        createdPost.setContent("Test content");
        createdPost.setMediaUrl("http://example.com/media");
        createdPost.setVisibility(PostVisibility.PUBLIC);
        createdPost.setCreatedAt(new Date());
        createdPost.setUpdatedAt(new Date());

        when(postService.createPost(any(PostEntity.class))).thenReturn(createdPost);

        // Act & Assert: send the POST request with a valid Authorization header.
        mockMvc.perform(post("/posts")
                .content(objectMapper.writeValueAsString(postRequest))
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value("1"))
            .andExpect(jsonPath("$.content").value("Test content"))
            .andExpect(jsonPath("$.userId").value("user1"));
    }

    @Test
    void test_getAllPosts() throws Exception {
        // Arrange: simulate valid authentication.
        when(userService.getUserFromToken(validToken)).thenReturn(validUser);

        // Create sample PostDto objects.
        PostDto postDto1 = new PostDto();
        postDto1.setId("1");
        postDto1.setUserId("user1");
        postDto1.setContent("Post 1 content");
        postDto1.setVisibility("PUBLIC");
        postDto1.setCommentCount(0);
        postDto1.setLikeCount(0);

        PostDto postDto2 = new PostDto();
        postDto2.setId("2");
        postDto2.setUserId("user2");
        postDto2.setContent("Post 2 content");
        postDto2.setVisibility("PUBLIC");
        postDto2.setCommentCount(1);
        postDto2.setLikeCount(2);

        List<PostDto> posts = Arrays.asList(postDto1, postDto2);
        // Note: the controller uses the user ID from the authenticated user.
        when(postService.getVisiblePosts("user1")).thenReturn(posts);

        // Act & Assert: perform GET request with a valid Authorization header.
        mockMvc.perform(get("/posts")
                .header("Authorization", "Bearer " + validToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value("1"))
            .andExpect(jsonPath("$[0].content").value("Post 1 content"))
            .andExpect(jsonPath("$[1].id").value("2"))
            .andExpect(jsonPath("$[1].content").value("Post 2 content"));
    }

    @Test
    void test_getPostById() throws Exception {
        // Arrange: valid authentication
        when(userService.getUserFromToken(validToken)).thenReturn(validUser);

        PostDto postDto = new PostDto();
        postDto.setId("1");
        postDto.setUserId("user1");
        postDto.setContent("Detailed post content");
        postDto.setVisibility("PUBLIC");
        postDto.setCommentCount(2);
        postDto.setLikeCount(3);

        // The controller passes the authenticated user's ID.
        when(postService.getPostById("1", "user1")).thenReturn(postDto);

        // Act & Assert: perform GET by id with Authorization header.
        mockMvc.perform(get("/posts/{id}", "1")
                .header("Authorization", "Bearer " + validToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("1"))
            .andExpect(jsonPath("$.content").value("Detailed post content"));
    }

    @Test
    void test_getPostById_notFound() throws Exception {
        // Arrange: valid authentication.
        when(userService.getUserFromToken(validToken)).thenReturn(validUser);

        when(postService.getPostById("1", "user1"))
            .thenThrow(new EntityNotFoundException("Post not found"));

        // Act & Assert: expect a 404 Not Found.
        mockMvc.perform(get("/posts/{id}", "1")
                .header("Authorization", "Bearer " + validToken))
            .andExpect(status().isNotFound());
    }

    @Test
    void test_updatePost() throws Exception {
        // Arrange: valid authentication.
        when(userService.getUserFromToken(validToken)).thenReturn(validUser);

        PostEntity updateRequest = new PostEntity();
        updateRequest.setContent("Updated content");
        updateRequest.setMediaUrl("http://example.com/updated");
        updateRequest.setVisibility(PostVisibility.PUBLIC);

        PostEntity updatedPost = new PostEntity();
        updatedPost.setId("1");
        updatedPost.setUserId("user1");
        updatedPost.setContent("Updated content");
        updatedPost.setMediaUrl("http://example.com/updated");
        updatedPost.setVisibility(PostVisibility.PUBLIC);
        updatedPost.setUpdatedAt(new Date());

        // Expect updatePostAuth instead of updatePost.
        when(postService.updatePostAuth(eq("1"), eq(validUser.getId()), any(PostEntity.class)))
            .thenReturn(updatedPost);

        // Act & Assert: perform PUT with Authorization header.
        mockMvc.perform(put("/posts/{id}", "1")
                .content(objectMapper.writeValueAsString(updateRequest))
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("1"))
            .andExpect(jsonPath("$.content").value("Updated content"));
    }

    @Test
    void test_updatePost_notFound() throws Exception {
        // Arrange: valid authentication.
        when(userService.getUserFromToken(validToken)).thenReturn(validUser);

        PostEntity updateRequest = new PostEntity();
        updateRequest.setContent("Updated content");

        when(postService.updatePostAuth(eq("1"), eq(validUser.getId()), any(PostEntity.class)))
            .thenThrow(new EntityNotFoundException("Post not found"));

        // Act & Assert: perform PUT with Authorization header expecting 404.
        mockMvc.perform(put("/posts/{id}", "1")
                .content(objectMapper.writeValueAsString(updateRequest))
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    void test_deletePost() throws Exception {
        // Arrange: valid authentication.
        when(userService.getUserFromToken(validToken)).thenReturn(validUser);
        doNothing().when(postService).deletePostAuth("1", validUser.getId());

        // Act & Assert: perform DELETE with Authorization header.
        mockMvc.perform(delete("/posts/{id}", "1")
                .header("Authorization", "Bearer " + validToken))
            .andExpect(status().isNoContent());

        verify(postService, times(1)).deletePostAuth("1", validUser.getId());
    }

    @Test
    void test_deletePost_notFound() throws Exception {
        // Arrange: valid authentication.
        when(userService.getUserFromToken(validToken)).thenReturn(validUser);
        doThrow(new EntityNotFoundException("Post not found")).when(postService)
            .deletePostAuth("1", validUser.getId());

        // Act & Assert: perform DELETE with Authorization header expecting 404.
        mockMvc.perform(delete("/posts/{id}", "1")
                .header("Authorization", "Bearer " + validToken))
            .andExpect(status().isNotFound());
    }

    // ------------------------------
    // New tests for user==null branches
    // ------------------------------

    @Test
    void test_createPost_userNull() throws Exception {
        // Simulate that getUserFromToken returns null.
        when(userService.getUserFromToken(validToken)).thenReturn(null);

        PostEntity postRequest = new PostEntity();
        postRequest.setContent("Test content");
        postRequest.setMediaUrl("http://example.com/media");
        postRequest.setVisibility(PostVisibility.PUBLIC);

        mockMvc.perform(post("/posts")
                .content(objectMapper.writeValueAsString(postRequest))
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void test_getAllPosts_userNull() throws Exception {
        when(userService.getUserFromToken(validToken)).thenReturn(null);

        mockMvc.perform(get("/posts")
                .header("Authorization", "Bearer " + validToken))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void test_getPostById_userNull() throws Exception {
        when(userService.getUserFromToken(validToken)).thenReturn(null);

        mockMvc.perform(get("/posts/{id}", "1")
                .header("Authorization", "Bearer " + validToken))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void test_updatePost_userNull() throws Exception {
        when(userService.getUserFromToken(validToken)).thenReturn(null);
        PostEntity updateRequest = new PostEntity();
        updateRequest.setContent("Updated content");
        updateRequest.setMediaUrl("http://example.com/updated");
        updateRequest.setVisibility(PostVisibility.PUBLIC);

        mockMvc.perform(put("/posts/{id}", "1")
                .content(objectMapper.writeValueAsString(updateRequest))
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void test_deletePost_userNull() throws Exception {
        when(userService.getUserFromToken(validToken)).thenReturn(null);

        mockMvc.perform(delete("/posts/{id}", "1")
                .header("Authorization", "Bearer " + validToken))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void test_deletePost_forbidden() throws Exception {
        // Arrange: simulate that the authenticated user is forbidden from deleting the post.
        when(userService.getUserFromToken(validToken)).thenReturn(forbiddenUser);
        // Simulate that deletePostAuth throws a ForbiddenAccessException.
        doThrow(new ForbiddenAccessException("User is not the owner of this post."))
            .when(postService).deletePostAuth("1", forbiddenUser.getId());
        
        // Act & Assert: perform DELETE and expect HTTP 403 Forbidden.
        mockMvc.perform(delete("/posts/{id}", "1")
                .header("Authorization", "Bearer " + validToken))
            .andExpect(status().isForbidden());
    }

    @Test
    void test_updatePost_forbidden() throws Exception {
        // Arrange: simulate that the authenticated user is forbidden from updating the post.
        when(userService.getUserFromToken(validToken)).thenReturn(forbiddenUser);
        
        PostEntity updateRequest = new PostEntity();
        updateRequest.setContent("Updated content");
        updateRequest.setMediaUrl("http://example.com/updated");
        updateRequest.setVisibility(PostVisibility.PUBLIC);
        
        // Simulate that updatePostAuth throws a ForbiddenAccessException.
        when(postService.updatePostAuth(eq("1"), eq(forbiddenUser.getId()), any(PostEntity.class)))
            .thenThrow(new ForbiddenAccessException("User is not the owner of this post."));
        
        // Act & Assert: perform PUT and expect HTTP 403 Forbidden.
        mockMvc.perform(put("/posts/{id}", "1")
                .content(objectMapper.writeValueAsString(updateRequest))
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    void test_authHeader_Null() throws Exception { // because all requests go through GlobalExceptionHandler, behaviour should be the same for all
        when(userService.getUserFromToken(validToken)).thenReturn(null);

        mockMvc.perform(delete("/posts/{id}", "1")
                .header("NoAuth", "Test "))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void test_authHeader_bad() throws Exception {
        when(userService.getUserFromToken(validToken)).thenReturn(null);

        mockMvc.perform(delete("/posts/{id}", "1")
                .header("Authorization", "Beaarer " + validToken))
            .andExpect(status().isUnauthorized());
    }

}
