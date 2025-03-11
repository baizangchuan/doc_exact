package org.example;
import java.util.*;
import java.util.regex.*;


public class Table_hhj {
    public static List<Map<String, String>> filterTable(List<Map<String, String>> keyTable, String content) {
        List<Map<String, String>> matchedTable = new ArrayList<>();

        for (Map<String, String> row : keyTable) {
            String namePattern = row.get("name");
            if (namePattern != null) {
                // 先尝试正则匹配（使用原始正则表达式）
                Pattern pattern = Pattern.compile(namePattern);
                Matcher matcher = pattern.matcher(content);
                
                if (matcher.find()) {
                    // 如果正则匹配成功，使用匹配到的实际文本
                    String matchedText = matcher.group();
                    row.put("name", matchedText);
                    matchedTable.add(row);
                    continue;
                }

                // 再尝试普通字符串匹配（转义特殊字符）
                String literalPattern = Pattern.quote(namePattern);
                if (content.contains(literalPattern)) {
                    row.put("name", namePattern);
                    matchedTable.add(row);
                }
            }
        }

        return matchedTable;
    }


    public static List<String> extract_List_by_key(List<Map<String, String>> dataList, String Key) {
        List<String> value_list = new ArrayList<>();
        for (Map<String, String> map : dataList) {
            if (map.containsKey(Key)) {
                value_list.add(map.get(Key));
            }
        }
        return value_list;
    }


        //#在inputString中，提取startMarker和endMarker之间的内容
        public static String extractContent(String inputString, String startMarker, String endMarker, String is_regu, String regu) {
            String combinedRegex = startMarker + "(.*?)" + endMarker;
            Pattern pattern = Pattern.compile(combinedRegex);
            Matcher matcher = pattern.matcher(inputString);
            String in_text="";
            // 查找匹配项
            if (matcher.find()) {
                in_text=matcher.group(1);
            }
            if (is_regu == null || is_regu.isEmpty()){//二者之间的情况
                    return in_text;
                }
            else{//只匹配正则表达式的情况
                Pattern pattern_regu = Pattern.compile(regu);
                // System.out.println(regu);
                    Matcher matcher_regu = pattern_regu.matcher(inputString);
                if (matcher_regu.find()) {
                    in_text=matcher_regu.group();
                    System.out.println(in_text);
                }
            }

            return in_text; // 如果没有找到匹配项，则返回""

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
    
        //去除"病案号"尾部的多余内容
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
    
    
            if(modify==1){
                return new_content;
            }else{
                return content;
            }
        }
        public static List<Map<String, String>> re_to_text(List<Map<String, String>> table, String json_content) {
            for (Map<String, String> regex : table) {
                Pattern pattern = Pattern.compile(regex.get("regu"));
                Matcher matcher = pattern.matcher(json_content);
    
                while (matcher.find()) {
                    regex.put("name", matcher.group());
                    removeTextBefore(json_content,matcher.group());
                }
            }
            return table;
        }

        //由原文和关键字集合，得到每个关键字对应的字段
        public static List<List<String>> getRawSeg(String content, List<Map<String, String>> table) {
            List<List<String>> rawSeg = new ArrayList<>();
            String inputString = content;
            inputString = inputString.replaceAll("\n", " ");

            System.out.println("-----inputString: ↓");
            System.out.println(inputString);

            table=re_to_text(table,content);
            for (int i = 0; i < table.size() - 1; i++) {
                String startMarker = table.get(i).get("name");
                // String startMarker = (i == 0) ? "" : rawSegKeySet.get(i);
                String endMarker = table.get(i + 1).get("name");
                String regu = table.get(i).get("regu");
                String result = extractContent(inputString, startMarker, endMarker, table.get(i).get("is_regu"), regu);
                if (!(result == null || result.isEmpty())){
                    result=result.trim().replace("\t", "").replace("\n", "").replace("\r", "").replaceAll("\u00A0", "");
                }
                String clean_result=RemoveExtraneousContent(startMarker,result);//对每个模板做精细调整
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
            if (!(table == null || table.isEmpty())){
                Map<String, String> last_table_line = table.get(table.size() - 1);
                String finalKey = last_table_line.get("name");
    
                if (last_table_line.get("is_regu") == null || last_table_line.get("is_regu").isEmpty()){//二者之间的情况
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
                }
                else{//只匹配正则表达式的情况
                    Pattern pattern_regu = Pattern.compile(last_table_line.get("regu"));
                    // System.out.println(regu);
                
                    Matcher matcher_regu = pattern_regu.matcher(inputString);
                    String result = "";
                    if (matcher_regu.find()) {
                    result = matcher_regu.group();
                    }
                    List<String> segPair = new ArrayList<>();
                    segPair.add(finalKey);
                    segPair.add(result);
                    rawSeg.add(segPair);
                }
            }

    
            return rawSeg;
        }

        public static ArrayList<ArrayList<String>> get_normResultGuide (List<Map<String, String>> keyTable){
            ArrayList<ArrayList<String>> normResultGuide=new ArrayList<ArrayList<String>>();
            ArrayList<String> line=new ArrayList<>();
            for (Map<String, String> table_line: keyTable){
                line=new ArrayList<>();
                line.add(table_line.get("schema"));
                line.add(table_line.get("name"));
                normResultGuide.add(line);
            }

            return normResultGuide;
        }
    
}
