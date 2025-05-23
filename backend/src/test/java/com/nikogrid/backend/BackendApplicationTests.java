package com.nikogrid.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class BackendApplicationTests {

    @Test
    void contextLoads() {
        // Test that the spring application context loads correctly
    }

}
