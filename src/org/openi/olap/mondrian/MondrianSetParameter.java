package org.openi.olap.mondrian;

import java.util.Hashtable;
import java.util.Map;

import mondrian.olap.Category;
import mondrian.olap.type.TypeUtil;

import org.apache.log4j.Logger;

import com.tonbeller.jpivot.core.ExtensionSupport;
import com.tonbeller.jpivot.olap.model.DoubleExpr;
import com.tonbeller.jpivot.olap.model.Expression;
import com.tonbeller.jpivot.olap.model.IntegerExpr;
import com.tonbeller.jpivot.olap.model.StringExpr;
import com.tonbeller.jpivot.olap.navi.SetParameter;

/**
 * set parameter to query
 */
public class MondrianSetParameter extends ExtensionSupport implements
		SetParameter {

	static Logger logger = Logger.getLogger(MondrianSetParameter.class);

	/**
   */
	public MondrianSetParameter() {
		super.setId(SetParameter.ID);
	}

	/**
	 * set parameter value to mondrian query
	 * 
	 * @see SetParameter#setParameter(String, Expression)
	 */
	public void setParameter(String paramName, Expression expr) {
		MondrianModel model = (MondrianModel) getModel();
		mondrian.olap.Query monQuery = ((MondrianQueryAdapter) model
				.getQueryAdapter()).getMonQuery();
		mondrian.olap.Parameter[] monParams = monQuery.getParameters();
		for (int i = 0; i < monParams.length; i++) {
			mondrian.olap.Parameter monParam = monParams[i];
			int pType = TypeUtil.typeToCategory(monParam.getType());
			String monParaName = monParam.getName();
			if (paramName.equals(monParaName)) {

				// found the parameter with the given name in the query
				switch (pType) {
				case Category.Numeric:
					if (expr instanceof DoubleExpr) {
						double d = ((DoubleExpr) expr).getValue();
						monParam.setValue(new Double(d));
					} else if (expr instanceof IntegerExpr) {
						int ii = ((IntegerExpr) expr).getValue();
						monParam.setValue(new Double(ii));
					} else {
						// wrong parameter type
						String str = "wrong Numeric parameter type "
								+ paramName + expr.getClass().toString();
						logger.error(str);
						throw new java.lang.IllegalArgumentException(str);
					}
					break;

				case Category.String:
					if (expr instanceof StringExpr) {
						String s = ((StringExpr) expr).getValue();
						monParam.setValue(s);
					} else {
						// wrong parameter type
						String str = "wrong String parameter type " + paramName
								+ expr.getClass().toString();
						logger.error(str);
						throw new java.lang.IllegalArgumentException(str);
					}

					break;

				case Category.Member:
					if (expr instanceof MondrianMember) {
						MondrianMember m = (MondrianMember) expr;
						monParam.setValue(m.getMonMember());
					} else {
						// wrong parameter type
						String str = "wrong Member parameter type " + paramName
								+ expr.getClass().toString();
						logger.error(str);
						throw new java.lang.IllegalArgumentException(str);
					}
					break;
				default:
				}
				model.fireModelChanged();
				return;
			}
		}
	}

	/**
	 * FIXME - this crashes if the parameter contains an expression like
	 * "LastChild" or "DefaultMember".
	 * 
	 * @return Map containing parameter names (= keys) and strings to display
	 *         value (= value)
	 * @see com.tonbeller.jpivot.olap.navi.SetParameter#getDisplayValues()
	 */
	public Map getDisplayValues() {
		Map map = new Hashtable();
		MondrianModel model = (MondrianModel) getModel();
		mondrian.olap.Query monQuery = ((MondrianQueryAdapter) model
				.getQueryAdapter()).getMonQuery();
		mondrian.olap.Parameter[] monParams = monQuery.getParameters();
		for (int i = 0; i < monParams.length; i++) {
			mondrian.olap.Parameter monParam = monParams[i];
			int pType = TypeUtil.typeToCategory(monParam.getType());
			String monParaName = monParam.getName();
			Object value = monParam.getValue();
			switch (pType) {
			case Category.Numeric:
				map.put(monParaName, value.toString());
				break;
			case Category.String:
				map.put(monParaName, value);
				break;
			case Category.Member:
				map.put(monParaName,
						((mondrian.olap.Member) value).getCaption());
				break;
			}
		}

		return map;
	}

	public String[] getParameterNames() {
		MondrianModel model = (MondrianModel) getModel();
		mondrian.olap.Query monQuery = ((MondrianQueryAdapter) model
				.getQueryAdapter()).getMonQuery();
		mondrian.olap.Parameter[] monParams = monQuery.getParameters();
		String[] names = new String[monParams.length];
		for (int i = 0; i < monParams.length; i++) {
			names[i] = monParams[i].getName();
		}
		return names;
	}

	/**
	 * Returns true if the query has one or more parameters. This does not
	 * evaluate the parameters.
	 * 
	 * @return
	 */
	public boolean getHasDisplayValues() {
		MondrianModel model = (MondrianModel) getModel();
		mondrian.olap.Query monQuery = ((MondrianQueryAdapter) model
				.getQueryAdapter()).getMonQuery();
		mondrian.olap.Parameter[] monParams = monQuery.getParameters();
		return (monParams.length > 0);
	}

} // MondrianSetParameter
