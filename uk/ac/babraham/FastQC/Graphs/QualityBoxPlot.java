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
package uk.ac.babraham.FastQC.Graphs;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

public class QualityBoxPlot extends JPanel {

	private int offset;
	private double [] numbps;
	private double [] mins;
	private double [] maxs;
	private double [] means;
	private double [] medians;
	private double [] lowest;
	private double [] highest;
	private double [] lowerQuartile;
	private double [] upperQuartile;
	private String [] xLabels;
	private String graphTitle;
	private double minY;
	private double maxY;
	private double yInterval;
	
	private static final Color GOOD = new Color(195,230,195);
	private static final Color BAD = new Color(230,220,195);
	private static final Color UGLY = new Color(230,195,195);
	
	private static final Color GOOD_DARK = new Color(175,230,175);
	private static final Color BAD_DARK = new Color(230,215,175);
	private static final Color UGLY_DARK = new Color(230,175,175);
		
	public QualityBoxPlot (
		int offset,
		double [] numbps,
		double [] mins, double [] maxs, 
		double [] means, double [] medians, 
		double [] lowest, double [] highest, 
		double [] lowerQuartile, double [] upperQuartile, 
		double minY, double maxY, double yInterval, String [] xLabels, String graphTitle) {

		this.offset = offset;
		this.numbps = numbps;
		this.mins = mins;
		this.maxs = maxs;
		this.means = means;
		this.means = means;
		this.medians = medians;
		this.lowest = lowest;
		this.highest = highest;
		this.lowerQuartile = lowerQuartile;
		this.upperQuartile = upperQuartile;
		this.minY = minY;
		this.maxY = maxY;
		this.yInterval = yInterval;
		this.xLabels = xLabels;
		this.graphTitle = graphTitle;
		this.yInterval = yInterval;
	}
		
	public void paint (Graphics g) {
		super.paint(g);
		
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());
		g.setColor(Color.BLACK);
		
		int lastY = 0;
		
		double yStart;
		
		if (minY % yInterval == 0) {
			yStart = minY;
		}
		else {
			yStart = yInterval * (((int)minY/yInterval)+1);
		}
		
		int xLeftOffset = 0;
		int xRightOffset = 0;
		
		// Draw the left y axis labels
		for (double i=yStart;i<=maxY;i+=yInterval) {
			String label = ""+i;
			label = label.replaceAll(".0$", "");
			label = label + " -> " + (char)(i+offset);
			int width = g.getFontMetrics().stringWidth(label);
			if (width > xLeftOffset) {
				xLeftOffset = width;
			}
			
			g.drawString(label, 2, getY(i)+(g.getFontMetrics().getAscent()/2));
		}


		// Draw the right y axis labels
		for (double i=0;i<=10;i+=yInterval) {
			String label = ""+i*10+"%";
			label = label.replaceAll(".0$", "");
			int width = g.getFontMetrics().stringWidth(label);
			if (width > xRightOffset) {
				xRightOffset = width;
			}
			
			g.drawString(label, getWidth() - width -2, getY(i)+(g.getFontMetrics().getAscent()/2));
		}
		
		// Give the x axis a bit of breathing space
		xLeftOffset += 5;
	
		g.setColor(Color.BLACK);
		
		
		// Draw the graph title
		int titleWidth = g.getFontMetrics().stringWidth(graphTitle);
		if (titleWidth <= getWidth()-xRightOffset -xLeftOffset){
			g.drawString(graphTitle, xLeftOffset + (getWidth()-xRightOffset -xLeftOffset - titleWidth)/2, 25);
		} else {

			String [] titles = graphTitle.split("-");
			int titlePartWidth = g.getFontMetrics().stringWidth(titles[0]);
			g.drawString(titles[0], xLeftOffset + (getWidth()-xRightOffset -xLeftOffset - titlePartWidth)/2, 15);
			titlePartWidth = g.getFontMetrics().stringWidth(titles[1]);
			g.drawString(titles[1], xLeftOffset + (getWidth()-xRightOffset -xLeftOffset - titlePartWidth)/2, 30);
		}
		
		// Work out the width of the x axis bins
		int baseWidth = (getWidth()-(xLeftOffset+xRightOffset))/means.length;
		if (baseWidth<1) baseWidth = 1;

		
		// First draw faint boxes over alternating bases so you can see which is which
		int lastXLabelEnd = 0;
		
		for (int i=0;i<means.length;i++) {		// Now draw some background colours which show good / bad quality
			if (i%2 != 0) {
				g.setColor(UGLY);
			}
			else {
				g.setColor(UGLY_DARK);
			}

			g.fillRect(xLeftOffset+(baseWidth*i), getY(20), baseWidth, getY(yStart)-getY(20));

			if (i%2 != 0) {
				g.setColor(BAD);
			}
			else {
				g.setColor(BAD_DARK);
			}

			g.fillRect(xLeftOffset+(baseWidth*i), getY(28), baseWidth, getY(20)-getY(28));

			if (i%2 != 0) {
				g.setColor(GOOD);
			}
			else {
				g.setColor(GOOD_DARK);
			}

			g.fillRect(xLeftOffset+(baseWidth*i), getY(maxY), baseWidth, getY(28)-getY(maxY));

			g.setColor(Color.BLACK);
			int baseNumberWidth = g.getFontMetrics().stringWidth(xLabels[i]);
			int labelStart = ((baseWidth/2)+xLeftOffset+(baseWidth*i))-(baseNumberWidth/2);
			
			if (labelStart > lastXLabelEnd) {
				g.drawString(xLabels[i], labelStart, getHeight()-25);
				lastXLabelEnd = labelStart+g.getFontMetrics().stringWidth(xLabels[i])+5;
			}
		}
		
		// Now draw the axes
		g.drawLine(xLeftOffset, getHeight()-40, xLeftOffset + baseWidth*means.length, getHeight()-40);
		g.drawLine(xLeftOffset, getHeight()-40, xLeftOffset, 40);
		g.drawString("Position in read (bp)", (getWidth()/2) - (g.getFontMetrics().stringWidth("Position in read (bp)")/2), getHeight()-5);
		
		// Now draw the boxplots
			
		for (int i=0;i<medians.length;i++) {
			
			int boxBottomY = getY(lowerQuartile[i]);
			int boxTopY = getY(upperQuartile[i]);
			int lowerWhiskerY = getY(lowest[i]);
			int upperWhiskerY = getY(highest[i]);
			int medianY = getY(medians[i]);
			
//			System.out.println("For base "+i+" values are BoxBottom="+lowerQuartile[i]+" boxTop="+upperQuartile[i]+" whiskerBottom="+lowest[i]+" whiskerTop="+highest[i]+" median="+medians[i]);
//			System.out.println("For base "+i+" Yvalues are BoxBottom="+boxBottomY+" boxTop="+boxTopY+" whiskerBottom="+lowerWhiskerY+" whiskerTop="+upperWhiskerY+" median="+medianY);
			
			// Draw the main box
			g.setColor(new Color(240,240,0)); // yellowish
			g.fillRect(xLeftOffset+(baseWidth*i)+2, boxTopY, baseWidth-4, boxBottomY-boxTopY);
			g.setColor(Color.BLACK);
			g.drawRect(xLeftOffset+(baseWidth*i)+2, boxTopY, baseWidth-4, boxBottomY-boxTopY);
			
			// Draw the upper whisker - highest quantiles
			g.drawLine(xLeftOffset+(baseWidth*i)+(baseWidth/2), upperWhiskerY, xLeftOffset+(baseWidth*i)+(baseWidth/2), boxTopY);
			g.drawLine(xLeftOffset+(baseWidth*i)+2, upperWhiskerY, xLeftOffset+(baseWidth*(i+1))-2, upperWhiskerY);
			
			// Draw the lower whisker - lowest quantiles
			g.drawLine(xLeftOffset+(baseWidth*i)+(baseWidth/2), lowerWhiskerY, xLeftOffset+(baseWidth*i)+(baseWidth/2), boxBottomY);
			g.drawLine(xLeftOffset+(baseWidth*i)+2, lowerWhiskerY, xLeftOffset+(baseWidth*(i+1))-2, lowerWhiskerY);

			// Draw the median line
			g.setColor(new Color(200,0,0));
			g.drawLine(xLeftOffset+(baseWidth*i)+2, medianY, (xLeftOffset+(baseWidth*(i+1)))-2,medianY);

		}

		// Now overlay the means
		g.setColor(new Color(0,0,200)); // blueish
		lastY = getY(means[0]);
		for (int i=1;i<means.length;i++) {
			int thisY = getY(means[i]);
			g.drawLine((baseWidth/2)+xLeftOffset+(baseWidth*(i-1)), lastY, (baseWidth/2)+xLeftOffset+(baseWidth*i), thisY);
			lastY = thisY;
		}

		// Now overlay the mins // green 1
		g.setColor(new Color(255,10,255));
		lastY = getY(mins[0]);
		for (int i=1;i<mins.length;i++) {
			int thisY = getY(mins[i]);
			g.drawLine((baseWidth/2)+xLeftOffset+(baseWidth*(i-1)), lastY, (baseWidth/2)+xLeftOffset+(baseWidth*i), thisY);
			lastY = thisY;
		}

		// Now overlay the maxs // green 2
		g.setColor(new Color(255,50,255));
		lastY = getY(maxs[0]);
		for (int i=1;i<maxs.length;i++) {
			int thisY = getY(maxs[i]);
			g.drawLine((baseWidth/2)+xLeftOffset+(baseWidth*(i-1)), lastY, (baseWidth/2)+xLeftOffset+(baseWidth*i), thisY);
			lastY = thisY;
		}

		// Now overlay the number of bp per position // red
		g.setColor(new Color(250,50,50));
		lastY = getY(numbps[0]);
		for (int i=1;i<numbps.length;i++) {
			int thisY = getY(numbps[i]);
			g.drawLine((baseWidth/2)+xLeftOffset+(baseWidth*(i-1)), lastY, (baseWidth/2)+xLeftOffset+(baseWidth*i), thisY);
			lastY = thisY;
		}
		
	}

	public int getY(double y) {
		return (getHeight()-40) - (int)(((getHeight()-80)/(maxY-minY))*(y-minY));
	}
	
}
