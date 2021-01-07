package converter.logic;


import converter.domain.Attribute;
import converter.domain.Converter;
import converter.domain.Element;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JSONConverter extends Converter {

    @Override
    public void convertToObject() {
        parse(inputString, new Element.Builder().setDepth(0).build());
        addRoot();
    }

    private void addRoot() {
        int x = 0;
        for (Element e : elements) {
            if (e.getDepth() == 1) {
                x++;
            }
        }
        if (x > 1) {
            for (Element e : elements) {
                e.increaseDepth(1);
            }
            Element element1 = new Element.Builder().setDepth(1).setPath("root").setHasChild(true).build();
            add(0, element1);
        }
    }

    private void parse(String input, Element element) {
        Pattern pattern = Pattern.compile("\\{},|\\[],|},|],|[}\\]\\[{]|\"\",|(\"\"],)|" +
                "((\"\\S*?\"):(.*?)(\\{\\s*?},|\\[\\s*?],|},|],|" +
                "[,{}\\[]))|([a-zA-Z0-9.]+?)[]},]");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            String found = matcher.group();
            if (found.equals("}") || found.equals("]") || found.equals("},") || found.equals("],")) { //remove last path
                element.decreaseDepth(1);
                element.setPath("");
            } else {
                String path="";
                if(matcher.group(3)!=null){
                    path = matcher.group(3).replaceAll("\"", "")
                            .replace("#", "")
                            .replace("@", "");
                }
                if (found.endsWith("{") || found.endsWith("[")) {
                    if(element.getPath().equals(path)&&found.startsWith("\"#")&&!path.isEmpty()){
                        element.setHasChild(true);
                        checkElement(element);
                        element = new Element.Builder().setDepth(element.getDepth()).build();
                        if(input.indexOf('}')<input.indexOf(']')){
                            input=input.replaceFirst("}","");
                        }else {
                            input=input.replaceFirst("]","");
                        }
                    }else if(!element.containsAttr(new Attribute(path,""))
                            &&!found.startsWith("\"\":")&&!found.startsWith("\"#\":")) {
                        element.setHasChild(true);
                        checkElement(element);
                        element = new Element.Builder().setDepth(element.getDepth()+1).build();
                        if (found.length() < 2) {
                            element.setPath("element");
                        } else {
                            element.setPath(path);
                        }
                    }else if(element.containsAttr(new Attribute(path,""))
                            ||found.startsWith("\"\"")||found.startsWith("\"#\":")){
                        char c = (char) (found.charAt(found.length()-1)+2);
                        int i = input.indexOf(c);
                        input=input.substring(i+1);
                        if(input.startsWith(",")){
                            input=input.replaceFirst(",","");
                        }
                    }
                } else if (matcher.group(3) != null || matcher.group(6) != null||matcher.group(1)!=null ){
                    if (matcher.group(3) != null && matcher.group(3).length() > 1) {
                        addKeyValue(found, element);
                    } else if (matcher.group(6) != null) {
                        addKeyValue("element:" + matcher.group(6), element);
                    }else if (matcher.group(1) != null) {
                        addKeyValue("element: " , element);
                    }
                    if ((found.endsWith("}") || found.endsWith("]")
                            || found.endsWith("],") || found.endsWith("},"))
                            && (!found.contains("{") && !found.contains("["))) {
                        checkElement(element);
                        element.decreaseDepth(1);
                        element.setPath("");
                    }
                } else if (found.startsWith("{") || found.startsWith("[") || found.startsWith("\"\"") && found.endsWith(",")) {
                    addKeyValue("element:", element);
                }
            }
            found = found.replaceFirst("\\{", "\\\\{").replaceFirst("\\[", "\\\\[");
            input = input.replaceFirst(found, "");
            parse(input, element);
        }
    }

    private void checkElement(Element element) {
        if (!element.getPath().isEmpty() ||
                !element.getAttributes().isEmpty()) {
            Iterator<Attribute> iterator = element.getAttributes().iterator();
            Element element1 = new Element.Builder()
                    .setDepth(element.getDepth()).setPath(element.getPath()).setHasChild(element.hasChild()).build();
            while (iterator.hasNext()) {
                Attribute attribute = iterator.next();
                String key = attribute.getKey();
                String value = attribute.getValue();
                if (attribute.getKey().startsWith("@")) {
                    key = key.replaceFirst("@", "");
                    if (attribute.getValue().equals("null")) {
                        if (key.length() > 0) {
                            element1.addAttr(new Attribute(key, ""));
                        }
                    } else {
                        element1.addAttr(new Attribute(key, attribute.getValue()));
                    }
                    iterator.remove();
                } else if (attribute.getKey().startsWith("#")) {
                    key = key.replaceFirst("#", "");
                    if (key.equals(element1.getPath())) {
                        if (value.equals("null")) {
                            element1.setValue("null");
                        } else {
                            element1.setValue(value);
                        }
                        iterator.remove();
                    } else {
                        iterator.remove();
                        element1.addAttr(new Attribute(key, value));
                    }
                    break;
                } else {
                    addVoidPath(element);
                    elementsFromAttributes(element);
                    element1 = new Element.Builder().build();
                    element=new Element.Builder().build();
                    break;
                }
            }
            if (!element1.getAttributes().isEmpty() || element1.getValue() != null) {  //has value or attr
                if (!element1.getAttributes().isEmpty() && element1.getValue() != null) {  //proper element
                    if (element.getAttributes().isEmpty()) {    //check if good to add if there is no more sub elements
                        if (isVoidAttribute(element1)) {
                            addVoidPath(element1);
                            element1.increaseDepth(1);
                        }
                        add(element1);
                    } else if (!element1.getValue().equals("null")) {      //more to add change change element1 to many elements
                        addVoidPath(element1);
                        elementsFromAttributes(element1);
                        elementsFromAttributes(element);
                    }
                } else if (element1.getAttributes().isEmpty()) {
                    add(element1);
                } else if (!element1.getAttributes().isEmpty()&&!element1.hasChild()) {
                    addVoidPath(element1);
                    elementsFromAttributes(element1);
                }else {
                    add(element1);
                }
            } else if (!element.getPath().isEmpty()) {
                add(new Element.Builder().setHasChild(element.hasChild()).setAttributes(element.getAttributes())
                        .setDepth(element.getDepth()).setPath(element.getPath()).build());
            }
            if (!element.getAttributes().isEmpty()) {
                checkElement(element);
            }
        }
    }

    public void addVoidPath(Element element) {
        Element element1 = new Element.Builder().setDepth(element.getDepth()).setPath(element.getPath())
                .setHasChild(true).build();
        add(element1);
    }

    private boolean isVoidAttribute(Element element) {
        Iterator<Attribute> iterator = element.getAttributes().iterator();
        while (iterator.hasNext()) {
            Attribute attribute = iterator.next();
            String key = attribute.getKey().
                    replaceFirst("@", "").replaceFirst("#", "");
            if (key.isEmpty()) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    private void elementsFromAttributes(Element element) {
        Element element1;
        Iterator<Attribute> iterator = element.getAttributes().iterator();
        while (iterator.hasNext()) {
            Attribute attribute = iterator.next();
            element1 = new Element.Builder().setDepth(element.getDepth()+1)
                    .setPath(attribute.getKey().replace("#", ""))
                    .setValue(attribute.getValue()).build();
            add(element1);
            iterator.remove();
        }
        if(element.getValue()!=null&&!element.getValue().isEmpty()){
            element1 = new Element.Builder().setDepth(element.getDepth()+1)
                    .setPath(element.getPath()).setValue(element.getValue()).build();
            add(element1);
        }
    }

    private void addKeyValue(String input, Element element) {
        input = input.replace("\"", "")
                .replace(",", "").replace("}", "")
                .replace("{", "").replace("[", "")
                .replace("]", "");
        String[] split = input.split(":");
        String key = split[0].trim();
        String value;
        try {
            value = split[1].trim();
        } catch (Exception e) {
            value = "";
        }

        if (key.startsWith("#")) {
            if (key.equals("#")) {
                key = "#" + element.getPath();
                value = "";
            }
        }
        Attribute attribute = new Attribute(key,value);
        if(!key.equals("element")){
            if (!element.containsAttr(attribute)){
                element.addAttr(attribute);
            }
        }else {
            element.addAttr(attribute);
        }

    }
}
