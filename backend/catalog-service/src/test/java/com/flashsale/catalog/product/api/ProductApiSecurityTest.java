package com.flashsale.catalog.product.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.flashsale.catalog.product.app.ProductReadService;
import com.flashsale.catalog.product.app.ProductWriteService;
import com.flashsale.catalog.product.dto.ProductDetailDto;
import com.flashsale.catalog.shared.security.ApiAccessDeniedHandler;
import com.flashsale.catalog.shared.security.ApiAuthenticationEntryPoint;
import com.flashsale.catalog.shared.security.SecurityConfig;
import com.flashsale.catalog.shared.tenant.TenantEnforcementFilter;
import com.flashsale.catalog.shared.web.RequestMdcFilter;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({ProductReadController.class, ProductWriteController.class})
@Import({
    SecurityConfig.class,
    RequestMdcFilter.class,
    TenantEnforcementFilter.class,
    ApiAuthenticationEntryPoint.class,
    ApiAccessDeniedHandler.class
})
class ProductApiSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductReadService productReadService;

    @MockBean
    private ProductWriteService productWriteService;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void clientCannotPostProduct() throws Exception {
        mockMvc.perform(post("/api/catalog/products")
                .with(jwtWithRole("CLIENT"))
                .with(csrf())
                .header("X-ORG-ID", "org-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Phone",
                      "description": "Demo",
                      "priceCents": 129900,
                      "currency": "USD"
                    }
                    """))
            .andExpect(status().isForbidden());
    }

    @Test
    void ownerCanPostProduct() throws Exception {
        ProductDetailDto dto = new ProductDetailDto(
            UUID.randomUUID(),
            "Phone",
            "Demo",
            129900,
            "USD",
            true,
            Instant.now(),
            Instant.now()
        );
        when(productWriteService.createProduct(any())).thenReturn(dto);

        mockMvc.perform(post("/api/catalog/products")
                .with(jwtWithRole("OWNER"))
                .with(csrf())
                .header("X-ORG-ID", "org-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Phone",
                      "description": "Demo",
                      "priceCents": 129900,
                      "currency": "USD"
                    }
                    """))
            .andExpect(status().isCreated());
    }

    @Test
    void bothClientAndOwnerCanGetProduct() throws Exception {
        UUID productId = UUID.randomUUID();
        when(productReadService.get(productId)).thenReturn(new ProductDetailDto(
            productId,
            "Phone",
            "Demo",
            129900,
            "USD",
            true,
            Instant.now(),
            Instant.now()
        ));

        mockMvc.perform(get("/api/catalog/products/{productId}", productId)
                .with(jwtWithRole("CLIENT"))
                .header("X-ORG-ID", "org-1"))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/catalog/products/{productId}", productId)
                .with(jwtWithRole("OWNER"))
                .header("X-ORG-ID", "org-1"))
            .andExpect(status().isOk());
    }

    @Test
    void clientCannotPutOrDeleteProduct() throws Exception {
        UUID productId = UUID.randomUUID();

        mockMvc.perform(put("/api/catalog/products/{productId}", productId)
                .with(jwtWithRole("CLIENT"))
                .with(csrf())
                .header("X-ORG-ID", "org-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Phone v2",
                      "description": "Updated",
                      "priceCents": 139900,
                      "currency": "USD"
                    }
                    """))
            .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/catalog/products/{productId}", productId)
                .with(jwtWithRole("CLIENT"))
                .with(csrf())
                .header("X-ORG-ID", "org-1"))
            .andExpect(status().isForbidden());
    }

    @Test
    void ownerCanPutOrDeleteProduct() throws Exception {
        UUID productId = UUID.randomUUID();
        ProductDetailDto dto = new ProductDetailDto(
            productId,
            "Phone v2",
            "Updated",
            139900,
            "USD",
            true,
            Instant.now(),
            Instant.now()
        );
        when(productWriteService.updateProduct(any(), any())).thenReturn(dto);

        mockMvc.perform(put("/api/catalog/products/{productId}", productId)
                .with(jwtWithRole("OWNER"))
                .with(csrf())
                .header("X-ORG-ID", "org-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Phone v2",
                      "description": "Updated",
                      "priceCents": 139900,
                      "currency": "USD"
                    }
                    """))
            .andExpect(status().isOk());

        mockMvc.perform(delete("/api/catalog/products/{productId}", productId)
                .with(jwtWithRole("OWNER"))
                .with(csrf())
                .header("X-ORG-ID", "org-1"))
            .andExpect(status().isNoContent());
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor jwtWithRole(String role) {
        return jwt().jwt(jwt -> jwt
                .claim("org_ids", List.of("org-1"))
                .claim("realm_access", Map.of("roles", List.of(role))))
            .authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }
}
