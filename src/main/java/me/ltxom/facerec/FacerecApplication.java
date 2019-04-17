package me.ltxom.facerec;

import org.json.JSONArray;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FacerecApplication {
   public final static float CONFIDENCE = 0.30f;

   public static void main(String[] args) {

      SpringApplication.run(FacerecApplication.class, args);

   }
}
