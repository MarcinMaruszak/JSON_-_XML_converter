package converter.logic;

import converter.domain.Attribute;
import converter.domain.Converter;
import converter.domain.Element;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XMLConverter extends Converter {

    @Override
    public void convertToObject() {
        parse(inputString, new Element.Builder().build());
    }

    private void parse(String input, Element element) {

        Pattern pattern1 = Pattern.compile("((<[/]?(\\w+)[ ]?(.*?)>)(.*?))<");
        Matcher matcher1 = pattern1.matcher(input);
        if (matcher1.find()) {
            String path = matcher1.group(3);
            if (matcher1.group().startsWith("</")) {
                element.decreaseDepth(1);
                input = input.replaceFirst("</" + path + ">", "").trim();
            } else {
                element.increaseDepth(1);
                element.setPath(path);
                if(path.contains("array")){
                    element.setArray(true);
                }
                add(element);
                if (matcher1.group(4) != null && !matcher1.group(4).isEmpty()) {
                    parseAttributes(matcher1.group(4), element);
                    if (matcher1.group(4).endsWith("/")) {
                        element.setValue("null");
                    }
                }
                if (!matcher1.group(5).trim().isEmpty()) {
                    element.setValue(matcher1.group(5));
                }
                try {
                    String subInput = input.substring(matcher1.end(2), input.indexOf("</" + path + ">"));
                    String pathElement= path.substring(0,path.length()-1);
                    if(input.contains("<"+pathElement+">")){
                        /*input=input.replaceAll("<"+pathElement+">","<element>").
                                replaceAll("</"+pathElement+">","</element>");*/
                        String subInput1=subInput.replaceAll("<"+pathElement+">","<element>")
                                .replaceAll("</"+pathElement+">","</element>");
                        input = input.replace(subInput, subInput1);
                        element.setArray(true);
                    }
                    if(isArray(subInput)){
                        element.setArray(true);
                    }
                    if (subInput.contains("<") && !"null".equals(element.getValue())) {
                        element.setHasChild(true);
                    } else if (subInput.isEmpty()) {
                        element.setValue("");
                    }
                    if (element.hasChild() && !element.getAttributes().isEmpty()) {
                        parse(subInput.trim(), new Element.Builder().setDepth(element.getDepth() + 1).build());
                        input = input.replace(subInput.trim(), "");
                    }
                } catch (Exception ignored) { }
                input = input.replaceFirst(matcher1.group(1), "");
            }
            if (("null").equals(element.getValue())) {
                parse(input, new Element.Builder().setDepth(element.getDepth() - 1).build());
            } else {
                parse(input, new Element.Builder().setDepth(element.getDepth()).build());
            }
        }
    }

    private void parseAttributes(String s, Element element) {
        Pattern pattern = Pattern.compile("(\\w+?[ ]?=[ ]?[\"'].*?[\"'])");
        Matcher matcher = pattern.matcher(s);
        while (matcher.find()) {
            String[] array = matcher.group(1).split("=");
            element.addAttr(new Attribute(array[0].trim(),
                    array[1].replaceAll("\"", "")
                            .replaceAll("'", "").trim()));
        }
    }

    private boolean isArray(String input){
        Pattern pattern = Pattern.compile("<[/]?(.+?)>");
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()){
            if(!matcher.group(1).equals("element")){
                return false;
            }
        }
        return true;
    }
}

