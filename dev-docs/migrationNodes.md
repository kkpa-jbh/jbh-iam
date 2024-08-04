# Migration Test Classes

## JUnit4

Starting with Spring Boot 2.4, JUnit 5’s vintage engine has been removed from spring-boot-starter-test. If we still want to write tests using JUnit 4, we need to add the following Maven dependency:

```xml
<dependency>
    <groupId>org.junit.vintage</groupId>
    <artifactId>junit-vintage-engine</artifactId>
    <scope>test</scope>
    <exclusions>
        <exclusion>
            <groupId>org.hamcrest</groupId>
            <artifactId>hamcrest-core</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```


## Integration Testing With @SpringBootTest
As the name suggests, integration tests focus on integrating different layers of the application. That also means no mocking is involved.

Ideally, we should keep the integration tests separated from the unit tests and should not run along with the unit tests. We can do this by using a different profile to only run the integration tests. A couple of reasons for doing this could be that the integration tests are time-consuming and might need an actual database to execute.


## JUnit 4 to JUnit 5

1. Rename import org.junit.Test to import `org.junit.jupiter.api.Test`
2. Replace @RunWith(JUnit4.class) with `@ExtendWith(JUnitRunner.class)` in your test classes.
3. Replace @Before and @After with @BeforeEach and @AfterEach, respectively.
4. Replace JUnit 4 assertion methods like assertEquals with JUnit 5 assertions from org.junit.jupiter.api.Assertions. These offer concise and readable options.