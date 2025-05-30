/*
 * Copyright [2025] [envite consulting GmbH]
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

package io.kadai.example.boot;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import java.util.List;
import java.util.Map;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info =
        @Info(
            title = "KADAI RESTful API Documentation",
            version = "8.2.0",
            description =
                "<h1>Overview</h1>"
                    + "<p>"
                    + "This is the REST documentation for [KADAI](http://kadai.io) - the "
                    + "world’s first open source solution for Enterprise Task Management."
                    + "</p>"
                    + "<p>"
                    + "**For all Query Parameters:**<br> Whenever a parameter is an array type,"
                    + " several values can be passed by declaring that parameter multiple times."
                    + "</p>"
                    + "<p>"
                    + "Whenever a parameter is a complex type, the attributes of the value-object"
                    + " can be passed as a json. For example, a complex parameter with the name "
                    + "\"complex-query-param\" and attributes \"attribute1\" and \"attribute2\" "
                    + "would be specified in the following way:complex-query-param={\"attribute1\""
                    + ":\"value1\",\"attribute2\":\"value2\"}"
                    + "</p>"
                    + "<p>"
                    + "Whenever a parameter is a value-less type (e.g owner-is-null and "
                    + "current-user) it is expected to be defined without a value "
                    + " (?parameter), the empty value (?parameter=) or with the value \"true\" "
                    + "(?parameter=true)"
                    + "</p>"
                    + "<h1>Hypermedia Support</h1>"
                    + "<p>"
                    + "NOTE: HATEOAS support is still in development.Please have a look at "
                    + "example responses for each resource to determine the available links."
                    + "</p>"
                    + "<p>"
                    + "KADAI uses the [HATEOAS](https://restfulapi.net/hateoas/) (Hypermedia"
                    + " as the Engine of Application State) REST constraint. Most of our resources"
                    + " contain a _links section which contains navigation links. Besides, helping"
                    + " to navigate through our REST API, the navigation links also encapsulate the"
                    + " API. Using HATEOAS allows us to change some endpoints without modifying "
                    + "your frontend."
                    + "</p>"
                    + "<h1>Errors</h1>"
                    + "<p>"
                    + "In order to support multilingual websites, KADAI uses error codes to "
                    + "define which error occurred. Additionally, an optional set of message "
                    + "variables, containing some technical information, is added, so that the "
                    + "website can describe the error with all details."
                    + "</p>"
                    + "<table>"
                    + "<thead>"
                    + "<tr>"
                    + "<th>Status Code</th>"
                    + "<th>Key</th>"
                    + "<th>Message Variables</th>"
                    + "</tr>"
                    + "</thead>"
                    + "<tbody>"
                    + "<tr>"
                    + "<td>**400 BAD_REQUEST**</td>"
                    + "<td>CLASSIFICATION_SERVICE_LEVEL_MALFORMED</td>"
                    + "<td>serviceLevel, classificationKey, domain</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>**400 BAD_REQUEST**</td>"
                    + "<td>CUSTOM_HOLIDAY_WRONG_FORMAT</td>"
                    + "<td>customHoliday</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>**400 BAD_REQUEST**</td>"
                    + "<td>DOMAIN_NOT_FOUND</td>"
                    + "<td>domain</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>**400 BAD_REQUEST**</td>"
                    + "<td>INVALID_ARGUMENT</td>"
                    + "<td></td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>**400 BAD_REQUEST**</td>"
                    + "<td>QUERY_PARAMETER_MALFORMED</td>"
                    + "<td>malformedQueryParameters</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>**400 BAD_REQUEST**</td>"
                    + "<td>TASK_INVALID_CALLBACK_STATE</td>"
                    + "<td>taskId, taskCallbackState, requiredCallbackStates</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>**400 BAD_REQUEST**</td>"
                    + "<td>TASK_INVALID_OWNER</td>"
                    + "<td>taskId, currentUserId</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>**400 BAD_REQUEST**</td>"
                    + "<td>TASK_INVALID_STATE</td>"
                    + "<td>taskId, taskState, requiredTaskStates</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>**403 FORBIDDEN**</td>"
                    + "<td>NOT_AUTHORIZED</td>"
                    + "<td>roles, currentUserId</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>**403 FORBIDDEN**</td>"
                    + "<td>NOT_AUTHORIZED_ON_TASK_COMMENT</td>"
                    + "<td>currentUserId, taskCommentId</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>**403 FORBIDDEN**</td>"
                    + "<td>NOT_AUTHORIZED_ON_WORKBASKET_WITH_ID</td>"
                    + "<td>currentUserId, workbasketId, requiredPermissions</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>**403 FORBIDDEN**</td>"
                    + "<td>NOT_AUTHORIZED_ON_WORKBASKET_WITH_KEY_AND_DOMAIN</td>"
                    + "<td>currentUserId, workbasketKey, domain, requiredPermissions</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>**404 NOT_FOUND**</td>"
                    + "<td>CLASSIFICATION_WITH_ID_NOT_FOUND</td>"
                    + "<td>classificationId</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>**404 NOT_FOUND**</td>"
                    + "<td>CLASSIFICATION_WITH_KEY_NOT_FOUND</td>"
                    + "<td>classificationKey, domain</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>**404 NOT_FOUND**</td>"
                    + "<td>TASK_COMMENT_NOT_FOUND</td>"
                    + "<td>taskCommentId</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>**404 NOT_FOUND**</td>"
                    + "<td>TASK_NOT_FOUND</td>"
                    + "<td>taskId</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>**404 NOT_FOUND**</td>"
                    + "<td>USER_NOT_FOUND</td>"
                    + "<td>userId</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>**404 NOT_FOUND**</td>"
                    + "<td>WORKBASKET_WITH_ID_NOT_FOUND</td>"
                    + "<td>workbasketId</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>**404 NOT_FOUND**</td>"
                    + "<td>WORKBASKET_WITH_KEY_NOT_FOUND</td>"
                    + "<td>workbasketKey, domain</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>**404 NOT_FOUND**</td>"
                    + "<td>HISTORY_EVENT_NOT_FOUND</td>"
                    + "<td>historyEventId</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>**409 CONFLICT**</td>"
                    + "<td>ATTACHMENT_ALREADY_EXISTS</td>"
                    + "<td>attachmentId, taskId</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>**409 CONFLICT**</td>"
                    + "<td>CLASSIFICATION_ALREADY_EXISTS</td>"
                    + "<td>classificationKey, domain</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>**409 CONFLICT**</td>"
                    + "<td>ENTITY_NOT_UP_TO_DATE</td>"
                    + "<td>entityId</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>**409 CONFLICT**</td>"
                    + "<td>TASK_ALREADY_EXISTS</td>"
                    + "<td>externalTaskId</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>**409 CONFLICT**</td>"
                    + "<td>USER_ALREADY_EXISTS</td>"
                    + "<td>userID</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>**409 CONFLICT**</td>"
                    + "<td>WORKBASKET_ACCESS_ITEM_ALREADY_EXISTS</td>"
                    + "<td>accessId, workbasketId</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>**409 CONFLICT**</td>"
                    + "<td>WORKBASKET_ALREADY_EXISTS</td>"
                    + "<td>workbasketKey, domain</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>**409 CONFLICT**</td>"
                    + "<td>WORKBASKET_MARKED_FOR_DELETION</td>"
                    + "<td>workbasketId</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>**413 PAYLOAD_TOO_LARGE**</td>"
                    + "<td>PAYLOAD_TOO_LARGE</td>"
                    + "<td></td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>**423 LOCKED**</td>"
                    + "<td>CLASSIFICATION_IN_USE</td>"
                    + "<td>classificationKey, domain</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>**423 LOCKED**</td>"
                    + "<td>WORKBASKET_IN_USE</td>"
                    + "<td>workbasketId</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>**500 INTERNAL_SERVER_ERROR**</td>"
                    + "<td>CONNECTION_AUTOCOMMIT_FAILED</td>"
                    + "<td></td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>**500 INTERNAL_SERVER_ERROR**</td>"
                    + "<td>CONNECTION_NOT_SET</td>"
                    + "<td></td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>**500 INTERNAL_SERVER_ERROR**</td>"
                    + "<td>CRITICAL_SYSTEM_ERROR</td>"
                    + "<td></td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>**500 INTERNAL_SERVER_ERROR**</td>"
                    + "<td>DATABASE_UNSUPPORTED</td>"
                    + "<td>databaseProductName</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>**500 INTERNAL_SERVER_ERROR**</td>"
                    + "<td>UNKNOWN_ERROR</td>"
                    + "<td></td>"
                    + "</tr>"
                    + "</tbody>"
                    + "</table>"
                    + "<h2>Errors</h2>"
                    + "<table>"
                    + "<thead>"
                    + "<tr>"
                    + "<th>Key</th>"
                    + "<th>Type</th>"
                    + "</tr>"
                    + "</thead>"
                    + "<tbody>"
                    + "<tr>"
                    + "<td>accessId</td>"
                    + "<td>String</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>attachmentId</td>"
                    + "<td>String</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>classificationId</td>"
                    + "<td>String</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>classificationKey</td>"
                    + "<td>String</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>currentUserId</td>"
                    + "<td>String</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>customHoliday</td>"
                    + "<td>String</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>databaseProductName</td>"
                    + "<td>String</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>domain</td>"
                    + "<td>String</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>externalTaskId</td>"
                    + "<td>String</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>historyEventId</td>"
                    + "<td>String</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>malformedQueryParameters</td>"
                    + "<td>MalformedQueryParameter[]</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>requiredCallbackStates</td>"
                    + "<td>CallbackState[]</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>requiredPermissions</td>"
                    + "<td>WorkbasketPermission[]</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>requiredTaskStates</td>"
                    + "<td>TaskState[]</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>roles</td>"
                    + "<td>KadaiRole[]</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>taskCallbackState</td>"
                    + "<td>CallbackState</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>taskCommentId</td>"
                    + "<td>String</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>taskId</td>"
                    + "<td>String</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>taskState</td>"
                    + "<td>TaskState</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>workbasketId</td>"
                    + "<td>String</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>workbasketKey</td>"
                    + "<td>String</td>"
                    + "</tr>"
                    + "</tbody>"
                    + "</table>"),
    security = {@SecurityRequirement(name = "basicAuth")})
@SecurityScheme(name = "basicAuth", type = SecuritySchemeType.HTTP, scheme = "basic")
public class OpenApiConfiguration {
  @Bean
  public OpenApiCustomizer openApiCustomizer() {
    return openApi -> {
      ArraySchema arraySchema = new ArraySchema().items(new StringSchema());

      // Define the ObjectSchema for the map with String keys and Array of Strings as values
      ObjectSchema mapSchema =
          (ObjectSchema)
              new ObjectSchema()
                  .additionalProperties(arraySchema)
                  .name("TypeMapSchema")
                  .example(
                      Map.of(
                          "key1",
                          List.of("value1", "value2"),
                          "key2",
                          List.of("value3", "value4")));

      // Add the schema to the components
      var schemas = openApi.getComponents().getSchemas();
      schemas.put(mapSchema.getName(), mapSchema);
    };
  }
}
