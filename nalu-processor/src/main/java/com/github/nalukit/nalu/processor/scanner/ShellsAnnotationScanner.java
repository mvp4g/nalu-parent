/*
 * Copyright (c) 2018 - Frank Hossfeld
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 */

package com.github.nalukit.nalu.processor.scanner;

import com.github.nalukit.nalu.client.application.annotation.Shell;
import com.github.nalukit.nalu.client.application.annotation.Shells;
import com.github.nalukit.nalu.processor.ProcessorException;
import com.github.nalukit.nalu.processor.ProcessorUtils;
import com.github.nalukit.nalu.processor.model.ApplicationMetaModel;
import com.github.nalukit.nalu.processor.model.intern.ClassNameModel;
import com.github.nalukit.nalu.processor.model.intern.ShellModel;
import com.github.nalukit.nalu.processor.scanner.validation.FiltersAnnotationValidator;
import com.github.nalukit.nalu.processor.scanner.validation.ShellAnnotationValidator;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;

public class ShellsAnnotationScanner {

  private ProcessorUtils processorUtils;

  private ProcessingEnvironment processingEnvironment;

  private ApplicationMetaModel applicationMetaModel;

  private TypeElement applicationTypeElement;

  @SuppressWarnings("unused")
  private ShellsAnnotationScanner(Builder builder) {
    super();
    this.processingEnvironment = builder.processingEnvironment;
    this.applicationMetaModel = builder.applicationMetaModel;
    this.applicationTypeElement = builder.applicationTypeElement;
    setUp();
  }

  public static Builder builder() {
    return new Builder();
  }

  private void setUp() {
    this.processorUtils = ProcessorUtils.builder()
                                        .processingEnvironment(this.processingEnvironment)
                                        .build();
  }

  ApplicationMetaModel scan(RoundEnvironment roundEnvironment)
      throws ProcessorException {
    // do validation
    ShellAnnotationValidator.builder()
                            .roundEnvironment(roundEnvironment)
                            .processingEnvironment(processingEnvironment)
                            .applicationTypeElement(this.applicationTypeElement)
                            .build()
                            .validate();
    // handle Shells-annotation
    Shells annotation = this.applicationTypeElement.getAnnotation(Shells.class);
    if (annotation != null) {
      for (Shell shell : annotation.value()) {
        // do validation
        ShellAnnotationValidator.builder()
                                .roundEnvironment(roundEnvironment)
                                .processingEnvironment(processingEnvironment)
                                .applicationTypeElement(this.applicationTypeElement)
                                .applicationMetaModel(this.applicationMetaModel)
                                .build()
                                .validateName(shell.name());
        // add shell model
        this.applicationMetaModel.getShells()
                                 .add(new ShellModel(shell.name(),
                                                     new ClassNameModel(getShellTypeElement(shell).toString())));
      }
    }
    return this.applicationMetaModel;
  }

  private TypeElement getShellTypeElement(Shell annotation) {
    try {
      annotation.shell();
    } catch (MirroredTypeException exception) {
      return (TypeElement) this.processingEnvironment.getTypeUtils()
                                                     .asElement(exception.getTypeMirror());
    }
    return null;
  }

  public static class Builder {

    ProcessingEnvironment processingEnvironment;

    ApplicationMetaModel applicationMetaModel;

    TypeElement applicationTypeElement;

    public Builder processingEnvironment(ProcessingEnvironment processingEnvironment) {
      this.processingEnvironment = processingEnvironment;
      return this;
    }

    public Builder applicationMetaModel(ApplicationMetaModel applicationMetaModel) {
      this.applicationMetaModel = applicationMetaModel;
      return this;
    }

    public Builder applicationTypeElement(TypeElement applicationTypeElement) {
      this.applicationTypeElement = applicationTypeElement;
      return this;
    }

    public ShellsAnnotationScanner build() {
      return new ShellsAnnotationScanner(this);
    }
  }
}
