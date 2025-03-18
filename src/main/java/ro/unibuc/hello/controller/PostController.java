package ro.unibuc.hello.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ro.unibuc.hello.data.PostEntity;
import ro.unibuc.hello.exception.EntityNotFoundException;
import ro.unibuc.hello.service.PostService;

import java.util.List;

@RestController
@RequestMapping("/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @PostMapping
    public PostEntity createPost(@RequestBody PostEntity post) {
        return postService.createPost(post);
    }

    @GetMapping
    public List<PostEntity> getAllPosts() {
        return postService.getAllPosts();
    }

    @GetMapping("/{id}")
    public PostEntity getPostById(@PathVariable String id) throws EntityNotFoundException {
        return postService.getPostById(id);
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
