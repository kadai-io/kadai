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

package io.kadai.user.rest;

import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.api.exceptions.NotAuthorizedException;
import io.kadai.common.api.security.CurrentUserContext;
import io.kadai.common.rest.QueryPagingParameter;
import io.kadai.common.rest.QuerySortParameter;
import io.kadai.common.rest.RestEndpoints;
import io.kadai.common.rest.util.QueryParamsValidator;
import io.kadai.user.api.UserQuery;
import io.kadai.user.api.UserService;
import io.kadai.user.api.exceptions.UserAlreadyExistException;
import io.kadai.user.api.exceptions.UserNotFoundException;
import io.kadai.user.api.models.User;
import io.kadai.user.api.models.UserSummary;
import io.kadai.user.rest.assembler.UserRepresentationModelAssembler;
import io.kadai.user.rest.assembler.UserSummaryRepresentationModelAssembler;
import io.kadai.user.rest.models.UserRepresentationModel;
import io.kadai.user.rest.models.UserSummaryPagedRepresentationModel;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
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
public class UserController implements UserApi {
  private final UserService userService;
  private final UserRepresentationModelAssembler userAssembler;
  private final UserSummaryRepresentationModelAssembler userSummaryAssembler;

  private final CurrentUserContext currentUserContext;

  @Autowired
  UserController(
      UserService userService,
      UserRepresentationModelAssembler userAssembler,
      UserSummaryRepresentationModelAssembler userSummaryAssembler,
      CurrentUserContext currentUserContext) {
    this.userService = userService;
    this.userAssembler = userAssembler;
    this.userSummaryAssembler = userSummaryAssembler;
    this.currentUserContext = currentUserContext;
  }

  @GetMapping(RestEndpoints.URL_USERS_ID)
  public ResponseEntity<UserRepresentationModel> getUser(@PathVariable("userId") String userId)
      throws UserNotFoundException, InvalidArgumentException {
    User user = userService.getUser(userId);
    return ResponseEntity.ok(userAssembler.toModel(user));
  }

  @GetMapping(RestEndpoints.URL_USERS)
  public ResponseEntity<UserSummaryPagedRepresentationModel> getUsers(
      HttpServletRequest request,
      @ParameterObject UserQueryFilterParameter filterParameter,
      @ParameterObject UserQuerySortParameter sortParameter,
      @ParameterObject QueryPagingParameter<UserSummary, UserQuery> pagingParameter)
      throws InvalidArgumentException {

    QueryParamsValidator.validateParams(
        request,
        UserQueryFilterParameter.class,
        QuerySortParameter.class,
        QueryPagingParameter.class);

    filterParameter.addCurrentUserIdIfPresentWithContext(currentUserContext);

    UserQuery query = userService.createUserQuery();
    filterParameter.apply(query);
    sortParameter.apply(query);

    List<UserSummary> users = pagingParameter.apply(query);

    UserSummaryPagedRepresentationModel pagedModels =
        userSummaryAssembler.toPagedModel(users, pagingParameter.getPageMetadata());

    return ResponseEntity.ok(pagedModels);
  }

  @PostMapping(RestEndpoints.URL_USERS)
  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<UserRepresentationModel> createUser(
      @RequestBody UserRepresentationModel repModel)
      throws InvalidArgumentException, UserAlreadyExistException, NotAuthorizedException {
    User user = userAssembler.toEntityModel(repModel);
    user = userService.createUser(user);

    return ResponseEntity.status(HttpStatus.CREATED).body(userAssembler.toModel(user));
  }

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

  @DeleteMapping(RestEndpoints.URL_USERS_ID)
  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<UserRepresentationModel> deleteUser(@PathVariable("userId") String userId)
      throws UserNotFoundException, NotAuthorizedException, InvalidArgumentException {
    userService.deleteUser(userId);

    return ResponseEntity.noContent().build();
  }
}
