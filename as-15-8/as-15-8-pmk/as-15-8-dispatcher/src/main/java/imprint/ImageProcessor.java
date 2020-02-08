package imprint;

import java.io.*;
import javax.imageio.ImageIO;
import java.awt.Font;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Class for image post processing
 *
 * @author	freelance.meridbt@yandex.ru
 * @version	0.1
 */
public class ImageProcessor {
	
	
	private enum TextAlign{
		LEFT,
		RIGHT
	}
	
	
	private final int MARGIN = 1; // header border ractangle margin, px
	private final int LINE_WIDTH = 3; // header border rectangle line width, px
	private final int PADDING = 3; // header border rectangle padding, px
	private final int FONT_SIZE = 18; // font kegel, px
	private final int LINE_GAP = 3; // gap between text rows, px
	private final int LABEL_LINE_COUNT = 2; // number of lines that can be filled by multiline label (Label 7)
	private final int UNDERSCORE_HEIGHT = 4; /* number of pixels laying under the
											  * bottom line border (configure it 
											  * according to chosen font and kegel)
											  */
	
	
	private Font TEXT_FONT;
	private int REAL_FONT_HEIGHT;
	
	
	/*============================================================================*/
	/*+++++++++++++++++++++++++++++++ PUBLIC REGION ++++++++++++++++++++++++++++++*/
	/*============================================================================*/
	
	
	/* LOAD FONT FROM FILE */ 
	/**
	 * Loads custom font from *.ttf file. Available for TrueType fonts only
	 *
	 * @param	filename name of the font file with path
	 */
	public void loadFontFromFile(String filename) throws Exception {
		InputStream is = new BufferedInputStream(new FileInputStream(new File(filename)));
		TEXT_FONT = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(Font.PLAIN, FONT_SIZE);
	}
	
	/* GET IMAGE SIZE */
	/** 
	 * Returns text information about size of the image
	 *
	 * @param	image target BufferedImage
	 * @return	text information with width and height 
	 */
	public String getImageSize(BufferedImage image) throws Exception { 
		return "Image size: " + image.getWidth() + " * " + image.getHeight(); 
	}
	
	
	/* PROCESS IMAGE */
	/** 
	 * Post processing of immage, adding rectangle and 7 text labels to original image 
	 *
	 * @param	bytes byte array of original image
	 * @param	header Header object containing labels
	 * @return	processed image as byte array
	 */
	public byte[] processImage(byte[] bytes, HeaderObject header) throws Exception {
		BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(bytes));
		String[] lines = header.getAllLabels();
		REAL_FONT_HEIGHT = 0;
		for(String line : lines) {
			REAL_FONT_HEIGHT = measureTextHeight(originalImage, line) > REAL_FONT_HEIGHT ? 
				measureTextHeight(originalImage, line) : REAL_FONT_HEIGHT;
		}		
		BufferedImage headerImage = getHeaderImage(originalImage.getWidth(), header, true);
		Graphics2D g = headerImage.createGraphics();
		addHeaderBorder(g, headerImage.getWidth(), headerImage.getHeight(), Color.BLACK);
		int lineCount = 0;
		if (header.getLabel1() != null) {
			if (header.getLabel2() != null) addHeaderLabel(
												g, 
												headerImage.getWidth(), 
												Color.BLACK, 
												++lineCount, 
												TextAlign.LEFT, 
												trimLongText(
													g, 
													headerImage.getWidth() - measureTextWidth(g, header.getLabel2()), 
													header.getLabel1(),
													true
												)
											);
			else addHeaderLabel(
					g, 
					headerImage.getWidth(), 
					Color.BLACK, 
					++lineCount, 
					TextAlign.LEFT, 
					trimLongText(
						g, 
						headerImage.getWidth(), 
						header.getLabel1(),
						true
					)
				);			
		}
		if (header.getLabel2() != null) addHeaderLabel(
											g, 
											headerImage.getWidth(), 
											Color.BLACK, 
											lineCount, 
											TextAlign.RIGHT, 
											trimLongText(
												g, 
												headerImage.getWidth(), 
												header.getLabel2(),
												true
											)
										);
										
		if (header.getLabel3() != null) addHeaderLabel(
											g, 
											headerImage.getWidth(), 
											Color.BLACK, 
											++lineCount, 
											TextAlign.LEFT, 
											trimLongText(
												g, 
												headerImage.getWidth(), 
												header.getLabel3(),
												true
											)
										);
										
		if (header.getLabel4() != null) {
			if (header.getLabel5() != null) addHeaderLabel(
												g, 
												headerImage.getWidth(), 
												Color.BLACK, 
												++lineCount, 
												TextAlign.LEFT, 
												trimLongText(
													g, 
													headerImage.getWidth() - measureTextWidth(g, header.getLabel5()), 
													header.getLabel4(),
													true
												)
											);
			else addHeaderLabel(
					g, 
					headerImage.getWidth(), 
					Color.BLUE, 
					++lineCount, 
					TextAlign.LEFT, 
					trimLongText(
						g, 
						headerImage.getWidth(), 
						header.getLabel4(),
						true
					)
				);
		}
		if (header.getLabel5() != null) addHeaderLabel(
											g, 
											headerImage.getWidth(), 
											Color.BLUE, 
											lineCount, 
											TextAlign.RIGHT, 
											trimLongText(
												g, 
												headerImage.getWidth(), 
												header.getLabel5(),
												true
											)
										);
										
		if (header.getLabel6() != null) addHeaderLabel(
											g, 
											headerImage.getWidth(), 
											Color.BLACK, 
											++lineCount, 
											TextAlign.LEFT, 
											trimLongText(
												g, 
												headerImage.getWidth(), 
												header.getLabel6(),
												true
											)
										);
										
		if (header.getLabel7() != null) {
			for (String line : composeMultilineLabel(g, headerImage.getWidth(), header.getLabel7(), LABEL_LINE_COUNT)) {
				addHeaderLabel(
					g, 
					headerImage.getWidth(), 
					Color.BLUE, 
					++lineCount, 
					TextAlign.LEFT, 
					line
				);
			}
		}
		
		g.dispose();
		BufferedImage processedImage = getGluedCopy(originalImage, headerImage, true);				
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(processedImage, "png", baos);
		return baos.toByteArray();
	}
	
	
	/*=============================================================================*/
	/*+++++++++++++++++++++++++++++++ PRIVATE REGION ++++++++++++++++++++++++++++++*/
	/*=============================================================================*/
	
	
	/* MEASURE TEXT WIDTH */
	/** 
	 * Measures text width in pixels usnig selected font
	 *
	 * @param	g Graphics2D object
	 * @param	text text string to be measured
	 * @return	text width in pixels
	 */
	private int measureTextWidth(Graphics2D g, String text) {
		g.setFont(TEXT_FONT);
		FontRenderContext frc = g.getFontRenderContext();
		Rectangle2D bounds = g.getFont().getStringBounds(text, frc);
		return (int)bounds.getWidth();
	}
	
	
	/* MEASURE TEXT HEIGHT */
	/** 
	 * Measures text height in pixels using selected font
	 *
	 * @param	image target BufferedImage
	 * @param	text text string to be measured
	 * @return	text height in pixels
	 */
	private int measureTextHeight(BufferedImage image, String text) {
		Graphics2D g = image.createGraphics();
		g.setFont(TEXT_FONT);
		FontRenderContext frc = g.getFontRenderContext();
		Rectangle2D bounds = g.getFont().getStringBounds(text, frc);
		g.dispose();
		return (int)bounds.getHeight();
	}
	
	
	/* TRIM LONG TEXT */
	/**
	 * Compares estimated width of the given text with a certain width value 
	 * and shorten text in case of too long length with "..." at the end
	 *
	 * @param	g Graphics2D object
	 * @param	width given width to compare with
	 * @param	text given text to be checked
	 * @param	addDots indicates if method should add "..." at the end of shorten line
	 * @return 	text, that matches given width
	 */
	private String trimLongText(Graphics2D g, int width, String text, boolean addDots){
		boolean beingShorten = false;
		while (measureTextWidth(g, text) > width - 2 * PADDING - 2 * LINE_WIDTH - 2 * MARGIN) {
			beingShorten = true;
			text = text.substring(0, text.length() - 2);
		}
		return beingShorten && addDots ? text.substring(0, text.length() - 2) + "..." : text;
	}
	
	
	/* COMPOSE MULTILINE LABEL */
	/**
	 * Composes a label, consinsting of two or more lines
	 *
	 * @param	g Graphics2D object
	 * @param	width header width in pixels
	 * @param	text label text to be composed in several lines
	 * @param	lineCount max number of lines
	 * @return	String array of label lines
	 */
	 private String[] composeMultilineLabel(Graphics2D g, int width, String text, int lineCount) {
		ArrayList<String> labels = new ArrayList<String>();		
		String line = trimLongText(g, width, text, false);
		while (text.length() > line.length() && labels.size() < lineCount - 1) {
			labels.add(line);
			text = text.substring(line.length());
			line = trimLongText(g, width, text, false);
		}
		labels.add(trimLongText(g, width, text, true));
		return labels.toArray(new String[labels.size()]);
	 }
	
	/* GET Y POS */
	/** 
	 * Calculates Y position of label
	 *
	 * @param	lineNumber order of current text line starting from up to down
	 * @return	Y position in pixels
	 */
	private int getYPos(int lineNumber) {
		return MARGIN + LINE_WIDTH + PADDING - UNDERSCORE_HEIGHT + REAL_FONT_HEIGHT * lineNumber + LINE_GAP * (lineNumber - 1);
	}
	
	
	/* GET X POS */
	/** 
	 * Calculates X position of label
	 *
	 * @param	g Graphics2D object
	 * @param	text given text
	 * @param	textAlign align of the text in label
	 * @param	width width of the header in pixels
	 * @return 	X position in pixels
	 */
	private int getXPos(Graphics2D g, String text, TextAlign textAlign, int width) {
		switch (textAlign) {
			case RIGHT:
				return width - measureTextWidth(g, text) - MARGIN - LINE_WIDTH - PADDING;
			default: //LEFT
				return MARGIN + LINE_WIDTH + PADDING;
		}
		
	}

	
	/* GET GLUED IMAGE */
	/** 
	 * Creates glued copy of original image with header
	 *
	 * @param	originalImage source BufferedImage
	 * @param	headerImage header BufferedImage
	 * @param	preserveAlpha indicates Alpha channel state 
	 * @return	processed image
	 */
	private BufferedImage getGluedCopy(BufferedImage originalImage, BufferedImage headerImage, boolean preserveAlpha) {
        int imageType = preserveAlpha ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage gluedImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight() + headerImage.getHeight(), imageType);
        Graphics2D g = gluedImage.createGraphics();
        if (preserveAlpha) {
            g.setComposite(AlphaComposite.Src);
        }
		g.setColor(Color.WHITE);
		g.fillRect(0,0, gluedImage.getWidth(), gluedImage.getHeight());
        g.drawImage(originalImage, 0, headerImage.getHeight(), gluedImage.getWidth(), gluedImage.getHeight(), null);		
        g.drawImage(headerImage, 0, 0, gluedImage.getWidth(), headerImage.getHeight(), null);		
        g.dispose();
        return gluedImage;
    }
	
	
	/* GET HEADER IMAGE */
	/** 
	 * Creates image with given width and height, calculated on number of labels and header settings
	 * 
	 * @param	width given width of header
	 * @param	header Header object
	 * @param	preserveAlpha indicates Alpha channel state 
	 * @return	header image
	 */
	private BufferedImage getHeaderImage(int width, HeaderObject header, boolean preserveAlpha) {
		int imageType = preserveAlpha ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
		int lineCount = 0;
		if (header.getLabel1() != null || header.getLabel2() != null) { lineCount++; }
		if (header.getLabel3() != null) { lineCount++; }
		if (header.getLabel4() != null || header.getLabel5() != null) { lineCount++; }
		if (header.getLabel6() != null) { lineCount++; }
		if (header.getLabel7() != null) { 
			BufferedImage temp = new BufferedImage(100, 100, imageType);
			Graphics2D g = temp.createGraphics();			
			lineCount += composeMultilineLabel(g, width, header.getLabel7(), LABEL_LINE_COUNT).length;		
			g.dispose();
		}
		int height = 2 * MARGIN + 2 * LINE_WIDTH + 2 * PADDING + REAL_FONT_HEIGHT * lineCount + LINE_GAP * (lineCount - 1);
		return new BufferedImage(width, height, imageType);		
	}
	
	
	/* ADD HEADER BORDER */
	/** 
	 * Creates border in header image
	 *
	 * @param	g Graphics2D object
	 * @param	headerWidth header width in pixels
	 * @param	headerHeight header height in pixels
	 * @param	color border color
	 */
	private void addHeaderBorder(Graphics2D g, int headerWidth, int headerHeight, Color color) {
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, headerWidth, headerHeight);
		g.setColor(color);		
		g.setStroke(new BasicStroke(LINE_WIDTH, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
		g.draw(new Rectangle2D.Double(
			LINE_WIDTH / 2 + MARGIN, 
			LINE_WIDTH / 2 + MARGIN, 
			headerWidth - (LINE_WIDTH + 2 * MARGIN), 
			headerHeight - (LINE_WIDTH + 2 * MARGIN)
		));
	}
	
	
	/* ADD HEADER LABEL */
	/** 
	 * Creates text label in image header 
	 *
	 * @param	g Graphics2D object
	 * @param	headerWidth header width
	 * @param	color label text color
	 * @param	lineNumber order of the line
	 * @param	textAlign alignment of the text in line
	 * @param	text text to be added
	 */
	private void addHeaderLabel(Graphics2D g, int headerWidth, Color color, int lineNumber, TextAlign textAlign, String text) {
		g.setFont(TEXT_FONT);
		g.setColor(color);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.drawString(text, getXPos(g, text, textAlign, headerWidth), getYPos(lineNumber));
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	}	
	
	
}