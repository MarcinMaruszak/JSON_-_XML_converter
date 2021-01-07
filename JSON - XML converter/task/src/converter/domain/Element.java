package converter.domain;

import java.util.ArrayList;
import java.util.Objects;

public class Element {
    private String value;
    private boolean hasChild;
    private ArrayList<Attribute> attributes;
    private int depth;
    private String path;
    private boolean array;


    public Element(String value, boolean hasChild, ArrayList<Attribute> attributes, int depth,
                   String path, boolean array) {
        this.value = value;
        this.hasChild = hasChild;
        this.attributes = attributes;
        this.depth = depth;
        this.path = path;
        this.array = array;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getDepth() {
        return depth;
    }

    public String getPath() {
        return path;
    }

    public void increaseDepth(int i){
        this.depth+=i;
    }

    public void decreaseDepth(int i){
        this.depth-=i;
    }

    public void setAttributes(ArrayList<Attribute> attributes) {
        this.attributes = attributes;
    }

    public ArrayList<Attribute> getAttributes() {
        return attributes;
    }

    public boolean hasChild() {
        return hasChild;
    }

    public void setHasChild(boolean hasChild) {
        this.hasChild = hasChild;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void addAttr(Attribute a){
        this.attributes.add(a);
    }

    public boolean isArray() {
        return array;
    }

    public void setArray(boolean array) {
        this.array = array;
    }

    public boolean containsAttr(Attribute a){
        for (Attribute attr : attributes){
            if(attr.getKey().equals(a.getKey())){
                return true;
            }
        }
        return false;
    }

    public String mapToXMLString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Attribute attribute : attributes) {
            stringBuilder.append(" ").append(attribute.getKey()).append("=\"").append(attribute.getValue()).append("\"");
        }
        return stringBuilder.toString();
    }

    public String mapToJSONString(int n) {
        String tab = "\t";
        StringBuilder stringBuilder = new StringBuilder();
        for (Attribute attribute : attributes) {
            stringBuilder.append(tab.repeat(n)).append("\"@").append(attribute.getKey()).append("\": \"").
                    append(attribute.getValue()).append("\",\n");
        }
        return stringBuilder.toString();
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Element:\n")
                .append("path = ").append(path).append("\n")
                .append("depth = ").append(depth)
                .append(value != null ? (value.equals("null") ? "\nvalue = null" : "\nvalue = \"" + value + "\"") : "")
                .append("\n")
                .append(attributes.isEmpty() ? "" : "attributes:\n");
        for (Attribute attribute : attributes) {
            stringBuilder.append(attribute.getKey()).append(" = \"").append(attribute.getValue()).append("\"\n");
        }
        return stringBuilder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Element element = (Element) o;
        return depth == element.depth && Objects.equals(path, element.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(depth, path);
    }

    public static class Builder{
        private String value;
        private boolean hasChild =false;
        private ArrayList<Attribute> attributes =new ArrayList<>();
        private int depth =0;
        private String path ="";
        private boolean array;

        public Builder() {
        }

        public Builder setValue(String value){
            this.value=value;
            return this;
        }

        public Builder setHasChild(boolean hasChild){
            this.hasChild=hasChild;
            return this;
        }

        public Builder setAttributes(ArrayList<Attribute> attributes) {
            this.attributes = attributes;
            return this;
        }

        public Builder setDepth(int depth) {
            this.depth = depth;
            return this;
        }

        public Builder setPath(String path) {
            this.path = path;
            return this;
        }

        public Builder setArray(boolean array){
            this.array = array;
            return this;
        }

        public Element build(){
            return new Element(value,hasChild,attributes,depth,path,array);
        }
    }

}
