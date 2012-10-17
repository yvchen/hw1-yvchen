/** 
 * BasePipe.java
 * 
 * Copyright (c) 2006, JULIE Lab. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 *
 * Author: tomanek
 * 
 * Current version: 1.4	
 * Since version:   0.1
 *
 * Creation date: Nov 1, 2006 
 * 
 * This is the BasePipe to be used in the SerialPipe for
 * feature extraction.
 * 
 * As input, it expects the data field to be filled with a Sentence object. All the 
 * other fields (source, target, name) are ignored and/or overwritten.
 **/

package tagger;

import java.util.*;
import util.Utils;
import com.uea.stemmer.UEALite;
import com.uea.stemmer.Word;
import edu.umass.cs.mallet.base.pipe.Pipe;
import edu.umass.cs.mallet.base.types.Instance;
import edu.umass.cs.mallet.base.types.LabelAlphabet;
import edu.umass.cs.mallet.base.types.LabelSequence;
import edu.umass.cs.mallet.base.types.Token;
import edu.umass.cs.mallet.base.types.TokenSequence;

class BasePipe extends Pipe {

	private static final long serialVersionUID = 1L;

	UEALite stemmer;

	Properties featureConfig;

	boolean doStemming;

	public BasePipe(Properties featureConfig) {
		super(null, LabelAlphabet.class);
		stemmer = new UEALite();
		doStemming = featureConfig.getProperty("stemming_enabled").equals(
				"true");
		this.featureConfig = featureConfig;
	}

	public Instance pipe(Instance carrier) {

		Sentence sentence = (Sentence) carrier.getData();
		ArrayList<Unit> sentenceUnits = sentence.getUnits();

		StringBuffer source = new StringBuffer();
		TokenSequence data = new TokenSequence(sentenceUnits.size());
		LabelSequence target = new LabelSequence(
				(LabelAlphabet) getTargetAlphabet(), sentenceUnits.size());

		String word, meta, wc, bwc;
		String[] trueMetas = Utils.getTrueMetas(featureConfig);

		try {
			for (int i = 0; i < sentenceUnits.size(); i++) {

				word = sentenceUnits.get(i).getRep();

				if (doStemming) {
					// here we do conservative stemming...
					word = ((Word) stemmer.stem(word)).getWord();
				}

				// GENERATE THE FEATURES
				Token token = new Token(word);

				// word
				token.setFeatureValue("W=" + word, 1);

				// meta tags
				for (String key : trueMetas) {
					String metaName = featureConfig.getProperty(key
							+ "_feat_unit");
					if ((meta = sentenceUnits.get(i).getMetaInfo(metaName)) != null) {
						token.setFeatureValue(metaName + "=" + meta, 1);
					}
				}
				token.setText(word);

				// word class

				if (featureConfig.getProperty("feat_wc_enabled").equals("true")) {
					wc = word;
					wc = wc.replaceAll("[A-Z]", "A");
					wc = wc.replaceAll("[a-z]", "a");
					wc = wc.replaceAll("[0-9]", "0");
					wc = wc.replaceAll("[^A-Za-z0-9]", "x");

					token.setFeatureValue("WC=" + wc, 1);
				}

				if (featureConfig.getProperty("feat_bwc_enabled")
						.equals("true")) {
					bwc = word;
					bwc = bwc.replaceAll("[A-Z]+", "A");
					bwc = bwc.replaceAll("[a-z]+", "a");
					bwc = bwc.replaceAll("[0-9]+", "0");
					bwc = bwc.replaceAll("[^A-Za-z0-9]+", "x");

					token.setFeatureValue("BWC=" + bwc, 1);
				}

				source.append(token.getText());
				source.append(" ");

				data.add(token);
				target.add(sentenceUnits.get(i).getLabel());
			}

			// produce an error, if a label was not found
			// this has to be done due to an insuffency in
			// the mallet code
			if (target.size() != data.size()) {
				throw new JNETException(
						"Label not found... check your label definition file.");
			}

			carrier.setData(data);
			carrier.setTarget(target);
			carrier.setSource(source);
			return carrier;

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}

}
