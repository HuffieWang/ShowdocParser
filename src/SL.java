import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.textmining.text.extraction.WordExtractor;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class SL {
    public static final int MODE_NONE = 0;
    public static final int MODE_URL = 1;
    public static final int MODE_REQUEST = 2;
    public static final int MODE_RESPONSE = 3;
    public static final int MODE_GENERATE = 4;

    private static int sMode;

    private static String sUrl;
    private static List<String> sRequestList;
    private static String sResponse;

    public static String run(String path) throws IOException {
        try {

            FileInputStream fileInput = new FileInputStream(path);
            WordExtractor wordExtractor = new WordExtractor();
            String word = wordExtractor.extractText(fileInput);

            List<String> contentList = new ArrayList<>();
            String splitTarget = new String(new char[]{7,7});
            String[] split = word.split("\n");
            for(String s : split){
                if(!s.contains(splitTarget)){
                    contentList.add(s);
                    continue;
                }
                String[] childSplit = s.split(splitTarget);
                contentList.addAll(Arrays.asList(childSplit));
            }
            String result = "";
            reset();
            for(String content : contentList){

                content = content.replaceAll("\r", "");

                if (content.contains("请求URL")){
                    sMode = MODE_URL;
                    continue;

                } else if (content.contains("请求方式")) {
                    sMode = MODE_NONE;
                    continue;

                } else if (content.contains("参数名") && content.contains("必选")) {
                    sMode = MODE_REQUEST;
                    continue;

                } else if (content.contains("返回示例")) {
                    sMode = MODE_RESPONSE;
                    continue;

                } else if (content.contains("返回参数说明")){
                    sMode = MODE_NONE;
                    continue;

                } else if (content.contains(new String("备注"))){
                    sMode = MODE_GENERATE;
                }

                switch (sMode){
                    case MODE_URL:
                        loadUrl(content);
                        break;

                    case MODE_REQUEST:
                        loadRequest(content);
                        break;

                    case MODE_RESPONSE:
                        loadResponse(content);
                        break;

                    case MODE_GENERATE:
                        JSONObject jsonObject = new JSONObject(sResponse);
                        result += process(sUrl, sRequestList, jsonObject.getJSONObject("data"), true);
                        reset();
                        sMode = MODE_NONE;
                        break;
                }
            }
            System.out.println(result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            if(sUrl != null){
                throw new IOException("Failed to parse " + sUrl);
            } else {
                throw new IOException("Please save the file as Word 97-2003(*.doc)");
            }
        }
    }

    private static void loadUrl(String url){
        sUrl = url.replaceAll("\r", "");
    }

    private static void loadRequest(String request){
        sRequestList.add(request);
    }

    private static void loadResponse(String response) {
        sResponse += response;
    }

    private static int customIndex = 0;

    private static String process(String url, List<String> requestList, JSONObject response, boolean isPost) {

        String finalRequest = "";
        String finalResponse = "";

        if(requestList.size() > 0){
            for(int i = 0; i < requestList.size(); i++){
                String s = requestList.get(i);
                String[] split = s.split(new String(new char[]{7}));
                finalRequest = finalRequest + "\"" + split[0] + "\"";
                if(i < requestList.size() - 1){
                    finalRequest = finalRequest + ", ";
                }
            }
        }

        Iterator keys = response.keys();

        while (keys.hasNext()) {
            Object next = keys.next();
            Object object = response.get(next.toString());

            if (object instanceof JSONObject) {
                process(url + "/CustomEntity" + customIndex, new ArrayList<String>(), (JSONObject) object, false);
                finalResponse = finalResponse + "\"" + next + "$" + "CustomEntity" + customIndex++ + "\"";

            } else if (object instanceof JSONArray) {
                JSONArray array = (JSONArray) object;
                process(url + "/CustomEntity" + customIndex, new ArrayList<String>(), (JSONObject) array.get(0), false);
                finalResponse = finalResponse + "\"" + next + "$" + "CustomEntity" + customIndex++ + "[]\"";

            } else {
                finalResponse = finalResponse + "\"" + next + "\"";
            }
            if(keys.hasNext()){
                finalResponse += ", ";
            }
        }
        String methodName = url.substring(url.indexOf("/") + 1);
        String entityName = url.substring(url.lastIndexOf("/") + 1);
        entityName = toUpperCaseFirstOne(entityName);
        String finalEntity = "@MSEntity(name=\"" + entityName + "\", " + "request = {" + finalRequest  + "}, response = {" + finalResponse + "}, post = " + isPost + ")\r\n" ;
        finalEntity = finalEntity + "void " + methodName.replaceAll("/", "_") + "();\r\n\r\n";

        return finalEntity;
    }

    private static void reset() {
        sUrl = "";
        sRequestList = new ArrayList<>();
        sResponse = "";
    }

    public static String toUpperCaseFirstOne(String nameString){
        String namePart1 = nameString.substring(0, 1).toUpperCase();
        String namePart2 = nameString.substring(1);
        return namePart1 + namePart2;
    }
}
