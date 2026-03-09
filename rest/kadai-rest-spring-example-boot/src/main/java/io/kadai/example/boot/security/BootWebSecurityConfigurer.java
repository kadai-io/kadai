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

package io.kadai.example.boot.security;

import static io.kadai.common.rest.ldap.LdapConfiguration.KADAI_LDAP_CONTEXT_SOURCE;

import io.kadai.common.rest.SpringSecurityToJaasFilter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.annotation.web.configurers.RequestCacheConfigurer;
import org.springframework.security.config.ldap.LdapPasswordComparisonAuthenticationManagerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.jaasapi.JaasApiIntegrationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

/** Default basic configuration for kadai web example. */
@Configuration
public class BootWebSecurityConfigurer {

  private final String ldapServerUrl;
  private final String ldapBaseDn;
  private final String ldapUserDnPatterns;
  private final String ldapGroupSearchBase;
  private final String ldapGroupSearchFilter;
  private final String ldapPermissionSearchBase;
  private final String ldapPermissionSearchFilter;

  private final boolean devMode;
  private final boolean enableCsrf;

  public BootWebSecurityConfigurer(
      @Value("${kadai.ldap.serverUrl:ldap://localhost:10389}") String ldapServerUrl,
      @Value("${kadai.ldap.baseDn:OU=Test,O=KADAI}") String ldapBaseDn,
      @Value("${kadai.ldap.userDnPatterns:uid={0},cn=users}") String ldapUserDnPatterns,
      @Value("${kadai.ldap.groupSearchBase:cn=groups}") String ldapGroupSearchBase,
      @Value("${kadai.ldap.groupSearchFilter:uniqueMember={0}}") String ldapGroupSearchFilter,
      @Value("${kadai.ldap.permissionSearchBase:cn=permissions}") String ldapPermissionSearchBase,
      @Value("${kadai.ldap.permissionSearchFilter:uniqueMember={0}}")
          String ldapPermissionSearchFilter,
      @Value("${enableCsrf:false}") boolean enableCsrf,
      @Value("${devMode:false}") boolean devMode) {
    this.enableCsrf = enableCsrf;
    this.ldapServerUrl = ldapServerUrl;
    this.ldapBaseDn = ldapBaseDn;
    this.ldapGroupSearchBase = ldapGroupSearchBase;
    this.ldapGroupSearchFilter = ldapGroupSearchFilter;
    this.ldapPermissionSearchBase = ldapPermissionSearchBase;
    this.ldapPermissionSearchFilter = ldapPermissionSearchFilter;
    this.ldapUserDnPatterns = ldapUserDnPatterns;
    this.devMode = devMode;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(
            authorizeHttpRequests ->
                authorizeHttpRequests
                    .requestMatchers("/css/**", "/img/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/docs/**")
                    .permitAll()
                    .requestMatchers(
                        HttpMethod.GET,
                        "/api-docs",
                        "/api-docs/**",
                        "/swagger-ui",
                        "/swagger-ui/**")
                    .permitAll())
        .cors(Customizer.withDefaults())
        .addFilter(jaasApiIntegrationFilter())
        .addFilterAfter(new SpringSecurityToJaasFilter(), JaasApiIntegrationFilter.class);

    if (enableCsrf) {
      CookieCsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
      csrfTokenRepository.setCookiePath("/");
      http.csrf(
              csrf ->
                  csrf.csrfTokenRepository(csrfTokenRepository)
                      .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler()))
          .addFilterAfter(new CsrfCookieFilter(), SpringSecurityToJaasFilter.class);
    } else {
      http.csrf(AbstractHttpConfigurer::disable).httpBasic(Customizer.withDefaults());
    }

    if (devMode) {
      http.headers(
              headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
          .authorizeHttpRequests(
              authorizeHttpRequests ->
                  authorizeHttpRequests
                      .requestMatchers("/h2-console/**")
                      .permitAll()
                      .anyRequest()
                      .fullyAuthenticated())
          .logout(logout -> logout.logoutSuccessUrl("http://localhost:4200/#").permitAll());
    } else {
      addLoginPageConfiguration(http);
    }
    http.requestCache(RequestCacheConfigurer::disable);
    return http.build();
  }

  @Bean
  public LdapAuthoritiesPopulator authoritiesPopulator(
      @Qualifier(KADAI_LDAP_CONTEXT_SOURCE) ContextSource contextSource) {
    Function<Map<String, List<String>>, GrantedAuthority> authorityMapper =
        recordVar -> new SimpleGrantedAuthority(recordVar.get("spring.security.ldap.dn").get(0));

    DefaultLdapAuthoritiesPopulator populator =
        new DefaultLdapAuthoritiesPopulator(contextSource, ldapGroupSearchBase);
    populator.setGroupSearchFilter(ldapGroupSearchFilter);
    populator.setSearchSubtree(true);
    populator.setRolePrefix("");
    populator.setAuthorityMapper(authorityMapper);
    return populator;
  }

  @Bean(name = KADAI_LDAP_CONTEXT_SOURCE)
  public BaseLdapPathContextSource ldapContextSource() {
    return new DefaultSpringSecurityContextSource(ldapServerUrl + "/" + ldapBaseDn);
  }

  @Bean
  public GrantedAuthoritiesMapper grantedAuthoritiesMapper() {
    SimpleAuthorityMapper grantedAuthoritiesMapper = new SimpleAuthorityMapper();
    grantedAuthoritiesMapper.setPrefix("");
    return grantedAuthoritiesMapper;
  }

  protected void addLoginPageConfiguration(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(
            authorizeHttpRequests -> authorizeHttpRequests.anyRequest().fullyAuthenticated())
        .formLogin(
            formLogin ->
                formLogin
                    .loginPage("/login")
                    .failureUrl("/login?error")
                    .defaultSuccessUrl("/index.html")
                    .permitAll())
        .logout(
            logout ->
                logout
                    .invalidateHttpSession(true)
                    .clearAuthentication(true)
                    .logoutRequestMatcher(
                        PathPatternRequestMatcher.withDefaults().matcher("/logout"))
                    .logoutSuccessUrl("/login?logout")
                    .deleteCookies("JSESSIONID")
                    .permitAll());
  }

  protected JaasApiIntegrationFilter jaasApiIntegrationFilter() {
    JaasApiIntegrationFilter filter = new JaasApiIntegrationFilter();
    filter.setCreateEmptySubject(true);
    return filter;
  }

  @Bean
  AuthenticationManager ldapAuthenticationManager(
      @Qualifier(KADAI_LDAP_CONTEXT_SOURCE) BaseLdapPathContextSource contextSource,
      LdapAuthoritiesPopulator authorities) {
    @SuppressWarnings("deprecation")
    LdapPasswordComparisonAuthenticationManagerFactory factory =
        new LdapPasswordComparisonAuthenticationManagerFactory(
            contextSource, NoOpPasswordEncoder.getInstance());
    factory.setUserDnPatterns(ldapUserDnPatterns);
    factory.setLdapAuthoritiesPopulator(authorities);
    factory.setAuthoritiesMapper(grantedAuthoritiesMapper());
    factory.setPasswordAttribute("userPassword");
    return factory.createAuthenticationManager();
  }
}
