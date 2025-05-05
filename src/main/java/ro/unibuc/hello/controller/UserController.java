package ro.unibuc.hello.controller;

import java.util.Date;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;

import ro.unibuc.hello.dto.UserDto;
import ro.unibuc.hello.service.implementation.UserServiceImpl;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserServiceImpl userService;
    
    public UserController(UserServiceImpl userService){
        this.userService = userService;
    }


    @Counted(value = "users_create_requests", description = "Număr total de cereri createUser")
    @Timed(value   = "users_create_latency",   description = "Durata apelului createUser")
    @PostMapping("/create")
    public ResponseEntity<UserDto> createUser(@RequestBody CreateUserRequest request) {
         UserDto userDto = userService.createUser(request.toUserDto(), request.getEmail(),  request.getPassword());
         return new ResponseEntity<>(userDto, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@RequestBody LoginRequest request) {
         UserDto userDto = userService.login(request.getUsername(), request.getPassword());
         return ResponseEntity.ok(userDto);
    }

    @PostMapping("/loginjwt")
    public ResponseEntity<String> loginjwt(@RequestBody LoginRequest request) {
        try{
            String token = userService.loginJWT(request.getUsername(), request.getPassword());
            return ResponseEntity.ok(token);
        } catch(Exception e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Login failed");
        }
    }

    // @GetMapping("/profile/{username}")
    // public ResponseEntity<UserDto> getUserProfile(@PathVariable String username) {
    //      UserDto userDto = userService.getUserProfile(username);
    //      return ResponseEntity.ok(userDto);
    // }

    @PutMapping("/{userId}/username")
    public ResponseEntity<UserDto> updateUsername(@PathVariable("userId") String userId, @RequestBody UpdateUsernameRequest request) {
         UserDto userDto = userService.updateUsername(userId, request.getNewUsername());
         return ResponseEntity.ok(userDto);
    }

    @PutMapping("/{userId}/password")
    public ResponseEntity<UserDto> updatePassword(@PathVariable("userId") String userId, @RequestBody UpdatePasswordRequest request) {
         UserDto userDto = userService.updatePassword(userId, request.getNewPassword());
         return ResponseEntity.ok(userDto);
    }

    @PutMapping("/{userId}/profile-picture")
    public ResponseEntity<UserDto> updateProfilePicture(@PathVariable("userId") String userId, @RequestBody UpdateProfilePictureRequest request) {
         UserDto userDto = userService.updateProfilePicture(userId, request.getNewProfilePicture());
         return ResponseEntity.ok(userDto);
    }

    @PutMapping("/{userId}/bio")
    public ResponseEntity<UserDto> updateBio(@PathVariable("userId") String userId, @RequestBody UpdateBioRequest request) {
         UserDto userDto = userService.updateBio(userId, request.getNewBio());
         return ResponseEntity.ok(userDto);
    }

    @Counted(value = "users_delete_requests", description = "Număr total de cereri deleteUser")
    @Timed(value   = "users_delete_latency",   description = "Durata apelului deleteUser")
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable("userId") String userId) {
         userService.deleteUser(userId);
         return ResponseEntity.noContent().build();
    }

    @GetMapping("/all")
    public ResponseEntity<List<UserDto>> getAllUsers() {
         List<UserDto> users = userService.getAllUsers();
         return ResponseEntity.ok(users);
    }

    // @GetMapping("/id/{userId}")
    // public ResponseEntity<UserDto> getUserById(@PathVariable String userId) {
    //      UserDto userDto = userService.getUserById(userId);
    //      return ResponseEntity.ok(userDto);
    // }

    @GetMapping("/id/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable("userId") String userId) {
    UserDto userDto = userService.getUserById(userId);
    return ResponseEntity.ok(userDto);
}

    @GetMapping("/profile/{username}")
    public ResponseEntity<UserDto> getUserProfile(@PathVariable("username") String username) {
    UserDto userDto = userService.getUserProfile(username);
    return ResponseEntity.ok(userDto);
}
    public static class CreateUserRequest {
        private String username;
        private String name;
        private String bio;
        private String profilePicture;
        private String password;
        private String email;
        private Date dateOfBirth;

        public Date getDateOfBirth(){
            return dateOfBirth;
        }

        public void setDateOfBirth(Date dateOfBirth){
            this.dateOfBirth = dateOfBirth;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail(){
            return email;
        }

        public void setEmail(String email){
            this.email = email;
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
            dto.setDateOfBirth(dateOfBirth);
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
