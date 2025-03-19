package io.kadai.spi.task.internal;

import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.internal.util.LogSanitizer;
import io.kadai.common.internal.util.SpiLoader;
import io.kadai.spi.task.api.TaskDistributionProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TaskDistributionManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(TaskDistributionManager.class);
  private final List<TaskDistributionProvider> taskDistributionProviderList;
  private final TaskDistributionProvider defaultProvider = new DefaultTaskDistributionProvider();

  public TaskDistributionManager(KadaiEngine kadaiEngine) {
    List<TaskDistributionProvider> loadedProviders = SpiLoader.load(TaskDistributionProvider.class);

    this.taskDistributionProviderList = new ArrayList<>(loadedProviders);
    this.taskDistributionProviderList.add(defaultProvider);

    for (TaskDistributionProvider provider : taskDistributionProviderList) {
      provider.initialize(kadaiEngine);
      LOGGER.info("Registered TaskDistribution provider: {}", provider.getClass().getName());
    }

    if (loadedProviders.isEmpty()) {
      LOGGER.info(
          "No Custom TaskDistribution Provider found. Using only DefaultTaskDistributionProvider.");
    }
  }

  public TaskDistributionProvider getProviderByName(String name) {
    if (name == null) {
      return defaultProvider;
    }

    return taskDistributionProviderList.stream()
        .filter(provider -> provider.getClass().getSimpleName().equals(name))
        .findFirst()
        .orElseThrow(
            () ->
                new InvalidArgumentException(
                    String.format("The distribution strategy '%s' does not exist.", name)));
  }

  public Map<String, List<String>> distributeTasks(
      List<String> taskIds,
      List<String> destinationWorkbasketIds,
      Map<String, Object> additionalInformation,
      String distributionStrategyName) {

    TaskDistributionProvider provider = getProviderByName(distributionStrategyName);

    String sanitizedDistributionStrategyName =
        LogSanitizer.stripLineBreakingChars(
            distributionStrategyName != null
                ? distributionStrategyName
                : "DefaultTaskDistributionProvider");
    LOGGER.info("Using TaskDistributionProvider: {}", sanitizedDistributionStrategyName);

    Map<String, List<String>> newTaskDistribution =
        provider.distributeTasks(taskIds, destinationWorkbasketIds, additionalInformation);

    if (newTaskDistribution == null || newTaskDistribution.isEmpty()) {
      throw new InvalidArgumentException(
          "The distribution strategy resulted in no task assignments. Please verify the input.");
    }

    return newTaskDistribution;
  }
}
