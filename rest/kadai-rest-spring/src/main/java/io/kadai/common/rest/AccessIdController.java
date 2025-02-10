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

package io.kadai.common.rest;

import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.KadaiRole;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.api.exceptions.NotAuthorizedException;
import io.kadai.common.rest.ldap.LdapClient;
import io.kadai.common.rest.models.AccessIdRepresentationModel;
import java.util.List;
import javax.naming.InvalidNameException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Controller for Access Id validation. */
@RestController
@EnableHypermediaSupport(type = HypermediaType.HAL)
public class AccessIdController implements AccessIdApi {

  private final LdapClient ldapClient;
  private final KadaiEngine kadaiEngine;

  @Autowired
  public AccessIdController(LdapClient ldapClient, KadaiEngine kadaiEngine) {
    this.ldapClient = ldapClient;
    this.kadaiEngine = kadaiEngine;
  }

  @GetMapping(path = RestEndpoints.URL_ACCESS_ID)
  public ResponseEntity<List<AccessIdRepresentationModel>> searchUsersAndGroupsAndPermissions(
      @RequestParam("search-for") String searchFor)
      throws InvalidArgumentException, NotAuthorizedException, InvalidNameException {
    kadaiEngine.checkRoleMembership(KadaiRole.ADMIN, KadaiRole.BUSINESS_ADMIN);

    List<AccessIdRepresentationModel> accessIdUsers =
        ldapClient.searchUsersAndGroupsAndPermissions(searchFor);
    return ResponseEntity.ok(accessIdUsers);
  }

  @GetMapping(path = RestEndpoints.URL_ACCESS_ID_WITH_NAME)
  public ResponseEntity<List<AccessIdRepresentationModel>> searchUsersByNameOrAccessIdForRole(
      @RequestParam("search-for") String nameOrAccessId, @RequestParam("role") String role)
      throws InvalidArgumentException, NotAuthorizedException {
    kadaiEngine.checkRoleMembership(KadaiRole.USER, KadaiRole.BUSINESS_ADMIN, KadaiRole.ADMIN);

    if (!role.equals("user")) {
      throw new InvalidArgumentException(
          String.format(
              "Requested users for not supported role %s.  Only role 'user' is supported'", role));
    }
    List<AccessIdRepresentationModel> accessIdUsers =
        ldapClient.searchUsersByNameOrAccessIdInUserRole(nameOrAccessId);
    return ResponseEntity.ok(accessIdUsers);
  }

  @GetMapping(path = RestEndpoints.URL_ACCESS_ID_GROUPS)
  public ResponseEntity<List<AccessIdRepresentationModel>> getGroupsByAccessId(
      @RequestParam("access-id") String accessId)
      throws InvalidArgumentException, NotAuthorizedException, InvalidNameException {
    kadaiEngine.checkRoleMembership(KadaiRole.ADMIN, KadaiRole.BUSINESS_ADMIN);

    List<AccessIdRepresentationModel> accessIds =
        ldapClient.searchGroupsAccessIdIsMemberOf(accessId);

    return ResponseEntity.ok(accessIds);
  }

  @GetMapping(path = RestEndpoints.URL_ACCESS_ID_PERMISSIONS)
  public ResponseEntity<List<AccessIdRepresentationModel>> getPermissionsByAccessId(
      @RequestParam("access-id") String accessId)
      throws InvalidArgumentException, NotAuthorizedException, InvalidNameException {
    kadaiEngine.checkRoleMembership(KadaiRole.ADMIN, KadaiRole.BUSINESS_ADMIN);

    List<AccessIdRepresentationModel> accessIds = ldapClient.searchPermissionsAccessIdHas(accessId);

    return ResponseEntity.ok(accessIds);
  }
}
