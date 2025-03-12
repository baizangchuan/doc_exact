package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DocTypeDetector {
    
    /**
     * 根据内容自动检测最匹配的文档类型
     * 
     * @param content 文档内容
     * @param url 数据库URL
     * @param user 数据库用户名
     * @param password 数据库密码
     * @return 最匹配的文档类型编码
     */
    public static String detectDocType(String content, String url, String user, String password) {
        // 获取所有模板的键列表
        Map<String, List<String>> allTemplateKeys = getAllTemplateKeys(url, user, password);
        
        if (allTemplateKeys.isEmpty()) {
            System.out.println("未能从数据库获取模板信息");
            return "";
        }
        
        // 计算每个模板的匹配数量
        Map<String, Integer> matchCounts = calculateMatchCounts(allTemplateKeys, content);
        
        // 找出匹配数量最多的模板
        String bestMatchTemplate = "";
        int highestCount = 0;
        int minDifference = Integer.MAX_VALUE;
        
        // 先找出匹配数量与总关键词数之差最小的模板中的最大匹配数
        for (Map.Entry<String, Integer> entry : matchCounts.entrySet()) {
            String templateCode = entry.getKey();
            int matchCount = entry.getValue();
            int totalKeys = allTemplateKeys.get(templateCode).size();
            int difference = totalKeys - matchCount;
            
            System.out.println("模板 " + templateCode + " 的匹配数量: " + matchCount + 
                              " (总关键词: " + totalKeys + ", 差值: " + difference + ")");
            
            // 如果差值小于3，考虑这个模板
            if (difference < 3) {
                if (matchCount > highestCount) {
                    highestCount = matchCount;
                    bestMatchTemplate = templateCode;
                    minDifference = difference;
                } else if (matchCount == highestCount && difference < minDifference) {
                    // 如果匹配数相同，选择差值更小的
                    bestMatchTemplate = templateCode;
                    minDifference = difference;
                }
            }
        }
        
        // 如果没有找到差值小于3的模板，就选择匹配数量最多的
        if (bestMatchTemplate.isEmpty()) {
            for (Map.Entry<String, Integer> entry : matchCounts.entrySet()) {
                if (entry.getValue() > highestCount) {
                    highestCount = entry.getValue();
                    bestMatchTemplate = entry.getKey();
                }
            }
        }
        
        System.out.println("最佳匹配模板: " + bestMatchTemplate + ", 匹配数量: " + highestCount);
        return bestMatchTemplate;
    }
    
    /**
     * 从数据库获取所有模板的键列表
     */
    private static Map<String, List<String>> getAllTemplateKeys(String url, String user, String password) {
        Map<String, List<String>> templateKeys = new HashMap<>();
        
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DriverManager.getConnection(url, user, password);
            
            // 查询所有模板的template_config_code和config_node_key，排除config_node_key为'type'的行（不区分大小写）
            String sql = "SELECT template_config_code, config_node_key FROM new_template WHERE LOWER(config_node_key) != 'type'";
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
            
            while (resultSet.next()) {
                String templateCode = resultSet.getString("template_config_code");
                String configKey = resultSet.getString("config_node_key");
                
                if (templateCode != null && configKey != null) {
                    // 将键添加到对应模板的列表中
                    templateKeys.computeIfAbsent(templateCode, k -> new ArrayList<>()).add(configKey);
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return templateKeys;
    }
    
    /**
     * 计算每个模板的匹配数量
     */
    private static Map<String, Integer> calculateMatchCounts(Map<String, List<String>> allTemplateKeys, String content) {
        Map<String, Integer> matchCounts = new HashMap<>();
        
        for (Map.Entry<String, List<String>> entry : allTemplateKeys.entrySet()) {
            String templateCode = entry.getKey();
            List<String> keys = entry.getValue();
            
            if (keys.isEmpty()) {
                matchCounts.put(templateCode, 0);
                continue;
            }
            
            int matchCount = 0;
            for (String key : keys) {
                if (isKeyInContent(key, content)) {
                    matchCount++;
                }
            }
            
            // 存储匹配数量
            matchCounts.put(templateCode, matchCount);
        }
        
        return matchCounts;
    }
    
    /**
     * 检查键是否在内容中出现
     */
    private static boolean isKeyInContent(String key, String content) {
        // 尝试作为正则表达式匹配
        try {
            Pattern pattern = Pattern.compile(key);
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                return true;
            }
        } catch (Exception e) {
            // 如果正则表达式无效，则尝试普通字符串匹配
        }
        
        // 尝试普通字符串匹配
        return content.contains(key);
    }
} 