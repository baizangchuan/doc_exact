package org.example;

import java.lang.reflect.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

import static org.example.Main.json_path;

public class Get_File_docType {

    public static ArrayList<String> Get_DocTypeList() {
        // 连接数据库
        String url = "jdbc:mysql://111.9.47.74:8922/emr_parser";
        String user = "root";
        String password = "Aliab12!2020";

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        ArrayList<String> doc_type_list = new ArrayList<>();
        String pre_schema = null;

        try {
            conn = DriverManager.getConnection(url, user, password);

            String sql = "select * from New_config_node";

            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                String schema = rs.getString("template_config_code");
                if (!schema.equals(pre_schema) && ! doc_type_list.contains(schema)){
                    doc_type_list.add(schema);
                    pre_schema = schema;
                }
            }
        }catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }

        return doc_type_list;
    }

    // public static String Get_DocType_zh(String Content, ArrayList<HashMap<String, Object>> database_doctype_list) {
    //     String  doc_type = null;
    //     // 遍历database_doctype_list
    //     for (int i = 0; i < database_doctype_list.size(); i++) {
    //         HashMap<String, Object> doc = database_doctype_list.get(i);
    //         String docType = doc.get("doc_type").toString();
    //         ArrayList<String> types = (ArrayList<String>) doc.get("type");
    //         // 对types列表进行排序，元素长的排前面
    //         // 按字符串长度排序
    //         Collections.sort(types, new Comparator<String>() {
    //             @Override
    //             public int compare(String s1, String s2) {
    //                 return s2.length() - s1.length();  // 长的排在前面
    //             }
    //         });


    //         // 遍历中文types列表
    //         for (int j = 0; j < types.size(); j++) {
    //             if (Content.contains(types.get(j))) {
    //                 doc_type = types.get(j);
    //                 break;

    //             }
    //         }
    //     }

    //     return doc_type;

    // }

    public static String Get_DocType_zh(String Content, ArrayList<HashMap<String, Object>> database_doctype_list) {
        String  doc_type = null;
        // 遍历database_doctype_list
        ArrayList<String> types=new ArrayList<String>() ;
        for (int i = 0; i < database_doctype_list.size(); i++) {
            HashMap<String, Object> doc = database_doctype_list.get(i);
            String docType = doc.get("doc_type").toString();

            //  types = (ArrayList<String>) doc.get("type");
            ArrayList type_list=(ArrayList)doc.get("type");
             types.addAll(type_list);
            // 对types列表进行排序，元素长的排前面
            // 按字符串长度排序
            // System.err.println(types);
            Collections.sort(types, new Comparator<String>() {
                @Override
                public int compare(String s1, String s2) {
                    return s2.length() - s1.length();  // 长的排在前面
                }
            });
        }
        System.err.println(types);
        // 遍历中文types列表
        for (int j = 0; j < types.size(); j++) {
            if (Content.contains(types.get(j))) {
                doc_type = types.get(j);
                break;

            }
        }

        return doc_type;

    }

    public static String Get_DocType_en(String Content, ArrayList<HashMap<String, Object>> database_doctype_list) {
        String  doc_type = null;
        // 遍历database_doctype_list
        ArrayList<ArrayList<String>> types=new ArrayList() ;
        ArrayList<String> docType=new ArrayList<String>() ;
        for (int i = 0; i < database_doctype_list.size(); i++) {
            HashMap<String, Object> doc = database_doctype_list.get(i);
            docType.add(doc.get("doc_type").toString());

            //  types = (ArrayList<String>) doc.get("type");
            ArrayList type_list=(ArrayList)doc.get("type");
            
            // 对types列表进行排序，元素长的排前面
            // 按字符串长度排序
            // System.err.println(types);
            Collections.sort(type_list, new Comparator<String>() {
                @Override
                public int compare(String s1, String s2) {
                    return s2.length() - s1.length();  // 长的排在前面
                }
            });
            types.add(type_list);
        }
        System.err.println(types);
        // 遍历中文types列表
        ArrayList<String>matched_doctype_zh_list=new ArrayList<String>() ;
        for (int j = 0; j < types.size(); j++) {
            for(int i=0; i<types.get(j).size();i++){
                if (Content.contains(types.get(j).get(i))) {
                    matched_doctype_zh_list.add( docType.get(j));
                    // break;
                }
            }

        }
        Collections.sort(matched_doctype_zh_list, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s2.length() - s1.length();  // 长的排在前面
            }
        });
        doc_type=matched_doctype_zh_list.get(0);
        return doc_type;

    }

    // public static String Get_DocType_en(String Content, ArrayList<HashMap<String, Object>> database_doctype_list) {
    //     String  doc_type = null;
    //     // 遍历database_doctype_list
    //     ArrayList<String> types=new ArrayList<String>() ;
    //     ArrayList<String> docType=new ArrayList<String>() ;
    //     for (int i = 0; i < database_doctype_list.size(); i++) {
    //         HashMap<String, Object> doc = database_doctype_list.get(i);
    //         docType.add(doc.get("doc_type").toString());

    //         //  types = (ArrayList<String>) doc.get("type");
    //         ArrayList type_list=(ArrayList)doc.get("type");
    //          types.addAll(type_list);
    //         // 对types列表进行排序，元素长的排前面
    //         // 按字符串长度排序
    //         // System.err.println(types);
    //         Collections.sort(types, new Comparator<String>() {
    //             @Override
    //             public int compare(String s1, String s2) {
    //                 return s2.length() - s1.length();  // 长的排在前面
    //             }
    //         });
    //     }
    //     System.err.println(types);
    //     // 遍历中文types列表
    //     for (int j = 0; j < types.size(); j++) {
    //         if (Content.contains(types.get(j))) {
    //             doc_type = docType.get(j);
    //             break;

    //         }
    //     }

    //     return doc_type;

    // }


    // public static String Get_DocType_en(String Content, ArrayList<HashMap<String, Object>> database_doctype_list) {
    //     String  doc_type = null;
    //     // 遍历database_doctype_list
    //     for (int i = 0; i < database_doctype_list.size(); i++) {
    //         HashMap<String, Object> doc = database_doctype_list.get(i);
    //         String docType = doc.get("doc_type").toString();
    //         ArrayList<String> types = (ArrayList<String>) doc.get("type");
    //         // 对types列表进行排序，元素长的排前面
    //         // 按字符串长度排序
    //         Collections.sort(types, new Comparator<String>() {
    //             @Override
    //             public int compare(String s1, String s2) {
    //                 return s2.length() - s1.length();  // 长的排在前面
    //             }
    //         });

    // //            // 打印排序后的列表
    // //            for (String type : types) {
    // //                System.out.println(type);
    // //            }
    //         // 遍历中文types列表
    //         for (int j = 0; j < types.size(); j++) {
    //             if (Content.contains(types.get(j))) {
    //                 String chinese = types.get(j);
    //                 doc_type = docType;
    //                 break;

    //             }
    //         }
    //     }

    //        HashMap<String, Object> doc = database_doctype_list.get(6);
    //        String docType = doc.get("doc_type").toString();
    ////        System.out.println(docType);
    //        ArrayList<String> types = (ArrayList<String>) doc.get("type");
    //        System.out.println(types);
    //        String f1 = types.get(0).toString();
    //        String f2 = types.get(1).toString();
    //        System.out.println(Content.contains(f1));

    //     return doc_type;

    // }
    // public static String Get_DocType_zh(String Content, ArrayList<HashMap<String, Object>> database_doctype_list) {

    // }

    public static ArrayList<HashMap<String, Object>> GetDataFromDatabase(String table_name, String url, String user, String password) {
        ArrayList<HashMap<String, Object>> databaseDoctypeList = new ArrayList<>();

        // 连接数据库
        // String url = url;
        // String user = "root";
        // String password = "Aliab12!2020";

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        ArrayList<String> doc_type_list = new ArrayList<>();
        String pre_schema = null;

        try {
            conn = DriverManager.getConnection(url, user, password);

            String sql = "select * from "+table_name;

            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                String isType = rs.getString("adm_column");
                if (isType.equals("type")) {
                    // System.out.println(".()1212222222222222222");
                    // String schema = rs.getString("template_config_code");
                    String doc_type_en = rs.getString("template_config_code");
                    
                    //判断databaseDoctypeList中是否有doc_type_en
                    boolean exit_doc_type_en = false;
                    for (Map<String, Object> entry : databaseDoctypeList) {
                        if (doc_type_en.equals(entry.get("doc_type"))) {
                            exit_doc_type_en = true;
                            //有则判断doc_type_zh是否在“type”里，不在则插入
                            List<String> typeList = (List<String>) entry.get("type");
                            typeList.add( rs.getString("config_node_key"));
                            break;
                        }
                    }
                    if (exit_doc_type_en){

                    }
                    else{//没有则创建，插入
                        HashMap<String, Object> newEntry = new HashMap<>();
                        newEntry.put("doc_type", doc_type_en);
                        newEntry.put("type", new ArrayList<>(Arrays.asList(rs.getString("config_node_key"))));
                
                        // Add the new element to the list
                        databaseDoctypeList.add(newEntry);
                    }
   
                }
                String schema = rs.getString("template_config_code");
                if (!schema.equals(pre_schema) && ! doc_type_list.contains(schema)){
                    doc_type_list.add(schema);
                    pre_schema = schema;
                }
            }

        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            // 确保资源被关闭
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // System.err.println(databaseDoctypeList);

        return databaseDoctypeList;
    }

    // public static void CreateLabel(String TableName) {
    //     String url = "jdbc:mysql://111.9.47.74:8922/emr_parser";
    //     String user = "root";
    //     String password = "Aliab12!2020";

    //     Connection conn = null;
    //     Statement stmt = null;
    //     ResultSet rs = null;

    //     try {
    //         conn = DriverManager.getConnection(url, user, password);
    //         stmt = conn.createStatement();

    //         String sql = "CREATE TABLE " + TableName + "(" +
    //                 "id BIGINT AUTO_INCREMENT, " +
    //                 "config_node_key VARCHAR(255) NOT NULL, " +
    //                 "adm_column VARCHAR(255) NOT NULL, " +
    //                 "match_sample_num INT, " +
    //                 "repeat_in_sample INT, " +
    //                 "version VARCHAR(255), " +
    //                 "parse_task_id BIGINT NOT NULL, " +
    //                 "template_config_node VARCHAR(255) NOT NULL, " +
    //                 "org_name VARCHAR(255) NOT NULL, " +
    //                 "rectified_time DATETIME NOT NULL, " +
    //                 "gmt_created DATETIME NOT NULL, " +
    //                 "gmt_modified DATETIME NOT NULL, " +
    //                 "first_node VARCHAR(255) NOT NULL, " +
    //                 "is_regular_expression VARCHAR(255) NOT NULL, " +
    //                 "PRIMARY KEY (id))";
    //         stmt.executeUpdate(sql);
    //     }catch (Exception e){
    //         e.printStackTrace();
    //     }
    // }



    public static void main(String[] args) throws Exception {
        // json_path = "./input/case_info_input.json";
        // String json_content = InfoExtracter.getContentFromInputJsonDir(json_path);
        // System.out.println(json_content);
        // ArrayList<String> doc_type_list = Get_DocTypeList();
        // System.out.println(doc_type_list);
        // ArrayList<HashMap<String, Object>> database_doctype_list = GetDataFromDatabase();
        // String doc_type = Get_DocType_zh(json_content, database_doctype_list);
        // System.out.println(doc_type);
        // // 输入是content,输出是type
        // // 遍历database_doctype_list

    }


}
