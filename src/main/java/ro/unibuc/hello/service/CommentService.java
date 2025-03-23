package ro.unibuc.hello.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ro.unibuc.hello.data.CommentEntity;
import ro.unibuc.hello.data.repository.CommentRepository;
import ro.unibuc.hello.exception.EntityNotFoundException;

import java.util.Date;
import java.util.List;

@Component
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private LikeService likeService;

    // function to create a comment (top-level, no parent)
    public CommentEntity createComment(String postId, String userId, String content) {
        CommentEntity comment = new CommentEntity();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setParentCommentId(null);
        comment.setCreatedAt(new Date());
        comment.setUpdatedAt(null);
        return commentRepository.save(comment);
    }

    // function to create a nested comment, given its parent (reply to)
    public CommentEntity createNestedComment(String postId, String userId, String content, String parentCommentId) throws EntityNotFoundException {
        CommentEntity parent = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new EntityNotFoundException("Comentariul părinte nu a fost găsit: " + parentCommentId));
        
        CommentEntity comment = new CommentEntity();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setParentCommentId(parentCommentId); // link with comment parent id
        comment.setCreatedAt(new Date());
        comment.setUpdatedAt(null);
        return commentRepository.save(comment);
    }

    // function to calculate number of likes for all comments on a post
    public long countCommentsForPost(String postId) {
        List<CommentEntity> comments = commentRepository.findByPostId(postId);
        return comments.size();
    }

    // function to edit a comment
    public CommentEntity editComment(String commentId, String newContent) throws EntityNotFoundException {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comentariul nu a fost găsit: " + commentId));
        comment.setContent(newContent);
        comment.setUpdatedAt(new Date());
        return commentRepository.save(comment);
    }

    public void deleteComment(String commentId) throws EntityNotFoundException {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comentariul nu a fost găsit: " + commentId));
        commentRepository.delete(comment);
    }

    // function to delete all comments for a post, removes its likes too via like service
    public void deletePostComments(String postId) throws EntityNotFoundException {
        List<CommentEntity> comments = commentRepository.findByPostId(postId);
        for (CommentEntity comment : comments) {
            likeService.deleteCommentLikes(comment.getId());
            deleteComment(comment.getId());
        }
    }
    
}
