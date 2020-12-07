package org.imanity.frameworktest.mysql;

import org.imanity.framework.mysql.connection.file.H2ConnectionFactory;
import org.junit.Test;

import javax.persistence.Column;
import javax.persistence.Table;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.fail;

public class TestSelect {
	
	@Test
	public void test() {
		
		H2ConnectionFactory db = new H2ConnectionFactory(new File("./h2test").toPath().toAbsolutePath(), true);
		
		db.query().sql("drop table if exists selecttest").execute();
		
		db.createTable(Row.class);
		
		Row row = new Row();
		row.id = 99;
		row.name = "bob";
		db.insert(row);
		
		// primitive
		Long myId = db.query().sql("select id from selecttest").first(Long.class);
		if (myId != 99) {
			fail();
		}
		
		// map
		Map myMap = db.table("selecttest").first(LinkedHashMap.class);
		String str = myMap.toString();
		if (!str.equalsIgnoreCase("{id=99, name=bob}")) {
			System.out.println(str);
			fail();
		}
		
		// pojo
		Row myRow = db.first(Row.class);
		String myRowStr = myRow.toString();
		if (!myRowStr.equals("99bob")) {
			System.out.println(myRowStr);
			fail();
		}
		
	}
	
	@Table(name="selecttest")
	public static class Row {
		@Column(unique=true)
		public long id;
		public String name; 
		public String toString() {
			return id + name;
		}
	}

}
