package ro.unibuc.hello.controller;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ro.unibuc.hello.data.FriendshipEntity;
import ro.unibuc.hello.data.FriendshipEntity.FriendshipStatus;
import ro.unibuc.hello.exception.EntityNotFoundException;
import ro.unibuc.hello.service.FriendshipService;

class FriendshipControllerTest {

    @Mock
    private FriendshipService friendshipService;

    @InjectMocks
    private FriendshipController friendshipController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(friendshipController).build();
    }

    @Test
    void test_sendFriendRequest() throws Exception {
        // Arrange
        FriendshipEntity inputFriendship = new FriendshipEntity();
        inputFriendship.setUserId1("user1");
        inputFriendship.setUserId2("user2");
        
        FriendshipEntity savedFriendship = new FriendshipEntity();
        savedFriendship.setUserId1("user1");
        savedFriendship.setUserId2("user2");
        savedFriendship.setStatus(FriendshipStatus.PENDING);
        
        when(friendshipService.sendRequest(eq("user1"), eq("user2"))).thenReturn(savedFriendship);
        
        // Act & Assert
        mockMvc.perform(post("/friendships")
                .content("{\"userId1\":\"user1\", \"userId2\":\"user2\"}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId1").value("user1"))
                .andExpect(jsonPath("$.userId2").value("user2"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void test_acceptFriendRequest() throws Exception {
        // Arrange
        FriendshipEntity acceptedFriendship = new FriendshipEntity();
        acceptedFriendship.setUserId1("user1");
        acceptedFriendship.setUserId2("user2");
        acceptedFriendship.setStatus(FriendshipStatus.ACCEPTED);
        
        when(friendshipService.acceptRequest(eq("user1"), eq("user2"))).thenReturn(acceptedFriendship);
        
        // Act & Assert
        mockMvc.perform(put("/friendships/accept")
                .param("userId1", "user1")
                .param("userId2", "user2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId1").value("user1"))
                .andExpect(jsonPath("$.userId2").value("user2"))
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    void test_acceptFriendRequest_NotFound() throws Exception {
        // Arrange
        when(friendshipService.acceptRequest(eq("user1"), eq("user2")))
                .thenThrow(new EntityNotFoundException("Friendship request not found"));
        
        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> 
                friendshipController.acceptFriendRequest("user1", "user2"));
    }

    @Test
    void test_rejectFriendRequest() throws Exception {
        // Arrange
        // No return value is expected; just verify the deletion
        doNothing().when(friendshipService).rejectRequest(eq("user1"), eq("user2"));
        
        // Act & Assert
        mockMvc.perform(delete("/friendships/reject")
                .param("userId1", "user1")
                .param("userId2", "user2"))
                .andExpect(status().isOk());
        
        verify(friendshipService, times(1)).rejectRequest("user1", "user2");
    }

    @Test
    void test_blockFriend() throws Exception {
        // Arrange
        FriendshipEntity blockedFriendship = new FriendshipEntity();
        blockedFriendship.setUserId1("user1");
        blockedFriendship.setUserId2("user2");
        blockedFriendship.setStatus(FriendshipStatus.BLOCKED);
        
        when(friendshipService.blockFriend(eq("user1"), eq("user2"))).thenReturn(blockedFriendship);
        
        // Act & Assert
        mockMvc.perform(put("/friendships/block")
                .param("userId1", "user1")
                .param("userId2", "user2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId1").value("user1"))
                .andExpect(jsonPath("$.userId2").value("user2"))
                .andExpect(jsonPath("$.status").value("BLOCKED"));
    }

    @Test
    void test_getFriendships() throws Exception {
        // Arrange
        FriendshipEntity f1 = new FriendshipEntity();
        f1.setUserId1("user1");
        f1.setUserId2("user2");
        f1.setStatus(FriendshipStatus.PENDING);
        
        FriendshipEntity f2 = new FriendshipEntity();
        f2.setUserId1("user3");
        f2.setUserId2("user1");
        f2.setStatus(FriendshipStatus.ACCEPTED);
        
        List<FriendshipEntity> friendships = Arrays.asList(f1, f2);
        when(friendshipService.getFriendships("user1")).thenReturn(friendships);
        
        // Act & Assert
        mockMvc.perform(get("/friendships")
                .param("userId", "user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId1").value("user1"))
                .andExpect(jsonPath("$[0].userId2").value("user2"))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[1].userId1").value("user3"))
                .andExpect(jsonPath("$[1].userId2").value("user1"))
                .andExpect(jsonPath("$[1].status").value("ACCEPTED"));
    }
}
