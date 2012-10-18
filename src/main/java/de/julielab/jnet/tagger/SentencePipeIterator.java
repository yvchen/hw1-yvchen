/** 
 * SentencePipeIterator.java
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
 * Creation date: Dec 7, 2006 
 * 
 * this is a PipeInputIterator that iterates over Sentence objects
 * and is used to fill an InstanceList.
 * 
 * As input, this iterator expects an ArrayList of Sentence object. For each
 * of these objects, the iterator creates an Instance (the data field is a Sentence object).
 **/

package de.julielab.jnet.tagger;

import java.util.ArrayList;
import java.util.Iterator;

import edu.umass.cs.mallet.base.pipe.iterator.AbstractPipeInputIterator;
import edu.umass.cs.mallet.base.types.Instance;

public class SentencePipeIterator extends AbstractPipeInputIterator {

	private Iterator sentIterator;

	public SentencePipeIterator(ArrayList<Sentence> sentences) {
		this.sentIterator = sentences.iterator();
	}

	public boolean hasNext() {
		return sentIterator.hasNext();
	}

	public Instance nextInstance() {
		Sentence sent = (Sentence) sentIterator.next();

		Instance inst = new Instance(sent, "", "", "", null);
		return inst;
	}
}