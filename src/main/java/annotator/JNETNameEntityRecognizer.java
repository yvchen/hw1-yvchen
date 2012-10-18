package annotator;
import java.util.*;
import org.apache.uima.resource.ResourceInitializationException;

import de.julielab.jnet.tagger.Unit;

import util.Sentence;

import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.ValueAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

/*
 * JNETNameEntity creates the name entity candidate list from a sentence for tagging.
 * Name entity list is a unit list, and unit includes the text and the initial label.
 * Initial label is "X".  
 */
public class JNETNameEntityRecognizer {

  private StanfordCoreNLP pipeline;

  public JNETNameEntityRecognizer() throws ResourceInitializationException {
    Properties props = new Properties();
    props.put("annotators", "tokenize, ssplit, pos");
    pipeline = new StanfordCoreNLP(props);
  }

  public Map<Integer, Integer> getGeneSpans(String text) {
    Map<Integer, Integer> begin2end = new HashMap<Integer, Integer>();
    Annotation document = new Annotation(text);
    pipeline.annotate(document);
    List<CoreMap> sentences = document.get(SentencesAnnotation.class);
    for (CoreMap sentence : sentences) {
      List<CoreLabel> candidate = new ArrayList<CoreLabel>();
      for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
        String pos = token.get(PartOfSpeechAnnotation.class);
        if (pos.startsWith("NN")) {
          candidate.add(token);
        } else if (candidate.size() > 0) {
          int begin = candidate.get(0).beginPosition();
          int end = candidate.get(candidate.size() - 1).endPosition();
          begin2end.put(begin, end);
          candidate.clear();
        }
      }
      if (candidate.size() > 0) {
        int begin = candidate.get(0).beginPosition();
        int end = candidate.get(candidate.size() - 1).endPosition();
        begin2end.put(begin, end);
        candidate.clear();
      }
    }
    return begin2end;
  }

  public ArrayList<Unit> toUnitList(String text) {
    ArrayList<Unit> tokens = new ArrayList<Unit>();
    /*
    String[] words = text.trim().split("[\\s]+");
    int offset = 0;
    for(String word : words){
      int begin = text.indexOf(word, offset);
      offset += word.length();
      Unit unit = new Unit(begin, begin + word.length(), word, "X");
      tokens.add(unit);
    }*/
    Annotation sentence = new Annotation(text);
    pipeline.annotate(sentence);
    int offset = 0;
    for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
      String word = token.get(ValueAnnotation.class);      
      int begin = text.indexOf(word, offset);
      if(begin != -1){
        Unit unit = new Unit(begin, begin + word.length(), word, "X");
        offset += word.length();
        tokens.add(unit);
      }
      
    }
    return tokens;
  }
}
