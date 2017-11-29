package pro.taskana;

import pro.taskana.exceptions.ClassificationNotFoundException;
import pro.taskana.exceptions.NotAuthorizedException;
import pro.taskana.exceptions.TaskNotFoundException;
import pro.taskana.exceptions.WorkbasketNotFoundException;
import pro.taskana.model.*;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * The Task Service manages all operations on tasks.
 */
public interface TaskService {

    /**
     * Claim an existing task.
     * @param id
     *            task id
     * @param userName
     *            user who claims the task
     * @return modified claimed Task
     * @throws TaskNotFoundException
     */
    Task claim(String id, String userName) throws TaskNotFoundException;

    /**
     * Set task to completed.
     * @param taskId
     *            the task id
     * @return changed Task after update.
     * @throws TaskNotFoundException
     */
    Task complete(String taskId) throws TaskNotFoundException;

    /**
     * Create a task by a task object.
     * @param task
     * @return the created task
     * @throws NotAuthorizedException
     */
    Task create(Task task) throws NotAuthorizedException, WorkbasketNotFoundException;

    /**
     * Create a task manually by filling the fields.
     * @param workbasketId not null
     * @param classificationId not null
     * @param domain
     * @param planned
     * @param name
     * @param description
     * @param primaryObjectReference
     * @param customAttributes
     * @return
     */
    Task createManualTask(String workbasketId, String classificationId, String domain, Timestamp planned, String name, String description, ObjectReference primaryObjectReference, Map<String, Object> customAttributes) throws NotAuthorizedException, WorkbasketNotFoundException, ClassificationNotFoundException;
    /**
     * Get the details of a task.
     * @param taskId
     *            the id of the task
     * @return the Task
     */
    Task getTaskById(String taskId) throws TaskNotFoundException;

    /**
     * This method counts all tasks with a given state.
     * @param states
     *            the countable states
     * @return a List of {@link TaskStateCounter}
     */
    List<TaskStateCounter> getTaskCountForState(List<TaskState> states);

    /**
     * Count all Tasks in a given workbasket with daysInPast as Days from today in
     * the past and a specific state.
     * @param workbasketId
     * @param daysInPast
     * @param states
     * @return
     */
    long getTaskCountForWorkbasketByDaysInPastAndState(String workbasketId, long daysInPast, List<TaskState> states);

    List<DueWorkbasketCounter> getTaskCountByWorkbasketAndDaysInPastAndState(long daysInPast, List<TaskState> states);

    /**
     * Transfer task to another workbasket. The transfer set the transferred flag
     * and resets the read flag.
     * @param workbasketId
     * @return the updated task
     * @throws NotAuthorizedException
     */
    Task transfer(String taskId, String workbasketId)
            throws TaskNotFoundException, WorkbasketNotFoundException, NotAuthorizedException;

    /**
     * Marks a task as read.
     * @param taskId
     *            the id of the task to be updated
     * @param isRead
     *            the new status of the read flag.
     * @return Task the updated Task
     */
    Task setTaskRead(String taskId, boolean isRead) throws TaskNotFoundException;

    /**
     * This method provides a query builder for quering the database.
     * @return a {@link TaskQuery}
     */
    TaskQuery createTaskQuery();

}
