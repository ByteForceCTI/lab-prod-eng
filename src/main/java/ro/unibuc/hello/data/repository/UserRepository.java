package ro.unibuc.hello.data.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ro.unibuc.hello.data.*;

public interface UserRepository extends MongoRepository<UserEntity, String>{

    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByEmail(String email);
    
}
