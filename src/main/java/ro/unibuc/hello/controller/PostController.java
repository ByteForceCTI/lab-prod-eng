package ro.unibuc.hello.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.unibuc.hello.data.PostEntity;
import ro.unibuc.hello.data.UserEntity;
import ro.unibuc.hello.dto.PostDto;
import ro.unibuc.hello.dto.UserDto;
import ro.unibuc.hello.exception.EntityNotFoundException;
import ro.unibuc.hello.service.PostService;
import ro.unibuc.hello.service.implementation.UserServiceImpl;

import java.util.List;

@RestController
@RequestMapping("/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private UserServiceImpl userService;

    @PostMapping
    public PostEntity createPost(@RequestBody PostEntity post) {
        return postService.createPost(post);
    }

    @GetMapping
    public ResponseEntity<List<PostDto>> getAllPosts(@RequestParam("Authorization") String token) {
        UserDto user = userService.getUserFromToken(token);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        List<PostDto> posts = postService.getVisiblePosts(user.getId());
        return ResponseEntity.ok(posts);
    }


    @GetMapping("/{id}")
    public PostDto getPostById(@PathVariable String id) throws EntityNotFoundException {
        return postService.getPostById(id, null); // TODO - get logged in user ID and filter their posts accordingly
    }

    @PutMapping("/{id}")
    public PostEntity updatePost(@PathVariable String id, @RequestBody PostEntity post) throws EntityNotFoundException {
        return postService.updatePost(id, post);
    }

    @DeleteMapping("/{id}")
    public void deletePost(@PathVariable String id) throws EntityNotFoundException {
        postService.deletePost(id);
    }
}
