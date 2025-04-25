package ro.unibuc.hello;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import jakarta.annotation.PostConstruct;
import ro.unibuc.hello.data.InformationEntity;
import ro.unibuc.hello.data.UserEntity;
import ro.unibuc.hello.data.repository.InformationRepository;
import ro.unibuc.hello.data.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;

@SpringBootApplication
@ComponentScan(basePackages = "ro.unibuc.hello")
@EnableMongoRepositories(basePackageClasses = {InformationRepository.class, UserRepository.class})
public class HelloApplication {

	@Autowired
	private InformationRepository informationRepository;

	@Autowired
    private UserRepository userRepository;

	public static void main(String[] args) {
		SpringApplication.run(HelloApplication.class, args);
	}

	@PostConstruct
	public void runAfterObjectCreated() {
		informationRepository.deleteAll();
		userRepository.deleteAll();

		informationRepository.save(new InformationEntity("Overview",
				"This is an example of using a data storage engine running separately from our applications server"));

		 UserEntity user1 = new UserEntity();
        user1.setUsername("johndoe");
        user1.setEmail("john@example.com");
        user1.setName("John Doe");
        user1.setBio("Bio for John");
        user1.setProfilePicture("profile1.jpg");
        user1.setPasswordHash("password1"); 

        UserEntity user2 = new UserEntity();
        user2.setUsername("janedoe");
        user2.setEmail("jane@example.com");
        user2.setName("Jane Doe");
        user2.setBio("Bio for Jane");
        user2.setProfilePicture("profile2.jpg");
        user2.setPasswordHash("password2");

        UserEntity user3 = new UserEntity();
        user3.setUsername("alexm1126");
        user3.setEmail("alex@example.com");
        user3.setName("A M");
        user3.setBio("Bio for alexm1126");
        user3.setProfilePicture("profile3.jpg");
        String plainPassword = "abc12345";
        user3.setPasswordHash(BCrypt.hashpw(plainPassword, BCrypt.gensalt()));

        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);
	}

}
