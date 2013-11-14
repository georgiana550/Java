package indexing_odd_redbug;
import java.util.Stack;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;

public class HTMLHandler extends ParserCallback {
    Stack<Tag> tag_stack = new Stack<Tag>();

    StringBuffer str = new StringBuffer();

    public void handleText(char[] text, int p) {
        Tag top = tag_stack.lastElement();

        if (top != HTML.Tag.STYLE && top != HTML.Tag.SCRIPT) {
            str.append(text);
        }
    }

    public String getTextOnly() {
        return str.toString();
    }

    public void handleStartTag(Tag tag, MutableAttributeSet att, int p) {
        tag_stack.push(tag);
        str.append("");
    }

  
    public void handleComment(char[] text, int p) {

    }

    public void handleSimpleTag(Tag t, MutableAttributeSet a, int p) {
        str.append("");
    }

    public void handleEndTag(Tag t, int p) {
    	if (!tag_stack.empty()) {
            tag_stack.pop();
        }

        str.append("");    
    }

    public void handleError(String t, int p) {

    }
}