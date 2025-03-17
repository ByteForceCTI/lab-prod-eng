package ro.unibuc.hello.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ro.unibuc.hello.data.FriendshipEntity;
import ro.unibuc.hello.exception.EntityNotFoundException;
import ro.unibuc.hello.service.FriendshipService;

import java.util.List;

@Controller
public class FriendshipController {

    @Autowired
    private FriendshipService friendshipService;

    // Send friend request
    @PostMapping("/friendships")
    @ResponseBody
    public FriendshipEntity sendFriendRequest(@RequestBody FriendshipEntity friendship) {
        // Expecting JSON with userId1 and userId2 fields; status and createdAt are set in the service.
        return friendshipService.sendRequest(friendship.getUserId1(), friendship.getUserId2());
    }

    // Accept friend request
    @PutMapping("/friendships/accept")
    @ResponseBody
    public FriendshipEntity acceptFriendRequest(@RequestParam String userId1, @RequestParam String userId2) throws EntityNotFoundException {
        return friendshipService.acceptRequest(userId1, userId2);
    }

    // Reject (delete) friend request 
    @DeleteMapping("/friendships/reject")
    @ResponseBody
    public void rejectFriendRequest(@RequestParam String userId1, @RequestParam String userId2) throws EntityNotFoundException {
        friendshipService.rejectRequest(userId1, userId2);
    }

    // Block friend
    @PutMapping("/friendships/block")
    @ResponseBody
    public FriendshipEntity blockFriend(@RequestParam String userId1, @RequestParam String userId2) throws EntityNotFoundException {
        return friendshipService.blockFriend(userId1, userId2);
    }

    // Get all friendships for a given user 
    @GetMapping("/friendships")
    @ResponseBody
    public List<FriendshipEntity> getFriendships(@RequestParam String userId) {
        return friendshipService.getFriendships(userId);
    }
}
