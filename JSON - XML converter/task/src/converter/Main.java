package converter;

import converter.logic.ConverterMethod;
import converter.logic.JSONConverter;
import converter.logic.XMLConverter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;


public class Main {
    private Scanner scanner;
    private final ConverterMethod converterMethod;

    public Main() {
        try {
            this.scanner = new Scanner(new File("test.txt"));
        } catch (FileNotFoundException e) {
            System.out.println("no file found");
            this.scanner=null;
        }
        converterMethod = new ConverterMethod();
    }

    public static void main(String[] args) {
        new Main().start();
    }

    private void start() {
        StringBuilder stringBuilder = new StringBuilder();
        String input;
        while (scanner.hasNextLine()){
            input= scanner.nextLine();
            stringBuilder.append(input);

        }
        scanner.close();

        input=stringBuilder.toString();

        if(input.contains("{")){
            input=input.replaceFirst("\\{","")
                    .replaceAll("\\s","");
            converterMethod.setConverter(new JSONConverter());
            converterMethod.getConverter().setInputString(input);
            converterMethod.convertToObject();
            converterMethod.printAsXML();
        }else {
            converterMethod.setConverter(new XMLConverter());
            converterMethod.getConverter().setInputString(input);
            converterMethod.convertToObject();
            converterMethod.printASJSON();
        }
    }
}
