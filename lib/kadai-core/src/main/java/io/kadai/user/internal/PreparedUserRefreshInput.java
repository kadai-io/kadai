package io.kadai.user.internal;

import java.util.Map;

/** Validated authoritative source for one refresh generation. */
public record PreparedUserRefreshInput(
    Map<String, UserRefreshState> usersById,
    int inputUsers,
    int acceptedUsers,
    int rejectedUsers) {}
