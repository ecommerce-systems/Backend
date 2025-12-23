package com.creepereye.ecommerce;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
    "jwt.secret=c3VwZXItc2VjcmV0LWtleS1mb3ItdGVzdGluZy1lY29tbWVyY2UtcHJvamVjdC0xMjM0NQ==",
    "jwt.access-token-validity-in-seconds=3600",
    "jwt.refresh-token-validity-in-seconds=86400"
})
@EnableAutoConfiguration(exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
@ActiveProfiles("local")
class EcommerceBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
