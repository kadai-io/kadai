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

package org.camunda.bpm.dmn.xlsx.elements;

import java.util.HashSet;
import java.util.Set;

public class HeaderValuesContainer {
  private static Set<String> LANGUAGES;

  static {
    LANGUAGES = new HashSet<String>();
    LANGUAGES.add("javascript");
    LANGUAGES.add("groovy");
    LANGUAGES.add("python");
    LANGUAGES.add("jruby");
    LANGUAGES.add("juel");
    LANGUAGES.add("feel");
  }

  private String id;
  private String text;
  private String label;
  private String typeRef;
  private String expressionLanguage;
  private String column;

  public String getExpressionLanguage() {
    if (expressionLanguage != null && LANGUAGES.contains(expressionLanguage)) {
      return expressionLanguage;
    } else {
      return null;
    }
  }

  public void setExpressionLanguage(String expressionLanguage) {
    this.expressionLanguage = expressionLanguage;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getTypeRef() {
    return typeRef;
  }

  public void setTypeRef(String typeRef) {
    this.typeRef = typeRef;
  }

  public String getColumn() {
    return column;
  }

  public void setColumn(String column) {
    this.column = column;
  }
}
