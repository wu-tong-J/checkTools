package com.unis.zkydatadetection.service;

import java.util.List;
import java.util.Map;

public interface checkService {
    String checkField(String[] titles, Map excelAttr);
    boolean checkFieldValue(Map excelAttr);
    boolean checkEfile(String path,Map excelAttr);
    String parasExcel(String excelPath,Map excelAttr) throws Exception;
    Map getCheckType();
}
