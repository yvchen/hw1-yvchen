package annotator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.uima.resource.ResourceInitializationException;

import de.julielab.jnet.tagger.Unit;

import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.ValueAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.WordFormAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class PosTagNamedEntityRecognizer {

  private StanfordCoreNLP pipeline;

  public PosTagNamedEntityRecognizer() throws ResourceInitializationException {
    Properties props = new Properties();
    props.put("annotators", "tokenize, ssplit, pos");
    pipeline = new StanfordCoreNLP(props);
  }

  public ArrayList<Unit> toUnitList(String text) {
    ArrayList<Unit> tokens = new ArrayList<Unit>();
    Annotation sentence = new Annotation(text);
    pipeline.annotate(sentence);
    for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
      Unit nowunit = new Unit(token.beginPosition(), token.endPosition(), token.get(ValueAnnotation.class), "X");
      tokens.add(nowunit);
    }    
    return tokens;
  }
  
  public ArrayList<Unit> getTokeList(String nowSent){
	  
	  ArrayList<Unit> result = new ArrayList<Unit>();
	  Annotation nowSentAnnotation = new Annotation(nowSent);
	  pipeline.annotate(nowSentAnnotation);
	  for (CoreLabel token : nowSentAnnotation.get(TokensAnnotation.class)) {
		  String nowLabel = "X";
	      String nowWord = token.get(ValueAnnotation.class);
	      Unit nowUnit = new Unit(token.beginPosition(), token.endPosition(), nowWord, nowLabel);
	      result.add(nowUnit);
	  }
	  return result;
	  
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
}
