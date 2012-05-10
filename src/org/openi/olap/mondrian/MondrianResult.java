package org.openi.olap.mondrian;

import org.apache.log4j.Logger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.tonbeller.jpivot.olap.model.Axis;
import com.tonbeller.jpivot.olap.model.impl.FormatStringParser;
import com.tonbeller.jpivot.olap.query.ResultBase;
import mondrian.olap.Position;
import mondrian.olap.Query;
import mondrian.olap.Member;
import mondrian.olap.ResultLimitExceededException;
import mondrian.olap.ResourceLimitExceededException;

/**
 * Result implementation for Mondrian
 */
public class MondrianResult extends ResultBase {
	static Logger logger = Logger.getLogger(MondrianResult.class);

	private mondrian.olap.Result monResult = null;
	private int[] posize;
	private FormatStringParser formatStringParser = new FormatStringParser();

	/**
	 * Constructor
	 * 
	 * @param model
	 *            the associated MondrianModel
	 */
	protected MondrianResult(mondrian.olap.Result monResult, MondrianModel model)
			throws ResultLimitExceededException {
		super(model);
		this.monResult = monResult;

		initData();
	}

	/**
	 * initData creates all the wrapper objects
	 */
	private void initData() throws ResultLimitExceededException {
		final int cellCountLimit = Integer
				.getInteger(MondrianModel.CELL_LIMIT_PROP,
						MondrianModel.CELL_LIMIT_DEFAULT).intValue();

		MondrianModel mmodel = (MondrianModel) model;

		mondrian.olap.Axis[] monAxes = monResult.getAxes();
		// first step: walk through axes and add the members to the model
		int nCells = 1;
		posize = new int[monAxes.length];
		for (int i = 0; i < monAxes.length; i++) {
			List monPositions = monAxes[i].getPositions();
			int size = 0;
			int nosPositions = 0;
			int nosMembers = 0;
			Iterator pit = monPositions.iterator();
			// For the first Position, record how many Members there are.
			if (pit.hasNext()) {
				nosPositions++;
				Position position = (Position) pit.next();
				Iterator mit = position.iterator();
				while (mit.hasNext()) {
					nosMembers++;
					mmodel.addMember((Member) mit.next());
				}
				size++;
			}
			while (pit.hasNext()) {
				nosPositions++;
				Position position = (Position) pit.next();
				Iterator mit = position.iterator();
				while (mit.hasNext()) {
					mmodel.addMember((Member) mit.next());
				}
				size++;
			}

			// If there is no data on a particular axis, the table might display
			// but
			// the axis with no data will not display. Further manipulation on
			// the
			// table may result in NullPointerException since JPivot expects
			// that
			// there at least be meta-data associated with every query on all
			// axes - hence we throw a controlled exception.

			// removed by av: see EmptyResultTest - thats a perfect MDX query
			// that should run w/o errors.
			// if ((nosPositions == 0) || (nosMembers == 0)) {
			// Query query = monResult.getQuery();
			// String mdx = query.toMdx();
			// throw new NoValidMemberException(i, mdx, nosPositions,
			// nosMembers);
			// }

			// check for OutOfMemory
			mmodel.checkListener();

			posize[i] = size;
			nCells = nCells * size;
		}
		mondrian.olap.Axis monSlicer = monResult.getSlicerAxis();
		List monPositions = monSlicer.getPositions();
		Iterator pit = monPositions.iterator();
		while (pit.hasNext()) {
			Position position = (Position) pit.next();
			Iterator mit = position.iterator();
			while (mit.hasNext()) {
				mmodel.addMember((Member) mit.next());
			}
			// check for OutOfMemory
			mmodel.checkListener();
		}

		if (logger.isDebugEnabled()) {
			StringBuffer buf = new StringBuffer();
			buf.append("initData: nCells=");
			buf.append(nCells);
			logger.debug(buf.toString());
		}

		// If we are limiting the number of cells, then we do not need to
		// read them all. Rather, the number read is upto the limit plus
		// enough to finish the slice. As an example, if there are two
		// dimisions, rows and columns, then enough cells are read in
		// so that each row is complete (all columns read) but if the
		// number read in is greater than the limit, then no further rows
		// (cells) are read in.
		if ((cellCountLimit > 0) && (cellCountLimit < nCells)) {
			// How big is a slice (do not include last axis)
			int sliceSize = 1;
			for (int i = 0; i < monAxes.length - 1; i++) {
				sliceSize *= posize[i];
			}
			if (logger.isDebugEnabled()) {
				StringBuffer buf = new StringBuffer();
				buf.append("initData: sliceSize=");
				buf.append(sliceSize);
				buf.append(", cellCountLimit=");
				buf.append(cellCountLimit);
				logger.debug(buf.toString());
			}
			if (sliceSize > cellCountLimit) {
				// One slice is bigger than can be displayed, Arrg....
				StringBuffer buf = new StringBuffer(100);
				buf.append("Can not display a single slice, exceeded cell limit(");
				buf.append(cellCountLimit);
				buf.append(") for mdx: ");
				buf.append(((MondrianQueryAdapter) mmodel.getQueryAdapter())
						.getMonQuery().toString());
				throw new ResourceLimitExceededException(buf.toString());
			}

			// So, how many slices should be read in
			// There is no reason to read cell in that will not be displayed;
			// this serves as a memory usage limit.
			int n = (cellCountLimit / sliceSize) + 1;
			nCells = n * sliceSize;

			if (logger.isDebugEnabled()) {
				StringBuffer buf = new StringBuffer();
				buf.append("initData: cell limit adjusted nCells=");
				buf.append(nCells);
				logger.debug(buf.toString());
			}
		}

		// second step: create the result data
		axesList = new ArrayList();
		for (int i = 0; i < monAxes.length; i++) {
			axesList.add(new MondrianAxis(i, monAxes[i], mmodel));
			// check for OutOfMemory
			mmodel.checkListener();
		}
		slicer = new MondrianAxis(-1, monSlicer, mmodel);

		int[] iar = new int[monAxes.length];
		for (int i = 0; i < monAxes.length; i++) {
			iar[i] = 0;
		}
		for (int i = 0; i < nCells; i++) {
			mondrian.olap.Cell monCell = monResult.getCell(iar);
			MondrianCell cell = new MondrianCell(monCell, mmodel);
			cell.setFormattedValue(monCell.getFormattedValue(),
					formatStringParser);
			aCells.add(cell);
			if (nCells > 1) {
				// not for 0-dimensional case
				increment(iar);
			}

			// check for OutOfMemory every 1000 cells created
			if (i % 1000 == 0) {

				// According to Java5 memory monitor are we close to running
				// out of memory.
				mmodel.checkListener();
			}
		}

	}

	/**
	 * increment int array according to size of axis positions first index
	 * changes fastest (0,0), (1,0) ... (NX-1, 0) (0,1), (1,1) ... (NX-1, 1)
	 */
	private void increment(int[] iar) {
		int nn = ++iar[0];
		// done for the 1-dimensional case
		if (iar.length > 1 && nn >= posize[0]) {
			iar[0] = 0;
			for (int i = 1; i < iar.length; i++) {
				int kk = ++iar[i];
				if (kk < posize[i])
					break;
				else
					iar[i] = 0;
			}
		}
	}

	/**
	 * Returns the axes.
	 * 
	 * @return Axis[]
	 */
	public Axis[] getAxes() {
		if (monResult == null)
			return null; // todo error handling
		return (Axis[]) axesList.toArray(new MondrianAxis[0]);
	}

} // MondrianResult
