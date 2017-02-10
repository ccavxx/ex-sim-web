package com.topsec.tsm.sim.node.dao;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.topsec.tsm.sim.common.dao.HibernateDaoImpl;
import com.topsec.tsm.sim.resource.persistence.SimRuleUpgrade;
import com.topsec.tsm.sim.util.FtpConfigUtil;

/*
 * 功能描述：dao层事件规则库升级对象接口实现类
 */
public class SimRuleUpgradeDaoImpl extends HibernateDaoImpl<SimRuleUpgrade, Integer> implements SimRuleUpgradeDao {

	@SuppressWarnings("unchecked")
	@Override
	public List<SimRuleUpgrade> list() {
		return findByCriteria() ;
	}

	@Override
	public List<SimRuleUpgrade> getRecordList(final int pageNum, final int pageSize) {
		Criteria cri = createCriteria() ;
		cri.addOrder(Order.desc("createTime")) ;
		return cri.list() ;
	}

	@Override
	public Long getRecordCount() {
		Number number = (Number) getSession().createQuery("SELECT COUNT(*) FROM SimRuleUpgrade").uniqueResult();
		return number.longValue() ;
	}

	@Override
	public Map<String, String> getMaxVersion() {
		List<SimRuleUpgrade> ruleList = getSession().createQuery(" FROM SimRuleUpgrade sru ORDER BY sru.name DESC ").list();
		if (ruleList.size() > 0) {
			SimRuleUpgrade simRuleUpgrade = ruleList.get(0);// 最高版本

			Map<String, Object> args = FtpConfigUtil.getInstance().getFTPConfigByKey("patch");
			Map<String, String> versionRes = new HashMap<String, String>();

			// FTP相关
			int port = Integer.parseInt((String) args.get("port"));
			String host = (String) args.get("host");
			String user = (String) args.get("user");
			String password = (String) args.get("password");
			String encoding = (String) args.get("encoding");
			String home = simRuleUpgrade.getFilePath();
			String patch = simRuleUpgrade.getName();

			// 整合
			versionRes.put("port", String.valueOf(port));
			versionRes.put("host", host);
			versionRes.put("user", user);
			versionRes.put("password", password);
			versionRes.put("home", home);
			versionRes.put("patch", patch);
			versionRes.put("encoding", encoding);

			return versionRes;
		} else {// 没有该版本的记录
			return null;
		}

	}

	@Override
	public SimRuleUpgrade getSimRuleUpgradeByName(final String fileName) {
		return findUniqueByCriteria(Restrictions.eq("name", fileName)) ;
	}
}
