package ro.unibuc.hello.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.*;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ro.unibuc.hello.data.CommentEntity;
import ro.unibuc.hello.data.repository.CommentRepository;
import ro.unibuc.hello.service.CommentService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Tag("IntegrationTest")
public class CommentControllerIntegrationTest {

    @Container
    public static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0.20")
            .withExposedPorts(27017)
            .withSharding();

    @BeforeAll
    public static void setUpContainer() {
        mongoDBContainer.start();
    }

    @AfterAll
    public static void tearDownContainer() {
        mongoDBContainer.stop();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        final String MONGO_URL = "mongodb://localhost:";
        final String PORT = String.valueOf(mongoDBContainer.getMappedPort(27017));
        registry.add("mongodb.connection.url", () -> MONGO_URL + PORT);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentRepository commentRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void cleanUp() {
        commentRepository.deleteAll();
    }

    @Test
    public void testCreateComment() throws Exception {
        mockMvc.perform(post("/comments")
                .content("{\"postId\":\"post1\", \"userId\":\"user1\", \"content\":\"This is a comment\"}")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.postId").value("post1"))
            .andExpect(jsonPath("$.userId").value("user1"))
            .andExpect(jsonPath("$.content").value("This is a comment"));
    }

    @Test
    public void testCreateNestedComment() throws Exception {
        String parentResponse = mockMvc.perform(post("/comments")
                .content("{\"postId\":\"post1\", \"userId\":\"user1\", \"content\":\"Parent comment\"}")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        CommentEntity parentComment = objectMapper.readValue(parentResponse, CommentEntity.class);
        String parentId = parentComment.getId();

        mockMvc.perform(post("/comments/nested")
                .content("{\"postId\":\"post1\", \"userId\":\"user2\", \"content\":\"This is a nested comment\", \"parentCommentId\":\"" + parentId + "\"}")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.postId").value("post1"))
            .andExpect(jsonPath("$.userId").value("user2"))
            .andExpect(jsonPath("$.content").value("This is a nested comment"))
            .andExpect(jsonPath("$.parentCommentId").value(parentId));
    }

    @Test
    public void testCreateNestedComment_EntityNotFound() throws Exception {
        String nonExistentParent = "nonExistingParent";
        mockMvc.perform(post("/comments/nested")
                .content("{\"postId\":\"post1\", \"userId\":\"user2\", \"content\":\"This is a nested comment\", \"parentCommentId\":\"" + nonExistentParent + "\"}")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    public void testCountCommentsForPost() throws Exception {
        mockMvc.perform(post("/comments")
                .content("{\"postId\":\"post1\", \"userId\":\"user1\", \"content\":\"Comment 1\"}")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        mockMvc.perform(post("/comments")
                .content("{\"postId\":\"post1\", \"userId\":\"user2\", \"content\":\"Comment 2\"}")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        mockMvc.perform(get("/comments/count")
                .param("postId", "post1"))
            .andExpect(status().isOk())
            .andExpect(content().string("2"));
    }

    @Test
    public void testEditComment() throws Exception {
        String response = mockMvc.perform(post("/comments")
                .content("{\"postId\":\"post1\", \"userId\":\"user1\", \"content\":\"Original comment\"}")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        CommentEntity createdComment = objectMapper.readValue(response, CommentEntity.class);
        String commentId = createdComment.getId();

        mockMvc.perform(put("/comments/{id}", commentId)
                .content("{\"content\":\"Edited comment\"}")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(commentId))
            .andExpect(jsonPath("$.content").value("Edited comment"));
    }

    @Test
    public void testDeleteComment() throws Exception {
        String response = mockMvc.perform(post("/comments")
                .content("{\"postId\":\"post1\", \"userId\":\"user1\", \"content\":\"Comment to delete\"}")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        CommentEntity createdComment = objectMapper.readValue(response, CommentEntity.class);
        String commentId = createdComment.getId();

        mockMvc.perform(delete("/comments/{id}", commentId))
            .andExpect(status().isOk());

        mockMvc.perform(get("/comments/count")
                .param("postId", "post1"))
            .andExpect(status().isOk())
            .andExpect(content().string("0"));
    }
}