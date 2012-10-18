/** 
 * Sentence.java
 * 
 * Copyright (c) 2006, JULIE Lab. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 *
 * Author: faessler
 * 
 * Current version: 1.4 	
 * Since version:   1.0
 *
 * Creation date: Nov 3, 2006 
 * 
 * This class is a kind of typedef for "ArrayList<Unit>". It is used for improved readability
 * of the source code that is, to avoid things like ArrayList<ArrayList<Unit>>.
 **/

package de.julielab.jnet.tagger;

import java.util.ArrayList;

public class Sentence {

	ArrayList<Unit> sentence;

	public Sentence(ArrayList<Unit> sentence) {
		this.sentence = new ArrayList<Unit>(sentence);
	}

	public Sentence() {
		this.sentence = new ArrayList<Unit>();
	}

	public void add(Unit unit) {
		sentence.add(unit);
	}

	public Unit get(int i) {
		return sentence.get(i);
	}

	public ArrayList<Unit> getUnits() {
		return sentence;
	}

	public int size() {
		return sentence.size();
	}

	public String toString() {
		return sentence.toString();
	}
}
