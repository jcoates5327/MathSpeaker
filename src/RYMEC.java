public class RYMEC {
	public RYMEC() {
	
	}
	
	public String runRYMEC(String data) {
		String out = data.replaceAll("\t", "")
						 .replaceAll("([^\"]) xml:lang=\"(.){1,15}\">", "$1>")
						 .replaceAll("<p ?/>", "")
						 .replaceAll("<author.*?>", "<author>")
						 // MathML
						 .replaceAll("<mml:m(ath|row|i|o|text|n|frac) .*?>", "<mml:m$1>")
						 .replaceAll("<mml:mtext>&#x(?:2009|200B);</mml:mtext>", "")
						 //.replaceAll("<mml:mstyle.*?>", "")
						 //.replaceAll("</mml:mstyle>", "")
						 .replaceAll(" mathsize='normal'", "")
				   		 .replaceAll("<mml:mtext>\\s*(&#x00A0;)?tan(&#x00A0;)?\\s*</mml:mtext>", "<mml:mo>tan</mml:mo>")
				   		 .replaceAll("<mml:mtext>\\s*(&#x00A0;)?sin(&#x00A0;)?\\s*</mml:mtext>", "<mml:mo>sin</mml:mo>")
				   		 .replaceAll("<mml:mtext>\\s*(&#x00A0;)?cos(&#x00A0;)?\\s*</mml:mtext>", "<mml:mo>cos</mml:mo>")
				   		 .replaceAll("<mml:mtext>\\s*(&#x00A0;)?sec(&#x00A0;)?\\s*</mml:mtext>", "<mml:mo>sec</mml:mo>")
				   		 .replaceAll("<mml:mtext>\\s*(&#x00A0;)?csc(&#x00A0;)?\\s*</mml:mtext>", "<mml:mo>csc</mml:mo>")
				   		 .replaceAll("<mml:mtext>\\s*(&#x00A0;)?cot(&#x00A0;)?\\s*</mml:mtext>", "<mml:mo>cot</mml:mo>")
				   		 .replaceAll("<mml:mtext>\\s*(&#x00A0;)?log(&#x00A0;)?\\s*</mml:mtext>", "<mml:mo>log</mml:mo>")
				   		 .replaceAll("<mml:mtext>\\s*(&#x00A0;)?ln(&#x00A0;)?\\s*</mml:mtext>", "<mml:mo>ln</mml:mo>")
				   		 .replaceAll("<mml:mtext>\\s*(&#x00A0;)?tanh(&#x00A0;)?\\s*</mml:mtext>", "<mml:mo>tanh</mml:mo>")
				   		 .replaceAll("<mml:mtext>\\s*(&#x00A0;)?tanh(&#x00A0;)?\\s*</mml:mtext>", "<mml:mo>tanh</mml:mo>")
				         .replaceAll("<mml:mtext>\\s*(&#x00A0;)?sinh(&#x00A0;)?\\s*</mml:mtext>", "<mml:mo>sinh</mml:mo>")
				         .replaceAll("<mml:mtext>\\s*(&#x00A0;)?cosh(&#x00A0;)?\\s*</mml:mtext>", "<mml:mo>cosh</mml:mo>")
				         .replaceAll("<mml:mtext>\\s*(&#x00A0;)?sech(&#x00A0;)?\\s*</mml:mtext>", "<mml:mo>sech</mml:mo>")
				         .replaceAll("<mml:mtext>\\s*(&#x00A0;)?csch(&#x00A0;)?\\s*</mml:mtext>", "<mml:mo>csch</mml:mo>")
				         .replaceAll("<mml:mtext>\\s*(&#x00A0;)?coth(&#x00A0;)?\\s*</mml:mtext>", "<mml:mo>coth</mml:mo>")
				         .replaceAll("<mml:mtext>\\s*(&#x00A0;)?lim(&#x00A0;)?\\s*</mml:mtext>", "<mml:mo>lim</mml:mo>")
				         .replaceAll("<mml:m(?:i|o)>(sin|cos|tan|csc|sec|cot|sinh|cosh|tanh|csch|sech|coth|&#x225F;)</mml:m(?:i|o)>",
				        		 "<mml:mo>$1</mml:mo>")
				         .replaceAll("<mml:mi>mod</mml:mi>", "<mml:mi>mod</mml:mi><mml:mtext>&#x00A0;</mml:mtext>")
				         .replaceAll("<mml:mi>lim</mml:mi>", "<mml:mi>lim</mml:mi><mml:mtext>&#x00A0;</mml:mtext>")
				         .replaceAll("<mml:math >", "<mml:math>")
				         .replaceAll("<mml:math display='block' ?>", "<mml:math>")
				         .replaceAll("<mml:mrow/>", "<mml:mrow></mml:mrow>")
				         .replaceAll("<mml:mtable.*?>", "<mml:mtable>")
				         .replaceAll("<mml:mtr.*?>", "<mml:mtr>")
				         .replaceAll("<mml:mtd.*?>", "<mml:mtd>")
				         .replaceAll("<mml:mfrac >", "<mml:mfrac>")
				         .replaceAll("<mml:malignmark>", "<mml:mrow>")
				         .replaceAll("</mml:malignmark>", "</mml:mrow>")
				         .replaceAll("<mml:malignmark/>", "")
				         //.replaceAll("&#x00A0;</mml:mtext>", "</mml:mtext><mml:mspace width=\"0.2em\"/>")
				         //.replaceAll("<mml:mtext>&#x00A0;", "<mml:mspace width=\"0.2em\"/><mml:mtext>")
				         .replaceAll("<mml:msup>\\s*<mml:mo>(?:&#x00A0;| )</mml:mo>", "<mml:msup><mml:mtext>&#x00A0;</mml:mtext>")
				         .replaceAll("<mml:mo>(?:&#x00A0;| )</mml:mo>", "")
				         //.replaceAll("<mml:mtext.*?>&#x00A0;</mml:mtext>", "<mml:mspace width=\"0.2em\"/>")
				         // TABLES
				   		 .replaceAll("<td ?>", "<td>")
				   		 .replaceAll("</td ?>", "</td>")
				   		 .replaceAll("<td ?>\\s*<div>", "<th>")
				   		 .replaceAll("<td colspan=\"(\\d+)\" ?>\\s*<div>", "<th colspan=\"$1\">")
				   		 .replaceAll("<td rowspan=\"(\\d+)\" ?>\\s*<div>", "<th rowspan=\"$1\">")
				   		 .replaceAll("</div>\\s*</td ?>", "</th>")
				   		 .replaceAll("<td>\\s*<p/>\\s*</td>", "<td><p class=\"speak\">blank</p></td>")
				   		 .replaceAll("<th>\\s*<p/>\\s*</th>", "<th><p class=\"speak\">blank</p></th>")
				   		 // MISC
				   		 .replaceAll("<a href=\"#bookmark\\d+\">(.*?)</a>", "$1")
				   		 .replaceAll("<a id=\"bookmark\\d+\">(.*?)</a>", "$1")
				   		 .replaceAll("(\\w)</em>(\\w)", "$1</em> $2")
				   		 .replaceAll("&#x0027;", "&#x2032;")
				   		 .replaceAll("(?:\\u2018|\\u2019|�|�|&#x2018;|&#x2019;)", "'")
				   		 .replaceAll("(?:\\u201C|\\u201D|�|�|&#x201C;|&#x201D;|&#x0022;)", "\"")
				   		 .replaceAll(" ,", ",")
				   		 .replaceAll(" ;", ";")
				   		 .replaceAll(" \\.", "\\.")
				   		 .replaceAll(" </em> ", "</em> ")
				   		 .replaceAll(" </strong> ", "</strong> ")
				   		 .replaceAll("<span.*?>", "")
				   		 .replaceAll("</span>", "")
				   		 .replaceAll("<mml:mtd>\\s*<mml:mrow></mml:mrow>\\s*</mml:mtd>",
				   				 "<mml:mtd><mml:mrow><mml:mi>&#x3000;</mml:mi></mml:mrow></mml:mtd>")
				   		 /*
				   		 .replaceAll("<span class=.*?>", "<span>")
				   		 .replaceAll("<span>\\s*<em>(.*?)</em>\\s*<em>(.*?)</em>(.*?)?\\s*</span>", "<span><em>$1$2</em>$3</span>")
				   		 .replaceAll(" </span>\\s*<span>(\\w)", " $1")
				   		 .replaceAll("(\\w)</span>\\s*<span>(\\w)", "$1$2")
				   		 .replaceAll("(\\w) </span>(\\w)", "$1 $2")
				   		 .replaceAll("\\s*</?span>", "")	// suck it
				   		 .replaceAll("<span class=\"color:#\\d+\">(.*?)</span>", "$1") */
				   		 //.replaceAll("(\\.|,|;)([a-zA-Z])", "$1 $2")
				   		 //.replaceAll("<img(.*?)\\. (\\w.*?>)", "<img$1\\.$2")
				   		 .replaceAll("<mml:mtext>((?:(?:(?:\\d{1,3})(?:,\\d{3})*)|(?:\\d+))(?:\\.\\d+)?)</mml:mtext>\\s*<mml:mtext>(\\.\\d+)</mml:mtext>", 
				   				 "<mml:mtext>$1$2</mml:mtext>")
				   		 .replaceAll("\">Page </pagenum>(.*?)</pagenum>", "\">Page $1</pagenum>")
				   		 .replaceAll("(\\w)<mml:math", "$1 <mml:math")
				   		 .replaceAll("(,|:)<mml:math", "$1 <mml:math")
				   		 .replaceAll("</mml:math>(\\w)", "</mml:math> $1")
				   		 .replaceAll("\\( <mml:math", "\\(<mml:math")
				   		 .replaceAll("</mml:math> \\)", "</mml:math>\\)")
				   		 .replaceAll("<(?:strong|em) ?/>", "");
				   		 //.replaceAll("<p ?>Page(.*?)</p>", "<pagenum page=\"special\" id=\"page1\">Page$1</pagenum>");
		
		// turn abbreviations into MathML
		/*
		out = out.replaceAll("(<p>| )((((\\d{1,3})(,\\d{3})*)|(\\d+))(\\.\\d+)?) ?hr(\\.|,|;)?( |</p>)", "<mml:math><mml:mrow><mml:mn>$1</mml:mn><mml:mtext>&#x00A0;</mml:mtext><mml:mi>h</mml:mi><mml:mi>r</mml:mi></mml:mrow></mml:math>");
		out = out.replaceAll("(<p>| )((((\\d{1,3})(,\\d{3})*)|(\\d+))(\\.\\d+)?) ?ft(\\.|,|;)?( |</p>)", "<mml:math><mml:mrow><mml:mn>$1</mml:mn><mml:mtext>&#x00A0;</mml:mtext><mml:mi>f</mml:mi><mml:mi>t</mml:mi></mml:mrow></mml:math>");
		out = out.replaceAll("(<p>| )((((\\d{1,3})(,\\d{3})*)|(\\d+))(\\.\\d+)?) ?km(\\.|,|;)?( |</p>)", "<mml:math><mml:mrow><mml:mn>$1</mml:mn><mml:mtext>&#x00A0;</mml:mtext><mml:mi>k</mml:mi><mml:mi>m</mml:mi></mml:mrow></mml:math>");
		out = out.replaceAll("(<p>| )((((\\d{1,3})(,\\d{3})*)|(\\d+))(\\.\\d+)?) ?mi(\\.|,|;)?( |</p>)", "<mml:math><mml:mrow><mml:mn>$1</mml:mn><mml:mtext>&#x00A0;</mml:mtext><mml:mi>m</mml:mi><mml:mi>i</mml:mi></mml:mrow></mml:math>");
		out = out.replaceAll("(<p>| )((((\\d{1,3})(,\\d{3})*)|(\\d+))(\\.\\d+)?) ?in(\\.|,|;)?( |</p>)", "<mml:math><mml:mrow><mml:mn>$1</mml:mn><mml:mtext>&#x00A0;</mml:mtext><mml:mi>i</mml:mi><mml:mi>n</mml:mi></mml:mrow></mml:math>");
		*/
		
		return out;
	}

}