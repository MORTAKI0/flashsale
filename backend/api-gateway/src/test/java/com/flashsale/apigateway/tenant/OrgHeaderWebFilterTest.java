package com.flashsale.apigateway.tenant;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Import(OrgHeaderWebFilterTest.TestWhoAmIController.class)
class OrgHeaderWebFilterTest {

  private static final String PATH = "/api/catalog/context/whoami";
  private static final String ORG_HEADER = "X-ORG-ID";
  private static final String CORRELATION_HEADER = "X-CORRELATION-ID";

  @org.springframework.beans.factory.annotation.Autowired
  private WebTestClient webTestClient;

  @Test
  void shouldAllowWhenOrgHeaderMatchesOrgIdsClaim() {
    webTestClient.mutateWith(mockJwt().jwt(jwt -> jwt.claim("org_ids", List.of("org-a"))))
        .get()
        .uri(PATH)
        .header(ORG_HEADER, "org-a")
        .exchange()
        .expectStatus().isOk()
        .expectHeader().exists(CORRELATION_HEADER)
        .expectBody()
        .jsonPath("$.tenantId").isEqualTo("org-a")
        .jsonPath("$.userId").isEqualTo("test-user");
  }

  @Test
  void shouldReturnForbiddenWhenOrgHeaderNotAllowed() {
    webTestClient.mutateWith(mockJwt().jwt(jwt -> jwt.claim("org_ids", List.of("org-a"))))
        .get()
        .uri(PATH)
        .header(ORG_HEADER, "org-z")
        .exchange()
        .expectStatus().isForbidden()
        .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
        .expectHeader().exists(CORRELATION_HEADER)
        .expectBody()
        .jsonPath("$.code").isEqualTo("ORG_FORBIDDEN")
        .jsonPath("$.message").isEqualTo("X-ORG-ID is not allowed for this user")
        .jsonPath("$.path").isEqualTo(PATH)
        .jsonPath("$.correlationId").isNotEmpty()
        .jsonPath("$.timestamp").isNotEmpty();
  }

  @Test
  void shouldAllowWhenOrgIdsClaimIsString() {
    webTestClient.mutateWith(mockJwt().jwt(jwt -> jwt.claim("org_ids", "org-a")))
        .get()
        .uri(PATH)
        .header(ORG_HEADER, "org-a")
        .exchange()
        .expectStatus().isOk()
        .expectHeader().exists(CORRELATION_HEADER);
  }

  @Test
  void shouldReturnBadRequestWhenOrgHeaderMissing() {
    webTestClient.mutateWith(mockJwt().jwt(jwt -> jwt.claim("org_ids", List.of("org-a"))))
        .get()
        .uri(PATH)
        .exchange()
        .expectStatus().isBadRequest()
        .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
        .expectHeader().exists(CORRELATION_HEADER)
        .expectBody()
        .jsonPath("$.code").isEqualTo("ORG_REQUIRED")
        .jsonPath("$.message").isEqualTo("X-ORG-ID header is required")
        .jsonPath("$.path").isEqualTo(PATH);
  }

  @Test
  void shouldReturnUnauthorizedWhenJwtMissing() {
    webTestClient.get()
        .uri(PATH)
        .header(ORG_HEADER, "org-a")
        .exchange()
        .expectStatus().isUnauthorized()
        .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
        .expectHeader().exists(CORRELATION_HEADER)
        .expectBody()
        .jsonPath("$.code").isEqualTo("UNAUTHORIZED")
        .jsonPath("$.message").isEqualTo("Missing or invalid JWT")
        .jsonPath("$.path").isEqualTo(PATH);
  }

  @RestController
  @RequestMapping("/api/catalog/context")
  static class TestWhoAmIController {

    @GetMapping("/whoami")
    Map<String, Object> whoAmI() {
      return Map.of(
          "tenantId", "org-a",
          "userId", "test-user",
          "roles", List.of("OWNER"),
          "correlationId", "from-controller"
      );
    }
  }
}
