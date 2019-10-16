package com.classificatr.ImageClassification.Helpers;

import android.graphics.Bitmap;
import android.os.Bundle;

import org.tensorflow.lite.Interpreter;

import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class Classifier {

  private Interpreter tfLiteInterpreter;
  private int modelSize;
  private List<String> labels;

  public Classifier(MappedByteBuffer model, int modelSize, List<String> labels) {
    this.tfLiteInterpreter = new Interpreter(model, new Interpreter.Options());
    this.modelSize = modelSize;
    this.labels = labels;
  }

  public List<Bundle> recognizeImage(Bitmap image, int maxResults) {
    // allocate buffers
    byte[][] labelProb = new byte[1][labels.size()];
    ByteBuffer imageBuffer = Utils.convertBitmapToByteBuffer(image, modelSize);

    // run inference
    tfLiteInterpreter.run(imageBuffer, labelProb);

    // post-process values
    PriorityQueue<Recognition> queue = new PriorityQueue<Recognition>();
    for (int i = 0; i < labels.size(); i++) {
      queue.add(new Recognition(Integer.toString(i), labels.get(i), (float) labelProb[0][i]));
    }
    List<Bundle> results = new ArrayList<>(maxResults);
    for (int i = 0; i < maxResults; i++) {
      Recognition recognition = queue.poll();
      Bundle bundle = new Bundle();
      bundle.putString("label", recognition.getLabel());
      bundle.putFloat("confidence", recognition.getConfidence());
      results.add(bundle);
    }

    return results;
  }

  public void close() {
    tfLiteInterpreter.close();
  }
}