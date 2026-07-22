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

import static io.kadai.common.api.SharedConstants.MASTER_DOMAIN;

import io.kadai.common.api.CustomHoliday;
import io.kadai.common.api.KadaiRole;
import io.kadai.common.api.LocalTimeInterval;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.api.exceptions.SystemException;
import io.kadai.common.internal.configuration.DB;
import io.kadai.common.internal.util.Pair;
import io.kadai.workbasket.api.WorkbasketPermission;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This central class creates the KadaiEngine and holds all the information about DB and Security.
 * <br>
 * Security is enabled by default. <br>
 * All members are immutable, also Lists and Maps and Sets.
 */
public class KadaiConfiguration {

  // region general configuration
  private final DataSource dataSource;
  private final boolean useManagedTransactions;
  private final String schemaName;
  private final boolean securityEnabled;

  private final List<String> domains;
  private final boolean enforceServiceLevel;
  private final boolean includeOwnerWhenRouting;
  // endregion

  // region authentication configuration
  private final Map<KadaiRole, Set<String>> roleMap;
  // endregion

  // region classification configuration
  private final List<String> classificationTypes;

  private final Map<String, List<String>> classificationCategoriesByType;
  // endregion

  // region working time configuration
  private final boolean useWorkingTimeCalculation;
  private final Map<DayOfWeek, Set<LocalTimeInterval>> workingTimeSchedule;
  private final ZoneId workingTimeScheduleTimeZone;
  private final Set<CustomHoliday> customHolidays;
  private final boolean germanPublicHolidaysEnabled;
  private final boolean germanPublicHolidaysCorpusChristiEnabled;
  // endregion

  // region history configuration
  private final boolean deleteHistoryEventsOnTaskDeletionEnabled;
  private final String logHistoryLoggerName;
  // endregion

  // region job configuration
  private final boolean jobSchedulerEnabled;
  private final long jobSchedulerInitialStartDelay;
  private final long jobSchedulerPeriod;
  private final TimeUnit jobSchedulerPeriodTimeUnit;
  private final int maxNumberOfJobRetries;
  private final int jobBatchSize;
  private final Instant jobFirstRun;
  private final Duration jobRunEvery;
  private final Duration jobLockExpirationPeriod;
  private final boolean taskCleanupJobEnabled;
  private final Duration taskCleanupJobMinimumAge;
  private final boolean taskCleanupJobAllCompletedSameParentBusiness;
  private final Duration taskCleanupJobLockExpirationPeriod;

  private final boolean workbasketCleanupJobEnabled;

  private final Duration workbasketCleanupJobLockExpirationPeriod;
  private final boolean simpleHistoryCleanupJobEnabled;
  private final int simpleHistoryCleanupJobBatchSize;
  private final Duration simpleHistoryCleanupJobMinimumAge;
  private final boolean simpleHistoryCleanupJobAllCompletedSameParentBusiness;
  private final Duration simpleHistoryCleanupJobLockExpirationPeriod;
  private final boolean taskUpdatePriorityJobEnabled;
  private final int taskUpdatePriorityJobBatchSize;
  private final Instant taskUpdatePriorityJobFirstRun;
  private final Duration taskUpdatePriorityJobRunEvery;
  private final Duration taskUpdatePriorityJobLockExpirationPeriod;

  private final boolean userInfoRefreshJobEnabled;
  private final Instant userRefreshJobFirstRun;
  private final Duration userRefreshJobRunEvery;
  private final Duration userRefreshJobLockExpirationPeriod;

  private final Set<String> customJobs;
  // endregion

  // region user configuration
  private final boolean addAdditionalUserInfo;
  private final Set<WorkbasketPermission> minimalPermissionsToAssignDomains;
  // endregion

  // region database configuration
  private final boolean useSpecificDb2Taskquery;
  // endregion

  // region custom configuration
  private final Map<String, String> properties;

  // endregion

  private KadaiConfiguration(Builder builder) {
    // general configuration
    this.dataSource = builder.dataSource;
    this.useManagedTransactions = builder.useManagedTransactions;
    this.schemaName = builder.schemaName;
    this.securityEnabled = builder.securityEnabled;
    this.domains = Collections.unmodifiableList(builder.domains);
    this.enforceServiceLevel = builder.enforceServiceLevel;
    this.includeOwnerWhenRouting = builder.includeOwnerWhenRouting;
    // authentication configuration
    this.roleMap =
        builder.roleMap.entrySet().stream()
            .collect(
                Collectors.toUnmodifiableMap(
                    Entry::getKey, e -> Collections.unmodifiableSet(e.getValue())));
    // classification configuration
    this.classificationTypes = Collections.unmodifiableList(builder.classificationTypes);
    this.classificationCategoriesByType =
        Collections.unmodifiableMap(
            builder.classificationCategoriesByType.entrySet().stream()
                .collect(
                    Collectors.toMap(
                        Map.Entry::getKey,
                        e -> Collections.unmodifiableList(e.getValue()),
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new)));
    // working time configuration
    this.useWorkingTimeCalculation = builder.useWorkingTimeCalculation;
    this.workingTimeSchedule =
        builder.workingTimeSchedule.entrySet().stream()
            .collect(
                Collectors.toUnmodifiableMap(
                    Entry::getKey, e -> Collections.unmodifiableSet(e.getValue())));
    this.workingTimeScheduleTimeZone = builder.workingTimeScheduleTimeZone;
    this.customHolidays = Collections.unmodifiableSet(builder.customHolidays);
    this.germanPublicHolidaysEnabled = builder.germanPublicHolidaysEnabled;
    this.germanPublicHolidaysCorpusChristiEnabled =
        builder.germanPublicHolidaysCorpusChristiEnabled;
    // history configuration
    this.deleteHistoryEventsOnTaskDeletionEnabled =
        builder.deleteHistoryEventsOnTaskDeletionEnabled;
    this.logHistoryLoggerName = builder.logHistoryLoggerName;
    // job configuration
    this.jobSchedulerEnabled = builder.jobSchedulerEnabled;
    this.jobSchedulerInitialStartDelay = builder.jobSchedulerInitialStartDelay;
    this.jobSchedulerPeriod = builder.jobSchedulerPeriod;
    this.jobSchedulerPeriodTimeUnit = builder.jobSchedulerPeriodTimeUnit;
    this.maxNumberOfJobRetries = builder.maxNumberOfJobRetries;
    this.jobBatchSize = builder.jobBatchSize;
    this.jobFirstRun = builder.jobFirstRun;
    this.jobRunEvery = builder.jobRunEvery;
    this.jobLockExpirationPeriod = builder.jobLockExpirationPeriod;
    this.taskCleanupJobEnabled = builder.taskCleanupJobEnabled;
    this.taskCleanupJobMinimumAge = builder.taskCleanupJobMinimumAge;
    this.taskCleanupJobAllCompletedSameParentBusiness =
        builder.taskCleanupJobAllCompletedSameParentBusiness;
    this.taskCleanupJobLockExpirationPeriod = builder.taskCleanupJobLockExpirationPeriod;
    this.workbasketCleanupJobEnabled = builder.workbasketCleanupJobEnabled;
    this.workbasketCleanupJobLockExpirationPeriod =
        builder.workbasketCleanupJobLockExpirationPeriod;
    this.simpleHistoryCleanupJobEnabled = builder.simpleHistoryCleanupJobEnabled;
    this.simpleHistoryCleanupJobBatchSize = builder.simpleHistoryCleanupJobBatchSize;
    this.simpleHistoryCleanupJobMinimumAge = builder.simpleHistoryCleanupJobMinimumAge;
    this.simpleHistoryCleanupJobAllCompletedSameParentBusiness =
        builder.simpleHistoryCleanupJobAllCompletedSameParentBusiness;
    this.simpleHistoryCleanupJobLockExpirationPeriod =
        builder.simpleHistoryCleanupJobLockExpirationPeriod;
    this.taskUpdatePriorityJobEnabled = builder.taskUpdatePriorityJobEnabled;
    this.taskUpdatePriorityJobBatchSize = builder.taskUpdatePriorityJobBatchSize;
    this.taskUpdatePriorityJobFirstRun = builder.taskUpdatePriorityJobFirstRun;
    this.taskUpdatePriorityJobRunEvery = builder.taskUpdatePriorityJobRunEvery;
    this.taskUpdatePriorityJobLockExpirationPeriod =
        builder.taskUpdatePriorityJobLockExpirationPeriod;
    this.userInfoRefreshJobEnabled = builder.userInfoRefreshJobEnabled;
    this.userRefreshJobFirstRun = builder.userRefreshJobFirstRun;
    this.userRefreshJobRunEvery = builder.userRefreshJobRunEvery;
    this.userRefreshJobLockExpirationPeriod = builder.userRefreshJobLockExpirationPeriod;
    this.customJobs = Collections.unmodifiableSet(builder.customJobs);
    // user configuration
    this.addAdditionalUserInfo = builder.addAdditionalUserInfo;
    this.minimalPermissionsToAssignDomains =
        Collections.unmodifiableSet(builder.minimalPermissionsToAssignDomains);
    // database configuration
    this.useSpecificDb2Taskquery = builder.useSpecificDb2Taskquery;
    // custom configuration
    this.properties = Map.copyOf(builder.properties);
  }

  public List<String> getAllClassificationCategories() {
    return this.classificationCategoriesByType.values().stream()
        .flatMap(Collection::stream)
        .toList();
  }

  public List<String> getClassificationCategoriesByType(String type) {
    return classificationCategoriesByType.getOrDefault(type, Collections.emptyList());
  }

  public Map<String, List<String>> getClassificationCategoriesByType() {
    return this.classificationCategoriesByType;
  }

  // region getters

  public DataSource getDataSource() {
    return dataSource;
  }

  public boolean isUseManagedTransactions() {
    return useManagedTransactions;
  }

  public String getSchemaName() {
    return schemaName;
  }

  public boolean isSecurityEnabled() {
    return securityEnabled;
  }

  public List<String> getDomains() {
    return domains;
  }

  public boolean isEnforceServiceLevel() {
    return enforceServiceLevel;
  }

  public boolean isIncludeOwnerWhenRouting() {
    return includeOwnerWhenRouting;
  }

  public Map<KadaiRole, Set<String>> getRoleMap() {
    return roleMap;
  }

  public List<String> getClassificationTypes() {
    return classificationTypes;
  }

  public boolean isUseWorkingTimeCalculation() {
    return useWorkingTimeCalculation;
  }

  public Map<DayOfWeek, Set<LocalTimeInterval>> getWorkingTimeSchedule() {
    return workingTimeSchedule;
  }

  public ZoneId getWorkingTimeScheduleTimeZone() {
    return workingTimeScheduleTimeZone;
  }

  public Set<CustomHoliday> getCustomHolidays() {
    return customHolidays;
  }

  public boolean isGermanPublicHolidaysEnabled() {
    return germanPublicHolidaysEnabled;
  }

  public boolean isGermanPublicHolidaysCorpusChristiEnabled() {
    return germanPublicHolidaysCorpusChristiEnabled;
  }

  public boolean isDeleteHistoryEventsOnTaskDeletionEnabled() {
    return deleteHistoryEventsOnTaskDeletionEnabled;
  }

  public String getLogHistoryLoggerName() {
    return logHistoryLoggerName;
  }

  public boolean isJobSchedulerEnabled() {
    return jobSchedulerEnabled;
  }

  public long getJobSchedulerInitialStartDelay() {
    return jobSchedulerInitialStartDelay;
  }

  public long getJobSchedulerPeriod() {
    return jobSchedulerPeriod;
  }

  public TimeUnit getJobSchedulerPeriodTimeUnit() {
    return jobSchedulerPeriodTimeUnit;
  }

  public int getMaxNumberOfJobRetries() {
    return maxNumberOfJobRetries;
  }

  public int getJobBatchSize() {
    return jobBatchSize;
  }

  public Instant getJobFirstRun() {
    return jobFirstRun;
  }

  public Duration getJobRunEvery() {
    return jobRunEvery;
  }

  public Duration getJobLockExpirationPeriod() {
    return jobLockExpirationPeriod;
  }

  public boolean isTaskCleanupJobEnabled() {
    return taskCleanupJobEnabled;
  }

  public Duration getTaskCleanupJobMinimumAge() {
    return taskCleanupJobMinimumAge;
  }

  public boolean isTaskCleanupJobAllCompletedSameParentBusiness() {
    return taskCleanupJobAllCompletedSameParentBusiness;
  }

  public Duration getTaskCleanupJobLockExpirationPeriod() {
    return taskCleanupJobLockExpirationPeriod;
  }

  public boolean isWorkbasketCleanupJobEnabled() {
    return workbasketCleanupJobEnabled;
  }

  public Duration getWorkbasketCleanupJobLockExpirationPeriod() {
    return workbasketCleanupJobLockExpirationPeriod;
  }

  public boolean isSimpleHistoryCleanupJobEnabled() {
    return simpleHistoryCleanupJobEnabled;
  }

  public int getSimpleHistoryCleanupJobBatchSize() {
    return simpleHistoryCleanupJobBatchSize;
  }

  public Duration getSimpleHistoryCleanupJobMinimumAge() {
    return simpleHistoryCleanupJobMinimumAge;
  }

  public boolean isSimpleHistoryCleanupJobAllCompletedSameParentBusiness() {
    return simpleHistoryCleanupJobAllCompletedSameParentBusiness;
  }

  public Duration getSimpleHistoryCleanupJobLockExpirationPeriod() {
    return simpleHistoryCleanupJobLockExpirationPeriod;
  }

  public boolean isTaskUpdatePriorityJobEnabled() {
    return taskUpdatePriorityJobEnabled;
  }

  public int getTaskUpdatePriorityJobBatchSize() {
    return taskUpdatePriorityJobBatchSize;
  }

  public Instant getTaskUpdatePriorityJobFirstRun() {
    return taskUpdatePriorityJobFirstRun;
  }

  public Duration getTaskUpdatePriorityJobRunEvery() {
    return taskUpdatePriorityJobRunEvery;
  }

  public Duration getTaskUpdatePriorityJobLockExpirationPeriod() {
    return taskUpdatePriorityJobLockExpirationPeriod;
  }

  public boolean isUserInfoRefreshJobEnabled() {
    return userInfoRefreshJobEnabled;
  }

  public Instant getUserRefreshJobFirstRun() {
    return userRefreshJobFirstRun;
  }

  public Duration getUserRefreshJobRunEvery() {
    return userRefreshJobRunEvery;
  }

  public Duration getUserRefreshJobLockExpirationPeriod() {
    return userRefreshJobLockExpirationPeriod;
  }

  public Set<String> getCustomJobs() {
    return customJobs;
  }

  public boolean isAddAdditionalUserInfo() {
    return addAdditionalUserInfo;
  }

  public Set<WorkbasketPermission> getMinimalPermissionsToAssignDomains() {
    return minimalPermissionsToAssignDomains;
  }

  public boolean isUseSpecificDb2Taskquery() {
    return useSpecificDb2Taskquery;
  }

  /**
   * return all properties loaded from kadai properties file. Per Design the normal Properties are
   * not immutable, so we return here an ImmutableMap, because we don't want direct changes in the
   * configuration.
   *
   * @return all properties loaded from kadai properties file
   */
  public Map<String, String> getProperties() {
    return properties;
  }

  // endregion

  // region hashCode, equals + toString

  private Map<String, String> filterOutNonKadaiAndSensitiveProperties() {
    return properties.entrySet().stream()
        .filter(entry -> entry.getKey().startsWith("kadai."))
        .filter((entry -> !entry.getKey().toLowerCase().contains("password")))
        .collect(Collectors.toUnmodifiableMap(Entry::getKey, Entry::getValue));
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        dataSource,
        useManagedTransactions,
        schemaName,
        securityEnabled,
        domains,
        enforceServiceLevel,
        includeOwnerWhenRouting,
        roleMap,
        classificationTypes,
        classificationCategoriesByType,
        useWorkingTimeCalculation,
        workingTimeSchedule,
        workingTimeScheduleTimeZone,
        customHolidays,
        germanPublicHolidaysEnabled,
        germanPublicHolidaysCorpusChristiEnabled,
        deleteHistoryEventsOnTaskDeletionEnabled,
        logHistoryLoggerName,
        jobSchedulerEnabled,
        jobSchedulerInitialStartDelay,
        jobSchedulerPeriod,
        jobSchedulerPeriodTimeUnit,
        maxNumberOfJobRetries,
        jobBatchSize,
        jobFirstRun,
        jobRunEvery,
        jobLockExpirationPeriod,
        taskCleanupJobEnabled,
        taskCleanupJobMinimumAge,
        taskCleanupJobAllCompletedSameParentBusiness,
        taskCleanupJobLockExpirationPeriod,
        workbasketCleanupJobEnabled,
        workbasketCleanupJobLockExpirationPeriod,
        simpleHistoryCleanupJobEnabled,
        simpleHistoryCleanupJobBatchSize,
        simpleHistoryCleanupJobMinimumAge,
        simpleHistoryCleanupJobAllCompletedSameParentBusiness,
        simpleHistoryCleanupJobLockExpirationPeriod,
        taskUpdatePriorityJobEnabled,
        taskUpdatePriorityJobBatchSize,
        taskUpdatePriorityJobFirstRun,
        taskUpdatePriorityJobRunEvery,
        taskUpdatePriorityJobLockExpirationPeriod,
        userInfoRefreshJobEnabled,
        userRefreshJobFirstRun,
        userRefreshJobRunEvery,
        userRefreshJobLockExpirationPeriod,
        customJobs,
        addAdditionalUserInfo,
        minimalPermissionsToAssignDomains,
        useSpecificDb2Taskquery,
        properties);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof KadaiConfiguration)) {
      return false;
    }
    KadaiConfiguration other = (KadaiConfiguration) obj;
    return useManagedTransactions == other.useManagedTransactions
        && securityEnabled == other.securityEnabled
        && enforceServiceLevel == other.enforceServiceLevel
        && includeOwnerWhenRouting == other.includeOwnerWhenRouting
        && useWorkingTimeCalculation == other.useWorkingTimeCalculation
        && germanPublicHolidaysEnabled == other.germanPublicHolidaysEnabled
        && germanPublicHolidaysCorpusChristiEnabled
            == other.germanPublicHolidaysCorpusChristiEnabled
        && deleteHistoryEventsOnTaskDeletionEnabled
            == other.deleteHistoryEventsOnTaskDeletionEnabled
        && jobSchedulerEnabled == other.jobSchedulerEnabled
        && jobSchedulerInitialStartDelay == other.jobSchedulerInitialStartDelay
        && jobSchedulerPeriod == other.jobSchedulerPeriod
        && maxNumberOfJobRetries == other.maxNumberOfJobRetries
        && jobBatchSize == other.jobBatchSize
        && taskCleanupJobEnabled == other.taskCleanupJobEnabled
        && taskCleanupJobAllCompletedSameParentBusiness
            == other.taskCleanupJobAllCompletedSameParentBusiness
        && workbasketCleanupJobEnabled == other.workbasketCleanupJobEnabled
        && simpleHistoryCleanupJobEnabled == other.simpleHistoryCleanupJobEnabled
        && simpleHistoryCleanupJobBatchSize == other.simpleHistoryCleanupJobBatchSize
        && simpleHistoryCleanupJobAllCompletedSameParentBusiness
            == other.simpleHistoryCleanupJobAllCompletedSameParentBusiness
        && taskUpdatePriorityJobEnabled == other.taskUpdatePriorityJobEnabled
        && taskUpdatePriorityJobBatchSize == other.taskUpdatePriorityJobBatchSize
        && userInfoRefreshJobEnabled == other.userInfoRefreshJobEnabled
        && addAdditionalUserInfo == other.addAdditionalUserInfo
        && useSpecificDb2Taskquery == other.useSpecificDb2Taskquery
        && Objects.equals(dataSource, other.dataSource)
        && Objects.equals(schemaName, other.schemaName)
        && Objects.equals(domains, other.domains)
        && Objects.equals(roleMap, other.roleMap)
        && Objects.equals(classificationTypes, other.classificationTypes)
        && Objects.equals(classificationCategoriesByType, other.classificationCategoriesByType)
        && Objects.equals(workingTimeSchedule, other.workingTimeSchedule)
        && Objects.equals(workingTimeScheduleTimeZone, other.workingTimeScheduleTimeZone)
        && Objects.equals(customHolidays, other.customHolidays)
        && Objects.equals(logHistoryLoggerName, other.logHistoryLoggerName)
        && jobSchedulerPeriodTimeUnit == other.jobSchedulerPeriodTimeUnit
        && Objects.equals(jobFirstRun, other.jobFirstRun)
        && Objects.equals(jobRunEvery, other.jobRunEvery)
        && Objects.equals(jobLockExpirationPeriod, other.jobLockExpirationPeriod)
        && Objects.equals(taskCleanupJobMinimumAge, other.taskCleanupJobMinimumAge)
        && Objects.equals(
            taskCleanupJobLockExpirationPeriod, other.taskCleanupJobLockExpirationPeriod)
        && Objects.equals(
            workbasketCleanupJobLockExpirationPeriod,
            other.workbasketCleanupJobLockExpirationPeriod)
        && Objects.equals(
            simpleHistoryCleanupJobMinimumAge, other.simpleHistoryCleanupJobMinimumAge)
        && Objects.equals(
            simpleHistoryCleanupJobLockExpirationPeriod,
            other.simpleHistoryCleanupJobLockExpirationPeriod)
        && Objects.equals(taskUpdatePriorityJobFirstRun, other.taskUpdatePriorityJobFirstRun)
        && Objects.equals(taskUpdatePriorityJobRunEvery, other.taskUpdatePriorityJobRunEvery)
        && Objects.equals(
            taskUpdatePriorityJobLockExpirationPeriod,
            other.taskUpdatePriorityJobLockExpirationPeriod)
        && Objects.equals(userRefreshJobFirstRun, other.userRefreshJobFirstRun)
        && Objects.equals(userRefreshJobRunEvery, other.userRefreshJobRunEvery)
        && Objects.equals(
            userRefreshJobLockExpirationPeriod, other.userRefreshJobLockExpirationPeriod)
        && Objects.equals(customJobs, other.customJobs)
        && Objects.equals(
            minimalPermissionsToAssignDomains, other.minimalPermissionsToAssignDomains)
        && Objects.equals(properties, other.properties);
  }

  @Override
  public String toString() {
    return "KadaiConfiguration{"
        + "dataSource="
        + dataSource
        + ", useManagedTransactions="
        + useManagedTransactions
        + ", schemaName='"
        + schemaName
        + '\''
        + ", securityEnabled="
        + securityEnabled
        + ", domains="
        + domains
        + ", enforceServiceLevel="
        + enforceServiceLevel
        + ", includeOwnerWhenRouting="
        + includeOwnerWhenRouting
        + ", roleMap="
        + roleMap
        + ", classificationTypes="
        + classificationTypes
        + ", classificationCategoriesByType="
        + classificationCategoriesByType
        + ", useWorkingTimeCalculation="
        + useWorkingTimeCalculation
        + ", workingTimeSchedule="
        + workingTimeSchedule
        + ", workingTimeScheduleTimeZone="
        + workingTimeScheduleTimeZone
        + ", customHolidays="
        + customHolidays
        + ", germanPublicHolidaysEnabled="
        + germanPublicHolidaysEnabled
        + ", germanPublicHolidaysCorpusChristiEnabled="
        + germanPublicHolidaysCorpusChristiEnabled
        + ", deleteHistoryEventsOnTaskDeletionEnabled="
        + deleteHistoryEventsOnTaskDeletionEnabled
        + ", logHistoryLoggerName='"
        + logHistoryLoggerName
        + '\''
        + ", jobSchedulerEnabled="
        + jobSchedulerEnabled
        + ", jobSchedulerInitialStartDelay="
        + jobSchedulerInitialStartDelay
        + ", jobSchedulerPeriod="
        + jobSchedulerPeriod
        + ", jobSchedulerPeriodTimeUnit="
        + jobSchedulerPeriodTimeUnit
        + ", maxNumberOfJobRetries="
        + maxNumberOfJobRetries
        + ", jobBatchSize="
        + jobBatchSize
        + ", jobFirstRun="
        + jobFirstRun
        + ", jobRunEvery="
        + jobRunEvery
        + ", jobLockExpirationPeriod="
        + jobLockExpirationPeriod
        + ", taskCleanupJobEnabled="
        + taskCleanupJobEnabled
        + ", taskCleanupJobMinimumAge="
        + taskCleanupJobMinimumAge
        + ", taskCleanupJobAllCompletedSameParentBusiness="
        + taskCleanupJobAllCompletedSameParentBusiness
        + ", taskCleanupJobLockExpirationPeriod="
        + taskCleanupJobLockExpirationPeriod
        + ", workbasketCleanupJobEnabled="
        + workbasketCleanupJobEnabled
        + ", workbasketCleanupJobLockExpirationPeriod="
        + workbasketCleanupJobLockExpirationPeriod
        + ", simpleHistoryCleanupJobEnabled="
        + simpleHistoryCleanupJobEnabled
        + ", simpleHistoryCleanupJobBatchSize="
        + simpleHistoryCleanupJobBatchSize
        + ", simpleHistoryCleanupJobMinimumAge="
        + simpleHistoryCleanupJobMinimumAge
        + ", simpleHistoryCleanupJobAllCompletedSameParentBusiness="
        + simpleHistoryCleanupJobAllCompletedSameParentBusiness
        + ", simpleHistoryCleanupJobLockExpirationPeriod="
        + simpleHistoryCleanupJobLockExpirationPeriod
        + ", taskUpdatePriorityJobEnabled="
        + taskUpdatePriorityJobEnabled
        + ", taskUpdatePriorityJobBatchSize="
        + taskUpdatePriorityJobBatchSize
        + ", taskUpdatePriorityJobFirstRun="
        + taskUpdatePriorityJobFirstRun
        + ", taskUpdatePriorityJobRunEvery="
        + taskUpdatePriorityJobRunEvery
        + ", taskUpdatePriorityJobLockExpirationPeriod="
        + taskUpdatePriorityJobLockExpirationPeriod
        + ", userInfoRefreshJobEnabled="
        + userInfoRefreshJobEnabled
        + ", userRefreshJobFirstRun="
        + userRefreshJobFirstRun
        + ", userRefreshJobRunEvery="
        + userRefreshJobRunEvery
        + ", userRefreshJobLockExpirationPeriod="
        + userRefreshJobLockExpirationPeriod
        + ", customJobs="
        + customJobs
        + ", addAdditionalUserInfo="
        + addAdditionalUserInfo
        + ", minimalPermissionsToAssignDomains="
        + minimalPermissionsToAssignDomains
        + ", useSpecificDb2Taskquery="
        + useSpecificDb2Taskquery
        + ", properties="
        + filterOutNonKadaiAndSensitiveProperties()
        + '}';
  }

  // endregion

  public static class Builder {

    private static final Logger LOGGER = LoggerFactory.getLogger(Builder.class);
    private static final String DEFAULT_KADAI_PROPERTIES = "/kadai.properties";

    // region general configuration
    private final DataSource dataSource;
    private final boolean useManagedTransactions;
    private final String schemaName;
    private final boolean securityEnabled;

    private List<String> domains;

    private boolean enforceServiceLevel;

    private boolean includeOwnerWhenRouting;

    // endregion

    // region authentication configuration
    private Map<KadaiRole, Set<String>> roleMap;

    // endregion

    // region classification configuration
    private List<String> classificationTypes;

    private Map<String, List<String>> classificationCategoriesByType;

    // endregion

    // region working time configuration

    private boolean useWorkingTimeCalculation;

    private Map<DayOfWeek, Set<LocalTimeInterval>> workingTimeSchedule;

    private ZoneId workingTimeScheduleTimeZone;

    private Set<CustomHoliday> customHolidays;

    private boolean germanPublicHolidaysEnabled;

    private boolean germanPublicHolidaysCorpusChristiEnabled;

    // endregion

    // region history configuration
    private boolean deleteHistoryEventsOnTaskDeletionEnabled;

    private String logHistoryLoggerName;

    // endregion

    // region job configuration
    private boolean jobSchedulerEnabled;

    private long jobSchedulerInitialStartDelay;

    private long jobSchedulerPeriod;

    private TimeUnit jobSchedulerPeriodTimeUnit;

    private int maxNumberOfJobRetries;

    private int jobBatchSize;

    private Instant jobFirstRun;

    private Duration jobRunEvery;

    private Duration jobLockExpirationPeriod;

    private boolean taskCleanupJobEnabled;

    private Duration taskCleanupJobMinimumAge;

    private boolean taskCleanupJobAllCompletedSameParentBusiness;

    private Duration taskCleanupJobLockExpirationPeriod;

    private boolean workbasketCleanupJobEnabled;

    private Duration workbasketCleanupJobLockExpirationPeriod;

    private boolean simpleHistoryCleanupJobEnabled;

    private int simpleHistoryCleanupJobBatchSize;

    private Duration simpleHistoryCleanupJobMinimumAge;

    private boolean simpleHistoryCleanupJobAllCompletedSameParentBusiness;

    private Duration simpleHistoryCleanupJobLockExpirationPeriod;

    private boolean taskUpdatePriorityJobEnabled;

    private int taskUpdatePriorityJobBatchSize;

    private Instant taskUpdatePriorityJobFirstRun;

    private Duration taskUpdatePriorityJobRunEvery;

    private Duration taskUpdatePriorityJobLockExpirationPeriod;

    private boolean userInfoRefreshJobEnabled;

    private Instant userRefreshJobFirstRun;

    private Duration userRefreshJobRunEvery;

    private Duration userRefreshJobLockExpirationPeriod;

    private Set<String> customJobs;

    // endregion

    // region user configuration
    private boolean addAdditionalUserInfo;

    private Set<WorkbasketPermission> minimalPermissionsToAssignDomains;

    // endregion

    // region database configuration
    private boolean useSpecificDb2Taskquery;

    // endregion

    // region custom configuration
    private Map<String, String> properties = Collections.emptyMap();

    // endregion

    public Builder(DataSource dataSource, boolean useManagedTransactions, String schemaName) {
      this(dataSource, useManagedTransactions, schemaName, true);
    }

    public Builder(
        DataSource dataSource,
        boolean useManagedTransactions,
        String schemaName,
        boolean securityEnabled) {
      this.useManagedTransactions = useManagedTransactions;
      this.securityEnabled = securityEnabled;
      this.dataSource = Objects.requireNonNull(dataSource);
      this.schemaName = initSchemaName(schemaName);
      applyKadaiProperties(new KadaiProperties(), false);
    }

    public Builder(KadaiConfiguration conf) {
      this(
          conf,
          conf.dataSource,
          conf.useManagedTransactions,
          conf.schemaName,
          conf.securityEnabled);
    }

    public Builder(KadaiConfiguration conf, DataSource dataSource) {
      this(conf, dataSource, conf.useManagedTransactions, conf.schemaName, conf.securityEnabled);
    }

    public Builder(KadaiConfiguration conf, DataSource dataSource, boolean useManagedTransactions) {
      this(conf, dataSource, useManagedTransactions, conf.schemaName, conf.securityEnabled);
    }

    public Builder(
        KadaiConfiguration conf,
        DataSource dataSource,
        boolean useManagedTransactions,
        String schemaName) {
      this(conf, dataSource, useManagedTransactions, schemaName, conf.securityEnabled);
    }

    public Builder(
        KadaiConfiguration conf,
        DataSource dataSource,
        boolean useManagedTransactions,
        String schemaName,
        boolean securityEnabled) {
      // general configuration
      this.dataSource = dataSource;
      this.useManagedTransactions = useManagedTransactions;
      this.schemaName = initSchemaName(schemaName);
      this.securityEnabled = securityEnabled;
      this.domains = conf.domains;
      this.enforceServiceLevel = conf.enforceServiceLevel;
      this.includeOwnerWhenRouting = conf.includeOwnerWhenRouting;
      // authentication configuration
      this.roleMap = conf.roleMap;
      // classification configuration
      this.classificationTypes = conf.classificationTypes;
      this.classificationCategoriesByType = conf.classificationCategoriesByType;
      // working time configuration
      this.useWorkingTimeCalculation = conf.useWorkingTimeCalculation;
      this.workingTimeSchedule = conf.workingTimeSchedule;
      this.workingTimeScheduleTimeZone = conf.workingTimeScheduleTimeZone;
      this.customHolidays = conf.customHolidays;
      this.germanPublicHolidaysEnabled = conf.germanPublicHolidaysEnabled;
      this.germanPublicHolidaysCorpusChristiEnabled = conf.germanPublicHolidaysCorpusChristiEnabled;
      // holiday configuration
      this.deleteHistoryEventsOnTaskDeletionEnabled = conf.deleteHistoryEventsOnTaskDeletionEnabled;
      this.logHistoryLoggerName = conf.logHistoryLoggerName;
      // job configuration
      this.jobSchedulerEnabled = conf.jobSchedulerEnabled;
      this.jobSchedulerInitialStartDelay = conf.jobSchedulerInitialStartDelay;
      this.jobSchedulerPeriod = conf.jobSchedulerPeriod;
      this.jobSchedulerPeriodTimeUnit = conf.jobSchedulerPeriodTimeUnit;
      this.maxNumberOfJobRetries = conf.maxNumberOfJobRetries;
      this.jobBatchSize = conf.jobBatchSize;
      this.jobFirstRun = conf.jobFirstRun;
      this.jobRunEvery = conf.jobRunEvery;
      this.jobLockExpirationPeriod = conf.jobLockExpirationPeriod;
      this.taskCleanupJobEnabled = conf.taskCleanupJobEnabled;
      this.taskCleanupJobMinimumAge = conf.taskCleanupJobMinimumAge;
      this.taskCleanupJobAllCompletedSameParentBusiness =
          conf.taskCleanupJobAllCompletedSameParentBusiness;
      this.taskCleanupJobLockExpirationPeriod = conf.taskCleanupJobLockExpirationPeriod;
      this.workbasketCleanupJobEnabled = conf.workbasketCleanupJobEnabled;
      this.workbasketCleanupJobLockExpirationPeriod = conf.workbasketCleanupJobLockExpirationPeriod;
      this.simpleHistoryCleanupJobEnabled = conf.simpleHistoryCleanupJobEnabled;
      this.simpleHistoryCleanupJobBatchSize = conf.simpleHistoryCleanupJobBatchSize;
      this.simpleHistoryCleanupJobMinimumAge = conf.simpleHistoryCleanupJobMinimumAge;
      this.simpleHistoryCleanupJobAllCompletedSameParentBusiness =
          conf.simpleHistoryCleanupJobAllCompletedSameParentBusiness;
      this.simpleHistoryCleanupJobLockExpirationPeriod =
          conf.simpleHistoryCleanupJobLockExpirationPeriod;
      this.taskUpdatePriorityJobEnabled = conf.taskUpdatePriorityJobEnabled;
      this.taskUpdatePriorityJobBatchSize = conf.taskUpdatePriorityJobBatchSize;
      this.taskUpdatePriorityJobFirstRun = conf.taskUpdatePriorityJobFirstRun;
      this.taskUpdatePriorityJobRunEvery = conf.taskUpdatePriorityJobRunEvery;
      this.taskUpdatePriorityJobLockExpirationPeriod =
          conf.taskUpdatePriorityJobLockExpirationPeriod;
      this.userInfoRefreshJobEnabled = conf.userInfoRefreshJobEnabled;
      this.userRefreshJobFirstRun = conf.userRefreshJobFirstRun;
      this.userRefreshJobRunEvery = conf.userRefreshJobRunEvery;
      this.userRefreshJobLockExpirationPeriod = conf.userRefreshJobLockExpirationPeriod;
      this.customJobs = conf.customJobs;
      // user configuration
      this.addAdditionalUserInfo = conf.addAdditionalUserInfo;
      this.minimalPermissionsToAssignDomains = conf.minimalPermissionsToAssignDomains;
      // database configuration
      this.useSpecificDb2Taskquery = conf.useSpecificDb2Taskquery;
      // custom configuration
      this.properties = conf.properties;
    }

    /**
     * Configure the {@linkplain KadaiConfiguration} with the default {@linkplain
     * #DEFAULT_KADAI_PROPERTIES property file location}.
     *
     * @see #initKadaiProperties(String)
     */
    @SuppressWarnings({"unused", "checkstyle:JavadocMethod"})
    public Builder initKadaiProperties() {
      return initKadaiProperties(DEFAULT_KADAI_PROPERTIES);
    }

    /**
     * Configure the {@linkplain KadaiConfiguration} with the default property file notation.
     *
     * @see KadaiProperties
     */
    @SuppressWarnings({"unused", "checkstyle:JavadocMethod"})
    public Builder initKadaiProperties(String propertiesFile) {
      if (propertiesFile == null || propertiesFile.isEmpty() || propertiesFile.isBlank()) {
        throw new SystemException("property file can't be null or empty");
      }

      LOGGER.debug("Reading kadai configuration from {}", propertiesFile);
      kadaiProperties(KadaiProperties.load(propertiesFile));
      return this;
    }

    public Builder kadaiProperties(KadaiProperties kadaiProperties) {
      applyKadaiProperties(kadaiProperties, true);
      return this;
    }

    private void applyKadaiProperties(KadaiProperties kadaiProperties, boolean addMasterDomain) {
      kadaiProperties.validate();
      this.properties = kadaiProperties.getProperties();
      // general configuration
      this.domains = new ArrayList<>(kadaiProperties.getDomains());
      this.enforceServiceLevel = kadaiProperties.getServiceLevel().getValidation().isEnforce();
      this.includeOwnerWhenRouting = kadaiProperties.getRouting().isIncludeOwner();
      // authentication configuration
      this.roleMap = kadaiProperties.getRoles();
      // classification configuration
      this.classificationTypes = kadaiProperties.getClassification().getTypes();
      this.classificationCategoriesByType = kadaiProperties.getClassification().getCategories();
      // working time configuration
      this.useWorkingTimeCalculation =
          kadaiProperties.getWorkingTime().isUseWorkingTimeCalculation();
      this.workingTimeSchedule = kadaiProperties.getWorkingTime().toWorkingTimeSchedule();
      this.workingTimeScheduleTimeZone = kadaiProperties.getWorkingTime().getTimezone();
      this.customHolidays = kadaiProperties.getWorkingTime().getHolidays().toCustomHolidays();
      this.germanPublicHolidaysEnabled =
          kadaiProperties.getWorkingTime().getHolidays().getGerman().isEnabled();
      this.germanPublicHolidaysCorpusChristiEnabled =
          kadaiProperties
              .getWorkingTime()
              .getHolidays()
              .getGerman()
              .getCorpusChristi()
              .isEnabled();
      // history configuration
      this.deleteHistoryEventsOnTaskDeletionEnabled =
          kadaiProperties.getHistory().getSimple().getDeleteOnTaskDeletion().isEnabled();
      this.logHistoryLoggerName = kadaiProperties.getHistory().getLogger().getName();
      // job configuration
      this.jobSchedulerEnabled = kadaiProperties.getJobs().getScheduler().isEnabled();
      this.jobSchedulerInitialStartDelay =
          kadaiProperties.getJobs().getScheduler().getInitialStartDelay();
      this.jobSchedulerPeriod = kadaiProperties.getJobs().getScheduler().getPeriod();
      this.jobSchedulerPeriodTimeUnit =
          kadaiProperties.getJobs().getScheduler().getPeriodTimeUnit();
      this.maxNumberOfJobRetries = kadaiProperties.getJobs().getMaxRetries();
      this.jobBatchSize = kadaiProperties.getJobs().getBatchSize();
      this.jobFirstRun = kadaiProperties.getJobs().getFirstRunAt();
      this.jobRunEvery = kadaiProperties.getJobs().getRunEvery();
      this.jobLockExpirationPeriod = kadaiProperties.getJobs().getLockExpirationPeriod();
      this.taskCleanupJobEnabled = kadaiProperties.getJobs().getCleanup().getTask().isEnable();
      this.taskCleanupJobMinimumAge =
          kadaiProperties.getJobs().getCleanup().getTask().getMinimumAge();
      this.taskCleanupJobAllCompletedSameParentBusiness =
          kadaiProperties.getJobs().getCleanup().getTask().isAllCompletedSameParentBusiness();
      this.taskCleanupJobLockExpirationPeriod =
          kadaiProperties.getJobs().getCleanup().getTask().getLockExpirationPeriod();
      this.workbasketCleanupJobEnabled =
          kadaiProperties.getJobs().getCleanup().getWorkbasket().isEnable();
      this.workbasketCleanupJobLockExpirationPeriod =
          kadaiProperties.getJobs().getCleanup().getWorkbasket().getLockExpirationPeriod();
      this.simpleHistoryCleanupJobEnabled =
          kadaiProperties.getJobs().getCleanup().getHistory().getSimple().isEnable();
      this.simpleHistoryCleanupJobBatchSize =
          kadaiProperties.getJobs().getCleanup().getHistory().getSimple().getBatchSize();
      this.simpleHistoryCleanupJobMinimumAge =
          kadaiProperties.getJobs().getCleanup().getHistory().getSimple().getMinimumAge();
      this.simpleHistoryCleanupJobAllCompletedSameParentBusiness =
          kadaiProperties
              .getJobs()
              .getCleanup()
              .getHistory()
              .getSimple()
              .isAllCompletedSameParentBusiness();
      this.simpleHistoryCleanupJobLockExpirationPeriod =
          kadaiProperties
              .getJobs()
              .getCleanup()
              .getHistory()
              .getSimple()
              .getLockExpirationPeriod();
      this.taskUpdatePriorityJobEnabled =
          kadaiProperties.getJobs().getPriority().getTask().isEnable();
      this.taskUpdatePriorityJobBatchSize =
          kadaiProperties.getJobs().getPriority().getTask().getBatchSize();
      this.taskUpdatePriorityJobFirstRun =
          kadaiProperties.getJobs().getPriority().getTask().getFirstRunAt();
      this.taskUpdatePriorityJobRunEvery =
          kadaiProperties.getJobs().getPriority().getTask().getRunEvery();
      this.taskUpdatePriorityJobLockExpirationPeriod =
          kadaiProperties.getJobs().getPriority().getTask().getLockExpirationPeriod();
      this.userInfoRefreshJobEnabled =
          kadaiProperties.getJobs().getRefresh().getUser().isEnable();
      this.userRefreshJobFirstRun =
          kadaiProperties.getJobs().getRefresh().getUser().getFirstRunAt();
      this.userRefreshJobRunEvery =
          kadaiProperties.getJobs().getRefresh().getUser().getRunEvery();
      this.userRefreshJobLockExpirationPeriod =
          kadaiProperties.getJobs().getRefresh().getUser().getLockExpirationPeriod();
      this.customJobs = kadaiProperties.getJobs().getCustomJobs();
      // user configuration
      this.addAdditionalUserInfo = kadaiProperties.getUser().isAddAdditionalUserInfo();
      this.minimalPermissionsToAssignDomains =
          kadaiProperties.getUser().getMinimalPermissionsToAssignDomains();
      // database configuration
      this.useSpecificDb2Taskquery = kadaiProperties.getFeature().isUseSpecificDb2Taskquery();
      if (addMasterDomain) {
        addMasterDomain();
      }
    }

    // region builder methods

    // region general configuration

    public Builder domains(List<String> domains) {
      this.domains = domains;
      return this;
    }

    public Builder includeOwnerWhenRouting(boolean includeOwnerWhenRouting) {
      this.includeOwnerWhenRouting = includeOwnerWhenRouting;
      return this;
    }

    public Builder enforceServiceLevel(boolean enforceServiceLevel) {
      this.enforceServiceLevel = enforceServiceLevel;
      return this;
    }

    // endregion

    // region authentication configuration

    public Builder roleMap(Map<KadaiRole, Set<String>> roleMap) {
      this.roleMap = roleMap;
      return this;
    }

    // endregion

    // region classification configuration

    public Builder classificationTypes(List<String> classificationTypes) {
      this.classificationTypes = classificationTypes;
      return this;
    }

    public Builder classificationCategoriesByType(
        Map<String, List<String>> classificationCategoriesByType) {
      this.classificationCategoriesByType = classificationCategoriesByType;
      return this;
    }

    // endregion

    // region working time configuration

    public Builder useWorkingTimeCalculation(boolean useWorkingTimeCalculation) {
      this.useWorkingTimeCalculation = useWorkingTimeCalculation;
      return this;
    }

    public Builder workingTimeSchedule(Map<DayOfWeek, Set<LocalTimeInterval>> workingTimeSchedule) {
      this.workingTimeSchedule = workingTimeSchedule;
      return this;
    }

    public Builder workingTimeScheduleTimeZone(ZoneId workingTimeScheduleTimeZone) {
      this.workingTimeScheduleTimeZone = workingTimeScheduleTimeZone;
      return this;
    }

    public Builder customHolidays(Set<CustomHoliday> customHolidays) {
      this.customHolidays = customHolidays;
      return this;
    }

    public Builder germanPublicHolidaysEnabled(boolean germanPublicHolidaysEnabled) {
      this.germanPublicHolidaysEnabled = germanPublicHolidaysEnabled;
      return this;
    }

    public Builder germanPublicHolidaysCorpusChristiEnabled(
        boolean germanPublicHolidaysCorpusChristiEnabled) {
      this.germanPublicHolidaysCorpusChristiEnabled = germanPublicHolidaysCorpusChristiEnabled;
      return this;
    }

    // endregion

    // region history configuration

    public Builder deleteHistoryEventsOnTaskDeletionEnabled(
        boolean deleteHistoryEventsOnTaskDeletionEnabled) {
      this.deleteHistoryEventsOnTaskDeletionEnabled = deleteHistoryEventsOnTaskDeletionEnabled;
      return this;
    }

    public Builder logHistoryLoggerName(String loggerName) {
      this.logHistoryLoggerName = loggerName;
      return this;
    }

    // endregion

    // region job configuration

    public Builder jobSchedulerEnabled(boolean jobSchedulerEnabled) {
      this.jobSchedulerEnabled = jobSchedulerEnabled;
      return this;
    }

    public Builder jobSchedulerInitialStartDelay(long jobSchedulerInitialStartDelay) {
      this.jobSchedulerInitialStartDelay = jobSchedulerInitialStartDelay;
      return this;
    }

    public Builder jobSchedulerPeriod(long jobSchedulerPeriod) {
      this.jobSchedulerPeriod = jobSchedulerPeriod;
      return this;
    }

    public Builder jobSchedulerPeriodTimeUnit(TimeUnit jobSchedulerPeriodTimeUnit) {
      this.jobSchedulerPeriodTimeUnit = jobSchedulerPeriodTimeUnit;
      return this;
    }

    public Builder maxNumberOfJobRetries(int maxNumberOfJobRetries) {
      this.maxNumberOfJobRetries = maxNumberOfJobRetries;
      return this;
    }

    public Builder jobBatchSize(int jobBatchSize) {
      this.jobBatchSize = jobBatchSize;
      return this;
    }

    public Builder jobFirstRun(Instant jobFirstRun) {
      this.jobFirstRun = jobFirstRun;
      return this;
    }

    public Builder jobRunEvery(Duration jobRunEvery) {
      this.jobRunEvery = jobRunEvery;
      return this;
    }

    public Builder taskCleanupJobEnabled(boolean taskCleanupJobEnabled) {
      this.taskCleanupJobEnabled = taskCleanupJobEnabled;
      return this;
    }

    public Builder jobLockExpirationPeriod(Duration jobLockExpirationPeriod) {
      this.jobLockExpirationPeriod = jobLockExpirationPeriod;
      return this;
    }

    public Builder taskCleanupJobMinimumAge(Duration taskCleanupJobMinimumAge) {
      this.taskCleanupJobMinimumAge = taskCleanupJobMinimumAge;
      return this;
    }

    public Builder taskCleanupJobAllCompletedSameParentBusiness(
        boolean taskCleanupJobAllCompletedSameParentBusiness) {
      this.taskCleanupJobAllCompletedSameParentBusiness =
          taskCleanupJobAllCompletedSameParentBusiness;
      return this;
    }

    public Builder taskCleanupJobLockExpirationPeriod(Duration taskCleanupJobLockExpirationPeriod) {
      this.taskCleanupJobLockExpirationPeriod = taskCleanupJobLockExpirationPeriod;
      return this;
    }

    public Builder workbasketCleanupJobEnabled(boolean workbasketCleanupJobEnabled) {
      this.workbasketCleanupJobEnabled = workbasketCleanupJobEnabled;
      return this;
    }

    public Builder workbasketCleanupJobLockExpirationPeriod(
        Duration workbasketCleanupJobLockExpirationPeriod) {
      this.workbasketCleanupJobLockExpirationPeriod = workbasketCleanupJobLockExpirationPeriod;
      return this;
    }

    public Builder simpleHistoryCleanupJobEnabled(boolean simpleHistoryCleanupJobEnabled) {
      this.simpleHistoryCleanupJobEnabled = simpleHistoryCleanupJobEnabled;
      return this;
    }

    public Builder simpleHistoryCleanupJobBatchSize(int simpleHistoryCleanupJobBatchSize) {
      this.simpleHistoryCleanupJobBatchSize = simpleHistoryCleanupJobBatchSize;
      return this;
    }

    public Builder simpleHistoryCleanupJobMinimumAge(Duration simpleHistoryCleanupJobMinimumAge) {
      this.simpleHistoryCleanupJobMinimumAge = simpleHistoryCleanupJobMinimumAge;
      return this;
    }

    public Builder simpleHistoryCleanupJobAllCompletedSameParentBusiness(
        boolean simpleHistoryCleanupJobAllCompletedSameParentBusiness) {
      this.simpleHistoryCleanupJobAllCompletedSameParentBusiness =
          simpleHistoryCleanupJobAllCompletedSameParentBusiness;
      return this;
    }

    public Builder simpleHistoryCleanupJobLockExpirationPeriod(
        Duration simpleHistoryCleanupJobLockExpirationPeriod) {
      this.simpleHistoryCleanupJobLockExpirationPeriod =
          simpleHistoryCleanupJobLockExpirationPeriod;
      return this;
    }

    public Builder taskUpdatePriorityJobEnabled(boolean taskUpdatePriorityJobEnabled) {
      this.taskUpdatePriorityJobEnabled = taskUpdatePriorityJobEnabled;
      return this;
    }

    public Builder taskUpdatePriorityJobBatchSize(int priorityJobBatchSize) {
      this.taskUpdatePriorityJobBatchSize = priorityJobBatchSize;
      return this;
    }

    public Builder taskUpdatePriorityJobFirstRun(Instant taskUpdatePriorityJobFirstRun) {
      this.taskUpdatePriorityJobFirstRun = taskUpdatePriorityJobFirstRun;
      return this;
    }

    public Builder taskUpdatePriorityJobRunEvery(Duration taskUpdatePriorityJobRunEvery) {
      this.taskUpdatePriorityJobRunEvery = taskUpdatePriorityJobRunEvery;
      return this;
    }

    public Builder taskUpdatePriorityJobLockExpirationPeriod(
        Duration taskUpdatePriorityJobLockExpirationPeriod) {
      this.taskUpdatePriorityJobLockExpirationPeriod = taskUpdatePriorityJobLockExpirationPeriod;
      return this;
    }

    public Builder userInfoRefreshJobEnabled(boolean userInfoRefreshJobEnabled) {
      this.userInfoRefreshJobEnabled = userInfoRefreshJobEnabled;
      return this;
    }

    public Builder userRefreshJobFirstRun(Instant userRefreshJobFirstRun) {
      this.userRefreshJobFirstRun = userRefreshJobFirstRun;
      return this;
    }

    public Builder userRefreshJobRunEvery(Duration userRefreshJobRunEvery) {
      this.userRefreshJobRunEvery = userRefreshJobRunEvery;
      return this;
    }

    public Builder userRefreshJobLockExpirationPeriod(Duration userRefreshJobLockExpirationPeriod) {
      this.userRefreshJobLockExpirationPeriod = userRefreshJobLockExpirationPeriod;
      return this;
    }

    public Builder customJobs(Set<String> customJobs) {
      this.customJobs = customJobs;
      return this;
    }

    // endregion

    // region user configuration

    public Builder addAdditionalUserInfo(boolean addAdditionalUserInfo) {
      this.addAdditionalUserInfo = addAdditionalUserInfo;
      return this;
    }

    public Builder minimalPermissionsToAssignDomains(
        Set<WorkbasketPermission> minimalPermissionsToAssignDomains) {
      this.minimalPermissionsToAssignDomains = minimalPermissionsToAssignDomains;
      return this;
    }

    // endregion
    // region database configuration
    public Builder useSpecificDb2Taskquery(boolean useSpecificDb2Taskquery) {
      this.useSpecificDb2Taskquery = useSpecificDb2Taskquery;
      return this;
    }

    public KadaiConfiguration build() {
      adjustConfiguration();
      validateConfiguration();
      return new KadaiConfiguration(this);
    }

    // endregion

    private void addMasterDomain() {
      // Master Domain is treat as empty string
      // it must be always added to the configuration
      // add the master domain always at the end of the list
      if (!this.domains.contains(MASTER_DOMAIN)) {
        this.domains.add(MASTER_DOMAIN);
      }
    }

    private void adjustConfiguration() {
      domains = domains.stream().map(String::toUpperCase).toList();
      classificationTypes = classificationTypes.stream().map(String::toUpperCase).toList();
      classificationCategoriesByType =
          classificationCategoriesByType.entrySet().stream()
              .map(
                  e ->
                      Map.entry(
                          e.getKey().toUpperCase(),
                          e.getValue().stream().map(String::toUpperCase).toList()))
              .sorted(Comparator.comparingInt(e -> classificationTypes.indexOf(e.getKey())))
              .collect(
                  Collectors.toMap(
                      Map.Entry::getKey,
                      Map.Entry::getValue,
                      (oldValue, newValue) -> oldValue,
                      LinkedHashMap::new));
      roleMap =
          Arrays.stream(KadaiRole.values())
              .map(role -> Pair.of(role, roleMap.getOrDefault(role, Set.of())))
              .map(
                  pair ->
                      Pair.of(
                          pair.getLeft(),
                          pair.getRight().stream()
                              .map(String::toLowerCase)
                              .collect(Collectors.toSet())))
              .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
    }

    private void validateConfiguration() {
      if (jobBatchSize <= 0) {
        throw new InvalidArgumentException(
            "Parameter jobBatchSize (kadai.jobs.batchSize) must be a positive integer");
      }
      if (maxNumberOfJobRetries <= 0) {
        throw new InvalidArgumentException(
            "Parameter maxNumberOfJobRetries (kadai.jobs.maxRetries)"
                + " must be a positive integer");
      }
      if (jobRunEvery == null || jobRunEvery.isNegative() || jobRunEvery.isZero()) {
        throw new InvalidArgumentException(
            "Parameter jobRunEvery (kadai.jobs.runEvery) must be a positive duration");
      }
      if (simpleHistoryCleanupJobMinimumAge == null
          || simpleHistoryCleanupJobMinimumAge.isNegative()) {
        throw new InvalidArgumentException(
            "Parameter simpleHistoryCleanupJobMinimumAge "
                + "(kadai.jobs.cleanup.history.simple.minimumAge) must not be negative");
      }
      if (taskCleanupJobMinimumAge == null || taskCleanupJobMinimumAge.isNegative()) {
        throw new InvalidArgumentException(
            "Parameter taskCleanupJobMinimumAge "
                + "(kadai.jobs.cleanup.task.minimumAge) must not be negative");
      }
      if (taskUpdatePriorityJobBatchSize <= 0) {
        throw new InvalidArgumentException(
            "Parameter taskUpdatePriorityJobBatchSize (kadai.jobs.priority.task.batchSize)"
                + " must be a positive integer");
      }
      if (taskUpdatePriorityJobRunEvery == null
          || taskUpdatePriorityJobRunEvery.isNegative()
          || taskUpdatePriorityJobRunEvery.isZero()) {
        throw new InvalidArgumentException(
            "Parameter taskUpdatePriorityJobRunEvery (kadai.jobs.priority.task.runEvery)"
                + " must be a positive duration");
      }
      if (userRefreshJobRunEvery == null
          || userRefreshJobRunEvery.isNegative()
          || userRefreshJobRunEvery.isZero()) {
        throw new InvalidArgumentException(
            "Parameter userRefreshJobRunEvery (kadai.jobs.refresh.user.runEvery)"
                + " must be a positive duration");
      }
      if (jobSchedulerInitialStartDelay < 0) {
        throw new InvalidArgumentException(
            "Parameter jobSchedulerInitialStartDelay (kadai.jobs.scheduler.initialStartDelay)"
                + " must be a natural integer");
      }
      if (jobSchedulerPeriod <= 0) {
        throw new InvalidArgumentException(
            "Parameter jobSchedulerPeriod (kadai.jobs.scheduler.period) "
                + "must be a positive integer");
      }
      if (!new HashSet<>(classificationTypes)
          .containsAll(classificationCategoriesByType.keySet())) {
        throw new InvalidArgumentException(
            "Parameter classificationCategoriesByType (kadai.classification.categories.<KEY>)"
                + " is configured incorrectly. Please check whether all specified"
                + " Classification Types exist in kadai.classification.types .");
      }

      if (!classificationCategoriesByType.keySet().containsAll(classificationTypes)) {
        throw new InvalidArgumentException(
            String.format(
                "Some Classification Categories for parameter classificationTypes "
                    + "(kadai.classification.types) are missing. "
                    + "configured: %s "
                    + "detected: %s",
                classificationTypes, classificationCategoriesByType.keySet()));
      }
    }

    private String initSchemaName(String schemaName) {
      if (schemaName == null || schemaName.isEmpty() || schemaName.isBlank()) {
        throw new SystemException("schema name can't be null or empty");
      }

      try (Connection connection = dataSource.getConnection()) {
        DB db = DB.getDB(connection);
        if (DB.POSTGRES == db) {
          return schemaName.toLowerCase();
        } else {
          return schemaName.toUpperCase();
        }
      } catch (SQLException ex) {
        throw new SystemException(
            "Caught exception when attempting to initialize the schema name", ex);
      }
    }

  }
}
