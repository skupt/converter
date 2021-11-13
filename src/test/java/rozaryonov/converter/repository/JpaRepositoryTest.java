package rozaryonov.converter.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import rozaryonov.converter.model.Role;
import rozaryonov.converter.model.User;

import java.util.Set;

@SpringBootTest
public class JpaRepositoryTest {
    @Autowired
    UserRepository userRepository;

    @Test
    @Transactional
    void shouldAddUserToDB() {
        User user = new User();
        user.setLogin("Roma");
        Set<Role> userRoles = user.getRole();
        userRoles.add(Role.ROLE_ADMIN);
        userRoles.add(Role.ROLE_USER);
        long id = userRepository.save(user).getId();
        User userRomaFromDB =userRepository.getById(id);
        Assertions.assertEquals(id, userRomaFromDB.getId());
        Assertions.assertEquals("Roma", userRomaFromDB.getLogin());
        userRepository.deleteById(id);
    }
}
