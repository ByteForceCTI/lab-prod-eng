package ro.unibuc.hello.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.unibuc.hello.data.PostEntity;
import ro.unibuc.hello.dto.PostDto;
import ro.unibuc.hello.dto.UserDto;
import ro.unibuc.hello.exception.EntityNotFoundException;
import ro.unibuc.hello.exception.ForbiddenAccessException;
import ro.unibuc.hello.service.PostService;

import java.util.List;

@RestController
@RequestMapping("/posts")
public class PostController extends AbstractAuthController {

    @Autowired
    private PostService postService;

    @PostMapping
    public ResponseEntity<PostEntity> createPost(@RequestBody PostEntity post,
                                                 @RequestHeader("Authorization") String authHeader) {
        UserDto user = getAuthenticatedUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        post.setUserId(user.getId());
        PostEntity createdPost = postService.createPost(post);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
    }

    @GetMapping
    public ResponseEntity<List<PostDto>> getAllPosts(@RequestHeader("Authorization") String authHeader) {
        UserDto user = getAuthenticatedUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<PostDto> posts = postService.getVisiblePosts(user.getId());
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDto> getPostById(@PathVariable("id") String id,
                                               @RequestHeader("Authorization") String authHeader) throws EntityNotFoundException {
        UserDto user = getAuthenticatedUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        PostDto post = postService.getPostById(id, user.getId()); // returned posts are already filtered
        return ResponseEntity.ok(post);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostEntity> updatePost(@PathVariable("id") String id, @RequestBody PostEntity post,
                                                 @RequestHeader("Authorization") String authHeader) throws EntityNotFoundException {
        UserDto user = getAuthenticatedUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try{
            PostEntity updatedPost = postService.updatePostAuth(id, user.getId(), post);
            return ResponseEntity.ok(updatedPost);
        }
        catch(ForbiddenAccessException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable("id") String id,
                                           @RequestHeader(value="Authorization", required = true) String authHeader) throws EntityNotFoundException {
        UserDto user = getAuthenticatedUser(authHeader);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try{
            postService.deletePostAuth(id, user.getId());
            return ResponseEntity.noContent().build();
        }
        catch(ForbiddenAccessException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
