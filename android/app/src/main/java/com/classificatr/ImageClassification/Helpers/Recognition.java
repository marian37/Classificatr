package com.classificatr.ImageClassification.Helpers;

import androidx.annotation.NonNull;

public class Recognition implements Comparable<Recognition> {

  private final String id;

  private final String label;

  private final Float confidence;

  public Recognition(String id, String label, Float confidence) {
    this.id = id;
    this.label = label;
    this.confidence = confidence;
  }

  public String getLabel() {
    return label;
  }

  public Float getConfidence() {
    return confidence;
  }

  @Override
  public int compareTo(@NonNull Recognition r) {
    // Intentionally reversed to put high confidence at the head of the queue.
    return r.getConfidence().compareTo(this.confidence);
  }
}