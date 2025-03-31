 package ro.unibuc.hello.service;

import java.util.List;

import ro.unibuc.hello.dto.UserDto;

public interface UserService {
    UserDto createUser(UserDto userDto, String email, String password);

    UserDto login(String username, String password);
    
    String loginJWT(String username, String password);

    UserDto getUserProfile(String username);

    UserDto updateUsername(String userId, String newUsername);

    UserDto updatePassword(String userId, String newPassword);

    UserDto updateProfilePicture(String userId, String newProfilePicture);

    UserDto updateBio(String userId, String newBio);

    void deleteUser(String userId);

    List<UserDto> getAllUsers();
    
    UserDto getUserById(String userId);

    UserDto getUserByUsername(String username);
}
