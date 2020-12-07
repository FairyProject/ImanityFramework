package org.imanity.framework.mysql.pojo.statement;


import org.imanity.framework.mysql.pojo.Query;
import org.imanity.framework.mysql.pojo.info.StandardPojoInfo;

public class MySqlStatementBuilder extends StandardSqlStatementBuilder {

	@Override
	public String getUpsertSql(Query query, Object row) {
		StandardPojoInfo pojoInfo = getPojoInfo(row.getClass());
		return pojoInfo.getUpsertSql();
	}

	@Override
	public Object[] getUpsertArgs(Query query, Object row) {
		
		// same args as insert, but we need to duplicate the values
		Object [] args = super.getInsertArgs(query, row);
		
		int count = args.length;
		
		Object [] upsertArgs = new Object[count * 2];
		System.arraycopy(args, 0, upsertArgs, 0, count);
		System.arraycopy(args, 0, upsertArgs, count, count);
		
		return upsertArgs;
	}
	

	@Override
	public void makeUpsertSql(StandardPojoInfo pojoInfo) {

		// INSERT INTO table (a,b,c) VALUES (1,2,3) ON DUPLICATE KEY UPDATE c=c+1;
		
		// mostly the same as the makeInsertSql code
		// it uses the same column names and argcount

		StringBuilder buf = new StringBuilder();
		buf.append(pojoInfo.getInsertSql());
		buf.append(" on duplicate key update ");
		
		boolean first = true;
		for (String colName: pojoInfo.getInsertColumnNames()) {
			if (first) {
				first = false;
			} else {
				buf.append(',');
			}
			buf.append(colName);
			buf.append("=?");
		}
		
		pojoInfo.setUpsertSql(buf.toString());
	}

	@Override
	protected String getColType(Class<?> dataType, int length, int precision, int scale) {
		String colType;

		if (dataType.equals(Boolean.class) || dataType.equals(boolean.class)) {
			colType = "tinyint";
		} else {
			colType = super.getColType(dataType, length, precision, scale);
		}
		return colType;
	}

	@Override
	public Object convertValue(Object value, String columnTypeName) {
		if ("TINYINT".equalsIgnoreCase(columnTypeName)) {
			value = (int) value == 1;
		}

		return value;
	}

}
