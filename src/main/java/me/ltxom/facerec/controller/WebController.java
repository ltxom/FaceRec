package me.ltxom.facerec.controller;

import me.ltxom.facerec.service.SourceImageService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Map;

@RestController
public class WebController {
   @Autowired
   private SourceImageService sourceImageService;

   @PostMapping(value = "/student")
   public String uploadStudent(@RequestParam String name,
                               @RequestParam() MultipartFile mFile) {
      File file = sourceImageService.convertAndSave(mFile,
              "img/source/" + name + "." + mFile.getContentType().split("/")[1]);
      sourceImageService.saveFileToOSS(file);
      JSONArray jsonArray = sourceImageService.getSourceImageInfo("https://facerec" +
              ".oss-us-west-1.aliyuncs.com/" + file.getName());
      sourceImageService.saveFaceImgInfo(jsonArray, name + ".jpg", true);

      return jsonArray.toString(2);
   }

   @GetMapping(value = "/students")
   public String getStudents() {
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
      return jsonArray.toString(2);
   }

   @PostMapping(value = "/auto-update")
   public String checkFaces(@RequestParam() MultipartFile mFile) {
      File file = sourceImageService.convertAndSave(mFile,
              "img/" + "checkData.jpg");
      sourceImageService.saveFileToOSS(file);

      JSONArray jsonArray = sourceImageService.getSourceImageInfo("https://facerec" +
              ".oss-us-west-1.aliyuncs.com/" + file.getName());
      sourceImageService.saveFaceImgInfo(jsonArray, "checkData.jpg", false);

      JSONArray resultArray = new JSONArray();
      Map<String, Boolean> map = sourceImageService.checkAttendance();

      for (String key : map.keySet()) {
         JSONObject jsonObject = new JSONObject();
         jsonObject.put("name", key.split("\\.")[0]);
         jsonObject.put("attendance", map.get(key));
         resultArray.put(jsonObject);
      }
      return resultArray.toString(2);
   }
}
