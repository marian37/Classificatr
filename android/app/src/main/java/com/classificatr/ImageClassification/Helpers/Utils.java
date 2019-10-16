package com.classificatr.ImageClassification.Helpers;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class Utils {

  private static final int PIXEL_SIZE = 3;

  // https://www.tensorflow.org/lite/models/image_classification/android#load_model_and_create_interpreter
  public static MappedByteBuffer loadModel(AssetManager assets, String modelPath)
      throws IOException {
    AssetFileDescriptor fileDescriptor = assets.openFd(modelPath);
    FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
    FileChannel fileChannel = inputStream.getChannel();
    long startOffset = fileDescriptor.getStartOffset();
    long declaredLength = fileDescriptor.getDeclaredLength();
    return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
  }

  public static List<String> loadLabels(AssetManager assets, String labelsPath)
      throws IOException {
    InputStream inputStream = assets.open(labelsPath);
    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
    String line;
    List<String> labels = new ArrayList<>();
    while ((line = reader.readLine()) != null) {
      labels.add(line);
    }
    reader.close();
    return labels;
  }

  // https://www.tensorflow.org/lite/models/image_classification/android#pre-process_bitmap_image
  public static ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap, int modelSize) {
    int[] intValues = new int[bitmap.getWidth() * bitmap.getHeight()];
    ByteBuffer imageBuffer = ByteBuffer.allocateDirect(PIXEL_SIZE * modelSize * modelSize);
    imageBuffer.order(ByteOrder.nativeOrder());
    imageBuffer.rewind();

    bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
    // Convert the image to floating point.
    int pixel = 0;
    for (int i = 0; i < modelSize; ++i) {
      for (int j = 0; j < modelSize; ++j) {
        final int val = intValues[pixel++];
        imageBuffer.put((byte) ((val >> 16) & 0xFF));
        imageBuffer.put((byte) ((val >> 8) & 0xFF));
        imageBuffer.put((byte) (val & 0xFF));
      }
    }
    return imageBuffer;
  }
}
