/*
 * Copyright [2025] [envite consulting GmbH]
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

package io.kadai.common.internal.transaction;

import io.kadai.classification.api.exceptions.ClassificationNotFoundException;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.task.api.TaskService;
import io.kadai.task.api.exceptions.AttachmentPersistenceException;
import io.kadai.task.api.exceptions.ObjectReferencePersistenceException;
import io.kadai.task.api.exceptions.TaskAlreadyExistException;
import io.kadai.task.api.models.Task;
import io.kadai.task.internal.models.ObjectReferenceImpl;
import io.kadai.workbasket.api.exceptions.NotAuthorizedOnWorkbasketException;
import io.kadai.workbasket.api.exceptions.WorkbasketNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** TODO. */
@Component
@Transactional
public class KadaiComponent {

  @Autowired TaskService taskService;

  public TaskService getTaskService() {
    return taskService;
  }

  public void triggerRollback()
      throws WorkbasketNotFoundException,
          ClassificationNotFoundException,
          TaskAlreadyExistException,
          InvalidArgumentException,
          AttachmentPersistenceException,
          ObjectReferencePersistenceException,
          NotAuthorizedOnWorkbasketException {
    Task task = taskService.newTask("1");
    task.setName("Unit Test Task");
    ObjectReferenceImpl objRef = new ObjectReferenceImpl();
    objRef.setCompany("aCompany");
    objRef.setSystem("aSystem");
    objRef.setSystemInstance("anInstance");
    objRef.setType("aType");
    objRef.setValue("aValue");
    task.setPrimaryObjRef(objRef);

    taskService.createTask(task);
    throw new RuntimeException();
  }
}
