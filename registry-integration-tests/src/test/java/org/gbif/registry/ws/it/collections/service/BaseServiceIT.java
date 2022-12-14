/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.registry.ws.it.collections.service;

import org.gbif.api.vocabulary.UserRole;
import org.gbif.registry.database.PostgresDBExtension;
import org.gbif.registry.database.RegistryDatabaseInitializer;
import org.gbif.registry.events.collections.AuditLogger;
import org.gbif.registry.ws.it.RegistryIntegrationTestsConfiguration;
import org.gbif.registry.ws.it.fixtures.RequestTestFixture;
import org.gbif.registry.ws.it.fixtures.TestConstants;
import org.gbif.ws.client.filter.SimplePrincipalProvider;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/** Base class for IT tests that initializes data sources and basic security settings. */
@ExtendWith(SpringExtension.class)
@SpringBootTest(
    classes = {RegistryIntegrationTestsConfiguration.class, BaseServiceIT.MockConfig.class})
@ActiveProfiles({"test", "mock"})
@DirtiesContext
public class BaseServiceIT {

  // the audit log is tested in the AuditLogIT
  @MockBean private AuditLogger auditLogger;

  @RegisterExtension
  protected static PostgresDBExtension database =
      PostgresDBExtension.builder()
          .liquibaseChangeLogFile(TestConstants.LIQUIBASE_MASTER_FILE)
          .initializer(new RegistryDatabaseInitializer())
          .build();

  private final SimplePrincipalProvider simplePrincipalProvider;

  public BaseServiceIT(SimplePrincipalProvider simplePrincipalProvider) {
    this.simplePrincipalProvider = simplePrincipalProvider;
  }

  @BeforeEach
  public void setup() {
    // reset SimplePrincipleProvider, configured for web resources tests only
    if (simplePrincipalProvider != null) {
      simplePrincipalProvider.setPrincipal(TestConstants.TEST_ADMIN);
      SecurityContext ctx = SecurityContextHolder.createEmptyContext();
      SecurityContextHolder.setContext(ctx);
      ctx.setAuthentication(
          new UsernamePasswordAuthenticationToken(
              simplePrincipalProvider.get().getName(),
              "",
              Arrays.asList(
                  new SimpleGrantedAuthority(UserRole.REGISTRY_ADMIN.name()),
                  new SimpleGrantedAuthority(UserRole.GRSCICOLL_ADMIN.name()))));
    }
  }

  protected void resetSecurityContext(String principal, UserRole... role) {
    simplePrincipalProvider.setPrincipal(principal);
    SecurityContext ctx = SecurityContextHolder.createEmptyContext();
    SecurityContextHolder.setContext(ctx);
    List<SimpleGrantedAuthority> authorities =
        Arrays.stream(role)
            .map(r -> new SimpleGrantedAuthority(r.name()))
            .collect(Collectors.toList());

    ctx.setAuthentication(
        new UsernamePasswordAuthenticationToken(
            simplePrincipalProvider.get().getName(), "", authorities));
  }

  public SimplePrincipalProvider getSimplePrincipalProvider() {
    return simplePrincipalProvider;
  }

  @TestConfiguration
  @Profile("mock")
  public static class MockConfig {

    @MockBean RequestTestFixture requestTestFixture;
  }

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    registry.add("registry.datasource.url", () -> database.getPostgresContainer().getJdbcUrl());
    registry.add(
        "registry.datasource.username", () -> database.getPostgresContainer().getUsername());
    registry.add(
        "registry.datasource.password", () -> database.getPostgresContainer().getPassword());
    registry.add("elasticsearch.mock", () -> "true");
  }
}
