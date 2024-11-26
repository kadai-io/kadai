package io.kadai.monitor.api.reports.item;

/**
 * The DetailedPriorityQueryItem extends the {@linkplain MonitorQueryItem}. The additional
 * classificationKey is used for the detailed classification report.
 */
public class DetailedPriorityQueryItem extends PriorityQueryItem {

  private String classificationKey;

  public String getClassificationKey() {
    return classificationKey;
  }

  public void setClassificationKey(String classificationKey) {
    this.classificationKey = classificationKey;
  }
}
