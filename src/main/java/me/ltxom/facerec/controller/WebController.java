package me.ltxom.facerec.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.ltxom.facerec.service.SourceImageService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Map;

@RestController
public class WebController {
   @Autowired
   private SourceImageService sourceImageService;

   @PostMapping(value = "/student")
   public String uploadStudent(@RequestParam("callback") String jsonpCallback,
                               @RequestParam String name,
                               @RequestParam() MultipartFile mFile) {
      File file = sourceImageService.convertAndSave(mFile,
              "img/source/" + name + ".jpg");
      sourceImageService.saveFileToOSS(file);
      JSONArray jsonArray = sourceImageService.getSourceImageInfo("https://facerec" +
              ".oss-us-west-1.aliyuncs.com/" + file.getName());
      sourceImageService.saveFaceImgInfo(jsonArray, name + ".jpg", true, true);

      return convertToJsonP(jsonArray.toString(2), jsonpCallback);
   }

   @GetMapping(value = "/students")
   public String getStudents(@RequestParam("callback") String jsonpCallback) {
      JSONArray jsonArray = new JSONArray();
      Map<String, String> map = sourceImageService.getFaceIdToNameMap(true);
      int id = 0;
      for (String key : map.keySet()) {
         JSONObject jsonObject = new JSONObject();
         jsonObject.put("id", id++);
         jsonObject.put("name", map.get(key).split("\\.")[0]);
         jsonObject.put("url", map.get(key));
         jsonArray.put(jsonObject);
      }
      return convertToJsonP(jsonArray.toString(2), jsonpCallback);
   }

   @PostMapping(value = "/auto-update")
   public String checkFaces(@RequestParam("callback") String jsonpCallback,
                            @RequestParam() MultipartFile mFile) {
      File file = sourceImageService.convertAndSave(mFile,
              "img/" + "checkData.jpg");
      sourceImageService.saveFileToOSS(file);

      JSONArray jsonArray = sourceImageService.getSourceImageInfo("https://facerec" +
              ".oss-us-west-1.aliyuncs.com/" + file.getName());
      sourceImageService.saveFaceImgInfo(jsonArray, "checkData.jpg", false, true);

      Map<String, Boolean> map = sourceImageService.checkAttendance();

      JSONObject resultObj = new JSONObject();

      JSONArray presentArr = new JSONArray();
      JSONArray absentArr = new JSONArray();

      for (String key : map.keySet()) {
         JSONObject tempObj = new JSONObject();
         tempObj.put("name", key.split("\\.")[0]);
         tempObj.put("imageLink",
                 "https://facerec" + ".oss-us-west-1.aliyuncs.com/" + key);
         if (map.get(key)) {
            presentArr.put(tempObj);
         } else {
            absentArr.put(tempObj);
         }
      }

      resultObj.put("present", presentArr);
      resultObj.put("absent", absentArr);
      resultObj.put("rendered-photo", "https://facerec" +
              ".oss-us-west-1.aliyuncs.com/" + "renderedData.jpg");
      return convertToJsonP(resultObj.toString(2), jsonpCallback);
   }

   @GetMapping(value = "/attendance")
   public String getAttendance(@RequestParam("callback") String jsonpCallback) {
      Map<String, Boolean> map = sourceImageService.getResultMap();
      JSONObject resultObj = new JSONObject();

      JSONArray presentArr = new JSONArray();
      JSONArray absentArr = new JSONArray();

      for (String key : map.keySet()) {
         JSONObject tempObj = new JSONObject();
         tempObj.put("name", key.split("\\.")[0]);
         {
         }
         tempObj.put("imageLink",
                 "https://facerec" + ".oss-us-west-1.aliyuncs.com/" + key);
         if (map.get(key)) {
            presentArr.put(tempObj);
         } else {
            absentArr.put(tempObj);
         }
      }

      resultObj.put("present", presentArr);
      resultObj.put("absent", absentArr);
      resultObj.put("rendered-photo", "https://facerec" +
              ".oss-us-west-1.aliyuncs.com/" + "renderedData.jpg");
      return convertToJsonP(resultObj.toString(2), jsonpCallback);
   }

   @RequestMapping(value = "/attendance")
   public String setAttendance(@RequestParam("callback") String jsonpCallback,
                               @RequestParam String name, @RequestParam Boolean flag) {
      Map<String, Boolean> map = sourceImageService.getResultMap();

      if (map.containsKey(name)) {
         PrintWriter printWriter = null;
         map.put(name, flag);
         try {
            printWriter = new PrintWriter(new FileOutputStream(new File("data/result" +
                    ".properties")));
            for (String key : map.keySet()) {
               printWriter.println(key + "=" + map.get(key));
            }
            printWriter.flush();
            printWriter.close();
         } catch (FileNotFoundException e) {
            e.printStackTrace();
         }
         return convertToJsonP("修改成功", jsonpCallback);
      } else {
         return convertToJsonP("名称不存在！请先录入.", jsonpCallback);
      }

   }


   private String convertToJsonP(Object o, String jsonpCallback) {
      String outputmessage = null;
      ObjectMapper mapper = new ObjectMapper();
      try {
         outputmessage = mapper.writeValueAsString(o);
      } catch (Exception e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      if (outputmessage != null) {
         outputmessage = jsonpCallback + "(" + outputmessage + ")";
      }
      return outputmessage;
   }
}
