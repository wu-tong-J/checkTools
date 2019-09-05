package com.unis.zkydatadetection.service;

import java.util.List;
import java.util.Map;

public interface logService {
    boolean addLog(String usercode,String libcode,String context);
    List<Map<String, Object>> selectLog(String tablename, String field, int pagesize, int pagenum, String whereStr,String order);
    List<Map<String, Object>> selectLog(String tablename, String field, String whereStr);
    int getCountLog(String tablename, String whereStr);
}
