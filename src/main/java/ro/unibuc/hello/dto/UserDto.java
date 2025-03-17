package ro.unibuc.hello.dto;

import lombok.Data;

@Data
public class UserDto {
    private String username;
    private String name;
    private String bio;
    private String profilePicture;
}
