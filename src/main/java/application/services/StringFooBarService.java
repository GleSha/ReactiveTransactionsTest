package application.services;

import application.entities.StringBar;
import application.entities.StringFoo;
import application.exceptions.CyrillicCharactersException;
import application.repositories.StringBarRepository;
import application.repositories.StringFooRepository;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple3;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.List;

@Service
public class StringFooBarService {

    private final StringFooRepository stringFooRepository;
    private final StringBarRepository stringBarRepository;

    private final EntityManagerFactory entityManagerFactory;

    public StringFooBarService(StringFooRepository stringFooRepository, StringBarRepository stringBarRepository, EntityManagerFactory entityManagerFactory) {
        this.stringFooRepository = stringFooRepository;
        this.stringBarRepository = stringBarRepository;
        this.entityManagerFactory = entityManagerFactory;
    }

    @Transactional(readOnly = true)
    public List<StringFoo> getStringFooList() {
        return stringFooRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<StringBar> getStringBarList() {
        return stringBarRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Mono<List<StringFoo>> getStringFooListReactive() {
        return Mono.just(1)
                .publishOn(Schedulers.single())
                .map(i -> stringFooRepository.findAll());
    }

    @Transactional(rollbackFor = CyrillicCharactersException.class)
    public boolean insertFooBarStrings(StringBar stringBar, StringFoo stringFoo) throws CyrillicCharactersException {
        stringBarRepository.save(stringBar);
        stringFooRepository.save(stringFoo);
        if (test(stringBar.getValue()) || test(stringFoo.getValue())) {
            throw new CyrillicCharactersException("One of the values contains cyrillic characters");
        }
        return true;
    }

    //Here some terrible code
    //Tried using subscriberContext() to put the EntityManager into it but haven't found an elegant way to do it yet
    public Mono<Boolean> insertReactive(StringBar stringBar, StringFoo stringFoo) {
        return Mono.zip(Mono.just(stringBar), Mono.just(stringFoo), Mono.just(entityManagerFactory.createEntityManager()))
                .publishOn(Schedulers.parallel())   //do our work in some thread
                .map((Tuple3<StringBar, StringFoo, EntityManager> data) -> {
                    EntityManager entityManager = data.getT3();
                    try {
                        entityManager.getTransaction().begin();
                        entityManager.persist(data.getT1());
                        entityManager.persist(data.getT2());
                        if (test(data.getT1().getValue()) || test(data.getT2().getValue())) {
                            throw new CyrillicCharactersException("One of the values contains cyrillic characters");
                        }
                    } catch (CyrillicCharactersException e) {
                        return Pair.of(false, entityManager);
                    }
                    return Pair.of(true, entityManager);
                }).map((Pair<Boolean, EntityManager> data) -> {
                    if (data.getFirst()) {
                        //our work thread is the same here so we can use EntityManager
                        //with Flux we would need to use Schedulers.single() or nothing at all
                        data.getSecond().getTransaction().commit();
                    } else {
                        data.getSecond().getTransaction().rollback();
                    }
                    return data.getFirst();
                });
    }

    private boolean test(String value) {
        return value.chars()
                .mapToObj(Character.UnicodeBlock::of)
                .anyMatch(b -> b.equals(Character.UnicodeBlock.CYRILLIC));
    }

    @Transactional(readOnly = true)
    public Long getFooBarCount() {
        return stringBarRepository.count() + stringFooRepository.count();
    }

    @Transactional
    public void deleteAll() {
        stringFooRepository.deleteAll();
        stringBarRepository.deleteAll();
    }

}
