package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class DatabaseConnector_old {

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
        String url = "jdbc:mysql://111.9.47.74:13000/emr_parser"; // 根据你的数据库配置修改
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
        sortListByContentOrder(normResultGuide,content);
        rawSegKeySet = extractSecondElements(normResultGuide);


        return new MyResult(rawSegKeySet, normResultGuide);
    }

    // public static ArrayList<ArrayList<String>> get_normResultGuide() {
    //     ArrayList<ArrayList<String>> normResultGuide = new ArrayList<>();


    //     return normResultGuide;
    // }




    public static void sortListByContentOrder(ArrayList<ArrayList<String>> list, String content) {


        // // 创建一个映射来存储每个元素第二项的顺序
        // Map<String, Integer> orderMap = new HashMap<>();
        // int position = 0;

        // // 遍历content并记录每个关键词的位置
        // for (List<String> item : list) {
        //     String key = item.get(1);
        //     int index = content.indexOf(key);
        //     if (index != -1) {
        //         orderMap.put(key, index);
        //     }
        // }

        // // 使用自定义比较器对列表进行排序
        // Collections.sort(list, new Comparator<List<String>>() {
        //     @Override
        //     public int compare(List<String> o1, List<String> o2) {
        //         return Integer.compare(orderMap.get(o1.get(1)), orderMap.get(o2.get(1)));
        //     }
        // });
    }




    public static List<String> sortByContentOrder(List<String> rawSegKeySet, String content) {
        List<ElementWithIndex> elementsWithIndices = new ArrayList<>();

        // Create a list of elements with their indices in rawSegKeySet
        for (int i = 0; i < rawSegKeySet.size(); i++) {
            String key = rawSegKeySet.get(i);
            int index = content.indexOf(key);
            if (index != -1) {
                elementsWithIndices.add(new ElementWithIndex(key, index, i));
                content = content.substring(0, index) + new String(new char[key.length()]).replace("\0", " ") + content.substring(index + key.length());
            }
        }

        // Sort the list based on the indices in content
        elementsWithIndices.sort(Comparator.comparingInt(e -> e.index));

        // Extract the sorted elements
        List<String> sortedList = new ArrayList<>();
        for (ElementWithIndex e : elementsWithIndices) {
            sortedList.add(e.element);
        }

        return sortedList;
    }

    private static class ElementWithIndex {
        String element;
        int index;
        int rawIndex;

        ElementWithIndex(String element, int index, int rawIndex) {
            this.element = element;
            this.index = index;
            this.rawIndex = rawIndex;
        }
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

    public static void main(String[] args) {
    }
}

