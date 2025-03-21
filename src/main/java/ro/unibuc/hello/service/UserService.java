 package ro.unibuc.hello.service;

import java.util.List;

import ro.unibuc.hello.dto.UserDto;

// import ro.unibuc.hello.exception.EntityNotFoundException;
// import ro.unibuc.hello.data.UserEntity;
// import ro.unibuc.hello.dto.UserDto;
// import ro.unibuc.hello.data.repository.UserRepository;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;

// import java.util.List;
// import java.util.Optional;
// import java.util.concurrent.atomic.AtomicLong;
// import java.util.stream.Collectors;

// @Service
// public class UserService {

//     @Autowired
//     private UserRepository userRepository;

//     private final AtomicLong counter = new AtomicLong();

//     public List<UserEntity> getAllUsers() {
//         return userRepository.findAll();
//     }

//     public UserEntity getUserById(String id) throws EntityNotFoundException {
//         Optional<UserEntity> optionalUser = userRepository.findById(id);
//         return optionalUser.orElseThrow(() -> new EntityNotFoundException(id));
//     }

//     public UserEntity saveUser(UserEntity user) {
//         return userRepository.save(user);
//     }

//     // --- SAVE ALL (creare în masă)
//     public List<UserEntity> saveAllUsers(List<UserEntity> users) {
//         return userRepository.saveAll(users);
//     }

//     // --- UPDATE
//     public UserEntity updateUser(String id, UserEntity userData) throws EntityNotFoundException {
//         UserEntity user = userRepository.findById(id)
//                 .orElseThrow(() -> new EntityNotFoundException(id));

//         user.setUsername(userData.getUsername());
//         user.setEmail(userData.getEmail());
//         user.setName(userData.getName());

//         userRepository.save(user);
//         return user;
//     }

//     // --- DELETE
//     public void deleteUser(String id) throws EntityNotFoundException {
//         UserEntity user = userRepository.findById(id)
//                 .orElseThrow(() -> new EntityNotFoundException(id));
//         userRepository.delete(user);
//     }

//     // --- DELETE ALL
//     public void deleteAllUsers() {
//         userRepository.deleteAll();
//     }
// }

public interface UserService {
    UserDto createUser(UserDto userDto, String password);

    UserDto login(String username, String password);

    UserDto getUserProfile(String username);

    UserDto updateUsername(String userId, String newUsername);

    UserDto updatePassword(String userId, String newPassword);

    UserDto updateProfilePicture(String userId, String newProfilePicture);

    UserDto updateBio(String userId, String newBio);

    void deleteUser(String userId);

    List<UserDto> getAllUsers();
    
    UserDto getUserById(String userId);
}
