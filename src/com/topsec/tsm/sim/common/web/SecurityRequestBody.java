package com.topsec.tsm.sim.common.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.topsec.tsm.sim.auth.security.AttackChecker;
import com.topsec.tsm.sim.auth.security.CommonAttackChecker;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface SecurityRequestBody{
    Class<? extends AttackChecker> value() default CommonAttackChecker.class;
    String name() default "";
    String[] allows() default {};
    /**
     * 不需要进行安全检测的属性
     * @return
     */
    String[] uncheck() default {} ;
}
