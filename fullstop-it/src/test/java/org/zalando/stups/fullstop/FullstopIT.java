package org.zalando.stups.fullstop;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.RequestEntity.get;

@RunWith(SpringJUnit4ClassRunner.class)
@WebIntegrationTest({"server.port=0", "management.port=0"})
@SpringApplicationConfiguration(Fullstop.class)
public class FullstopIT {

    @Value("${local.management.port}")
    private String managementPort;

    private RestOperations http;

    private String baseUrl;

    @Before
    public void setUp() throws Exception {
        http = new RestTemplate();
        baseUrl = "http://localhost:" + managementPort;
    }

    @Test
    public void testHealth() {
        final ResponseEntity<JsonNode> response = http.exchange(get(URI.create(baseUrl + "/health")).build(), JsonNode.class);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }
}
