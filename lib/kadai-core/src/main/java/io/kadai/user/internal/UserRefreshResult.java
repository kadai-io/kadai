package io.kadai.user.internal;

/** Counts emitted by a differential user refresh. */
public record UserRefreshResult(
    int inputUsers,
    int acceptedUsers,
    int rejectedUsers,
    int insertedUsers,
    int updatedUsers,
    int removedUsers,
    int unchangedUsers,
    int addedGroups,
    int removedGroups,
    int addedPermissions,
    int removedPermissions,
    int orphanGroupsRemoved,
    int orphanPermissionsRemoved) {}
