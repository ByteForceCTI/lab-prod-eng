package ro.unibuc.hello.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ro.unibuc.hello.data.FriendshipEntity;
import ro.unibuc.hello.data.FriendshipEntity.FriendshipStatus;
import ro.unibuc.hello.data.PostEntity;
import ro.unibuc.hello.data.UserEntity;
import ro.unibuc.hello.data.PostEntity.PostVisibility;
import ro.unibuc.hello.data.repository.PostRepository;
import ro.unibuc.hello.data.repository.UserRepository;
import ro.unibuc.hello.dto.PostDto;
import ro.unibuc.hello.exception.EntityNotFoundException;
import ro.unibuc.hello.exception.ForbiddenAccessException;
import ro.unibuc.hello.service.CommentService;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;
    
    @Autowired
    private FriendshipService friendshipService;

    private PostDto convertToDto(PostEntity post) {
        PostDto dto = new PostDto();
        dto.setId(post.getId());
        dto.setUserId(post.getUserId());
        dto.setContent(post.getContent());
        dto.setMediaUrl(post.getMediaUrl());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUpdatedAt(post.getUpdatedAt());
        dto.setVisibility(post.getVisibility().name());
        return dto;
    } // helper method to return aggregated data into a single entity to the controller

    public PostEntity createPost(PostEntity post) {
        post.setCreatedAt(new java.util.Date());
        return postRepository.save(post);
    }

    private List<PostEntity> getAllPosts() {
        return postRepository.findAll();
    } // we do not allow access to ALL posts of the application, we use the method to get visible posts only for the current user that takes visibility into account

    // determine if a post is visible to the current user using friendship data.
    private boolean isPostVisible(PostEntity post, String currentUserId) {
        // get friendship between the post creator and the user who is viewing the post
        FriendshipEntity friendship = friendshipService.getFriendshipBetween(post.getUserId(), currentUserId);
        
        // if post creator has blocked the viewer do not show post
        if (friendship != null && friendship.getStatus() == FriendshipStatus.BLOCKED) {
            return false;
        }
        
        // if the post is FRIENDS_ONLY make sure the friendship is accepted by the viewer
        if (post.getVisibility() == PostVisibility.FRIENDS_ONLY) {
            if (friendship == null || friendship.getStatus() != FriendshipStatus.ACCEPTED) {
                return false;
            }
        }
        
        return true; // if the conditions are met, we will allow the post to be shown to the viewer
    }

    // function to get visible posts (as DTOs) for the current user, with aggregated comment and like counts.
    public List<PostDto> getVisiblePosts(String currentUserId) {
        List<PostEntity> posts = getAllPosts();
        return posts.stream()
                .filter(post -> isPostVisible(post, currentUserId))
                .map(post -> {
                    PostDto dto = convertToDto(post);
                    // include like and comment counts in the DTO
                    dto.setCommentCount(commentService.countCommentsForPost(post.getId()));
                    dto.setLikeCount(likeService.countLikesForPost(post.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // function to view a particular post by its id together with its aggrgated dataa
    public PostDto getPostById(String id, String currentUserId) throws EntityNotFoundException {
        PostEntity post = postRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Post not found"));
        if (!isPostVisible(post, currentUserId)) {
            throw new EntityNotFoundException("Post is not visible for the current user");
        }
        PostDto postDto = convertToDto(post);
        postDto.setCommentCount(commentService.countCommentsForPost(post.getId()));
        postDto.setLikeCount(likeService.countLikesForPost(post.getId()));
        return postDto;
    }
    
    
    // function for post update
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

    // function for post update (with authentication)
    public PostEntity updatePostAuth(String postId, String userId, PostEntity updatedPost) throws EntityNotFoundException {
        Optional<PostEntity> existingPostOpt = postRepository.findById(postId);
        Optional <UserEntity> currentUser = userRepository.findById(userId);

        if (!existingPostOpt.isPresent()) {
            throw new EntityNotFoundException("Post not found");
        } else {
            if(!currentUser.isPresent()){
                throw new EntityNotFoundException("User not found");
            }
            if(existingPostOpt.get().getUserId().equals(currentUser.get().getId())){
                PostEntity existingPost = existingPostOpt.get();
                existingPost.setContent(updatedPost.getContent());
                existingPost.setMediaUrl(updatedPost.getMediaUrl());
                existingPost.setUpdatedAt(new java.util.Date());
                return postRepository.save(existingPost);
            } else {
                throw new ForbiddenAccessException("User is not the owner of this post.");
            }
        }
    }

    // function to delete a post and its associated data
    public void deletePost(String id) throws EntityNotFoundException {
        if (!postRepository.existsById(id)) {
            throw new EntityNotFoundException("Post not found");
        }
        postRepository.deleteById(id);
        commentService.deletePostComments(id);
        likeService.deletePostLikes(id);
    }

    // function to delete a post and its associated data (with authentication)
    public void deletePostAuth(String postId, String userId) throws EntityNotFoundException {
        Optional <PostEntity> currentPost = postRepository.findById(postId);
        Optional <UserEntity> currentUser = userRepository.findById(userId);

        if (!currentPost.isPresent()) {
            throw new EntityNotFoundException("Post not found");
        } else {
            if(!currentUser.isPresent()){
                throw new EntityNotFoundException("User does not exist");
            }
            if(currentPost.get().getUserId().equals(currentUser.get().getId())){
                postRepository.deleteById(postId);
                commentService.deletePostComments(postId);
                likeService.deletePostLikes(postId);
            } else {
                throw new ForbiddenAccessException("User is not the owner of this post.");
            }
        }
    }
}
