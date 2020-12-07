package org.imanity.framework.mysql;

@SuppressWarnings("serial")

/**
 * Generic unchecked database exception. 
 */
public class ImanitySqlException extends RuntimeException {
	
	private String sql;
	
	public ImanitySqlException() {}
	
	public ImanitySqlException(String msg) {
		super(msg);
	}

	public ImanitySqlException(Throwable t) {
		super(t);
	}

	public ImanitySqlException(String msg, Throwable t) {
		super(msg, t);
	}
	
	public void setSql(String sql) {
		this.sql = sql;
	}
	
	public String getSql() {
		return sql;
	}
	
}
