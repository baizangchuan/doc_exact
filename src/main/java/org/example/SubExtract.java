package org.example;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.json.JSONArray;

public class SubExtract {
    // DatabaseConnector.MyResult result = DatabaseConnector.get_rawSegKeySet(doc_type, json_content);//这里改写了函数，增加了入参：doc_type

    //由每行中二级解析指向的根节点，从rawSeg中找出对应的内容
    public static String  get_root_content(List<List<String>> rawSeg, String root_node){
        String root_content="";
        for(List<String> line :rawSeg){
            String combinedRegex = root_node;
            Pattern pattern = Pattern.compile(combinedRegex);
            Matcher matcher = pattern.matcher(line.get(0));
            if (matcher.find()) {
                root_content=line.get(1);
                return root_content;
            }
        }
        return root_content;
    }

    // 由rawSeg中获取与root_node匹配的实际展示key（无正则符号）
    public static String get_root_display_key(List<List<String>> rawSeg, String root_node){
        for(List<String> line : rawSeg){
            String combinedRegex = root_node;
            Pattern pattern = Pattern.compile(combinedRegex);
            Matcher matcher = pattern.matcher(line.get(0));
            if (matcher.find()) {
                return line.get(0);
            }
        }
        return "";
    }

    // 清洗用于展示的一极key，移除模板中的通配与空白转义
    private static String sanitizeDisplayKey(String key){
        if (key == null) return "";
        // 去除星号与\s相关转义
        String cleaned = key.replace("*", "");
        cleaned = cleaned.replace("\\s+", "");
        cleaned = cleaned.replace("\\s*", "");
        return cleaned;
    }

    // 严格判定两个键是否为同一展示键：均经清洗后比较
    private static boolean isSameKey(String a, String b) {
        return sanitizeDisplayKey(a).equals(sanitizeDisplayKey(b));
    }

    // 安全的正则匹配：patternStr 作为正则与 text 比较，失败则退化为清洗后比较
    private static boolean patternMatchesText(String patternStr, String text) {
        if (patternStr == null || text == null) return false;
        try {
            Pattern p = Pattern.compile(patternStr);
            return p.matcher(text).find();
        } catch (Exception e) {
            return sanitizeDisplayKey(patternStr).equals(text);
        }
    }

    // 从 main_body 中通过根标题精确定位父级 schema
    private static String determineParentSchema(String root_display_key, String root_node_raw, List<Map<String,String>> main_body) {
        if (main_body == null) return null;
        for (Map<String,String> row : main_body) {
            String name = row.get("name");
            if (isSameKey(name, root_display_key) || isSameKey(name, root_node_raw)) {
                return row.get("schema");
            }
        }
        return null;
    }

    public static void Print_test(List<List<Map<String, String>>> sub_content_list, List<List<String>> rawSeg){
        for (List<Map<String, String>>sub_content : sub_content_list){
            String root_node=sub_content.get(0).get("root_node").replace("\\s+", "\\s*");
            System.err.println(root_node);
            System.err.println("-------------------------------root_node ↑");
            String root_content=get_root_content(rawSeg,root_node);
            System.err.println(root_content);
            System.err.println("-------------------------------root_content ↑");

            // sub_content = Table_hhj.filterTable(sub_content, root_content);

            

            List<List<String>> sub_rawSeg = Table_hhj.getRawSeg(root_content, sub_content);
            System.out.println(sub_rawSeg);
            System.out.println("------------------------------------------------ sub_rawSeg↑");
            
            //从模板获取原始字段的组合规则
            ArrayList<ArrayList<String>> sub_normResultGuide = Table_hhj.get_normResultGuide(sub_content);
            System.out.println(sub_normResultGuide);
            System.out.println("------------------------------------------------sub_normResultGuide↑");
            
            
            //将组合规则按照相同的schema组合为树状
            List<List<Object>> sub_schema_tree_list = InfoExtracter.mergeLists(sub_normResultGuide);
            System.out.println(sub_schema_tree_list);
            System.out.println("------------------------------------------------sub_schema_tree_list↑");
            
            //由组合规则对原始字段做组合拼凑
            JSONObject sub_normResult = InfoExtracter.getNormResultFromSchemaTreeList(sub_schema_tree_list, sub_rawSeg);
            System.out.println(sub_normResult);
            System.out.println("------------------------------------------------sub_normResult ↑");


        }
    }
    public static void mergeJsonObjects(JSONObject target, JSONObject source) {
        // 遍历source JSONObject中的所有键值对
        Iterator<String> keys = source.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            // 将source中的键值对添加到target中
            target.put(key, source.get(key));
        }
    }

    //根据first_level_node和rawSeg的内容，得到sub_level_content
    public static JSONObject get_sub_NormResult(List<List<Map<String, String>>> sub_content_list, List<List<String>> rawSeg, List<Map<String,String>> main_body){
        JSONObject sub_normResult_all=new JSONObject();
        for (List<Map<String, String>>sub_content : sub_content_list){
            // root_node 原文与用于匹配的正则形式
            String root_node_raw = sub_content.get(0).get("root_node");
            String root_node = root_node_raw.replace("\\s+", "\\s*");
            String root_content=get_root_content(rawSeg,root_node);
            System.err.println(root_content);
            System.err.println("-------------------------------root_content ↑");

            // 获取用于展示的真实根key
            String root_display_key = get_root_display_key(rawSeg, root_node);
            if (root_display_key == null || root_display_key.isEmpty()) {
                root_display_key = sanitizeDisplayKey(root_node_raw);
            }

            // 使用 main_body 精确定位父级 schema
            String parent_schema = determineParentSchema(root_display_key, root_node_raw, main_body);
            if (parent_schema == null) {
                // 无法确定父级 schema 时，跳过生成，避免错误挂载到子 schema
                continue;
            }

            // 过滤并排序，限定在 root_content 内解析
            List<Map<String, String>> processed = Table_hhj.filterTable(sub_content, root_content);
            processed = DatabaseConnector.sortTable(processed, root_content); // 修改类名

            // 去掉根节点自身，只保留二级键用于子解析（严格等价而非宽泛正则）
            List<Map<String, String>> child_only = new ArrayList<>();
            for (Map<String, String> row : processed) {
                String name = row.get("name");
                if (name == null) continue;
                if (isSameKey(name, root_display_key) || isSameKey(name, root_node_raw)) continue;
                child_only.add(row);
            }

            // 构造二级解析结果
            List<List<String>> sub_rawSeg = Table_hhj.getRawSeg(root_content, child_only);
            System.out.println(sub_rawSeg);
            System.out.println("------------------------------------------------ sub_rawSeg↑");
            ArrayList<ArrayList<String>> sub_normResultGuide = Table_hhj.get_normResultGuide(child_only);
            System.out.println(sub_normResultGuide);
            System.out.println("------------------------------------------------sub_normResultGuide↑");
            List<List<Object>> sub_schema_tree_list = InfoExtracter.mergeLists(sub_normResultGuide);
            System.out.println(sub_schema_tree_list);
            System.out.println("------------------------------------------------sub_schema_tree_list↑");
            JSONObject sub_normResult = InfoExtracter.getNormResultFromSchemaTreeList(sub_schema_tree_list, sub_rawSeg);
            System.out.println(sub_normResult);
            System.out.println("------------------------------------------------sub_normResult ↑");

            // 生成一级节点条目，并把二级解析结果挂到该条目下
            JSONObject parentItem = new JSONObject();
            parentItem.put("value", root_content);
            parentItem.put("key", root_display_key);
            Iterator<String> subKeys = sub_normResult.keys();
            while (subKeys.hasNext()) {
                String k = subKeys.next();
                parentItem.put(k, sub_normResult.get(k));
            }

            // 将一级条目放入对应 schema 的数组中
            JSONArray arr = sub_normResult_all.optJSONArray(parent_schema);
            if (arr == null) arr = new JSONArray();
            arr.put(parentItem);
            sub_normResult_all.put(parent_schema, arr);
        }

        return sub_normResult_all;
    }


}
