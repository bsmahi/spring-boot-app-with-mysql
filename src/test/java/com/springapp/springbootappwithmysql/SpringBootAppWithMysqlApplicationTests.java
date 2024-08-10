package com.springapp.springbootappwithmysql;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = SpringBootAppWithMysqlApplication.class)
@TestPropertySource(locations = "classpath:application-test.properties")
class SpringBootAppWithMysqlApplicationTests {

    @Test
    void contextLoads() {
    }

}
