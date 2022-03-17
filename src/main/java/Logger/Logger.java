package Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Formatter;

public class Logger {

	private final static String generalLogsFilePath = "general-logs.txt";
	private final static String eventLogsFilePath = "event-logs.txt";
	private final static String queryLogsFilePath = "query-logs.txt";
	private final static Integer maxLogFileSize = 0;
	
	//
	
	public final static String databaseCreationChangeMessage = "Database %s created sucessfully ";
	public final static String databaseMetaErrorChangeMessage = "Database %s created sucessfully, Meta file creation failed ";
	public final static String databaseCreationErrorChangeMessage = "Database creation failed ";
	
	
	public Logger() {
		checkIfLogFileExists();
	}
	
	public static void processLogs(MainLogger logger) throws IOException {
		File generalLogs = new File(generalLogsFilePath);
		File eventLogs = new File(eventLogsFilePath);
		File queryLogs = new File(queryLogsFilePath);
		checkIfLogFileExists();
		processGeneralLogs(logger,generalLogs);
		processQueryLogs(logger, queryLogs);
		processEventLogs(logger, eventLogs);
	}
	
	private static void processGeneralLogs(MainLogger logger, File generalLogs) throws IOException {
		Formatter formatter = new Formatter();
		if(logger.getUserName()!=null && !(logger.getUserName().isBlank())) {
			formatter.format("%s|%s|", "user",logger.getUserName());
		}
		if(logger.getActiveDatabase()!=null && !(logger.getActiveDatabase().isBlank())) {
			formatter.format("%s|%s|","database",logger.getActiveDatabase());
		}
		if(logger.getTableName()!=null) {
			formatter.format("%s|%s|","table",logger.getTableName());
		}
		if(logger.getTotalTables()!=null) {
			formatter.format("%s|%s|", "Total Tables",logger.getTotalTables());
		}
		if(logger.getTotalRecords()!=null) {
			formatter.format("%s|%s|", "Total Records",logger.getTotalRecords());
		}
		if(logger.getCommand()!=null && !(logger.getCommand().isBlank())) {
			formatter.format("%s|%s|","query",logger.getCommand());
		}
		if(logger.getLogType()!=null) {
		formatter.format("%s|%s|", "type",logger.getLogType());
		}
		formatter.format("%s|%s%s|","start_time",logger.getCurrentTimeMillis(),"ms");
		formatter.format("%s|%s%s","execution_time",logger.getExecutionTimeMillis(),"ms");
		formatter.format("%s", "\n");
		Files.write(generalLogs.toPath(), formatter.toString().getBytes(), StandardOpenOption.APPEND);
	}
	
	private static void processQueryLogs(MainLogger logger, File queryLogs) throws IOException {
		Formatter formatter = new Formatter();
		if(logger.getUserName()!=null) {
			formatter.format("%s|%s|", "user",logger.getUserName());
		}
		if(logger.getActiveDatabase()!=null) {
			formatter.format("%s|%s|","database",logger.getActiveDatabase());
		}
		if(logger.getTableName()!=null) {
			formatter.format("%s|%s|","table",logger.getTableName());
		}
		if(logger.getCommand()!=null) {
			formatter.format("%s|%s|","query",logger.getCommand());
		}
		formatter.format("%s|%s%s|","start_time",logger.getCurrentTimeMillis(),"ms");
		formatter.format("%s|%s%s","execution_time",logger.getExecutionTimeMillis(),"ms");
		formatter.format("%s", "\n");
		Files.write(queryLogs.toPath(), formatter.toString().getBytes(), StandardOpenOption.APPEND);
		}
	
	private static void processEventLogs(MainLogger logger, File eventLogs) throws IOException {
		Formatter formatter = new Formatter();
		if(logger.getActiveDatabase()!=null) {
			formatter.format("%s|%s|", "database",logger.getActiveDatabase());
		}
		if(logger.getTableName()!=null) {
			formatter.format("%s|%s|","table",logger.getTableName());
		}
		if(logger.getChangeMessage()!=null) {
		formatter.format("%s|%s|", "message",logger.getChangeMessage());
		}
		if(logger.getLogType()!=null) {
		formatter.format("%s|%s", "type",logger.getLogType());
		}
		formatter.format("%s", "\n");
		Files.write(eventLogs.toPath(), formatter.toString().getBytes(), StandardOpenOption.APPEND);
	}
	
	private static void checkIfLogFileExists() {
	
		try {
			File generalLogs = new File(generalLogsFilePath);
			File eventLogs = new File(eventLogsFilePath);
			File queryLogs = new File(queryLogsFilePath);
			if(!generalLogs.exists()) {
				generalLogs.createNewFile();
			}
			if(!eventLogs.exists()) {
				eventLogs.createNewFile();
			}
			if(!queryLogs.exists()) {
				queryLogs.createNewFile();
			}
		} catch (Exception e) {
			System.err.println("Error in creating log files");
		}
	}
	
	public static void log(MainLogger logger) throws IOException {
		processLogs(logger);
	}

}
