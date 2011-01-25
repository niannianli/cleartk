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

package org.cleartk.examples.documentclassification;

import java.io.File;
import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.examples.ExampleComponents;
import org.cleartk.syntax.opennlp.SentenceAnnotator;
import org.cleartk.token.stem.snowball.DefaultSnowballStemmer;
import org.cleartk.token.tokenizer.TokenAnnotator;
import org.cleartk.util.cr.FilesCollectionReader;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.pipeline.SimplePipeline;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 * 
 */

public class Step1BuildIDFMap {

  public static class Args {
    @Option(name = "-d", aliases = "--documentDirectory", usage = "specify the directory containing the training documents")
    public String documentDirectory = "../ClearTK Data/data/20newsgroups/20news-bydate-train/";

    @Option(name = "-map", aliases = "--idfmapFileName", usage = "specify the directory name where the IDFMap file will be written")
    public String idfmapDirectoryName = "example/documentclassification";

    public static Args parseArguments(String[] stringArgs) {
      Args args = new Args();
      CmdLineParser parser = new CmdLineParser(args);
      try {
        parser.parseArgument(stringArgs);
      } catch (CmdLineException e) {
        e.printStackTrace();
        parser.printUsage(System.err);
        System.exit(1);
      }
      return args;
    }
  }

  public static void main(String[] stringArgs) throws UIMAException, IOException {

    Args args = Args.parseArguments(stringArgs);
    String documentDirectory = args.documentDirectory;
    File idfmapDirectory = new File(args.idfmapDirectoryName);
    if (!idfmapDirectory.exists()) {
      idfmapDirectory.mkdirs();
    }

    AnalysisEngineDescription documentClassificationAnnotatorDescription = AnalysisEngineFactory
        .createPrimitiveDescription(
            DocumentClassificationAnnotator.class,
            ExampleComponents.TYPE_SYSTEM_DESCRIPTION,
            CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
            IDFMapWriterFactory.class.getName(),
            IDFMapWriterFactory.PARAM_OUTPUT_DIRECTORY,
            idfmapDirectory.getPath());

    SimplePipeline.runPipeline(
        FilesCollectionReader.getCollectionReader(
            ExampleComponents.TYPE_SYSTEM_DESCRIPTION,
            documentDirectory),
        SentenceAnnotator.getDescription(),
        TokenAnnotator.getDescription(),
        DefaultSnowballStemmer.getDescription("English"),
        AnalysisEngineFactory.createPrimitiveDescription(
            GoldAnnotator.class,
            ExampleComponents.TYPE_SYSTEM_DESCRIPTION),
        documentClassificationAnnotatorDescription);

    System.out.println("IDF map file written to: " + idfmapDirectory.getPath());
  }
}
