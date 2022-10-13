package io.antmedia.plugin.tensorflow.detection;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utils {

    public static byte[] readAllBytesOrExit(Path path) {
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            System.err.println("Failed to read [" + path + "]: " + e.getMessage());
            System.exit(1);
        }
        return null;
    }
    
    public static List<String> readAllLinesOrExit(Path path) {
		try {
			return Files.readAllLines(path, Charset.forName("UTF-8"));
		} catch (IOException e) {
			System.err.println("Failed to read [" + path + "]: " + e.getMessage());
			System.exit(0);
		}
		return null;
	}
    public static BufferedImage blurRectangles(BufferedImage img, ArrayList<Rectangle> rectList) {
        float weight = 1.0f / (20 * 20);
        float[] data = new float[20 * 20];
        Arrays.fill(data, weight);

        for(Rectangle rectangle:rectList){
            BufferedImage dest = img.getSubimage(rectangle.x, rectangle.y, rectangle.width, rectangle.height); // x, y, width, height
            ColorModel cm = img.getColorModel();
            BufferedImage src = new BufferedImage(cm, dest.copyData(dest.getRaster().createCompatibleWritableRaster()), cm.isAlphaPremultiplied(), null).getSubimage(0, 0, dest.getWidth(), dest.getHeight());
            Kernel kernel = new Kernel(20, 20, data);
            new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null).filter(src, dest);
        }
        return img;
    }
}
