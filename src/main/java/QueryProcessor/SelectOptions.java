package QueryProcessor;

import java.util.ArrayList;
import java.util.List;

public class SelectOptions {

	private List<String> columns = new ArrayList<>();
	private boolean all = false;
	private boolean where = false;
	private String tableName;
	private String columnName;
	private String columnValue;
	
	//
	
	public List<String> getColumns() {
		return columns;
	}
	public void setColumns(List<String> columns) {
		this.columns = columns;
	}
	public boolean isAll() {
		return all;
	}
	public void setAll(boolean all) {
		this.all = all;
	}
	public boolean isWhere() {
		return where;
	}
	public void setWhere(boolean where) {
		this.where = where;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public String getColumnValue() {
		return columnValue;
	}
	public void setColumnValue(String columnValue) {
		this.columnValue = columnValue;
	}
	@Override
	public String toString() {
		return "SelectOptions [columns=" + columns + ", all=" + all + ", where=" + where + ", tableName=" + tableName
				+ ", columnName=" + columnName + ", columnValue=" + columnValue + "]";
	}
	
	
	
	
}
