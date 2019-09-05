package com.unis.zkydatadetection.service;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface baseDataService {
    boolean isContainChinese(String str);//判断字符串是否包含中文
    String filterChinese(String str);//字符串过滤中文
    boolean isChineseChar(char c);//消息一个字符是否是汉字
    char validateLegalString(String content,String illegal);//验证字符串是否包含特殊字符
    String checkKeyword(int row,String keyword,String arclvl,Map voldataM,Map excelAttr);//检测档号
    String checkSipKeyword(String keyword, String arclvl, Map excelAttr);//检测档号
    String[] checkqzdate(int row,String keyword,String arclvl,Map voldataM,Map excelAttr,String configResult);//检测起止时间
    Integer checkzjs(int row,String keyword,String arclvl,Map voldataM,Map excelAttr);//检测总件数
    Integer checkzys(int row,String keyword,String arclvl,Map voldataM,Map excelAttr);//检测总页数
    String getzzyh(int row,String keyword,String arclvl,Map voldataM,Map excelAttr);//检测终止日期
    String checkyh(int row,String keyword,String arclvl,Map voldataM,Map excelAttr);//检测页号
    String checkSipyh(List fileList,String volSyscode,String archiveType);//检测SIP页号
    Set getmj(int row, String keyword, String arclvl, Map voldataM, Map excelAttr);//检测密级
    List<List> checkFlData(int row, String keyword, String arclvl, Map voldataM, Map excelAttr);//检测密级
    boolean checkkg(String content);//检测空格
    List getParamMJ(String version);//获取代码表
    String checkFileTile(int row, String keyword, String arclvl, Map voldataM, Map excelAttr,String configResult1);//检测题名
    String digitalMd5(String sipPath) throws NoSuchAlgorithmException;//获取数字摘要
    boolean isInteger(String str);
}
