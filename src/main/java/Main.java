import java.io.*;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.util.ArrayList;


public class Main {

    private static final String CLASS = "class";
    private static final String CLASS_NAMES = "classNames";
    private static final String IDENTIFIER = "identifier";
    private static final String CONTENT_VIEW = "contentView";
    private static final String SUBVIEWS = "subviews";
    private static final String CONTROL = "control";


    public static void main(String[] args) {
        if(args.length != 1){
            System.out.println("Invalid number of arguments, please run with passing in JSON file");
            return;
        }

        File file = new File(args[0]);
        if(!file.exists()){
            System.out.println("File entered does not exist");
            return;
        }

        String line;
        StringBuilder fileContents = new StringBuilder();

        try{
            FileReader fr = new FileReader(file.getAbsolutePath());
            BufferedReader br = new BufferedReader(fr);

            while((line = br.readLine()) != null){
                fileContents.append(line);
            }

            br.close();
            fr.close();
        } catch (FileNotFoundException fnfe){
            fnfe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        JsonReader reader = Json.createReader(new StringReader(fileContents.toString()));
        JsonObject jsonObject = reader.readObject();

        System.out.println("JSON file loaded. Enter a selector to match on or enter 'exit' to quit.");


        while(true){
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
            try{
                String userInput = inputReader.readLine();
                if(userInput.equals("exit") || userInput.equals("'exit'")){
                    return;
                }

                ArrayList<JsonObject> views;
                if(userInput.matches("[A-Z].*")){
                    views = filter(CLASS, userInput, jsonObject);
                } else if(userInput.matches("\\..*")){
                    views = filter(CLASS_NAMES, userInput.substring(1), jsonObject);
                } else if(userInput.matches("#.*")){
                    views = filter(IDENTIFIER, userInput.substring(1), jsonObject);
                } else {
                    System.out.println("Invalid selector format. Use one of the following formats: 'SelectorForClass', '.selectorForClassNames', '#selectorForIdentifier'");
                    views = new ArrayList<JsonObject>();
                }

                System.out.println(views.size() + " matching views: ");
                for(JsonObject obj : views){
                    System.out.println(obj.toString());
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    private static ArrayList<JsonObject> filter (String selectingField, String selector, JsonObject jsonObject){
        System.out.println(selector);
        ArrayList<JsonObject> matches = new ArrayList<JsonObject>();
        ArrayList<JsonObject> objectsToInspect = new ArrayList<JsonObject>();
        objectsToInspect.add(jsonObject);
        while(objectsToInspect.size() > 0){
            JsonObject inspecting = objectsToInspect.remove(0);
            if(selectingField.equals(CLASS_NAMES)){
                if(inspecting.containsKey(selectingField)){
                    JsonArray arr = inspecting.getJsonArray(selectingField);
                    int size = arr.size();
                    for(int i = 0; i < size; i++){
                        String val = arr.getString(i);
                        if(val.equals(selector)){
                            matches.add(inspecting);
                        }
                    }
                }
            } else {
                if(inspecting.containsKey(selectingField)){
                    if(inspecting.getString(selectingField).equals(selector)){
                        matches.add(inspecting);
                    }
                }
            }

            if(inspecting.containsKey(CONTENT_VIEW)){
                JsonArray arr = inspecting.getJsonObject(CONTENT_VIEW).getJsonArray(SUBVIEWS);
                for(JsonValue val: arr) {
                    JsonObject obj = (JsonObject) val;
                    objectsToInspect.add(obj);
                }
            }

            if(inspecting.containsKey(SUBVIEWS)){
                JsonArray arr = inspecting.getJsonArray(SUBVIEWS);
                for(JsonValue val: arr) {
                    JsonObject obj = (JsonObject) val;
                    objectsToInspect.add(obj);
                }
            }

            if(inspecting.containsKey(CONTROL)){
                objectsToInspect.add(inspecting.getJsonObject(CONTROL));
            }

        }
        return matches;
    }
}

