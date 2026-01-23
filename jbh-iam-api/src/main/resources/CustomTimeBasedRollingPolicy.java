package com.jbh.iam.api.config.logback;

import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;

public class CustomTimeBasedRollingPolicy extends TimeBasedRollingPolicy {

  @Override
  public String getFileNamePattern() {
    String filePattern = "%d.%i." + super.getFileNamePattern();
    System.out.println("File Pattern: " + filePattern);
    return filePattern;
  }
}
