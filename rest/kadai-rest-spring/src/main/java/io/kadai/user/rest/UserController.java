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

package io.kadai.user.rest;

import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.api.exceptions.NotAuthorizedException;
import io.kadai.common.api.security.CurrentUserContext;
import io.kadai.common.rest.RestEndpoints;
import io.kadai.common.rest.util.QueryParamsValidator;
import io.kadai.user.api.UserQuery;
import io.kadai.user.api.UserService;
import io.kadai.user.api.exceptions.UserAlreadyExistException;
import io.kadai.user.api.exceptions.UserNotFoundException;
import io.kadai.user.api.models.User;
import io.kadai.user.rest.assembler.UserRepresentationModelAssembler;
import io.kadai.user.rest.models.UserCollectionRepresentationModel;
import io.kadai.user.rest.models.UserRepresentationModel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Controller for all {@linkplain User} related endpoints. */
@RestController
@EnableHypermediaSupport(type = HypermediaType.HAL)
public class UserController {
  private final UserService userService;
  private final UserRepresentationModelAssembler userAssembler;

  private final CurrentUserContext currentUserContext;

  @Autowired
  UserController(
      UserService userService,
      UserRepresentationModelAssembler userAssembler,
      CurrentUserContext currentUserContext) {
    this.userService = userService;
    this.userAssembler = userAssembler;
    this.currentUserContext = currentUserContext;
  }

  /**
   * This endpoint retrieves a User.
   *
   * @title Get a User
   * @param userId the id of the requested User
   * @return the requested User
   * @throws UserNotFoundException if the id has not been found
   * @throws InvalidArgumentException if the id is null or empty
   */
  @Operation(
      summary = "Get a User",
      description = "This endpoint retrieves a User.",
      parameters = {
        @Parameter(
            name = "userId",
            description = "The ID of the requested user",
            example = "teamlead-1")
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "The requested User",
            content =
                @Content(
                    mediaType = MediaTypes.HAL_JSON_VALUE,
                    schema = @Schema(implementation = UserRepresentationModel.class)))
      })
  @GetMapping(RestEndpoints.URL_USERS_ID)
  @Transactional(readOnly = true, rollbackFor = Exception.class)
  public ResponseEntity<UserRepresentationModel> getUser(@PathVariable("userId") String userId)
      throws UserNotFoundException, InvalidArgumentException {
    User user = userService.getUser(userId);
    return ResponseEntity.ok(userAssembler.toModel(user));
  }

  /**
   * This endpoint retrieves multiple Users. If a userId can't be found in the database it will be
   * ignored. Any combination of parameters is interpreted as conjunction of those.
   *
   * @title Get multiple Users
   * @param request the HttpServletRequest of the request itself
   * @param filterParameter the filter parameters regarding UserQueryFilterParameter
   * @return the requested Users
   * @throws InvalidArgumentException if the userIds are null or empty
   */
  @Operation(
      summary = "Get multiple Users",
      description =
          "This endpoint retrieves multiple Users. If a userId can't be found in the database it "
              + "will be ignored. "
              + "Any combination of parameters is interpreted as conjunction of those.",
      parameters = {
        @Parameter(
            name = "user-id",
            description = "The IDs of the users to be retrieved",
            example = "teamlead-1"),
        @Parameter(
            name = "current-user",
            description = "Whether to fetch the current user as well",
            example = "user-1-1"),
        @Parameter(
            name = "orgLevel1",
            description = "the orgLevel1 of the users to be retrieved",
            example = "envite"),
        @Parameter(
            name = "orgLevel2",
            description = "the orgLevel2 of the users to be retrieved",
            example = "bpm"),
        @Parameter(
            name = "orgLevel3",
            description = "the orgLevel3 of the users to be retrieved",
            example = "green-bpm"),
        @Parameter(
            name = "orgLevel4",
            description = "the orgLevel4 of the users to be retrieved",
            example = "kadai"),
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "The requested Users",
            content =
                @Content(
                    mediaType = MediaTypes.HAL_JSON_VALUE,
                    schema = @Schema(implementation = UserCollectionRepresentationModel.class)))
      })
  @GetMapping(RestEndpoints.URL_USERS)
  @Transactional(readOnly = true, rollbackFor = Exception.class)
  public ResponseEntity<UserCollectionRepresentationModel> getUsers(
      HttpServletRequest request, @ParameterObject UserQueryFilterParameter filterParameter)
      throws InvalidArgumentException {
    if (filterParameter.getCurrentUser() != null
        && QueryParamsValidator.hasQueryParameterValues(request, "current-user")) {
      throw new InvalidArgumentException(
          "It is prohibited to use the param current-user with values.");
    }
    filterParameter.addCurrentUserIdIfPresentWithContext(currentUserContext);

    UserQuery query = userService.createUserQuery();
    filterParameter.apply(query);

    return ResponseEntity.ok(userAssembler.toKadaiCollectionModel(query.list()));
  }

  /**
   * This endpoint creates a User.
   *
   * @title Create a User
   * @param repModel the User which should be created
   * @return the inserted User
   * @throws InvalidArgumentException if the id has not been set
   * @throws UserAlreadyExistException if a User with id } is already existing
   * @throws NotAuthorizedException if the current user is no admin or business-admin
   */
  @Operation(
      summary = "Create a User",
      description = "This endpoint creates a new User.",
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "the User which should be created",
              required = true,
              content =
                  @Content(
                      schema = @Schema(implementation = UserRepresentationModel.class),
                      examples =
                          @ExampleObject(
                              value =
                                  """
                                      {
                                        "userId": "user-10-2",
                                        "groups": [],
                                        "permissions": [],
                                        "domains": [],
                                        "firstName": "Hans",
                                        "lastName": "Georg"
                                      }"""))),
      responses = {
        @ApiResponse(
            responseCode = "201",
            description = "The inserted User",
            content =
                @Content(
                    mediaType = MediaTypes.HAL_JSON_VALUE,
                    schema = @Schema(implementation = UserRepresentationModel.class)))
      })
  @PostMapping(RestEndpoints.URL_USERS)
  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<UserRepresentationModel> createUser(
      @RequestBody UserRepresentationModel repModel)
      throws InvalidArgumentException, UserAlreadyExistException, NotAuthorizedException {
    User user = userAssembler.toEntityModel(repModel);
    user = userService.createUser(user);

    return ResponseEntity.status(HttpStatus.CREATED).body(userAssembler.toModel(user));
  }

  /**
   * This endpoint updates a User.
   *
   * @title Update a User
   * @param userId the id of the User to update
   * @param repModel the User with the updated fields
   * @return the updated User
   * @throws InvalidArgumentException if the id has not been set
   * @throws UserNotFoundException if a User with id is not existing in the database
   * @throws NotAuthorizedException if the current user is no admin or business-admin
   */
  @Operation(
      summary = "Update a User",
      description = "This endpoint updates a User.",
      parameters = {
        @Parameter(
            name = "userId",
            description = "The ID of the User to update",
            example = "teamlead-1")
      },
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "the User with the updated fields",
              required = true,
              content =
                  @Content(
                      schema = @Schema(implementation = UserRepresentationModel.class),
                      examples = {
                        @ExampleObject(
                            value =
                                """
                                    {
                                      "userId": "teamlead-1",
                                      "groups": [],
                                      "permissions": [],
                                      "domains": ["DOMAIN_A"],
                                      "firstName": "new name",
                                      "lastName": "Toll",
                                      "fullName": "Toll, Titus",
                                      "longName": "Toll, Titus - (teamlead-1)",
                                      "email": "titus.toll@web.de",
                                      "phone": "040-2951854",
                                      "mobilePhone": "015637683197",
                                      "orgLevel4": "Envite",
                                      "orgLevel3": "BPM",
                                      "orgLevel2": "Human Workflow",
                                      "orgLevel1": "KADAI",
                                      "data": "xy"
                                    }""")
                      })),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "The updated User",
            content =
                @Content(
                    mediaType = MediaTypes.HAL_JSON_VALUE,
                    schema = @Schema(implementation = UserRepresentationModel.class)))
      })
  @PutMapping(RestEndpoints.URL_USERS_ID)
  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<UserRepresentationModel> updateUser(
      @PathVariable("userId") String userId, @RequestBody UserRepresentationModel repModel)
      throws InvalidArgumentException, UserNotFoundException, NotAuthorizedException {
    if (!userId.equals(repModel.getUserId())) {
      throw new InvalidArgumentException(
          String.format(
              "UserId '%s' of the URI is not identical"
                  + " with the userId '%s' of the object in the payload.",
              userId, repModel.getUserId()));
    }
    User user = userAssembler.toEntityModel(repModel);
    user = userService.updateUser(user);

    return ResponseEntity.ok(userAssembler.toModel(user));
  }

  /**
   * This endpoint deletes a User.
   *
   * @title Delete a User
   * @param userId the id of the User to delete
   * @return no content
   * @throws UserNotFoundException if the id has not been found
   * @throws NotAuthorizedException if the current user is no admin or business-admin
   * @throws InvalidArgumentException if the id is null or empty
   */
  @Operation(
      summary = "Delete a User",
      description = "This endpoint deletes a User.",
      parameters = {
        @Parameter(
            name = "userId",
            description = "The ID of the user to delete",
            example = "user-1-1")
      },
      responses = {
        @ApiResponse(
            responseCode = "204",
            description = "User deleted",
            content = @Content(schema = @Schema()))
      })
  @DeleteMapping(RestEndpoints.URL_USERS_ID)
  @Transactional(readOnly = true, rollbackFor = Exception.class)
  public ResponseEntity<UserRepresentationModel> deleteUser(@PathVariable("userId") String userId)
      throws UserNotFoundException, NotAuthorizedException, InvalidArgumentException {
    userService.deleteUser(userId);

    return ResponseEntity.noContent().build();
  }
}
