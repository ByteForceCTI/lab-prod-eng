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

    // creare comentariu top-level (fără comentariu parinte)
    public CommentEntity createComment(String postId, String userId, String content) {
        CommentEntity comment = new CommentEntity();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setParentCommentId(null); // comentariu de nivel superior
        comment.setCreatedAt(new Date());
        comment.setUpdatedAt(null);
        return commentRepository.save(comment);
    }

    // Creare comentariu nested (raspuns la un comentariu existent)
    public CommentEntity createNestedComment(String postId, String userId, String content, String parentCommentId) throws EntityNotFoundException {
        // Verificăm dacă comentariul părinte există
        CommentEntity parent = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new EntityNotFoundException("Comentariul părinte nu a fost găsit: " + parentCommentId));
        
        CommentEntity comment = new CommentEntity();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setParentCommentId(parentCommentId); // legare cu comentariul parinte
        comment.setCreatedAt(new Date());
        comment.setUpdatedAt(null);
        return commentRepository.save(comment);
    }

    // Calculare numar comentarii pentru o postare (include toate comentariile asociate, inclusiv cele nested)
    public long countCommentsForPost(String postId) {
        List<CommentEntity> comments = commentRepository.findByPostId(postId);
        return comments.size();
    }

    // Editare comentariu (actualizare conținut șs setare data actualizare comment)
    public CommentEntity editComment(String commentId, String newContent) throws EntityNotFoundException {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comentariul nu a fost găsit: " + commentId));
        comment.setContent(newContent);
        comment.setUpdatedAt(new Date());
        return commentRepository.save(comment);
    }

    // sterge comment
    public void deleteComment(String commentId) throws EntityNotFoundException {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comentariul nu a fost găsit: " + commentId));
        commentRepository.delete(comment);
    }
}
