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
 * Create the PPD format from several input files.
 * 
 **/

package de.julielab.jnet.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeSet;


public class FormatConverter {

	public static void main(String[] args) {
		try {

			if (args.length < 3) {
				System.out
						.println("usage: java FormatConverter <iobFile> <posFile> [further meta data files] <outFile> <taglist (or 0 if not used)>");
				System.exit(0);
			}

			File iobFile = new File(args[0]);
			ArrayList<File> metaDataFiles = new ArrayList<File>();
			for (int i = 1; i < args.length - 2; i++) {
				metaDataFiles.add(new File(args[i]));
			}
			File outFile = new File(args[args.length - 2]);

			System.out.println("Reading iob and meta data files...");
			ArrayList<String> iobData = Utils.readFile(iobFile);
			ArrayList<ArrayList<String>> metaData = new ArrayList<ArrayList<String>>();
			for (int i = 0; i < metaDataFiles.size(); i++) {
				metaData.add(Utils.readFile(metaDataFiles.get(i)));
			}

			TreeSet<String> tagList = null;
			if (!args[args.length - 1].equals("0")) {
				tagList = new TreeSet<String>(Utils.readFile(new File(
						args[args.length - 1])));
			}

			// make piped format
			System.out.println("Making piped format...");
			ArrayList<String> pipedData = makePipedFormat(iobData, metaData,
					tagList);
			Utils.writeFile(outFile, pipedData);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * converts a IOB and a POS file into one file in piped format
	 */
	public static ArrayList<String> makePipedFormat(ArrayList<String> iobData,
			ArrayList<ArrayList<String>> metaData, TreeSet<String> tags) {

		boolean checkTags = true;
		if (tags == null) {
			// ignore tags check
			checkTags = false;
		}

		for (int i = 0; i < metaData.size(); i++) {
			if (iobData.size() != metaData.get(i).size()) {
				System.err.println("Error: IOB file and " + (i + 1)
						+ ". meta data file have different length!");
				System.exit(-1);
			}
		}

		ArrayList<String> pipedData = new ArrayList<String>();
		StringBuffer sentence = new StringBuffer();

		for (int i = 0; i < iobData.size(); i++) {
			String line_iob = (String) iobData.get(i);
			String[] meta_lines = new String[metaData.size()];
			for (int j = 0; j < meta_lines.length; j++) {
				meta_lines[j] = metaData.get(j).get(i);
			}

			// conversion: several white spaces to a tab
			line_iob = line_iob.replaceAll("[\\s]+", "\t");
			for (int j = 0; j < meta_lines.length; j++) {
				meta_lines[j] = meta_lines[j].replaceAll("[\\s]+", "\t");
			}

			if (line_iob.equals("-DOCSTART-\tO")) {
				// ignore this line
			} else if (line_iob.equals("") || line_iob.equals("\t")) {
				if (sentence.length() > 0) // sentence finished

					pipedData.add(sentence.toString());
				sentence.delete(0, sentence.length());
			} else {
				String[] toks_iob = line_iob.split("[\t]");
				String[][] toks_meta = new String[meta_lines.length][];
				for (int j = 0; j < meta_lines.length; j++) {
					toks_meta[j] = meta_lines[j].split("[\t]");

					if (toks_iob.length != 2 || toks_meta[j].length != 2) {
						System.err
								.println("Error: format error. Incorrect size of line.");
						System.err.println(line_iob + " - " + toks_iob.length);
						System.err.println(meta_lines[j] + " - "
								+ toks_meta[j].length);
					}

					if (!toks_meta[j][0].equals(toks_iob[0])) {
						System.err.println("error reading, word pos!=word iob");
						System.out.println("IOB: " + toks_iob[0]);
						System.out.println("POS: " + toks_meta[j][0]);
						System.out.println(toks_meta[j][0] + " - "
								+ toks_iob[0]);
						System.out.println(line_iob + " -- " + meta_lines[j]);
						System.out.println("line number: " + i);
					}
				}

				// check tags: if tag is not contained in taglist, replace it
				// with the "O" tag
				if (checkTags) {
					if (!tags.contains(toks_iob[1])) {
						toks_iob[1] = "O";
					}
				}

				String token = toks_iob[0];
				for (int j = 0; j < meta_lines.length; j++) {
					token += "|" + toks_meta[j][1];
				}
				token += "|" + toks_iob[1] + " ";
				sentence.append(token);
			}
		}
		return pipedData;
	}

	/**
	 * split data in piped format into pool data (corpus) and gold data
	 * 
	 * @param fractionGold
	 *            the fraction of the gold
	 *            input
	 * @param poolOut
	 *            output of pooldata
	 * @param goldOut
	 *            output of golddata
	 */
	public static void makeDataSplit(double fractionGold,
			ArrayList<String> pipedData, ArrayList<String> poolOut,
			ArrayList<String> goldOut) {
		ArrayList<String> dummy = new ArrayList<String>();
		makeDataSplit(fractionGold, 0, pipedData, dummy, poolOut, goldOut);
	}

	/**
	 * split data in piped format into pool data (corpus) and gold data and a
	 * inital trainingset
	 * 
	 * @param fractionGold
	 *            the fraction of the gold
	 * @param initSize
	 *            size of initial trainingset
	 * @param initOut
	 *            output of initial trainingset
	 * @param poolOut
	 *            output of pooldata
	 * @param goldOut
	 *            output of golddata
	 */
	public static void makeDataSplit(double fractionGold, int initSize,
			ArrayList<String> pipedData, ArrayList<String> initOut,
			ArrayList<String> poolOut, ArrayList<String> goldOut) {

		// remove all elements from out-lists
		initOut.clear();
		poolOut.clear();
		goldOut.clear();

		int goldSize = (int) ((pipedData.size() - initSize) * fractionGold);
		int poolSize = pipedData.size() - goldSize;
		System.out.println("datasize: " + pipedData.size());
		System.out.println("initSize: " + initSize);
		System.out.println("goldSize: " + goldSize);
		System.out.println("poolSize: " + poolSize);

		if (fractionGold < 0.01 || (goldSize < 1 || fractionGold > 0.98)) {
			System.err
					.println("Error: fractionGold too small/large! Must be between 0.01 and 0.98 and result in at least one sentence.");
			System.exit(-1);
		}

		Collections.shuffle(pipedData);

		for (int i = 0; i < pipedData.size(); i++) {
			if (initOut.size() < initSize) {
				initOut.add(pipedData.get(i));
			} else if (goldOut.size() < goldSize) {
				goldOut.add(pipedData.get(i));
			} else {
				poolOut.add(pipedData.get(i));
			}
		}

	}

}
