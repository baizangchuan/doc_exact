package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

public class InfoExtracter {

    //提取input中的content
    public static String getContentFromInputJsonDir(String dir) throws IOException {
        String content;
        String final_content;
        String x_content;
        String jsonData = new String(Files.readAllBytes(Paths.get(dir)));
        JSONObject jsonObject = new JSONObject(jsonData);
        content = jsonObject.getJSONObject("args").getString("content");
        final_content = content.replaceAll("\u3000", "");
        x_content= final_content.replaceAll("\\u0020", "");
        return x_content;
    }
    //判断doc_type
    public static String Get_docType(String content){
        String doc_type="";
        int match_num=0; //用来检测是否属于多种文书类型

        if(content.contains("入院记录")){
            doc_type="admit_info";
            match_num+=1;
        }
        if(content.contains("病程记录")&&!(content.contains("首次病程记录"))&&!(content.contains("术后病程记录"))){
            doc_type="case_info";
            match_num+=1;
        }
        if(content.contains("出院记录")){
            doc_type="discharge_info";
            match_num+=1;
        }

        if(content.contains("首次病程记录")){
            doc_type="first_case_info";
            match_num+=1;
        }
        if(content.contains("知情同意书")||content.contains("知情书")){
            doc_type="inform_info";
            match_num+=1;
        }
        if(content.contains("手术记录")){
            doc_type="operation_info";
            match_num+=1;
        }
        if(content.contains("术后病程记录")){
            doc_type="postoperative_first_case_info";
            match_num+=1;
        }

        System.out.println(match_num);
        return doc_type;
    }

    //确保rawSegKeySet在content都存在，去除掉在content中找不到的RawSeg_item
    public static List<String> Get_Effective_RawSegKeySet(List<String> rawSegKeySet, String content){
        List<String> Effective_rawSegKeySet = new ArrayList<>();
        for(String RawSeg_item:rawSegKeySet){
            if (content.contains(RawSeg_item)){
                Effective_rawSegKeySet.add(RawSeg_item);
            }
        }
        return Effective_rawSegKeySet;
    }

    //从格式文档中提取rawSeg_key_set，即所有待匹配的关键词
    public static List<String> getRawSegKeySetFromFormatGuideJson(String formatGuideJsonDir) throws IOException {
        List<String> rawSegKeySet = new ArrayList<>();
        String jsonData = new String(Files.readAllBytes(Paths.get(formatGuideJsonDir)));
        JSONObject jsonObject = new JSONObject(jsonData);
        JSONArray rawSeg = jsonObject.getJSONObject("data").getJSONObject("result").getJSONArray("raw_seg");
        for (int i = 0; i < rawSeg.length(); i++) {
//            rawSegKeySet.add(rawSeg.getJSONArray(i).getString(0));
            String rawString = rawSeg.getJSONArray(i).getString(0);
            String stringWithoutFullWidthSpace = rawString.replaceAll("\\u0020", "");
            rawSegKeySet.add(stringWithoutFullWidthSpace);
        }
        return rawSegKeySet;
    }

    //#在inputString中，提取startMarker和endMarker之间的内容
    public static String extractContent(String inputString, String startMarker, String endMarker) {
        Pattern pattern = Pattern.compile(startMarker + "(.*?)" + endMarker, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(inputString);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return "";
        }
    }

    //#从文本a中删除文本b及其之前的内容
    public static String removeTextBefore(String textA, String textB) {
        int index = textA.indexOf(textB);
        if (index != -1) {
            return textA.substring(index + textB.length());
        } else {
            return textA;
        }
    }


    //由原文和关键字集合，得到每个关键字对应的字段
    public static List<List<String>> getRawSeg(String content, List<String> rawSegKeySet) {
        List<List<String>> rawSeg = new ArrayList<>();
        String inputString = content;
        for (int i = 0; i < rawSegKeySet.size() - 1; i++) {
            String startMarker = rawSegKeySet.get(i);
            // String startMarker = (i == 0) ? "" : rawSegKeySet.get(i);
            String endMarker = rawSegKeySet.get(i + 1);

            String result = extractContent(inputString, startMarker, endMarker);
            if (!(result == null || result.isEmpty())){
                result=result.trim().replace("\t", "").replace("\n", "").replace("\r", "").replaceAll("\u00A0", "");
            }
            String clean_result=Template.RemoveExtraneousContent(startMarker,result);//对每个模板做精细调整
            List<String> segPair = new ArrayList<>();
            segPair.add(startMarker);
            segPair.add(clean_result);

            rawSeg.add(segPair);
            if (!(result == null || result.isEmpty())){
                inputString = removeTextBefore(inputString, result);
            }
            else{
                inputString = removeTextBefore(inputString, startMarker);
            }
            // System.out.println(segPair);
        }
        // 处理最后一个关键字
        String finalKey = rawSegKeySet.get(rawSegKeySet.size() - 1);
        int index = inputString.indexOf(finalKey);
        if (index + finalKey.length() <= inputString.length()) {
            String result = inputString.substring(index + finalKey.length()).trim().replace("\t", "").replaceAll("\u00A0", "");
            List<String> segPair = new ArrayList<>();
            segPair.add(finalKey);
            segPair.add(result);
            rawSeg.add(segPair);
        }
        else{
            String result = inputString.substring(index+1).trim().replace("\t", "").replaceAll("\u00A0", "");
            List<String> segPair = new ArrayList<>();
            segPair.add(finalKey);
            segPair.add(result);
            rawSeg.add(segPair);
        }

        return rawSeg;
    }

    //从目标输出文件中，找出normResult格式参考列表
    public static List<List<String>> getNormResultGuideFromFormatGuideJson(String formatGuideJson) throws IOException {
        List<List<String>> normResultGuide = new ArrayList<>();
        String jsonData = new String(Files.readAllBytes(Paths.get(formatGuideJson)));
        JSONObject jsonObject = new JSONObject(jsonData);
        JSONObject normResult = jsonObject.getJSONObject("data").getJSONObject("result").getJSONObject("norm_result");
        for (String itemName : normResult.keySet()) {
            List<String> resultItem = new ArrayList<>();
            resultItem.add(itemName);
            JSONArray normResultItemList = normResult.getJSONArray(itemName);
            for (int i = 0; i < normResultItemList.length(); i++) {
                JSONObject normResultItem = normResultItemList.getJSONObject(i);
                resultItem.add(normResultItem.getString("key"));
            }
            normResultGuide.add(resultItem);
        }
        return normResultGuide;
    }

    public static String replaceUnicode(String input) {
        if (!(input == null || input.isEmpty())){
            return input.replaceAll("\\u0004", " ")
                    .replaceAll("\\u201c", "“")
                    .replaceAll("\\u201d", "”")
                    .replaceAll("第[%pageindex%]页", "");
        }
        else{
            return input;
        }
    }

    // public static void makeNormResultItem(
    //         JSONObject normResult,
    //         String itemName,
    //         List<List<String>> rawSeg,
    //         List<String> rawSegItemKey) {
    //     JSONArray normResultItemList = new JSONArray();
    //     List<JSONObject> other_List = new ArrayList<>();

    //     // rawSegItemKey不包含other键, itemName中包含other
    //     for (String key : rawSegItemKey) {
    //         boolean notFound = true;
    //         // 对key的空格进行处理
    //         String mid_key = key.replaceAll("\u3000", "");
    //         String final_key = mid_key.replaceAll("\\u0020", "");
    //         for (List<String> rawSegItem : rawSeg) {
    //             if (final_key.equals(rawSegItem.get(0))) {
    //                 JSONObject normResultItem = new JSONObject();
    //                 JSONObject sub = new JSONObject();
    //                 if (final_key.equals("入院记录") || final_key.equals("科别：") || final_key.equals("床号：") || final_key.equals("病案号：") || final_key.equals("出院诊断：")) {
    //                     sub.put("value", rawSegItem.get(1));
    //                     sub.put("key", final_key);
    //                     other_List.add(sub);
    //                 }
    //                 if (final_key.equals("检查") || final_key.equals("专科情况")) {
    //                     List<String> keyList = new ArrayList<>(Arrays.asList("T","P","R","BP"));
    //                     String endWord = "生命征";
    //                     List<JSONObject> keyMetrics = InfoExtracter.subExtract(rawSeg, "检查", keyList, endWord);
    //                     normResultItem.put("key_metrics", keyMetrics);
    //                     normResultItem.put("parse_result", new JSONObject());
    //                 }
    //                 normResultItem.put("value", replaceUnicode(rawSegItem.get(1)));
    //                 normResultItem.put("key", final_key);
    //                 normResultItemList.put(normResultItem);
    //                 notFound = false;
    //                 break;
    //             }
    //         }
    //         // Handle the case where the key is not found
    //         if (notFound) {
    //             JSONObject normResultItem = new JSONObject();
    //             normResultItem.put("value", "");
    //             normResultItem.put("key", key);
    //             normResultItemList.put(normResultItem);
    //         }
    //     }
    //     if (itemName.equals("other")) {
    //         normResult.put(itemName, other_List);
    //     } else {
    //         normResult.put(itemName, normResultItemList);
    //     }
    // }
    public static void makeNormResultItem(
            JSONObject normResult,
            String schema,
            List<List<String>> rawSeg,
            List<String> rawSegItemKey) {
        JSONArray normResultItemList = new JSONArray();

        // rawSegItemKey不包含other键, schema中包含other
        for (String key : rawSegItemKey) {
            // System.err.println("---key:");
            // System.err.println(key);
            boolean notFound = true;
            // 对key的空格进行处理
            String final_key = key.replaceAll("\u3000", "").replaceAll("\\u0020", "");

            for (List<String> rawSegItem : rawSeg) {
                if (final_key.equals(rawSegItem.get(0))) {
                    JSONObject normResultItem = new JSONObject();

                    normResultItem.put("value", replaceUnicode(rawSegItem.get(1)));
                    normResultItem.put("key", final_key);
                    normResultItemList.put(normResultItem);
                    notFound = false;
                    break;
                }
            }
            // Handle the case where the key is not found
            if (notFound) {
                JSONObject normResultItem = new JSONObject();
                normResultItem.put("value", "");
                normResultItem.put("key", key);
                normResultItemList.put(normResultItem);
            }
        }
        normResult.put(schema, normResultItemList);
    }

    // 还没测试能不能用
    public static void makeNormResultItem_from_GuideTree(
            JSONObject normResult,
            Object schema_obj,
            List<List<String>> rawSeg,
            List<Object> rawSegItemKey_list) {
        // List<List<String>> a=(List<List<String>>) rawSegItemKey_list;
        String schema=(String)schema_obj;

        JSONArray normResultItemList = new JSONArray();
        // System.out.println(schema);
        // System.out.println(rawSegItemKey_list);
        // System.out.println("");
        for (Object rawSegItemKey_obj:rawSegItemKey_list){
            List<String>rawSegItemKey=(List<String>)rawSegItemKey_obj;
            for (String key : rawSegItemKey) {
                // System.err.println("---key:");
                // System.err.println(key);
                boolean notFound = true;
                // 对key的空格进行处理
                String final_key = key.replaceAll("\u3000", "").replaceAll("\\u0020", "");

                for (List<String> rawSegItem : rawSeg) {
                    if (final_key.equals(rawSegItem.get(0))) {
                        JSONObject normResultItem = new JSONObject();

                        normResultItem.put("value", replaceUnicode(rawSegItem.get(1)));
                        normResultItem.put("key", final_key);
                        normResultItemList.put(normResultItem);
                        notFound = false;
                        break;
                    }
                }
                // Handle the case where the key is not found
                if (notFound) {
                    JSONObject normResultItem = new JSONObject();
                    normResultItem.put("value", "");
                    normResultItem.put("key", key);
                    normResultItemList.put(normResultItem);
                }
            }
        }



        normResult.put(schema, normResultItemList);
    }
    //由schema_tree_list列表得到normResult
    public static JSONObject getNormResultFromSchemaTreeList(List<List<Object>> normResultGuide, List<List<String>> rawSeg) {
        JSONObject normResult = new JSONObject();
        for (List<Object> normResultGuideItem : normResultGuide) {
            makeNormResultItem_from_GuideTree(normResult, normResultGuideItem.get(0), rawSeg, normResultGuideItem.subList(1, normResultGuideItem.size()));
        }
        return normResult;
    }

    //由normResult格式参考列表得到normResult
    public static JSONObject getNormResultFromNormResultGuide(ArrayList<ArrayList<String>> normResultGuide, List<List<String>> rawSeg) {
        JSONObject normResult = new JSONObject();
        for (List<String> normResultGuideItem : normResultGuide) {
            makeNormResultItem(normResult, normResultGuideItem.get(0), rawSeg, normResultGuideItem.subList(1, normResultGuideItem.size()));
        }
        return normResult;
    }

    //把normResultGuide变成一个树形结构
    public static List<List<Object>> mergeLists(ArrayList<ArrayList<String>> normResultGuide) {
        List<List<Object>> schema_tree = new ArrayList<>();

        for (List<String> item : normResultGuide) {
            String key = item.get(0);
            String value = item.get(1);

            // 检查key是否已经在result列表中
            boolean found = false;
            for (List<Object> resItem : schema_tree) {
                if (resItem.get(0).equals(key)) {
                    @SuppressWarnings("unchecked")
                    List<String> values = (List<String>) resItem.get(1);
                    values.add(value);
                    found = true;
                    break;
                }
            }

            // 如果key不在result列表中，创建新的子列表并添加到result中
            if (!found) {
                List<Object> newItem = new ArrayList<>();
                newItem.add(key);
                List<String> values = new ArrayList<>();
                values.add(value);
                newItem.add(values);
                schema_tree.add(newItem);
            }
        }

        return schema_tree;
    }


    //二级提取
    public static List<JSONObject> subExtract(List<List<String>> rawSeg, String rawSegItemName, List<String> keyList, String endWord) {
        String content = "";
        for (List<String> item : rawSeg) {
            if (item.get(0).contains(rawSegItemName)) {
                content = item.get(1);
                break;
            }
        }
        keyList.add(endWord);
        List<JSONObject> extractList = new ArrayList<>();
        for (int i = 0; i < keyList.size() - 1; i++) {
            String startMarker = keyList.get(i);
            String endMarker = keyList.get(i + 1);
            String value = extractContent(content, startMarker, endMarker).trim().replaceAll("\t", "");
            JSONObject extractItem = new JSONObject();
            extractItem.put("key", keyList.get(i));
            extractItem.put("value", value);
            extractList.add(extractItem);
        }
        return extractList;
    }

    //确立最终文件的格式
    public static JSONObject genFinalData(JSONObject normResult, List<List<String>> rawSeg, String doc_type) {

        JSONObject finalData = new JSONObject();
        JSONObject result = new JSONObject();
        JSONObject data = new JSONObject();


        switch (doc_type) {
            case "Preoperative_Summary_Info":
                // JSONObject finalData = new JSONObject();
                result = new JSONObject();
                result.put("norm_result", normResult);
                result.put("recordType", "preoperative_summary_info");
                result.put("normResSubSeg", new JSONObject());
                result.put("raw_seg", rawSeg);
                finalData.put("code", 0);
                data = new JSONObject();
                data.put("result", result);
                finalData.put("data", data);
                break;

            case "Postoperative_First_Case_Info":
                break;

            case "Death_Info":
                result = new JSONObject();
                result.put("norm_result", normResult);
                result.put("recordType", "preoperative_summary_info");
                result.put("normResSubSeg", new JSONObject());
                result.put("raw_seg", rawSeg);
                finalData.put("code", 0);
                data = new JSONObject();
                data.put("result", result);
                finalData.put("data", data);
                break;

            case "Case_Info":
                break;

            default:

                break;
        }

        return finalData;
    }
}
