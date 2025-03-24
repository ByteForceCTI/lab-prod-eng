package ro.unibuc.hello.service;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import ro.unibuc.hello.data.FriendshipEntity;
import ro.unibuc.hello.data.FriendshipEntity.FriendshipStatus;
import ro.unibuc.hello.data.repository.FriendshipRepository;
import ro.unibuc.hello.exception.EntityNotFoundException;

@ExtendWith(SpringExtension.class)
class FriendshipServiceTest {

    @Mock
    private FriendshipRepository friendshipRepository;

    @InjectMocks
    private FriendshipService friendshipService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendRequest() {
        // Arrange
        String requesterId = "user1";
        String receiverId = "user2";
        FriendshipEntity savedFriendship = new FriendshipEntity();
        savedFriendship.setUserId1(requesterId);
        savedFriendship.setUserId2(receiverId);
        savedFriendship.setStatus(FriendshipStatus.PENDING);
        // createdAt is set by the service so we don't verify its exact value here

        when(friendshipRepository.save(any(FriendshipEntity.class))).thenReturn(savedFriendship);

        // Act
        FriendshipEntity result = friendshipService.sendRequest(requesterId, receiverId);

        // Assert
        assertNotNull(result);
        assertEquals(requesterId, result.getUserId1());
        assertEquals(receiverId, result.getUserId2());
        assertEquals(FriendshipStatus.PENDING, result.getStatus());
        verify(friendshipRepository, times(1)).save(any(FriendshipEntity.class));
    }

    @Test
    void testAcceptRequest_ExistingRequest() throws EntityNotFoundException {
        // Arrange
        String requesterId = "user1";
        String receiverId = "user2";
        FriendshipEntity existingFriendship = new FriendshipEntity();
        existingFriendship.setUserId1(requesterId);
        existingFriendship.setUserId2(receiverId);
        existingFriendship.setStatus(FriendshipStatus.PENDING);

        FriendshipEntity acceptedFriendship = new FriendshipEntity();
        acceptedFriendship.setUserId1(requesterId);
        acceptedFriendship.setUserId2(receiverId);
        acceptedFriendship.setStatus(FriendshipStatus.ACCEPTED);

        when(friendshipRepository.findByUserId1AndUserId2(requesterId, receiverId))
                .thenReturn(existingFriendship);
        when(friendshipRepository.save(any(FriendshipEntity.class))).thenReturn(acceptedFriendship);

        // Act
        FriendshipEntity result = friendshipService.acceptRequest(requesterId, receiverId);

        // Assert
        assertNotNull(result);
        assertEquals(FriendshipStatus.ACCEPTED, result.getStatus());
        verify(friendshipRepository, times(1)).findByUserId1AndUserId2(requesterId, receiverId);
        verify(friendshipRepository, times(1)).save(existingFriendship);
    }

    @Test
    void testAcceptRequest_NonExistingRequest() {
        // Arrange
        String requesterId = "user1";
        String receiverId = "user2";

        when(friendshipRepository.findByUserId1AndUserId2(requesterId, receiverId))
                .thenReturn(null);

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> friendshipService.acceptRequest(requesterId, receiverId));
        assertTrue(exception.getMessage().contains(requesterId));
        verify(friendshipRepository, times(1)).findByUserId1AndUserId2(requesterId, receiverId);
    }

    @Test
    void testRejectRequest_ExistingRequest() throws EntityNotFoundException {
        // Arrange
        String requesterId = "user1";
        String receiverId = "user2";
        FriendshipEntity existingFriendship = new FriendshipEntity();
        existingFriendship.setUserId1(requesterId);
        existingFriendship.setUserId2(receiverId);

        when(friendshipRepository.findByUserId1AndUserId2(requesterId, receiverId))
                .thenReturn(existingFriendship);

        // Act
        friendshipService.rejectRequest(requesterId, receiverId);

        // Assert
        verify(friendshipRepository, times(1)).findByUserId1AndUserId2(requesterId, receiverId);
        verify(friendshipRepository, times(1)).delete(existingFriendship);
    }

    @Test
    void testRejectRequest_NonExistingRequest() {
        // Arrange
        String requesterId = "user1";
        String receiverId = "user2";

        when(friendshipRepository.findByUserId1AndUserId2(requesterId, receiverId))
                .thenReturn(null);

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> friendshipService.rejectRequest(requesterId, receiverId));
        verify(friendshipRepository, times(1)).findByUserId1AndUserId2(requesterId, receiverId);
    }

    @Test
    void testBlockFriend_ExistingFriendship() throws EntityNotFoundException {
        // Arrange
        String requesterId = "user1";
        String receiverId = "user2";
        FriendshipEntity existingFriendship = new FriendshipEntity();
        existingFriendship.setUserId1(requesterId);
        existingFriendship.setUserId2(receiverId);
        existingFriendship.setStatus(FriendshipStatus.PENDING);

        FriendshipEntity blockedFriendship = new FriendshipEntity();
        blockedFriendship.setUserId1(requesterId);
        blockedFriendship.setUserId2(receiverId);
        blockedFriendship.setStatus(FriendshipStatus.BLOCKED);

        when(friendshipRepository.findByUserId1AndUserId2(requesterId, receiverId))
                .thenReturn(existingFriendship);
        when(friendshipRepository.save(any(FriendshipEntity.class))).thenReturn(blockedFriendship);

        // Act
        FriendshipEntity result = friendshipService.blockFriend(requesterId, receiverId);

        // Assert
        assertNotNull(result);
        assertEquals(FriendshipStatus.BLOCKED, result.getStatus());
        verify(friendshipRepository, times(1)).findByUserId1AndUserId2(requesterId, receiverId);
        verify(friendshipRepository, times(1)).save(existingFriendship);
    }

    @Test
    void testBlockFriend_NonExistingFriendship() {
        // Arrange
        String requesterId = "user1";
        String receiverId = "user2";

        when(friendshipRepository.findByUserId1AndUserId2(requesterId, receiverId))
                .thenReturn(null);

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> friendshipService.blockFriend(requesterId, receiverId));
        verify(friendshipRepository, times(1)).findByUserId1AndUserId2(requesterId, receiverId);
    }

    @Test
    void testGetFriendships() {
        // Arrange
        String userId = "user1";
        FriendshipEntity f1 = new FriendshipEntity();
        f1.setUserId1(userId);
        f1.setUserId2("user2");

        FriendshipEntity f2 = new FriendshipEntity();
        f2.setUserId1("user3");
        f2.setUserId2(userId);

        List<FriendshipEntity> friendships = Arrays.asList(f1, f2);
        when(friendshipRepository.findByUserId1OrUserId2(userId, userId)).thenReturn(friendships);

        // Act
        List<FriendshipEntity> result = friendshipService.getFriendships(userId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(friendshipRepository, times(1)).findByUserId1OrUserId2(userId, userId);
    }

    @Test
    void testGetFriendshipBetween_FirstOrderFound() {
        // Arrange
        String userId1 = "user1";
        String userId2 = "user2";
        FriendshipEntity friendship = new FriendshipEntity();
        friendship.setUserId1(userId1);
        friendship.setUserId2(userId2);

        when(friendshipRepository.findByUserId1AndUserId2(userId1, userId2)).thenReturn(friendship);

        // Act
        FriendshipEntity result = friendshipService.getFriendshipBetween(userId1, userId2);

        // Assert
        assertNotNull(result);
        assertEquals(userId1, result.getUserId1());
        assertEquals(userId2, result.getUserId2());
        verify(friendshipRepository, times(1)).findByUserId1AndUserId2(userId1, userId2);
    }

    @Test
    void testGetFriendshipBetween_SecondOrderFound() {
        // Arrange
        String userId1 = "user1";
        String userId2 = "user2";
        // First call returns null
        when(friendshipRepository.findByUserId1AndUserId2(userId1, userId2)).thenReturn(null);
        // Second call returns the friendship
        FriendshipEntity friendship = new FriendshipEntity();
        friendship.setUserId1(userId2);
        friendship.setUserId2(userId1);
        when(friendshipRepository.findByUserId1AndUserId2(userId2, userId1)).thenReturn(friendship);

        // Act
        FriendshipEntity result = friendshipService.getFriendshipBetween(userId1, userId2);

        // Assert
        assertNotNull(result);
        assertEquals(userId2, result.getUserId1());
        assertEquals(userId1, result.getUserId2());
        verify(friendshipRepository, times(1)).findByUserId1AndUserId2(userId1, userId2);
        verify(friendshipRepository, times(1)).findByUserId1AndUserId2(userId2, userId1);
    }

    @Test
    void testGetFriendshipBetween_NotFound() {
        // Arrange
        String userId1 = "user1";
        String userId2 = "user2";
        when(friendshipRepository.findByUserId1AndUserId2(userId1, userId2)).thenReturn(null);
        when(friendshipRepository.findByUserId1AndUserId2(userId2, userId1)).thenReturn(null);

        // Act
        FriendshipEntity result = friendshipService.getFriendshipBetween(userId1, userId2);

        // Assert
        assertNull(result);
        verify(friendshipRepository, times(1)).findByUserId1AndUserId2(userId1, userId2);
        verify(friendshipRepository, times(1)).findByUserId1AndUserId2(userId2, userId1);
    }
}
