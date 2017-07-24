import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * MathML to MathSpeak translator
 */
public class MMLTranslator {
	private Translations trans;
	private boolean insideDiv = false, isFirstElement = true;
	private boolean underBrace = false, endScripts = true, endEnclose = true;
		
	public MMLTranslator() {
		trans = new Translations();
	}
	
	/*
	 * Process a MathML equation, converting its content into MathSpeak.
	 * 
	 * @param String[] lines An array of strings, where each element is a line of MathML
	 * 
	 * @return String A string containing the MathSpeak translation of the input MathML equation.
	 */
	public String processMML(String[] lines) throws ArrayIndexOutOfBoundsException {
		StringBuilder mathSpeak = new StringBuilder();
		Map<Integer, List<String>> toAddLater = new HashMap<>();
		XMLDoc xml = XMLDoc.getInstance();
		
		String line;
		for (int i = 0; i < lines.length; i++) {
			line = lines[i];
						
			if (toAddLater.keySet().contains(i)) {
				for (String s : toAddLater.get(i)) {
					mathSpeak.append(s);
				}
			}
			
			if (line.contains("<mml:mstyle mathvariant='bold'>")) {
				mathSpeak.append("Start Bold ");
				
				int level = 1;
				for (int endStyle = i; endStyle < lines.length; endStyle++) {
					if (lines[endStyle].contains("</mml:mstyle>")) {
						if (level <= 1) {
							toAddLater = addMappingDeep(toAddLater, endStyle, "End Bold ");
							break;
						} else {
							level--;
						}
					} else if (lines[endStyle].contains("<mml:mstyle.*?>")) {
						level++;
					}
				}
				
				continue;
			}
						
			/*
			 * SEE IF WE ARE INSIDE AN EQUATION THAT REQUIRES SPECIAL PROCESSING
			 */
			if (line.contains("<mml:math type='div'")) {
				insideDiv = true;
			}
			
			/*
			 * TAGS WITH ACTUAL CONTENT
			 * 
			 * <MI>, <MO>, <MN>, and <MTEXT>
			 */
			if (line.contains("<mml:mi") || (line.contains("<mml:mo") && !line.contains("<mml:mover")) || line.contains("<mml:mn") || line.contains("<mml:mtext")) {
				if (line.contains("<mml:mtext>")) {
					String fixed = checkForUnicodeInText(line);
					if (fixed != null) {
						line = fixed;
					}
				}
				
				String trans = translateLine(line);
				mathSpeak.append(trans);
				
				if (trans != null && !trans.equals("")) {
					mathSpeak.append(" ");
				}
			}
			
			/*
			 * FRACTION - <MFRAC>
			 */
			else if (line.contains("<mml:mfrac")) {
				mathSpeak.append("Start Fraction ");
				
				if (lines[i+1].contains("<mml:mrow>")) {
					// find index of numer
					int index = getEndRow(i+1, lines);
					toAddLater = addMappingDeep(toAddLater, index, "Over ");
				} else {
					toAddLater = addMappingDeep(toAddLater, i+2, "Over ");
				}
			}
			
			/*
			 * SUBSCRIPT - <MSUB>
			 */
			else if (line.matches("<mml:msub[^s]?")) {
				if (lines[i+1].contains("<mml:mrow>")) {
					// find index of base end
					int index = getEndRow(i+1, lines);
					toAddLater = addMappingDeep(toAddLater, index, "Sub ");
				} else if (lines[i+1].contains("<mml:mover")) {
					toAddLater = addMappingDeep(toAddLater, i+4, "Sub ");
				} else {
					toAddLater = addMappingDeep(toAddLater, i+2, "Sub ");
				}
			}
			
			/*
			 * SUPERSCRIPT - <MSUP>
			 */
			else if (line.contains("<mml:msup")) {
				if (lines[i+1].contains("<mml:mrow>")) {
					// find index of base end
					int index = getEndRow(i+1, lines);
					toAddLater = addMappingDeep(toAddLater, index, "Super ");
				} else if (lines[i+1].contains("<mml:mover")) {
					toAddLater = addMappingDeep(toAddLater, i+4, "Super ");
				} else if (lines[i+1].contains("<mml:msup")) {
					//toAddLater = addMappingDeep(toAddLater, i+3, "Super ");
					int index = getEndTag(i+1, "mml:msup", lines);
					toAddLater = addMappingDeep(toAddLater, index+1, "Super ");
				} else {
					toAddLater = addMappingDeep(toAddLater, i+2, "Super ");
				}
			}
			
			/*
			 * SUPER AND SUBSCRIPT - <MSUBSUP>
			 */
			else if (line.contains("<mml:msubsup")) {
				int sub, sup;
				
				if (lines[i+1].contains("<mml:mrow>")) {
					sub = getEndRow(i+1, lines);
				} else {
					sub = i + 1;
				}
				sub++;
				
				if (lines[sub].contains("<mml:mrow>")) {
					sup = getEndRow(sub, lines);
				} else {
					sup = sub;
				}
				sup++;
				
				toAddLater = addMappingDeep(toAddLater, sub, "Sub ");
				toAddLater = addMappingDeep(toAddLater, sup, "Base Super ");
			}
			
			/*
			 * ROOT - <MROOT>
			 */
			else if (line.contains("<mml:mroot")) {
				mathSpeak.append("Start Root ");
				int index;
				
				if (lines[i+1].contains("<mml:mrow>")) {
					index = getEndRow(i+1, lines);
				} else {
					index = i + 2;
				}
				
				if (!lines[index].contains("</mml:mroot>")) {
					toAddLater = addMappingDeep(toAddLater, index, "Root Index ");
				}
			}
			
			/*
			 * UNDERSCRIPTS - <MUNDER>
			 */
			else if (line.matches("<mml:munder[^o]?")) {
				mathSpeak.append("Start Scripts ");
				
				// find end of underscript block
				if (lines[i+3].contains("</mml:munder")) {
					toAddLater = addMappingDeep(toAddLater, i+2, "Underscript ");
				} else if (lines[i+1].contains("<mml:mrow>")) {
					int endRow = getEndRow(i+1, lines);
					toAddLater = addMappingDeep(toAddLater, endRow, "Underscript ");
				} 
				
				if (lines[i+1].matches("<mml:munder[^o]?")) {
					// nested underscript; probably bottom brace
					underBrace = true;
				}
				
			}
			
			/*
			 * OVERSCRIPTS - <MOVER>
			 */
			else if (line.contains("<mml:mover")) {
				// special rules for overbar
				if (lines[i+3].contains("</mml:mover")) {
					if (lines[i+2].contains("&#x00AF;") || lines[i+2].contains("&#x005E")) {
						//toAddLater = addMappingDeep(toAddLater, i+3, "overbar");
						endScripts = false;
					}
				} else {
					mathSpeak.append("Start Scripts ");
					
					// find end of underscript block
					if (lines[i+3].contains("</mml:mover")) {
						toAddLater = addMappingDeep(toAddLater, i+2, "Overscript ");
					} else if (lines[i+1].contains("<mml:mrow>")) {
						int endRow = getEndRow(i+1, lines);
						toAddLater = addMappingDeep(toAddLater, endRow, "Overscript ");
					} else {
						// single element, <mrow> around underscript
					}
					endScripts = true;
				}
			}
			
			/*
			 * UNDER & OVERSCRIPTS - <MUNDEROVER>
			 */
			else if (line.contains("<mml:munderover")) {
				mathSpeak.append("Start Scripts ");
				
				if (lines[i+4].contains("</mml:munderover")) {
					toAddLater = addMappingDeep(toAddLater, i+2, "Underscript ");
					toAddLater = addMappingDeep(toAddLater, i+3, "Overscript ");
				} else {
					int under, over;
					
					if (lines[i+1].contains("<mml:mrow>")) {
						under = getEndRow(i+1, lines);
						under++;
					} else {
						under = i + 2;
					}
					
					if (lines[under].contains("<mml:mrow>")) {
						over = getEndRow(under, lines);
						over++;
					} else {
						over = under + 1;
					}
					
					toAddLater = addMappingDeep(toAddLater, under, "Underscript ");
					toAddLater = addMappingDeep(toAddLater, over, "Overscript ");
				}
			}
			
			/*
			 * EMBELLISHMENTS - strike through, over bar, etc. - <MENCLOSE>
			 */
			else if (line.contains("<mml:menclose")) {
				//line = line.replaceAll("updiagonalstrike downdiagonalstrike", "updiagonalstrikedowndiagonalstrike");
				Matcher m = Pattern.compile("<mml:menclose notation='(\\w+(?: \\w+)?)'>").matcher(line);
				String modifier;
				
				if (m.find()) {
					switch (m.group(1)) {
						case "actuarial": modifier = "actuarial symbol"; break;
						case "radical": modifier = "square root"; break;
						case "box": modifier = "enclosing box"; break;
						case "roundedbox": modifier = "enclosing rounded box"; break;
						case "circle": modifier = "enclosing circle"; break;
						case "left": modifier = "left vertical line"; break;
						case "right": modifier = "right vertical line"; break;
						case "top": modifier = "over bar"; break;
						case "bottom": modifier = "under bar"; break;
						case "updiagonalstrike downdiagonalstrike": modifier = "up and down diagonal strike through";
						case "updiagonalstrike": modifier = "up diagonal strike through"; break;
						case "downdiagonalstrike": modifier = "down diagonal strike through"; break;
						case "verticalstrike": modifier = "vertical strike through"; break;
						case "horizontalstrike": modifier = "strike through"; break;
						case "madruwb": modifier = "arabic factorial symbol"; break;
						case "updiagonalarrow": modifier = "up diagonal arrow"; break;
						case "phasorangle": modifier = "phasor angle"; break;
						
						default : modifier = "long division symbol"; break;
					}
				} else {
					modifier = "long division symbol";
				}
				
				if (lines[i+2].contains("</mml:menclose") && (modifier.contains("strike through") || modifier.contains(" bar"))) {
					modifier = "with " + modifier + " ";
					toAddLater = addMappingDeep(toAddLater, i+2, modifier);
					endEnclose = false;
				} else {
					modifier = "Begin Modifier, " + modifier + ", ";
					mathSpeak.append(modifier);					
				}
				
			}
			
			/*
			 * PRE- & POST- SUB AND SUPERSCRIPTS - <MMULTISCRIPTS>
			 */
			else if (line.contains("<mml:mmultiscripts")) {
				// get base
				int curLine = i + 1;	// point curLine at base
				if (lines[curLine].contains("<mml:mrow")) {
					// base is a complex expression
					curLine = getEndRow(curLine, lines) + 1;
				} else {
					// base is a single term
					curLine++;
				}
				
				// curLine now points at PostSubscript
				if (lines[curLine].contains("<mml:none")) {
					// no PostSubscript
				} else if (lines[curLine].contains("<mml:mrow")) {
					// complex PostSubscript
				} else {
					// single term PostSubscript
				}
				curLine++;
				
				// curLine points at PostSuperscript
				if (lines[curLine].contains("<mml:none")) {
					// no PostSuperscript
				} else if (lines[curLine].contains("<mml:mrow")) {
					// complex PostSuperscript
				} else {
					// single term PostSuperscript
				}
				curLine++;
				
				
				
				mathSpeak.append("Start multi scripts ");
				int pre;
				if (lines[i+1].contains("<mml:mrow>")) {
					pre = getEndRow(i+1, lines) + 1;
				} else {
					pre = i+2;
				}
				
				/*
				 * Handle pre scripts
				 */
				int endMulti = -1;
				if (lines[pre].contains("<mml:mprescripts")) {
					int post;
					
					toAddLater = addMappingDeep(toAddLater, pre, "Start pre scripts ");
					if (lines[pre+1].contains("<mml:none")) {
						post = pre + 2;
					} else if (lines[pre+1].contains("<mml:mrow>")) {
						toAddLater = addMappingDeep(toAddLater, pre + 1, "Sub ");
						toAddLater = addMappingDeep(toAddLater, getEndRow(pre+1, lines), "Base ");
						post = getEndRow(pre+1, lines) + 1;
					} else {
						toAddLater = addMappingDeep(toAddLater, pre + 1, "Sub ");
						toAddLater = addMappingDeep(toAddLater, pre + 2, "Base ");
						post = pre + 2;
					}
					
					if (lines[post].contains("<mml:mrow>")) {
						toAddLater = addMappingDeep(toAddLater, post, "Super ");
						toAddLater = addMappingDeep(toAddLater, getEndRow(post, lines), "Base ");
					} else if (!lines[post].contains("<mml:none")){
						toAddLater = addMappingDeep(toAddLater, post, "Super ");
						toAddLater = addMappingDeep(toAddLater, post + 1, "Base ");
					}
					
					endMulti = post;
					while (!lines[endMulti].contains("</mml:mmultiscripts")) {
						endMulti++;
					}
					toAddLater = addMappingDeep(toAddLater, endMulti, "End pre scripts ");
					
				} else {
					Logger.reportError("Unexpected tag: " + lines[pre] + "; expected <mml:mprescripts/>\r\n");
				}
				
				/*
				 * Handle post scripts
				 */
				if (endMulti < 0) {
					Logger.reportError("Prescript not found.");
				} else {
					endMulti++;
					if (!lines[endMulti].contains("<mml:msup") && !lines[endMulti].contains("<mml:msub")) {
						mathSpeak.append("End multi scripts ");
					} else {
						Matcher m = Pattern.compile("<mml:m(sup|sub|subsup)>").matcher(lines[endMulti]);
						if (m.find()) {
							int end = getEndTag(endMulti, "mml:m" + m.group(1) + ">", lines) + 1;
							toAddLater = addMappingDeep(toAddLater, end, "End multi scripts ");
						} else {
							Logger.reportError("Unable to find sub/super script in multiscript.\r\n");
						}
					}
				}
			}
			
			/*
			 * MATRICES - <MTABLE>
			 * 
			 * IF insideDiv - TREAT AS PIECEWISE FUNCTION
			 * ELSE         - TREAT AS MATRIX
			 */
			else if (line.contains("<mml:mtable")) {
				// count number of rows and columns;
				boolean firstRow = false;
				int rows = 0, cols = 0;
				int j = i + 1;
				
				if (!insideDiv) {
					// MATRIX
					int nesting = 0;
					while (nesting >= 0) {
						if (lines[j].contains("<mml:mtable")) {
							nesting++;
						} else if (lines[j].contains("</mml:mtable>")) {
							nesting--;
						} else if (lines[j].contains("<mml:mtr")) {
							rows++;
							toAddLater = addMappingDeep(toAddLater, j, "Start Row ");
						} else if (lines[j].contains("<mml:mtd") && !firstRow) {
							cols++;
							
							// handle blank cells
														
						} else if (lines[j].contains("</mml:mtd")) {
							toAddLater = addMappingDeep(toAddLater, j, ", ");
						} else if (lines[j].contains("</mml:mtr>") && !firstRow) {
							firstRow = true;
						}
						j++;
					}
					mathSpeak.append("Start " + rows + " by " + cols + " Matrix ");
				} else {
					// PIECEWISE FUNCTION
					mathSpeak.append("Start Piecewise Function ");
					
					while (!lines[j].contains("</mml:mtable")) {
						if (lines[j].contains("</mml:mtr") && !lines[j+1].contains("</mml:mtable")) {
							toAddLater = addMappingDeep(toAddLater, j, ", or ");
						}
						j++;
					}
				}
			}
			
			/*
			 * END TAGS
			 */
			else if (line.contains("</mml:menclose")) {
				if (endEnclose) {
					mathSpeak.append(", End Modifier ");
				} else {
					endEnclose = true;
				}
			} else if (line.matches("</mml:munder[^o]?")) {
				if (!underBrace) {
					if (endScripts) {
						mathSpeak.append("End Scripts ");
					} else {
						endScripts = true;
					}
				} else {
					underBrace = false;
				}
			} else if (line.contains("</mml:mover")) {
				if (endScripts) {
					mathSpeak.append("End Scripts ");
				} else {
					endScripts = true;
				}
			} else if (line.contains("</mml:munderover") && endScripts) {
				mathSpeak.append("End Scripts ");
			} else if (line.contains("</mml:mfrac>")) {
				mathSpeak.append("End Fraction ");
			} else if (line.contains("</mml:msub>")) {
				mathSpeak.append("Base ");
			} else if (line.contains("</mml:msup>")) {
				mathSpeak.append("Base ");
			} else if (line.contains("</mml:msubsup>")) {
				mathSpeak.append("Base ");
			} else if (line.contains("</mml:mroot>")) {
				mathSpeak.append("End Root ");
			} else if (line.contains("<mml:msqrt")) {
				mathSpeak.append("Start Square Root ");
			} else if (line.contains("</mml:msqrt>")) {
				mathSpeak.append("End Square Root ");
			} else if (line.contains("</mml:mtable>")) {
				if (!insideDiv) {
					mathSpeak.append("End Matrix ");
				} /* else {
					if (mathSpeak.toString().contains("comma") && !mathSpeak.toString().contains("if")) {
						mathSpeak = new StringBuilder(mathSpeak.toString().replaceAll("comma", "if"));
					}
				} */
			}
			
			xml.append(line);
		}
		
		return mathSpeak.toString();
	}
	
	/*
	 * Get the array index of line containing </mml:mrow> to input tag
	 * 
	 * @param lines Array of strings to search through
	 * @param start Array index to start searching at, containing line with initial <mml:mrow>
	 * 
	 * @return The index in input array containing line with closing </mml:mrow>
	 */
	private int getEndRow(int start, String[] lines) {
		int index = start + 1, rowNesting = 1;
		while (rowNesting > 0) {
			if (lines[index].contains("<mml:mrow")) {
				rowNesting++;
			} else if (lines[index].contains("</mml:mrow>")) {
				rowNesting--;
			}
			index++;
		}
		index--;
		return index;
	}
	
	private int getEndTag(int start, String tag, String[] lines) {
		int index = start + 1, tagNesting = 1;
		while (tagNesting > 0) {
			if (lines[index].contains("<" + tag)) {
				tagNesting++;
			} else if (lines[index].contains("</" + tag)) {
				tagNesting--;
			}
			index++;
		}
		index--;
		return index;
	}
	
	public String translateLine(String line) {
		if (line.contains("<mml:mover")) {
			System.out.println("caught over posing as mo");
			return "";
		}
		
		int startContent = line.indexOf('>') + 1;
		int endContent = line.indexOf('<', startContent);
		String result = "";
		
		if (startContent == endContent) {
			// empty tag
			return null;
		} else {
			String tagType = getTagType(line);
			String content = line.substring(startContent, endContent);
			
			switch (tagType) {
				// actual content
			
				// mi and mo require further processing
				case "mi": result = translateIdentifier(content);
						   break;
				case "mo": result = translateOperator(content);
						   break;
						   
				// numbers can be read by screen readers - no translation needed
				case "mn": result = content.replaceAll("&#x(?:00A0|2009|200A);", " ");
						   if (isFirstElement) isFirstElement = false;
						   break;
				// mtext represents raw text - no translation by definition
				case "mtext": result = content.replaceAll("&#x(?:00A0|2009|200A);", " ");
							  if (isFirstElement) isFirstElement = false;
							  break;
			}
		}
		
		return result;
	}

	private String translateIdentifier(String line) {
		if (isFirstElement) {
			isFirstElement = false;
		}
		
		if (line.length() == 1) {
			if (line.matches("[a-z]")) {
				if (line.equals("a")) {
					return "eh";
				}
				// lower case letter
				return line;
			} else if (line.matches("[A-Z]")) {
				if (line.equals("A")) {
					return "Upper eh";
				}
				// upper case letter
				return "Upper " + line;
			} else {
				// some other character - consult translation table
				String res = trans.getTranslation(line);
				
				if (res != null) {
					return res;
				}
			}
		} else {
			// unicode character or function - consult translation table
			String res;
			
			if (line.contains("&#x")) {
				res = line.toUpperCase().substring(3, line.length()-1);
			} else {
				res = line;
			}

			return trans.getTranslation(res);
		}
		
		return null;	// return null if translation can't be found
	}
	
	private String translateOperator(String line) {
		if (line.equals("(") || line.equals("=") || line.equals("[")) {
			isFirstElement = true;
		}
		
		if (line.length() > 1 && line.contains("&#x")) {
			// handle unicode
			line = line.toUpperCase().substring(3, line.length()-1);
			
			// choose between "negative" or "minus", depending on context
			if (line.contains("2212")) {
				if (isFirstElement) {
					isFirstElement = false;
					return "negative";
				}
			}
		}
		
		if (isFirstElement && !line.equals("(") && !line.equals("=") && !line.equals("[")) {
			isFirstElement = false;
		}
		
		return trans.getTranslation(line);
	}
	
	private String checkForUnicodeInText(String line) {
		String text = getContent(line).replace("&#x0026;", "&");
		System.out.println("content: " + text);
		if (text.contains("&#x") && text.contains(";")) {
			System.out.println("matches unicode pattern");
			String codePoint = text.substring(3, text.length()-1);
			int cp = Integer.parseInt(codePoint, 16);
			System.out.println("codepoint: " + codePoint + "; cp: " + cp);
			
			Character.UnicodeBlock block;
			if (Character.isLetterOrDigit(cp)) {
				return line.replaceAll("mml:mtext", "mml:mi").replace("&#x0026;", "&");
			} else {
				return line.replaceAll("mml:mtext", "mml:mo").replace("&#x0026;", "&");
			}
		}
		
		
		return null;
	}
	
	private String getContent(String line) {
		int startContent = line.indexOf('>') + 1;
		int endContent = line.indexOf('<', startContent);
		
		if (startContent == endContent) {
			return null;	// empty tag
		}
		return line.substring(startContent, endContent);
	}
	
	/*
	 * Helper function to add a (K,V) pair to a map, accounting for duplicates.
	 * Maps an Integer to a List<String>.
	 * If a mapping for input (K,V) exists, input string is appended to list at input index.
	 * If no mapping exists, a new mapping is created with a new list containing the input string.
	 * 
	 * @param map A map (Integer -> List<String>)
	 * @param index Integer key to add to input map
	 * @param toAdd String to add to List<> corresponding to map(index)
	 * 
	 * @return The modified Map.
	 */
	private Map<Integer, List<String>> addMappingDeep(Map<Integer, List<String>> map, Integer index, String toAdd) {
		List<String> list;
		
		if (map.containsKey(index)) {
			list = map.get(index);
		} else {
			list = new ArrayList<>();
		}
		list.add(toAdd);
		map.put(index, list);
		
		return map;
	}
	
	public String getTagType(String line) {
		int startTag = line.indexOf(':') + 1,
				endTag = line.indexOf('>');
		return line.substring(startTag, endTag);
	}
	
	public void setInsideDiv(boolean newVal) {
		insideDiv = newVal;
	}
	
	public boolean getInsideDiv() {
		return insideDiv;
	}

}
