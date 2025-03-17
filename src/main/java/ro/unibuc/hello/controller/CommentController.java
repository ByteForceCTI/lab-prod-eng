package ro.unibuc.hello.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ro.unibuc.hello.data.CommentEntity;
import ro.unibuc.hello.exception.EntityNotFoundException;
import ro.unibuc.hello.service.CommentService;

@RestController
@RequestMapping("/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    // asteaptă in request body un CommentEntity cu postId, userId si content
    @PostMapping
    public CommentEntity createComment(@RequestBody CommentEntity comment) {
        return commentService.createComment(comment.getPostId(), comment.getUserId(), comment.getContent());
    }

    // asteapta in request body un CommentEntity cu postId, userId, content si parentCommentId
    @PostMapping("/nested")
    public CommentEntity createNestedComment(@RequestBody CommentEntity comment) throws EntityNotFoundException {
        return commentService.createNestedComment(comment.getPostId(), comment.getUserId(), comment.getContent(), comment.getParentCommentId());
    }

    // calculare nr comment
    @GetMapping("/count")
    public long countCommentsForPost(@RequestParam String postId) {
        return commentService.countCommentsForPost(postId);
    }

    // editare comment - se transmite id-ul comentariului ca parte a URL-ului
    // request body contine noul continut
    @PutMapping("/{id}")
    public CommentEntity editComment(@PathVariable("id") String commentId, @RequestBody CommentEntity comment) throws EntityNotFoundException {
        return commentService.editComment(commentId, comment.getContent());
    }

    // sterge comment 
    @DeleteMapping("/{id}")
    public void deleteComment(@PathVariable("id") String commentId) throws EntityNotFoundException {
        commentService.deleteComment(commentId);
    }
}
