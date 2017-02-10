package com.topsec.tsm.sim.auth.security;

import java.util.Set;

/**
 * 攻击检测
 * @author hp
 *
 */
public interface AttackChecker {

	public Attack findAttack(Object object,Set<String> uncheckProperties,String[] allows) ;
}
