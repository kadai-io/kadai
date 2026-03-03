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

package io.kadai.rest.test;

import io.kadai.sampledata.SampleDataGenerator;
import java.util.Collections;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.DependsOn;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

@SpringBootApplication
@ComponentScan("io.kadai")
@DependsOn("getKadaiEngine") // wait for schema to be created BEFORE inserting test data
public class TestConfiguration {

  @Autowired
  public TestConfiguration(
      @Value("${kadai.schemaName:KADAI}") String schemaName, DataSource dataSource) {
    new SampleDataGenerator(dataSource, schemaName).generateSampleData();
  }

  @Bean
  public PlatformTransactionManager txManager(DataSource dataSource) {
    return new DataSourceTransactionManager(dataSource);
  }

  @Bean
  public RestClient restClient(@Autowired JsonMapper jsonMapper) {
    JacksonJsonHttpMessageConverter converter = new JacksonJsonHttpMessageConverter(jsonMapper);
    converter.setSupportedMediaTypes(Collections.singletonList(MediaTypes.HAL_JSON));

    // Create RestClient with custom message converter
    return RestClient.builder()
        .configureMessageConverters(converters -> converters.addCustomConverter(converter))
        .build();
  }

  @Bean
  JsonMapperBuilderCustomizer customizer() {
    return builder ->
        builder
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
  }
}
