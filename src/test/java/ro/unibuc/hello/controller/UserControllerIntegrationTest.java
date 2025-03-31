// package ro.unibuc.hello.controller;

// import com.fasterxml.jackson.databind.ObjectMapper;
// import org.junit.jupiter.api.*;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.http.MediaType;
// import org.springframework.test.context.DynamicPropertyRegistry;
// import org.springframework.test.context.DynamicPropertySource;
// import org.springframework.test.web.servlet.MockMvc;
// import org.testcontainers.containers.MongoDBContainer;
// import org.testcontainers.junit.jupiter.Container;
// import org.testcontainers.junit.jupiter.Testcontainers;
// import ro.unibuc.hello.dto.UserDto;
// import ro.unibuc.hello.service.implementation.UserServiceImpl;

// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// import java.util.Date;

// @SpringBootTest
// @AutoConfigureMockMvc
// @Testcontainers
// @Tag("IntegrationTest")
// public class UserControllerIntegrationTest {

//     @Container
//     public static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0.20")
//             .withExposedPorts(27017)
//             .withSharding();

//     @BeforeAll
//     public static void setUpContainer() {
//         mongoDBContainer.start();
//     }

//     @AfterAll
//     public static void tearDownContainer() {
//         mongoDBContainer.stop();
//     }

//     @DynamicPropertySource
//     static void setProperties(DynamicPropertyRegistry registry) {
//         final String MONGO_URL = "mongodb://localhost:";
//         final String PORT = String.valueOf(mongoDBContainer.getMappedPort(27017));
//         registry.add("mongodb.connection.url", () -> MONGO_URL + PORT);
//     }

//     @Autowired
//     private MockMvc mockMvc;

//     @Autowired
//     private UserServiceImpl userService;

//     private static final ObjectMapper objectMapper = new ObjectMapper();

//     private UserDto user1;
//     private UserDto user2;

//     @BeforeEach
//     public void cleanUpAndAddTestData() {
//         userService.deleteAllUsers();

//         UserController.CreateUserRequest createUserRequest1 = new UserController.CreateUserRequest();
//         createUserRequest1.setUsername("testuser1");
//         createUserRequest1.setEmail("test1@test.com");
//         createUserRequest1.setPassword("pass1");
//         createUserRequest1.setBio("Bio1");
//         createUserRequest1.setProfilePicture("pic1");
//         createUserRequest1.setDateOfBirth(new Date());

//         user1 = userService.createUser(createUserRequest1.toUserDto(), createUserRequest1.getEmail(), createUserRequest1.getPassword());

//         UserController.CreateUserRequest createUserRequest2 = new UserController.CreateUserRequest();
//         createUserRequest2.setUsername("testuser2");
//         createUserRequest2.setEmail("test2@test.com");
//         createUserRequest2.setPassword("pass2");
//         createUserRequest2.setBio("Bio2");
//         createUserRequest2.setProfilePicture("pic2");
//         createUserRequest2.setDateOfBirth(new Date());

//         user2 = userService.createUser(createUserRequest2.toUserDto(), createUserRequest2.getEmail(), createUserRequest2.getPassword());
//     }

//     @Test
//     public void testCreateUser() throws Exception {
//         UserController.CreateUserRequest request = new UserController.CreateUserRequest();
//         request.setUsername("newuser");
//         request.setEmail("newuser@test.com");
//         request.setPassword("newpass");
//         request.setBio("New Bio");
//         request.setProfilePicture("newpic");
//         request.setDateOfBirth(new Date());

//         mockMvc.perform(post("/users/create")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(request)))
//                 .andExpect(status().isCreated())
//                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(jsonPath("$.username").value("newuser"))
//                 .andExpect(jsonPath("$.bio").value("New Bio"))
//                 .andExpect(jsonPath("$.profilePicture").value("newpic"));

//         mockMvc.perform(get("/users/all"))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.length()").value(3));
//     }

//     @Test
//     public void testLogin() throws Exception {
//         UserController.LoginRequest request = new UserController.LoginRequest();
//         request.setUsername("testuser1");
//         request.setPassword("pass1");

//         mockMvc.perform(post("/users/login")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(request)))
//                 .andExpect(status().isOk())
//                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(jsonPath("$.username").value("testuser1"));
//     }

//     @Test
//     public void testGetUserProfile() throws Exception {
//         mockMvc.perform(get("/users/profile/testuser1"))
//                 .andExpect(status().isOk())
//                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(jsonPath("$.username").value("testuser1"));
//     }

//     @Test
//     public void testUpdateUsername() throws Exception {
//         UserController.UpdateUsernameRequest request = new UserController.UpdateUsernameRequest();
//         request.setNewUsername("updatedUser");

//         mockMvc.perform(put("/users/" + user1.getId() + "/username")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(request)))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.username").value("updatedUser"));

//         mockMvc.perform(get("/users/id/" + user1.getId()))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.username").value("updatedUser"));
//     }

//     @Test
//     public void testUpdatePassword() throws Exception {
//         UserController.UpdatePasswordRequest request = new UserController.UpdatePasswordRequest();
//         request.setNewPassword("newPass1");

//         mockMvc.perform(put("/users/" + user1.getId() + "/password")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(request)))
//                 .andExpect(status().isOk());

//         UserController.LoginRequest loginRequest = new UserController.LoginRequest();
//         loginRequest.setUsername("testuser1");
//         loginRequest.setPassword("newPass1");

//         mockMvc.perform(post("/users/login")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(loginRequest)))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.username").value("testuser1"));
//     }

//     @Test
//     public void testUpdateProfilePicture() throws Exception {
//         UserController.UpdateProfilePictureRequest request = new UserController.UpdateProfilePictureRequest();
//         request.setNewProfilePicture("updatedPic");

//         mockMvc.perform(put("/users/" + user1.getId() + "/profile-picture")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(request)))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.profilePicture").value("updatedPic"));
//     }

//     @Test
//     public void testUpdateBio() throws Exception {
//         UserController.UpdateBioRequest request = new UserController.UpdateBioRequest();
//         request.setNewBio("Updated Bio");

//         mockMvc.perform(put("/users/" + user1.getId() + "/bio")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(request)))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.bio").value("Updated Bio"));
//     }

//     @Test
//     public void testDeleteUser() throws Exception {
//         mockMvc.perform(delete("/users/" + user1.getId()))
//                 .andExpect(status().isNoContent());

//         mockMvc.perform(get("/users/all"))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.length()").value(1));
//     }

//     @Test
//     public void testGetAllUsers() throws Exception {
//         mockMvc.perform(get("/users/all"))
//                 .andExpect(status().isOk())
//                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(jsonPath("$.length()").value(2));
//     }

//     @Test
//     public void testGetUserById() throws Exception {
//         mockMvc.perform(get("/users/id/" + user1.getId()))
//                 .andExpect(status().isOk())
//                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(jsonPath("$.username").value("testuser1"));
//     }
// }
