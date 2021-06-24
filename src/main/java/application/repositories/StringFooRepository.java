package application.repositories;

import application.entities.StringFoo;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StringFooRepository extends CrudRepository<StringFoo, Long> {

    List<StringFoo> findAll();

    List<StringFoo> findByValue(String value);

}
