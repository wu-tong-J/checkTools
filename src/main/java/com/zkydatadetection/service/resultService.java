package com.unis.zkydatadetection.service;

import java.util.List;
import java.util.Map;

public interface resultService {
    boolean addResultInField(Map excelAttr, List contrastFieldList);
    boolean addResultInValue(Map<String,Object> checkResult);
    boolean addSipResultInValue(Map<String,Object> checkResult)throws Exception;
    int getCount(String tablename,String whereStr);
    int getCacheCount(String tablename, String whereStr);
    int getCheckDataCount(String tablename,String whereStr,String ifHistory);
    int addField(String[] excelTitles,Map excelAttr);
    int addExcelData(Map<Integer, Map<String,Object>> result,Map excelAttr) throws Exception;
    void deleteSql(String tablename, String whereStr);
    List getData(String field ,String tablename,String whereStr);
    int getSum(String field ,String tablename,String whereStr);
    boolean updData(Map<String,Object> checkResult);
    boolean updStatus(Map<String,Object> checkResult);
    String getConfig(String where);
    Map getConfigEfile(String field,String fieldType);
    List getCheckData(String tablename, String field, String ifHistory, String whereStr);
    List getCheckData(String tablename,String field,int pagesize,int pagenum,String ifHistory, String whereStr,String order);
    String getMysqlField(String tablename,String tablespace,String arcLvl);
}
