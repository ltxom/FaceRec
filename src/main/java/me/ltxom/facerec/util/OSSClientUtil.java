package me.ltxom.facerec.util;

import com.aliyun.oss.OSSClient;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class OSSClientUtil {
   public static String upload(File file) throws IOException {
      // Endpoint以杭州为例，其它Region请按实际情况填写。
      String endpoint = "http://oss-us-west-1.aliyuncs.com";
      // 阿里云主账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM账号进行API访问或日常运维，请登录 https://ram
      // .console.aliyun.com 创建RAM账号。
      String accessKeyId = "LTAIxvMKqbAFcoXx";
      String accessKeySecret = "PYGBWpc1eFjyit6MPGYHjbabz4a9vH";
      String bucketName = "facerec";

      // 创建OSSClient实例。
      OSSClient ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);

      // 上传内容到指定的存储空间（bucketName）并保存为指定的文件名称（objectName）。

      ossClient.putObject(bucketName, file.getName(),
              new ByteArrayInputStream(Files.readAllBytes(file.toPath())));

      // 关闭OSSClient。
      ossClient.shutdown();

      return "https://oss-us-west-1.aliyuncs.com/"+file.getName();
   }

}