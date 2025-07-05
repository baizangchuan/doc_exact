import requests
import json
import os

data = {
    "args": {
        "content":"    转出记录	患者{姓名、{RB_表inchinfo-性别}、{RB_表inchinfo-年龄}。因“{RB_主诉}”于{RB_表inchinfo-入院日期}入院。    入院情况：    入院诊断：{入院诊断}    诊疗经过：    目前情况：    目前诊断：    转科目的及注意事项：  			{医师签名}",
        "hospital": "415801168",
        "tmpl_type": "txt",
        "multi_task": True,
        "record_type": None,
        "extract_title": True,
        "load_latest_config": False
    }
}

url = "http://localhost:8083/api/process"
# url = "http://111.9.47.74:8083/api/process"

response = requests.post(url,json=data)
# 打印响应状态码
print(f"Status Code: {response.status_code}")

# 打印响应的JSON内容并保存到文件
if response.status_code == 200:
    print("Response JSON:")
    print(response.text)
    
    # 确保输出目录存在
    output_dir = "/Users/baicangchuan/Library/Containers/com.tencent.xinWeChat/Data/Library/Application Support/com.tencent.xinWeChat/2.0b4.0.9/da6364f4fd6b21dd7c3bbfa91da91fa1/Message/MessageTemp/e41b622775d870723b8550357fa73b29/File/Last2"
    os.makedirs(output_dir, exist_ok=True)
    
    # 解析响应内容为JSON对象
    response_json = response.json()
    
    # 保存格式化的JSON到文件
    output_file = os.path.join(output_dir, "test_output.json")
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(response_json, f, ensure_ascii=False, indent=4)
    print(f"Response has been saved to {output_file}")
else:
    print("Failed to get a valid response")
    print(response.text)