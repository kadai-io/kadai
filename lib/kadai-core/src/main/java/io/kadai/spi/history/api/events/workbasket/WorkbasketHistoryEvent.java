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

package io.kadai.spi.history.api.events.workbasket;

import io.kadai.common.api.exceptions.SystemException;
import io.kadai.workbasket.api.WorkbasketCustomField;
import io.kadai.workbasket.api.models.WorkbasketSummary;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/** Super class for all workbasket related events. */
public class WorkbasketHistoryEvent {

  protected String id;
  protected String eventType;
  protected Instant created;
  protected String userId;
  protected String domain;
  protected String workbasketId;
  protected String key;
  protected String type;
  protected String owner;
  protected String custom1;
  protected String custom2;
  protected String custom3;
  protected String custom4;
  protected String custom5;
  protected String custom6;
  protected String custom7;
  protected String custom8;
  protected String orgLevel1;
  protected String orgLevel2;
  protected String orgLevel3;
  protected String orgLevel4;
  protected String details;

  public WorkbasketHistoryEvent() {}

  public WorkbasketHistoryEvent(
      String id, WorkbasketSummary workbasket, String userId, String details) {
    this.id = id;
    this.userId = userId;
    this.details = details;
    workbasketId = workbasket.getId();
    domain = workbasket.getDomain();
    key = workbasket.getKey();
    type = workbasket.getType().name();
    owner = workbasket.getOwner();
    custom1 = workbasket.getCustomField(WorkbasketCustomField.CUSTOM_1);
    custom2 = workbasket.getCustomField(WorkbasketCustomField.CUSTOM_2);
    custom3 = workbasket.getCustomField(WorkbasketCustomField.CUSTOM_3);
    custom4 = workbasket.getCustomField(WorkbasketCustomField.CUSTOM_4);
    custom5 = workbasket.getCustomField(WorkbasketCustomField.CUSTOM_5);
    custom6 = workbasket.getCustomField(WorkbasketCustomField.CUSTOM_6);
    custom7 = workbasket.getCustomField(WorkbasketCustomField.CUSTOM_7);
    custom8 = workbasket.getCustomField(WorkbasketCustomField.CUSTOM_8);
    orgLevel1 = workbasket.getOrgLevel1();
    orgLevel2 = workbasket.getOrgLevel2();
    orgLevel3 = workbasket.getOrgLevel3();
    orgLevel4 = workbasket.getOrgLevel4();
  }

  public void setCustomAttribute(WorkbasketCustomField customField, String value) {
    switch (customField) {
      case CUSTOM_1:
        custom1 = value;
        break;
      case CUSTOM_2:
        custom2 = value;
        break;
      case CUSTOM_3:
        custom3 = value;
        break;
      case CUSTOM_4:
        custom4 = value;
        break;
      case CUSTOM_5:
        custom5 = value;
        break;
      case CUSTOM_6:
        custom6 = value;
        break;
      case CUSTOM_7:
        custom7 = value;
        break;
      case CUSTOM_8:
        custom8 = value;
        break;
      default:
        throw new SystemException("Unknown customField '" + customField + "'");
    }
  }

  public String getCustomAttribute(WorkbasketCustomField customField) {
    switch (customField) {
      case CUSTOM_1:
        return custom1;
      case CUSTOM_2:
        return custom2;
      case CUSTOM_3:
        return custom3;
      case CUSTOM_4:
        return custom4;
      case CUSTOM_5:
        return custom5;
      case CUSTOM_6:
        return custom6;
      case CUSTOM_7:
        return custom7;
      case CUSTOM_8:
        return custom8;
      default:
        throw new SystemException("Unknown customField '" + customField + "'");
    }
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getEventType() {
    return eventType;
  }

  public void setEventType(String eventType) {
    this.eventType = eventType;
  }

  public Instant getCreated() {
    return created != null ? created.truncatedTo(ChronoUnit.MILLIS) : null;
  }

  public void setCreated(Instant created) {
    this.created = created != null ? created.truncatedTo(ChronoUnit.MILLIS) : null;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getDomain() {
    return domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  public String getWorkbasketId() {
    return workbasketId;
  }

  public void setWorkbasketId(String workbasketId) {
    this.workbasketId = workbasketId;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public String getOrgLevel1() {
    return orgLevel1;
  }

  public void setOrgLevel1(String orgLevel1) {
    this.orgLevel1 = orgLevel1;
  }

  public String getOrgLevel2() {
    return orgLevel2;
  }

  public void setOrgLevel2(String orgLevel2) {
    this.orgLevel2 = orgLevel2;
  }

  public String getOrgLevel3() {
    return orgLevel3;
  }

  public void setOrgLevel3(String orgLevel3) {
    this.orgLevel3 = orgLevel3;
  }

  public String getOrgLevel4() {
    return orgLevel4;
  }

  public void setOrgLevel4(String orgLevel4) {
    this.orgLevel4 = orgLevel4;
  }

  public String getDetails() {
    return details;
  }

  public void setDetails(String details) {
    this.details = details;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        getId(),
        getEventType(),
        getCreated(),
        getUserId(),
        getDomain(),
        getWorkbasketId(),
        getKey(),
        getType(),
        getOwner(),
        custom1,
        custom2,
        custom3,
        custom4,
        custom5,
        custom6,
        custom7,
        custom8,
        getOrgLevel1(),
        getOrgLevel2(),
        getOrgLevel3(),
        getOrgLevel4(),
        getDetails());
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof WorkbasketHistoryEvent)) {
      return false;
    }
    WorkbasketHistoryEvent other = (WorkbasketHistoryEvent) obj;
    return Objects.equals(getId(), other.getId())
        && Objects.equals(getEventType(), other.getEventType())
        && Objects.equals(getCreated(), other.getCreated())
        && Objects.equals(getUserId(), other.getUserId())
        && Objects.equals(getDomain(), other.getDomain())
        && Objects.equals(getWorkbasketId(), other.getWorkbasketId())
        && Objects.equals(getKey(), other.getKey())
        && Objects.equals(getType(), other.getType())
        && Objects.equals(getOwner(), other.getOwner())
        && Objects.equals(custom1, other.custom1)
        && Objects.equals(custom2, other.custom2)
        && Objects.equals(custom3, other.custom3)
        && Objects.equals(custom4, other.custom4)
        && Objects.equals(custom5, other.custom5)
        && Objects.equals(custom6, other.custom6)
        && Objects.equals(custom7, other.custom7)
        && Objects.equals(custom8, other.custom8)
        && Objects.equals(getOrgLevel1(), other.getOrgLevel1())
        && Objects.equals(getOrgLevel2(), other.getOrgLevel2())
        && Objects.equals(getOrgLevel3(), other.getOrgLevel3())
        && Objects.equals(getOrgLevel4(), other.getOrgLevel4())
        && Objects.equals(getDetails(), other.getDetails());
  }

  @Override
  public String toString() {
    return "WorkbasketEvent [id="
        + id
        + ", eventType="
        + eventType
        + ", created="
        + created
        + ", userId="
        + userId
        + ", domain="
        + domain
        + ", workbasketId="
        + workbasketId
        + ", workbasketKey="
        + key
        + ", workbasketType="
        + type
        + ", owner="
        + owner
        + ", custom1="
        + custom1
        + ", custom2="
        + custom2
        + ", custom3="
        + custom3
        + ", custom4="
        + custom4
        + ", custom5="
        + custom5
        + ", custom6="
        + custom6
        + ", custom7="
        + custom7
        + ", custom8="
        + custom8
        + ", orgLevel1="
        + orgLevel1
        + ", orgLevel2="
        + orgLevel2
        + ", orgLevel3="
        + orgLevel3
        + ", orgLevel4="
        + orgLevel4
        + ", details="
        + details
        + "]";
  }
}
