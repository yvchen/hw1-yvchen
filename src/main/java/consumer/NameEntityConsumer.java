package consumer;

import java.io.*;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JFSIndexRepository;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;

import util.NameEntity;
import util.Sentence;

public class NameEntityConsumer extends CasConsumer_ImplBase {
  
  private File fout;
  private String encoding = "UTF-8";
  private BufferedWriter buffer = null;
  
  public void initialize() throws ResourceInitializationException {
    fout = new File(((String)getConfigParameterValue("OutputFile")).trim());
    
    try {
      buffer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fout, false), encoding));
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }
  
  @Override
  public void processCas(CAS cas) throws ResourceProcessException {
    JCas jcas = null;
      try {
        jcas = cas.getJCas();
      } catch (CASException e) {
        e.printStackTrace();
      }
      JFSIndexRepository idx = jcas.getJFSIndexRepository();
      AnnotationIndex tokenAnnotIndex = idx.getAnnotationIndex(NameEntity.typeIndexID);
      FSIterator it = tokenAnnotIndex.iterator();
      
      while(it.hasNext()){
              NameEntity NE = (NameEntity) it.next();
              Sentence sent = NE.getSent();
              try {
                buffer.write(sent.getId());
                buffer.write("|");
                buffer.write(Integer.toString(NE.getBegin()));
                buffer.write(" ");
                buffer.write(Integer.toString(NE.getEnd()));
                buffer.write("|");
                buffer.write(NE.getText());
                buffer.newLine();
              } catch (IOException e) {
                e.printStackTrace();
              }
      }
  }
  
  public void finalize() throws IOException {
    buffer.close();
  }
}
