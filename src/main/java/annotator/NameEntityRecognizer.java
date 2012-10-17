package annotator;

import java.util.Map;

import javax.swing.text.html.HTMLDocument.Iterator;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JFSIndexRepository;
import org.apache.uima.resource.ResourceInitializationException;

import util.NameEntity;
import util.Sentence;

public class NameEntityRecognizer extends JCasAnnotator_ImplBase {

  @Override
  public void process(JCas jcas) throws AnalysisEngineProcessException {
    
    PosTagNamedEntityRecognizer NER = null;
    try {
      NER = new PosTagNamedEntityRecognizer();
    } catch (ResourceInitializationException e) {
      e.printStackTrace();
    }
    JFSIndexRepository idx = jcas.getJFSIndexRepository();
    AnnotationIndex sentenceAnnotIndex = idx.getAnnotationIndex(Sentence.typeIndexID);
    FSIterator it = sentenceAnnotIndex.iterator();
    
    while(it.hasNext()){
            Sentence sent = (Sentence) it.next();
            String text = sent.getText();
            Map<Integer, Integer> res = NER.getGeneSpans(text);
            NameEntityAnnotator(res, jcas, sent);
    }
    
  }

  private void NameEntityAnnotator(Map<Integer, Integer> res, JCas jcas, Sentence sent) {
  
    for(Map.Entry<Integer, Integer> entry : res.entrySet()){
      
      Integer begin = entry.getKey();
      Integer end = entry.getValue();      
      NameEntity NE = new NameEntity(jcas, begin, end);
    
      NE.setBegin((int) begin);
      NE.setEnd((int) end);
      NE.setText(sent.getText().substring((int)begin, (int)end));
      NE.setSent(sent);
      NE.addToIndexes();
    }
  }
}
