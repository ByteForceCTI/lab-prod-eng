package ro.unibuc.hello.service.implementation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mindrot.jbcrypt.BCrypt;

import ro.unibuc.hello.data.UserEntity;
import ro.unibuc.hello.data.repository.UserRepository;
import ro.unibuc.hello.service.*;
import ro.unibuc.hello.service.implementation.UserServiceImpl;
import ro.unibuc.hello.dto.*;


import java.time.LocalDate;
import java.util.Date;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;

public class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testAddUser() {
        UserDto userDto = new UserDto();
        userDto.setUsername("testUser");
        userDto.setBio("Test bio");
        userDto.setProfilePicture("test.jpg");
        userDto.setDateOfBirth(new Date());

        UserEntity savedUser = new UserEntity();
        savedUser.setId("1");
        savedUser.setUsername("testUser");
        savedUser.setBio("Test bio");
        savedUser.setProfilePicture("test.jpg");
        savedUser.setEmail("test@example.com");
        savedUser.setDateOfBirth(new Date());
        savedUser.setPasswordHash(BCrypt.hashpw("password123", BCrypt.gensalt()));

        when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);

        UserDto result = userService.createUser(userDto, "test@example.com", "password123");

        assertEquals("testUser", result.getUsername());
        assertEquals("Test bio", result.getBio());
        assertEquals("test.jpg", result.getProfilePicture());
        assertNotNull(result.getDateOfBirth());
    }

    @Test
    public void testLoginSuccess() {
        UserEntity user = new UserEntity();
        user.setId("1");
        user.setUsername("testUser");
        user.setBio("Test bio");
        user.setProfilePicture("test.jpg");
        user.setDateOfBirth(new Date());
        String originalPassword = "password123";
        user.setPasswordHash(BCrypt.hashpw(originalPassword, BCrypt.gensalt()));

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));

        UserDto result = userService.login("testUser", originalPassword);
        assertEquals("testUser", result.getUsername());
    }

    @Test
    public void testLoginWrongPassword() {
        UserEntity user = new UserEntity();
        user.setId("1");
        user.setUsername("testUser");
        String correctPassword = "password123";
        user.setPasswordHash(BCrypt.hashpw(correctPassword, BCrypt.gensalt()));

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.login("testUser", "wrongPassword");
        });
        assertEquals("Wrong password", exception.getMessage());
    }

    @Test
    public void testLoginUserNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.login("nonexistent", "password");
        });
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    public void testUpdateUsernameSuccess() {
        UserEntity user = new UserEntity();
        user.setId("1");
        user.setUsername("oldUser");
        user.setBio("Bio");
        user.setProfilePicture("pic.jpg");
        user.setDateOfBirth(new Date());

        when(userRepository.findById("1")).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserEntity.class))).thenReturn(user);

        UserDto result = userService.updateUsername("1", "newUser");
        assertEquals("newUser", result.getUsername());
    }

    @Test
    public void testUpdatePasswordSuccess() {
        UserEntity user = new UserEntity();
        user.setId("1");
        user.setUsername("testUser");
        user.setPasswordHash(BCrypt.hashpw("oldPassword", BCrypt.gensalt()));
        user.setBio("Bio");
        user.setProfilePicture("pic.jpg");
        user.setDateOfBirth(new Date());

        when(userRepository.findById("1")).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserEntity.class))).thenReturn(user);

        UserDto result = userService.updatePassword("1", "newPassword");
        verify(userRepository).save(user);
    }

    @Test
    public void testUpdateProfilePictureSuccess() {
        UserEntity user = new UserEntity();
        user.setId("1");
        user.setUsername("testUser");
        user.setProfilePicture("old.jpg");
        user.setBio("Bio");
        user.setDateOfBirth(new Date());

        when(userRepository.findById("1")).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserEntity.class))).thenReturn(user);

        UserDto result = userService.updateProfilePicture("1", "new.jpg");
        assertEquals("new.jpg", result.getProfilePicture());
    }

    @Test
    public void testUpdateBioSuccess() {
        UserEntity user = new UserEntity();
        user.setId("1");
        user.setUsername("testUser");
        user.setBio("Old bio");
        user.setProfilePicture("pic.jpg");
        user.setDateOfBirth(new Date());

        when(userRepository.findById("1")).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserEntity.class))).thenReturn(user);

        UserDto result = userService.updateBio("1", "New bio");
        assertEquals("New bio", result.getBio());
    }

    @Test
    public void testDeleteUser() {
        userService.deleteUser("1");
        verify(userRepository).deleteById("1");
    }

    @Test
    public void testGetAllUsers() {
        UserEntity user1 = new UserEntity();
        user1.setId("1");
        user1.setUsername("user1");
        user1.setBio("Bio1");
        user1.setProfilePicture("pic1.jpg");
        user1.setDateOfBirth(new Date());

        UserEntity user2 = new UserEntity();
        user2.setId("2");
        user2.setUsername("user2");
        user2.setBio("Bio2");
        user2.setProfilePicture("pic2.jpg");
        user2.setDateOfBirth(new Date());

        List<UserEntity> users = Arrays.asList(user1, user2);
        when(userRepository.findAll()).thenReturn(users);

        List<UserDto> result = userService.getAllUsers();
        assertEquals(2, result.size());
    }

    @Test
    public void testGetUserByIdSuccess() {
        UserEntity user = new UserEntity();
        user.setId("1");
        user.setUsername("testUser");
        user.setBio("Bio");
        user.setProfilePicture("pic.jpg");
        user.setDateOfBirth(new Date());

        when(userRepository.findById("1")).thenReturn(Optional.of(user));

        UserDto result = userService.getUserById("1");
        assertEquals("testUser", result.getUsername());
    }

    @Test
    public void testGetUserByIdNotFound() {
        when(userRepository.findById("1")).thenReturn(Optional.empty());
        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.getUserById("1");
        });
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    public void testGetUserByUsernameSuccess() {
        UserEntity user = new UserEntity();
        user.setId("1");
        user.setUsername("testUser");
        user.setBio("Bio");
        user.setProfilePicture("pic.jpg");
        user.setDateOfBirth(new Date());

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));

        UserDto result = userService.getUserByUsername("testUser");
        assertEquals("testUser", result.getUsername());
    }

    @Test
    public void testGetUserByUsernameNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.getUserByUsername("nonexistent");
        });
        assertEquals("User not found", exception.getMessage());
    }
}

