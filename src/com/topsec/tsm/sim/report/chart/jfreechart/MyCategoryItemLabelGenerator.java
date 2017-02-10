package com.topsec.tsm.sim.report.chart.jfreechart;

import java.text.DateFormat;
import java.text.NumberFormat;

import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.data.category.CategoryDataset;

public class MyCategoryItemLabelGenerator extends StandardCategoryItemLabelGenerator {

	public MyCategoryItemLabelGenerator()
    {
        super("{2}", NumberFormat.getInstance());
    }

    public MyCategoryItemLabelGenerator(String labelFormat, NumberFormat formatter)
    {
        super(labelFormat, formatter);
    }

    public MyCategoryItemLabelGenerator(String labelFormat, NumberFormat formatter, NumberFormat percentFormatter)
    {
        super(labelFormat, formatter, percentFormatter);
    }

    public MyCategoryItemLabelGenerator(String labelFormat, DateFormat formatter)
    {
        super(labelFormat, formatter);
    }
    /**
     * 如果数据数据源数据为0则不显示数字信息
     */
	@Override
	public String generateLabel(CategoryDataset dataset, int row, int column) {
		Number value = dataset.getValue(row, column) ;
		if (value != null&&value.doubleValue()==0) {
			return "" ;
		}
		return super.generateLabel(dataset, row, column);
	}

}
