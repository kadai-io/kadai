/*
 * Copyright [2024] [envite consulting GmbH]
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

package io.kadai.rest.test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule;
import org.springframework.http.HttpHeaders;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

/** Helps to simplify rest api testing. */
@Component
public class RestHelper {

  public static final RestClient CLIENT = getRestClient();

  private Environment environment;
  private int port;

  @Autowired
  public RestHelper(Environment environment) {
    this.environment = environment;
  }

  public RestHelper(int port) {
    this.port = port;
  }

  public static HttpHeaders generateHeadersForUser(String user) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", encodeUserAndPasswordAsBasicAuth(user));
    headers.add("Content-Type", MediaTypes.HAL_JSON_VALUE);
    return headers;
  }

  public static String encodeUserAndPasswordAsBasicAuth(String user) {
    String toEncode = user + ":" + user;
    return "Basic " + Base64.getEncoder().encodeToString(toEncode.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Return a REST Client which is capable of dealing with responses in HAL format.
   *
   * @return RestClient
   */
  private static RestClient getRestClient() {
    ObjectMapper mapper =
        new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .registerModule(new Jackson2HalModule())
            .registerModule(new ParameterNamesModule())
            .registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule());
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
    converter.setSupportedMediaTypes(Collections.singletonList(MediaTypes.HAL_JSON));
    converter.setObjectMapper(mapper);

    // Create RestClient with custom message converter
    return RestClient.builder()
        .messageConverters(converters -> converters.add(0, converter))
        .build();
  }

  public String toUrl(String relativeUrl, Object... uriVariables) {
    return UriComponentsBuilder.fromPath(relativeUrl)
        .scheme("http")
        .host("127.0.0.1")
        .port(getPort())
        .build(false)
        .expand(uriVariables)
        .toString();
  }

  private int getPort() {
    return Optional.ofNullable(environment)
        .map(e -> e.getRequiredProperty("local.server.port", int.class))
        .orElse(port);
  }
}
