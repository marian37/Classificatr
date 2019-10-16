package com.classificatr.ImageClassification;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import com.classificatr.ImageClassification.Helpers.Classifier;
import com.classificatr.ImageClassification.Helpers.Utils;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.MappedByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageClassificationModule extends ReactContextBaseJavaModule {

  private static final int MAX_RESULTS = 3;

  private static final int MODEL_SIZE = 224;

  public static final String MODEL_PATH = "tensorflow/mobilenet_v1_1.0_224_quant.tflite";

  public static final String LABELS_PATH = "tensorflow/labels_mobilenet_quant_v1_224.txt";

  private static ReactApplicationContext context;

  public ImageClassificationModule(ReactApplicationContext reactContext) {
    super(reactContext);
    context = reactContext;
  }

  @Override
  public String getName() {
    return "ImageClassification";
  }

  @Override
  public Map<String, Object> getConstants() {
    final Map<String, Object> constants = new HashMap<>();
    constants.put("MAX_RESULTS", MAX_RESULTS);
    constants.put("MODEL_SIZE", MODEL_SIZE);
    constants.put("MODEL_PATH", MODEL_PATH);
    constants.put("LABELS_PATH", LABELS_PATH);
    return constants;
  }

  @ReactMethod
  public void process(String imagePath, Promise promise) {
    try {
      Uri imageUri = Uri.parse(imagePath);

      // pre-process image - crop to proper size (MODEL_SIZE x MODEL_SIZE)
      Bitmap imageBitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(imageUri));
      Matrix rotationMatrix = new Matrix();
      rotationMatrix.postRotate(90);
      Bitmap rotatedBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, imageBitmap.getWidth(), imageBitmap.getHeight(), rotationMatrix, false);
      Bitmap croppedBitmap = Bitmap.createScaledBitmap(rotatedBitmap, MODEL_SIZE, MODEL_SIZE, false);

      // save image to gallery (debugging)
      FileOutputStream out = new FileOutputStream(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "image.jpg"));
      croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
      out.flush();
      out.close();

      // load model and labels
      AssetManager assetManager = context.getAssets();
      MappedByteBuffer model = Utils.loadModel(assetManager, MODEL_PATH);
      List<String> labels = Utils.loadLabels(assetManager, LABELS_PATH);

      // run classification
      Classifier tfLiteClassifier = new Classifier(model, MODEL_SIZE, labels);
      List<Bundle> tags = tfLiteClassifier.recognizeImage(croppedBitmap, MAX_RESULTS);
      tfLiteClassifier.close();

      WritableArray tagsArray = Arguments.makeNativeArray(tags);

      promise.resolve(tagsArray);
    } catch (Exception e) {
      promise.reject(e);
    }

    promise.resolve(imagePath);
  }
}
