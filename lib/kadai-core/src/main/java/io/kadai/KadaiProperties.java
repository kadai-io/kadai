/*
 * Copyright [2026] [envite consulting GmbH]
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 *
 */

package io.kadai;

import io.kadai.common.api.CustomHoliday;
import io.kadai.common.api.KadaiRole;
import io.kadai.common.api.LocalTimeInterval;
import io.kadai.common.api.exceptions.SystemException;
import io.kadai.common.internal.util.FileLoaderUtil;
import io.kadai.workbasket.api.WorkbasketPermission;
import java.io.IOException;
import java.io.InputStream;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.BindException;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

/** Bindable KADAI configuration properties. */
@ConfigurationProperties(prefix = "kadai")
public class KadaiProperties {

  private Map<String, String> properties = Map.of();
  private List<String> domains = new ArrayList<>();
  private ServiceLevel serviceLevel = new ServiceLevel();
  private Routing routing = new Routing();
  private Map<KadaiRole, Set<String>> roles = new EnumMap<>(KadaiRole.class);
  private Classification classification = new Classification();
  private WorkingTime workingTime = new WorkingTime();
  private History history = new History();
  private Jobs jobs = new Jobs();
  private User user = new User();
  private Feature feature = new Feature();

  public static KadaiProperties load(String propertiesFile) {
    return from(
        loadRawProperties(propertiesFile), String.format("properties file '%s'", propertiesFile));
  }

  public static KadaiProperties from(Map<String, String> rawProperties) {
    return from(rawProperties, "provided properties");
  }

  private static KadaiProperties from(Map<String, String> rawProperties, String sourceDescription) {
    Objects.requireNonNull(rawProperties, "rawProperties must not be null");
    try {
      KadaiProperties properties =
          new Binder(new MapConfigurationPropertySource(rawProperties))
              .bind("kadai", KadaiProperties.class)
              .orElseGet(KadaiProperties::new);
      properties.validate();
      properties.properties = Map.copyOf(rawProperties);
      return properties;
    } catch (BindException e) {
      throw new SystemException(String.format("Could not bind %s", sourceDescription), e);
    }
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  public List<String> getDomains() {
    return domains;
  }

  public void setDomains(List<String> domains) {
    this.domains = domains;
  }

  public ServiceLevel getServiceLevel() {
    return serviceLevel;
  }

  public void setServiceLevel(ServiceLevel serviceLevel) {
    this.serviceLevel = serviceLevel;
  }

  public Routing getRouting() {
    return routing;
  }

  public void setRouting(Routing routing) {
    this.routing = routing;
  }

  public Map<KadaiRole, Set<String>> getRoles() {
    return roles;
  }

  public void setRoles(Map<KadaiRole, Set<String>> roles) {
    this.roles = roles;
  }

  public Classification getClassification() {
    return classification;
  }

  public void setClassification(Classification classification) {
    this.classification = classification;
  }

  public WorkingTime getWorkingTime() {
    return workingTime;
  }

  public void setWorkingTime(WorkingTime workingTime) {
    this.workingTime = workingTime;
  }

  public History getHistory() {
    return history;
  }

  public void setHistory(History history) {
    this.history = history;
  }

  public Jobs getJobs() {
    return jobs;
  }

  public void setJobs(Jobs jobs) {
    this.jobs = jobs;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public Feature getFeature() {
    return feature;
  }

  public void setFeature(Feature feature) {
    this.feature = feature;
  }

  void validate() {
    validateTopLevelProperties();
    validateWorkingTime();
    validateJobs();
  }

  private void validateTopLevelProperties() {
    requireNotNull(properties, "kadai.properties");
    requireNotNull(domains, "kadai.domains");
    requireNotNull(serviceLevel, "kadai.service-level");
    requireNotNull(serviceLevel.getValidation(), "kadai.service-level.validation");
    requireNotNull(routing, "kadai.routing");
    requireNotNull(roles, "kadai.roles");
    requireNotNull(classification, "kadai.classification");
    requireNotNull(classification.getTypes(), "kadai.classification.types");
    requireNotNull(classification.getCategories(), "kadai.classification.categories");
    requireNotNull(workingTime, "kadai.working-time");
    requireNotNull(history, "kadai.history");
    requireNotNull(history.getSimple(), "kadai.history.simple");
    requireNotNull(
        history.getSimple().getDeleteOnTaskDeletion(),
        "kadai.history.simple.delete-on-task-deletion");
    requireNotNull(history.getLogger(), "kadai.history.logger");
    requireNotNull(jobs, "kadai.jobs");
    requireNotNull(user, "kadai.user");
    requireNotNull(
        user.getMinimalPermissionsToAssignDomains(),
        "kadai.user.minimal-permissions-to-assign-domains");
    requireNotNull(feature, "kadai.feature");
  }

  private void validateWorkingTime() {
    requireNotNull(workingTime.getTimezone(), "kadai.working-time.timezone");
    requireNotNull(workingTime.getSchedule(), "kadai.working-time.schedule");
    workingTime
        .getSchedule()
        .forEach(
            (day, intervals) -> {
              requireNotNull(intervals, String.format("kadai.working-time.schedule.%s", day));
              intervals.forEach(
                  interval -> {
                    requireNotNull(
                        interval, String.format("kadai.working-time.schedule.%s[]", day));
                    requireNotNull(
                        interval.getBegin(),
                        String.format("kadai.working-time.schedule.%s[].begin", day));
                    requireNotNull(
                        interval.getEnd(),
                        String.format("kadai.working-time.schedule.%s[].end", day));
                  });
            });
    requireNotNull(workingTime.getHolidays(), "kadai.working-time.holidays");
    requireNotNull(workingTime.getHolidays().getCustom(), "kadai.working-time.holidays.custom");
    workingTime
        .getHolidays()
        .getCustom()
        .forEach(
            customHoliday -> {
              requireNotNull(customHoliday, "kadai.working-time.holidays.custom[]");
              requireBetween(
                  customHoliday.getDay(), 1, 31, "kadai.working-time.holidays.custom[].day");
              requireBetween(
                  customHoliday.getMonth(), 1, 12, "kadai.working-time.holidays.custom[].month");
            });
    requireNotNull(workingTime.getHolidays().getGerman(), "kadai.working-time.holidays.german");
    requireNotNull(
        workingTime.getHolidays().getGerman().getCorpusChristi(),
        "kadai.working-time.holidays.german.corpus-christi");
  }

  private void validateJobs() {
    requireNotNull(jobs.getScheduler(), "kadai.jobs.scheduler");
    requirePositive(jobs.getMaxRetries(), "kadai.jobs.max-retries");
    requirePositive(jobs.getBatchSize(), "kadai.jobs.batch-size");
    requireNotNull(jobs.getFirstRunAt(), "kadai.jobs.first-run-at");
    requireNotNull(jobs.getRunEvery(), "kadai.jobs.run-every");
    requireNotNull(jobs.getLockExpirationPeriod(), "kadai.jobs.lock-expiration-period");

    Scheduler schedulerProperties = jobs.getScheduler();
    requirePositiveOrZero(
        schedulerProperties.getInitialStartDelay(), "kadai.jobs.scheduler.initial-start-delay");
    requirePositive(schedulerProperties.getPeriod(), "kadai.jobs.scheduler.period");
    requireNotNull(
        schedulerProperties.getPeriodTimeUnit(), "kadai.jobs.scheduler.period-time-unit");

    requireNotNull(jobs.getCleanup(), "kadai.jobs.cleanup");
    requireNotNull(jobs.getCleanup().getTask(), "kadai.jobs.cleanup.task");
    CleanupTask cleanupTaskProperties = jobs.getCleanup().getTask();
    requireNotNull(cleanupTaskProperties.getMinimumAge(), "kadai.jobs.cleanup.task.minimum-age");
    requireNotNull(
        cleanupTaskProperties.getLockExpirationPeriod(),
        "kadai.jobs.cleanup.task.lock-expiration-period");

    requireNotNull(jobs.getCleanup().getWorkbasket(), "kadai.jobs.cleanup.workbasket");
    CleanupWorkbasket cleanupWorkbasketProperties = jobs.getCleanup().getWorkbasket();
    requireNotNull(
        cleanupWorkbasketProperties.getLockExpirationPeriod(),
        "kadai.jobs.cleanup.workbasket.lock-expiration-period");

    requireNotNull(jobs.getCleanup().getHistory(), "kadai.jobs.cleanup.history");
    requireNotNull(jobs.getCleanup().getHistory().getSimple(), "kadai.jobs.cleanup.history.simple");
    CleanupHistorySimple cleanupHistorySimpleProperties =
        jobs.getCleanup().getHistory().getSimple();
    requirePositive(
        cleanupHistorySimpleProperties.getBatchSize(),
        "kadai.jobs.cleanup.history.simple.batch-size");
    requireNotNull(
        cleanupHistorySimpleProperties.getMinimumAge(),
        "kadai.jobs.cleanup.history.simple.minimum-age");
    requireNotNull(
        cleanupHistorySimpleProperties.getLockExpirationPeriod(),
        "kadai.jobs.cleanup.history.simple.lock-expiration-period");

    requireNotNull(jobs.getPriority(), "kadai.jobs.priority");
    requireNotNull(jobs.getPriority().getTask(), "kadai.jobs.priority.task");
    PriorityTask priorityTaskProperties = jobs.getPriority().getTask();
    requirePositive(priorityTaskProperties.getBatchSize(), "kadai.jobs.priority.task.batch-size");
    requireNotNull(priorityTaskProperties.getFirstRunAt(), "kadai.jobs.priority.task.first-run-at");
    requireNotNull(priorityTaskProperties.getRunEvery(), "kadai.jobs.priority.task.run-every");
    requireNotNull(
        priorityTaskProperties.getLockExpirationPeriod(),
        "kadai.jobs.priority.task.lock-expiration-period");

    requireNotNull(jobs.getRefresh(), "kadai.jobs.refresh");
    requireNotNull(jobs.getRefresh().getUser(), "kadai.jobs.refresh.user");
    RefreshUser refreshUserProperties = jobs.getRefresh().getUser();
    requireNotNull(refreshUserProperties.getFirstRunAt(), "kadai.jobs.refresh.user.first-run-at");
    requireNotNull(refreshUserProperties.getRunEvery(), "kadai.jobs.refresh.user.run-every");
    requireNotNull(
        refreshUserProperties.getLockExpirationPeriod(),
        "kadai.jobs.refresh.user.lock-expiration-period");
    requireNotNull(jobs.getCustomJobs(), "kadai.jobs.custom-jobs");
  }

  private void requireNotNull(Object value, String propertyName) {
    if (value == null) {
      throw new SystemException(String.format("Property '%s' must not be null", propertyName));
    }
  }

  private void requirePositive(int value, String propertyName) {
    if (value <= 0) {
      throw new SystemException(String.format("Property '%s' must be positive", propertyName));
    }
  }

  private void requirePositive(long value, String propertyName) {
    if (value <= 0) {
      throw new SystemException(String.format("Property '%s' must be positive", propertyName));
    }
  }

  private void requirePositiveOrZero(long value, String propertyName) {
    if (value < 0) {
      throw new SystemException(
          String.format("Property '%s' must be positive or zero", propertyName));
    }
  }

  private void requireBetween(Integer value, int min, int max, String propertyName) {
    requireNotNull(value, propertyName);
    if (value < min || value > max) {
      throw new SystemException(
          String.format("Property '%s' must be between %d and %d", propertyName, min, max));
    }
  }

  private static Map<String, String> loadRawProperties(String propertiesFile) {
    Properties props = new Properties();
    try (InputStream stream =
        FileLoaderUtil.openFileFromClasspathOrSystem(propertiesFile, KadaiConfiguration.class)) {
      props.load(stream);
    } catch (IOException e) {
      throw new SystemException(
          String.format("Could not process properties file '%s'", propertiesFile), e);
    }
    return props.entrySet().stream()
        .collect(
            Collectors.toUnmodifiableMap(
                entry -> entry.getKey().toString(), entry -> entry.getValue().toString()));
  }

  public static class ServiceLevel {
    private Validation validation = new Validation();

    public Validation getValidation() {
      return validation;
    }

    public void setValidation(Validation validation) {
      this.validation = validation;
    }
  }

  public static class Validation {
    private boolean enforce = true;

    public boolean isEnforce() {
      return enforce;
    }

    public void setEnforce(boolean enforce) {
      this.enforce = enforce;
    }
  }

  public static class Routing {
    private boolean includeOwner = false;

    public boolean isIncludeOwner() {
      return includeOwner;
    }

    public void setIncludeOwner(boolean includeOwner) {
      this.includeOwner = includeOwner;
    }
  }

  public static class Classification {
    private List<String> types = new ArrayList<>();
    private Map<String, List<String>> categories = new HashMap<>();

    public List<String> getTypes() {
      return types;
    }

    public void setTypes(List<String> types) {
      this.types = types;
    }

    public Map<String, List<String>> getCategories() {
      return categories;
    }

    public void setCategories(Map<String, List<String>> categories) {
      this.categories = categories;
    }
  }

  public static class WorkingTime {
    private boolean useWorkingTimeCalculation = true;
    private Map<DayOfWeek, Set<TimeInterval>> schedule = initDefaultWorkingTimeSchedule();
    private ZoneId timezone = ZoneId.of("Europe/Berlin");
    private Holidays holidays = new Holidays();

    public boolean isUseWorkingTimeCalculation() {
      return useWorkingTimeCalculation;
    }

    public void setUseWorkingTimeCalculation(boolean useWorkingTimeCalculation) {
      this.useWorkingTimeCalculation = useWorkingTimeCalculation;
    }

    public Map<DayOfWeek, Set<TimeInterval>> getSchedule() {
      return schedule;
    }

    public void setSchedule(Map<DayOfWeek, Set<TimeInterval>> schedule) {
      this.schedule = schedule;
    }

    public Map<DayOfWeek, Set<LocalTimeInterval>> toWorkingTimeSchedule() {
      return schedule.entrySet().stream()
          .collect(
              Collectors.toMap(
                  Map.Entry::getKey,
                  entry ->
                      entry.getValue().stream()
                          .map(TimeInterval::toLocalTimeInterval)
                          .collect(Collectors.toSet()),
                  (oldValue, newValue) -> oldValue,
                  () -> new EnumMap<>(DayOfWeek.class)));
    }

    public ZoneId getTimezone() {
      return timezone;
    }

    public void setTimezone(ZoneId timezone) {
      this.timezone = timezone;
    }

    public Holidays getHolidays() {
      return holidays;
    }

    public void setHolidays(Holidays holidays) {
      this.holidays = holidays;
    }
  }

  public static class TimeInterval {
    private LocalTime begin = LocalTime.MIN;
    private LocalTime end = LocalTime.MAX;

    public TimeInterval() {}

    public TimeInterval(LocalTime begin, LocalTime end) {
      this.begin = begin;
      this.end = end;
    }

    public LocalTime getBegin() {
      return begin;
    }

    public void setBegin(LocalTime begin) {
      this.begin = begin;
    }

    public LocalTime getEnd() {
      return end;
    }

    public void setEnd(LocalTime end) {
      this.end = end;
    }

    public LocalTimeInterval toLocalTimeInterval() {
      return new LocalTimeInterval(begin, end.equals(LocalTime.MIN) ? LocalTime.MAX : end);
    }
  }

  public static class Holidays {
    private Set<CustomHolidayProperties> custom = new HashSet<>();
    private German german = new German();

    public Set<CustomHolidayProperties> getCustom() {
      return custom;
    }

    public void setCustom(Set<CustomHolidayProperties> custom) {
      this.custom = custom;
    }

    public Set<CustomHoliday> toCustomHolidays() {
      return custom.stream()
          .map(CustomHolidayProperties::toCustomHoliday)
          .collect(Collectors.toSet());
    }

    public German getGerman() {
      return german;
    }

    public void setGerman(German german) {
      this.german = german;
    }
  }

  public static class CustomHolidayProperties {
    private Integer day;

    private Integer month;

    public Integer getDay() {
      return day;
    }

    public void setDay(Integer day) {
      this.day = day;
    }

    public Integer getMonth() {
      return month;
    }

    public void setMonth(Integer month) {
      this.month = month;
    }

    public CustomHoliday toCustomHoliday() {
      return CustomHoliday.of(day, month);
    }
  }

  public static class German {
    private boolean enabled = true;
    private CorpusChristi corpusChristi = new CorpusChristi();

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public CorpusChristi getCorpusChristi() {
      return corpusChristi;
    }

    public void setCorpusChristi(CorpusChristi corpusChristi) {
      this.corpusChristi = corpusChristi;
    }
  }

  public static class CorpusChristi {
    private boolean enabled = false;

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }
  }

  public static class History {
    private Simple simple = new Simple();
    private Logger logger = new Logger();

    public Simple getSimple() {
      return simple;
    }

    public void setSimple(Simple simple) {
      this.simple = simple;
    }

    public Logger getLogger() {
      return logger;
    }

    public void setLogger(Logger logger) {
      this.logger = logger;
    }
  }

  public static class Simple {
    private DeleteOnTaskDeletion deleteOnTaskDeletion = new DeleteOnTaskDeletion();

    public DeleteOnTaskDeletion getDeleteOnTaskDeletion() {
      return deleteOnTaskDeletion;
    }

    public void setDeleteOnTaskDeletion(DeleteOnTaskDeletion deleteOnTaskDeletion) {
      this.deleteOnTaskDeletion = deleteOnTaskDeletion;
    }
  }

  public static class DeleteOnTaskDeletion {
    private boolean enabled = false;

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }
  }

  public static class Logger {
    private String name = null;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }

  public static class Jobs {
    private Scheduler scheduler = new Scheduler();
    private int maxRetries = 3;
    private int batchSize = 100;
    private Instant firstRunAt = Instant.parse("2023-01-01T00:00:00Z");
    private Duration runEvery = Duration.ofDays(1);
    private Duration lockExpirationPeriod = Duration.ofMinutes(30);
    private Cleanup cleanup = new Cleanup();
    private Priority priority = new Priority();
    private Refresh refresh = new Refresh();
    private Set<String> customJobs = new HashSet<>();

    public Scheduler getScheduler() {
      return scheduler;
    }

    public void setScheduler(Scheduler scheduler) {
      this.scheduler = scheduler;
    }

    public int getMaxRetries() {
      return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
      this.maxRetries = maxRetries;
    }

    public int getBatchSize() {
      return batchSize;
    }

    public void setBatchSize(int batchSize) {
      this.batchSize = batchSize;
    }

    public Instant getFirstRunAt() {
      return firstRunAt;
    }

    public void setFirstRunAt(Instant firstRunAt) {
      this.firstRunAt = firstRunAt;
    }

    public Duration getRunEvery() {
      return runEvery;
    }

    public void setRunEvery(Duration runEvery) {
      this.runEvery = runEvery;
    }

    public Duration getLockExpirationPeriod() {
      return lockExpirationPeriod;
    }

    public void setLockExpirationPeriod(Duration lockExpirationPeriod) {
      this.lockExpirationPeriod = lockExpirationPeriod;
    }

    public Cleanup getCleanup() {
      return cleanup;
    }

    public void setCleanup(Cleanup cleanup) {
      this.cleanup = cleanup;
    }

    public Priority getPriority() {
      return priority;
    }

    public void setPriority(Priority priority) {
      this.priority = priority;
    }

    public Refresh getRefresh() {
      return refresh;
    }

    public void setRefresh(Refresh refresh) {
      this.refresh = refresh;
    }

    public Set<String> getCustomJobs() {
      return customJobs;
    }

    public void setCustomJobs(Set<String> customJobs) {
      this.customJobs = customJobs;
    }
  }

  public static class Scheduler {
    private boolean enabled = true;
    private long initialStartDelay = 0;
    private long period = 5;
    private TimeUnit periodTimeUnit = TimeUnit.MINUTES;

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public long getInitialStartDelay() {
      return initialStartDelay;
    }

    public void setInitialStartDelay(long initialStartDelay) {
      this.initialStartDelay = initialStartDelay;
    }

    public long getPeriod() {
      return period;
    }

    public void setPeriod(long period) {
      this.period = period;
    }

    public TimeUnit getPeriodTimeUnit() {
      return periodTimeUnit;
    }

    public void setPeriodTimeUnit(TimeUnit periodTimeUnit) {
      this.periodTimeUnit = periodTimeUnit;
    }
  }

  public static class Cleanup {
    private CleanupTask task = new CleanupTask();
    private CleanupWorkbasket workbasket = new CleanupWorkbasket();
    private CleanupHistory history = new CleanupHistory();

    public CleanupTask getTask() {
      return task;
    }

    public void setTask(CleanupTask task) {
      this.task = task;
    }

    public CleanupWorkbasket getWorkbasket() {
      return workbasket;
    }

    public void setWorkbasket(CleanupWorkbasket workbasket) {
      this.workbasket = workbasket;
    }

    public CleanupHistory getHistory() {
      return history;
    }

    public void setHistory(CleanupHistory history) {
      this.history = history;
    }
  }

  public static class CleanupTask {
    private boolean enable = true;
    private Duration minimumAge = Duration.ofDays(14);
    private boolean allCompletedSameParentBusiness = true;
    private Duration lockExpirationPeriod = Duration.ofMinutes(30);

    public boolean isEnable() {
      return enable;
    }

    public void setEnable(boolean enable) {
      this.enable = enable;
    }

    public Duration getMinimumAge() {
      return minimumAge;
    }

    public void setMinimumAge(Duration minimumAge) {
      this.minimumAge = minimumAge;
    }

    public boolean isAllCompletedSameParentBusiness() {
      return allCompletedSameParentBusiness;
    }

    public void setAllCompletedSameParentBusiness(boolean allCompletedSameParentBusiness) {
      this.allCompletedSameParentBusiness = allCompletedSameParentBusiness;
    }

    public Duration getLockExpirationPeriod() {
      return lockExpirationPeriod;
    }

    public void setLockExpirationPeriod(Duration lockExpirationPeriod) {
      this.lockExpirationPeriod = lockExpirationPeriod;
    }
  }

  public static class CleanupWorkbasket {
    private boolean enable = true;
    private Duration lockExpirationPeriod = Duration.ofMinutes(30);

    public boolean isEnable() {
      return enable;
    }

    public void setEnable(boolean enable) {
      this.enable = enable;
    }

    public Duration getLockExpirationPeriod() {
      return lockExpirationPeriod;
    }

    public void setLockExpirationPeriod(Duration lockExpirationPeriod) {
      this.lockExpirationPeriod = lockExpirationPeriod;
    }
  }

  public static class CleanupHistory {
    private CleanupHistorySimple simple = new CleanupHistorySimple();

    public CleanupHistorySimple getSimple() {
      return simple;
    }

    public void setSimple(CleanupHistorySimple simple) {
      this.simple = simple;
    }
  }

  public static class CleanupHistorySimple {
    private boolean enable = false;
    private int batchSize = 100;
    private Duration minimumAge = Duration.ofDays(14);
    private boolean allCompletedSameParentBusiness = true;
    private Duration lockExpirationPeriod = Duration.ofMinutes(30);

    public boolean isEnable() {
      return enable;
    }

    public void setEnable(boolean enable) {
      this.enable = enable;
    }

    public int getBatchSize() {
      return batchSize;
    }

    public void setBatchSize(int batchSize) {
      this.batchSize = batchSize;
    }

    public Duration getMinimumAge() {
      return minimumAge;
    }

    public void setMinimumAge(Duration minimumAge) {
      this.minimumAge = minimumAge;
    }

    public boolean isAllCompletedSameParentBusiness() {
      return allCompletedSameParentBusiness;
    }

    public void setAllCompletedSameParentBusiness(boolean allCompletedSameParentBusiness) {
      this.allCompletedSameParentBusiness = allCompletedSameParentBusiness;
    }

    public Duration getLockExpirationPeriod() {
      return lockExpirationPeriod;
    }

    public void setLockExpirationPeriod(Duration lockExpirationPeriod) {
      this.lockExpirationPeriod = lockExpirationPeriod;
    }
  }

  public static class Priority {
    private PriorityTask task = new PriorityTask();

    public PriorityTask getTask() {
      return task;
    }

    public void setTask(PriorityTask task) {
      this.task = task;
    }
  }

  public static class PriorityTask {
    private boolean enable = false;
    private int batchSize = 100;
    private Instant firstRunAt = Instant.parse("2023-01-01T00:00:00Z");
    private Duration runEvery = Duration.ofDays(1);
    private Duration lockExpirationPeriod = Duration.ofMinutes(30);

    public boolean isEnable() {
      return enable;
    }

    public void setEnable(boolean enable) {
      this.enable = enable;
    }

    public int getBatchSize() {
      return batchSize;
    }

    public void setBatchSize(int batchSize) {
      this.batchSize = batchSize;
    }

    public Instant getFirstRunAt() {
      return firstRunAt;
    }

    public void setFirstRunAt(Instant firstRunAt) {
      this.firstRunAt = firstRunAt;
    }

    public Duration getRunEvery() {
      return runEvery;
    }

    public void setRunEvery(Duration runEvery) {
      this.runEvery = runEvery;
    }

    public Duration getLockExpirationPeriod() {
      return lockExpirationPeriod;
    }

    public void setLockExpirationPeriod(Duration lockExpirationPeriod) {
      this.lockExpirationPeriod = lockExpirationPeriod;
    }
  }

  public static class Refresh {
    private RefreshUser user = new RefreshUser();

    public RefreshUser getUser() {
      return user;
    }

    public void setUser(RefreshUser user) {
      this.user = user;
    }
  }

  public static class RefreshUser {
    private boolean enable = false;
    private Instant firstRunAt = Instant.parse("2023-01-01T23:00:00Z");
    private Duration runEvery = Duration.ofDays(1);
    private Duration lockExpirationPeriod = Duration.ofMinutes(30);

    public boolean isEnable() {
      return enable;
    }

    public void setEnable(boolean enable) {
      this.enable = enable;
    }

    public Instant getFirstRunAt() {
      return firstRunAt;
    }

    public void setFirstRunAt(Instant firstRunAt) {
      this.firstRunAt = firstRunAt;
    }

    public Duration getRunEvery() {
      return runEvery;
    }

    public void setRunEvery(Duration runEvery) {
      this.runEvery = runEvery;
    }

    public Duration getLockExpirationPeriod() {
      return lockExpirationPeriod;
    }

    public void setLockExpirationPeriod(Duration lockExpirationPeriod) {
      this.lockExpirationPeriod = lockExpirationPeriod;
    }
  }

  public static class User {
    private boolean addAdditionalUserInfo = false;
    private Set<WorkbasketPermission> minimalPermissionsToAssignDomains = new HashSet<>();

    public boolean isAddAdditionalUserInfo() {
      return addAdditionalUserInfo;
    }

    public void setAddAdditionalUserInfo(boolean addAdditionalUserInfo) {
      this.addAdditionalUserInfo = addAdditionalUserInfo;
    }

    public Set<WorkbasketPermission> getMinimalPermissionsToAssignDomains() {
      return minimalPermissionsToAssignDomains;
    }

    public void setMinimalPermissionsToAssignDomains(
        Set<WorkbasketPermission> minimalPermissionsToAssignDomains) {
      this.minimalPermissionsToAssignDomains = minimalPermissionsToAssignDomains;
    }
  }

  public static class Feature {
    private boolean useSpecificDb2Taskquery = true;

    public boolean isUseSpecificDb2Taskquery() {
      return useSpecificDb2Taskquery;
    }

    public void setUseSpecificDb2Taskquery(boolean useSpecificDb2Taskquery) {
      this.useSpecificDb2Taskquery = useSpecificDb2Taskquery;
    }
  }

  private static Map<DayOfWeek, Set<TimeInterval>> initDefaultWorkingTimeSchedule() {
    Map<DayOfWeek, Set<TimeInterval>> workingTime = new EnumMap<>(DayOfWeek.class);
    Set<TimeInterval> standardWorkingSlots =
        Set.of(new TimeInterval(LocalTime.MIN, LocalTime.MAX));
    workingTime.put(DayOfWeek.MONDAY, standardWorkingSlots);
    workingTime.put(DayOfWeek.TUESDAY, standardWorkingSlots);
    workingTime.put(DayOfWeek.WEDNESDAY, standardWorkingSlots);
    workingTime.put(DayOfWeek.THURSDAY, standardWorkingSlots);
    workingTime.put(DayOfWeek.FRIDAY, standardWorkingSlots);
    return workingTime;
  }
}
