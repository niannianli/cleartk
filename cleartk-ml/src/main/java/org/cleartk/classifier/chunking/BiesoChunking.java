/* 
 * Copyright (c) 2013, Regents of the University of Colorado 
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
package org.cleartk.classifier.chunking;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.cas.Feature;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

/**
 * Chunking with Begin-Inside-End-Singleton-Outside labels.
 * 
 * Also known as BILOU (Begin-Inside-Last-Outside-Unit) and IOBES
 * (Inside-Outside-Begin-End-Singleton).
 * 
 * <br>
 * Copyright (c) 2013, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Alexey V Romanov
 * @author Steven Bethard
 */
public class BiesoChunking<SUB_CHUNK_TYPE extends Annotation, CHUNK_TYPE extends Annotation>
    extends Chunking_ImplBase<SUB_CHUNK_TYPE, CHUNK_TYPE> {

  public BiesoChunking(
      Class<? extends SUB_CHUNK_TYPE> subChunkClass,
      Class<? extends CHUNK_TYPE> chunkClass) {
    super(subChunkClass, chunkClass, null);
  }

  public BiesoChunking(
      Class<? extends SUB_CHUNK_TYPE> subChunkClass,
      Class<? extends CHUNK_TYPE> chunkClass,
      String featureName) {
    super(subChunkClass, chunkClass, featureName);
  }

  @Override
  protected Map<SUB_CHUNK_TYPE, String> getSubChunkToOutcomeMap(
      JCas jCas,
      List<SUB_CHUNK_TYPE> chunkComponents,
      List<CHUNK_TYPE> chunks) {
    Feature feature = this.getFeature(jCas);
    Map<SUB_CHUNK_TYPE, String> subChunkToOutcome = new HashMap<SUB_CHUNK_TYPE, String>();
    for (CHUNK_TYPE chunk : chunks) {
      String suffix = this.getOutcomeSuffix(chunk, feature);
      List<? extends SUB_CHUNK_TYPE> subChunks = JCasUtil.selectCovered(this.subChunkClass, chunk);
      int nSubChunks = subChunks.size();
      if (nSubChunks == 1) {
        subChunkToOutcome.put(subChunks.get(0), "S" + suffix);
      } else {
        for (int i = 0; i < nSubChunks; ++i) {
          SUB_CHUNK_TYPE subChunk = subChunks.get(i);
          if (i == 0) {
            subChunkToOutcome.put(subChunk, "B" + suffix);
          } else if (i == nSubChunks - 1) {
            subChunkToOutcome.put(subChunk, "E" + suffix);
          } else {
            subChunkToOutcome.put(subChunk, "I" + suffix);
          }
        }
      }
    }
    return subChunkToOutcome;
  }

  @Override
  protected boolean isEndOfChunk(
      char currPrefix,
      String currLabel,
      char nextPrefix,
      String nextLabel) {
    return currPrefix == 'E' || currPrefix == 'S' || nextPrefix == 'O' || nextPrefix == 'B'
        || !nextLabel.equals(currLabel);
  }
}
