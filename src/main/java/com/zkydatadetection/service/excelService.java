package com.unis.zkydatadetection.service;

import com.unis.zkydatadetection.model.log;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.util.List;
import java.util.Map;

public interface excelService {
    Map<Integer, Map<String,Object>> readExcel(String filepath, Map excelAttr,String[] amsField);
    String[] getExcelTitle(String filepath, Map excelAttr);
    String[] getAmsField(String[] excelTitles, Map excelAttr);
    HSSFWorkbook export(List<log> list,String[] fields,String checkField);
    HSSFWorkbook exportForMap(List<Map<String, Object>> list,String[] fields);
    HSSFWorkbook exportCheckForMap(List<Map<String, Object>> list,String[] excelHeader,String checkField);
    String[] getExcelField(String arclvl,String field);
}
