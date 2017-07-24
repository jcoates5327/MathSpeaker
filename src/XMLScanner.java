import java.io.IOException;

public class XMLScanner {
	private final String[] VALID_TAGS = {"</?dtbook>", "</?book>", "</?frontmatter>", "</?meta( name=.*? content=.*?)? />",
										 "</?doctitle>", "</?docauthor>", "</?h\\d>", "</?p ?>", "</?table.*?>", "</?td.*?>",
										 "</?span>", "<pagenum( page=.*? id=.*?)?>", "</?mml:math>", "</?th.*?>", "</?tr.*?>",
										 "</?div>", "</?imggroup>", "</pagenum>", "</bodymatter>", "</?author>", "</?mml:moverride>"};
	
	private final String[] MML_TAGS = {"</?mml:math>", "</?mml:mrow>", "</?mml:mo>", "</?mml:mi.*?>", "</?mml:mn>", "</?mml:mfrac>",
									   "</?mml:mroot>", "</?mml:msqrt>", "</?mml:msub>", "</?mml:msup>", "</?mml:msubsup>",
									   "<mml:mstyle mathvariant='bold'>", "</mml:mstyle>",
									   "</?mml:munder.*?>", "</?mml:mover.*?>", "</?mml:mmultiscripts.*?>", "</?mml:mtable.*?>", "</?mml:mtd.*?>",
									   "</?mml:mtr.*?>", "</?mml:mtext>", "</?mml:menclose.*?>", "</?mml:munderover.*?>", "<mml:none ?/>",
									   "<mml:mprescripts ?/>", "<mml:mspace.*?>"};
	
	private String fname, xml;
	private String curTag, prevTag;
	private String[] tagSet;
	private int ptr;	// keeps track of where we are within the document
	private int mode;	// 1 (default) == standard, 2 == MathML
	
	public XMLScanner() {
		ptr = -1;
		mode = 1;
		tagSet = VALID_TAGS;
	}
	
	public XMLScanner(String rawXML) {
		xml = rawXML;
		ptr = -1;
		fname = "";
		mode = 1;
		tagSet = VALID_TAGS;
	}
	
	public String nextTag() {
		String tagName = "";
		boolean isValid = false;
		
		while (!isValid) {
			ptr++;
			while (xml.charAt(ptr) != '<') {
				if (ptr >= xml.length() - 1) {
					return null;
				}
				ptr++;
			}
			
			int tagEnd = xml.indexOf('>', ptr)+1;
			tagName = xml.substring(ptr, tagEnd).toLowerCase().trim();
			
			for (String s : tagSet) {
				if (tagName.matches(s)) {
					isValid = true;
					prevTag = curTag;
					curTag = tagName;
					//if (tagName.contains("pagenum")) System.out.println(tagName);
					break;
				}
			}
			
			if (!isValid && !tagName.contains("mml:")) {
				//Logger.reportError("Invalid tag @ position " + ptr + ", " + tagName + "\r\n");
			}
		}
		
		if (isValid) {
			return tagName;
		} else {
			// sanity check
			return "default";
		}
	}
	
	// returns the next available tag, without moving the pointer
	public String peek(int numTags) {
		int oldPtr = ptr;	// store pointer
		
		String tag = "";
		for (int i = 1; i <= numTags; i++) {
			tag = nextTag();	// get next tag
		}
		ptr = oldPtr;	// reset pointer
		
		return tag;
	}
	
	public int peekPtr(int numTags) {
		int oldPtr = ptr;	// store pointer
		
		for (int i = 1; i <= numTags; i++) {
			nextTag();	// get next tag
		}
		int peekPtr = ptr;
		ptr = oldPtr;	// reset pointer
		
		return peekPtr;
	}
	
	public int peekPtr(int numTags, int ptrIn) {
		int oldPtr = ptr;	// store pointer
		ptr = ptrIn;
		
		for (int i = 1; i <= numTags; i++) {
			nextTag();	// get next tag
		}
		int peekPtr = ptr;
		ptr = oldPtr;	// reset pointer
		
		return peekPtr;
	}
	
	public int openFile(String file) {
		fname = file;
		
		try {
			xml = FileHandler.readFileAsString(fname, false);
		} catch (IOException e) {
			return -1;
		}
		
		return 0;
	}
	
	public int pointAtBody() {
		int i = xml.indexOf("<bodymatter");
		
		if (i < 0) {
			return -1;
		}
		ptr = i;
		
		return i;
	}
	
	/*
	 * delete the override tag for the current math block
	 * technically deletes the NEXT tag; curTag should be <mml:math>
	 */
	public void deleteOverrideTag() {
		int firstPartEndIndex = peekPtr(1);
		int secondPartBeginIndex = xml.indexOf("</mml:moverride>", firstPartEndIndex);
		
		xml = xml.substring(0, firstPartEndIndex) + xml.substring(secondPartBeginIndex);
	}
	
	// <h#>, <pagenum>, <mml:XYZ>, <doctitle>, <docauthor>
	public String getDataBetweenUTags(String curTag) {
		//Matcher m = Pattern.compile("<" + curTag + )
		
		String nextTag = peek(1);
		
		// should never happen - indicates doc ends with open tag w/out close
		if (nextTag == null) return null;
		
		if (curTag.contains(nextTag.substring(2, nextTag.length() - 1))) {
			int startData = xml.indexOf('>', ptr);
			int endData = xml.indexOf('<', startData);
			return xml.substring(startData+1, endData);
		}
		
		return "ERROR";
	}
	
	public String getDataBetweenTags() {
		int startData = xml.indexOf('>', ptr);
		
		/*
		if (peek(1).contains("<imggroup")) {
			return getDataBetweenTags(peekPtr(2));
		} */
		return xml.substring(startData+1, peekPtr(1));
	}
	
	public String getDataBetweenTags(int ptrIn) {
		int startData = xml.indexOf('>', ptrIn) ;
		return xml.substring(startData+1, peekPtr(1, ptrIn));
	}
	
	public String getDocTitle() {
		int i = xml.indexOf("<doctitle");
		return getDataBetweenTags(i);
	}
	
	public String getDocAuthor() {
		int i = xml.indexOf("<docauthor");
		return getDataBetweenTags(i);
	}
	
	public boolean hasTextAfter() {
		String data = xml.substring(ptr + curTag.length(), peekPtr(1));
		
		if (data.matches("\\s+") || data.equals("")) {
			return false;	// only whitespace between tags or nothing at all
		} else if (data.matches("(?:\\s*\\S+\\s*)+")) {
			return true;
		} else {
			// LOG ERROR
			return false;
		}
	}
	
	public void writeXML(String dir) {
		String out = xml.replaceAll("\\. (jpg|svg|png)", "\\.$1")
						.replaceAll("<!DOCTYPE dtbook.*?] >\\s*<dtbook", "<dtbook")
						.replaceAll("www\\. ?w3\\. ?org", "www\\.w3\\.org")
						.replaceAll("www\\. ?daisy\\. ?org", "www\\.daisy\\.org")
						.replaceAll("dtbookbasic\\. css", "dtbookbasic\\.css")
						.replaceAll("<mml:moverride>.*?</mml:moverride>", "");
		try {
			FileHandler.writeString(out, dir);
		} catch (IOException e) {
			Logger.reportError("Error writing XML file.");
		}
	}
	
	public void splitMText() {
		StringBuilder newXML = new StringBuilder();
		String tag;
		int prevPtr = 0;
		setMode(2);
		
		while ((tag = nextTag()) != null) {
			if (tag.matches("<mml:mtext.*?>")) {
				String text = getDataBetweenTags().replaceAll("&#x00A0;", " ").trim();
				for (String s : text.split(" ")) {
					newXML.append("<mml:mtext>");
					newXML.append(s);
					newXML.append("</mml:mtext>");
					newXML.append("<mml:mspace width=\"0.2em\"/>");
				}
				
			} else {
				newXML.append(xml.substring(prevPtr, ptr));
			}
			prevPtr = ptr;
		}
		setMode(1);
		xml = newXML.toString();
	}
	
	// broken garbage
	public void fixMultiLine() {
		StringBuilder newXML = new StringBuilder();
		int prevPtr = 0;
		
		String tag;
		while ((tag = nextTag()) != null) {
			if (tag.equals("<author>")) {
				// indicates multi-line equation
				setMode(2);	// switch to MML
				if (peek(1).equals("<mml:math>")) {
					if (peek(2).equals("<mml:mtable>")) {
						// good to go
						boolean done = false;
						newXML.append(xml.substring(prevPtr, ptr));	// append everything up to "<author>" tag
						while (!done) {
							newXML.append("<p><mml:math><mml:mrow>");
							while (!(tag = nextTag()).equals("<mml:mtd>")) {}	// position scanner at start of equation
							nextTag();
							prevPtr = ptr;	// prevPtr holds index of equation start
							while (!(tag = nextTag()).equals("</mml:mtd>")) {}	// position scanner at end of equation
							newXML.append(xml.substring(prevPtr, ptr));	// append MML content
							newXML.append("</mml:mrow></mml:math></p>");
							
							if (peek(1).equals("</mml:mtr>") && peek(2).equals("</mml:mtable>")) {
								// end multi-line equation
								done = true;
								nextTag();	// </mml:mtr>
								nextTag();	// </mml:mtable>
								nextTag();	// </mml:math>
								setMode(1);	// switch back to DTBOOK
								nextTag();	// </author>
								prevPtr = peekPtr(1);
							}
						}
					}
				}
			}
			setMode(1);
		}
		newXML.append(xml.substring(prevPtr, xml.length()));
		xml = newXML.toString();
	}
	
	public void runRYMEC() {
		RYMEC r = new RYMEC();
		xml = r.runRYMEC(xml);
	}
	
	/*
	 * void setMode(int m);
	 * 
	 * @param m Mode flag: 1 for dtbook, 2 for MML
	 * 
	 * @return void
	 */
	public void setMode (int m) {
		mode = m;
		
		if (mode == 1) {
			tagSet = VALID_TAGS;
		} else if (mode == 2) {
			tagSet = MML_TAGS;
		}
	}
	
	public void setPtr(int ptr) {
		this.ptr = ptr;
	}
	
	public String getCurTag() {
		return curTag;
	}
	
	public String getPrevTag() {
		return prevTag;
	}
	
	public int getPtr() {
		return ptr;
	}
	
	public String getRawXML() {
		return xml;
	}
	
}
