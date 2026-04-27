package com.example.mvp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        classes = ProductionMvpApplication.class,
        properties = {
                "security.jwt.secret=TestSecretKeyForJwtAtLeastThirtyTwo!",
                "spring.flyway.enabled=false",
                "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.hibernate.ddl-auto=none"
        }
)
class Assignment3ApplicationTests {

    @Test
    void contextLoads() {
    }

}
