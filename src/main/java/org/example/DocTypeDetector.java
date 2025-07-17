package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        double bestScore = 0.0;  // 使用分数机制，考虑匹配数量和差值
        
        for (Map.Entry<String, Integer> entry : matchCounts.entrySet()) {
            String templateCode = entry.getKey();
            int matchCount = entry.getValue();
            int totalKeys = allTemplateKeys.get(templateCode).size();
            int difference = totalKeys - matchCount;
            
            // 计算匹配分数：匹配数量占主导地位，差值作为权重因子
            double matchScore = matchCount * (1.0 / (1 + difference * 0.1));
            
            System.out.println("模板 " + templateCode + " 的匹配数量: " + matchCount + 
                             " (总关键词: " + totalKeys + ", 差值: " + difference + 
                             ", 分数: " + String.format("%.2f", matchScore) + ")");
            
            if (matchScore > bestScore) {
                bestScore = matchScore;
                highestCount = matchCount;
                bestMatchTemplate = templateCode;
            }
        }
        
        System.out.println("最佳匹配模板: " + bestMatchTemplate + ", 匹配数量: " + highestCount + 
                         ", 最终分数: " + String.format("%.2f", bestScore));
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
            
            // 使用Map<String, Set<String>>临时存储，以实现去重
            Map<String, Set<String>> tempKeys = new HashMap<>();
            
            // 查询所有模板的template_config_code和config_node_key，排除config_node_key为'type'的行（不区分大小写）
            String sql = "SELECT template_config_code, config_node_key FROM new_template WHERE LOWER(config_node_key) != 'type'";
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
            
            while (resultSet.next()) {
                String templateCode = resultSet.getString("template_config_code");
                String configKey = resultSet.getString("config_node_key");
                
                if (templateCode != null && configKey != null) {
                    // 使用Set来自动去重
                    tempKeys.computeIfAbsent(templateCode, k -> new HashSet<>()).add(configKey);
                }
            }
            
            // 将Set转换为List
            for (Map.Entry<String, Set<String>> entry : tempKeys.entrySet()) {
                templateKeys.put(entry.getKey(), new ArrayList<>(entry.getValue()));
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