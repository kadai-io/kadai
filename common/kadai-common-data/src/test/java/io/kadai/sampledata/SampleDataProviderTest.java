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

package io.kadai.sampledata;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Test SampleDataGenerator. */
class SampleDataProviderTest {

  @Test
  void getScriptsNotNull() {
    assertThat(SampleDataProvider.getSampleDataCreationScripts()).isNotNull();
  }

  @Test
  void getScriptsNotEmpty() {
    assertThat(SampleDataProvider.getSampleDataCreationScripts()).isNotEmpty();
  }

  @Test
  void getScriptsFileExists() {
    assertThat(SampleDataProvider.getSampleDataCreationScripts())
        .extracting(SqlReplacer::getScriptBufferedStream)
        .doesNotContainNull();
  }
}
