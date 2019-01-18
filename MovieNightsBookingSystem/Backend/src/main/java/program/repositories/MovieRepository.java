package program.repositories;

import org.springframework.data.repository.CrudRepository;
import program.entities.Movie;

public interface MovieRepository extends CrudRepository<Movie, Integer> {
}
