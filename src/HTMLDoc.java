public class HTMLDoc {
	private StringBuilder content;
	
	public HTMLDoc() {
		content = new StringBuilder();
	}
	
	public void append(String data) {
		content.append(data);
	}
	
	public String toString() {
		return content.toString();
	}
	
	public void indent(int indent) {
		for (int i = 1; i <= indent; i++) {
			content.append("\t");
		}
	}
}