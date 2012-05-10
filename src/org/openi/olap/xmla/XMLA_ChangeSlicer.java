package org.openi.olap.xmla;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.tonbeller.jpivot.core.ExtensionSupport;
import com.tonbeller.jpivot.olap.mdxparse.Exp;
import com.tonbeller.jpivot.olap.mdxparse.FunCall;
import com.tonbeller.jpivot.olap.mdxparse.ParsedQuery;
import com.tonbeller.jpivot.olap.model.Axis;
import com.tonbeller.jpivot.olap.model.Member;
import com.tonbeller.jpivot.olap.model.OlapException;
import com.tonbeller.jpivot.olap.model.Position;
import com.tonbeller.jpivot.olap.model.Result;
import com.tonbeller.jpivot.olap.navi.ChangeSlicer;
import com.tonbeller.jpivot.olap.query.MDXElement;
import com.tonbeller.jpivot.util.ArrayUtil;

/**
 * change slicer extension
 * 
 * @author SUJEN
 * 
 */
public class XMLA_ChangeSlicer extends ExtensionSupport implements ChangeSlicer {

	static Logger logger = Logger.getLogger(XMLA_ChangeSlicer.class);

	/**
	 * Constructor sets ID
	 */
	public XMLA_ChangeSlicer() {
		super.setId(ChangeSlicer.ID);
	}

	/**
	 * @see com.tonbeller.jpivot.olap.navi.ChangeSlicer#getSlicer()
	 */
	public Member[] getSlicer() {

		XMLA_Model model = (XMLA_Model) getModel();
		// use result rather than query
		Result res = null;
		try {
			res = model.getResult();
		} catch (OlapException ex) {
			// do not handle
			return new Member[0];
		}

		Axis slicer = res.getSlicer();
		List positions = slicer.getPositions();
		List members = new ArrayList();
		for (Iterator iter = positions.iterator(); iter.hasNext();) {
			Position pos = (Position) iter.next();
			Member[] posMembers = pos.getMembers();
			for (int i = 0; i < posMembers.length; i++) {
				if (!members.contains(posMembers[i]))
					members.add(posMembers[i]);
			}
		}

		return (Member[]) members.toArray(new Member[0]);
	}

	/**
	 * @see com.tonbeller.jpivot.olap.navi.ChangeSlicer#setSlicer(Member[])
	 */
	public void setSlicer(Member[] members) {
		XMLA_Model model = (XMLA_Model) getModel();
		XMLA_QueryAdapter adapter = (XMLA_QueryAdapter) model.getQueryAdapter();
		ParsedQuery pq = adapter.getParsedQuery();

		boolean logInfo = logger.isInfoEnabled();

		if (members.length == 0) {
			// empty slicer
			pq.setSlicer(null); // ???
			if (logInfo)
				logger.info("slicer set to null");
		} else {
			Map<String, List<Member>> membersMap = new HashMap<String, List<Member>>();
			for (Member member : members) {
				String hierName = ((MDXElement) member.getLevel()
						.getHierarchy()).getUniqueName();
				List<Member> itsMembers = null;
				if (membersMap.containsKey(hierName)) {
					itsMembers = membersMap.get(hierName);
				} else {
					itsMembers = new ArrayList<Member>();
					membersMap.put(hierName, itsMembers);
				}
				itsMembers.add(member);
			}

			List<Exp> sets = new ArrayList<Exp>();
			for (List<Member> itsMembers : membersMap.values()) {
				FunCall f = new FunCall("{}",
						(Exp[]) ArrayUtil.naturalCast(itsMembers
								.toArray(new Exp[0])), FunCall.TypeBraces);
				sets.add(f);
			}

			Iterator setsItr = sets.iterator();
			FunCall slicerFunCall = null;
			
			if(sets.size() < 1) {
				slicerFunCall = new FunCall("()", (Exp[]) sets.toArray(new Exp[0]),
						FunCall.TypeParentheses);
			}
			else {
				while(setsItr.hasNext()) {
					FunCall f = (FunCall) setsItr.next();
					if(slicerFunCall == null)
						slicerFunCall = f;
					else {
						List<Exp> tempSets = new ArrayList<Exp>();
						tempSets.add(slicerFunCall);
						tempSets.add(f);
						slicerFunCall = new FunCall("Crossjoin", (Exp[]) tempSets.toArray(new Exp[0]), FunCall.TypeFunction);
					}
				}
			}
			pq.setSlicer(slicerFunCall);

		}

		model.fireModelChanged();
	}

} // End XMLA_ChangeSlicer
