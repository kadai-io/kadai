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

  /**
   * Raw properties as loaded for KADAI.
   *
   * <p>This is mainly used to retain custom extension properties and should not normally be
   * configured through Spring Boot binding.
   */
  private Map<String, String> properties = Map.of();

  /**
   * Domains available for classifications and workbaskets.
   *
   * <p>Values are normalized to upper case when KADAI builds the final configuration.
   */
  private List<String> domains = new ArrayList<>();

  /** Service-level validation configuration. */
  private ServiceLevel serviceLevel = new ServiceLevel();

  /** Task routing configuration. */
  private Routing routing = new Routing();

  /**
   * Role assignments from KADAI roles to access ids.
   *
   * <p>Configure values with indexed entries, for example {@code kadai.roles.user[0]=user-1}.
   */
  private Map<KadaiRole, Set<String>> roles = new EnumMap<>(KadaiRole.class);

  /** Classification type and category configuration. */
  private Classification classification = new Classification();

  /** Working time calculation configuration. */
  private WorkingTime workingTime = new WorkingTime();

  /** Simple history and audit logger configuration. */
  private History history = new History();

  /** Background job configuration. */
  private Jobs jobs = new Jobs();

  /** User-related KADAI configuration. */
  private User user = new User();

  /** Feature flags for KADAI internals. */
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
    /** Service-level validation switches. */
    private Validation validation = new Validation();

    public Validation getValidation() {
      return validation;
    }

    public void setValidation(Validation validation) {
      this.validation = validation;
    }
  }

  public static class Validation {
    /**
     * Whether KADAI enforces task date attributes to be consistent with the classification service
     * level.
     */
    private boolean enforce = true;

    public boolean isEnforce() {
      return enforce;
    }

    public void setEnforce(boolean enforce) {
      this.enforce = enforce;
    }
  }

  public static class Routing {
    /** Whether task routing should include the current task owner. */
    private boolean includeOwner = false;

    public boolean isIncludeOwner() {
      return includeOwner;
    }

    public void setIncludeOwner(boolean includeOwner) {
      this.includeOwner = includeOwner;
    }
  }

  public static class Classification {
    /**
     * Classification types.
     *
     * <p>Values are case-insensitive and are normalized to upper case when KADAI builds the final
     * configuration.
     */
    private List<String> types = new ArrayList<>();

    /**
     * Classification categories for each configured classification type.
     *
     * <p>The map key is the classification type, for example {@code task} or {@code document}.
     */
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
    /**
     * Whether KADAI calculates service levels from configured working time intervals.
     *
     * <p>If disabled, KADAI uses working-day based calculation.
     */
    private boolean useWorkingTimeCalculation = true;

    /**
     * Working time intervals by day of week.
     *
     * <p>Configure values with indexed entries, for example {@code
     * kadai.working-time.schedule.monday[0].begin=09:00} and {@code
     * kadai.working-time.schedule.monday[0].end=18:00}.
     */
    private Map<DayOfWeek, Set<TimeInterval>> schedule = initDefaultWorkingTimeSchedule();

    /** Time zone used for working time calculation. */
    private ZoneId timezone = ZoneId.of("Europe/Berlin");

    /** Holiday configuration for working time calculation. */
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
    /** Start time of a working time interval. */
    private LocalTime begin = LocalTime.MIN;

    /**
     * End time of a working time interval.
     *
     * <p>{@code 00:00} is interpreted as the end of the day.
     */
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
    /**
     * Custom holidays that do not count as working time.
     *
     * <p>Configure each holiday with day and month entries, for example {@code
     * kadai.working-time.holidays.custom[0].day=31} and {@code
     * kadai.working-time.holidays.custom[0].month=7}.
     */
    private Set<CustomHolidayProperties> custom = new HashSet<>();

    /** German public holiday configuration. */
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
    /** Day of month for a custom holiday. Valid values are 1 through 31. */
    private Integer day;

    /** Month for a custom holiday. Valid values are 1 through 12. */
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
    /** Whether standard German public holidays are enabled. */
    private boolean enabled = true;

    /** German Corpus Christi public holiday configuration. */
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
    /** Whether the German Corpus Christi public holiday is enabled. */
    private boolean enabled = false;

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }
  }

  public static class History {
    /** Simple history configuration. */
    private Simple simple = new Simple();

    /** Audit/history logger configuration. */
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
    /** Task-deletion behavior for simple history events. */
    private DeleteOnTaskDeletion deleteOnTaskDeletion = new DeleteOnTaskDeletion();

    public DeleteOnTaskDeletion getDeleteOnTaskDeletion() {
      return deleteOnTaskDeletion;
    }

    public void setDeleteOnTaskDeletion(DeleteOnTaskDeletion deleteOnTaskDeletion) {
      this.deleteOnTaskDeletion = deleteOnTaskDeletion;
    }
  }

  public static class DeleteOnTaskDeletion {
    /** Whether simple history events for a task are deleted when the task itself is deleted. */
    private boolean enabled = false;

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }
  }

  public static class Logger {
    /** Logger name used for audit/history logging, for example {@code AUDIT}. */
    private String name = null;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }

  public static class Jobs {
    /** Scheduler configuration for KADAI jobs. */
    private Scheduler scheduler = new Scheduler();

    /** Number of automatic retries after a job has failed. */
    private int maxRetries = 3;

    /** Upper bound for how many tasks can be processed by one job. */
    private int batchSize = 100;

    /** Default first execution time for KADAI jobs. */
    private Instant firstRunAt = Instant.parse("2023-01-01T00:00:00Z");

    /** Default period between executions of KADAI jobs. */
    private Duration runEvery = Duration.ofDays(1);

    /**
     * Default duration for which a job lock is valid.
     *
     * <p>It should be longer than the longest expected job execution time.
     */
    private Duration lockExpirationPeriod = Duration.ofMinutes(30);

    /** Cleanup job configuration. */
    private Cleanup cleanup = new Cleanup();

    /** Task priority recalculation job configuration. */
    private Priority priority = new Priority();

    /** User information refresh job configuration. */
    private Refresh refresh = new Refresh();

    /** Fully qualified class names of custom KADAI jobs to initialize. */
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
    /** Whether automated scheduling of KADAI jobs is enabled. */
    private boolean enabled = true;

    /** Delay before the scheduler performs its first run. */
    private long initialStartDelay = 0;

    /** Interval between scheduler runs. */
    private long period = 5;

    /** Time unit for the scheduler initial start delay and scheduler period. */
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
    /** Task cleanup job configuration. */
    private CleanupTask task = new CleanupTask();

    /** Workbasket cleanup job configuration. */
    private CleanupWorkbasket workbasket = new CleanupWorkbasket();

    /** History cleanup job configuration. */
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
    /** Whether automated cleanup of completed tasks is enabled. */
    private boolean enable = true;

    /** Minimum age a completed task must have before it may be deleted by the cleanup job. */
    private Duration minimumAge = Duration.ofDays(14);

    /**
     * Whether completed tasks are kept if other tasks with the same parent business process id are
     * not completed yet.
     */
    private boolean allCompletedSameParentBusiness = true;

    /**
     * Duration for which the task cleanup job lock is valid.
     *
     * <p>It should be longer than the longest expected job execution time.
     */
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
    /** Whether the workbasket cleanup job is enabled. */
    private boolean enable = true;

    /**
     * Duration for which the workbasket cleanup job lock is valid.
     *
     * <p>It should be longer than the longest expected job execution time.
     */
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
    /** Simple history cleanup job configuration. */
    private CleanupHistorySimple simple = new CleanupHistorySimple();

    public CleanupHistorySimple getSimple() {
      return simple;
    }

    public void setSimple(CleanupHistorySimple simple) {
      this.simple = simple;
    }
  }

  public static class CleanupHistorySimple {
    /** Whether the simple history cleanup job is enabled. */
    private boolean enable = false;

    /**
     * Upper bound for how many history events can be processed by one simple history cleanup job.
     */
    private int batchSize = 100;

    /** Minimum age a simple history event must have before it may be deleted by the cleanup job. */
    private Duration minimumAge = Duration.ofDays(14);

    /**
     * Whether simple history cleanup should keep task history events if related task history events
     * with the same parent business process id are not completed.
     */
    private boolean allCompletedSameParentBusiness = true;

    /**
     * Duration for which the simple history cleanup job lock is valid.
     *
     * <p>It should be longer than the longest expected job execution time.
     */
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
    /** Task priority recalculation job configuration. */
    private PriorityTask task = new PriorityTask();

    public PriorityTask getTask() {
      return task;
    }

    public void setTask(PriorityTask task) {
      this.task = task;
    }
  }

  public static class PriorityTask {
    /**
     * Whether automated priority recalculation for tasks that are not in an end state is enabled.
     */
    private boolean enable = false;

    /** Upper bound for how many tasks can be processed by one task priority recalculation job. */
    private int batchSize = 100;

    /** First execution time of the task priority recalculation job. */
    private Instant firstRunAt = Instant.parse("2023-01-01T00:00:00Z");

    /** Period between executions of the task priority recalculation job. */
    private Duration runEvery = Duration.ofDays(1);

    /**
     * Duration for which the task priority recalculation job lock is valid.
     *
     * <p>It should be longer than the longest expected job execution time.
     */
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
    /** User information refresh job configuration. */
    private RefreshUser user = new RefreshUser();

    public RefreshUser getUser() {
      return user;
    }

    public void setUser(RefreshUser user) {
      this.user = user;
    }
  }

  public static class RefreshUser {
    /** Whether the user information refresh job is enabled. */
    private boolean enable = false;

    /** First execution time of the user information refresh job. */
    private Instant firstRunAt = Instant.parse("2023-01-01T23:00:00Z");

    /** Period between executions of the user information refresh job. */
    private Duration runEvery = Duration.ofDays(1);

    /**
     * Duration for which the user information refresh job lock is valid.
     *
     * <p>It should be longer than the longest expected job execution time.
     */
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
    /**
     * Whether additional attributes from the {@code USER_INFO} table are added to user-related task
     * responses and task queries.
     */
    private boolean addAdditionalUserInfo = false;

    /**
     * Minimal workbasket permissions a user needs to be assigned to a domain during dynamic domain
     * computation.
     */
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
    /** Whether KADAI should use the DB2-specific task query implementation. */
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
