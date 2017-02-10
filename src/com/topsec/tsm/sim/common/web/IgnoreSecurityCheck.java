package com.topsec.tsm.sim.common.web;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 忽略安全检测的注解
 * @author hp
 *
 */

@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreSecurityCheck {
}
