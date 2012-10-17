/** 
 * FeatureSubsetModel.java
 * 
 * Copyright (c) 2007, JULIE Lab. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 *
 * Author: faessler
 * 
 * Current version: 1.4
 * Since version: 1.0
 *
 * Creation date: 29.01.2007 
 * 
 * This class contains a CRF4 model as well as a properties object "featureConfig". The featureConfig
 * object contains information about the feature subset used with this CRF4 model.
 **/

package tagger;

import java.io.Serializable;
import java.util.Properties;

import edu.umass.cs.mallet.base.fst.CRF4;

public class FeatureSubsetModel implements Serializable {

	private CRF4 model;

	private Properties featureConfig;

	static final long serialVersionUID = 23; // used since V 1.3

	FeatureSubsetModel() {
		this.model = null;
		this.featureConfig = null;
	}

	FeatureSubsetModel(CRF4 model, Properties featureConfig) {
		this.model = model;
		this.featureConfig = featureConfig;
	}

	void setModel(CRF4 model) {
		this.model = model;
	}

	void setFeatureConfig(Properties featureConfig) {
		this.featureConfig = featureConfig;
	}

	CRF4 getModel() {
		return this.model;
	}

	Properties getFeatureConfig() {
		return this.featureConfig;
	}
}
