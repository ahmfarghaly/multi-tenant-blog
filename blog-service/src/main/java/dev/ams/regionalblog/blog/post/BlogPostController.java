package dev.ams.regionalblog.blog.post;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
public class BlogPostController {

    private final BlogPostService postService;

    public BlogPostController(BlogPostService postService) {
        this.postService = postService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostDtos.PostResponse createPost(@Valid @RequestBody PostDtos.CreatePostRequest request) {
        return postService.createPost(request);
    }

    @GetMapping
    public List<PostDtos.PostResponse> listPosts() {
        return postService.listPosts();
    }

    @GetMapping("/{id}")
    public PostDtos.PostResponse getPost(@PathVariable UUID id) {
        return postService.getPost(id);
    }

    @PatchMapping("/{id}")
    public PostDtos.PostResponse updatePost(
            @PathVariable UUID id,
            @Valid @RequestBody PostDtos.UpdatePostRequest request) {
        return postService.updatePost(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable UUID id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}
