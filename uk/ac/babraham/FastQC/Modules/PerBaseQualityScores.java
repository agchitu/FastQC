/**
 * Copyright Copyright 2010-17 Simon Andrews
 *
 *    This file is part of FastQC.
 *
 *    FastQC is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    FastQC is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with FastQC; if not, write to the Free Software
 *    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package uk.ac.babraham.FastQC.Modules;

import java.io.IOException;

import javax.swing.JPanel;
import javax.xml.stream.XMLStreamException;

import uk.ac.babraham.FastQC.Graphs.BaseGroup;
import uk.ac.babraham.FastQC.Graphs.QualityBoxPlot;
import uk.ac.babraham.FastQC.Report.HTMLReportArchive;
import uk.ac.babraham.FastQC.Sequence.Sequence;
import uk.ac.babraham.FastQC.Sequence.QualityEncoding.PhredEncoding;
import uk.ac.babraham.FastQC.Utilities.QualityCount;

public class PerBaseQualityScores extends AbstractQCModule {

	public QualityCount [] qualityCounts = new QualityCount[0];
	long max_num_seq = 1;
	long min_num_seq = 0;
	double lowest_q_val = 10;
	double highest_q_val = 90;
	double [] numbps = null;
	double [] mins = null;
	double [] maxs = null;
	double [] means = null;
	double [] medians = null;
	double [] lowerQuartile = null;
	double [] upperQuartile = null;
	double [] lowest = null;
	double [] highest = null;
	String [] xLabels;
	int low = 0;
	int high = 0;
	PhredEncoding encodingScheme;

	public boolean isCalculated()
	{
		return calculated;
	}

	public void setCalculated(boolean value){
		calculated = value;
	}

	public JPanel getResultsPanel() {
		
		if (!calculated) getPercentages();

		return new QualityBoxPlot(encodingScheme.offset(), numbps, mins, maxs, means, medians, lowest, highest, lowerQuartile, upperQuartile, 
		low, high, 2d, xLabels, 
		"Quality scores across all bases ("+encodingScheme+" encoding) - #seq max: " + max_num_seq + " #seq min: " + min_num_seq);
	}
	
	public boolean ignoreFilteredSequences() {
		return true;
	}
	
	public boolean ignoreInReport () {
		// We don't show this if there is no quality data.
		if (ModuleConfig.getParam("quality_base", "ignore") > 0 || qualityCounts.length == 0) {
			return true;
		}
		return false;
	}

	private synchronized void getPercentages () {
		
		char [] range = calculateOffsets();
		encodingScheme = PhredEncoding.getFastQEncodingOffset(range[0]);
		low = 0;
		high = range[1] - encodingScheme.offset() + 1;
		if (high < 35) {
			high = 35;
		}
		
		BaseGroup [] groups = BaseGroup.makeBaseGroups(qualityCounts.length);
		numbps = new double[groups.length];
		mins = new double[groups.length];
		maxs = new double[groups.length];
		means = new double[groups.length];
		medians = new double[groups.length];
		lowest = new double[groups.length];
		highest = new double[groups.length];
		lowerQuartile = new double[groups.length];
		upperQuartile = new double[groups.length];
		xLabels = new String[groups.length];
		
		if (qualityCounts[0].getTotalCount() > 0){
			max_num_seq = qualityCounts[0].getTotalCount();
		}
		min_num_seq = qualityCounts[qualityCounts.length-1].getTotalCount();

		for (int i=0;i<groups.length;i++) {
			xLabels[i] = groups[i].toString();
			int minBase = groups[i].lowerCount();
			int maxBase = groups[i].upperCount();
			numbps[i] = getMeanNumbps(minBase, maxBase);
			mins[i] = getMin(minBase, maxBase, encodingScheme.offset());
			maxs[i] = getMax(minBase, maxBase, encodingScheme.offset());
			lowest[i] = getPercentile(minBase, maxBase, encodingScheme.offset(), lowest_q_val);
			highest[i] = getPercentile(minBase, maxBase, encodingScheme.offset(), highest_q_val);
			means[i] = getMean(minBase,maxBase,encodingScheme.offset());
			medians[i] = getPercentile(minBase, maxBase, encodingScheme.offset(), 50);
			lowerQuartile[i] = getPercentile(minBase, maxBase, encodingScheme.offset(), 25);
			upperQuartile[i] = getPercentile(minBase, maxBase, encodingScheme.offset(), 75);
		}

		calculated = true;

	}
	
	private char [] calculateOffsets () {
		// Works out from the set of chars what is the most
		// likely encoding scale for this file.
		
		char minChar = 0;
		char maxChar = 0;
		
		for (int q=0;q<qualityCounts.length;q++) {
			if (q == 0) {
				minChar = qualityCounts[q].getMinChar();
				maxChar = qualityCounts[q].getMaxChar();
			}
			else {
				if (qualityCounts[q].getMinChar() < minChar) {
					minChar = qualityCounts[q].getMinChar();
				}
				if (qualityCounts[q].getMaxChar() > maxChar) {
					maxChar = qualityCounts[q].getMaxChar();
				}
			}
		}
		
		return new char[] {minChar,maxChar};
	}
	
	public void processSequence(Sequence sequence) {
		
		calculated = false;
		char [] qual = sequence.getQualityString().toCharArray();
		if (qualityCounts.length < qual.length) {
			
			QualityCount [] qualityCountsNew = new QualityCount[qual.length];
			
			for (int i=0;i<qualityCounts.length;i++) {
				qualityCountsNew[i] = qualityCounts[i];
			}
			for (int i=qualityCounts.length;i<qualityCountsNew.length;i++) {
				qualityCountsNew[i] = new QualityCount();				
			}
			qualityCounts = qualityCountsNew;
			
		}
		
		for (int i=0;i<qual.length;i++) {
			qualityCounts[i].addValue(qual[i]);
		}
		
	}
	
	public void reset () {
		qualityCounts = new QualityCount[0];
	}

	public String description() {
		return "Shows the Quality scores of all bases at a given position in a sequencing run";
	}

	public String name() {
		return "Per base sequence quality";
	}

	public boolean raisesError() {
		if (!calculated) getPercentages();

		for (int i=0;i<lowerQuartile.length;i++) {
			if (Double.isNaN(lowerQuartile[i])) {
				// There wasn't enough data for this group to make an assessment
				continue;
			}
			if (lowerQuartile[i] < ModuleConfig.getParam("quality_base_lower", "error") || medians[i] < ModuleConfig.getParam("quality_base_median", "error")) {
				return true;
			}
		}
		return false;
	}

	public boolean raisesWarning() {
		if (!calculated) getPercentages();

		for (int i=0;i<lowerQuartile.length;i++) {
			if (Double.isNaN(lowerQuartile[i])) {
				// There wasn't enough data for this group to make an assessment
				continue;
			}
			if (lowerQuartile[i] < ModuleConfig.getParam("quality_base_lower", "warn") || medians[i] < ModuleConfig.getParam("quality_base_median", "warn")) {
				return true;
			}
		}
		return false;
	}
	
	public void makeReport(HTMLReportArchive report) throws IOException,XMLStreamException
		{
		if (!calculated) getPercentages();

		writeDefaultImage(report, "per_base_quality.png", "Per base quality graph", Math.max(800, means.length*15), 600);		
		
		StringBuffer sb = report.dataDocument();
		sb.append("#Base\tMean\tMedian\tLower Quartile\tUpper Quartile\t10th Percentile\t90th Percentile\n");
		for (int i=0;i<means.length;i++) {
			sb.append(xLabels[i]);
			sb.append("\t");

			sb.append(means[i]);
			sb.append("\t");

			sb.append(medians[i]);
			sb.append("\t");

			sb.append(lowerQuartile[i]);
			sb.append("\t");

			sb.append(upperQuartile[i]);
			sb.append("\t");

			sb.append(lowest[i]);
			sb.append("\t");

			sb.append(highest[i]);

			sb.append("\n");
		}
	}

	private double getMin (int minbp, int maxbp, int offset) {
		double min_group = qualityCounts[minbp-1].getMinChar()-offset;
		
		for (int i=minbp;i<maxbp;i++) {
			double mn = qualityCounts[minbp-1].getMinChar()-offset;
			if (mn < min_group) {
				min_group = mn;
			}
		}
		
		return min_group;
	}

	private double getMax (int minbp, int maxbp, int offset) {
		double max_group = qualityCounts[minbp-1].getMaxChar()-offset;
		
		for (int i=minbp;i<maxbp;i++) {
			double mx = qualityCounts[minbp-1].getMaxChar()-offset;
			if (mx > max_group) {
				max_group = mx;
			}
		}
		
		return max_group;
	}

	private double getPercentile (int minbp, int maxbp, int offset, double percentile) {
		int count = 0;
		double total = 0;
	
		for (int i=minbp-1;i<maxbp;i++) {
			if (qualityCounts[i].getTotalCount() > 100) {
				count++;
				total += qualityCounts[i].getPercentile(offset, percentile);
			}
		}
		
		if (count > 0) {
			return total/count;
		}
		return Double.NaN;
		
	}


	private double getMeanNumbps (int minbp, int maxbp) {
		int count = maxbp - minbp + 1;
		double total = 0;

		for (int i=minbp-1;i<maxbp;i++) {
			total += qualityCounts[i].getTotalCount()*10.0/max_num_seq;
		}
		
		if (count > 0) {
			return total/count;
		}
		return Double.NaN;
	}


	private double getMean (int minbp, int maxbp, int offset) {
		int count = 0;
		double total = 0;
	
		for (int i=minbp-1;i<maxbp;i++) {
			if (qualityCounts[i].getTotalCount() > 0) {
				count++;
				total += qualityCounts[i].getMean(offset);
			}
		}
		
		if (count > 0) {
			return total/count;
		}
		return 0;
		
	}

}
