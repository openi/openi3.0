package org.openi.olap.mondrian;

import javax.sql.DataSource;

import mondrian.rolap.RolapMember;

import org.apache.log4j.Logger;

import com.tonbeller.jpivot.core.ExtensionSupport;
import com.tonbeller.jpivot.olap.model.Member;
import com.tonbeller.jpivot.olap.navi.ExpressionParser;
import com.tonbeller.jpivot.param.SqlAccess;
import com.tonbeller.wcf.param.SessionParam;

/**
 * @author av
 * @author SUJEN
 */
public class MondrianSqlAccess extends ExtensionSupport implements SqlAccess {
	private static Logger logger = Logger.getLogger(MondrianSqlAccess.class);

	public DataSource getDataSource() {
		MondrianModel mm = (MondrianModel) getModel();
		return mm.getSqlDataSource();
	}

	public SessionParam createParameter(Member member, String paramName) {
		MondrianMember mm = (MondrianMember) member;
		RolapMember rm = (RolapMember) mm.getMonMember();
		paramName = checkParamName(paramName, rm);
		// All or calculated?
		if (rm.getKey() == null) {
			return null;
		}
		SessionParam p = new SessionParam();
		p.setSqlValue(rm.getKey());
		p.setDisplayName(member.getLevel().getLabel());
		p.setDisplayValue(member.getLabel());

		ExpressionParser parser = (ExpressionParser) getModel().getExtension(
				ExpressionParser.ID);
		if (parser != null)
			p.setMdxValue(parser.unparse(member));

		p.setName(paramName);
		return p;
	}

	private String checkParamName(String paramName, RolapMember rm) {
		if (paramName != null)
			return paramName;
		return MondrianUtil.defaultParamName(rm);
	}

	public SessionParam createParameter(Member member, String paramName,
			String propertyName) {
		MondrianMember mm = (MondrianMember) member;
		RolapMember rm = (RolapMember) mm.getMonMember();
		paramName = checkParamName(paramName, rm);
		// propertyValue may be null
		Object propertyValue = rm.getPropertyValue(propertyName);
		SessionParam p = new SessionParam();
		p.setSqlValue(propertyValue);
		p.setDisplayName(member.getLevel().getLabel());
		p.setDisplayValue(member.getLabel());

		ExpressionParser parser = (ExpressionParser) getModel().getExtension(
				ExpressionParser.ID);
		if (parser != null)
			p.setMdxValue(parser.unparse(member));
		p.setName(paramName);
		return p;
	}

}