package converter.domain;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

public abstract class Converter {
    protected String inputString;
    protected ArrayList<Element> elements;

    public Converter() {
        inputString = "";
        this.elements = new ArrayList<>();
    }

    public void setInputString(String inputString) {
        this.inputString = inputString;
    }

    public final void add(Element element) {
        if (!element.getPath().isEmpty()) {
            elements.add(element);
        }
    }

    public final void add(int i, Element element) {
        if (!this.elements.contains(element) && !element.getPath().isEmpty()) {
            elements.add(i, element);
        }
    }

    public abstract void convertToObject();

    public final void printAsXML() {
        Deque<String> pathsDeque = new ArrayDeque<>();
        for (Element element : elements) {
            printCloseTag(pathsDeque, element.getDepth());
            if (element.hasChild()) {
                pathsDeque.push(element.getPath());
            }
            System.out.println(tabulatorStr(element.getDepth() - 1) + "<" + element.getPath()
                    + (element.getAttributes().isEmpty() ? "" : "" + element.mapToXMLString() + "")
                    + (element.getValue() == null ? (element.hasChild() ? ">" : "") : element.getValue().equals("null") ?
                    " />" : (">" + element.getValue()) + "</" + element.getPath() + ">"));
        }
        printCloseTag(pathsDeque, 1);
    }

    private void printCloseTag(Deque<String> pathsDeque, int n) {
        while (pathsDeque.size() >= n) {
            System.out.print(tabulatorStr(pathsDeque.size() - 1) + "</" + pathsDeque.poll() + ">\n");
        }
    }

    public final void printASJSON() {
        Deque<String> brackets = new ArrayDeque<>();
        System.out.print("{");
        brackets.push("}");
        Element element1 = new Element.Builder().setHasChild(true).build();
        for (Element element : elements) {
            printClosingBrackets(brackets,element1, element.getDepth());
            System.out.print(tabulatorStr(element.getDepth()) +
                    (element.getPath().equals("element")? "":"\"" + element.getPath() + "\": "));
            if (element.getAttributes().isEmpty()) {
                if (element.hasChild()) {
                    if (element.isArray()) {
                        System.out.print("[");
                        brackets.push("]");
                    }else{
                        System.out.print("{");
                        brackets.push("}");
                    }
                } else {
                    System.out.print(value(element.getValue()));
                }
            } else {
                brackets.push("}");
                System.out.print("{\n" + element.mapToJSONString(element.getDepth() + 1));
                System.out.print(tabulatorStr(element.getDepth() + 1) + "\"#"
                        + element.getPath() + "\": ");
                if (element.getValue() == null && element.hasChild()) {
                    if (element.isArray()) {
                        System.out.print("[");
                        brackets.push("]");
                    }else {
                        System.out.print("{");
                        brackets.push("}");
                    }
                } else {
                    System.out.print(value(element.getValue()));
                }

            }
            element1 = element;
        }
        printClosingBrackets(brackets, new Element.Builder().build(), 0);
    }

    private void printClosingBrackets(Deque<String> brackets, Element e, int n) {
        while ( brackets.size()>n) {
            System.out.print("\n"+tabulatorStr(brackets.size()-1) + brackets.poll());
        }
        if (!e.hasChild()) {
            if (brackets.size()>0) {
                System.out.println(",");
            }
        }else {
            System.out.println();
        }
    }

    private String value(String value) {
        if (value == null) {
            return null;
        }
        if (value.isEmpty()) {
            return "\"\"";
        }
        if (value.equals("null")) {
            return null;
        }
        return "\"" + value + "\"";
    }

    private String tabulatorStr(int n) {
        return "\t".repeat(n);
    }
}
