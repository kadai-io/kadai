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
import jakarta.validation.Valid;
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
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.BindException;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.validation.annotation.Validated;

/** Bindable KADAI configuration properties. */
@Validated
@ConfigurationProperties(prefix = "kadai")
public class KadaiProperties {

  private Map<String, String> properties = Map.of();
  private List<String> domains = new ArrayList<>();
  @Valid private ServiceLevel serviceLevel = new ServiceLevel();
  @Valid private Routing routing = new Routing();
  private Map<KadaiRole, Set<String>> roles = new EnumMap<>(KadaiRole.class);
  @Valid private Classification classification = new Classification();
  @Valid private WorkingTime workingTime = new WorkingTime();
  @Valid private History history = new History();
  @Valid private Jobs jobs = new Jobs();
  @Valid private User user = new User();
  @Valid private Feature feature = new Feature();

  public static KadaiProperties load(String propertiesFile) {
    Map<String, String> rawProperties = loadRawProperties(propertiesFile);
    try {
      KadaiProperties properties =
          new Binder(new MapConfigurationPropertySource(rawProperties))
              .bind("kadai", KadaiProperties.class)
              .orElseGet(KadaiProperties::new);
      properties.properties = rawProperties;
      return properties;
    } catch (BindException e) {
      throw new SystemException(
          String.format("Could not bind properties file '%s'", propertiesFile), e);
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
    @Valid private Validation validation = new Validation();

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
    private Map<DayOfWeek, Set<@Valid TimeInterval>> schedule = initDefaultWorkingTimeSchedule();
    @NotNull private ZoneId timezone = ZoneId.of("Europe/Berlin");
    @Valid private Holidays holidays = new Holidays();

    public boolean isUseWorkingTimeCalculation() {
      return useWorkingTimeCalculation;
    }

    public void setUseWorkingTimeCalculation(boolean useWorkingTimeCalculation) {
      this.useWorkingTimeCalculation = useWorkingTimeCalculation;
    }

    public Map<DayOfWeek, Set<@Valid TimeInterval>> getSchedule() {
      return schedule;
    }

    public void setSchedule(Map<DayOfWeek, Set<@Valid TimeInterval>> schedule) {
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
    @NotNull private LocalTime begin = LocalTime.MIN;
    @NotNull private LocalTime end = LocalTime.MAX;

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
    private Set<@Valid CustomHolidayProperties> custom = new HashSet<>();
    @Valid private German german = new German();

    public Set<@Valid CustomHolidayProperties> getCustom() {
      return custom;
    }

    public void setCustom(Set<@Valid CustomHolidayProperties> custom) {
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
    @NotNull
    @Min(1)
    @Max(31)
    private Integer day;

    @NotNull
    @Min(1)
    @Max(12)
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
    @Valid private CorpusChristi corpusChristi = new CorpusChristi();

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
    @Valid private Simple simple = new Simple();
    @Valid private Logger logger = new Logger();

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
    @Valid private DeleteOnTaskDeletion deleteOnTaskDeletion = new DeleteOnTaskDeletion();

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
    @Valid private Scheduler scheduler = new Scheduler();
    @Positive private int maxRetries = 3;
    @Positive private int batchSize = 100;
    @NotNull private Instant firstRunAt = Instant.parse("2023-01-01T00:00:00Z");
    @NotNull private Duration runEvery = Duration.ofDays(1);
    @NotNull private Duration lockExpirationPeriod = Duration.ofMinutes(30);
    @Valid private Cleanup cleanup = new Cleanup();
    @Valid private Priority priority = new Priority();
    @Valid private Refresh refresh = new Refresh();
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
    @PositiveOrZero private long initialStartDelay = 0;
    @Positive private long period = 5;
    @NotNull private TimeUnit periodTimeUnit = TimeUnit.MINUTES;

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
    @Valid private CleanupTask task = new CleanupTask();
    @Valid private CleanupWorkbasket workbasket = new CleanupWorkbasket();
    @Valid private CleanupHistory history = new CleanupHistory();

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
    @NotNull private Duration minimumAge = Duration.ofDays(14);
    private boolean allCompletedSameParentBusiness = true;
    @NotNull private Duration lockExpirationPeriod = Duration.ofMinutes(30);

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
    @NotNull private Duration lockExpirationPeriod = Duration.ofMinutes(30);

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
    @Valid private CleanupHistorySimple simple = new CleanupHistorySimple();

    public CleanupHistorySimple getSimple() {
      return simple;
    }

    public void setSimple(CleanupHistorySimple simple) {
      this.simple = simple;
    }
  }

  public static class CleanupHistorySimple {
    private boolean enable = false;
    @Positive private int batchSize = 100;
    @NotNull private Duration minimumAge = Duration.ofDays(14);
    private boolean allCompletedSameParentBusiness = true;
    @NotNull private Duration lockExpirationPeriod = Duration.ofMinutes(30);

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
    @Valid private PriorityTask task = new PriorityTask();

    public PriorityTask getTask() {
      return task;
    }

    public void setTask(PriorityTask task) {
      this.task = task;
    }
  }

  public static class PriorityTask {
    private boolean enable = false;
    @Positive private int batchSize = 100;
    @NotNull private Instant firstRunAt = Instant.parse("2023-01-01T00:00:00Z");
    @NotNull private Duration runEvery = Duration.ofDays(1);
    @NotNull private Duration lockExpirationPeriod = Duration.ofMinutes(30);

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
    @Valid private RefreshUser user = new RefreshUser();

    public RefreshUser getUser() {
      return user;
    }

    public void setUser(RefreshUser user) {
      this.user = user;
    }
  }

  public static class RefreshUser {
    private boolean enable = false;
    @NotNull private Instant firstRunAt = Instant.parse("2023-01-01T23:00:00Z");
    @NotNull private Duration runEvery = Duration.ofDays(1);
    @NotNull private Duration lockExpirationPeriod = Duration.ofMinutes(30);

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
