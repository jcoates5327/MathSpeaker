import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class FileHandler {
	public FileHandler() {
		
	}
	
	public static List<String[]> readMathML(String[] lines) {
		List<String[]> mml = new ArrayList<>();
		
		int i;
		for (i = 0; i < lines.length; i++) {
			if (lines[i].contains("<mml:math")) {
				List<String> outLines = new ArrayList<>();
				outLines.add("<mml:math>\n");
				i++;
				while (!lines[i].contains("</mml:math")) {
					outLines.add(lines[i]);
					i++;
				}
				outLines.add("</mml:math>");
				i--;	// decrement i to account for possible <mml:math> on same line as </mml:math>
				mml.add(outLines.toArray(new String[outLines.size()]));
			}
		}
		return mml;
	}
	
	public static String[] readLines(String filename) throws IOException {
		CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
		decoder.onMalformedInput(CodingErrorAction.REPORT);
		decoder.onUnmappableCharacter(CodingErrorAction.REPORT);		
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), decoder));
		
		List<String> lines = new ArrayList<String>();
		String line = null;
		
		while ((line = bufferedReader.readLine()) != null) {
			lines.add(line);
		}
		bufferedReader.close();
		return lines.toArray(new String[lines.size()]);
	}
	
	public static String readFileAsString(String filename, boolean appendNL) throws IOException {
		CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
		decoder.onMalformedInput(CodingErrorAction.REPORT);
		decoder.onUnmappableCharacter(CodingErrorAction.REPORT);		
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), decoder));
		
		StringBuilder s = new StringBuilder();
		String line = null;
		
		while ((line = bufferedReader.readLine()) != null) {
			s.append(line);
			if (appendNL) s.append("\n");
		}
		bufferedReader.close();
		return s.toString();
	}
	
	public static void writeString(String data, String filename) throws IOException {
		CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
		encoder.onMalformedInput(CodingErrorAction.REPORT);
		encoder.onUnmappableCharacter(CodingErrorAction.REPORT);
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), encoder));
		
		out.write(data);
		out.close();
	}
	
	public static void writeLines(String[] data, String filename) throws IOException {
		CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
		encoder.onMalformedInput(CodingErrorAction.REPORT);
		encoder.onUnmappableCharacter(CodingErrorAction.REPORT);
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), encoder));
		
		for (String s : data) {
			out.write(s);
		}
		out.close();
	}
	
	public static String getExtension(String fname) {
		return fname.substring(fname.length() - 3, fname.length());
	}
	
	public static String getFileName(String fname) {
		return fname.substring(0, fname.length() - 3);
	}
	
	public static void fileCopy(String src, String dst) throws IOException {
		Path source = Paths.get(src);
		Path dest = Paths.get(dst);
		
		Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
	}
}
