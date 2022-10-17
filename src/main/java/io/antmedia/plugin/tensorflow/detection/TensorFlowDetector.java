package io.antmedia.plugin.tensorflow.detection;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.antmedia.plugin.BlurFactor;
import io.antmedia.plugin.BlurTechnique;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.antmedia.plugin.IDeepLearningProcessor;
import io.antmedia.plugin.tensorflow.detection.Classifier.Recognition;
import io.vertx.core.Vertx;

public class TensorFlowDetector implements IDeepLearningProcessor {

	private Classifier classifier;
	private String streamId;
	private long captureCount = 0;
	private List<Recognition> recognitionList = new ArrayList<Classifier.Recognition>();
	private Vertx vertx;
	private long lastUpdate;
	private boolean tensorflowRunning;

	private BlurTechnique blurTechnique;

	private BlurFactor blurFactor;

	private static Logger logger = LoggerFactory.getLogger(TensorFlowDetector.class);

	public TensorFlowDetector(String modelDir, Vertx vertx, BlurTechnique blurTechnique, BlurFactor blurFactor) throws IOException {
		this.classifier = TFObjectDetector.create(modelDir);
		this.vertx = vertx;
		this.blurTechnique = blurTechnique;
		this.blurFactor = blurFactor;
	}


	@Override
	public BufferedImage process(int width, int height, byte[] data, boolean immediately) throws IOException {
		long startTime = System.currentTimeMillis();

		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		int k = 0;
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				int r = (int)(data[k++]& 0xFF);
				int g = (int)(data[k++]& 0xFF);
				int b = (int)(data[k++]& 0xFF);
				int a = (int)(data[k++]& 0xFF);

				Color c = new Color(r, g, b);
				image.setRGB(x, y, c.getRGB());
			}
		}
		
		if(immediately) {
			recognitionList = classifier.recognizeImage(image);
		}
		else {
			if(!tensorflowRunning) {
				tensorflowRunning = true;
				vertx.executeBlocking(a->{
					long t0 = System.currentTimeMillis();
					recognitionList = classifier.recognizeImage(image);
					logger.info("Processing time: {} ms. Number of found objects {} @{}", (System.currentTimeMillis() - t0), recognitionList.size(), System.currentTimeMillis());
					tensorflowRunning = false;
					a.complete();
				}, null);
				lastUpdate = startTime;
			}
		}

		ArrayList<Rectangle> rectList = new ArrayList<>();

		if (recognitionList.size() > 0) {

			for (Classifier.Recognition recognition : recognitionList) {
				double rectangleX = recognition.getLocation().getMinX()-10;
				double rectangleY = recognition.getLocation().getMinY()-10;
				double rectangleWidth = recognition.getLocation().getWidth()+20;
				double rectangleHeight = recognition.getLocation().getHeight()+20;
				if(rectangleX < 0){
					rectangleX = 0;
				}

				if(rectangleY < 0){
					rectangleY = 0;
				}
				if(rectangleX + rectangleWidth > image.getWidth()){
					rectangleX = image.getWidth() - rectangleWidth;
				}
				if(rectangleY + rectangleHeight > image.getHeight()){
					rectangleY = image.getHeight() - rectangleHeight;
				}

				Rectangle rectangle = new Rectangle((int) rectangleX,
						(int) rectangleY,
						(int) rectangleWidth,
						(int) rectangleHeight);
				rectList.add(rectangle);
			}

			captureCount++;
		}
		return Utils.blurRectangles(image, rectList, blurTechnique, blurFactor);
	}
}
