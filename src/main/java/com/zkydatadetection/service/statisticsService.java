package com.unis.zkydatadetection.service;

import java.util.List;
import java.util.Map;

public interface statisticsService {
    boolean addDataStat(Map statMap);
    int getCountStat(String tablename,String whereStr,String libcode,String mj,String ifHistory);
    List<Map<String, Object>> selectStat(String libcode,String mj,String whereStr,String ifHistory);
}
