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
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
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
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.BindException;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

/** Bindable KADAI configuration properties. */
@ConfigurationProperties(prefix = "kadai")
public class KadaiProperties {

  private static final ValidatorFactory VALIDATOR_FACTORY = createValidatorFactory();
  private static final Validator VALIDATOR = VALIDATOR_FACTORY.getValidator();

  /**
   * Raw properties as loaded for KADAI.
   *
   * <p>This is mainly used to retain custom extension properties and should not normally be
   * configured through Spring Boot binding.
   */
  @NotNull(message = "kadai.properties")
  private Map<String, String> properties = Map.of();

  /**
   * Domains available for classifications and workbaskets.
   *
   * <p>Values are normalized to upper case when KADAI builds the final configuration.
   */
  @NotNull(message = "kadai.domains")
  private List<String> domains = new ArrayList<>();

  /** Service-level validation configuration. */
  @Valid
  @NotNull(message = "kadai.service-level")
  private ServiceLevel serviceLevel = new ServiceLevel();

  /** Task routing configuration. */
  @NotNull(message = "kadai.routing")
  private Routing routing = new Routing();

  /**
   * Role assignments from KADAI roles to access ids.
   *
   * <p>Configure values with indexed entries, for example {@code kadai.roles.user[0]=user-1}.
   */
  @NotNull(message = "kadai.roles")
  private Map<KadaiRole, Set<String>> roles = new EnumMap<>(KadaiRole.class);

  /** Classification type and category configuration. */
  @Valid
  @NotNull(message = "kadai.classification")
  private Classification classification = new Classification();

  /** Working time calculation configuration. */
  @Valid
  @NotNull(message = "kadai.working-time")
  private WorkingTime workingTime = new WorkingTime();

  /** Simple history and audit logger configuration. */
  @Valid
  @NotNull(message = "kadai.history")
  private History history = new History();

  /** Background job configuration. */
  @Valid
  @NotNull(message = "kadai.jobs")
  private Jobs jobs = new Jobs();

  /** User-related KADAI configuration. */
  @Valid
  @NotNull(message = "kadai.user")
  private User user = new User();

  /** Feature flags for KADAI internals. */
  @NotNull(message = "kadai.feature")
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
    Set<ConstraintViolation<KadaiProperties>> violations = VALIDATOR.validate(this);
    if (!violations.isEmpty()) {
      String invalidProperties =
          violations.stream()
              .map(KadaiProperties::toPropertyName)
              .sorted()
              .collect(Collectors.joining(", "));
      throw new SystemException(
          String.format("Invalid KADAI properties: %s", invalidProperties));
    }
  }

  private static ValidatorFactory createValidatorFactory() {
    return jakarta.validation.Validation.byDefaultProvider()
        .configure()
        .messageInterpolator(new ParameterMessageInterpolator())
        .buildValidatorFactory();
  }

  private static String toPropertyName(ConstraintViolation<KadaiProperties> violation) {
    String message = violation.getMessage();
    if (message.startsWith("kadai.")) {
      return message;
    }
    return "kadai." + toKebabCasePropertyPath(violation.getPropertyPath().toString());
  }

  private static String toKebabCasePropertyPath(String propertyPath) {
    String normalizedPath =
        propertyPath
            .replaceAll("\\[([^\\]]+)]\\.<map value>\\[\\]", ".$1[]")
            .replace(".<map value>", "")
            .replace(".<iterable element>", "")
            .replaceAll("\\[[0-9]+]", "[]");
    return java.util.Arrays.stream(normalizedPath.split("\\."))
        .map(KadaiProperties::toKebabCasePropertyPathSegment)
        .collect(Collectors.joining("."));
  }

  private static String toKebabCasePropertyPathSegment(String segment) {
    if (segment.matches("[A-Z_]+(\\[\\])?")) {
      return segment;
    }
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < segment.length(); i++) {
      char c = segment.charAt(i);
      if (Character.isUpperCase(c)) {
        result.append('-').append(Character.toLowerCase(c));
      } else {
        result.append(c);
      }
    }
    return result.toString();
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
    @NotNull(message = "kadai.service-level.validation")
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
    @NotNull(message = "kadai.classification.types")
    private List<String> types = new ArrayList<>();

    /**
     * Classification categories for each configured classification type.
     *
     * <p>The map key is the classification type, for example {@code task} or {@code document}.
     */
    @NotNull(message = "kadai.classification.categories")
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
    @NotNull(message = "kadai.working-time.schedule")
    private Map<DayOfWeek, @NotNull @Valid Set<@NotNull @Valid TimeInterval>> schedule =
        initDefaultWorkingTimeSchedule();

    /** Time zone used for working time calculation. */
    @NotNull(message = "kadai.working-time.timezone")
    private ZoneId timezone = ZoneId.of("Europe/Berlin");

    /** Holiday configuration for working time calculation. */
    @Valid
    @NotNull(message = "kadai.working-time.holidays")
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
    @NotNull
    private LocalTime begin = LocalTime.MIN;

    /**
     * End time of a working time interval.
     *
     * <p>{@code 00:00} is interpreted as the end of the day.
     */
    @NotNull
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
    @NotNull(message = "kadai.working-time.holidays.custom")
    private Set<@NotNull @Valid CustomHolidayProperties> custom = new HashSet<>();

    /** German public holiday configuration. */
    @Valid
    @NotNull(message = "kadai.working-time.holidays.german")
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
    @NotNull(message = "kadai.working-time.holidays.custom[].day")
    @Min(value = 1, message = "kadai.working-time.holidays.custom[].day")
    @Max(value = 31, message = "kadai.working-time.holidays.custom[].day")
    private Integer day;

    /** Month for a custom holiday. Valid values are 1 through 12. */
    @NotNull(message = "kadai.working-time.holidays.custom[].month")
    @Min(value = 1, message = "kadai.working-time.holidays.custom[].month")
    @Max(value = 12, message = "kadai.working-time.holidays.custom[].month")
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
    @NotNull(message = "kadai.working-time.holidays.german.corpus-christi")
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
    @Valid
    @NotNull(message = "kadai.history.simple")
    private Simple simple = new Simple();

    /** Audit/history logger configuration. */
    @NotNull(message = "kadai.history.logger")
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
    @NotNull(message = "kadai.history.simple.delete-on-task-deletion")
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
    @Valid
    @NotNull(message = "kadai.jobs.scheduler")
    private Scheduler scheduler = new Scheduler();

    /** Number of automatic retries after a job has failed. */
    @Positive(message = "kadai.jobs.max-retries")
    private int maxRetries = 3;

    /** Upper bound for how many tasks can be processed by one job. */
    @Positive(message = "kadai.jobs.batch-size")
    private int batchSize = 100;

    /** Default first execution time for KADAI jobs. */
    @NotNull(message = "kadai.jobs.first-run-at")
    private Instant firstRunAt = Instant.parse("2023-01-01T00:00:00Z");

    /** Default period between executions of KADAI jobs. */
    @NotNull(message = "kadai.jobs.run-every")
    private Duration runEvery = Duration.ofDays(1);

    /**
     * Default duration for which a job lock is valid.
     *
     * <p>It should be longer than the longest expected job execution time.
     */
    @NotNull(message = "kadai.jobs.lock-expiration-period")
    private Duration lockExpirationPeriod = Duration.ofMinutes(30);

    /** Cleanup job configuration. */
    @Valid
    @NotNull(message = "kadai.jobs.cleanup")
    private Cleanup cleanup = new Cleanup();

    /** Task priority recalculation job configuration. */
    @Valid
    @NotNull(message = "kadai.jobs.priority")
    private Priority priority = new Priority();

    /** User information refresh job configuration. */
    @Valid
    @NotNull(message = "kadai.jobs.refresh")
    private Refresh refresh = new Refresh();

    /** Fully qualified class names of custom KADAI jobs to initialize. */
    @NotNull(message = "kadai.jobs.custom-jobs")
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
    @PositiveOrZero(message = "kadai.jobs.scheduler.initial-start-delay")
    private long initialStartDelay = 0;

    /** Interval between scheduler runs. */
    @Positive(message = "kadai.jobs.scheduler.period")
    private long period = 5;

    /** Time unit for the scheduler initial start delay and scheduler period. */
    @NotNull(message = "kadai.jobs.scheduler.period-time-unit")
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
    @Valid
    @NotNull(message = "kadai.jobs.cleanup.task")
    private CleanupTask task = new CleanupTask();

    /** Workbasket cleanup job configuration. */
    @Valid
    @NotNull(message = "kadai.jobs.cleanup.workbasket")
    private CleanupWorkbasket workbasket = new CleanupWorkbasket();

    /** History cleanup job configuration. */
    @Valid
    @NotNull(message = "kadai.jobs.cleanup.history")
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

  public static class CleanupTask extends CleanupJob {
    /** Whether automated cleanup of completed tasks is enabled. */
    private boolean enable = true;

    /** Minimum age a completed task must have before it may be deleted by the cleanup job. */
    @NotNull
    private Duration minimumAge = Duration.ofDays(14);

    /**
     * Whether completed tasks are kept if other tasks with the same parent business process id are
     * not completed yet.
     */
    private boolean allCompletedSameParentBusiness = true;

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
  }

  public static class CleanupWorkbasket extends CleanupJob {
    /** Whether the workbasket cleanup job is enabled. */
    private boolean enable = true;

    public boolean isEnable() {
      return enable;
    }

    public void setEnable(boolean enable) {
      this.enable = enable;
    }
  }

  public static class CleanupHistory {
    /** Simple history cleanup job configuration. */
    @Valid
    @NotNull(message = "kadai.jobs.cleanup.history.simple")
    private CleanupHistorySimple simple = new CleanupHistorySimple();

    public CleanupHistorySimple getSimple() {
      return simple;
    }

    public void setSimple(CleanupHistorySimple simple) {
      this.simple = simple;
    }
  }

  public static class CleanupHistorySimple extends BatchCleanupJob {
    /** Whether the simple history cleanup job is enabled. */
    private boolean enable = false;

    public boolean isEnable() {
      return enable;
    }

    public void setEnable(boolean enable) {
      this.enable = enable;
    }
  }

  public static class Priority {
    /** Task priority recalculation job configuration. */
    @Valid
    @NotNull(message = "kadai.jobs.priority.task")
    private PriorityTask task = new PriorityTask();

    public PriorityTask getTask() {
      return task;
    }

    public void setTask(PriorityTask task) {
      this.task = task;
    }
  }

  public static class PriorityTask extends BatchScheduledJob {
    /**
     * Whether automated priority recalculation for tasks that are not in an end state is enabled.
     */
    private boolean enable = false;

    /** First execution time of the task priority recalculation job. */
    @NotNull
    private Instant firstRunAt = Instant.parse("2023-01-01T00:00:00Z");

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
  }

  public static class Refresh {
    /** User information refresh job configuration. */
    @Valid
    @NotNull(message = "kadai.jobs.refresh.user")
    private RefreshUser user = new RefreshUser();

    public RefreshUser getUser() {
      return user;
    }

    public void setUser(RefreshUser user) {
      this.user = user;
    }
  }

  public static class RefreshUser extends ScheduledJob {
    /** Whether the user information refresh job is enabled. */
    private boolean enable = false;

    /** First execution time of the user information refresh job. */
    @NotNull
    private Instant firstRunAt = Instant.parse("2023-01-01T23:00:00Z");

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
  }

  public abstract static class JobWithLock {
    /**
     * Duration for which the job lock is valid.
     *
     * <p>It should be longer than the longest expected job execution time.
     */
    @NotNull
    private Duration lockExpirationPeriod = Duration.ofMinutes(30);

    public Duration getLockExpirationPeriod() {
      return lockExpirationPeriod;
    }

    public void setLockExpirationPeriod(Duration lockExpirationPeriod) {
      this.lockExpirationPeriod = lockExpirationPeriod;
    }
  }

  public abstract static class CleanupJob extends JobWithLock {}

  public abstract static class BatchCleanupJob extends CleanupJob {
    /** Upper bound for how many cleanup records can be processed by one job run. */
    @Positive
    private int batchSize = 100;

    /** Minimum age an item must have before it may be deleted by the cleanup job. */
    @NotNull
    private Duration minimumAge = Duration.ofDays(14);

    /**
     * Whether cleanup should keep items if related items with the same parent business process id
     * are not completed.
     */
    private boolean allCompletedSameParentBusiness = true;

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
  }

  public abstract static class ScheduledJob extends JobWithLock {
    /** Period between job executions. */
    @NotNull
    private Duration runEvery = Duration.ofDays(1);

    public Duration getRunEvery() {
      return runEvery;
    }

    public void setRunEvery(Duration runEvery) {
      this.runEvery = runEvery;
    }
  }

  public abstract static class BatchScheduledJob extends ScheduledJob {
    /** Upper bound for how many items can be processed by one job run. */
    @Positive
    private int batchSize = 100;

    public int getBatchSize() {
      return batchSize;
    }

    public void setBatchSize(int batchSize) {
      this.batchSize = batchSize;
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
    @NotNull(message = "kadai.user.minimal-permissions-to-assign-domains")
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
