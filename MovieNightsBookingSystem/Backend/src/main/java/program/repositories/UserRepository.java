package program.repositories;

import org.springframework.data.repository.CrudRepository;
import program.entities.User;

public interface UserRepository extends CrudRepository<User, Integer> {
}
