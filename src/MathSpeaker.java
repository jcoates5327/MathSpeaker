import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class MathSpeaker {
	private static final String CSS_DIR = "includes\\";
	private static final String START_HERE_DIR = CSS_DIR;

	public static void main(String[] args) {
		HTMLGenerator g = null;

		Scanner sc = new Scanner(System.in);
		System.out.print("Enter a file to convert (e.g., 05 Chapter.xml): ");
		String fileIn = sc.nextLine();
		
		
		if (fileIn == null || fileIn.equals("")
				|| !FileHandler.getExtension(fileIn).equals("xml")) {
			// invalid input
			System.out.println("Invalid file name - proper format is: File_Name.xml");
			sc.close();
			return;
		} else {
			System.out.println("Generating HTML...");
			
			
			
			String chName = FileHandler.getFileName(fileIn);
			File xmlDir = new File(chName + "\\XML");
			File htmlDir = new File(chName + "\\XHTML\\html\\imgs");
			File miscDir = new File("misc");
			xmlDir.mkdirs();
			htmlDir.mkdirs();
			miscDir.mkdir();
			
			g = new HTMLGenerator(fileIn);
			g.generateContinuousView();
			
			System.out.println("Converting MathML to SVG...");
			
			ProcessBuilder pb = new ProcessBuilder("C:\\Python27\\python", "SVGMath\\math2svg.py", "-o", "misc\\math2svgout.xml", "misc\\mathml.xml");
			File log = new File("log.txt");
			pb.redirectErrorStream(true);
			pb.redirectOutput(ProcessBuilder.Redirect.appendTo(log));
			
			try {
				Process p = pb.start();
				p.waitFor();
				g.splitSVGs();
				
				// copy css, start here
				FileHandler.fileCopy(CSS_DIR + "main.css", chName + "\\XHTML\\html\\main.css");
				FileHandler.fileCopy(CSS_DIR + "dtbookbasic.css", chName + "\\XML\\dtbookbasic.css");
				FileHandler.fileCopy(CSS_DIR + "test.css", chName + "\\XHTML\\html\\test.css");
				
				String startPage = FileHandler.readFileAsString(START_HERE_DIR + "Start Here.html", false);
				String[] meta = g.getMetaData();
				startPage = startPage.replaceAll("DOC_TITLE", meta[0])
									 .replaceAll("DOC_AUTHOR", meta[1])
									 .replaceAll("DOC_FIRST_PAGE", meta[2]);
				FileHandler.writeString(startPage, chName + "\\XHTML\\Start Here.html");
				
				/*
				// temp eq. numbering
				String xml = FileHandler.readFileAsString("\\XML\\05 Chapter.xml", false);
				int eq = 1;
				XMLScanner scanner = new XMLScanner(xml);
				String curTag;
				while ((curTag = scanner.nextTag()) != null) {
					if (curTag.equals("<mml:math>")) {
						
					}
				} */
				
				System.out.println("Conversion complete!");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				System.out.println("There was an error during MathML to SVG conversion.");
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Error handling files.");
			}
			
		}

		if (Logger.hasError()) {
			Logger.writeLog(g.getMiscDir() + "log.txt");
		}
		sc.close();
	}
	
}
