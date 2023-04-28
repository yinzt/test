package com.xinchuang.common.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class OcrUtils {

    public static final String username = "elasticsearch";
    public static final String type = "8999"; //通用类型


    public static void main(String[] args) throws IOException {

        String fileName = "E:\\学习文档\\图片\\集成易混淆知识点.pdf";
        String orcUrl = "http://103.85.176.30:10007/icr/recognize_document_multi";



        System.out.println("main:"+heHeOcr(fileName,orcUrl));

    }

    public static String heHeOcr(String fileName,String ocrUrl){
        try{
            File file = new File(fileName);
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(10000).setConnectionRequestTimeout(20000).build();
            MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
            multipartEntityBuilder.addBinaryBody("file",file);
            HttpEntity httpEntity = multipartEntityBuilder.build();
            HttpPost httpPost = new HttpPost(ocrUrl);
            httpPost.setConfig(requestConfig);
            httpPost.setEntity(httpEntity);
            HttpResponse response = httpClient.execute(httpPost);
            if(response == null || response.getEntity() == null){
                log.info("调用合合信息返回结果为空,fileName:{},ocrUrl:{}",fileName,ocrUrl);
                return null;
            }
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity responseEntity = response.getEntity();
            String result = EntityUtils.toString(responseEntity, Charset.forName("UTF-8"));
            log.info("调用合合信息返回结果信息statusCode:{},result:{},fileName:{},ocrUrl:{}",statusCode,result,fileName,ocrUrl);
            List<String> textList = new ArrayList<>();
            if (statusCode == HttpStatus.SC_OK) {
                JSONObject jsonObject = JSONObject.parseObject(result);
                if(jsonObject == null){
                    return null;
                }
                JSONArray jsonArray = jsonObject.getJSONArray("pages");
                for(Object obj : jsonArray){
                    JSONObject tmpJson = JSONObject.parseObject(obj.toString());
                    JSONArray tmpArray = tmpJson.getJSONArray("linesText");
                    if (CollectionUtils.isEmpty(tmpArray)){
                        continue;
                    }
                    List<String> tmpList = tmpArray.stream().filter(item->!StringUtils.isEmpty(item)).map(item->item.toString()).collect(Collectors.toList());
                    if(!CollectionUtils.isEmpty(tmpList)){
                        textList.addAll(tmpList);
                    }
                }
            }
            if(CollectionUtils.isEmpty(textList)){
                return null;
            }
            return String.join(",",textList);
        } catch (Exception e){
            log.error("调用合合信息ocr失败,fileName:{},ocrUrl:{}",fileName,ocrUrl,e);
        }
        return null;
    }

    ////  imgtype图片类型       paramdata图像文件base64编码与其他参数的拼接
    public static String requestOcrServer(String imgtype,String paramdata,String ocrUrl){
        JSONObject json = new JSONObject();
        json.put("username", username);
        json.put("paramdata", paramdata);
        json.put("signdata", "NULL");
        json.put("imgtype", imgtype);
        // 3.将json对象发送到url中,返回String类型
        String resultback = doPost(ocrUrl, json);
        StringBuffer stringBuffer = new StringBuffer();
        try {
            Gson gson = new Gson();
            JSONObject jsonObject = gson.fromJson(resultback, JSONObject.class);
            JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONObject("cardsinfo").getJSONObject("card").getJSONArray("rowitem");
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONArray jsonInnerArray = jsonArray.getJSONObject(i).getJSONObject("rowContext").getJSONArray("charitem");
                for (int i1 = 0; i1 < jsonInnerArray.size(); i1++) {
                    String charValue = jsonInnerArray.getJSONObject(i1).getString("charValue");
                    stringBuffer.append(charValue).append(" ");
                }
            }
            return stringBuffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultback;
    }
    public static String requestOcrServerV2(String imgtype,String paramdata,String ocrUrl){
        JSONObject json = new JSONObject();
        /*json.put("username", username);
        json.put("paramdata", paramdata);
        json.put("signdata", "NULL");
        json.put("imgtype", imgtype);*/
        // 3.将json对象发送到url中,返回String类型
        String resultback = doPost(ocrUrl, json);
        if(imgtype.endsWith("pdf")){
           
            byte[] bs=java.util.Base64.getDecoder().decode(resultback);
            InputStream buffer=new ByteArrayInputStream(bs);
            return TikaUtils.parseWord(buffer, imgtype);
        }
        StringBuffer stringBuffer = new StringBuffer();
        try {
            Gson gson = new Gson();
            JSONObject jsonObject = gson.fromJson(resultback, JSONObject.class);
            JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONObject("cardsinfo").getJSONObject("card").getJSONArray("rowitem");
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONArray jsonInnerArray = jsonArray.getJSONObject(i).getJSONObject("rowContext").getJSONArray("charitem");
                for (int i1 = 0; i1 < jsonInnerArray.size(); i1++) {
                    String charValue = jsonInnerArray.getJSONObject(i1).getString("charValue");
                    stringBuffer.append(charValue).append(" ");
                }
            }
            return stringBuffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultback;
    }

    public static String doPost(String url,JSONObject json){
        String result="";
        CloseableHttpClient client = HttpClientBuilder.create().build();                //1.创建httpclient对象
        HttpPost post = new HttpPost(url);                                              //2.通过url创建post方法
        try {
            StringEntity entity = new StringEntity(json.toString());                   //3.将传入的json封装成实体，并压入post方法
            entity.setContentEncoding("UTF-8");
            entity.setContentType("application/json");//发送json数据需要设置contentType
            post.setEntity(entity);
            CloseableHttpResponse response = client.execute(post);                     //4.执行post方法，返回HttpResponse的对象
            if(response.getStatusLine().getStatusCode() == 200){
                result = EntityUtils.toString(response.getEntity(),"UTF-8");             //5.如果返回结果状态码为200，则读取响应实体response对象的实体内容，并封装成String对象返回
            }
            try{
                HttpEntity e = response.getEntity();                               //6.关闭资源
                if(e != null){
                    InputStream instream = e.getContent();
                    instream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }finally{
                response.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    //imagePath 图片绝对路径   type 证件类型;二代证2;行驶证6;驾照5;银行卡17;车牌19;如“2”;
    //option “”    固定双引号    password  null
    public static String GetBase64StrFromImage(String imagePath, String option, String password) {
        String imgFile = imagePath;
        InputStream in = null;
        byte[] data = (byte[])null;
        try {
            in = new FileInputStream(imgFile);
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(data)+ "==##" + type + "==##" + option + "==##" + password;
    }
}
