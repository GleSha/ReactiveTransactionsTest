package application.repositories;

import application.entities.StringBar;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StringBarRepository extends CrudRepository<StringBar, Long> {

    List<StringBar> findAll();

    List<StringBar> findByValue(String value);

}
