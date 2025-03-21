package ro.unibuc.hello.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ro.unibuc.hello.data.PostEntity;
import ro.unibuc.hello.data.repository.PostRepository;
import ro.unibuc.hello.exception.EntityNotFoundException;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    public PostEntity createPost(PostEntity post) {
        post.setCreatedAt(new java.util.Date());
        return postRepository.save(post);
    }

    public List<PostEntity> getAllPosts() {
        return postRepository.findAll();
    } // se vor filtra dupa criterii - utilizator blocat, vizibilitate

    public PostEntity getPostById(String id) throws EntityNotFoundException {
        return postRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Post not found"));
    }

    public PostEntity updatePost(String id, PostEntity updatedPost) throws EntityNotFoundException {
        Optional<PostEntity> existingPostOpt = postRepository.findById(id);

        if (existingPostOpt.isPresent()) {
            PostEntity existingPost = existingPostOpt.get();
            existingPost.setContent(updatedPost.getContent());
            existingPost.setMediaUrl(updatedPost.getMediaUrl());
            existingPost.setUpdatedAt(new java.util.Date());
            return postRepository.save(existingPost);
        } else {
            throw new EntityNotFoundException("Post not found");
        }
    }

    public void deletePost(String id) throws EntityNotFoundException {
        if (!postRepository.existsById(id)) {
            throw new EntityNotFoundException("Post not found");
        }
        postRepository.deleteById(id);
        // se vor sterge in cascada si detaliile asociate (comentarii, likes etc)
    }
}
