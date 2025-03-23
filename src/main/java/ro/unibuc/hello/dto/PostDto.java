package ro.unibuc.hello.dto;

import java.util.Date;
import lombok.Data;

@Data
public class PostDto {
    private String id;
    private String userId;
    private String content;
    private String mediaUrl;
    private Date createdAt;
    private Date updatedAt;
    private String visibility;
    private long commentCount;
    private long likeCount;
}
