package Logger;

public class MainLogger {

	private String activeDatabase;
	private String command;
	private Long currentTimeMillis;
	private Long executionTimeMillis;
	private Integer totalRecords;
	private Integer totalTables;
	private String changeMessage;
	private LogType logType;
	private String tableName;
	private String userName;
	
	//
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public LogType getLogType() {
		return logType;
	}
	public void setLogType(LogType logType) {
		this.logType = logType;
	}
	public Integer getTotalRecords() {
		return totalRecords;
	}
	public void setTotalRecords(Integer totalRecords) {
		this.totalRecords = totalRecords;
	}
	public Integer getTotalTables() {
		return totalTables;
	}
	public void setTotalTables(Integer totalTables) {
		this.totalTables = totalTables;
	}
	public String getChangeMessage() {
		return changeMessage;
	}
	public void setChangeMessage(String changeMessage) {
		this.changeMessage = changeMessage;
	}
	public String getActiveDatabase() {
		return activeDatabase;
	}
	public void setActiveDatabase(String activeDatabase) {
		this.activeDatabase = activeDatabase;
	}
	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}
	public Long getCurrentTimeMillis() {
		return currentTimeMillis;
	}
	public void setCurrentTimeMillis(Long currentTimeMillis) {
		this.currentTimeMillis = currentTimeMillis;
	}
	public Long getExecutionTimeMillis() {
		return executionTimeMillis;
	}
	public void setExecutionTimeMillis(Long executionTimeMillis) {
		this.executionTimeMillis = executionTimeMillis;
	}
	@Override
	public String toString() {
		return "MainLogger [activeDatabase=" + activeDatabase + ", command=" + command + ", currentTimeMillis="
				+ currentTimeMillis + ", executionTimeMillis=" + executionTimeMillis + ", totalRecords=" + totalRecords
				+ ", totalTables=" + totalTables + ", changeMessage=" + changeMessage + ", logType=" + logType
				+ ", tableName=" + tableName + ", userName=" + userName + "]";
	}
	
	
	
	
}
