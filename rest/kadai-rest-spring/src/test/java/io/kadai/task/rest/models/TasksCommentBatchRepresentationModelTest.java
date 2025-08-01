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

    assertThat(model)
            .isNotEqualTo(null)
            .isNotEqualTo(new Object())
            .isNotEqualTo(withOtherLink);
  }
}
