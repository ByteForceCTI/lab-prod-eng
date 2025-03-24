package ro.unibuc.hello.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ro.unibuc.hello.data.LikeEntity;
import ro.unibuc.hello.exception.EntityNotFoundException;
import ro.unibuc.hello.service.LikeService;

@RestController
@RequestMapping("/likes")
public class LikeController {

    @Autowired
    private LikeService likeService;

    // endpoint to add a like to a post
    // expects a LikeEntity in the request body with postId and userId
    @PostMapping("/post")
    public LikeEntity createPostLike(@RequestBody LikeEntity like) {
        return likeService.createPostLike(like.getPostId(), like.getUserId());
    }

    // endpoint to add a like to a comment
    // expects a LikeEntity in the request body with commentId and userId
    @PostMapping("/comment")
    public LikeEntity createCommentLike(@RequestBody LikeEntity like) {
        return likeService.createCommentLike(like.getCommentId(), like.getUserId());
    }

    // endpoint to delete a like by id
    @DeleteMapping("/{id}")
    public void deleteLike(@PathVariable("id") String likeId) throws EntityNotFoundException {
        likeService.deleteLike(likeId);
    }

}
