package com.topsec.tsm.sim.node.dao;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.criterion.Restrictions;

import com.topsec.tsm.sim.common.dao.HibernateDaoImpl;
import com.topsec.tsm.sim.resource.persistence.SimNodeUpgrade;
import com.topsec.tsm.sim.util.FtpConfigUtil;

/*
 * 功能描述：dao层升级对象接口实现类
 */
public class SimNodeUpgradeDaoImpl extends HibernateDaoImpl<SimNodeUpgrade, Integer> implements SimNodeUpgradeDao {

	@SuppressWarnings("unchecked")
	@Override
	public List<SimNodeUpgrade> list() {
		return getSession().createQuery("FROM SimNodeUpgrade").list();
	}

	@Override
	public List<SimNodeUpgrade> getRecordList(final int pageNum, final int pageSize) {
		return getSession().createQuery(" FROM SimNodeUpgrade snu ORDER BY snu.createDate DESC ")// 按创建时间倒序
						   .setFirstResult((pageNum - 1) * pageSize)
						   .setMaxResults(pageSize)//
						   .list();//
	}

	@Override
	public long getRecordCount() {
		Number number = (Number) getSession().createQuery("SELECT COUNT(*) FROM SimNodeUpgrade").uniqueResult();//
		return number.longValue() ;
	}
	
	@Override
	public Serializable save(SimNodeUpgrade simNodeUpgrade) {
		return getSession().save(simNodeUpgrade);
	}

	@Override
	public void delete(SimNodeUpgrade simNodeUpgrade) {
		Object entity = getSession().get(SimNodeUpgrade.class, simNodeUpgrade.getId());
		getSession().delete(entity);
	}

	@Override
	public Map<String, String> getMaxVersionStrByType(final String type, String versionFrom) {
		// versionFrom格式示例3.1.129.008
		String[] versionNames = versionFrom.split("\\.");
		if (versionNames.length != 4) {// 版本格式不对
			return null;
		}
		final String _versionFrom = versionFrom.replace(".", "_");// 替换成目标格式示例3_1_129_008

		List<SimNodeUpgrade> simNodeUpgradeList;
		simNodeUpgradeList = getSession().createQuery(" FROM SimNodeUpgrade snu WHERE snu.nodeType=? AND snu.versionFrom=? ORDER BY snu.versionTo DESC ")//
										 .setParameter(0, type.toUpperCase())
										 .setParameter(1, _versionFrom)
										 .list();
		
		if (simNodeUpgradeList.size() > 0) {
			SimNodeUpgrade simNodeUpgrade = simNodeUpgradeList.get(0);// 最高版本

			Map<String, Object> args = FtpConfigUtil.getInstance().getFTPConfigByKey("patch");
			Map<String, String> versionRes = new HashMap<String, String>();

			// FTP相关
			int port = Integer.parseInt((String) args.get("port"));
			String host = (String) args.get("host");
			String user = (String) args.get("user");
			String password = (String) args.get("password");
			String home = simNodeUpgrade.getFilePath();
			String patch = simNodeUpgrade.getName();

			// 整合
			versionRes.put("port", String.valueOf(port));
			versionRes.put("host", host);
			versionRes.put("user", user);
			versionRes.put("password", password);
			versionRes.put("home", home);
			versionRes.put("patch", patch);

			return versionRes;
		} else {// 没有该版本的记录
			return null;
		}

	}

	@Override
	public SimNodeUpgrade getSimNodeUpgradeByName(final String fileName) {
		SimNodeUpgrade nodeUpgrade = findUniqueByCriteria(Restrictions.eq("name", fileName)) ;
		return nodeUpgrade ;
	}
}
