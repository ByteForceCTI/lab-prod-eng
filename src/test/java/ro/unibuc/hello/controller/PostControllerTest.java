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
import ro.unibuc.hello.exception.EntityNotFoundException;
import ro.unibuc.hello.exception.GlobalExceptionHandler;
import ro.unibuc.hello.service.PostService;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

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

    @InjectMocks
    private PostController postController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(postController).setControllerAdvice(new GlobalExceptionHandler()).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void test_createPost() throws Exception {
        // arrange: create a sample request for creating a new post
        PostEntity postRequest = new PostEntity();
        postRequest.setUserId("user1");
        postRequest.setContent("Test content");
        postRequest.setMediaUrl("http://example.com/media");
        postRequest.setVisibility(PostVisibility.PUBLIC);

        // simulate the service returning the created post with its fields set
        PostEntity createdPost = new PostEntity();
        createdPost.setId("1");
        createdPost.setUserId("user1");
        createdPost.setContent("Test content");
        createdPost.setMediaUrl("http://example.com/media");
        createdPost.setVisibility(PostVisibility.PUBLIC);
        createdPost.setCreatedAt(new Date());
        createdPost.setUpdatedAt(new Date());

        when(postService.createPost(any(PostEntity.class))).thenReturn(createdPost);

        // assert: send the POST request and check the response json
        mockMvc.perform(post("/posts")
                .content(objectMapper.writeValueAsString(postRequest))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("1"))
            .andExpect(jsonPath("$.content").value("Test content"))
            .andExpect(jsonPath("$.userId").value("user1"));
    }

    @Test
    void test_getAllPosts() throws Exception {
        // arrange: create a list of PostDto objects that would be returned for visible posts
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
        when(postService.getVisiblePosts(null)).thenReturn(posts);

        // assert: send a GET request and verify the returned json array
        mockMvc.perform(get("/posts"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value("1"))
            .andExpect(jsonPath("$[0].content").value("Post 1 content"))
            .andExpect(jsonPath("$[1].id").value("2"))
            .andExpect(jsonPath("$[1].content").value("Post 2 content"));
    }

    @Test
    void test_getPostById() throws Exception {
        // arrange: creates a sample PostDto that represents the detailed post information
        PostDto postDto = new PostDto();
        postDto.setId("1");
        postDto.setUserId("user1");
        postDto.setContent("Detailed post content");
        postDto.setVisibility("PUBLIC");
        postDto.setCommentCount(2);
        postDto.setLikeCount(3);

        when(postService.getPostById("1", null)).thenReturn(postDto);

        // assert: send a GET request by id and verify the response json
        mockMvc.perform(get("/posts/{id}", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("1"))
            .andExpect(jsonPath("$.content").value("Detailed post content"));
    }

    @Test
    void test_getPostById_notFound() throws Exception {
        // arrange: simulate the service throwing an exception for a non-existent or inaccessible post
        when(postService.getPostById("1", null))
            .thenThrow(new EntityNotFoundException("Post not found"));

        // assert: check for a 404 Not Found status
        mockMvc.perform(get("/posts/{id}", "1"))
            .andExpect(status().isNotFound());
    }

    @Test
    void test_updatePost() throws Exception {
        // arrange: create an update request for a post
        PostEntity updateRequest = new PostEntity();
        updateRequest.setContent("Updated content");
        updateRequest.setMediaUrl("http://example.com/updated");
        updateRequest.setVisibility(PostVisibility.PUBLIC);

        // simulated updated post entity returned by the service
        PostEntity updatedPost = new PostEntity();
        updatedPost.setId("1");
        updatedPost.setUserId("user1");
        updatedPost.setContent("Updated content");
        updatedPost.setMediaUrl("http://example.com/updated");
        updatedPost.setVisibility(PostVisibility.PUBLIC);
        updatedPost.setUpdatedAt(new Date());

        when(postService.updatePost(eq("1"), any(PostEntity.class))).thenReturn(updatedPost);

        // assert: send a PUT request and verify the updated post json data
        mockMvc.perform(put("/posts/{id}", "1")
                .content(objectMapper.writeValueAsString(updateRequest))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("1"))
            .andExpect(jsonPath("$.content").value("Updated content"));
    }

    @Test
    void test_deletePost() throws Exception {
        // arrange: mock the service delete action
        doNothing().when(postService).deletePost("1");

        // assert: send a DELETE request and verify ok status
        mockMvc.perform(delete("/posts/{id}", "1"))
            .andExpect(status().isOk());

        verify(postService, times(1)).deletePost("1");
    }

    @Test
    void test_updatePost_notFound() throws Exception {
        // arrange: create an update request for a non-existent post.
        PostEntity updateRequest = new PostEntity();
        updateRequest.setContent("Updated content");

        when(postService.updatePost(eq("1"), any(PostEntity.class)))
            .thenThrow(new EntityNotFoundException("Post not found"));

        // assert: send a PUT request and expect a 404 Not Found
        mockMvc.perform(put("/posts/{id}", "1")
                .content(objectMapper.writeValueAsString(updateRequest))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    void test_deletePost_notFound() throws Exception {
        // arrange: simulate that the deleted post does not exist
        doThrow(new EntityNotFoundException("Post not found")).when(postService).deletePost("1");

        // assert: send a DELETE request and expect 404 Not Found
        mockMvc.perform(delete("/posts/{id}", "1"))
            .andExpect(status().isNotFound());
    }


}
