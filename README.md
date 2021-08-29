# MathSpeaker

My attempt to replace a slow, web-based tool with a faster and more flexible local version. The previous solution made use of the MathJax library hosted on an AWS instance, and was incredibly slow for the large document processing we needed to do. This program needed to parse an XML document containing large numbers of embedded MathML equations, translate the MathML to English sentences, then output an XHTML document compatible with screen reading software.

This solved a lot of issues we were having with more complex equations. In math texts with lots of these complexities, a ton of repetitive manual editing was required, driving me to come up with a better solution.

Another constant pain point was the generation of SVG images of equations. Using a library called SVGmath this tool can also export vector images of every equation. This ended up being significantly faster than the previous MathJax solution.
