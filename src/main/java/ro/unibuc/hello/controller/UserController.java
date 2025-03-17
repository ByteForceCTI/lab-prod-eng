import ro.unibuc.hello.service.UserService;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.unibuc.hello.dto.UserDto;
import ro.unibuc.hello.service.UserService;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    
    public UserController(UserService userService){
        this.userService = userService;
    }


    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody CreateUserRequest request) {
         UserDto userDto = userService.createUser(request.toUserDto(), request.getPassword());
         return new ResponseEntity<>(userDto, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@RequestBody LoginRequest request) {
         UserDto userDto = userService.login(request.getUsername(), request.getPassword());
         return ResponseEntity.ok(userDto);
    }

    @GetMapping("/profile/{username}")
    public ResponseEntity<UserDto> getUserProfile(@PathVariable String username) {
         UserDto userDto = userService.getUserProfile(username);
         return ResponseEntity.ok(userDto);
    }

    @PutMapping("/{userId}/username")
    public ResponseEntity<UserDto> updateUsername(@PathVariable String userId, @RequestBody UpdateUsernameRequest request) {
         UserDto userDto = userService.updateUsername(userId, request.getNewUsername());
         return ResponseEntity.ok(userDto);
    }

    @PutMapping("/{userId}/password")
    public ResponseEntity<UserDto> updatePassword(@PathVariable String userId, @RequestBody UpdatePasswordRequest request) {
         UserDto userDto = userService.updatePassword(userId, request.getNewPassword());
         return ResponseEntity.ok(userDto);
    }

    @PutMapping("/{userId}/profile-picture")
    public ResponseEntity<UserDto> updateProfilePicture(@PathVariable String userId, @RequestBody UpdateProfilePictureRequest request) {
         UserDto userDto = userService.updateProfilePicture(userId, request.getNewProfilePicture());
         return ResponseEntity.ok(userDto);
    }

    @PutMapping("/{userId}/bio")
    public ResponseEntity<UserDto> updateBio(@PathVariable String userId, @RequestBody UpdateBioRequest request) {
         UserDto userDto = userService.updateBio(userId, request.getNewBio());
         return ResponseEntity.ok(userDto);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
         userService.deleteUser(userId);
         return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
         List<UserDto> users = userService.getAllUsers();
         return ResponseEntity.ok(users);
    }

    @GetMapping("/id/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable String userId) {
         UserDto userDto = userService.getUserById(userId);
         return ResponseEntity.ok(userDto);
    }

    // Clase DTO pentru request-uri (se pot muta în pachetul DTO)
    public static class CreateUserRequest {
        private String username;
        private String name;
        private String bio;
        private String profilePicture;
        private String password;
        // Getters și setters

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getBio() {
            return bio;
        }

        public void setBio(String bio) {
            this.bio = bio;
        }

        public String getProfilePicture() {
            return profilePicture;
        }

        public void setProfilePicture(String profilePicture) {
            this.profilePicture = profilePicture;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
        
        public UserDto toUserDto() {
            UserDto dto = new UserDto();
            dto.setUsername(username);
            dto.setName(name);
            dto.setBio(bio);
            dto.setProfilePicture(profilePicture);
            return dto;
        }
   }


public static class LoginRequest {
    private String username;
    private String password;
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}

public static class UpdateUsernameRequest {
    private String newUsername;
    
    public String getNewUsername() {
        return newUsername;
    }
    
    public void setNewUsername(String newUsername) {
        this.newUsername = newUsername;
    }
}

public static class UpdatePasswordRequest {
    private String newPassword;
    
    public String getNewPassword() {
        return newPassword;
    }
    
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}

public static class UpdateProfilePictureRequest {
    private String newProfilePicture;
    
    public String getNewProfilePicture() {
        return newProfilePicture;
    }
    
    public void setNewProfilePicture(String newProfilePicture) {
        this.newProfilePicture = newProfilePicture;
    }
}

public static class UpdateBioRequest {
    private String newBio;
    
    public String getNewBio() {
        return newBio;
    }
    
    public void setNewBio(String newBio) {
        this.newBio = newBio;
    }
}
}
