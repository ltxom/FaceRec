package me.ltxom.facerec.service;

import me.ltxom.facerec.FacerecApplication;
import me.ltxom.facerec.util.OSSClientUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.*;

@Service
public class SourceImageService {

   public String saveFileToOSS(File file) {
      try {
         return OSSClientUtil.upload(file);
      } catch (IOException e) {
         e.printStackTrace();
         return null;
      }
   }


   public JSONArray getSourceImageInfo(String ossUrl) {
      String apiKey = "ad488703e2054f11aa1019daa5102bed";
      String uriBase =
              "https://westcentralus.api.cognitive.microsoft.com/face/v1.0/detect";
      String faceAttributes =
              "age,gender,headPose,smile,facialHair,glasses,emotion,hair,makeup,occlusion," +
                      "accessories,blur,exposure,noise";


      HttpClient httpclient = new DefaultHttpClient();
      ossUrl = "{\"url\":\"" + ossUrl + "\"}";

      try {
         URIBuilder builder = new URIBuilder(uriBase);

         // Request parameters. All of them are optional.
         builder.setParameter("returnFaceId", "true");
         builder.setParameter("returnFaceLandmarks", "false");
         builder.setParameter("returnFaceAttributes", faceAttributes);

         // Prepare the URI for the REST API call.
         URI uri = builder.build();
         HttpPost request = new HttpPost(uri);

         // Request headers.
         request.setHeader("Content-Type", "application/json");
         request.setHeader("Ocp-Apim-Subscription-Key", apiKey);

         // Request body.
         StringEntity reqEntity = new StringEntity(ossUrl);
         request.setEntity(reqEntity);

         // Execute the REST API call and get the response entity.
         HttpResponse response = httpclient.execute(request);
         HttpEntity entity = response.getEntity();
         if (entity != null) {
            // Format and display the JSON response.
            System.out.println("REST Response:\n");


            String jsonString = EntityUtils.toString(entity).trim();
            if (jsonString.charAt(0) == '[') {
               JSONArray jsonArray = new JSONArray(jsonString);
               return jsonArray;
            } else if (jsonString.charAt(0) == '{') {
               JSONObject jsonObject = new JSONObject(jsonString);

            } else {
               System.out.println(jsonString);
            }
         }
      } catch (UnsupportedEncodingException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } catch (URISyntaxException e) {
         e.printStackTrace();
      } catch (JSONException e) {
         e.printStackTrace();
      }
      return null;
   }

   public void saveFaceImgInfo(JSONArray jsonArray, String fileName, boolean isSource) {
      if (jsonArray != null)
         try {
            File file = null;
            if (isSource) file = new File("data" +
                    "/source/" + fileName + ".json");
            else file = new File("data" +
                    "/" + fileName + ".json");
            if (file.exists())
               file.delete();
            PrintWriter printWriter = new PrintWriter(file);
            printWriter.print(jsonArray.toString(2));
            printWriter.close();

            Map<String, String> map = new HashMap<>();
            for (int i = 0; i < jsonArray.length(); i++) {
               map.put(jsonArray.getJSONObject(i).get("faceId").toString(),
                       fileName
               );
            }
            appendFaceIdToNameMap(map, isSource);

         } catch (IOException e) {
            e.printStackTrace();
         }
   }

   public JSONArray getSourceInfoByName(String fileName) {
      try {
         BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
         StringBuilder sb = new StringBuilder();
         String line = "";
         while ((line = bufferedReader.readLine()) != null)
            sb.append(line);
         JSONArray jsonArray = new JSONArray(sb.toString());
         return jsonArray;
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }
      return null;
   }

   public void appendFaceIdToNameMap(Map<String, String> map, boolean isSource) {
      PrintWriter printWriter = null;
      try {
         printWriter = new PrintWriter(new FileOutputStream(new File(isSource ? "data/map" +
                 ".properties" : "data/attendance.properties")
                 , isSource));
         for (String key : map.keySet()) {
            printWriter.println(key + "=" + map.get(key));
         }
         printWriter.flush();
         printWriter.close();
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      }
   }

   public Map<String, String> getFaceIdToNameMap(boolean isSource) {
      Properties prop = new Properties();
      InputStream in = null;
      Map<String, String> map = new HashMap<>();
      try {
         in = new FileInputStream(new File(isSource ? "data/map.properties" : "data" +
                 "/attendance.properties"));
         prop.load(in);
         Set keyValue = prop.keySet();
         for (Iterator it = keyValue.iterator(); it.hasNext(); ) {
            String key = (String) it.next();
            map.put(key, prop.getProperty(key));
         }
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }
      return map;
   }

   public File convertAndSave(MultipartFile file, String savePath) {
      File convFile = new File(savePath);
      try {
         convFile.createNewFile();
         FileOutputStream fos = new FileOutputStream(convFile);
         fos.write(file.getBytes());
         fos.close();
      } catch (IOException e) {
         e.printStackTrace();
      }


      return convFile;
   }

   public Map<String, Boolean> checkAttendance() {
      Map<String, String> sourceMap = getFaceIdToNameMap(true);

      Object[] candidateFaces = sourceMap.keySet().toArray();
      Map<String, String> attendanceMap = getFaceIdToNameMap(false);
      Map<String, Boolean> resultMap = new HashMap<>();

      String apiKey = "ad488703e2054f11aa1019daa5102bed";
      String uriBase =
              "https://westcentralus.api.cognitive.microsoft.com/face/v1.0/findsimilars";

      for (String attendanceKey : attendanceMap.keySet()) {
         HttpClient httpclient = new DefaultHttpClient();

         try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("faceId", attendanceKey);
            JSONArray candidateFacesArr = new JSONArray(candidateFaces);
            jsonObject.put("faceIds", candidateFacesArr);

            URIBuilder builder = new URIBuilder(uriBase);


            // Prepare the URI for the REST API call.
            URI uri = builder.build();
            HttpPost request = new HttpPost(uri);

            // Request headers.
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Ocp-Apim-Subscription-Key", apiKey);
            // Request body.
            StringEntity reqEntity = new StringEntity(jsonObject.toString());
            request.setEntity(reqEntity);
            // Execute the REST API call and get the response entity.
            HttpResponse response = httpclient.execute(request);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
               // Format and display the JSON response.
               String jsonString = EntityUtils.toString(entity).trim();
               if (jsonString.charAt(0) == '[') {
                  JSONArray jsonArray = new JSONArray(jsonString);
                  if (jsonArray.length() > 0)
                     if ((Double) jsonArray.getJSONObject(0).get("confidence") > FacerecApplication.CONFIDENCE) {
                        resultMap.put(sourceMap.get((String) jsonArray.getJSONObject(0).get(
                                "faceId")).split("\\.")[0], true);
                     }
               } else if (jsonString.charAt(0) == '{') {
                  jsonObject = new JSONObject(jsonString);
               }
            }
         } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
         } catch (IOException e) {
            e.printStackTrace();
         } catch (URISyntaxException e) {
            e.printStackTrace();
         } catch (JSONException e) {
            e.printStackTrace();
         }
      }

      for (String sourceFaceName : sourceMap.values()) {
         sourceFaceName = sourceFaceName.split("\\.")[0];
         if (!resultMap.containsKey(sourceFaceName))
            resultMap.put(sourceFaceName, false);
      }

      PrintWriter printWriter = null;
      try {
         printWriter = new PrintWriter(new FileOutputStream(new File("data/result.properties")));
         for (String key : resultMap.keySet()) {
            printWriter.println(key + "=" + resultMap.get(key));
         }
         printWriter.flush();
         printWriter.close();
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      }

      return resultMap;
   }


   public Map<String, Boolean> getResultMap() {
      Properties prop = new Properties();
      InputStream in = null;
      Map<String, Boolean> map = new HashMap<>();
      try {
         in = new FileInputStream(new File("data/result.properties"));
         prop.load(in);
         Set keyValue = prop.keySet();
         for (Iterator it = keyValue.iterator(); it.hasNext(); ) {
            String key = (String) it.next();
            map.put(key, Boolean.valueOf(prop.getProperty(key)));
         }
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }
      return map;
   }


   public static void main(String[] args) throws IOException {
//      File file = new File("img/source/garyfam.jpeg");
//      System.out.println(file.getName());
//      SourceImageService.saveFileToOSS(file);
//
//      JSONArray jsonArray = SourceImageService.getSourceImageInfo("https://facerec" +
//              ".oss-us-west-1.aliyuncs" +
//              ".com/"+file.getName());
//      SourceImageService.saveSourceInfoToLocal(jsonArray, file.getName());
//
//      JSONArray jsonArray = getSourceInfoByName("data/source/"+file.getName()+".json");
//      for(int i = 0; i < jsonArray.length(); i++){
//         JSONObject jsonObject = jsonArray.getJSONObject(i);
//         System.out.println(jsonObject.get("faceId"));
//      }

//      getFaceIdToNameMap();

//      Map<String, String> map = new HashMap<>();
//      map.put("a", "b");
//      appendFaceIdToNameMap(map);

      SourceImageService sourceImageService = new SourceImageService();
//      JSONArray jsonArray = sourceImageService.getSourceImageInfo("https://facerec" +
//              ".oss-us-west-1.aliyuncs.com/JAPAN111.jpg");
//      sourceImageService.saveSourceInfoToLocal(jsonArray, "Japan.jpg");


      System.out.println(sourceImageService.checkAttendance().toString());
   }
}
