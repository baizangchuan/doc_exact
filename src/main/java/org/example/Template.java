package org.example;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;
import java.util.HashSet;

import org.json.JSONArray;
import org.json.JSONObject;


public class Template {
    public static int Get_key_metrics_flag(String doc_type){
        int key_metrics_flag;
        switch (doc_type) {
            case "admit_info":
                key_metrics_flag = 0;
                break;

            case "discharge_info":
                key_metrics_flag = 0;
                break;

            case "first_case_info":
                key_metrics_flag = 1;
                break;

            case "operation_info":
                key_metrics_flag = 0;
                break;

            case "case_info":
                key_metrics_flag = 1;
                break;

            case "inform_info":
                key_metrics_flag = 0;
                break;

            case "postoperative_first_case_info":
                key_metrics_flag = 1;
                break;

            default:
                key_metrics_flag = 0;
                break;
        }
        return key_metrics_flag;
    }

    //在提取raw_seg时做些细致化提取，去除多余的部分 这个之后放到InfoExxx里去

    public static String RemoveExtraneousContent(String key_name, String content){
        String new_content="";
        int modify=0;//是否修改的标记
        if (key_name.contains("病案号")){
            Pattern pattern = Pattern.compile("^\\d+");
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                new_content = matcher.group();
            }
            modify=1;
        }
        // if (key_name.contains("特此签字为证")){
        //     new_content="";
        //     modify=1;
        // }
        // if (key_name.contains("告知医师签名")){
        //     new_content="";
        //     modify=1;
        // }
        // if (key_name.contains("患者或授权（法定）代理人签字")){
        //     new_content="";
        //     modify=1;
        // }

        // 如果要除去尾部多余内容，用这个：
        // InfoExtracter.extractContent(inputString, startMarker, endMarker);

        if(modify==1){
            return new_content;
        }else{
            return content;
        }
    }


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
            String value = InfoExtracter.extractContent(content, startMarker, endMarker).trim().replaceAll("\t", "");
            JSONObject extractItem = new JSONObject();
            extractItem.put("key", keyList.get(i));
            extractItem.put("value", value);
            extractList.add(extractItem);
        }
        return extractList;
    }

    public static void Template_modify(String doc_type, JSONObject normResult,List<List<String>> rawSeg, String content){
        switch (doc_type) {
            case "admit_info":
                normResult=Modify_admit_info(doc_type, normResult, rawSeg, content);
                break;

            case "discharge_info":
                break;

            case "first_case_info":
                break;

            case "operation_info":
                break;

            case "case_info":
                normResult=Modify_case_info(doc_type, normResult, content);
                break;

            case "inform_info":
                break;

            case "postoperative_first_case_info":
                break;

            default:
                break;
        }
    }

    public static JSONObject Modify_admit_info(String doc_type, JSONObject normResult, List<List<String>> rawSeg, String content){
        List<String> keyList = new ArrayList<>(Arrays.asList("T","P","R","BP"));
        String endWord = "生命征";
        List<JSONObject> keyMetrics = InfoExtracter.subExtract(rawSeg, "体格检查", keyList, endWord);
        JSONObject physical_exam = new JSONObject();

        //获取physical_exam原来的内容
        JSONArray physical_exam_org=normResult.getJSONArray("physical_exam");
        for (int i = 0; i < physical_exam_org.length(); i++) {
            physical_exam.put("key",physical_exam_org.getJSONObject(i).getString("key"));
            physical_exam.put("value",physical_exam_org.getJSONObject(i).getString("value"));
        }

        //加上二级解析的内容
        physical_exam.put("physical_exam", keyMetrics);

        //少了个parse_result，这里之后补上
        physical_exam.put("parse_result", new JSONObject());


        //把新增的东西放回normResult里
        JSONArray array_temp = new JSONArray();
        array_temp.put(physical_exam);
        normResult.put("physical_exam", array_temp);

        return normResult;
    }

    public static JSONObject Modify_discharge_info(String doc_type, JSONObject normResult, String content){

        return normResult;
    }
    public static JSONObject Modify_first_case_info(String doc_type, JSONObject normResult, String content){

        return normResult;
    }
    public static JSONObject Modify_operation_info(String doc_type, JSONObject normResult, String content){

        return normResult;
    }
    public static JSONObject Modify_case_info(String doc_type, JSONObject normResult, String content){
        //在normResult中增加一个subjective
        String subjective_value = InfoExtracter.extractContent(content, "姓名：", "；").trim().replaceAll("\t", "");
        JSONObject subjective = new JSONObject();
        subjective.put("key", "nonkey");
        subjective.put("value", subjective_value);
        JSONArray array_temp = new JSONArray();
        array_temp.put(subjective);
        normResult.put("subjective", array_temp);


        JSONObject assessment = new JSONObject();
        assessment.put("key", "nonkey");
        assessment.put("value", "");
        array_temp = new JSONArray();
        array_temp.put(assessment);
        normResult.put("assessment", assessment);


        JSONObject plan = new JSONObject();
        plan.put("key", "nonkey");
        plan.put("value", "");
        array_temp = new JSONArray();
        array_temp.put(plan);
        normResult.put("plan", plan);

        JSONObject roles = new JSONObject();
        roles.put("key", "nonkey");
        roles.put("value", "");
        array_temp = new JSONArray();
        array_temp.put(roles);
        normResult.put("roles", roles);

        return normResult;
    }
    public static JSONObject Modify_inform_info(String doc_type, JSONObject normResult, String content){

        return normResult;
    }
    public static JSONObject Modify_postoperative_first_case_info(String doc_type, JSONObject normResult, String content){

        return normResult;
    }

    /**
     * 去除 JSONObject 中列表类型值的重复元素
     */
    public static JSONObject removeDuplicatesFromLists(JSONObject jsonObject) {
        // 遍历 JSONObject 的所有键
        for (String key : jsonObject.keySet()) {
            Object value = jsonObject.get(key);
            
            // 如果值是 JSONArray
            if (value instanceof org.json.JSONArray) {
                org.json.JSONArray jsonArray = (org.json.JSONArray) value;
                
                // 创建一个新的不含重复元素的列表
                List<Object> uniqueItems = new ArrayList<>();
                Set<String> uniqueStrings = new HashSet<>();
                
                for (int i = 0; i < jsonArray.length(); i++) {
                    Object item = jsonArray.get(i);
                    String itemString = item.toString();
                    
                    // 如果这个元素还没有添加过，则添加它
                    if (!uniqueStrings.contains(itemString)) {
                        uniqueItems.add(item);
                        uniqueStrings.add(itemString);
                    }
                }
                
                // 创建新的 JSONArray 并替换原来的
                org.json.JSONArray newArray = new org.json.JSONArray();
                for (Object item : uniqueItems) {
                    newArray.put(item);
                }
                
                jsonObject.put(key, newArray);
            }
        }
        
        return jsonObject;
    }

    /**
     * 生成最终数据
     */
    public static JSONObject genFinalData(JSONObject normResult, List<List<String>> rawSeg, String doc_type, String doc_type_zh) {
        // 去除 normResult 中列表值的重复元素
        normResult = removeDuplicatesFromLists(normResult);
        
        String record_title=doc_type_zh;
        // switch (doc_type) {
        //     case "admit_info":
        //         record_title="入院记录";
        //         break;
        //     case "discharge_info":
        //         record_title="出院记录";
        //         break;
        //     case "first_case_info":
        //         record_title="首次病程记录";
        //         break;
        //     case "operation_info":
        //         record_title="手术记录";
        //         break;
        //     case "case_info":
        //         record_title="病程记录";
        //         break;
        //     case "inform_info":
        //         record_title="手术知情同意书"; //这里可能会变，需要的话做个参数传进去
        //         break;
        //     case "postoperative_first_case_info":
        //         record_title="术后首次病程记录";
        //         break;

        //     default:
        //         record_title="";
        //         break;
        // }
        JSONObject finalData = genFinalData_Template(normResult, rawSeg, doc_type, record_title);

        return finalData;
    }

    public static JSONObject genFinalData_Template(JSONObject normResult, List<List<String>> rawSeg, String doc_type, String record_title){
        JSONObject finalData = new JSONObject();
        JSONObject result = new JSONObject();
        JSONObject data = new JSONObject();
        result = new JSONObject();
        result.put("recordType", doc_type);
        result.put("record_title", record_title);
        result.put("record_date", JSONObject.NULL);
        result.put("doctor_titles", "");
        result.put("norm_result", normResult);
        result.put("normResSubSeg", new JSONObject());
        result.put("raw_seg", rawSeg);
        result.put("msg", "ok");
        result.put("code", "0");

        data = new JSONObject();
        data.put("result", result);
        data.put("sample_rate", JSONObject.NULL);
        data.put("sample_count", JSONObject.NULL);

        finalData.put("data", data);
        return finalData;

    }
}
