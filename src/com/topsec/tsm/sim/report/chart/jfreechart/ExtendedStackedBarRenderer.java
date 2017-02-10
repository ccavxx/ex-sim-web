package com.topsec.tsm.sim.report.chart.jfreechart;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;

import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRendererState;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.text.TextUtilities;
import org.jfree.ui.TextAnchor;
   
public class ExtendedStackedBarRenderer extends StackedBarRenderer   
{   
	private static final long serialVersionUID = 1L;
	private boolean showPositiveTotal;   
    private boolean showNegativeTotal;   
    private Font totalLabelFont;   
    private NumberFormat totalFormatter;   
   
    public ExtendedStackedBarRenderer()   
    {   
        showPositiveTotal = true;   
        showNegativeTotal = true;   
        totalLabelFont = new Font("SansSerif", 0, 10);   
        totalFormatter = NumberFormat.getInstance();   
    }   
   
    public NumberFormat getTotalFormatter()   
    {       	
        return totalFormatter;   
    }   
   
    public void setTotalFormatter(NumberFormat numberformat)   
    {   
        if(numberformat == null)   
        {   
            throw new IllegalArgumentException("Null format not permitted.");   
        } else   
        {   
            totalFormatter = numberformat;   
            return;   
        }   
    }   
   
    public void drawItem(Graphics2D graphics2d, CategoryItemRendererState categoryitemrendererstate, Rectangle2D rectangle2d, CategoryPlot categoryplot, CategoryAxis categoryaxis, ValueAxis valueaxis, CategoryDataset categorydataset,    
            int i, int j, int k)   
    {   
        Number number = categorydataset.getValue(i, j);   
        if(number == null)   
            return;   
        double d = number.doubleValue();   
        PlotOrientation plotorientation = categoryplot.getOrientation();   
        double d1 = categoryaxis.getCategoryMiddle(j, getColumnCount(), rectangle2d, categoryplot.getDomainAxisEdge()) - categoryitemrendererstate.getBarWidth() / 2D;   
        double d2 = 0.0D;   
        double d3 = 0.0D;   
        for(int l = 0; l < i; l++)   
        {   
            Number number1 = categorydataset.getValue(l, j);   
            if(number1 == null)   
                continue;   
            double d5 = number1.doubleValue();   
            if(d5 > 0.0D)   
                d2 += d5;   
            else   
                d3 += d5;   
        }   
   
        org.jfree.ui.RectangleEdge rectangleedge = categoryplot.getRangeAxisEdge();   
        double d4;   
        double d6;   
        if(d > 0.0D)   
        {   
            d4 = valueaxis.valueToJava2D(d2, rectangle2d, rectangleedge);   
            d6 = valueaxis.valueToJava2D(d2 + d, rectangle2d, rectangleedge);   
        } else   
        {   
            d4 = valueaxis.valueToJava2D(d3, rectangle2d, rectangleedge);   
            d6 = valueaxis.valueToJava2D(d3 + d, rectangle2d, rectangleedge);   
        }   
        double d7 = Math.min(d4, d6);   
        double d8 = Math.max(Math.abs(d6 - d4), getMinimumBarLength());   
        java.awt.geom.Rectangle2D.Double double1 = null;   
        if(plotorientation == PlotOrientation.HORIZONTAL)   
            double1 = new java.awt.geom.Rectangle2D.Double(d7, d1, d8, categoryitemrendererstate.getBarWidth());   
        else   
            double1 = new java.awt.geom.Rectangle2D.Double(d1, d7, categoryitemrendererstate.getBarWidth(), d8);   
        java.awt.Paint paint = getItemPaint(i, j);   
        graphics2d.setPaint(paint);   
        graphics2d.fill(double1);   
        if(isDrawBarOutline() && categoryitemrendererstate.getBarWidth() > 3D)   
        {   
            graphics2d.setStroke(getItemStroke(i, j));   
            graphics2d.setPaint(getItemOutlinePaint(i, j));   
            graphics2d.draw(double1);   
        }   
        org.jfree.chart.labels.CategoryItemLabelGenerator categoryitemlabelgenerator = getItemLabelGenerator(i, j);   
        if(categoryitemlabelgenerator != null && isItemLabelVisible(i, j))   
            drawItemLabel(graphics2d, categorydataset, i, j, categoryplot, categoryitemlabelgenerator, double1, d < 0.0D);   
        if(d > 0.0D)   
        {   
            if(showPositiveTotal && isLastPositiveItem(categorydataset, i, j))   
            {   
                graphics2d.setPaint(Color.black);   
                graphics2d.setFont(totalLabelFont);   
                double d9 = calculateSumOfPositiveValuesForCategory(categorydataset, j);   
                TextUtilities.drawRotatedString(totalFormatter.format(d9), graphics2d, (float)double1.getCenterX(), (float)(double1.getMinY() - 4D), TextAnchor.BOTTOM_CENTER, 0.0D, TextAnchor.BOTTOM_CENTER);   
            }   
        } else   
        if(showNegativeTotal && isLastNegativeItem(categorydataset, i, j))   
        {   
            graphics2d.setPaint(Color.black);   
            graphics2d.setFont(totalLabelFont);   
            double d10 = calculateSumOfNegativeValuesForCategory(categorydataset, j);   
            TextUtilities.drawRotatedString(String.valueOf(d10), graphics2d, (float)double1.getCenterX(), (float)(double1.getMaxY() + 4D), TextAnchor.TOP_CENTER, 0.0D, TextAnchor.TOP_CENTER);   
        }   
        if(categoryitemrendererstate.getInfo() != null)   
        {   
        	EntityCollection entitycollection = null;
        	if(categoryitemrendererstate.getInfo().getOwner()!=null){
        		entitycollection = categoryitemrendererstate.getInfo().getOwner().getEntityCollection();
        	}
            if(entitycollection != null)   
            {   
                String s = null;   
                CategoryToolTipGenerator categorytooltipgenerator = getToolTipGenerator(i, j);   
                if(categorytooltipgenerator != null)   
                    s = categorytooltipgenerator.generateToolTip(categorydataset, i, j);   
                String s1 = null;   
                if(getItemURLGenerator(i, j) != null)   
                    s1 = getItemURLGenerator(i, j).generateURL(categorydataset, i, j);   
                CategoryItemEntity categoryitementity = new CategoryItemEntity(double1, s, s1, categorydataset, i, categorydataset.getColumnKey(j), j);   
                entitycollection.add(categoryitementity);   
            }   
        }   
    }   
   
    private boolean isLastPositiveItem(CategoryDataset categorydataset, int i, int j)   
    {   
        boolean flag = true;   
        Number number = categorydataset.getValue(i, j);   
        if(number == null)   
            return false;   
        for(int k = i + 1; k < categorydataset.getRowCount(); k++)   
        {   
            Number number1 = categorydataset.getValue(k, j);   
            if(number1 != null)   
                flag = flag && number1.doubleValue() <= 0.0D;   
        }   
   
        return flag;   
    }   
   
    private boolean isLastNegativeItem(CategoryDataset categorydataset, int i, int j)   
    {   
        boolean flag = true;   
        Number number = categorydataset.getValue(i, j);   
        if(number == null)   
            return false;   
        for(int k = i + 1; k < categorydataset.getRowCount(); k++)   
        {   
            Number number1 = categorydataset.getValue(k, j);   
            if(number1 != null)   
                flag = flag && number1.doubleValue() >= 0.0D;   
        }   
   
        return flag;   
    }   
   
    private double calculateSumOfPositiveValuesForCategory(CategoryDataset categorydataset, int i)   
    {   
        double d = 0.0D;   
        for(int j = 0; j < categorydataset.getRowCount(); j++)   
        {   
            Number number = categorydataset.getValue(j, i);   
            if(number == null)   
                continue;   
            double d1 = number.doubleValue();   
            if(d1 > 0.0D)   
                d += d1;   
        }   
   
        return d;   
    }   
   
    private double calculateSumOfNegativeValuesForCategory(CategoryDataset categorydataset, int i)   
    {   
        double d = 0.0D;   
        for(int j = 0; j < categorydataset.getRowCount(); j++)   
        {   
            Number number = categorydataset.getValue(j, i);   
            if(number == null)   
                continue;   
            double d1 = number.doubleValue();   
            if(d1 < 0.0D)   
                d += d1;   
        }   
   
        return d;   
    }

	public boolean isShowPositiveTotal() {
		return showPositiveTotal;
	}

	public void setShowPositiveTotal(boolean showPositiveTotal) {
		this.showPositiveTotal = showPositiveTotal;
	}

	public boolean isShowNegativeTotal() {
		return showNegativeTotal;
	}

	public void setShowNegativeTotal(boolean showNegativeTotal) {
		this.showNegativeTotal = showNegativeTotal;
	}   
}  