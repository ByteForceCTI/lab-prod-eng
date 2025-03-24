package ro.unibuc.hello.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ro.unibuc.hello.data.*;
import ro.unibuc.hello.dto.*;
import ro.unibuc.hello.service.*;
import ro.unibuc.hello.service.implementation.UserServiceImpl;
import ro.unibuc.hello.controller.UserController;

import java.util.Date;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class UserControllerTest {
    @Mock
    private UserServiceImpl userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAllUsers() {
        List<UserDto> mockUsers = Arrays.asList(
                createUserDto("user1", "Bio1", "pic1.jpg"),
                createUserDto("user2",  "Bio2", "pic2.jpg")
        );
        when(userService.getAllUsers()).thenReturn(mockUsers);

        ResponseEntity<List<UserDto>> response = userController.getAllUsers();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertIterableEquals(mockUsers, response.getBody());
    }

    @Test
    public void testGetUserById() {
        UserDto mockUser = createUserDto("user1", "Bio1", "pic1.jpg");
        when(userService.getUserById("1")).thenReturn(mockUser);

        ResponseEntity<UserDto> response = userController.getUserById("1");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("user1", response.getBody().getUsername());
    }

    @Test
    public void testCreateUser() {
        UserController.CreateUserRequest req = new UserController.CreateUserRequest();
        req.setUsername("user1");
        req.setBio("Bio1");
        req.setProfilePicture("pic1.jpg");
        req.setPassword("password");
        req.setEmail("user1@example.com");
        req.setDateOfBirth(new Date());

        UserDto mockUser = createUserDto("user1", "Bio1", "pic1.jpg");
        when(userService.createUser(any(UserDto.class), eq("user1@example.com"), eq("password"))).thenReturn(mockUser);

        ResponseEntity<UserDto> response = userController.createUser(req);
        System.out.println(response.getBody());
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        //assertEquals("user1", response.getUsername());
    }

    @Test
    public void testLogin() {
        UserController.LoginRequest req = new UserController.LoginRequest();
        req.setUsername("user1");
        req.setPassword("password");

        UserDto mockUser = createUserDto("user1", "Bio1", "pic1.jpg");
        when(userService.login("user1", "password")).thenReturn(mockUser);

        ResponseEntity<UserDto> response = userController.login(req);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("user1", response.getBody().getUsername());
    }

    @Test
    public void testGetUserProfile() {
        UserDto mockUser = createUserDto("user1", "Bio1", "pic1.jpg");
        when(userService.getUserProfile("user1")).thenReturn(mockUser);

        ResponseEntity<UserDto> response = userController.getUserProfile("user1");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("user1", response.getBody().getUsername());
    }

    @Test
    public void testUpdateUsername() {
        UserController.UpdateUsernameRequest req = new UserController.UpdateUsernameRequest();
        req.setNewUsername("newUser");

        UserDto mockUser = createUserDto("newUser", "Bio1", "pic1.jpg");
        when(userService.updateUsername("1", "newUser")).thenReturn(mockUser);

        ResponseEntity<UserDto> response = userController.updateUsername("1", req);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("newUser", response.getBody().getUsername());
    }

    @Test
    public void testUpdatePassword() {
        UserController.UpdatePasswordRequest req = new UserController.UpdatePasswordRequest();
        req.setNewPassword("newPassword");

        UserDto mockUser = createUserDto("user1", "Bio1", "pic1.jpg");
        when(userService.updatePassword("1", "newPassword")).thenReturn(mockUser);

        ResponseEntity<UserDto> response = userController.updatePassword("1", req);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testUpdateProfilePicture() {
        UserController.UpdateProfilePictureRequest req = new UserController.UpdateProfilePictureRequest();
        req.setNewProfilePicture("newPic.jpg");

        UserDto mockUser = createUserDto("user1",  "Bio1", "newPic.jpg");
        when(userService.updateProfilePicture("1", "newPic.jpg")).thenReturn(mockUser);

        ResponseEntity<UserDto> response = userController.updateProfilePicture("1", req);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("newPic.jpg", response.getBody().getProfilePicture());
    }

    @Test
    public void testUpdateBio() {
        UserController.UpdateBioRequest req = new UserController.UpdateBioRequest();
        req.setNewBio("New bio");

        UserDto mockUser = createUserDto("user1",  "New bio", "pic1.jpg");
        when(userService.updateBio("1", "New bio")).thenReturn(mockUser);

        ResponseEntity<UserDto> response = userController.updateBio("1", req);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("New bio", response.getBody().getBio());
    }

    @Test
    public void testDeleteUser() {
        ResponseEntity<Void> response = userController.deleteUser("1");
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userService).deleteUser("1");
    }

    private UserDto createUserDto(String username, String bio, String profilePicture) {
        UserDto userDto = new UserDto();
        userDto.setUsername(username);
        userDto.setDateOfBirth(new Date());
        userDto.setBio(bio);
        userDto.setProfilePicture(profilePicture);
        return userDto;
    }
}
