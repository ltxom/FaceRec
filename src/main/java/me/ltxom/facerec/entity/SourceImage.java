package me.ltxom.facerec.entity;

import java.util.Map;

public class SourceImage {
   private String id;
   private Map<String, Integer> faceRectangle;
   private String name;

   public String getId() {
      return id;
   }

   public void setId(String id) {
      this.id = id;
   }

   public Map<String, Integer> getFaceRectangle() {
      return faceRectangle;
   }

   public void setFaceRectangle(Map<String, Integer> faceRectangle) {
      this.faceRectangle = faceRectangle;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

}
