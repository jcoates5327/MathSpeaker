import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Translations {
	private final String TRANSLATION_FILE = "includes\\MathSpeakTranslations.txt";
	Map<String, String> mathSpeak, mathSpeakReverse;
	
	public Translations() {
		mathSpeak = new HashMap<>();
		mathSpeakReverse = new HashMap<>();
		buildTranslations();
	}
	
	private void buildTranslations() {
		try {
			String[] lines = readLines(TRANSLATION_FILE);
			
			for (String line : lines) {
				if (line.contains(":")) {
					String[] data = line.split(":");
					String[] input = data[0].split(",");
					
					
					mathSpeak.put(input[0], data[1]);
					mathSpeak.put(input[1], data[1]);
					mathSpeakReverse.put(data[1], input[0]);
					mathSpeakReverse.put(data[1], input[1]);
				}
			}
			
			// manual additions - ',' and ':' are used as separators in the dictionary file
			mathSpeak.put(":", "colon");
			mathSpeakReverse.put("colon", ":");
			mathSpeak.put(",", "comma");
			mathSpeakReverse.put("comma", ",");
		} catch (IOException e) {
			Logger.reportError("Failed to read MathSpeak translation file: " + TRANSLATION_FILE);
		}
	}
	
	public String getTranslation(String toTranslate) {
		// handle whitespace
		if (toTranslate.equals("00A0") || toTranslate.equals("2009") || toTranslate.equals("200A")
				|| toTranslate.equals("0020")) {
			return "";
		}
		
		String res = mathSpeak.get(toTranslate);
		
		if (res == null) {
			Logger.reportError("Unknown identifier: " + toTranslate + "\r\n");
			return null;
		}
		
		return res;	// return null if translation not available
	}
	
	public String getReverseTranslation(String toTranslate)	 {
		String res = mathSpeakReverse.get(toTranslate);
		
		return res == null ? null : res;
	}
	
	private String[] readLines(String filename) throws IOException {
		FileReader fileReader = new FileReader(filename);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		List<String> lines = new ArrayList<String>();
		String line = null;
		while ((line = bufferedReader.readLine()) != null) {
			lines.add(line);
		}
		bufferedReader.close();
		return lines.toArray(new String[lines.size()]);
	}
}
