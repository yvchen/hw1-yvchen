package annotator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.html.HTMLDocument.Iterator;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JFSIndexRepository;
import org.apache.uima.resource.ResourceInitializationException;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.dict.ExactDictionaryChunker;
import com.aliasi.util.AbstractExternalizable;

import de.julielab.jnet.tagger.JNETException;
import de.julielab.jnet.tagger.NETagger;
import de.julielab.jnet.tagger.Unit;

import util.NameEntity;
import util.Sentence;
/*
 * NameEntityRecognizer receives JCAS object, which is a sentence from input file.
 * It converts the object into the sentence string and calls JNETNameEntityRecognizer to tokenizes the sentence to generate a unit list.
 * Each unit includes the text and the name entity label.
 */
public class NameEntityRecognizer extends JCasAnnotator_ImplBase {  

  File configFile = new File("src/main/resources/defaultFeatureConf.conf");
  File modelFile = new File("src/main/resources/models/pennbio_genes.mod.gz");
  File LinpipeModel = new File("src/main/resources/models/ne-en-bio-genetag.HmmChunker");
  NETagger tagger;
  Chunker chunker;
  
  @Override
  public void process(JCas jcas) throws AnalysisEngineProcessException {
    try {
      JNETNameEntityRecognizer NER = new JNETNameEntityRecognizer();
      JFSIndexRepository idx = jcas.getJFSIndexRepository();
      AnnotationIndex sentenceAnnotIndex = idx.getAnnotationIndex(Sentence.typeIndexID);
      FSIterator it = sentenceAnnotIndex.iterator();
      
      
      while(it.hasNext()){        
              Sentence sent = (Sentence) it.next();
              String text = sent.getText();
              ArrayList<Unit> res = NER.toUnitList(text);
              res = tagger.predictNER(new de.julielab.jnet.tagger.Sentence(res));
              //Map<Integer, Integer> res = NER.getGeneSpans(text);
              NameEntityAnnotator(res, jcas, sent);
      }

    } catch (ResourceInitializationException e) {
      e.printStackTrace();
    } catch (JNETException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
 }

  private void NameEntityAnnotator(ArrayList<Unit> res, JCas jcas, Sentence sent) {
    Chunking chunking = chunker.chunk(sent.getText());

    for (Chunk chunk : chunking.chunkSet()) {
      int start = chunk.start();
      int end = chunk.end();
      
      String phrase = sent.getText().substring(start,end);
      Unit unit = new Unit(start, end, phrase, "LingPipe");
      res.add(unit);
    }
    
    for(Unit unit : res){
      String text = unit.getRep();      
      // set Acronym
/*      Pattern pattern = Pattern.compile("[A-Z\\-]+");
      Matcher matcher = pattern.matcher(text);
      if(matcher.matches() && text.length() >= 3 && text.length() <= 5){
        unit.setLabel("Acronym");
      }*/      
      String label = unit.getLabel();
      if (!label.equals("O")) {
        NameEntity NE = new NameEntity(jcas, unit.begin, unit.end);
        NE.setText(text);
        NE.setSent(sent);
        NE.setLabel(label);
        NE.addToIndexes();
      }
    }
  }
  
  public void initialize(UimaContext aContext) throws ResourceInitializationException{
    
    super.initialize(aContext);
    
    try {
      chunker = (Chunker) AbstractExternalizable.readObject(LinpipeModel);
      tagger = new NETagger(configFile);
      tagger.readModel(modelFile.getAbsolutePath());
    } catch (ClassNotFoundException e1) {
      e1.printStackTrace();
    } catch (IOException e1) {
      e1.printStackTrace();
    }    
  }
}
