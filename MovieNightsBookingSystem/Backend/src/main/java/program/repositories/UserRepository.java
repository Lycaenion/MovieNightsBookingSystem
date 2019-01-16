package program.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.CrudRepository;
import program.entities.User;

public interface UserRepository extends CrudRepository<User, Integer> {

    @Modifying
    @Query("UPDATE User set accessToken = :accessToken where email = :email")
         void updateRefreshtoken(@Param("accesstoken") String accessToken, @Param("email") String email);
}
