package ro.unibuc.hello.service.implementation;

import ro.unibuc.hello.service.UserService;

import java.util.List;
import java.util.Optional;
import java.util.Date;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ro.unibuc.hello.data.UserEntity;
import ro.unibuc.hello.dto.UserDto;
import ro.unibuc.hello.data.repository.*;

@Service
public class UserServiceImpl implements UserService{
    

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
         this.userRepository = userRepository;
         this.passwordEncoder = passwordEncoder;
    }

    private UserDto convertToDto(UserEntity user) {
        UserDto dto = new UserDto();
        dto.setUsername(user.getUsername());
        dto.setName(user.getName());
        dto.setBio(user.getBio());
        dto.setProfilePicture(user.getProfilePicture());
        return dto;
    }

    @Override
    public UserDto createUser(UserDto userDto, String password) {
         UserEntity userEntity = new UserEntity();
         userEntity.setUsername(userDto.getUsername());
         userEntity.setName(userDto.getName());
         userEntity.setBio(userDto.getBio());
         userEntity.setProfilePicture(userDto.getProfilePicture());
         userEntity.setPasswordHash(passwordEncoder.encode(password));

         userRepository.save(userEntity);
         return convertToDto(userEntity);
    }

    @Override
    public UserDto login(String username, String password) {
         Optional<UserEntity> userOptional = userRepository.findByUsername(username);
         if(userOptional.isPresent()) {
              UserEntity user = userOptional.get();
              if(passwordEncoder.matches(password, user.getPasswordHash())) {
                  return convertToDto(user);
              } else {
                  throw new RuntimeException("Parolă incorectă");
              }
         } else {
              throw new RuntimeException("Utilizatorul nu a fost găsit");
         }
    }

    @Override
    public UserDto getUserProfile(String username) {
         Optional<UserEntity> userOptional = userRepository.findByUsername(username);
         if(userOptional.isPresent()) {
             return convertToDto(userOptional.get());
         } else {
             throw new RuntimeException("Utilizatorul nu a fost găsit");
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
             throw new RuntimeException("Utilizatorul nu a fost găsit");
         }
    }

    @Override
    public UserDto updatePassword(String userId, String newPassword) {
         Optional<UserEntity> userOptional = userRepository.findById(userId);
         if(userOptional.isPresent()) {
             UserEntity user = userOptional.get();
             user.setPasswordHash(passwordEncoder.encode(newPassword));
             userRepository.save(user);
             return convertToDto(user);
         } else {
             throw new RuntimeException("Utilizatorul nu a fost găsit");
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
             throw new RuntimeException("Utilizatorul nu a fost găsit");
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
             throw new RuntimeException("Utilizatorul nu a fost găsit");
         }
    }

    @Override
    public void deleteUser(String userId) {
         // Înainte de ștergere, poți implementa logica pentru ștergerea entităților asociate
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
             throw new RuntimeException("Utilizatorul nu a fost găsit");
         }
    }
}
