package reader;

import java.io.*;
import java.util.*;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;

import util.Sentence;

/*
 * CollectionReader reads the input file and separates different sentences by sentence id.
 * It saves each sentence as a JCAS then make it enter Analysis Engine. 
 */
public class CollectionReader extends CollectionReader_ImplBase {
  
  private File fin;
  private String text;
  private BufferedReader buffer;
  private String encoding = "UTF-8";
  
  public void initialize() throws ResourceInitializationException {
    
    fin = new File(((String)getConfigParameterValue("InputFile")).trim());
    text = "";
    try {
            buffer = new BufferedReader(new InputStreamReader(new FileInputStream(fin), encoding));
    } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
    } catch (FileNotFoundException e) {
            e.printStackTrace();
    }
  }
  
  @Override
  public void getNext(CAS arg0) throws IOException, CollectionException {

    int space = text.indexOf(" ");
    String id = text.substring(0, space);
    String sent = text.substring(space + 1).trim();
    
    JCas jcas = null;
    try {
      jcas = arg0.getJCas();
    } catch (CASException e) {
      e.printStackTrace();
    }
    
    // generate a jcas, which includes id and text
    Sentence s = new Sentence(jcas, 0, sent.length());
    s.setId(id);
    s.setText(sent);
    s.addToIndexes();
  }

  @Override
  public void close() throws IOException {
    // TODO Auto-generated method stub
    buffer.close();
  }

  @Override
  public Progress[] getProgress() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean hasNext() throws IOException, CollectionException {
    // read next line if not end-of-file
    text = buffer.readLine();
    if(text != null){
      return true;
    }
    return false;
  }

}
