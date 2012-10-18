/** 
 * FeatureConfigExchanger.java
 * 
 * Copyright (c) 2007, JULIE Lab. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 *
 * Author: tomanek
 * 
 * Current version: 1.5 	
 * Since version:   1.5
 *
 * Creation date: Sep 27, 2007 
 * 
 * class to exchange the feature config of an already saved model
 **/

package de.julielab.jnet.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import de.julielab.jnet.tagger.NETagger;

public class FeatureConfigExchanger {

	public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException {
		if (args.length!=3) {
			System.err.println("Usage: FeatureConfigExchanger <model file> <new model file> <new feature config file>");
			System.exit(-1);
		}
		String orgModelName = args[0];
		String newModelName = args[1];
		String newFeatureConfig = args[2];
		NETagger tagger = new NETagger();
		tagger.readModel(orgModelName);
		Properties featureConfig = new Properties();
		featureConfig.load(new FileInputStream(newFeatureConfig));
		tagger.setFeatureConfig(featureConfig);
		tagger.writeModel(newModelName);
		
		System.out.println("wrote model with new feature config to: " + newModelName);
	}
}
