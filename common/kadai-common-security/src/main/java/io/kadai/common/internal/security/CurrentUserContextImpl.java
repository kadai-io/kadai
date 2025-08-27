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

package io.kadai.common.internal.security;

import static java.util.function.Predicate.not;

import io.kadai.common.api.security.CurrentUserContext;
import io.kadai.common.api.security.GroupPrincipal;
import io.kadai.common.api.security.ProxyPrincipal;
import io.kadai.common.api.security.UserPrincipal;
import java.security.AccessController;
import java.security.Principal;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.security.auth.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CurrentUserContextImpl implements CurrentUserContext {

  private static final Logger LOGGER = LoggerFactory.getLogger(CurrentUserContextImpl.class);

  public CurrentUserContextImpl() {}

  @Override
  @SuppressWarnings("removal")
  public String getUserId() {
    // TODO replace with Subject.current() when migrating to newer Version than 17
    Subject subject = Subject.getSubject(AccessController.getContext());
    LOGGER.trace("Subject of caller: {}", subject);
    if (subject != null) {
      final Set<Principal> principals = subject.getPrincipals();
      LOGGER.trace("Public principals of caller: {}", principals);
      return principals.stream()
          .filter(not(GroupPrincipal.class::isInstance))
          .filter(UserPrincipal.class::isInstance)
          .map(Principal::getName)
          .map(CurrentUserContextImpl::convertAccessId)
          .findFirst()
          .orElse(null);
    }
    LOGGER.trace("No userId found in subject!");
    return null;
  }

  @Override
  @SuppressWarnings("removal")
  public String getProxyAccessId() {
    // TODO replace with Subject.current() when migrating to newer Version than 17
    Subject subject = Subject.getSubject(AccessController.getContext());
    LOGGER.trace("Subject of caller: {}", subject);
    if (subject != null) {
      final Set<Principal> principals = subject.getPrincipals();
      LOGGER.trace("Public principals of caller: {}", principals);
      return principals.stream()
          .filter(not(GroupPrincipal.class::isInstance))
          .filter(ProxyPrincipal.class::isInstance)
          .map(Principal::getName)
          .map(CurrentUserContextImpl::convertAccessId)
          .findFirst()
          .orElse(null);
    }
    LOGGER.trace("No proxyAccessId found in subject!");
    return null;
  }

  @Override
  @SuppressWarnings("removal")
  public List<String> getGroupIds() {
    // TODO replace with Subject.current() when migrating to newer Version than 17
    Subject subject = Subject.getSubject(AccessController.getContext());
    LOGGER.trace("Subject of caller: {}", subject);
    if (subject != null) {
      Set<GroupPrincipal> groups = subject.getPrincipals(GroupPrincipal.class);
      LOGGER.trace("Public groups of caller: {}", groups);
      return groups.stream()
          .map(Principal::getName)
          .filter(Objects::nonNull)
          .map(CurrentUserContextImpl::convertAccessId)
          .toList();
    }
    LOGGER.trace("No groupIds found in subject!");
    return Collections.emptyList();
  }

  @Override
  public List<String> getAccessIds() {
    Set<String> accessIds = new HashSet<>(getGroupIds());
    accessIds.add(getUserId());
    accessIds.add(getProxyAccessId());

    return accessIds.stream().toList();
  }

  private static String convertAccessId(String accessId) {
    String toReturn = accessId.toLowerCase();
    LOGGER.trace("Found AccessId '{}'. Returning AccessId '{}' ", accessId, toReturn);
    return toReturn;
  }
}
