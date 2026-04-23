/*
 * Copyright [2026] [envite consulting GmbH]
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 *
 */

package io.kadai.common.rest.config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for Spring Boot Actuator management endpoints.
 *
 * <p>The {@code /actuator/health} endpoint is intentionally exposed without authentication so that
 * external monitoring systems (e.g., load-balancers, Kubernetes liveness probes, Uptime Robot) can
 * check application health without credentials.
 *
 * <p>All other management endpoints (e.g., {@code /actuator/info}) require HTTP Basic
 * authentication and are therefore protected from anonymous access.
 *
 * <p>This filter chain is registered with {@code @Order(1)} so that it takes precedence over the
 * application's main security filter chain for every request that targets a management endpoint.
 */
@Configuration(proxyBeanMethods = false)
public class ActuatorSecurityConfiguration {

  /**
   * Creates a dedicated {@link SecurityFilterChain} that secures all Actuator management endpoints.
   *
   * <ul>
   *   <li>{@code GET /actuator/health} – publicly accessible (no credentials required)
   *   <li>All other management endpoints – require HTTP Basic authentication
   * </ul>
   *
   * @param http the {@link HttpSecurity} to configure
   * @return the configured {@link SecurityFilterChain}
   * @throws Exception if an error occurs during configuration
   */
  @Bean
  @Order(1)
  public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
    http.securityMatcher(EndpointRequest.toAnyEndpoint())
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(EndpointRequest.to(HealthEndpoint.class))
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .httpBasic(withDefaults());
    return http.build();
  }
}
