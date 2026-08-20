package io.kadai.common.rest.ldap;

import io.kadai.user.api.models.User;
import java.util.List;

/** A complete LDAP user-role result suitable for authoritative reconciliation. */
public record LdapUserSnapshot(List<User> users, int pageCount, int resultCount) {}
