package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class DatabaseConnector {

    static class MyResult {
        List<String> rawSegKeySet=new ArrayList<>();
        ArrayList<ArrayList<String>> normResultGuide = new ArrayList<>();

        public MyResult(List<String> rawSegKeySet, ArrayList<ArrayList<String>> normResultGuide) {
            this.rawSegKeySet=rawSegKeySet;
            this.normResultGuide=normResultGuide;

        }
    }

    public static MyResult get_rawSegKeySet(String doc_type, String content) {
        // 数据库连接信息
        String url = "jdbc:mysql://111.9.47.74:8922/emr_parser"; // 根据你的数据库配置修改
        String user = "root"; // 根据你的数据库用户名修改
        String password = "Aliab12!2020"; // 根据你的数据库密码修改

        // 声明连接和语句对象
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        List<String> rawSegKeySet=new ArrayList<>();
        ArrayList<ArrayList<String>> normResultGuide = new ArrayList<>();

        try {
            // 连接数据库
            connection = DriverManager.getConnection(url, user, password);

            // 创建SQL语句
            String sql = "SELECT * FROM hp_config_node"; // 根据你的表格名称修改

            // 创建并执行SQL语句
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);

            // 打印表格信息
            while (resultSet.next()) {
                // 这里想要通用可以把病程记录换成按索引读取template_list
                if (resultSet.getString("template_config_code").equals(doc_type)) {
                    String value = resultSet.getString("config_node_key");
                    value = value.replaceAll("\\\\s\\+", "");
                    String key = resultSet.getString("adm_column");
                    rawSegKeySet.add(value);
                    normResultGuide.add(new ArrayList<>(Arrays.asList(key, value)));
                }

            }
           //  处理一下rawSegKeySet的\s+问题
//            for (String rawSegKey : rawSegKeySet) {
//
//            }
            resultSet.close();

            String sql_template = "SELECT * FROM hp_template_config";
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql_template);

            // 定义一个template_list存储模板名称
            List<String> templateList = new ArrayList<>();

            while (resultSet.next()) {
                templateList.add(resultSet.getString("record_type"));
            }
            resultSet.close();
            for (String template : templateList) {
//                System.out.println(template);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // 关闭连接和语句对象
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        // sortListByContentOrder(normResultGuide,content);
        rawSegKeySet = extractSecondElements(normResultGuide);


        return new MyResult(rawSegKeySet, normResultGuide);
    }

    // public static ArrayList<ArrayList<String>> get_normResultGuide() {
    //     ArrayList<ArrayList<String>> normResultGuide = new ArrayList<>();


    //     return normResultGuide;
    // }




    static class Table{
        List<String> rawSegKeySet=new ArrayList<>();
        ArrayList<ArrayList<String>> normResultGuide = new ArrayList<>();
        Map<String, String> table_line = new HashMap<>();
        List<Map<String, String>> table = new ArrayList<>();
        // ArrayList<ArrayList<String>> table = new ArrayList<>();
        // ArrayList<String> table_line = new ArrayList<>();

        /*
        数据类应该有的东西：
        table_line 长度为4的hashmap，包含：name, schema, root_node, is_regu, regu以及每个key对应的内容
        table 长度为n的ArrayList，包含除了sub_content的内容，每个单元类型为table_line，之后的rawSegKeySet和normResultGuide都要往里面取（normResultGuide待定）
        sub_content_list（！这个最后做，好像有写这个的伪代码） 用于存放二级解析的列表，以root为分类依据，分出不同的sub_content
        */
        public Table(List<Map<String, String>> table,  Map<String, String> table_line){
            this.table=table;
            this.table_line=table_line;
        }
    }
    //构造数据类
    public static class Table_pair{
        List<Map<String, String>> main_body= new ArrayList<>();
        List<List<Map<String, String>>> sub_content_list= new ArrayList<>();
        List<Map<String, String>> all_line = new ArrayList<>();

        public Table_pair(List<Map<String, String>> main_body,  List<List<Map<String, String>>> sub_content_list){
            this.main_body=main_body;
            this.sub_content_list=sub_content_list;
        }
    }

    public static Table_pair get_Table(String doc_type, String content, String url, String user, String password) {
        // // 数据库连接信息
        // String url = "jdbc:mysql://111.9.47.74:8922/emr_parser"; // 根据你的数据库配置修改
        // String user = "root"; // 根据你的数据库用户名修改
        // String password = "Aliab12!2020"; // 根据你的数据库密码修改

        // 声明连接和语句对象
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        List<Map<String, String>> table = new ArrayList<>();
        Map<String, String> table_line = new HashMap<>();
        

        try {
            // 连接数据库
            connection = DriverManager.getConnection(url, user, password);

            // 创建SQL语句
            // String sql = "SELECT * FROM hp_config_node"; // 根据你的表格名称修改
            String sql = "SELECT * FROM new_template"; // 根据你的表格名称修改

            // 创建并执行SQL语句
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);

            // 打印表格信息
            while (resultSet.next()) {
                // 这里想要通用可以把病程记录换成按索引读取template_list
                String node = resultSet.getString("template_config_code");
                if (node.equals(doc_type)) {
                    
                    table_line = new HashMap<>();
                    table_line.put("name",resultSet.getString("config_node_key").replace("\\s+", "\\s*"));
                    table_line.put("schema",resultSet.getString("adm_column"));
                    table_line.put("root_node",resultSet.getString("first_node"));//先不测试这里
                    table_line.put("is_regu",resultSet.getString("is_regular_expression"));//先不测试这里
                    table_line.put("regu",resultSet.getString("config_node_key").replace("\\s+", "\\s*"));//先不测试这里
                    table.add(table_line);
                }
                //新文件类型
                if(doc_type.equals("")){
                    table_line = new HashMap<>();
                    table_line.put("name",resultSet.getString("config_node_key").replace("\\s+", "\\s*"));
                    table_line.put("schema",resultSet.getString("adm_column"));
                    table_line.put("root_node",resultSet.getString("first_node"));//先不测试这里
                    table_line.put("is_regu",resultSet.getString("is_regular_expression"));//先不测试这里
                    table_line.put("regu",resultSet.getString("config_node_key").replace("\\s+", "\\s*"));//先不测试这里
                    table.add(table_line);
                }
            }
            // System.out.println(table);
            // Table_pair table_pair=Get_sub_content(table);
            // List<Map<String, String>> main_body =table_pair.main_body;
            // List<List<Map<String, String>>> sub_content_list =table_pair.sub_content_list;

            resultSet.close();

            String sql_template = "SELECT * FROM hp_template_config"; //这里可以做优化，只选取符合条件的行
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql_template);

            // 定义一个template_list存储模板名称
            List<String> templateList = new ArrayList<>();

            while (resultSet.next()) {
                templateList.add(resultSet.getString("record_type"));
            }
            resultSet.close();
            for (String template : templateList) {
//                System.out.println(template);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // 关闭连接和语句对象
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        // System.out.println(table);

        Table_pair table_pair=Get_sub_content(table);

        return table_pair;
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

    // //去除重复元素
    // public static List<String> removeDuplicates(List<String> list) {
    //     Set<String> set = new HashSet<>(list); // 使用HashSet去除重复元素

    //     return new ArrayList<>(set); // 将Set转换回List
    // }
    public static List<String> removeDuplicates(List<String> list) {
        // 检查列表是否为空或者所有元素都是null
        if (list == null || list.stream().allMatch(Objects::isNull)) {
            return new ArrayList<>(); // 返回一个空列表
        }

        Set<String> set = new HashSet<>(list); // 使用HashSet去除重复元素
        return new ArrayList<>(set); // 将Set转换回List
    }


    public static Table_pair Get_sub_content(List<Map<String, String>> table){
        //找到所有的二级解析对应的根结点
        List<String> root_node_list_full= extract_List_by_key(table,"root_node");
        List<String> root_node_list=removeDuplicates(root_node_list_full);
        
        //二级解析内容的列表
        List<List<Map<String, String>>> sub_content_list= new ArrayList<>();
        List<Map<String, String>>sub_content= new ArrayList<>();
        // System.out.println(root_node_list);
        if(!root_node_list.isEmpty()){
            for (String root_node: root_node_list){
                sub_content= new ArrayList<>();
                for(Map<String, String> table_line:table){
                    // System.out.println(table_line);
                    if (table_line.get("root_node")!=null){
                        if (table_line.get("root_node").equals(root_node)){//可能会存在为空的报错
                            sub_content.add(table_line);
                        }
                    }
                }
                if(!sub_content.isEmpty()){
                    sub_content_list.add(sub_content);
                }
            }
        }

        //去除二级解析后的内容
        List<Map<String, String>> main_body= new ArrayList<>();
        for(Map<String, String> table_line_2:table){
            // System.out.println(table_line_2);
            if (table_line_2.get("root_node")==null&&table_line_2.get("schema")!="type"){
                main_body.add(table_line_2);
            }
        }

        Table_pair table_pair=new Table_pair(main_body, sub_content_list);
        return table_pair;
    }



    // public static List<String> sortByContentOrder(List<String> rawSegKeySet, String content) {
    //     List<ElementWithIndex> elementsWithIndices = new ArrayList<>();
        
    //     // Create a list of elements with their indices in rawSegKeySet
    //     for (int i = 0; i < rawSegKeySet.size(); i++) {
    //         String key = rawSegKeySet.get(i);
    //         int index = content.indexOf(key);
    //         if (index != -1) {
    //             elementsWithIndices.add(new ElementWithIndex(key, index, i));
    //             content = content.substring(0, index) + new String(new char[key.length()]).replace("\0", " ") + content.substring(index + key.length());
    //         }
    //     }

    //     // Sort the list based on the indices in content
    //     elementsWithIndices.sort(Comparator.comparingInt(e -> e.index));

    //     // Extract the sorted elements
    //     List<String> sortedList = new ArrayList<>();
    //     for (ElementWithIndex e : elementsWithIndices) {
    //         sortedList.add(e.element);
    //     }

    //     return sortedList;
    // }
    // private static class ElementWithIndex {
    //     String element;
    //     int index;
    //     int rawIndex;

    //     ElementWithIndex(String element, int index, int rawIndex) {
    //         this.element = element;
    //         this.index = index;
    //         this.rawIndex = rawIndex;
    //     }
    // }
    // public static List<Map<String, String>> sortTable(List<Map<String, String>> table, String content) {
    //     List<ElementWithIndex> elementsWithIndices = new ArrayList<>();
        
    //     // Create a list of elements with their indices in rawSegKeySet
    //     for (int i = 0; i < table.size(); i++) {
    //         String key = table.get(i).get("name");
    //         int index = content.indexOf(key);
    //         if (index != -1) {
    //             elementsWithIndices.add(new ElementWithIndex(key, index, i));
    //             content = content.substring(0, index) + new String(new char[key.length()]).replace("\0", " ") + content.substring(index + key.length());
    //         }
    //     }

    //     // Sort the list based on the indices in content
    //     elementsWithIndices.sort(Comparator.comparingInt(e -> e.index));

    //     // Extract the sorted elements
    //     List<String> sortedList = new ArrayList<>();
    //     for (ElementWithIndex e : elementsWithIndices) {
    //         sortedList.add(e.element);
    //     }

    //     return sortedList;
    // }


    private static class ElementWithIndex {
        Map<String, String> element;
        int index;
        int rawIndex;

        ElementWithIndex(Map<String, String> element, int index, int rawIndex) {
            this.element = element;
            this.index = index;
            this.rawIndex = rawIndex;
        }
    }
    public static List<Map<String, String>> sortTable(List<Map<String, String>> table, String content) {
        List<ElementWithIndex> elementsWithIndices = new ArrayList<>();
        
        // Create a list of elements with their indices in the content
        for (int i = 0; i < table.size(); i++) {
            Map<String, String> entry = table.get(i);
            String name = entry.get("name");
            if (name != null) {
                int index = content.indexOf(name);
                if (index != -1) {
                    elementsWithIndices.add(new ElementWithIndex(entry, index, i));
                    content = content.substring(0, index) + new String(new char[name.length()]).replace("\0", " ") + content.substring(index + name.length());
                }
            }
        }

        // Sort the list based on the indices in content
        elementsWithIndices.sort(Comparator.comparingInt(e -> e.index));

        // Extract the sorted elements
        List<Map<String, String>> sortedList = new ArrayList<>();
        for (ElementWithIndex e : elementsWithIndices) {
            sortedList.add(e.element);
        }

        return sortedList;
    }



    public static List<String> extractSecondElements(ArrayList<ArrayList<String>>list) {
        List<String> extractedList = new ArrayList<>();

        // 遍历原始列表并提取每个子列表的第二个元素
        for (List<String> sublist : list) {
            if (sublist.size() > 1) {
                extractedList.add(sublist.get(1));
            }
        }

        return extractedList;
    }
    public static String root="/data1/hhj/project/doc_extract_v_4/";

    public static List<String> doc_type_list = new ArrayList<>(Arrays.asList(
        "admit_info",// 0
        "discharge_info",// 1 
        "first_case_info",// 2
        "operation_info",// 3
        "case_info", // 4 hhj 
        "inform_info", // 5 hhj
        "postoperative_first_case_info"// 6 hhj
        ));
    public static String doc_type = doc_type_list.get(0);

    // public static String xml_path = root + "xml_fac/党入院记录.xml";

    public static String json_path = root + "input/"+doc_type+"_input.json";

    public static void main(String[] args) throws Exception  {


        
        //提取入参中的有效部分
        String json_content = InfoExtracter.getContentFromInputJsonDir(json_path);
        //    List<String> rawSegKeySet = Admit_Info.get_rawSegKeySet();

        //判断doc_type
        System.out.println(doc_type);
        doc_type=InfoExtracter.Get_docType(json_content);
        System.out.println(doc_type);
        System.out.println("------------------------------------------------#########");

        //根据文书类型，从数据库提取相应的模板
        DatabaseConnector.MyResult result = DatabaseConnector.get_rawSegKeySet(doc_type, json_content);//这里改写了函数，增加了入参：doc_type

        List<String> rawSegKeySet = result.rawSegKeySet;
        System.out.println(rawSegKeySet);
        System.out.println("------------------------------------------------rawSegKeySet↑");

        // //根据文书类型，从数据库提取相应的模板
        // DatabaseConnector.Table table = DatabaseConnector.get_Table(doc_type, json_content);//这里改写了函数，增加了入参：doc_type

        // List<Map<String, String>>  all_table = table.table;
        // System.out.println(all_table);
        // System.out.println("------------------------------------------------all_table↑");
    }
}

