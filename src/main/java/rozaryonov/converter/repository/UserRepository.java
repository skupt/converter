package rozaryonov.converter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rozaryonov.converter.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
}