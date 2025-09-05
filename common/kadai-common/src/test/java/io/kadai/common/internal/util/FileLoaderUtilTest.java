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

package io.kadai.common.internal.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.kadai.common.api.exceptions.SystemException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileLoaderUtilTest {
  @TempDir Path tempDir;

  @Test
  void should_DetectFile_When_FileIsPresentOnSystem() throws Exception {
    Path file = Files.createFile(tempDir.resolve("systemTest.txt"));
    boolean fileExists = FileLoaderUtil.fileExistsOnSystem(file.toAbsolutePath().toString());
    assertThat(fileExists).isTrue();
  }

  @Test
  void should_NotDetectFile_When_FileDoesNotExist() {
    boolean fileExists = FileLoaderUtil.fileExistsOnSystem("doesnotexist");
    assertThat(fileExists).isFalse();
  }

  @Test
  void should_NotDetectFile_When_FileExistsOnClasspath() {
    boolean fileExists = FileLoaderUtil.fileExistsOnSystem("fileInClasspath.txt");
    assertThat(fileExists).isFalse();
  }

  @Test
  void should_OpenFile_When_FileIsPresentOnSystem() throws Exception {
    Path file = Files.createFile(tempDir.resolve("systemTest.txt"));
    String expectedFileContent = "This file is in the file system";
    Files.write(file, List.of(expectedFileContent), StandardCharsets.UTF_8);

    try (InputStream stream =
        FileLoaderUtil.openFileFromClasspathOrSystem(
            file.toAbsolutePath().toString(), getClass())) {
      String fileContent = convertToString(stream);
      assertThat(fileContent).isEqualTo(expectedFileContent);
    }
  }

  @Test
  void should_ThrowSystemException_When_FileDoesNotExist() {
    Class<?> clazz = getClass();
    assertThatThrownBy(() -> FileLoaderUtil.openFileFromClasspathOrSystem("doesnotexist", clazz))
        .isInstanceOf(SystemException.class)
        .hasMessage("Could not find a file in the classpath 'doesnotexist'");
  }

  @Test
  void should_OpenFile_When_FileExistsOnClasspath() {
    InputStream stream =
        FileLoaderUtil.openFileFromClasspathOrSystem("fileInClasspath.txt", getClass());

    String fileContent = convertToString(stream);
    assertThat(fileContent).isEqualTo("This file is in the classpath");
  }

  private String convertToString(InputStream stream) {
    return new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
        .lines()
        .collect(Collectors.joining("\n"));
  }
}
