package me.ltxom.facerec.util;

import me.ltxom.facerec.service.SourceImageService;
import org.json.JSONArray;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class ImgUtil {
   public static void generateRenderedImage(Map<String, String> sourceMap, JSONArray attendanceArray,
                                            JSONArray jsonArray) {


      pressText(sourceMap, attendanceArray, jsonArray, "img/checkData.jpg", "img" +
                      "/test.jpg",
              "宋体",
              Font.BOLD, 12, Color.RED, -1, -1, 1.0f, null, "jpg");

   }

   /**
    * 添加文字水印
    *
    * @param targetImg 目标图片路径，如：C://myPictrue//1.jpg
    * @param pressText 水印文字， 如：云账房
    * @param fontName  字体名称，    如：宋体
    * @param fontStyle 字体样式，如：粗体和斜体(Font.BOLD|Font.ITALIC)
    * @param fontSize  字体大小，单位为像素
    * @param color     字体颜色
    * @param x         水印文字距离目标图片左侧的偏移量，如果x<0, 则在正中间
    * @param y         水印文字距离目标图片上侧的偏移量，如果y<0, 则在正中间
    * @param alpha     透明度(0.0 -- 1.0, 0.0为完全透明，1.0为完全不透明)
    */
   public static void pressText(Map<String, String> sourceMap,
                                JSONArray attendanceArray, JSONArray jsonArray,
                                String targetImg, String outImg,
                                String fontName, int fontStyle, int fontSize, Color color,
                                int positionX, int positionY, float alpha, Integer degree,
                                String suffix) {
      try {
         File file = new File(targetImg);
         // 如果没有指定文件存放地址，则默认替换掉原图片
         File outFile;
         if (StringUtils.isEmpty(outImg)) {
            outFile = file;
         } else {
            outFile = new File(outImg);
         }
         for (int i = 0; i < jsonArray.length(); i++) {
            if (i > 0)
               file = outFile;
            Image image = ImageIO.read(file);
            int width = image.getWidth(null);
            int height = image.getHeight(null);
            BufferedImage bufferedImage = new BufferedImage(width, height,
                    BufferedImage.TYPE_INT_RGB);
            Font font = new Font(fontName, fontStyle, fontSize);


            String faceId = jsonArray.getJSONArray(i).length() != 0 ?
                    String.valueOf(jsonArray.getJSONArray(i).getJSONObject(0).get("faceId")) : "null";
            String name = "No Record";
            if (sourceMap.containsKey(faceId))
               name = sourceMap.get(faceId).split("\\.")[0];



            System.out.println(name);
            Graphics2D g = bufferedImage.createGraphics();
            Graphics g2 = image.getGraphics();
            g2.setColor(Color.RED);
            g2.drawRect(100, 100, 100, 100);//矩形框(原点x坐标，原点y坐标，矩形的长，矩形的宽)

            g.drawImage(image, 0, 0, width, height, null);
            g.setColor(color);

            //获取文字所占的像素
            FontRenderContext context = g.getFontRenderContext();
            Rectangle2D stringBounds = font.getStringBounds(name, context);

            int textWidth = (int) stringBounds.getWidth();
            int textHeight = (int) stringBounds.getHeight();

            int widthDiff = width - textWidth;
            int heightDiff = height - textHeight;
            if (positionX < 0) {
               positionX = widthDiff / 2;
            } else if (positionX > widthDiff) {
               positionX = widthDiff;
            }
            if (positionY < 0) {
               positionY = heightDiff / 2;
            } else if (positionY > heightDiff) {
               positionY = heightDiff;
            }

            g.drawString(name, positionX, positionY + textHeight);
            g.dispose();
            ImageIO.write(bufferedImage, suffix, outFile);
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public static void main(String[] args) {
      SourceImageService sourceImageService = new SourceImageService();
//      Map<String, String> sourceMap = sourceImageService.getFaceIdToNameMap(true);
//
//      generateRenderedImage(sourceMap, sourceImageService.getSourceInfoByName("data/checkData.jpg.json") ,
//              sourceImageService.getSourceInfoByName("data/result.json"));
      JSONArray jsonArray = sourceImageService.getSourceImageInfo("https://facerec" +
              ".oss-us-west-1.aliyuncs.com/IMG_6700.jpg");
   }
}
