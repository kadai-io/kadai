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

package io.kadai.testapi.generator;

import io.kadai.KadaiConfiguration;
import io.kadai.classification.api.ClassificationCustomField;
import io.kadai.classification.api.ClassificationQuery;
import io.kadai.classification.api.ClassificationService;
import io.kadai.classification.api.exceptions.ClassificationAlreadyExistException;
import io.kadai.classification.api.exceptions.MalformedServiceLevelException;
import io.kadai.classification.api.models.Classification;
import io.kadai.classification.api.models.ClassificationSummary;
import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.KadaiRole;
import io.kadai.common.api.WorkingTimeCalculator;
import io.kadai.common.api.exceptions.DomainNotFoundException;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.api.exceptions.NotAuthorizedException;
import io.kadai.common.internal.util.IdGenerator;
import io.kadai.task.api.CallbackState;
import io.kadai.task.api.TaskCustomField;
import io.kadai.task.api.TaskCustomIntField;
import io.kadai.task.api.TaskState;
import io.kadai.task.api.models.ObjectReference;
import io.kadai.task.api.models.Task;
import io.kadai.task.internal.models.ObjectReferenceImpl;
import io.kadai.task.internal.models.TaskImpl;
import io.kadai.testapi.generator.exceptions.TestDataGenerationException;
import io.kadai.workbasket.api.WorkbasketCustomField;
import io.kadai.workbasket.api.WorkbasketQuery;
import io.kadai.workbasket.api.WorkbasketService;
import io.kadai.workbasket.api.WorkbasketType;
import io.kadai.workbasket.api.exceptions.WorkbasketAlreadyExistException;
import io.kadai.workbasket.api.models.Workbasket;
import io.kadai.workbasket.api.models.WorkbasketSummary;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SplittableRandom;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import org.json.JSONObject;
import org.postgresql.PGConnection;
import org.postgresql.copy.CopyManager;

/**
 * Environment-aware generator for realistic KADAI task test data.
 *
 * <p>The generator discovers or bootstraps classifications and workbaskets that are compatible with
 * the active {@link KadaiConfiguration}. Generated tasks are streamed or emitted in batches to keep
 * the memory footprint bounded even for very large data sets.
 */
@SuppressWarnings({
  "checkstyle:JavadocMethod",
  "checkstyle:CatchParameterName",
  "checkstyle:LambdaParameterName"
})
public final class TaskTestDataGenerator {

  private static final long DEFAULT_SEED = 42_4242_1337L;
  private static final int GENERATED_CLASSIFICATIONS_PER_CATEGORY = 2;
  private static final int GENERATED_PERSONAL_WORKBASKETS_PER_DOMAIN = 2;
  private static final String GENERATED_KEY_PREFIX = "TDG";
  private static final String GENERATED_DESCRIPTION_SUFFIX = " - automatisch generierte Testdaten";
  private static final List<String> EXTERNAL_CLASSIFICATION_NAMES =
      List.of(
          "Widerruf",
          "Schadenfall",
          "Datenschutzerklärung",
          "Bezugsberechtigungen",
          "Vertragslaufzeit",
          "Zustimmungserklärung",
          "Monitoring",
          "Dynamikänderung",
          "Gewährung-Policendarlehen");
  private static final List<String> MANUAL_CLASSIFICATION_NAMES =
      List.of(
          "Vertragstermin",
          "Kündigungsfristen",
          "Kündigungsrechte",
          "Versicherungsbedingungen",
          "Rechtsschutz",
          "Beratungsprotokoll");
  private static final List<String> AUTOMATIC_CLASSIFICATION_NAMES =
      List.of(
          "Monitoring-Protokoll",
          "Honorarrechnung erstellen",
          "Dynamik-Ablehnung",
          "Dynamik-Ausschluss");
  private static final List<String> PROCESS_CLASSIFICATION_NAMES =
      List.of("Online-Marketing", "Mindestprämie", "Strukturierung", "Service-Prozess");
  private static final List<String> FALLBACK_CLASSIFICATION_NAMES =
      List.of("Vorgang", "Fallbearbeitung", "Anliegen", "Geschäftsvorfall");
  private static final List<String> WORKBASKET_TOPIC_NAMES =
      List.of("VIP", "Vertragsservice", "Leistungsfall", "Digitalisierung", "ServiceCenter");
  private static final List<String> COMPANIES =
      List.of(
          "MyCompany1",
          "MyCompany2",
          "Versicherung Nord",
          "Versicherung Süd",
          "Service Center GmbH");
  private static final List<String> SYSTEMS =
      List.of("PASystem", "CRMSystem", "PartnerPortal", "BestandsSystem", "InputManagement");
  private static final List<String> SYSTEM_INSTANCES = List.of("00", "01", "02", "03", "05", "06");
  private static final List<String> OBJECT_TYPES = List.of("VNR", "SDNR", "RVNR", "KOLVNR", "ANR");
  private static final List<String> TEXT_FRAGMENTS =
      List.of(
          "Unterlagen prüfen",
          "Kundenvorgang abschließen",
          "Rückfrage an Fachbereich",
          "Frist beachten",
          "Dokumentation aktualisieren",
          "Vorgang plausibilisieren");
  private static final EnumMap<WorkbasketType, Double> WORKBASKET_TYPE_WEIGHTS =
      createWorkbasketTypeWeights();
  private static final int COPY_BUFFER_SIZE_PER_TASK = 2_048;
  private static final int TASK_TABLE_COLUMN_COUNT = 61;
  private static final DateTimeFormatter POSTGRES_TIMESTAMP_FORMATTER =
      DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSS", Locale.ROOT);
  private static final String TASK_TABLE_COLUMNS =
      "ID, EXTERNAL_ID, CREATED, CLAIMED, COMPLETED, MODIFIED, PLANNED, RECEIVED, DUE, "
          + "NAME, CREATOR, DESCRIPTION, NOTE, PRIORITY, MANUAL_PRIORITY, STATE, "
          + "CLASSIFICATION_CATEGORY, CLASSIFICATION_KEY, CLASSIFICATION_ID, WORKBASKET_ID, "
          + "WORKBASKET_KEY, DOMAIN, BUSINESS_PROCESS_ID, PARENT_BUSINESS_PROCESS_ID, OWNER, "
          + "POR_COMPANY, POR_SYSTEM, POR_INSTANCE, POR_TYPE, POR_VALUE, IS_READ, "
          + "IS_TRANSFERRED, IS_REOPENED, CALLBACK_INFO, CALLBACK_STATE, CUSTOM_ATTRIBUTES, "
          + "CUSTOM_1, CUSTOM_2, CUSTOM_3, CUSTOM_4, CUSTOM_5, CUSTOM_6, CUSTOM_7, CUSTOM_8, "
          + "CUSTOM_9, CUSTOM_10, CUSTOM_11, CUSTOM_12, CUSTOM_13, CUSTOM_14, CUSTOM_15, "
          + "CUSTOM_16, CUSTOM_INT_1, CUSTOM_INT_2, CUSTOM_INT_3, CUSTOM_INT_4, CUSTOM_INT_5, "
          + "CUSTOM_INT_6, CUSTOM_INT_7, CUSTOM_INT_8, NUMBER_OF_COMMENTS";
  private static final String TASK_INSERT_SQL =
      "INSERT INTO TASK("
          + TASK_TABLE_COLUMNS
          + ") VALUES ("
          + createPlaceholderList(TASK_TABLE_COLUMN_COUNT)
          + ")";
  private static final String TASK_POSTGRES_COPY_SQL =
      "COPY TASK (" + TASK_TABLE_COLUMNS + ") " + "FROM STDIN WITH (FORMAT text)";

  private final TaskGenerationEnvironment environment;
  private final Clock clock;
  private final long seed;
  private final KadaiEngine kadaiEngine;
  private final WorkingTimeCalculator workingTimeCalculator;
  private final Map<String, DomainFixtures> fixturesByDomain;

  /** Creates a generator from a previously resolved environment. */
  public TaskTestDataGenerator(TaskGenerationEnvironment environment) {
    this(environment, Clock.systemUTC(), DEFAULT_SEED, null, null);
  }

  /** Creates a generator from a previously resolved environment using a custom clock and seed. */
  public TaskTestDataGenerator(TaskGenerationEnvironment environment, Clock clock, long seed) {
    this(environment, clock, seed, null, null);
  }

  private TaskTestDataGenerator(
      TaskGenerationEnvironment environment,
      Clock clock,
      long seed,
      KadaiEngine kadaiEngine,
      WorkingTimeCalculator workingTimeCalculator) {
    this.environment = sanitizeEnvironment(environment);
    this.clock = Objects.requireNonNull(clock, "clock must not be null");
    this.seed = seed;
    this.kadaiEngine = kadaiEngine;
    this.workingTimeCalculator = workingTimeCalculator;
    this.fixturesByDomain = buildDomainFixtures(this.environment);
  }

  /**
   * Creates a generator for the given engine and bootstraps missing compatible support data as
   * admin.
   */
  public static TaskTestDataGenerator from(KadaiEngine kadaiEngine) {
    Objects.requireNonNull(kadaiEngine, "kadaiEngine must not be null");
    try {
      TaskGenerationEnvironment environment =
          kadaiEngine.runAsAdmin(
              () ->
                  resolveEnvironment(
                      kadaiEngine.getConfiguration(),
                      kadaiEngine.getClassificationService(),
                      kadaiEngine.getWorkbasketService()));
      return new TaskTestDataGenerator(
          environment,
          Clock.systemUTC(),
          DEFAULT_SEED,
          kadaiEngine,
          kadaiEngine.getWorkingTimeCalculator());
    } catch (RuntimeException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new TestDataGenerationException("Failed to initialize task test data generator", ex);
    }
  }

  /**
   * Resolves a configuration-compatible generation environment and bootstraps missing support data.
   */
  public static TaskGenerationEnvironment resolveEnvironment(
      KadaiConfiguration configuration,
      ClassificationService classificationService,
      WorkbasketService workbasketService) {
    Objects.requireNonNull(configuration, "configuration must not be null");
    Objects.requireNonNull(classificationService, "classificationService must not be null");
    Objects.requireNonNull(workbasketService, "workbasketService must not be null");

    try {
      List<String> domains = normalizeConfiguredDomains(configuration.getDomains());
      if (domains.isEmpty()) {
        throw new TestDataGenerationException(
            "The active KADAI configuration does not define any domains");
      }

      String taskClassificationType = determineTaskClassificationType(configuration);
      List<String> taskCategories = determineTaskCategories(configuration, taskClassificationType);
      Map<String, Set<String>> existingClassificationKeysByDomain = new HashMap<>();
      List<ClassificationSummary> classifications =
          new ArrayList<>(
              loadCompatibleClassifications(
                  classificationService, domains, taskClassificationType, taskCategories));
      classifications.forEach(
          classification ->
              existingClassificationKeysByDomain
                  .computeIfAbsent(classification.getDomain(), _ -> new LinkedHashSet<>())
                  .add(classification.getKey().toLowerCase(Locale.ROOT)));

      for (String domain : domains) {
        for (String category : taskCategories) {
          long compatibleCount =
              classifications.stream()
                  .filter(classification -> domain.equals(classification.getDomain()))
                  .filter(
                      classification ->
                          classification.getCategory() != null
                              && classification.getCategory().equalsIgnoreCase(category))
                  .count();
          for (int i = (int) compatibleCount; i < GENERATED_CLASSIFICATIONS_PER_CATEGORY; i++) {
            classifications.add(
                createClassification(
                    classificationService,
                    domain,
                    taskClassificationType,
                    category,
                    i,
                    existingClassificationKeysByDomain));
          }
        }
      }

      Map<String, Set<String>> existingWorkbasketKeysByDomain = new HashMap<>();
      List<WorkbasketSummary> workbaskets =
          new ArrayList<>(loadCompatibleWorkbaskets(workbasketService, domains));
      workbaskets.forEach(
          workbasket ->
              existingWorkbasketKeysByDomain
                  .computeIfAbsent(workbasket.getDomain(), _ -> new LinkedHashSet<>())
                  .add(workbasket.getKey().toLowerCase(Locale.ROOT)));

      List<String> candidateUsers = extractCandidateUsers(configuration, workbaskets);

      for (String domain : domains) {
        workbaskets.addAll(
            ensureWorkbasketsForDomain(
                workbasketService,
                domain,
                candidateUsers,
                workbaskets,
                existingWorkbasketKeysByDomain));
      }

      candidateUsers = extractCandidateUsers(configuration, workbaskets);
      return new TaskGenerationEnvironment(
          domains,
          candidateUsers,
          taskClassificationType,
          taskCategories,
          sortClassifications(classifications),
          sortWorkbaskets(workbaskets));
    } catch (RuntimeException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new TestDataGenerationException(
          "Failed to resolve a compatible task test environment", ex);
    }
  }

  /** Returns the resolved generation environment. */
  public TaskGenerationEnvironment getEnvironment() {
    return environment;
  }

  /** Returns a lazily generated stream of tasks. */
  public Stream<Task> stream(long numberOfTasks) {
    validateTaskCount(numberOfTasks);
    return LongStream.range(0, numberOfTasks).mapToObj(this::generateTask);
  }

  /**
   * Generates the requested number of tasks and emits them in bounded batches.
   *
   * @param numberOfTasks the number of tasks to generate
   * @param batchSize the maximum number of tasks per emitted batch
   * @param batchConsumer consumer that handles one generated batch at a time
   * @return summary of the generation run
   */
  public GenerationSummary generate(
      long numberOfTasks, int batchSize, TaskBatchConsumer batchConsumer) {
    validateTaskCount(numberOfTasks);
    validateBatchSize(batchSize);
    Objects.requireNonNull(batchConsumer, "batchConsumer must not be null");

    if (numberOfTasks == 0) {
      return new GenerationSummary(
          0, 0, 0, environment.classifications().size(), environment.workbaskets().size());
    }

    List<Task> batch = new ArrayList<>((int) Math.min(numberOfTasks, batchSize));
    long emittedTaskCount = 0;
    long emittedBatchCount = 0;

    for (long index = 0; index < numberOfTasks; index++) {
      batch.add(generateTask(index));
      if (batch.size() == batchSize) {
        emitBatch(batchConsumer, batch);
        batch.clear();
        emittedBatchCount++;
      }
      emittedTaskCount++;
    }

    if (!batch.isEmpty()) {
      emitBatch(batchConsumer, batch);
      emittedBatchCount++;
    }

    return new GenerationSummary(
        numberOfTasks,
        emittedTaskCount,
        emittedBatchCount,
        environment.classifications().size(),
        environment.workbaskets().size());
  }

  /**
   * Persists generated tasks with a direct high-throughput JDBC path.
   *
   * <p>For PostgreSQL the generator prefers {@code COPY FROM STDIN} and disables synchronous commit
   * for the transaction. Other databases use a prepared-statement batch insert fallback.
   */
  public GenerationSummary persist(long numberOfTasks, int batchSize) {
    if (kadaiEngine == null) {
      throw new TestDataGenerationException(
          "Persisting tasks requires a generator created via TaskTestDataGenerator.from(...)"
              + " so that a KadaiEngine is available");
    }

    validateTaskCount(numberOfTasks);
    validateBatchSize(batchSize);

    if (numberOfTasks == 0) {
      return new GenerationSummary(
          0, 0, 0, environment.classifications().size(), environment.workbaskets().size());
    }

    Connection connection = null;
    try {
      connection = kadaiEngine.getConfiguration().getDataSource().getConnection();
      connection.setAutoCommit(false);
      setSchema(connection, kadaiEngine.getConfiguration().getSchemaName());

      boolean postgreSql = isPostgreSql(connection);
      CopyManager copyManager = postgreSql ? getPostgreSqlCopyManager(connection) : null;
      if (postgreSql) {
        tunePostgreSqlSession(connection);
      }

      GenerationSummary summary =
          copyManager != null
              ? persistWithPostgreSqlCopy(numberOfTasks, batchSize, copyManager)
              : persistWithJdbcBatch(numberOfTasks, batchSize, connection);
      connection.commit();
      return summary;
    } catch (Exception ex) {
      rollbackQuietly(connection);
      throw new TestDataGenerationException("Task persistence failed", ex);
    } finally {
      closeQuietly(connection);
    }
  }

  @SuppressWarnings("checkstyle:VariableDeclarationUsageDistance")
  Task generateTask(long index) {
    SplittableRandom random = new SplittableRandom(mixSeed(seed, index));
    Instant now = clock.instant();
    String domain = environment.domains().get((int) (index % environment.domains().size()));
    DomainFixtures fixtures = fixturesByDomain.get(domain);
    ClassificationSummary classification = pickClassification(fixtures, random);
    WorkbasketSummary workbasket = pickWorkbasket(fixtures, random);

    TaskState state = pickTaskState(random);
    String creator = pickCreator(random);
    long correlationGroup = index / 3;
    Instant received =
        random.nextDouble() < 0.15
            ? null
            : now.minus(Duration.ofHours(random.nextLong(24, 24L * 180)));
    Instant created =
        received == null
            ? now.minus(Duration.ofHours(random.nextLong(12, 24L * 120)))
            : received.plus(Duration.ofHours(random.nextLong(0, 48)));
    Instant planned = created.plus(Duration.ofHours(random.nextLong(1, 36)));
    Instant due = computeDue(planned, classification.getServiceLevel());
    String owner = determineOwner(state, workbasket, random);
    Instant claimed = null;
    Instant completed = null;
    Instant modified;

    if (state == TaskState.CLAIMED
        || state == TaskState.READY_FOR_REVIEW
        || state == TaskState.IN_REVIEW
        || state == TaskState.COMPLETED) {
      claimed = created.plus(Duration.ofHours(random.nextLong(1, 18)));
    }

    if (state.isEndState()) {
      Instant completionBase = claimed == null ? planned : claimed;
      completed = completionBase.plus(Duration.ofHours(random.nextLong(1, 48)));
    }

    modified =
        Stream.of(completed, claimed, due, planned, created)
            .filter(Objects::nonNull)
            .max(Comparator.naturalOrder())
            .orElse(created);

    if (!state.isEndState() && modified.isAfter(now)) {
      modified = now.minus(Duration.ofMinutes(random.nextLong(1, 45)));
    }

    String taskName = deriveTaskName(classification, random);
    String taskDescription = deriveTaskDescription(classification, creator, random);
    String externalId = formatIdentifier("ETI", index);
    String businessProcessId = formatIdentifier("BPI", index);
    String parentBusinessProcessId = formatIdentifier("PBPI", correlationGroup);
    String referenceValue = eightDigits(correlationGroup + random.nextLong(10, 9999));
    ObjectReference primaryObjRef =
        new ObjectReferenceImpl(
            pick(COMPANIES, random),
            pick(SYSTEMS, random),
            pick(SYSTEM_INSTANCES, random),
            pick(OBJECT_TYPES, random),
            referenceValue);

    GeneratedTaskImpl task = new GeneratedTaskImpl();
    task.setExternalId(externalId);
    task.setClassificationSummary(classification);
    task.setWorkbasketSummary(workbasket);
    task.setPrimaryObjRef(primaryObjRef);
    task.setCreatedIgnoreFreeze(created);
    task.freezeCreated();
    task.setModifiedIgnoreFreeze(modified);
    task.freezeModified();
    task.setReceived(received);
    task.setPlanned(planned);
    task.setDue(due);
    task.setClaimed(claimed);
    task.setCompleted(completed);
    task.setStateIgnoreFreeze(state);
    task.freezeState();
    task.setReadIgnoreFreeze(isRead(state, random));
    task.freezeRead();
    task.setTransferredIgnoreFreeze(random.nextDouble() < 0.08);
    task.freezeTransferred();
    task.setReopenedIgnoreFreeze(state.isEndState() && random.nextDouble() < 0.03);
    task.freezeReopened();
    task.setName(taskName);
    task.setDescription(taskDescription);
    task.setNote(random.nextDouble() < 0.32 ? pick(TEXT_FRAGMENTS, random) : null);
    task.setBusinessProcessId(businessProcessId);
    task.setParentBusinessProcessId(parentBusinessProcessId);
    task.setCreator(creator);
    task.setOwner(owner);
    task.setNumberOfComments(determineNumberOfComments(state, random));

    int manualPriority = determineManualPriority(classification.getPriority(), random);
    task.setManualPriority(manualPriority);
    int effectivePriority = manualPriority >= 0 ? manualPriority : classification.getPriority();
    task.setPriorityIgnoreFreeze(effectivePriority);
    task.freezePriority();

    task.setCallbackState(determineCallbackState(state, random));
    if (task.getCallbackState() != CallbackState.NONE) {
      task.setCallbackInfo(
          Map.of(
              Task.CALLBACK_STATE,
              task.getCallbackState().name(),
              "correlationId",
              formatIdentifier("CBI", correlationGroup)));
    }

    enrichCustomFields(task, classification, workbasket, referenceValue, random);
    return task;
  }

  private GenerationSummary persistWithPostgreSqlCopy(
      long numberOfTasks, int batchSize, CopyManager copyManager) throws SQLException, IOException {
    StringBuilder buffer = new StringBuilder(estimateCopyBufferCapacity(batchSize));
    long emittedTaskCount = 0;
    long emittedBatchCount = 0;

    for (long index = 0; index < numberOfTasks; index++) {
      appendTaskAsPostgreSqlCopyRow(buffer, prepareTaskForDirectPersistence(generateTask(index)));
      emittedTaskCount++;
      if (emittedTaskCount % batchSize == 0) {
        copyBatch(copyManager, buffer);
        emittedBatchCount++;
        buffer.setLength(0);
      }
    }

    if (!buffer.isEmpty()) {
      copyBatch(copyManager, buffer);
      emittedBatchCount++;
    }

    return new GenerationSummary(
        numberOfTasks,
        emittedTaskCount,
        emittedBatchCount,
        environment.classifications().size(),
        environment.workbaskets().size());
  }

  private GenerationSummary persistWithJdbcBatch(
      long numberOfTasks, int batchSize, Connection connection) throws SQLException {
    long emittedTaskCount = 0;
    long emittedBatchCount = 0;

    try (PreparedStatement statement = connection.prepareStatement(TASK_INSERT_SQL)) {
      for (long index = 0; index < numberOfTasks; index++) {
        bindTask(statement, prepareTaskForDirectPersistence(generateTask(index)));
        statement.addBatch();
        emittedTaskCount++;
        if (emittedTaskCount % batchSize == 0) {
          statement.executeLargeBatch();
          emittedBatchCount++;
        }
      }

      if (emittedTaskCount % batchSize != 0) {
        statement.executeLargeBatch();
        emittedBatchCount++;
      }
    }

    return new GenerationSummary(
        numberOfTasks,
        emittedTaskCount,
        emittedBatchCount,
        environment.classifications().size(),
        environment.workbaskets().size());
  }

  private TaskImpl prepareTaskForDirectPersistence(Task task) {
    TaskImpl taskImpl = (TaskImpl) task;
    if (taskImpl.getId() == null || taskImpl.getId().isBlank()) {
      taskImpl.setId(IdGenerator.generateWithPrefix(IdGenerator.ID_PREFIX_TASK));
    }
    if (taskImpl.getExternalId() == null || taskImpl.getExternalId().isBlank()) {
      taskImpl.setExternalId(IdGenerator.generateWithPrefix(IdGenerator.ID_PREFIX_EXT_TASK));
    }
    return taskImpl;
  }

  private static void bindTask(PreparedStatement statement, TaskImpl task) throws SQLException {
    int index = 1;
    statement.setString(index++, task.getId());
    statement.setString(index++, task.getExternalId());
    setTimestamp(statement, index++, task.getCreated());
    setTimestamp(statement, index++, task.getClaimed());
    setTimestamp(statement, index++, task.getCompleted());
    setTimestamp(statement, index++, task.getModified());
    setTimestamp(statement, index++, task.getPlanned());
    setTimestamp(statement, index++, task.getReceived());
    setTimestamp(statement, index++, task.getDue());
    statement.setString(index++, task.getName());
    statement.setString(index++, task.getCreator());
    statement.setString(index++, task.getDescription());
    statement.setString(index++, task.getNote());
    setInteger(statement, index++, task.getPriority());
    setInteger(statement, index++, task.getManualPriority());
    statement.setString(index++, task.getState() == null ? null : task.getState().name());
    statement.setString(index++, task.getClassificationSummary().getCategory());
    statement.setString(index++, task.getClassificationSummary().getKey());
    statement.setString(index++, task.getClassificationSummary().getId());
    statement.setString(index++, task.getWorkbasketSummary().getId());
    statement.setString(index++, task.getWorkbasketSummary().getKey());
    statement.setString(index++, task.getWorkbasketSummary().getDomain());
    statement.setString(index++, task.getBusinessProcessId());
    statement.setString(index++, task.getParentBusinessProcessId());
    statement.setString(index++, task.getOwner());
    statement.setString(index++, task.getPrimaryObjRef().getCompany());
    statement.setString(index++, task.getPrimaryObjRef().getSystem());
    statement.setString(index++, task.getPrimaryObjRef().getSystemInstance());
    statement.setString(index++, task.getPrimaryObjRef().getType());
    statement.setString(index++, task.getPrimaryObjRef().getValue());
    statement.setBoolean(index++, task.isRead());
    statement.setBoolean(index++, task.isTransferred());
    statement.setBoolean(index++, task.isReopened());
    setJsonMap(statement, index++, task.getCallbackInfo());
    statement.setString(
        index++, task.getCallbackState() == null ? null : task.getCallbackState().name());
    setJsonMap(statement, index++, task.getCustomAttributes());
    statement.setString(index++, task.getCustom1());
    statement.setString(index++, task.getCustom2());
    statement.setString(index++, task.getCustom3());
    statement.setString(index++, task.getCustom4());
    statement.setString(index++, task.getCustom5());
    statement.setString(index++, task.getCustom6());
    statement.setString(index++, task.getCustom7());
    statement.setString(index++, task.getCustom8());
    statement.setString(index++, task.getCustom9());
    statement.setString(index++, task.getCustom10());
    statement.setString(index++, task.getCustom11());
    statement.setString(index++, task.getCustom12());
    statement.setString(index++, task.getCustom13());
    statement.setString(index++, task.getCustom14());
    statement.setString(index++, task.getCustom15());
    statement.setString(index++, task.getCustom16());
    setInteger(statement, index++, task.getCustomInt1());
    setInteger(statement, index++, task.getCustomInt2());
    setInteger(statement, index++, task.getCustomInt3());
    setInteger(statement, index++, task.getCustomInt4());
    setInteger(statement, index++, task.getCustomInt5());
    setInteger(statement, index++, task.getCustomInt6());
    setInteger(statement, index++, task.getCustomInt7());
    setInteger(statement, index++, task.getCustomInt8());
    setInteger(statement, index, task.getNumberOfComments());
  }

  private static void appendTaskAsPostgreSqlCopyRow(StringBuilder buffer, TaskImpl task) {
    appendPostgreSqlCopyValue(buffer, task.getId());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getExternalId());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getCreated());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getClaimed());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getCompleted());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getModified());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getPlanned());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getReceived());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getDue());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getName());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getCreator());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getDescription());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getNote());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getPriority());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getManualPriority());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getState());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getClassificationSummary().getCategory());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getClassificationSummary().getKey());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getClassificationSummary().getId());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getWorkbasketSummary().getId());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getWorkbasketSummary().getKey());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getWorkbasketSummary().getDomain());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getBusinessProcessId());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getParentBusinessProcessId());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getOwner());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getPrimaryObjRef().getCompany());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getPrimaryObjRef().getSystem());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getPrimaryObjRef().getSystemInstance());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getPrimaryObjRef().getType());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getPrimaryObjRef().getValue());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.isRead());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.isTransferred());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.isReopened());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, mapToJson(task.getCallbackInfo()));
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getCallbackState());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, mapToJson(task.getCustomAttributes()));
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getCustom1());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getCustom2());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getCustom3());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getCustom4());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getCustom5());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getCustom6());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getCustom7());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getCustom8());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getCustom9());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getCustom10());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getCustom11());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getCustom12());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getCustom13());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getCustom14());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getCustom15());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getCustom16());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getCustomInt1());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getCustomInt2());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getCustomInt3());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getCustomInt4());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getCustomInt5());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getCustomInt6());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getCustomInt7());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getCustomInt8());
    appendPostgreSqlCopySeparator(buffer);
    appendPostgreSqlCopyValue(buffer, task.getNumberOfComments());
    buffer.append('\n');
  }

  private static void appendPostgreSqlCopySeparator(StringBuilder buffer) {
    buffer.append('\t');
  }

  private static void appendPostgreSqlCopyValue(StringBuilder buffer, Object value) {
    if (value == null) {
      buffer.append("\\N");
      return;
    }
    if (value instanceof Instant instant) {
      buffer.append(
          POSTGRES_TIMESTAMP_FORMATTER.format(LocalDateTime.ofInstant(instant, ZoneOffset.UTC)));
      return;
    }
    if (value instanceof Boolean booleanValue) {
      buffer.append(booleanValue ? 't' : 'f');
      return;
    }
    if (value instanceof Enum<?> enumValue) {
      escapePostgreSqlCopyText(buffer, enumValue.name());
      return;
    }
    escapePostgreSqlCopyText(buffer, String.valueOf(value));
  }

  private static void escapePostgreSqlCopyText(StringBuilder buffer, String value) {
    for (int i = 0; i < value.length(); i++) {
      char current = value.charAt(i);
      switch (current) {
        case '\\' -> buffer.append("\\\\");
        case '\t' -> buffer.append("\\t");
        case '\n' -> buffer.append("\\n");
        case '\r' -> buffer.append("\\r");
        default -> buffer.append(current);
      }
    }
  }

  private static void copyBatch(CopyManager copyManager, StringBuilder buffer)
      throws SQLException, IOException {
    byte[] payload = buffer.toString().getBytes(StandardCharsets.UTF_8);
    try (ByteArrayInputStream inputStream = new ByteArrayInputStream(payload)) {
      copyManager.copyIn(TASK_POSTGRES_COPY_SQL, inputStream);
    }
  }

  private static String mapToJson(Map<String, ?> values) {
    if (values == null || values.isEmpty()) {
      return null;
    }
    return new JSONObject(values).toString();
  }

  private static void setJsonMap(PreparedStatement statement, int index, Map<String, ?> values)
      throws SQLException {
    String json = mapToJson(values);
    if (json == null) {
      statement.setNull(index, Types.CLOB);
    } else {
      statement.setString(index, json);
    }
  }

  private static void setInteger(PreparedStatement statement, int index, Integer value)
      throws SQLException {
    if (value == null) {
      statement.setNull(index, Types.INTEGER);
    } else {
      statement.setInt(index, value);
    }
  }

  private static void setTimestamp(PreparedStatement statement, int index, Instant value)
      throws SQLException {
    if (value == null) {
      statement.setNull(index, Types.TIMESTAMP);
      return;
    }
    Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    statement.setTimestamp(index, Timestamp.from(value), utcCalendar);
  }

  private static boolean isPostgreSql(Connection connection) throws SQLException {
    return connection
        .getMetaData()
        .getDatabaseProductName()
        .toLowerCase(Locale.ROOT)
        .contains("postgresql");
  }

  private static CopyManager getPostgreSqlCopyManager(Connection connection) {
    try {
      PGConnection pgConnection = connection.unwrap(PGConnection.class);
      return pgConnection.getCopyAPI();
    } catch (SQLException | RuntimeException _) {
      return null;
    }
  }

  private static void tunePostgreSqlSession(Connection connection) throws SQLException {
    try (Statement statement = connection.createStatement()) {
      statement.execute("SET LOCAL synchronous_commit = OFF");
    }
  }

  private static int estimateCopyBufferCapacity(int batchSize) {
    long cappedBatchSize = Math.max(1, Math.min(batchSize, 512));
    long estimatedCapacity = cappedBatchSize * COPY_BUFFER_SIZE_PER_TASK;
    return (int) Math.min(estimatedCapacity, Integer.MAX_VALUE);
  }

  private static void setSchema(Connection connection, String schemaName) throws SQLException {
    if (schemaName != null && !schemaName.isBlank()) {
      connection.setSchema(schemaName);
    }
  }

  private static void rollbackQuietly(Connection connection) {
    if (connection == null) {
      return;
    }
    try {
      connection.rollback();
    } catch (SQLException _) {
      // ignore rollback failures and preserve the original exception
    }
  }

  private static void closeQuietly(Connection connection) {
    if (connection == null) {
      return;
    }
    try {
      connection.close();
    } catch (SQLException _) {
      // ignore close failures and preserve the original exception
    }
  }

  private static TaskGenerationEnvironment sanitizeEnvironment(
      TaskGenerationEnvironment environment) {
    Objects.requireNonNull(environment, "environment must not be null");
    List<String> domains = normalizeConfiguredDomains(environment.domains());
    if (domains.isEmpty()) {
      throw new IllegalArgumentException("environment.domains must not be empty");
    }

    String taskClassificationType =
        environment.taskClassificationType() == null
                || environment.taskClassificationType().isBlank()
            ? "TASK"
            : environment.taskClassificationType();
    List<String> taskCategories =
        environment.taskCategories() == null || environment.taskCategories().isEmpty()
            ? List.of("EXTERNAL")
            : List.copyOf(environment.taskCategories());
    List<ClassificationSummary> classifications =
        sortClassifications(environment.classifications());
    List<WorkbasketSummary> workbaskets = sortWorkbaskets(environment.workbaskets());
    if (classifications.isEmpty()) {
      throw new IllegalArgumentException("environment.classifications must not be empty");
    }
    if (workbaskets.isEmpty()) {
      throw new IllegalArgumentException("environment.workbaskets must not be empty");
    }

    List<String> candidateUsers =
        environment.candidateUsers() == null || environment.candidateUsers().isEmpty()
            ? extractCandidateUsers(Collections.emptyMap(), workbaskets)
            : List.copyOf(new LinkedHashSet<>(environment.candidateUsers()));

    return new TaskGenerationEnvironment(
        domains,
        candidateUsers,
        taskClassificationType,
        taskCategories,
        classifications,
        workbaskets);
  }

  private static Map<String, DomainFixtures> buildDomainFixtures(
      TaskGenerationEnvironment environment) {
    Map<String, DomainFixtures> fixtures = new LinkedHashMap<>();
    for (String domain : environment.domains()) {
      List<ClassificationSummary> classifications =
          environment.classifications().stream()
              .filter(
                  classification ->
                      domain.equals(classification.getDomain())
                          || classification.getDomain().isBlank())
              .collect(Collectors.toCollection(ArrayList::new));
      if (classifications.isEmpty()) {
        throw new IllegalArgumentException(
            String.format("No compatible classifications configured for domain '%s'", domain));
      }

      List<WorkbasketSummary> workbaskets =
          environment.workbaskets().stream()
              .filter(workbasket -> domain.equals(workbasket.getDomain()))
              .collect(Collectors.toCollection(ArrayList::new));
      if (workbaskets.isEmpty()) {
        throw new IllegalArgumentException(
            String.format("No compatible workbaskets configured for domain '%s'", domain));
      }

      Map<WorkbasketType, List<WorkbasketSummary>> workbasketsByType =
          new EnumMap<>(WorkbasketType.class);
      for (WorkbasketType type : WorkbasketType.values()) {
        workbasketsByType.put(
            type,
            workbaskets.stream()
                .filter(workbasket -> workbasket.getType() == type)
                .collect(Collectors.toCollection(ArrayList::new)));
      }
      fixtures.put(domain, new DomainFixtures(classifications, workbaskets, workbasketsByType));
    }
    return fixtures;
  }

  private static List<String> normalizeConfiguredDomains(List<String> configuredDomains) {
    if (configuredDomains == null) {
      return List.of();
    }
    return configuredDomains.stream()
        .filter(Objects::nonNull)
        .map(String::trim)
        .filter(domain -> !domain.isEmpty())
        .distinct()
        .toList();
  }

  private static String determineTaskClassificationType(KadaiConfiguration configuration) {
    return configuration.getClassificationTypes().stream()
        .filter(Objects::nonNull)
        .map(String::trim)
        .filter(type -> !type.isEmpty())
        .filter(type -> type.equalsIgnoreCase("TASK"))
        .findFirst()
        .or(
            () ->
                configuration.getClassificationTypes().stream()
                    .filter(Objects::nonNull)
                    .findFirst())
        .orElse("TASK");
  }

  private static List<String> determineTaskCategories(
      KadaiConfiguration configuration, String taskClassificationType) {
    List<String> categories =
        configuration.getClassificationCategoriesByType(taskClassificationType);
    if (categories == null || categories.isEmpty()) {
      categories = configuration.getAllClassificationCategories();
    }
    if (categories == null || categories.isEmpty()) {
      return List.of("EXTERNAL");
    }
    return categories.stream()
        .filter(Objects::nonNull)
        .map(String::trim)
        .filter(category -> !category.isEmpty())
        .distinct()
        .toList();
  }

  private static List<ClassificationSummary> loadCompatibleClassifications(
      ClassificationService classificationService,
      List<String> domains,
      String taskClassificationType,
      List<String> taskCategories) {
    ClassificationQuery query = classificationService.createClassificationQuery();
    query.typeIn(taskClassificationType);
    query.domainIn(domains.toArray(String[]::new));
    List<ClassificationSummary> classifications = query.list();
    return classifications.stream()
        .filter(
            classification ->
                classification.getCategory() != null
                    && taskCategories.stream()
                        .anyMatch(
                            category -> category.equalsIgnoreCase(classification.getCategory())))
        .toList();
  }

  private static List<WorkbasketSummary> loadCompatibleWorkbaskets(
      WorkbasketService workbasketService, List<String> domains) {
    WorkbasketQuery query = workbasketService.createWorkbasketQuery();
    query.domainIn(domains.toArray(String[]::new));
    return query.list();
  }

  private static ClassificationSummary createClassification(
      ClassificationService classificationService,
      String domain,
      String taskClassificationType,
      String category,
      int sequence,
      Map<String, Set<String>> existingClassificationKeysByDomain)
      throws ClassificationAlreadyExistException,
          DomainNotFoundException,
          MalformedServiceLevelException,
          NotAuthorizedException {
    Classification classification =
        classificationService.newClassification(
            uniqueClassificationKey(domain, category, sequence, existingClassificationKeysByDomain),
            domain,
            taskClassificationType);
    classification.setCategory(category);
    classification.setName(pickGeneratedClassificationName(category, sequence));
    classification.setDescription(classification.getName() + GENERATED_DESCRIPTION_SUFFIX);
    classification.setPriority(Math.max(1, sequence + 1));
    classification.setServiceLevel("P" + (sequence + 1) + "D");
    classification.setApplicationEntryPoint(sequence % 2 == 0 ? "load-test" : "workplace");
    classification.setIsValidInDomain(true);
    classification.setCustomField(ClassificationCustomField.CUSTOM_1, "VNR,RVNR,KOLVNR");
    if (sequence % 2 == 0) {
      classification.setCustomField(ClassificationCustomField.CUSTOM_2, "TEXT_" + (sequence + 1));
    }
    return classificationService.createClassification(classification).asSummary();
  }

  private static List<WorkbasketSummary> ensureWorkbasketsForDomain(
      WorkbasketService workbasketService,
      String domain,
      List<String> candidateUsers,
      List<WorkbasketSummary> existingWorkbaskets,
      Map<String, Set<String>> existingWorkbasketKeysByDomain)
      throws WorkbasketAlreadyExistException, DomainNotFoundException, NotAuthorizedException {
    List<WorkbasketSummary> created = new ArrayList<>();
    List<WorkbasketSummary> domainWorkbaskets =
        existingWorkbaskets.stream()
            .filter(workbasket -> domain.equals(workbasket.getDomain()))
            .toList();

    long personalCount =
        domainWorkbaskets.stream().filter(wb -> wb.getType() == WorkbasketType.PERSONAL).count();
    for (int i = (int) personalCount; i < GENERATED_PERSONAL_WORKBASKETS_PER_DOMAIN; i++) {
      created.add(
          createWorkbasket(
              workbasketService,
              domain,
              WorkbasketType.PERSONAL,
              i,
              candidateUsers,
              existingWorkbasketKeysByDomain));
    }

    if (domainWorkbaskets.stream().noneMatch(wb -> wb.getType() == WorkbasketType.GROUP)) {
      created.add(
          createWorkbasket(
              workbasketService,
              domain,
              WorkbasketType.GROUP,
              0,
              candidateUsers,
              existingWorkbasketKeysByDomain));
    }

    if (domainWorkbaskets.stream().noneMatch(wb -> wb.getType() == WorkbasketType.TOPIC)) {
      created.add(
          createWorkbasket(
              workbasketService,
              domain,
              WorkbasketType.TOPIC,
              0,
              candidateUsers,
              existingWorkbasketKeysByDomain));
    }

    return created;
  }

  private static WorkbasketSummary createWorkbasket(
      WorkbasketService workbasketService,
      String domain,
      WorkbasketType type,
      int sequence,
      List<String> candidateUsers,
      Map<String, Set<String>> existingWorkbasketKeysByDomain)
      throws WorkbasketAlreadyExistException, DomainNotFoundException, NotAuthorizedException {
    String key = uniqueWorkbasketKey(domain, type, sequence, existingWorkbasketKeysByDomain);
    Workbasket workbasket = workbasketService.newWorkbasket(key, domain);
    String owner = determineGeneratedWorkbasketOwner(type, sequence, candidateUsers);
    workbasket.setType(type);
    workbasket.setName(generatedWorkbasketName(domain, type, sequence));
    workbasket.setDescription(workbasket.getName() + GENERATED_DESCRIPTION_SUFFIX);
    workbasket.setOwner(owner);
    workbasket.setOrgLevel1("versicherung");
    workbasket.setOrgLevel2("service");
    workbasket.setOrgLevel3(type.name().toLowerCase(Locale.ROOT));
    workbasket.setOrgLevel4(domain.toLowerCase(Locale.ROOT));
    workbasket.setCustomField(WorkbasketCustomField.CUSTOM_1, domain);
    workbasket.setCustomField(WorkbasketCustomField.CUSTOM_4, type.name());
    return workbasketService.createWorkbasket(workbasket).asSummary();
  }

  private static String uniqueClassificationKey(
      String domain,
      String category,
      int sequence,
      Map<String, Set<String>> existingClassificationKeysByDomain) {
    String domainPrefix = sanitizeForKey(domain);
    String categoryPrefix = sanitizeForKey(category);
    Set<String> keys =
        existingClassificationKeysByDomain.computeIfAbsent(domain, _ -> new LinkedHashSet<>());
    int counter = sequence + 1;
    String candidate;
    do {
      candidate =
          GENERATED_KEY_PREFIX
              + "_CLI_"
              + domainPrefix
              + "_"
              + categoryPrefix
              + "_"
              + pad(counter, 2);
      counter++;
    } while (!keys.add(candidate.toLowerCase(Locale.ROOT)));
    return candidate;
  }

  private static String uniqueWorkbasketKey(
      String domain,
      WorkbasketType type,
      int sequence,
      Map<String, Set<String>> existingWorkbasketKeysByDomain) {
    String domainPrefix = sanitizeForKey(domain);
    Set<String> keys =
        existingWorkbasketKeysByDomain.computeIfAbsent(domain, _ -> new LinkedHashSet<>());
    int counter = sequence + 1;
    String typePrefix =
        switch (type) {
          case PERSONAL -> "USER";
          case GROUP -> "GPK";
          case TOPIC -> "TPK";
          default -> throw new IllegalArgumentException("Unsupported workbasket type: " + type);
        };
    String candidate;
    do {
      candidate =
          GENERATED_KEY_PREFIX + "_" + typePrefix + "_" + domainPrefix + "_" + pad(counter, 2);
      counter++;
    } while (!keys.add(candidate.toLowerCase(Locale.ROOT)));
    return candidate;
  }

  private static String determineGeneratedWorkbasketOwner(
      WorkbasketType type, int sequence, List<String> candidateUsers) {
    if (candidateUsers.isEmpty()) {
      return "admin";
    }
    if (type == WorkbasketType.TOPIC) {
      return "";
    }
    if (type == WorkbasketType.GROUP) {
      return candidateUsers.get(sequence % candidateUsers.size());
    }
    return candidateUsers.get(sequence % candidateUsers.size());
  }

  private static String generatedWorkbasketName(String domain, WorkbasketType type, int sequence) {
    return switch (type) {
      case PERSONAL -> "Persönlicher Postkorb " + domain + " " + (sequence + 1);
      case GROUP -> "Gruppenpostkorb " + domain + " " + (sequence + 1);
      case TOPIC ->
          "Themenpostkorb " + WORKBASKET_TOPIC_NAMES.get(sequence % WORKBASKET_TOPIC_NAMES.size());
      default -> throw new IllegalArgumentException("Unsupported workbasket type: " + type);
    };
  }

  private static String pickGeneratedClassificationName(String category, int sequence) {
    String normalizedCategory = category.toUpperCase(Locale.ROOT);
    List<String> names =
        switch (normalizedCategory) {
          case "EXTERNAL" -> EXTERNAL_CLASSIFICATION_NAMES;
          case "MANUAL" -> MANUAL_CLASSIFICATION_NAMES;
          case "AUTOMATIC" -> AUTOMATIC_CLASSIFICATION_NAMES;
          case "PROCESS" -> PROCESS_CLASSIFICATION_NAMES;
          default -> FALLBACK_CLASSIFICATION_NAMES;
        };
    return names.get(sequence % names.size());
  }

  private static String sanitizeForKey(String value) {
    return value.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "_");
  }

  private static List<String> extractCandidateUsers(
      KadaiConfiguration configuration, List<WorkbasketSummary> workbaskets) {
    return extractCandidateUsers(configuration.getRoleMap(), workbaskets);
  }

  private static List<String> extractCandidateUsers(
      Map<KadaiRole, Set<String>> roleMap, List<WorkbasketSummary> workbaskets) {
    LinkedHashSet<String> users = new LinkedHashSet<>();
    if (roleMap != null) {
      roleMap.values().stream()
          .filter(Objects::nonNull)
          .flatMap(Collection::stream)
          .filter(TaskTestDataGenerator::isLikelyUserId)
          .forEach(users::add);
    }

    workbaskets.stream()
        .map(WorkbasketSummary::getOwner)
        .filter(TaskTestDataGenerator::isLikelyUserId)
        .forEach(users::add);

    if (users.isEmpty()) {
      users.add("admin");
    }
    return List.copyOf(users);
  }

  private static boolean isLikelyUserId(String accessId) {
    return accessId != null
        && !accessId.isBlank()
        && !accessId.contains("=")
        && !accessId.contains(",")
        && !accessId.contains(" ");
  }

  private static List<ClassificationSummary> sortClassifications(
      List<ClassificationSummary> classifications) {
    return classifications.stream()
        .filter(Objects::nonNull)
        .sorted(
            Comparator.comparing(ClassificationSummary::getDomain)
                .thenComparing(
                    ClassificationSummary::getCategory, Comparator.nullsLast(String::compareTo))
                .thenComparing(ClassificationSummary::getKey))
        .toList();
  }

  private static List<WorkbasketSummary> sortWorkbaskets(List<WorkbasketSummary> workbaskets) {
    return workbaskets.stream()
        .filter(Objects::nonNull)
        .sorted(
            Comparator.comparing(WorkbasketSummary::getDomain)
                .thenComparing(workbasket -> workbasket.getType().name())
                .thenComparing(WorkbasketSummary::getKey))
        .toList();
  }

  private ClassificationSummary pickClassification(
      DomainFixtures fixtures, SplittableRandom random) {
    return fixtures.classifications().get(random.nextInt(fixtures.classifications().size()));
  }

  private WorkbasketSummary pickWorkbasket(DomainFixtures fixtures, SplittableRandom random) {
    double roll = random.nextDouble();
    double threshold = 0.0;
    for (Map.Entry<WorkbasketType, Double> entry : WORKBASKET_TYPE_WEIGHTS.entrySet()) {
      threshold += entry.getValue();
      if (roll <= threshold) {
        List<WorkbasketSummary> matches = fixtures.workbasketsByType().get(entry.getKey());
        if (matches != null && !matches.isEmpty()) {
          return matches.get(random.nextInt(matches.size()));
        }
      }
    }
    return fixtures.workbaskets().get(random.nextInt(fixtures.workbaskets().size()));
  }

  private String pickCreator(SplittableRandom random) {
    return environment.candidateUsers().get(random.nextInt(environment.candidateUsers().size()));
  }

  private static TaskState pickTaskState(SplittableRandom random) {
    double roll = random.nextDouble();
    if (roll < 0.60) {
      return TaskState.READY;
    }
    if (roll < 0.80) {
      return TaskState.CLAIMED;
    }
    if (roll < 0.87) {
      return TaskState.READY_FOR_REVIEW;
    }
    if (roll < 0.92) {
      return TaskState.IN_REVIEW;
    }
    if (roll < 0.975) {
      return TaskState.COMPLETED;
    }
    if (roll < 0.992) {
      return TaskState.CANCELLED;
    }
    return TaskState.TERMINATED;
  }

  private String determineOwner(
      TaskState state, WorkbasketSummary workbasket, SplittableRandom random) {
    if (state == TaskState.READY || state == TaskState.CANCELLED || state == TaskState.TERMINATED) {
      return null;
    }
    if (workbasket.getOwner() != null && !workbasket.getOwner().isBlank()) {
      return workbasket.getOwner();
    }
    return environment.candidateUsers().get(random.nextInt(environment.candidateUsers().size()));
  }

  private Instant computeDue(Instant planned, String serviceLevel) {
    Duration duration = parseServiceLevel(serviceLevel);
    if (workingTimeCalculator == null) {
      return planned.plus(duration);
    }
    try {
      return workingTimeCalculator.addWorkingTime(planned, duration);
    } catch (InvalidArgumentException ex) {
      throw new TestDataGenerationException("Failed to calculate due date from service level", ex);
    }
  }

  private static Duration parseServiceLevel(String serviceLevel) {
    if (serviceLevel == null || serviceLevel.isBlank()) {
      return Duration.ofDays(1);
    }
    try {
      return Duration.parse(serviceLevel);
    } catch (RuntimeException _) {
      return Duration.ofDays(1);
    }
  }

  private static boolean isRead(TaskState state, SplittableRandom random) {
    if (state == TaskState.READY) {
      return random.nextDouble() < 0.15;
    }
    return random.nextDouble() < 0.9;
  }

  private static int determineNumberOfComments(TaskState state, SplittableRandom random) {
    int bound =
        switch (state) {
          case READY -> 2;
          case CLAIMED, READY_FOR_REVIEW, IN_REVIEW -> 4;
          case COMPLETED, CANCELLED, TERMINATED -> 3;
        };
    return random.nextInt(bound);
  }

  private static int determineManualPriority(int classificationPriority, SplittableRandom random) {
    if (random.nextDouble() < 0.08) {
      return Math.max(0, classificationPriority + random.nextInt(-1, 4));
    }
    return -1;
  }

  private static CallbackState determineCallbackState(TaskState state, SplittableRandom random) {
    if (state == TaskState.COMPLETED && random.nextDouble() < 0.15) {
      return CallbackState.CALLBACK_PROCESSING_COMPLETED;
    }
    if ((state == TaskState.CLAIMED || state == TaskState.IN_REVIEW)
        && random.nextDouble() < 0.03) {
      return CallbackState.CLAIMED;
    }
    return CallbackState.NONE;
  }

  private static String deriveTaskName(
      ClassificationSummary classification, SplittableRandom random) {
    if (classification.getName() != null && !classification.getName().isBlank()) {
      return classification.getName();
    }
    return pick(FALLBACK_CLASSIFICATION_NAMES, random);
  }

  private static String deriveTaskDescription(
      ClassificationSummary classification, String creator, SplittableRandom random) {
    StringBuilder description = new StringBuilder();
    description.append(deriveTaskName(classification, random));
    description.append(" – ").append(pick(TEXT_FRAGMENTS, random));
    description.append(" (Erzeuger: ").append(creator).append(")");
    return description.toString();
  }

  private static void enrichCustomFields(
      GeneratedTaskImpl task,
      ClassificationSummary classification,
      WorkbasketSummary workbasket,
      String referenceValue,
      SplittableRandom random) {
    task.setCustomField(TaskCustomField.CUSTOM_1, classification.getKey());
    task.setCustomField(TaskCustomField.CUSTOM_2, workbasket.getKey());
    if (random.nextDouble() < 0.35) {
      task.setCustomField(TaskCustomField.CUSTOM_3, pick(TEXT_FRAGMENTS, random));
    }
    if (random.nextDouble() < 0.45) {
      task.setCustomField(TaskCustomField.CUSTOM_5, "important");
    }
    task.setCustomField(TaskCustomField.CUSTOM_14, "abc");
    if (random.nextDouble() < 0.2) {
      task.setCustomField(TaskCustomField.CUSTOM_15, referenceValue);
    }
    task.setCustomIntField(TaskCustomIntField.CUSTOM_INT_1, random.nextInt(1, 100));
    if (random.nextDouble() < 0.3) {
      task.setCustomIntField(TaskCustomIntField.CUSTOM_INT_2, random.nextInt(1, 100));
    }
  }

  private static String formatIdentifier(String prefix, long sequence) {
    return String.format("%s:%s", prefix, pad(sequence, 12));
  }

  private static String pad(long value, int length) {
    return String.format(Locale.ROOT, "%0" + length + "d", value);
  }

  private static String eightDigits(long value) {
    long normalized = Math.floorMod(value, 100_000_000L);
    return pad(normalized, 8);
  }

  private static <T> T pick(List<T> values, SplittableRandom random) {
    return values.get(random.nextInt(values.size()));
  }

  private static long mixSeed(long seed, long index) {
    long mixed = seed ^ (index + 0x9E3779B97F4A7C15L);
    mixed ^= (mixed >>> 30);
    mixed *= 0xBF58476D1CE4E5B9L;
    mixed ^= (mixed >>> 27);
    mixed *= 0x94D049BB133111EBL;
    mixed ^= (mixed >>> 31);
    return mixed;
  }

  private static void validateTaskCount(long numberOfTasks) {
    if (numberOfTasks < 0) {
      throw new IllegalArgumentException("numberOfTasks must not be negative");
    }
  }

  private static void validateBatchSize(int batchSize) {
    if (batchSize <= 0) {
      throw new IllegalArgumentException("batchSize must be greater than zero");
    }
  }

  private static void emitBatch(TaskBatchConsumer batchConsumer, List<Task> batch) {
    try {
      batchConsumer.accept(List.copyOf(batch));
    } catch (RuntimeException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new TestDataGenerationException("Failed to process generated task batch", ex);
    }
  }

  private static EnumMap<WorkbasketType, Double> createWorkbasketTypeWeights() {
    EnumMap<WorkbasketType, Double> weights = new EnumMap<>(WorkbasketType.class);
    weights.put(WorkbasketType.PERSONAL, 0.55d);
    weights.put(WorkbasketType.GROUP, 0.30d);
    weights.put(WorkbasketType.TOPIC, 0.15d);
    return weights;
  }

  private static String createPlaceholderList(int placeholderCount) {
    return String.join(", ", Collections.nCopies(placeholderCount, "?"));
  }
}
