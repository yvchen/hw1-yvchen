package consumer;

import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JFSIndexRepository;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;

import util.NameEntity;
import util.Sentence;
/*
 * NameEntityConsumer receives a lot of CAS, one of which is a term labeled with gene name entity or out-domain term.
 * It converts them into real strings and maintains their indexes (begin and end) for output format.  
 */
public class NameEntityConsumer extends CasConsumer_ImplBase {
  
  private File fout;
  private String encoding = "UTF-8";
  private BufferedWriter buffer;
  
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
      
      String candidate_text = null;
      String candidate_id = null;
      int candidate_begin = -1;
      int candidate_end = -1;
      int flag = 0;

      NameEntity NE;
      Hashtable<Integer, Integer> map = new Hashtable<Integer, Integer>();
      Hashtable<Integer, Integer> control = new Hashtable<Integer, Integer>();
      
      while(it.hasNext()){
        NE = (NameEntity) it.next();        
        Sentence sent = NE.getSent();
        if(flag == 0){
          int now = 0;
          int i;
          for(i = 0; i < sent.getText().length(); i ++){           
            map.put(i, now);          
            if(!sent.getText().substring(i, i + 1).equals(" "))
              now ++;                     
          }
          map.put(i, now);
          candidate_id = sent.getId();
          candidate_text = NE.getText();
          candidate_begin = NE.getBegin();
          candidate_end = NE.getEnd() - 1;
          flag = 1;
        }
        else{
          if(map.get(NE.getBegin()) == map.get(candidate_end) + 1){
            candidate_end = NE.getEnd() - 1;
            candidate_text += " " + NE.getText();
            }
          else{
            try {
              if(!control.containsKey(map.get(candidate_begin))){
              String output = candidate_id + "|" + Integer.toString(map.get(candidate_begin)) + " " + Integer.toString(map.get(candidate_end)) + "|" + candidate_text;
              System.out.println(output);
              buffer.write(output);
              buffer.newLine();
              for(int i = map.get(candidate_begin); i < map.get(candidate_end); i ++)
                control.put(i, map.get(candidate_end));
              }
              candidate_id = sent.getId();
              candidate_text = NE.getText();
              candidate_begin = NE.getBegin();
              candidate_end = NE.getEnd() - 1;
              } catch (IOException e) {
                e.printStackTrace();
                }
            }
          }
      }
      if(flag == 1){
        try {
          if(!control.containsKey(map.get(candidate_begin))){
            
          String output = candidate_id + "|" + map.get(candidate_begin).toString() + " " + map.get(candidate_end).toString() + "|" + candidate_text;
          buffer.write(output);
          buffer.newLine();
          for(int i = map.get(candidate_begin); i < map.get(candidate_end); i ++)
            control.put(i, map.get(candidate_end));

          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
  }
  
  public void collectionProcessComplete(ProcessTrace aTrace) throws ResourceProcessException, IOException{
    try {
      System.out.println("Finish writing...");
      buffer.close();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        }
    }
}
