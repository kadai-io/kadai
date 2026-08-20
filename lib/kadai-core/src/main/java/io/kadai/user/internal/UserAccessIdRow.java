package io.kadai.user.internal;

/** A flat membership row used by the refresh snapshot mapper. */
public class UserAccessIdRow {
  private String userId;
  private String accessId;

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getAccessId() {
    return accessId;
  }

  public void setAccessId(String accessId) {
    this.accessId = accessId;
  }
}
