/** 
 * Copyright (c) 2009, Regents of the University of Colorado 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 
 * Neither the name of the University of Colorado at Boulder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE. 
 */
package org.cleartk.classifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.CleartkException;
import org.cleartk.util.ReflectionUtil;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.factory.initializable.Initializable;
import org.uimafit.factory.initializable.InitializableFactory;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p>
 */

public abstract class CleartkSequenceAnnotator<OUTCOME_TYPE> extends JCasAnnotator_ImplBase
    implements Initializable {

  public static final String PARAM_CLASSIFIER_FACTORY_CLASS_NAME = ConfigurationParameterFactory
      .createConfigurationParameterName(
          CleartkSequenceAnnotator.class,
          "classifierFactoryClassName");

  @ConfigurationParameter(mandatory = false, description = "provides the full name of the SequenceClassifierFactory class to be used.", defaultValue = "org.cleartk.classifier.jar.SequenceJarClassifierFactory")
  private String classifierFactoryClassName;

  public static final String PARAM_DATA_WRITER_FACTORY_CLASS_NAME = ConfigurationParameterFactory
      .createConfigurationParameterName(
          CleartkSequenceAnnotator.class,
          "dataWriterFactoryClassName");

  @ConfigurationParameter(mandatory = false, description = "provides the full name of the SequenceDataWriterFactory class to be used.")
  private String dataWriterFactoryClassName;

  protected SequenceDataWriter<OUTCOME_TYPE> dataWriter;

  protected SequenceClassifier<OUTCOME_TYPE> classifier;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);

    if (dataWriterFactoryClassName != null) {
      // create the factory and instantiate the data writer
      SequenceDataWriterFactory<?> factory = InitializableFactory.create(
          context,
          dataWriterFactoryClassName,
          SequenceDataWriterFactory.class);
      SequenceDataWriter<?> untypedDataWriter;
      try {
        untypedDataWriter = factory.createDataWriter();
      } catch (IOException e) {
        throw new ResourceInitializationException(e);
      }
      InitializableFactory.initialize(untypedDataWriter, context);
      this.dataWriter = ReflectionUtil.uncheckedCast(untypedDataWriter);
    } else {
      // create the factory and instantiate the classifier
      SequenceClassifierFactory<?> factory = InitializableFactory.create(
          context,
          classifierFactoryClassName,
          SequenceClassifierFactory.class);
      SequenceClassifier<?> untypedClassifier;
      try {
        untypedClassifier = factory.createClassifier();
      } catch (IOException e) {
        throw new ResourceInitializationException(e);
      } catch (CleartkException e) {
        throw new ResourceInitializationException(e);
      }

      this.classifier = ReflectionUtil.uncheckedCast(untypedClassifier);

      ReflectionUtil.checkTypeParameterIsAssignable(
          CleartkSequenceAnnotator.class,
          "OUTCOME_TYPE",
          this,
          SequenceClassifier.class,
          "OUTCOME_TYPE",
          this.classifier);

      InitializableFactory.initialize(untypedClassifier, context);
    }
  }

  @Override
  public void collectionProcessComplete() throws AnalysisEngineProcessException {
    super.collectionProcessComplete();

    if (isTraining()) {
      try {
        dataWriter.finish();
      } catch (CleartkException ctke) {
        throw new AnalysisEngineProcessException(ctke);
      }
    }
  }

  protected boolean isTraining() {
    return dataWriter != null ? true : false;

  }

  protected List<OUTCOME_TYPE> classify(List<Instance<OUTCOME_TYPE>> instances)
      throws CleartkException {
    List<List<Feature>> instanceFeatures = new ArrayList<List<Feature>>();
    for (Instance<OUTCOME_TYPE> instance : instances) {
      instanceFeatures.add(instance.getFeatures());
    }
    return this.classifier.classify(instanceFeatures);
  }

}
