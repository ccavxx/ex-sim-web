package com.topsec.tsm.sim.common.web;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface NotCheck {
	String[] properties();
	String[] allows() default {};
}
