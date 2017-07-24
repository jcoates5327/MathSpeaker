import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Logger {
	private static boolean hasError = false;
	private static List<String> errors = new ArrayList<>();
	
	public static void reportError(String err) {
		errors.add(err);
		
		if (!hasError) hasError = true;
	}
	
	public static void writeLog(String file) {
		try {
			FileHandler.writeLines(errors.toArray(new String[errors.size()]), file);
		} catch (IOException e) {
			System.err.println("Unable to write log file.");
		}
	}
	
	public static List<String> getErrors() {
		return errors;
	}
	
	public static boolean hasError() {
		return hasError;
	}

}
