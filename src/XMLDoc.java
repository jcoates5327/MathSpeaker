public final class XMLDoc {
	private static XMLDoc instance = null;
	private static StringBuilder content;
	
	private XMLDoc() {
		content = new StringBuilder();
	}
	
	public static XMLDoc getInstance() {
		if (instance == null) {
			instance = new XMLDoc();
		}
		return instance;
	}
	
	public static void append(String data) {
		content.append(data);
	}
	
	public static String getString() {
		return content.toString();
	}
	
	public static void indent(int indent) {
		for (int i = 1; i <= indent; i++) {
			content.append("\t");
		}
	}
}