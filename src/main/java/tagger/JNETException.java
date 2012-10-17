/** 
 * JNETExcpeption.java
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
 * Default Exception for JNET.
 **/

package tagger;

public class JNETException extends Exception {

	final static long serialVersionUID = 23;

	public JNETException() {
		super();
	}

	public JNETException(String s) {
		super(s);

	}
}
