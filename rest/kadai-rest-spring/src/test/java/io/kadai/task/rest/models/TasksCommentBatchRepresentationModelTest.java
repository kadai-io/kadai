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

package io.kadai.task.rest.models;

import static org.assertj.core.api.Assertions.assertThat;

import io.kadai.rest.test.KadaiSpringBootTest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.Link;

/** Test TasksCommentBatchRepresentationModelTest. */
@KadaiSpringBootTest
class TasksCommentBatchRepresentationModelTest {

  @Test
  void should_Equal_when_allFieldsAreSame_and_hashCodesMatch() {
    var a = new TasksCommentBatchRepresentationModel(List.of("T1", "T2"), "comment");
    var b = new TasksCommentBatchRepresentationModel(List.of("T1", "T2"), "comment");

    assertThat(a)
        .isEqualTo(b)
        .hasSameHashCodeAs(b);
  }

  @Test
  void should_NotEqual_when_taskIdsOrTextFieldDiffer() {
    var base = new TasksCommentBatchRepresentationModel(List.of("T1", "T2"), "comment");
    var diffIds = new TasksCommentBatchRepresentationModel(List.of("T3"), "comment");
    var diffText = new TasksCommentBatchRepresentationModel(List.of("T1", "T2"), "different");

    assertThat(base)
        .isNotEqualTo(diffIds)
        .isNotEqualTo(diffText);
  }

  @Test
  void should_ReturnTrue_when_SameInstanceCompared() {
    TasksCommentBatchRepresentationModel model = new TasksCommentBatchRepresentationModel(
            List.of("T1"), "comment");

    TasksCommentBatchRepresentationModel sameInstanceRef1 = model;
    TasksCommentBatchRepresentationModel sameInstanceRef2 = model;

    assertThat(sameInstanceRef1).isEqualTo(sameInstanceRef2);
  }

  @Test
  void should_ReturnFalse_when_comparedToNullOrDifferentTypeOrWhenSuperEqualsFails() {
    var model = new TasksCommentBatchRepresentationModel(List.of("T1"), "comment");
    var withOtherLink = new TasksCommentBatchRepresentationModel(List.of("T1"), "comment");
    withOtherLink.add(Link.of("http://diff"));

    assertThat(model).isNotEqualTo(new Object());
    assertThat(model.equals(withOtherLink)).isFalse();
  }
}
