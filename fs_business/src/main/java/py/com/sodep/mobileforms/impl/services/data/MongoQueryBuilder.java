package py.com.sodep.mobileforms.impl.services.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import py.com.sodep.mf.exchange.objects.data.ConditionalCriteria;
import py.com.sodep.mf.exchange.objects.data.Criteria;
import py.com.sodep.mf.exchange.objects.data.MFRestriction;
import py.com.sodep.mf.exchange.objects.data.MFRestriction.OPERATOR_MODIF;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;

public class MongoQueryBuilder {

	public static DBObject getQuery(ConditionalCriteria criterias) {

		BasicDBObjectBuilder qBuilder = BasicDBObjectBuilder.start();
		BasicDBObjectBuilder notInBuilder = BasicDBObjectBuilder.start();
		BasicDBObjectBuilder inBuilder = BasicDBObjectBuilder.start();
		
		
		if (criterias != null && criterias.getRestrictions().size() > 0) {
			ArrayList<? extends MFRestriction> restrictions = criterias.getRestrictions();

			// a map from fields to the restrition. mongo needs the constratint
			// of a fields on the same JSON field. Otherwise, it won't returned
			// the expected results. For example:
			// .find({"data.ID":{"$gt":2},"data.ID":{"$lt":4}}); won't return
			// the expected result, while the query:
			// find({"data.ID":{"$gt":2,"$lt":4}}); will do it
			HashMap<String, List<Criteria>> fieldsCriteria = new HashMap<String, List<Criteria>>();

			for (MFRestriction r : restrictions) {
				if (r instanceof ConditionalCriteria) {
					throw new RuntimeException("Multi level criteria is not yet supported");
				}
				if (r instanceof Criteria) {
					Criteria c = (Criteria) r;
					String field;
					if (c.getNamespace() != null &&
						!c.getField().equals(DataContainer._ID)) {	//this is for "_id" mongo field without namespace restriction
						field = c.getNamespace() + "." + c.getField();
					} else {
						field = c.getField();
					}
					if (c.getOp().equals(Criteria.OPERATOR.EQUALS)) {
						qBuilder.add(field, c.getValue());
					} else if (c.getOp().equals(Criteria.OPERATOR.NOT_IN)) {
						BasicDBObjectBuilder fieldOperator = BasicDBObjectBuilder.start();
						fieldOperator.add(getOperator(c), c.getValue());
						notInBuilder.add(field, fieldOperator.get());
					} else if (c.getOp().equals(Criteria.OPERATOR.IN)) {
						BasicDBObjectBuilder fieldOperator = BasicDBObjectBuilder.start();
						fieldOperator.add(getOperator(c), c.getValue());
						inBuilder.add(field, fieldOperator.get());
					}else if (c.getOp().equals(Criteria.OPERATOR.REGEX)) {
						String patternStr = null;
						if (c.getModificators().contains(OPERATOR_MODIF.ANYWHERE)) {
							patternStr = ".*" + c.getValue() + ".*";
						}
						if (c.getModificators().contains(OPERATOR_MODIF.BEGIN)) {
							patternStr = "^" + c.getValue();
						}
						if (c.getModificators().contains(OPERATOR_MODIF.END)) {
							patternStr = c.getValue() + "$";
						}
						Pattern pattern = null;
						if (c.getModificators().contains(OPERATOR_MODIF.CASE_INSENSITIVE)) {
							pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
						} else {
							pattern = Pattern.compile(patternStr);
						}
						qBuilder.add(field, pattern);
					} else {
						List<Criteria> fieldRestrictions = fieldsCriteria.get(field);
						if (fieldRestrictions == null) {
							fieldRestrictions = new ArrayList<Criteria>();
							fieldsCriteria.put(field, fieldRestrictions);
						}
						fieldRestrictions.add(c);
					}

				}
			}

			Set<String> fields = fieldsCriteria.keySet();
			for (String field : fields) {
				List<Criteria> fieldRestrictions = fieldsCriteria.get(field);
				BasicDBObjectBuilder fieldsOperator = BasicDBObjectBuilder.start();
				for (Criteria c : fieldRestrictions) {
					fieldsOperator.add(getOperator(c), c.getValue());
					// Add a restriction of the form { "field" : { $gt:
					// value } }
				}
				qBuilder.add(field, fieldsOperator.get());
			}

		}
		// only retrieve comitted data
		qBuilder.add(DataContainer.FIELD_TXSTATE, DataContainer.TX_STATE.CONFIRMED.ordinal());
		
		if (!notInBuilder.isEmpty()) {
			QueryBuilder secondBuilder = QueryBuilder.start();
			secondBuilder.and(qBuilder.get(), notInBuilder.get());
			return secondBuilder.get();
		} else if (!inBuilder.isEmpty()){
			if (!qBuilder.isEmpty()) {
				QueryBuilder secondBuilder = QueryBuilder.start();
				secondBuilder.and(qBuilder.get(), inBuilder.get());
				return secondBuilder.get();
			} else {
				return inBuilder.get();
			}
		} else {
			return qBuilder.get();
		}
		
		

	}

	/**
	 * Translate the operator of the criteria to a valid Mongo operator
	 */
	public static String getOperator(Criteria criteria) {
		Criteria.OPERATOR op = criteria.getOp();
		if (Criteria.OPERATOR.LT.equals(op)) {
			return "$lt";
		} else if (Criteria.OPERATOR.LTEQUAL.equals(op)) {
			return "$lte";
		} else if (Criteria.OPERATOR.GT.equals(op)) {
			return "$gt";
		} else if (Criteria.OPERATOR.GTEQUAL.equals(op)) {
			return "$gte";
		} else if (Criteria.OPERATOR.REGEX.equals(op)) {
			return "$regex";
		} else if (Criteria.OPERATOR.IN.equals(op)) {
			return "$in";
		} else if (Criteria.OPERATOR.NOT_EQUAL.equals(op)) {
			return "$ne";
		} else if (Criteria.OPERATOR.NOT_IN.equals(op)) {
			return "$nin";
		}
		return null;
	}
}
