package io.kadai.common.internal.security;

import io.kadai.common.api.security.UserContext;
import java.util.Optional;

public class UserContextImpl implements UserContext {

  private final String puppet;
  private final String puppeteer;

  public UserContextImpl(String puppet, String puppeteer) {
    this.puppet = puppet;
    this.puppeteer = puppeteer;
  }

  public UserContextImpl(String puppet) {
    this.puppet = puppet;
    this.puppeteer = null;
  }

  @Override
  public String getPuppet() {
    return this.puppet;
  }

  @Override
  public Optional<String> getPuppeteer() {
    return Optional.ofNullable(this.puppeteer);
  }
}
