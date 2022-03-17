package Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LogReader {

	private final static String generalLogsFilePath = "general-logs.txt";
	
	public MainLogger mainLogger;
	
	public static void main(String[] args) throws IOException {
		LogReader lr = new LogReader();
		lr.getLogger();
	}
	
	public List<MainLogger> getLogger() throws IOException {
		List<MainLogger> allLogs = new ArrayList<>();
		File generalLogs = new File(generalLogsFilePath);
		if(!generalLogs.exists()) {
			return new ArrayList<>();
		}
		FileReader generalLogReader = new FileReader(generalLogs);
		BufferedReader bufferedReader = new BufferedReader(generalLogReader);
		String nextLine = "";
		while((nextLine=bufferedReader.readLine())!=null) {
			MainLogger log = new MainLogger();
			if(nextLine.contains("|")) {
			List<String> logs =   Arrays.asList( nextLine.split("\\|") );
				if(logs.size()>0) {
					if(logs.contains("user")) {
						String user = logs.get(logs.indexOf("user")+1);
						log.setUserName(user);
					}
					if(logs.contains("database")) {
						String activeDatabase = logs.get(logs.indexOf("database")+1);
						log.setActiveDatabase(activeDatabase);
					}
					if(logs.contains("table")) {
						String table = logs.get(logs.indexOf("table")+1);
						log.setTableName(table);
					}
					if(logs.contains("type")) {
						LogType type = LogType.valueOf( logs.get(logs.indexOf("type")+1));
						log.setLogType(type);
					}

				}
			}
			allLogs.add(log);
		}
		bufferedReader.close();
		return allLogs;
	}
}
