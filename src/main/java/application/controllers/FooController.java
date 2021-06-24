package application.controllers;

import application.dto.request.FooBarStrings;
import application.entities.StringBar;
import application.entities.StringFoo;
import application.exceptions.CyrillicCharactersException;
import application.services.StringFooBarService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;


@RestController
public class FooController {

    private final StringFooBarService stringFooBarService;

    public FooController(StringFooBarService stringFooService) {
        this.stringFooBarService = stringFooService;
    }

    @GetMapping(value = "/foo", produces = TEXT_EVENT_STREAM_VALUE)
    public Flux<String> getFoo() {
        return Flux.just("1", "2", "3")
                .publishOn(Schedulers.parallel())
                .doOnRequest(l -> System.out.println("request from " + Thread.currentThread().getName()))
                .doOnSubscribe(l -> System.out.println("subscribed from " + Thread.currentThread().getName()))
                .doOnNext(s -> {
                    System.out.println(Thread.currentThread().getName());
                    stringFooBarService.getStringFooList().forEach(System.out::println);
                })
                .doOnCancel(() -> System.out.println("cancel from " + Thread.currentThread().getName()))
                .delayElements(Duration.ofSeconds(3));
    }

    @PostMapping(value = "/foobar")
    public Mono<ResponseEntity<Boolean>> postFooBar(@RequestBody FooBarStrings fooBarStrings) {
        return Mono.just(fooBarStrings)
                .publishOn(Schedulers.elastic())
                .map(s -> {
                    try {
                        return ResponseEntity.ok(stringFooBarService.insertFooBarStrings(new StringBar(s.getBar()), new StringFoo(s.getFoo())));
                    } catch (CyrillicCharactersException e) {
                        return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                });
    }

    @PostMapping(value = "/foobar_other")
    public Mono<ResponseEntity<Boolean>> postFooBarOther(@RequestBody FooBarStrings fooBarStrings) {
        return stringFooBarService.insertReactive(new StringBar(fooBarStrings.getBar()), new StringFoo(fooBarStrings.getFoo()))
                .map(result -> {
                    if (result) {
                        return ResponseEntity.ok(true);
                    } else {
                        return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                });
    }

    @GetMapping(value = "/foo_list")
    public Flux<StringFoo> getFooListReactive() {
        return stringFooBarService
                .getStringFooListReactive()
                .flatMapMany(v -> Flux.just(v.toArray(new StringFoo[0])));
    }

    @GetMapping("/get_count")
    public Mono<Long> getFooBarCount() {
        return Mono.just(stringFooBarService.getFooBarCount());
    }

    @DeleteMapping("/delete_all")
    public void deleteAll() {
        stringFooBarService.deleteAll();
    }
}
