package QueryProcessor;

public class UpdateOptions {

	private String tableName;
	private String columnName;
	private String columnValue;
	private boolean isReferenced;
	private String targetColumnName;
	private String targetColumnValue;
	
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
	public boolean isReferenced() {
		return isReferenced;
	}
	public void setReferenced(boolean isReferenced) {
		this.isReferenced = isReferenced;
	}
	public String getTargetColumnName() {
		return targetColumnName;
	}
	public void setTargetColumnName(String targetColumnName) {
		this.targetColumnName = targetColumnName;
	}
	public String getTargetColumnValue() {
		return targetColumnValue;
	}
	public void setTargetColumnValue(String targetColumnValue) {
		this.targetColumnValue = targetColumnValue;
	}
	
	
}
