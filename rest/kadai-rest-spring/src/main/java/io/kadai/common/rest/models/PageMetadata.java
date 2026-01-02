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

package io.kadai.common.rest.models;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.beans.ConstructorProperties;
import java.util.Objects;

@Schema(
    description =
        "This is copied from org.springframework.hateoas.PagedModel.PageMetadata. Reason: The "
            + "Spring OpenAPI only parses our code to check for OpenAPI notations. Since we want "
            + "this class to be documented we had to copy it.")
public class PageMetadata {

  @Parameter(hidden = true)
  @Schema(name = "size", description = "The element size of the page.")
  private final long size;

  @Parameter(hidden = true)
  @Schema(name = "totalElements", description = "The total number of elements available.")
  private final long totalElements;

  @Parameter(hidden = true)
  @Schema(name = "totalPages", description = "Amount of pages that are available in total.")
  private final long totalPages;

  @Parameter(hidden = true)
  @Schema(name = "number", description = "The current page number.")
  private final long number;

  @ConstructorProperties({"size", "totalElements", "totalPages", "number"})
  public PageMetadata(long size, long totalElements, long totalPages, long number) {
    this.size = size;
    this.totalElements = totalElements;
    this.totalPages = totalPages;
    this.number = number;
  }

  public long getSize() {
    return size;
  }

  public long getTotalElements() {
    return totalElements;
  }

  public long getTotalPages() {
    return totalPages;
  }

  public long getNumber() {
    return number;
  }

  @Override
  public int hashCode() {
    return Objects.hash(size, totalElements, totalPages, number);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof PageMetadata)) {
      return false;
    }
    PageMetadata other = (PageMetadata) obj;
    return size == other.size
        && totalElements == other.totalElements
        && totalPages == other.totalPages
        && number == other.number;
  }

  @Override
  public String toString() {
    return "PageMetadata [size="
        + size
        + ", totalElements="
        + totalElements
        + ", totalPages="
        + totalPages
        + ", number="
        + number
        + "]";
  }
}
