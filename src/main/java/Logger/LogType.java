package Logger;

public enum LogType {

	INSERT("INSERT"),
	CREATE("CREATE"),
	UPDATE("UPDATE"),
	DELETE("DELETE"),
	SELECT("SELECT"),
	USE("USE"),
	ERROR("ERROR"),
	INVALID("INVALID"),
	TRANSACTIONSTART("TSTART"),
	TRANSACTIONEND("TEND");
	
	private String logType;
	
	LogType(String logType) {
		this.logType=logType;
	}

	public String getLogType() {
		return logType;
	}

	public void setLogType(String logType) {
		this.logType = logType;
	}
	
	
}
