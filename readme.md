文件上传，取scheme：

解析接口：
解析函数：Main.java
解析部署：/Users/baicangchuan/Desktop/Last/src/main/java/org/example/ExampleController.java


# 流程策略图
文件上传，取scheme：
Main.java


# debug日志
调通文件上传和解析
梳理一遍解析逻辑
找到相关上传的测试用文件
上传后观察测试表的信息,看解析结果，准确率，分析不准的原因

## 测试
将原表中涉及的表备份，删除
批量上传文件，建新表
测试解析接口得到的结果


## 2025-02-20 
Main.java可以出结果，不过出参数目是否对上了尚不清楚

- 检查线上策略用的表是否和本地用的表一样
- 是否是文件上传模块有问题，导致缺少对应的表
- 