package ro.unibuc.hello.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.mindrot.jbcrypt.BCrypt;
import ro.unibuc.hello.data.PostEntity;
import ro.unibuc.hello.data.PostEntity.PostVisibility;
import ro.unibuc.hello.data.UserEntity;
import ro.unibuc.hello.dto.PostDto;
import ro.unibuc.hello.data.repository.PostRepository;
import ro.unibuc.hello.data.repository.UserRepository;
import ro.unibuc.hello.service.implementation.UserServiceImpl;

import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Tag("IntegrationTest")
public class PostControllerIntegrationTest {

    @Container
    public static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0.20")
            .withExposedPorts(27017)
            .withSharding();

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        final String MONGO_URL = "mongodb://host.docker.internal:";
        final String PORT = String.valueOf(mongoDBContainer.getMappedPort(27017));
        registry.add("mongodb.connection.url", () -> MONGO_URL + PORT);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private ObjectMapper objectMapper;

    private final String validUsername = "testUser";
    private final String validPassword = "testPassword";
    
    @BeforeAll
    public static void startContainer() {
        mongoDBContainer.start();
    }

    @AfterAll
    public static void stopContainer() {
        mongoDBContainer.stop();
    }

    @BeforeEach
    public void setUp() {
        postRepository.deleteAll();
        userRepository.deleteAll();

        UserEntity testUser = new UserEntity();
        testUser.setId("user1");
        testUser.setUsername(validUsername);
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash(BCrypt.hashpw(validPassword, BCrypt.gensalt()));
        userRepository.save(testUser);
    }

    private String obtainAccessToken() {
        return userService.loginJWT(validUsername, validPassword);
    } // get jwt for authentication on each request

    @Test
    public void testCreatePost() throws Exception {
        String validToken = obtainAccessToken();

        // add post
        PostEntity postRequest = new PostEntity();
        postRequest.setContent("Integration test post");
        postRequest.setMediaUrl("http://example.com/media");
        postRequest.setVisibility(PostVisibility.PUBLIC);

        mockMvc.perform(post("/posts")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postRequest)))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").value("Integration test post"))
            .andExpect(jsonPath("$.userId").value("user1"));
    }

    @Test
    public void testGetAllPosts() throws Exception {
        String validToken = obtainAccessToken();

        // add post
        PostEntity post = new PostEntity();
        post.setUserId("user1");
        post.setContent("Integration post 1");
        post.setMediaUrl("http://example.com/1");
        post.setVisibility(PostVisibility.PUBLIC);
        post.setCreatedAt(new Date());
        post.setUpdatedAt(new Date());
        postRepository.save(post);

        mockMvc.perform(get("/posts")
                .header("Authorization", "Bearer " + validToken))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].content").value("Integration post 1"));
    }

    @Test
    public void testGetPostById() throws Exception {
        String validToken = obtainAccessToken();

        PostEntity post = new PostEntity();
        post.setUserId("user1");
        post.setContent("Integration post by id");
        post.setMediaUrl("http://example.com/2");
        post.setVisibility(PostVisibility.PUBLIC);
        post.setCreatedAt(new Date());
        post.setUpdatedAt(new Date());
        post = postRepository.save(post);

        mockMvc.perform(get("/posts/{id}", post.getId())
                .header("Authorization", "Bearer " + validToken))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").value("Integration post by id"));
    }

    @Test
    public void testUpdatePost() throws Exception {
        String validToken = obtainAccessToken();

        // add post
        PostEntity post = new PostEntity();
        post.setUserId("user1");
        post.setContent("Old integration content");
        post.setMediaUrl("http://example.com/old");
        post.setVisibility(PostVisibility.PUBLIC);
        post.setCreatedAt(new Date());
        post.setUpdatedAt(new Date());
        post = postRepository.save(post);

        // update post and send the request
        PostEntity updateRequest = new PostEntity();
        updateRequest.setContent("Updated integration content");
        updateRequest.setMediaUrl("http://example.com/new");
        updateRequest.setVisibility(PostVisibility.PUBLIC);

        mockMvc.perform(put("/posts/{id}", post.getId())
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").value("Updated integration content"));
    }

    @Test
    public void testDeletePost() throws Exception {
        String validToken = obtainAccessToken();

        PostEntity post = new PostEntity();
        post.setUserId("user1");
        post.setContent("Post to be deleted");
        post.setMediaUrl("http://example.com/delete");
        post.setVisibility(PostVisibility.PUBLIC);
        post.setCreatedAt(new Date());
        post.setUpdatedAt(new Date());
        post = postRepository.save(post);

        mockMvc.perform(delete("/posts/{id}", post.getId())
                .header("Authorization", "Bearer " + validToken))
            .andExpect(status().isNoContent());
    }
}
