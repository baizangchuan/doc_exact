package org.example;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/api")
public class ExampleController {
    // 动态配置
    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String user;

    @Value("${spring.datasource.password}")
    private String password;

    @PostMapping("/process")
    public ResponseEntity<StreamingResponseBody> processPostRequest(@RequestBody Request request) {
//        //数据库配置
//        String url = "jdbc:mysql://111.9.47.74:13000/emr_parser";
//        String user = "root";
//        String password = "Aliab12!2020";

        String Content = request.getArgs().getContent();
        // 去除掉jsonContent的所有空格
        String content = Content.replaceAll("\u3000", "");
        String jsonContent = content.replaceAll("\\u0020", "");


        //    List<String> rawSegKeySet = Admit_Info.get_rawSegKeySet();
        JSONObject finalData=Main.text_to_FinalData(jsonContent, url, user, password);
        System.out.println(finalData);

        // String doc_type;

        // String Content = request.getArgs().getContent();
        // // 去除掉jsonContent的所有空格
        // String content = Content.replaceAll("\u3000", "");
        // String jsonContent = content.replaceAll("\\u0020", "");



        // doc_type = InfoExtracter.Get_docType(jsonContent);
        // // int key_metrics_flag = Template.Get_key_metrics_flag(doc_type);
        // DatabaseConnector_old.MyResult result = DatabaseConnector_old.get_rawSegKeySet(doc_type, jsonContent);
        // List<String> rawSegKeySet = result.rawSegKeySet;
        // //验证数据库中模板的关键词在入参中都存在
        // rawSegKeySet = InfoExtracter.Get_Effective_RawSegKeySet(rawSegKeySet, jsonContent);
        // rawSegKeySet = DatabaseConnector_old.sortByContentOrder(rawSegKeySet, jsonContent);
        // List<List<String>> rawSeg = InfoExtracter.getRawSeg(jsonContent, rawSegKeySet);

        // ArrayList<ArrayList<String>> normResultGuide = result.normResultGuide;
        // //将组合规则按照相同的schema组合为树状
        // List<List<Object>> schema_tree_list = InfoExtracter.mergeLists(normResultGuide);
        // //由组合规则对原始字段做组合拼凑
        // JSONObject normResult = InfoExtracter.getNormResultFromSchemaTreeList(schema_tree_list, rawSeg);

        // // if (key_metrics_flag == 1) {
        // //     List<String> keyList = new ArrayList<>(Arrays.asList("T", "P", "R", "BP"));
        // //     String endWord = "mmHg";
        // //     List<JSONObject> keyMetrics = InfoExtracter.subExtract(rawSeg, "体格检查", keyList, endWord);
        // //     normResult.put("key_metrics", keyMetrics);//这里的第四个结果缺个单位，要补一下
        // // }

        // //对每份模版的normResult根据doc_type做微调
        // Template.Template_modify(doc_type, normResult, rawSeg, jsonContent);

        // //生成出参内容
        // JSONObject finalData = Template.genFinalData(normResult, rawSeg, doc_type);
        // System.out.println(finalData);


        StreamingResponseBody body = outputStream -> {
            outputStream.write(finalData.toString().getBytes());
            outputStream.flush();
        };

        return ResponseEntity.ok(body);
    }
}
