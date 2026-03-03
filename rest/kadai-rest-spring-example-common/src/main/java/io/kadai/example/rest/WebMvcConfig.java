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

package io.kadai.example.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import tools.jackson.databind.json.JsonMapper;

/** The Web MVC Configuration. */
@Configuration
@EnableWebMvc
public class WebMvcConfig implements WebMvcConfigurer {

  private static final String[] CLASSPATH_RESOURCE_LOCATIONS = {
    "classpath:/META-INF/resources/",
    "classpath:/resources/",
    "classpath:/static/",
    "classpath:/public/",
    "classpath:/templates/"
  };

  private final JsonMapper jsonMapper;

  @Autowired
  public WebMvcConfig(JsonMapper jsonMapper) {
    this.jsonMapper = jsonMapper;
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    if (!registry.hasMappingForPattern("/webjars/**")) {
      registry
          .addResourceHandler("/webjars/**")
          .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
    if (!registry.hasMappingForPattern("/**")) {
      registry.addResourceHandler("/**").addResourceLocations(CLASSPATH_RESOURCE_LOCATIONS);
    }
  }

  //  @Override
  //  public void configureMessageConverters(ServerBuilder builder) {
  //    builder.configureMessageConverters(converter -> {
  //      if (converter instanceof JacksonJsonHttpMessageConverter) {
  //        JacksonJsonHttpMessageConverter jacksonConverter =
  //            (JacksonJsonHttpMessageConverter) converter;
  //        jacksonConverter.setPrettyPrint(true);
  //      }
  //    });
  //  }
  //
  //  @PostConstruct
  //  public void enableObjectIndent() {
  //    jsonMapper.(SerializationFeature.INDENT_OUTPUT);
  //  }
}
