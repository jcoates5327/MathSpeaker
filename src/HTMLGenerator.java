import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTMLGenerator {
	private final String METADATA = "<html>\n"
			+ "\t<head>\n\t\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n"
			+ "\t\t<meta charset=\"utf-8\" />\n\t\t<title>Continuous View</title>\n\t\t<link href=\"main.css\" rel=\"stylesheet\" />\n"
			+ "\t</head>\n\t<body>\n\n";
	private final String PAGE_METADATA1 = "<html>\n"
			+ "\t<head>\n"
			+ "\t\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"></meta>\n"
			+ "\t\t<meta charset=\"utf-8\"></meta>\n"
			+ "\t\t<title>";
	private final String PAGE_METADATA2 = "</title>\n"
			+ "\t\t<link href=\"main.css\" rel=\"stylesheet\"></link>\n"
			+ "\t</head>\n"
			+ "\t<body>\n\n";
	private final String PAGE_LINK = "\t\t\t<a class=\"pagelink\" href=\"PAGEGOESHERE\">";	// don't forget to close!
	private final String[] IGNORE_TAGS = {"/?level\\d", "p ?/", "/?rearmatter", "/?head"};
	
	private final String MISC_DIR = "misc\\";
	
	private String fname;
	private XMLScanner scanner;
	private MMLTranslator translator;
	private List<String> pages, mml;
	private int indent, eqNum;
	
	private String chName, xmlDir, htmlDir, imgDir;
	private String docTitle, docAuthor, firstPage;
	
	private boolean appendGoldText = false, override = false;
	private String overrideText;
	
		
	/*
	 * (MOSTLY DONE - NEEDS TESTING) implement page view generation
	 * (MOSTLY DONE - NEEDS TESTING) finish mml -> mathspeak translation (munder, mover, etc. not done)
	 * (MOSTLY DONE - NEEDS TESTING) output cleaned up xml
	 * periods in URLs have spaces inserted after for some reason
	 * export math speak and equations to separate doc for easy qc
	 * create imgs\eqs directory for equation images (keeps things cleaner)
	 * proper exception handling & error reporting
	 * allow users to choose files to process
     * include custom stylesheet (main.css)
     * (STRETCH) add options for changing MathSpeak verbosity
     * (STRETCH) track line number
     * (STRETCH) implement listfixer
     * (DONE) properly size SVGs
     * (DONE) clean up/remove <span> tags
     * (DONE) handle equations in headers
	 */
	
	public HTMLGenerator(String fname) {
		this.fname = fname;
		indent = 2;
		eqNum = 1;
		
		chName = FileHandler.getFileName(fname);
		xmlDir = chName + "\\XML\\";
		htmlDir = chName + "\\XHTML\\html\\";
		imgDir = chName + "\\XHTML\\html\\imgs\\";
		
		scanner = new XMLScanner();
		pages = new ArrayList<>();
		mml = new ArrayList<>();
		translator = new MMLTranslator();
	}

	public int generateContinuousView() {
		boolean isComplexHeader = false, isHeader = false;
		boolean insideDiv = false, multiLine = false;
		
		int res = scanner.openFile(fname);
		if (res < 0) return -1;
		scanner.runRYMEC();	// RYMEC
		//scanner.splitMText();
		docTitle = scanner.getDocTitle();
		docAuthor = scanner.getDocAuthor();
		scanner.pointAtBody();
		
		HTMLDoc html = new HTMLDoc();	// buffer to hold contents of continuous view page
		XMLDoc xml = XMLDoc.getInstance();	// garbage
		html.append(METADATA);
		xml.append(METADATA);
				
		/*
		 * MAIN LOOP
		 * 
		 * processes xml tag-by-tag
		 */
		String curTag;
		while ((curTag = scanner.nextTag()) != null) {
			// skip over certain tags
			for (String s : IGNORE_TAGS) {
				if (curTag.matches(s)) {
					continue;
				}
			}
						
			/*
			 * PAGENUM 
			 */
			if (curTag.contains("<pagenum")) {
				String page = scanner.getDataBetweenUTags(curTag);
				pages.add(page);
								
				html.append("\t\t<div class=\"navigation\">\n");
								
				xml.append("\t\t<div class=\"navigation\">\n");
				html.append("\t\t\t<p class=\"pagenum\">\n");
				xml.append("\t\t\t<p class=\"pagenum\">\n");
				html.append("\t\t\t\t" + page + "\n");
				xml.append("\t\t\t\t" + page + "\n");
				html.append("\t\t\t</p>\n");
				xml.append("\t\t\t</p>\n");
				html.append("\t\t</div>\n");
				xml.append("\t\t</div>\n");
				
				
			/*
			 * HEADERS
			 */
			} else if (curTag.matches("<h\\d>")) {
				isHeader = true;
				String heading = scanner.getDataBetweenTags().trim();
				heading = stripFormatting(heading).trim();
				
				html.indent(indent);
				html.append(curTag);
				xml.append(curTag);
				html.append(heading);
				xml.append(heading);
				
				if (scanner.peek(1).equals("<mml:math>")) {
					isComplexHeader = true;
					html.append("\n");
					xml.append("\n");
				}
			
			/*
			 * END HEADERS
			 */
			} else if (curTag.matches("</h\\d>")) {
				isHeader = false;
				if (isComplexHeader) {
					html.indent(indent);
					isComplexHeader = false;
				}
				
				html.append(curTag);
				
				xml.append(curTag);
				html.append("\n");
				xml.append("\n");
				
			/*
			 * PARAGRAPH
			 */
			} else if (curTag.equals("<p>")) {
				html.indent(indent);
				html.append("<p>");
				xml.append("<p>");
				if (!scanner.peek(1).equals("<span>")) html.append("\n"); xml.append("\n");
				indent++;
				
				String data = scanner.getDataBetweenTags();
				if (data.matches("(?:\\s*\\S+\\s*)+")) {
					html.indent(indent);
					html.append(data);
					xml.append(data);
				} 
				
				html.append("\n"); 
				
				xml.append("\n");
				
			/*
			 * END PARAGRAPH
			 */
			} else if (curTag.equals("</p>")) {
				indent--;
				html.indent(indent);
				html.append("</p>\n");
				xml.append("</p>\n");
				
			/*
			 * TABLE
			 */
			} else if (curTag.matches("<table.*?>") || curTag.matches("<th.*?>") || curTag.matches("<td.*?>")
					|| curTag.matches("<tr.*?>")) {
				html.indent(indent);
				html.append(curTag);
				xml.append(curTag);
				html.append("\n");
				xml.append("\n");
				html.append(scanner.getDataBetweenTags());
				xml.append(scanner.getDataBetweenTags());
				indent++;
			
			/*
			 * END TABLE
			 */
			} else if (curTag.matches("</table.*?>") || curTag.matches("</th.*?>") || curTag.matches("</td.*?>")
					|| curTag.matches("</tr.*?>")) {
				indent--;
				html.indent(indent);
				html.append(curTag);
				xml.append(curTag);
				html.append("\n");
				xml.append("\n");
			
			/*
			 * MATHML
			 */			
			} else if (curTag.equals("<mml:math>")) {
				// handle special processing instructions
				String startMML = "";
				if (insideDiv) {
					startMML = "<mml:math type='div'>";
				} else if (multiLine) {
					startMML = "<mml:math type='multiline'>";
				}
				
				String mathmlTranslate = startMML + scanner.getDataBetweenTags() + "</mml:math>";
				String mathml = "<mml:math>" + scanner.getDataBetweenTags() + "</mml:math>";
				
				mathml = formatMathML(mathml);
				mathmlTranslate = formatMathML(mathmlTranslate);
				mml.add(mathml);
				
				String mathSpeak = "MATH PROCESSING ERROR";
				if (override) {
					mathSpeak = overrideText;
					override = false;
				} else {
					try {
						mathSpeak = translator.processMML(mathmlTranslate.split("\n"));
					} catch (ArrayIndexOutOfBoundsException e) {
						System.out.println("MATH PROCESSING ERROR: " + mathml);
					}					
				}
				
				String mathLine1 = "<img src=\"imgs/eq" + eqNum + ".svg\" />\n";
				String mathLine2 = "<span id=\"spk" + eqNum + "\" class=\"speak\">\n";
				html.indent(indent);
				html.append(mathLine1);
				html.indent(indent);
				html.append(mathLine2);
				indent++;
				html.indent(indent);
				html.append(mathSpeak);
				html.append("\n");
				indent--;
				html.indent(indent);
				html.append("</span>\n");
								
				if (appendGoldText) {
					String goldLine = "<span class=\"gold\">\n";
					html.indent(indent);
					html.append(goldLine);
					indent++;
					html.indent(indent);
					html.append(mathSpeak);
					html.append("\n");
					indent--;
					html.indent(indent);
					html.append("</span>\n");
				}
				
				eqNum++;
			
			/*
			 * END MATHML
			 */
			} else if (curTag.equals("</mml:math>")) {
				if (!isHeader) {
					html.indent(indent);
				}
				
				// make sure nothing else breaks
				String data = scanner.getDataBetweenTags();
				if (data.matches("(?:\\s*\\S+\\s*)+")) {
					html.append(data);
					xml.append(data);
				}
				
				if (!isHeader) {
					html.append("\n");
					xml.append("\n");
				}
			
			/*
			 * IMAGE
			 */
			}  else if (curTag.equals("<imggroup>")) {
				html.indent(indent);
				
				String img = scanner.getDataBetweenTags().trim().replaceAll("src=\"(.*?)\"", "src=\"imgs/$1\"");	 // get <img> tag
				/* int altTextStart = img.indexOf("alt=\"") + 5;	// get index of start of alt text
				int altTextEnd = img.indexOf("\" width"); // get index of end of alt text
				if (altTextEnd < 0) {
					altTextEnd = img.indexOf("\" />");	// in case there is no width attribute
				}
				// extract alt text and make any necessary changes
				String altText = img.substring(altTextStart, altTextEnd);
				
				// rebuild <img> tag and insert into HTML
					System.out.println("--------------------------------------");
					System.out.println("Image " + imagesOnPage + " on Page " + pages.get(pages.size()-1));
					System.out.println("Alt Text: " + altText);
					System.out.println("--------------------------------------");
					
					boolean done = false;
					Scanner sc = new Scanner(System.in);
					String input;
					while (!done) {
						System.out.println("Edit? (y/n)");
						input = sc.nextLine();
						if (input == null || input.equals("n") || input.equals("")) {
							done = true;
						} else {
							System.out.println("Enter new alt text.");
							String newAltText = sc.nextLine();
							System.out.println("New Alt Text: " + newAltText);
							System.out.println("Type anything to confirm, leave blank to cancel.");
							
							input = sc.nextLine();
							if (input == null || input.equals("")) {
								continue;
							} else {
								altText = newAltText;
								done = true;
							}
						}
					}
				
				img = img.substring(0, altTextStart-5) + "alt=\"" + altText + img.substring(altTextEnd, img.length());
				*/
				html.append(img);
				xml.append(img);
				html.append("\n");
				xml.append("\n");
				//scanner.nextTag();	// skip over </imggroup>
				
			/*
			 * END IMAGE
			 */
			} else if (curTag.equals("</imggroup>")) {
				html.indent(indent);
				html.append(scanner.getDataBetweenTags());
				xml.append(scanner.getDataBetweenTags());
				html.append("\n");
				xml.append("\n");
				
			/*
			 * END BODYMATTER
			 */
			} else if (curTag.equals("</bodymatter>")) {
				if ((curTag = scanner.nextTag()).equals("</book>")) {
					if ((curTag = scanner.nextTag()).equals("</dtbook>")) {
						html.append("\t");
						xml.append("\t");
						html.append("</body>\n");
						xml.append("</body>\n");
						html.append("</html>");
						xml.append("</html>");
					} else {
						Logger.reportError("Missing </dtbook> tag.");
					}
				} else {
					Logger.reportError("Missing </book> tag.");
				}
			
			/*
			 * DIV - INDICATES SPECIAL HANDLING FOR TRANSLATOR
			 */
			} else if (curTag.equals("<div>")) {
				translator.setInsideDiv(true);
			} else if (curTag.equals("</div>")) {
				translator.setInsideDiv(false);
			} else if (curTag.equals("<mml:moverride>")) {
				overrideText = scanner.getDataBetweenTags();
				override = true;
			}
		}
		
		
		//scanner.writeXML(xmlDir + fname);	// output cleaned up XML for use with Dolphin EasyReader
		String xmlOut = xml.getString().replaceAll("<mml:moverride>.*?</mml:moverride>", "");
		generateXMLForConversion();
		String htmlOut = html.toString().replaceAll("<mml:moverride>.*?</mml:moverride>", "");
				
		try {
			FileHandler.writeString(htmlOut, htmlDir + "Continuous View.html");
			FileHandler.writeString(xmlOut, xmlDir + fname);
		} catch (IOException e) {
			Logger.reportError("Failed to write Continuous View to disk.");
			return -1;
		}		
		generatePageView(htmlOut);
		
		
		return 1;
	}
	
	private void generatePageView(String html) {
		String[] splitPages = html.split("<div class=\"navigation\">\\s*<p class=\"pagenum\">\\s*.*?\\s*</p>\\s*</div>");
		firstPage = pages.get(0).replaceAll(" ", "%20") + ".html";
		
		int curPage = 0;
		int mode = 0;
		StringBuilder page;
		
		while (curPage < pages.size()) {
			if (curPage == 0) {
				mode = -1;
			} else if (curPage == pages.size() - 1) {
				mode = 1;
			} else {
				mode = 0;
			}
			
			page = new StringBuilder();
			page.append(PAGE_METADATA1);
			page.append(pages.get(curPage));
			page.append(PAGE_METADATA2);
			
			page.append(buildPageNavSection(curPage, mode));
			page.append("\t");
			
			page.append(splitPages[curPage + 1].trim());
			page.append("\n");
			
			if (mode != 1) {
				page.append(buildPageNavSection(curPage, mode));
				page.append("</body>\n</html>");				
			}
			
			try {
				FileHandler.writeString(page.toString(), htmlDir + pages.get(curPage) + ".html");
			} catch (IOException e) {
				Logger.reportError("Unable to write page: " + pages.get(curPage));
			}
			
			curPage++;
		}
	}
	
	/*
	 * MODE parameter
	 * 
	 * mode < 0: first page
	 * mode = 0: middle page
	 * mode > 0: last page
	 */
	private String buildPageNavSection(int curPage, int mode) {
		StringBuilder nav = new StringBuilder();
		String page = pages.get(curPage);
		
		nav.append("\t\t<div class=\"navigation\">\r\n\t\t\t<p class=\"pagenum\">");
		nav.append(page);
		nav.append("</p>\n");
		
		if (mode < 0) {
			nav.append(PAGE_LINK.replaceAll("PAGEGOESHERE", "../Start%20Here.html"));
		} else {
			nav.append(PAGE_LINK.replaceAll("PAGEGOESHERE", pages.get(curPage - 1) + ".html"));			
		}
		
		nav.append("Previous Page</a>\n");
		
		if (mode <= 0 && pages.size() > 1) {
			nav.append(PAGE_LINK.replaceAll("PAGEGOESHERE", pages.get(curPage + 1) + ".html"));			
			nav.append("Next Page</a>\n");
		}
		
		nav.append("\t\t</div>\n\t");
		
		return nav.toString();
	}
	
	private void generateXMLForConversion() {
		StringBuilder out = new StringBuilder();
		out.append("<?xml version='1.0' encoding='UTF-8'?>\n");
		out.append("<book xmlns:mml=\"http://www.w3.org/1998/Math/MathML\">\n");
		
		int eq = 1;
		for (String s : mml) {
			out.append("eq" + (eq++) + "\n");
			out.append(s);
		}
		out.append("</book>\n");
		
		try {
			FileHandler.writeString(out.toString(), MISC_DIR + "mathml.xml");
		} catch (IOException e) {
			Logger.reportError("Unable to write raw MathML file for conversion: mathml.xml");
		}
	}
	
	public void splitSVGs() {
		String[] svg;
		int eqNum = 1;
		try {
			svg = FileHandler.readLines(MISC_DIR + "math2svgout.xml");
			for (int i = 0; i < svg.length; i++) {
				if (svg[i].contains("svg:svg")) {
					Matcher m = Pattern.compile("height=\"(\\d+\\.\\d+)pt\"").matcher(svg[i]);
					
					if (m.find()) {
						int h = Float.valueOf(m.group(1).substring(0, m.group(1).length())).intValue();
						//h += 4;
						String svgOut = svg[i];
						svgOut = svgOut.replaceFirst("height=\".*?pt\"", "height=\"" + h + "pt\"")
									   .replaceAll("font-weight=\"bold\" font-style=\"italic\"", "font-weight=\"bold\"");
						FileHandler.writeString(svgOut, imgDir + "eq" + eqNum + ".svg");
						eqNum++;
					} else {
						Logger.reportError("Unable to find height attribute in SVG");
					}
				}
				float pct = (((float) i) / ((float) svg.length)) * 100.0f;
				if (pct % 10 == 0) {
					System.out.println(pct + "%");
				}
			}
		} catch (IOException e) {
			Logger.reportError("Failed to read raw SVG output from math2svg: math2svgout.xml");
		}
	}
	
	private String formatMathML(String mml) {
		XMLScanner sc = new XMLScanner(mml);
		sc.setMode(2);
		StringBuilder out = new StringBuilder();
		boolean styleOpen = false;
		
		String tag;
		String multiTags = "mo|mi|mn|mtext";
		while ((tag = sc.nextTag()) != null) {
			if (tag.matches("<mml:mstyle mathvariant='bold'>")) {
				styleOpen = true;
			}
			
			if (tag.matches("<mml:(?:" + multiTags + ")(?: mathvariant=\"bold\")?>")) {
				out.append(tag);
				out.append(sc.getDataBetweenTags());
				out.append(sc.nextTag());
				out.append("\n");
			} else if (tag.matches("<mml:(?:mtr|mtd)>")) {
				out.append(tag);
				if (!sc.peek(1).matches("</mml:(?:mtr|mtd)>")) {
					out.append("\n");
					continue;
				}
				
				out.append(sc.getDataBetweenTags());
				out.append(sc.nextTag());
				out.append("\n");
			} else if (tag.matches("</mml:mstyle>")) {
				if (!styleOpen) {
					continue;
				} else {
					out.append("</mml:mstyle>\n");
					styleOpen = false;
				}
			} else {
				out.append(tag);
				out.append("\n");
			}
		}
		
		return out.toString();
	}
	
	private String stripFormatting(String data) {
		if (data.contains("<strong>")) {
			data = data.replaceAll("<strong>", "")
					   .replaceAll("</strong>", "");
		}
		
		return data;
	}
	
	private StringBuilder indent(StringBuilder sb) {
		for (int i = 1; i <= indent; i++) {
			sb.append("\t");
		}
		return sb;
	}
	
	public String getMiscDir() {
		return MISC_DIR;
	}
	
	public String[] getMetaData() {
		String[] meta = {docTitle, docAuthor, firstPage};
		return meta;
	}
	
	public void setAppendGoldText(boolean val) {
		appendGoldText = val;
	}
	
}
