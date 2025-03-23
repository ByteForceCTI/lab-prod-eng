package ro.unibuc.hello.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.unibuc.hello.data.LikeEntity;
import ro.unibuc.hello.data.repository.LikeRepository;
import ro.unibuc.hello.exception.EntityNotFoundException;

import java.util.Date;
import java.util.List;

@Service
public class LikeService {

    @Autowired
    private LikeRepository likeRepository;

    // function to add a like to a post
    public LikeEntity createPostLike(String postId, String userId) {
        if (likeRepository.findByUserIdAndPostId(userId, postId) != null) {
            throw new IllegalArgumentException("User has already liked this post.");
        } //
        LikeEntity like = new LikeEntity();
        like.setPostId(postId);
        like.setUserId(userId);
        like.setCommentId(null);
        like.setCreatedAt(new Date());
        return likeRepository.save(like);
    }

    // function to add a like to a comment
    public LikeEntity createCommentLike(String commentId, String userId) {
        if (likeRepository.findByUserIdAndCommentId(userId, commentId) != null) {
            throw new IllegalArgumentException("User has already liked this comment.");
        }
        LikeEntity like = new LikeEntity();
        like.setPostId(null);
        like.setCommentId(commentId);
        like.setUserId(userId);
        like.setCreatedAt(new Date());
        return likeRepository.save(like);
    }

    // function to count likes for a post
    public long countLikesForPost(String postId) {
        List<LikeEntity> likes = likeRepository.findByPostId(postId);
        return likes.size();
    }

    // function to count likes for a comment
    public long countLikesForComment(String commentId) {
        List<LikeEntity> likes = likeRepository.findByCommentId(commentId);
        return likes.size();
    }

    // function to delete a like by its id
    private void deleteLike(String likeId) throws EntityNotFoundException {
        LikeEntity like = likeRepository.findById(likeId)
                .orElseThrow(() -> new EntityNotFoundException("Like not found: " + likeId));
        likeRepository.delete(like);
    }

    // function to delete all likes for a post
    public void deletePostLikes(String postId) throws EntityNotFoundException {
        List<LikeEntity> likes = likeRepository.findByPostId(postId);
        for (LikeEntity like : likes) {
            deleteLike(like.getId());
        }
    }

    // function to delete all likes for a comment
    public void deleteCommentLikes(String commentId) throws EntityNotFoundException {
        List<LikeEntity> likes = likeRepository.findByCommentId(commentId);
        for (LikeEntity like : likes) {
            deleteLike(like.getId());
        }
    }
    
}
