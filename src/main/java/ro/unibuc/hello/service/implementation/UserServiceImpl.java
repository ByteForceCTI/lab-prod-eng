package ro.unibuc.hello.service.implementation;

import ro.unibuc.hello.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.unibuc.hello.data.UserEntity;
import ro.unibuc.hello.dto.UserDto;
import ro.unibuc.hello.data.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    
    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
         this.userRepository = userRepository;
    }
    
    private UserDto convertToDto(UserEntity user) {
        UserDto dto = new UserDto();
        dto.setUsername(user.getUsername());
        dto.setBio(user.getBio());
        dto.setProfilePicture(user.getProfilePicture());
        dto.setDateOfBirth(user.getDateOfBirth());
        return dto;
    }
    
    @Override
    public UserDto createUser(UserDto userDto, String email, String password) {
         UserEntity userEntity = new UserEntity();
         userEntity.setUsername(userDto.getUsername());
         userEntity.setBio(userDto.getBio());
         userEntity.setProfilePicture(userDto.getProfilePicture());
        userEntity.setDateOfBirth(userDto.getDateOfBirth());
        userEntity.setEmail(email);
         userEntity.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt()));
         
         userRepository.save(userEntity);
         return convertToDto(userEntity);
    }
    
    @Override
    public UserDto login(String username, String password) {
         Optional<UserEntity> userOptional = userRepository.findByUsername(username);
         if(userOptional.isPresent()) {
              UserEntity user = userOptional.get();
              if(BCrypt.checkpw(password, user.getPasswordHash())) {
                  return convertToDto(user);
              } else {
                  throw new RuntimeException("Wrong password");
              }
         } else {
              throw new RuntimeException("User not found");
         }
    }
    
    @Override
    public UserDto getUserProfile(String username) {
         Optional<UserEntity> userOptional = userRepository.findByUsername(username);
         if(userOptional.isPresent()) {
             return convertToDto(userOptional.get());
         } else {
             throw new RuntimeException("User not found");
         }
    }
    
    @Override
    public UserDto updateUsername(String userId, String newUsername) {
         Optional<UserEntity> userOptional = userRepository.findById(userId);
         if(userOptional.isPresent()) {
             UserEntity user = userOptional.get();
             user.setUsername(newUsername);
             userRepository.save(user);
             return convertToDto(user);
         } else {
             throw new RuntimeException("User not found");
         }
    }
    
    @Override
    public UserDto updatePassword(String userId, String newPassword) {
         Optional<UserEntity> userOptional = userRepository.findById(userId);
         if(userOptional.isPresent()) {
             UserEntity user = userOptional.get();
             // Use BCrypt to hash the new password
             user.setPasswordHash(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
             userRepository.save(user);
             return convertToDto(user);
         } else {
             throw new RuntimeException("User not found");
         }
    }
    
    @Override
    public UserDto updateProfilePicture(String userId, String newProfilePicture) {
         Optional<UserEntity> userOptional = userRepository.findById(userId);
         if(userOptional.isPresent()) {
             UserEntity user = userOptional.get();
             user.setProfilePicture(newProfilePicture);
             userRepository.save(user);
             return convertToDto(user);
         } else {
             throw new RuntimeException("User not found");
         }
    }
    
    @Override
    public UserDto updateBio(String userId, String newBio) {
         Optional<UserEntity> userOptional = userRepository.findById(userId);
         if(userOptional.isPresent()) {
             UserEntity user = userOptional.get();
             user.setBio(newBio);
             userRepository.save(user);
             return convertToDto(user);
         } else {
             throw new RuntimeException("User not found");
         }
    }
    
    @Override
    public void deleteUser(String userId) {
         userRepository.deleteById(userId);
    }
    
    @Override
    public List<UserDto> getAllUsers() {
         return userRepository.findAll()
                              .stream()
                              .map(this::convertToDto)
                              .collect(Collectors.toList());
    }
    
    @Override
    public UserDto getUserById(String userId) {
         Optional<UserEntity> userOptional = userRepository.findById(userId);
         if(userOptional.isPresent()) {
             return convertToDto(userOptional.get());
         } else {
             throw new RuntimeException("User not found");
         }
    }

    @Override
    public UserDto getUserByUsername(String username){
        Optional<UserEntity> userOptional = userRepository.findByUsername(username);
        if(userOptional.isPresent()) {
            return convertToDto(userOptional.get());
        } else {
            throw new RuntimeException("User not found");
        }
    }

    public UserEntity getUserEntityById(String username){
        return userRepository.findByUsername(username).get();
    }
}
