package org.imanity.framework.mysql.pojo.statement;

import org.imanity.framework.mysql.pojo.Property;
import org.imanity.framework.mysql.pojo.info.StandardPojoInfo;

import javax.persistence.Column;

public class PostgresStatementBuilder extends StandardSqlStatementBuilder {

	@Override
	public String getCreateTableSql(Class<?> clazz) {
		
		StringBuilder buf = new StringBuilder();

		StandardPojoInfo pojoInfo = getPojoInfo(clazz);
		buf.append("create table if not exists ");
		buf.append(pojoInfo.getTable());
		buf.append(" (");
		
		boolean needsComma = false;
		for (Property prop : pojoInfo.getPropertyMap().values()) {
			
			if (needsComma) {
				buf.append(',');
			}
			needsComma = true;

			Column columnAnnot = prop.getColumnAnnotation();
			if (columnAnnot == null) {
	
				buf.append(prop.getName());
				buf.append(" ");
				if (prop.isGenerated()) {
					buf.append(" serial");
				} else {
					buf.append(getColType(prop.getDataType(), 255, 10, 2));
				}
				
			} else {
				if (columnAnnot.columnDefinition() == null) {
					
					// let the column def override everything
					buf.append(columnAnnot.columnDefinition());
					
				} else {

					buf.append(prop.getName());
					buf.append(" ");
					if (prop.isGenerated()) {
						buf.append(" serial");
					} else {
						buf.append(getColType(prop.getDataType(), columnAnnot.length(), columnAnnot.precision(), columnAnnot.scale()));
					}
					
					if (columnAnnot.unique()) {
						buf.append(" unique");
					}
					
					if (!columnAnnot.nullable()) {
						buf.append(" not null");
					}
				}
			}
		}
		
		if (pojoInfo.getPrimaryKeyName() != null) {
			buf.append(", primary key (");
			buf.append(pojoInfo.getPrimaryKeyName());
			buf.append(")");
		}
		
		buf.append(")");
		
		return buf.toString();
	}


}
