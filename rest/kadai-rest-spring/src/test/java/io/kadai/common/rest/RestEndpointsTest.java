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

package io.kadai.common.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import io.kadai.task.rest.TaskController;
import org.junit.jupiter.api.Test;
import org.springframework.web.util.UriComponentsBuilder;

/** Test RestEndpoints and Linkbuilder. */
class RestEndpointsTest {

  @Test
  void testMapping() {

    String mapUrl = RestEndpoints.URL_DOMAIN;
    String buildUrl = linkTo(methodOn(KadaiEngineController.class).getDomains()).toString();
    assertThat(buildUrl).isEqualTo(mapUrl);
  }

  @Test
  void testMappingWithVariable() throws Exception {

    String id = "25";

    String mapUrl =
        UriComponentsBuilder.fromPath(RestEndpoints.URL_TASKS_ID).buildAndExpand(id).toUriString();
    String buildUrl = linkTo(methodOn(TaskController.class).getTask(id)).toString();
    assertThat(buildUrl).isEqualTo(mapUrl);
  }
}
