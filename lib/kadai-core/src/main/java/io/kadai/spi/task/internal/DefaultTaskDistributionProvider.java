package io.kadai.spi.task.internal;

import io.kadai.common.api.KadaiEngine;
import io.kadai.spi.task.api.TaskDistributionProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DefaultTaskDistributionProvider implements TaskDistributionProvider {

  @Override
  public void initialize(KadaiEngine kadaiEngine) {
    // NOOP
  }

  @Override
  public Map<String, List<String>> distributeTasks(
      List<String> taskIds, List<String> workbasketIds, Map<String, Object> additionalInformation) {

    if (taskIds == null || taskIds.isEmpty()) {
      throw new IllegalArgumentException("Task Ids list cannot be null or empty.");
    }
    if (workbasketIds == null || workbasketIds.isEmpty()) {
      throw new IllegalArgumentException("Ids of destinationWorkbaskets cannot be null or empty.");
    }

    Map<String, List<String>> distributedTaskIds =
        workbasketIds.stream().collect(Collectors.toMap(id -> id, id -> new ArrayList<>()));

    int workbasketCount = workbasketIds.size();
    IntStream.range(0, taskIds.size())
        .forEach(
            i ->
                distributedTaskIds.get(workbasketIds.get(i % workbasketCount)).add(taskIds.get(i)));

    return distributedTaskIds;
  }
}
