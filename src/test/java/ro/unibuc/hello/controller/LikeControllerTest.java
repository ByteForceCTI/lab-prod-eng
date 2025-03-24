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
import ro.unibuc.hello.data.LikeEntity;
import ro.unibuc.hello.exception.EntityNotFoundException;
import ro.unibuc.hello.exception.GlobalExceptionHandler;
import ro.unibuc.hello.service.LikeService;

import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LikeControllerTest {

    @Mock
    private LikeService likeService;

    @InjectMocks
    private LikeController likeController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(likeController).setControllerAdvice(new GlobalExceptionHandler()).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testCreatePostLike() throws Exception {
        // arrange: create a request LikeEntity containing postId and userId
        LikeEntity requestLike = new LikeEntity();
        requestLike.setPostId("post1");
        requestLike.setUserId("user1");

        // simulated LikeEntity from database
        LikeEntity savedLike = new LikeEntity();
        savedLike.setId("like1");
        savedLike.setPostId("post1");
        savedLike.setUserId("user1");
        savedLike.setCreatedAt(new Date());

        when(likeService.createPostLike("post1", "user1")).thenReturn(savedLike);

        // assert: send POST request to /likes/post
        mockMvc.perform(post("/likes/post")
                .content(objectMapper.writeValueAsString(requestLike))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("like1"))
            .andExpect(jsonPath("$.postId").value("post1"))
            .andExpect(jsonPath("$.userId").value("user1"));
    }

    @Test
    void testCreateCommentLike() throws Exception {
        // arrange: create a request LikeEntity containing commentId and userId
        LikeEntity requestLike = new LikeEntity();
        requestLike.setCommentId("comment1");
        requestLike.setUserId("user1");

        // simulate returning a saved LikeEntity
        LikeEntity savedLike = new LikeEntity();
        savedLike.setId("like2");
        savedLike.setCommentId("comment1");
        savedLike.setUserId("user1");
        savedLike.setCreatedAt(new Date());

        when(likeService.createCommentLike("comment1", "user1")).thenReturn(savedLike);

        // assert: send POST request to /likes/comment
        mockMvc.perform(post("/likes/comment")
                .content(objectMapper.writeValueAsString(requestLike))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("like2"))
            .andExpect(jsonPath("$.commentId").value("comment1"))
            .andExpect(jsonPath("$.userId").value("user1"));
    }

    @Test
    void testDeleteLike_Success() throws Exception {
        // arrange: simulate that deleteLike in the service succeeds
        doNothing().when(likeService).deleteLike("like1");

        // assert: send DELETE request to /likes/like1
        mockMvc.perform(delete("/likes/{id}", "like1"))
            .andExpect(status().isOk());

        verify(likeService, times(1)).deleteLike("like1");
    }

    @Test
    void testDeleteLike_NotFound() throws Exception {
        // arrange: simulate that deleteLike throws an exception for a non-existing like id
        doThrow(new EntityNotFoundException("Like not found")).when(likeService).deleteLike("like2");

        // assert: send DELETE request and expect a 404 Not Found response from the controller
        mockMvc.perform(delete("/likes/{id}", "like2"))
            .andExpect(status().isNotFound());
    }
}
