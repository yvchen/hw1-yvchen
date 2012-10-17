/** 
 * FeatureGenerator.java
 * 
 * Copyright (c) 2006, JULIE Lab. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 *
 * Author: tomanek
 * 
 * Current version: 1.4 	
 * Since version:   1.0
 *
 * Creation date: Nov 1, 2006 
 * 
 * Generating features for given text. Text is given as an ArrayList of Sentence objects that is,
 * an ArrayList of ArrayLists of Unit objects.
 **/

package tagger;

import java.util.ArrayList;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.umass.cs.mallet.base.pipe.Pipe;
import edu.umass.cs.mallet.base.pipe.SerialPipes;
import edu.umass.cs.mallet.base.pipe.TokenSequence2FeatureVectorSequence;
import edu.umass.cs.mallet.base.pipe.tsf.OffsetConjunctions;
import edu.umass.cs.mallet.base.pipe.tsf.RegexMatches;
import edu.umass.cs.mallet.base.pipe.tsf.TokenTextCharPrefix;
import edu.umass.cs.mallet.base.pipe.tsf.TokenTextCharSuffix;
import edu.umass.cs.mallet.base.types.InstanceList;
import edu.umass.cs.mallet.base.types.LabelAlphabet;

class FeatureGenerator {

	static String GREEK = "(alpha|beta|gamma|delta|epsilon|zeta|eta|theta|iota|kappa|lambda|mu|nu|xi|omicron|pi|rho|sigma|tau|upsilon|phi|chi|psi|omega)";

	// all upper case letters (consider different languages, too)
	static String CAPS = "A-Z????????????????";

	// all lower case letters (consider different languages, too)
	static String LOW = "a-z?èìòùáéíóúçñïäöü";

	static public InstanceList createFeatureData(ArrayList<Sentence> sentences,
			LabelAlphabet dict, Properties featureConfig) {

		ArrayList<Pipe> pipeParam = new ArrayList<Pipe>();

		pipeParam.add(new BasePipe(featureConfig));
		pipeParam.add(new TokenTextCharPrefix("PREFIX=", 3));
		pipeParam.add(new TokenTextCharSuffix("SUFFIX=", 3));
		pipeParam.add(new RegexMatches("INITCAPS", Pattern.compile("[" + CAPS
				+ "].*")));
		pipeParam.add(new RegexMatches("INITCAPSALPHA", Pattern.compile("["
				+ CAPS + "][" + LOW + "].*")));
		pipeParam.add(new RegexMatches("ALLCAPS", Pattern.compile("[" + CAPS
				+ "]+")));
		pipeParam.add(new RegexMatches("CAPSMIX", Pattern.compile("[" + CAPS
				+ LOW + "]+")));
		pipeParam
				.add(new RegexMatches("HASDIGIT", Pattern.compile(".*[0-9].*")));
		pipeParam
				.add(new RegexMatches("SINGLEDIGIT", Pattern.compile("[0-9]")));
		pipeParam.add(new RegexMatches("DOUBLEDIGIT", Pattern
				.compile("[0-9][0-9]")));
		pipeParam.add(new RegexMatches("NATURALNUMBER", Pattern
				.compile("[0-9]+")));
		pipeParam.add(new RegexMatches("REALNUMBER", Pattern
				.compile("[-0-9]+[.,]+[0-9.,]+")));
		pipeParam.add(new RegexMatches("HASDASH", Pattern.compile(".*-.*")));
		pipeParam.add(new RegexMatches("INITDASH", Pattern.compile("-.*")));
		pipeParam.add(new RegexMatches("ENDDASH", Pattern.compile(".*-")));
		pipeParam.add(new RegexMatches("ALPHANUMERIC", Pattern.compile(".*["
				+ CAPS + LOW + "].*[0-9].*")));
		pipeParam.add(new RegexMatches("ALPHANUMERIC", Pattern
				.compile(".*[0-9].*[" + CAPS + LOW + "].*")));

		if (featureConfig.getProperty("feat_bioregexp_enabled").equals("true")) {
			pipeParam.add(new RegexMatches("ROMAN", Pattern
					.compile("[IVXDLCM]+")));
			pipeParam.add(new RegexMatches("HASROMAN", Pattern
					.compile(".*\\b[IVXDLCM]+\\b.*")));
			pipeParam.add(new RegexMatches("GREEK", Pattern.compile(GREEK)));
			pipeParam.add(new RegexMatches("HASGREEK", Pattern.compile(".*\\b"
					+ GREEK + "\\b.*")));
		}

		pipeParam.add(new RegexMatches("PUNCTUATION", Pattern
				.compile("[,.;:?!-+]")));
		pipeParam.add(new OffsetConjunctions(offsetConjFromConfig(featureConfig
				.getProperty("offset_conjunctions"))));
		// un-comment this for printing out the generated features
		// pipeParam.add( new PrintTokenSequenceFeatures() );
		pipeParam.add(new TokenSequence2FeatureVectorSequence(true, true));

		Pipe[] pipeParamArray = new Pipe[pipeParam.size()];
		pipeParam.toArray(pipeParamArray);
		Pipe myPipe = new SerialPipes(pipeParamArray);

		myPipe.setTargetAlphabet(dict);

		InstanceList data = new InstanceList(myPipe);

		data.add(new SentencePipeIterator(sentences));
		return data;
	}

	/**
	 * extracts the offset conjunction information (feature creation horizon)
	 * from a String of the form (-1) (0) (1), (-1) (0) (1,2) or (-1) (0) (1 2)
	 */
	static int[][] offsetConjFromConfig(String offset_conjunctions) {
		int[][] conjunctions;

		// to find round brackets with digits sperated with whitespaces
		Pattern inBrackets = Pattern.compile("\\([-\\d\\s,]+\\)");
		Matcher bracketMatcher = inBrackets.matcher(offset_conjunctions);

		ArrayList<String> bracketContents = new ArrayList<String>();
		int bracketStart, bracketEnd;
		int pos = 0;

		// get all brackets and write their content in the ArrayList
		while (bracketMatcher.find(pos)) {
			bracketStart = bracketMatcher.start() + 1;
			bracketEnd = bracketMatcher.end() - 1;
			bracketContents.add(offset_conjunctions.substring(bracketStart,
					bracketEnd));
			pos = bracketMatcher.end();
		}
		// create an array for each bracket and fill it with the digits in the
		// bracket
		conjunctions = new int[bracketContents.size()][];
		for (int i = 0; i < bracketContents.size(); i++) {
			String[] digits = bracketContents.get(i).split(",");
			conjunctions[i] = new int[digits.length]; // array creation
			for (int j = 0; j < digits.length; j++) {
				conjunctions[i][j] = Integer.parseInt(digits[j].trim()); // filling
			}
		}
		return conjunctions;
	}

}
