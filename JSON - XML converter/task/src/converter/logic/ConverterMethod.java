package converter.logic;

import converter.domain.Converter;

public class ConverterMethod{
    Converter converter;

    public void setConverter(Converter converter) {
        this.converter = converter;
    }

    public Converter getConverter() {
        return converter;
    }

    public void convertToObject(){
        converter.convertToObject();
    }

    public void printAsXML(){
        converter.printAsXML();
    }

    public void  printASJSON(){
        converter.printASJSON();
    }

}
