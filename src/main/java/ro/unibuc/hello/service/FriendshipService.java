package ro.unibuc.hello.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ro.unibuc.hello.data.FriendshipEntity;
import ro.unibuc.hello.data.FriendshipEntity.FriendshipStatus;
import ro.unibuc.hello.data.repository.FriendshipRepository;
import ro.unibuc.hello.exception.EntityNotFoundException;

import java.util.Date;
import java.util.List;

@Component
public class FriendshipService {

    @Autowired
    private FriendshipRepository friendshipRepository;

    public FriendshipEntity sendRequest(String requesterId, String receiverId) {
        FriendshipEntity friendship = new FriendshipEntity();
        friendship.setUserId1(requesterId);
        friendship.setUserId2(receiverId);
        friendship.setStatus(FriendshipStatus.PENDING);
        friendship.setCreatedAt(new Date());
        return friendshipRepository.save(friendship);
    }

    public FriendshipEntity acceptRequest(String requesterId, String receiverId) throws EntityNotFoundException {
        FriendshipEntity friendship = friendshipRepository.findByUserId1AndUserId2(requesterId, receiverId);
        if (friendship == null) {
            throw new EntityNotFoundException("Friendship request not found for users: " 
                    + requesterId + " and " + receiverId);
        }
        friendship.setStatus(FriendshipStatus.ACCEPTED);
        return friendshipRepository.save(friendship);
    }

    public void rejectRequest(String requesterId, String receiverId) throws EntityNotFoundException {
        FriendshipEntity friendship = friendshipRepository.findByUserId1AndUserId2(requesterId, receiverId);
        if (friendship == null) {
            throw new EntityNotFoundException("Friendship request not found for users: " 
                    + requesterId + " and " + receiverId);
        }
        friendshipRepository.delete(friendship);
    }

    public FriendshipEntity blockFriend(String requesterId, String receiverId) throws EntityNotFoundException {
        FriendshipEntity friendship = friendshipRepository.findByUserId1AndUserId2(requesterId, receiverId);
        if (friendship == null) {
            throw new EntityNotFoundException("Friendship not found for users: " 
                    + requesterId + " and " + receiverId);
        }
        friendship.setStatus(FriendshipStatus.BLOCKED);
        return friendshipRepository.save(friendship);
    }

    public List<FriendshipEntity> getFriendships(String userId) {
        return friendshipRepository.findByUserId1OrUserId2(userId, userId);
    }
}
