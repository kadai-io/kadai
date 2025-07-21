package io.kadai.common.api.security;

import java.util.Optional;

public interface UserContext {

  default String getUserId() {
    return getPuppet();
  }

  String getPuppet();

  Optional<String> getPuppeteer();
}
