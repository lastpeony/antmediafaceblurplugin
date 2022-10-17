package io.antmedia.plugin.tensorflow.detection;

import io.antmedia.plugin.BlurFactor;
import io.antmedia.plugin.BlurTechnique;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

    public static Mat BufferedImage2Mat(BufferedImage image) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "bmp", byteArrayOutputStream);
        byteArrayOutputStream.flush();
        return Imgcodecs.imdecode(new MatOfByte(byteArrayOutputStream.toByteArray()), Imgcodecs.IMREAD_UNCHANGED);
    }
    public static BufferedImage Mat2BufferedImage(Mat matrix)throws IOException {
        MatOfByte mob=new MatOfByte();
        Imgcodecs.imencode(".bmp", matrix, mob);
        return ImageIO.read(new ByteArrayInputStream(mob.toArray()));
    }

    static BufferedImage blurRectangles(BufferedImage img, ArrayList<Rectangle> rectList, BlurTechnique blurTechnique, BlurFactor blurFactor) throws IOException {
        if(blurTechnique == BlurTechnique.CONVOLUTION_BLUR){
            float weight = 1.0f / (blurFactor.value * blurFactor.value);
            float[] data = new float[blurFactor.value * blurFactor.value];
            Arrays.fill(data, weight);
            for(Rectangle rectangle:rectList){
                BufferedImage dest = img.getSubimage(rectangle.x, rectangle.y, rectangle.width-2, rectangle.height-2); // x, y, width, height
                ColorModel cm = img.getColorModel();
                BufferedImage src = new BufferedImage(cm, dest.copyData(dest.getRaster().createCompatibleWritableRaster()), cm.isAlphaPremultiplied(), null).getSubimage(0, 0, dest.getWidth(), dest.getHeight());
                Kernel kernel = new Kernel(blurFactor.value, blurFactor.value, data);
                new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null).filter(src, dest);
            }
        }else{
            for(Rectangle rectangle:rectList){

                Rect rect = new Rect(rectangle.x,rectangle.y,rectangle.width,rectangle.height);
                Mat srcMat = Utils.BufferedImage2Mat(img);

                Imgproc.GaussianBlur(new Mat(srcMat,rect),new Mat(srcMat,rect), new Size(15,15),0);
                img = Utils.Mat2BufferedImage(srcMat);

            }
        }

        return img;
    }
}
