package application.controllers;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebFlux;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureWebFlux
@AutoConfigureWebTestClient
@ContextConfiguration(initializers = {FooControllerIntegrationTest.Initializer.class})
public class FooControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @ClassRule
    public static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:latest")
            .withDatabaseName("integration-tests-db")
            .withUsername("test")
            .withPassword("test");


    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + postgreSQLContainer.getJdbcUrl(),
                    "spring.datasource.username=" + postgreSQLContainer.getUsername(),
                    "spring.datasource.password=" + postgreSQLContainer.getPassword(),
                    "spring.jpa.properties.hibernate.hbm2ddl.auto=create-drop"
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }


    @Test
    public void postFooBarOther_catchErrorAndRollback () throws Exception {
        clearDB();
        webTestClient.post()
                .uri("http://localhost:8080/foobar_other")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"foo\": \"foo\", \"bar\": \"йцу\"}")
                .exchange()
                .expectStatus().is5xxServerError();

        webTestClient.get()
                .uri("http://localhost:8080/get_count")
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<Long>() {})
                .isEqualTo(0L);
    }

    @Test
    public void postFooBarOther_InsertRecords() throws Exception {
        clearDB();
        webTestClient.post()
                .uri("http://localhost:8080/foobar_other")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"foo\": \"foo\", \"bar\": \"bar\"}")
                .exchange()
                .expectStatus().is2xxSuccessful();

        webTestClient.get()
                .uri("http://localhost:8080/get_count")
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<Long>() {})
                .isEqualTo(2L);

        clearDB();

        webTestClient.get()
                .uri("http://localhost:8080/get_count")
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<Long>() {})
                .isEqualTo(0L);
    }

    @Test
    public void postFooBar_catchErrorAndRollback() throws Exception {
        clearDB();
        webTestClient.post()
                .uri("http://localhost:8080/foobar")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"foo\": \"foo\", \"bar\": \"йцу\"}")
                .exchange()
                .expectStatus().is5xxServerError();

        webTestClient.get()
                .uri("http://localhost:8080/get_count")
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<Long>() {})
                .isEqualTo(0L);

    }

    @Test
    public void postFooBar_InsertRecords() throws Exception {
        clearDB();
        webTestClient.post()
                .uri("http://localhost:8080/foobar")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"foo\": \"foo\", \"bar\": \"bar\"}")
                .exchange()
                .expectStatus().is2xxSuccessful();

        webTestClient.get()
                .uri("http://localhost:8080/get_count")
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<Long>() {})
                .isEqualTo(2L);

        clearDB();

        webTestClient.get()
                .uri("http://localhost:8080/get_count")
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<Long>() {})
                .isEqualTo(0L);
    }

    private void clearDB() {
        webTestClient.delete()
                .uri("http://localhost:8080/delete_all")
                .exchange()
                .expectStatus().is2xxSuccessful();
    }


    @Test
    public void whenSelectQueryExecuted_thenResultsReturned()
            throws Exception {
        String jdbcUrl = postgreSQLContainer.getJdbcUrl();
        String username = postgreSQLContainer.getUsername();
        String password = postgreSQLContainer.getPassword();
        Connection conn = DriverManager
                .getConnection(jdbcUrl, username, password);
        ResultSet resultSet =
                conn.createStatement().executeQuery("SELECT 1");
        resultSet.next();
        int result = resultSet.getInt(1);
        assertEquals(1, result);
    }

}