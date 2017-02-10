package com.topsec.tsm.sim.asset;

import java.io.InputStream;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

/**
 * @ClassName: ExcelOperaterManager
 * @Declaration: excel文件处理类
 * 
 * @author: WangZhiai create on2014年6月12日下午2:38:51
 * @modify: 
 * </p>
 * @version: 3.2 Copyright ©2014 TopSec
 */
public class ExcelOperaterManager {
	private Workbook workbook;
    private Sheet defaultSheet;
    private String[][] defaultCellArr;

    public ExcelOperaterManager(InputStream instream,int startRow,int maxRowRead ,int startcol) throws Exception {
        init(instream,startRow,maxRowRead,startcol);
    }
    public ExcelOperaterManager(InputStream instream) throws Exception {
        init(instream,0,0,0);
    }
    
    private void init(InputStream instream,int startRow,int maxRowRead,int startcol)throws Exception{
        jxl.Workbook readwb = null;
        try {
            //构建Workbook对象, 只读Workbook对象   
 
            //直接从本地文件创建Workbook   
  
            readwb = Workbook.getWorkbook(instream);
            workbook=readwb;
            
            //Sheet的下标是从0开始
            //获取第一张Sheet表   
            Sheet readsheet = readwb.getSheet(0);
            defaultSheet=readsheet;
           //获取Sheet表中所包含的总列数
           int rsColumns = readsheet.getColumns();   
 
            //获取Sheet表中所包含的总行数
           int rsRows = readsheet.getRows();
           startRow=startRow<0?0:startRow;
           startcol=startcol<0?0:startcol;
           if(maxRowRead>0){
        	   rsRows=rsRows<maxRowRead?rsRows:maxRowRead+startRow;
           }
           defaultCellArr=new String[rsRows-startRow][rsColumns-startcol];
            //获取指定单元格的对象引用
            for (int i = startRow; i < rsRows; i++)
            {
                for (int j = startcol; j < rsColumns; j++)
                {
                   Cell cell = readsheet.getCell(j, i);
                   defaultCellArr[i-startRow][j-startcol]=cell.getContents().trim();
                   //System.out.print(cell.getContents() + " ");
                }   
                //System.out.println();
            }
            /*
           //利用已经创建的Excel工作薄,创建新的可写入的Excel工作薄 
            jxl.write.WritableWorkbook wwb = Workbook.createWorkbook(new File("F:/红楼人物1.xls"), readwb);   
 
            //读取第一张工作表  
            jxl.write.WritableSheet ws = wwb.getSheet(0);
            
            //获得第一个单元格对象
            jxl.write.WritableCell wc = ws.getWritableCell(0, 0);   
  
           //判断单元格的类型, 做出相应的转化   
           if (wc.getType() == CellType.LABEL)    
           {   
               Label l = (Label) wc;   
               l.setString("新姓名");   
          }   
            //写入Excel对象   
            wwb.write();   
            wwb.close();*/
       } catch (Exception e){   
            throw e;  
       } finally {   
          readwb.close();   
       }
    }

	public Workbook getWorkbook() {
		return workbook;
	}

	public void setWorkbook(Workbook workbook) {
		this.workbook = workbook;
	}

	public Sheet getDefaultSheet() {
		return defaultSheet;
	}

	public void setDefaultSheet(Sheet defaultSheet) {
		this.defaultSheet = defaultSheet;
	}

	public String[][] getDefaultCellArr() {
		return defaultCellArr;
	}

	public void setDefaultCellArr(String[][] defaultCellArr) {
		this.defaultCellArr = defaultCellArr;
	}
    
}
