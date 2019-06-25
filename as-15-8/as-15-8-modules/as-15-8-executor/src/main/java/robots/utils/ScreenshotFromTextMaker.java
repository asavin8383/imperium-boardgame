package robots.utils;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.springframework.util.StringUtils;

public class ScreenshotFromTextMaker {
	
	public static byte[] makeScreenshot(String content) {
		BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        
        Font font = new Font("Arial", Font.PLAIN, 12); 
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics(font);
        int height = fm.getHeight() * StringUtils.countOccurrencesOf(content, "\n");
        g2d.dispose();

        img = new BufferedImage(1920, height, BufferedImage.TYPE_INT_ARGB);
        g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2d.setFont(font);
        fm = g2d.getFontMetrics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, img.getWidth(), img.getHeight());
        g2d.setColor(Color.BLACK);
        String[] lines = content.split("\n");
        int y = 0;
        for(String line : lines)
        	g2d.drawString(line, 0, y += fm.getHeight());
        g2d.dispose();
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(img, "png", outputStream);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return outputStream.toByteArray();
	}
	
	public static void main(String[] args) throws IOException {
		byte[] imgBytes = makeScreenshot("hello\r\nworld");
		ByteArrayInputStream bis = new ByteArrayInputStream(imgBytes);
		BufferedImage bImage2 = ImageIO.read(bis);
		ImageIO.write(bImage2, "png", new File("D:\\output.png") );
	}
}
