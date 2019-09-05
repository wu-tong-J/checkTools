package com.unis.zkydatadetection.service.impl;

import ch.qos.logback.classic.Logger;
import com.unis.zkydatadetection.service.baseDataService;
import com.unis.zkydatadetection.service.resultService;
import lombok.extern.slf4j.Slf4j;
//import org.apache.tika.Tika;
//import org.apache.tika.exception.TikaException;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service(value = "baseDataService")
public class baseDataServiceImpl implements baseDataService {

    private final Logger log = (Logger) LoggerFactory.getLogger("baseDataServiceImpl.class");

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    private resultService rs;

    @Override
    public boolean isContainChinese(String str) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }

    @Override
    public String filterChinese(String str) {
        // 用于返回结果
        String result = str;
        boolean flag = isContainChinese(str);
        if (flag) {// 包含中文
            // 用于拼接过滤中文后的字符
            StringBuffer sb = new StringBuffer();
            // 用于校验是否为中文
            boolean flag2 = false;
            // 用于临时存储单字符
            char chinese = 0;
            // 5.去除掉文件名中的中文
            // 将字符串转换成char[]
            char[] charArray = str.toCharArray();
            // 过滤到中文及中文字符
            for (int i = 0; i < charArray.length; i++) {
                chinese = charArray[i];
                flag2 = this.isChineseChar(chinese);
                if (!flag2) {// 不是中日韩文字及标点符号
                    sb.append(chinese);
                }
            }
            result = sb.toString();
        }
        return result;
    }

    @Override
    public boolean isChineseChar(char c) {
        try {
            return String.valueOf(c).getBytes("UTF-8").length > 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public char validateLegalString(String content,String illegal) {
//        String illegal = "`~!#%^&*=+\\|{};:'\",<>/?○●★☆☉♀♂※¤╬の〆";
        char isLegalChar = 't';
        L1: for (int i = 0; i < content.length(); i++) {
            for (int j = 0; j < illegal.length(); j++) {
                if (content.charAt(i) == illegal.charAt(j)) {
                    isLegalChar = content.charAt(i);
                    break L1;
                }
            }
        }
        return isLegalChar;
    }

    @Override
    public String checkKeyword(int row, String keyword, String arclvl, Map voldataM, Map excelAttr) {
        StringBuffer  checkerror = new StringBuffer();
        String libcode = excelAttr.get("libcode").toString();
        String unitsys = excelAttr.get("unitsys").toString();
        String whereStr ="";
        String[] keywords = keyword.split("-");
        if("vol".equals(arclvl)) {
            whereStr = "paramcode='volkeyword' and libcode='"+libcode+"' and unitsys = '"+unitsys+"' and arclvl = '"+arclvl+"'";
        }
        if("file".equals(arclvl)) {
            whereStr = "paramcode='keyword' and libcode='"+libcode+"' and unitsys = '"+unitsys+"' and arclvl = '"+arclvl+"'";
        }
        List keywordSysL = rs.getData("syscode","s_paramconfig",whereStr);
        if(keywordSysL.size()>0){
        Map keywordSysM = (Map)keywordSysL.get(0);
            String paramSys = keywordSysM.get("syscode").toString();
            String field = "paramenname,paramchname";
            String where  = "paramsys = '"+paramSys+"'";
            List keywordL = rs.getData(field,"s_paramdata",where);
            if(keywordL.size()>0){
                Map keywordM = (Map)keywordL.get(0);
                String keywordenname = keywordM.get("paramenname").toString();
                String keywordchname = keywordM.get("paramchname").toString();
                String[] keywordennames = keywordenname.split("-");
                String[] keywordchnames = keywordchname.split("-");
                if(keywords.length!=keywordennames.length){
                    checkerror.append("第"+row+"行，档号规则与配置不匹配；");
                }
//                else{
//                    for(int i =0 ;i<keywords.length;i++){
//                        if(voldataM.get(keywordennames[i])!=null && !"".equals(voldataM.get(keywordennames[i]))){
//                            String value = voldataM.get(keywordennames[i]).toString();
//                            if(keywords[i].equals(value)){
//                                continue;
//                            }else{
//                                checkerror.append("第"+row+"行，档号中的"+keywordchnames[i]+"与excel中的值不相等;");
//                            }
//                        }else {
//                            if(keywords[i].equals(keywordennames[i])){
//                                continue;
//                            }else{
//                                checkerror.append("第"+row+"行，档号中的"+keywordchnames[i]+"与excel中的值不相等;");
//                            }
//                        }
//                    }
//                }
            }else{
                checkerror.append("档号规则不存在；");
            }
        }else{
            checkerror.append("档号规则不存在；");
        }
        return checkerror.toString();
    }

    @Override
    public String checkSipKeyword(String keyword, String arclvl, Map excelAttr) {
        StringBuffer  checkerror = new StringBuffer();
        String libcode = excelAttr.get("libcode").toString();
        String unitsys = excelAttr.get("unitsys").toString();
        String whereStr ="";
        String[] keywords = keyword.split("-");
        if("vol".equals(arclvl)) {
            whereStr = "paramcode='volkeyword' and libcode='"+libcode+"' and unitsys = '"+unitsys+"' and arclvl = '"+arclvl+"'";
        }
        if("file".equals(arclvl)) {
            whereStr = "paramcode='keyword' and libcode='"+libcode+"' and unitsys = '"+unitsys+"' and arclvl = '"+arclvl+"'";
        }
        List keywordSysL = rs.getData("syscode","s_paramconfig",whereStr);
        if(keywordSysL.size()>0){
            Map keywordSysM = (Map)keywordSysL.get(0);
            String paramSys = keywordSysM.get("syscode").toString();
            String field = "paramenname,paramchname";
            String where  = "paramsys = '"+paramSys+"'";
            List keywordL = rs.getData(field,"s_paramdata",where);
            if(keywordL.size()>0){
                Map keywordM = (Map)keywordL.get(0);
                String keywordenname = keywordM.get("paramenname").toString();
                String keywordchname = keywordM.get("paramchname").toString();
                String[] keywordennames = keywordenname.split("-");
                String[] keywordchnames = keywordchname.split("-");
                if(keywords.length!=keywordennames.length){
                    checkerror.append("档号规则与配置不匹配；");
                }
            }else{
                checkerror.append("档号规则不存在；");
            }
        }else{
            checkerror.append("档号规则不存在；");
        }
        return checkerror.toString();
    }

    @Override
    public String[] checkqzdate(int row, String keyword, String arclvl, Map voldataM, Map excelAttr,String configResult) {
//        String[] dates = new String[2];
        String[] dates = new String[2];
        String archiveType = excelAttr.get("archiveType").toString();
        List<Integer> l = new ArrayList<Integer>();
        String tablename = archiveType+arclvl+"data";
        String field = "rq";
        String whereStr = "keyword like '"+keyword+"%' ORDER BY REVERSE(LEFT(REVERSE(keyword),INSTR(REVERSE(keyword),'-')-1))";
        try{
            List<Map> rqL = rs.getData(field,tablename,whereStr);
            for(int i=0;i<rqL.size();i++){
                Map rqm = (Map)rqL.get(i);
                String rq = rqm.get("rq").toString();
                if(rq.length()==configResult.length()){
                    int date = 0;
                    if(rq.contains("-")){
                        rq=rq.replaceAll("-","");
                        date = Integer.valueOf(rq);
                    }else {
                        date = Integer.valueOf(rq);
                    }
                    l.add(date);
                }else {
                    l.add(0);
                }
            }
            if(l.size()== rqL.size() && l.size()!=0 && rqL.size()!=0){
                Collections.sort(l);
                dates[0]=l.get(0).toString();
                dates[1]=l.get(l.size()-1).toString();
            }else{
                Collections.sort(l);
                dates[0]="0";
                dates[1]="0";
            }
        }catch(Exception e){
            log.info(e.getMessage());
        }
        return dates;
    }

    @Override
    public Integer checkzjs(int row, String keyword, String arclvl, Map voldataM, Map excelAttr) {
        String archiveType = excelAttr.get("archiveType").toString();
        List<Integer> l = new ArrayList<Integer>();
        String tablename = archiveType+arclvl+"data";
        String field = "count(1)";
        String whereStr = "keyword like '"+keyword+"%' ORDER BY REVERSE(LEFT(REVERSE(keyword),INSTR(REVERSE(keyword),'-')-1))";
        int count = rs.getCacheCount(tablename,whereStr);
        return count;
    }

    @Override
    public Integer checkzys(int row, String keyword, String arclvl, Map voldataM, Map excelAttr) {
        String archiveType = excelAttr.get("archiveType").toString();
        List<Integer> l = new ArrayList<Integer>();
        String tablename = archiveType+arclvl+"data";
        String field = "pagenum";
        String whereStr = "keyword like '"+keyword+"%' ORDER BY REVERSE(LEFT(REVERSE(keyword),INSTR(REVERSE(keyword),'-')-1))";
        List<Map> ysL = rs.getData(field,tablename,whereStr);
        int ys = 0;
        for(int i=0;i<ysL.size();i++){
            if(ysL.get(i)!=null){
                Map ysm = (Map)ysL.get(i);
                if(ysm.get("pagenum")!=null && !"".equals(ysm.get("pagenum"))){
                    ys = ys+Integer.valueOf(ysm.get("pagenum").toString());
                }
            }
        }
        return ys;
    }

    @Override
    public String getzzyh(int row, String keyword, String arclvl, Map voldataM, Map excelAttr) {
        String result = "";
        String archiveType = excelAttr.get("archiveType").toString();
        List<Integer> l = new ArrayList<Integer>();
        String tablename = archiveType+arclvl+"data";
        String field = "yh,keyword";
        String whereStr = "keyword like '"+keyword+"%' ORDER BY REVERSE(LEFT(REVERSE(keyword),INSTR(REVERSE(keyword),'-')-1))";
        List<Map> yhL = rs.getData(field,tablename,whereStr);
        List yhlist = new ArrayList();
        for(int i=0;i<yhL.size();i++) {
            if(yhL.get(i)!=null && !"".equals(yhL.get(i))){
                Map yhm = (Map) yhL.get(i);
                if(yhm.get("yh")!=null && !"".equals(yhm.get("yh"))){
                    String yh = yhm.get("yh").toString();
                    if(yh.contains("-")){
                        yhlist.add(yh);
                    }
                }
            }
        }
        if(yhlist.size()==1){
            result = yhlist.get(0).toString();
        }
        return result;
    }

    @Override
    public String checkyh(int row, String keyword, String arclvl, Map voldataM, Map excelAttr) {
        String archiveType = excelAttr.get("archiveType").toString();
        List<Integer> l = new ArrayList<Integer>();
        String tablename = archiveType+arclvl+"data";
        String field = "yh,pagenum,keyword,row";
        String whereStr = "keyword like '"+keyword+"%' ORDER BY REVERSE(LEFT(REVERSE(keyword),INSTR(REVERSE(keyword),'-')-1))";
        int yhAddys = 0;//上一件的页数加页号
        List<Map> yhL = rs.getData(field,tablename,whereStr);
        StringBuffer result = new StringBuffer();
        for(int i=0;i<yhL.size();i++) {
            Map yhm = (Map) yhL.get(i);
            if(yhm.get("yh")!=null && !"".equals(yhm.get("yh"))){
                String yh = yhm.get("yh").toString();
                if(yhAddys==0 || "".equals(yhAddys)){//第一件时
                    if(yhm.get("pagenum")!=null && !"".equals(yhm.get("pagenum"))){
                        int pagenum = Integer.valueOf(yhm.get("pagenum").toString());
                        if(!"1".equals(yh)){
                            result.append("卷内文件级，第" + yhm.get("row") + "行，该文件是该卷第一个文件页号却不是1；");
                        }else{
                            yhAddys = Integer.valueOf(yh)+pagenum;
                        }
                    }else {
                        result.append("卷内文件级，第" + yhm.get("row") + "行，该文件页数的值为空；");
                    }
                }else {
                    if(yh.contains("-")){
                        if(Integer.valueOf(yhm.get("row").toString())==yhL.size()){
                            int yhys = 0;
                            String[] yhs = yh.split("-");
                            if(yhm.get("pagenum")!=null && !"".equals(yhm.get("pagenum"))){
                                int pagenum = Integer.valueOf(yhm.get("pagenum").toString());
                                yhys = Integer.valueOf(yhs[0])+pagenum;
                            }else {
                                result.append("卷内文件级，第" + yhm.get("row") + "行，该文件页数的值为空；");
                            }
                            if(yhAddys == Integer.valueOf(yhs[0]) && yhys-1 == Integer.valueOf(yhs[1])){
                                log.info("页号正常");
                            }else {
                                result.append("卷内文件级，第" + yhm.get("row") + "行，该文件页次（号）不等于上一件的页数加页次（号）；");
                            }
                        }else{
                            result.append("卷内文件级，第" + yhm.get("row") + "行，该文件不是卷内最后一个但页号中包含“-”；");
                        }
                    }else{
                        if(Integer.valueOf(yhm.get("row").toString())!=yhL.size()){
                            if(yhAddys != Integer.valueOf(yh)){
                                result.append("卷内文件级，第" + yhm.get("row") + "行，该文件页次（号）不等于上一件的页数加页次（号）；");
                            }else {
                                if(yhm.get("pagenum")!=null && !"".equals(yhm.get("pagenum"))){
                                    int pagenum = Integer.valueOf(yhm.get("pagenum").toString());
                                    yhAddys = Integer.valueOf(yh)+pagenum;
                                }else {
                                    result.append("卷内文件级，第" + yhm.get("row") + "行，该文件页数的值为空；");
                                }
                            }
                        }else{
                            result.append("卷内文件级，第" + yhm.get("row") + "行，该文件是卷内最后一个但页号中不包含“-”；");
                        }
                    }

                }
                String filekeyword = yhm.get("keyword").toString();
                String filedh = filekeyword.substring(filekeyword.lastIndexOf("-")+1,filekeyword.length());
                int fileLsh = Integer.valueOf(filedh);
                if(fileLsh != yhL.size()){
                    if(yh.contains("-")) {
                        result.append("卷内文件级，第" + yhm.get("row") + "行，该文件不是卷内最后一个但页号中包含“-”；");
                    }
                }else{
                    if(!yh.contains("-")){
                        result.append("卷内文件级，第" + yhm.get("row") + "行，该文件是卷内最后一个但页号中不包含“-”；");
                    }else{
                        String fileyh = yh.substring(yh.lastIndexOf("-")+1,yh.length());
                        result.append(fileyh);
                    }
                }
            }
        }
        return result.toString();
    }

    @Override
    public String checkSipyh(List fileList,String volSyscode,String archiveType) {
        List<Integer> l = new ArrayList<Integer>();
        int yhAddys = 0;//上一件的页数加页号
        StringBuffer result = new StringBuffer();
        for(int i=0;i<fileList.size();i++) {
            Map<String,Object> fileM = (Map)fileList.get(i);
            String keyword = "";
            if("xd".equals(archiveType)){
                keyword = fileM.get("先导专项文件级档号").toString();
            }else{
                keyword = fileM.get("文件级档号").toString();
            }
            String jha = keyword.substring(keyword.lastIndexOf("-")+1,keyword.length());
            String psyscode = fileM.get("pid").toString();
            if(!psyscode.equals(volSyscode)){
                continue;
            }else{
                if(fileM.get("页号")!=null && !"".equals(fileM.get("页号"))){
                    String yh = fileM.get("页号").toString();
                    if(yhAddys==0 || "".equals(yhAddys)){//第一件时
                        if(fileM.get("页号")!=null && !"".equals(fileM.get("页号"))){
                            int pagenum = Integer.valueOf(fileM.get("页数").toString());
                            if(!"1".equals(yh)){
                                result.append("卷内文件级，该文件是该卷第一个文件页号却不是1；");
                            }else{
                                yhAddys = Integer.valueOf(yh)+pagenum;
                            }
                        }else {
                            result.append("卷内文件级，该文件页数的值为空；");
                        }
                    }else{
                        if(yh.contains("-")){
                            if(Integer.valueOf(jha)==fileList.size()){//最后一件
                                int yhys = 0;
                                String[] yhs = yh.split("-");
                                if(fileM.get("页号")!=null && !"".equals(fileM.get("页号"))){
                                    int pagenum = Integer.valueOf(fileM.get("页号").toString());
                                    yhys = Integer.valueOf(yhs[0])+pagenum;
                                }else {
                                    result.append("卷内文件级，该文件页数的值为空；");
                                }
                                if(yhAddys == Integer.valueOf(yhs[0]) && yhys-1 == Integer.valueOf(yhs[1])){
                                    log.info("页号正常");
                                }else {
                                    result.append("卷内文件级，该文件页次（号）不等于上一件的页数加页次（号）；");
                                }
                            }else{
                                result.append("卷内文件级，该文件不是卷内最后一个但页号中包含“-”；");
                            }
                        }else{
                            if(Integer.valueOf(jha)!=fileList.size()){
                                if(yhAddys != Integer.valueOf(yh)){
                                    result.append("卷内文件级，该文件页次（号）不等于上一件的页数加页次（号）；");
                                }else {
                                    if(fileM.get("页号")!=null && !"".equals(fileM.get("页号"))){
                                        int pagenum = Integer.valueOf(fileM.get("页号").toString());
                                        yhAddys = Integer.valueOf(yh)+pagenum;
                                    }else {
                                        result.append("卷内文件级，该文件页数的值为空；");
                                    }
                                }
                            }else{
                                result.append("卷内文件级，该文件是卷内最后一个但页号中不包含“-”；");
                            }
                        }
                    }
                }
            }
            if(fileM.get("页号")!=null && !"".equals(fileM.get("页号"))){
                String yh = fileM.get("页号").toString();
                String filekeyword = "";
                if("xd".equals(archiveType)){
                    filekeyword = fileM.get("先导专项文件级档号").toString();
                }else{
                    filekeyword = fileM.get("文件级档号").toString();
                }
                String filedh = filekeyword.substring(filekeyword.lastIndexOf("-")+1,filekeyword.length());
                int fileLsh = Integer.valueOf(filedh);
                if(fileLsh != fileList.size()){
                    if(yh.contains("-")) {
                        result.append("卷内文件级，该文件不是卷内最后一个但页号中包含“-”；");
                    }
                }else{
                    if(!yh.contains("-")){
                        result.append("卷内文件级，该文件是卷内最后一个但页号中不包含“-”；");
                    }else{
                        String fileyh = yh.substring(yh.lastIndexOf("-")+1,yh.length());
                        result.append(fileyh);
                    }
                }
            }
        }
        return result.toString();
    }

    @Override
    public Set getmj(int row, String keyword, String arclvl, Map voldataM, Map excelAttr) {
        String archiveType = excelAttr.get("archiveType").toString();
        List<Integer> l = new ArrayList<Integer>();
        String tablename = archiveType+arclvl+"data";
        String field = "mj";
        String whereStr = "keyword like '"+keyword+"%' ORDER BY REVERSE(LEFT(REVERSE(keyword),INSTR(REVERSE(keyword),'-')-1))";
        List<Map> mjL = rs.getData(field,tablename,whereStr);
        Set mjs = new HashSet();
        for(int i=0;i<mjL.size();i++){
            if(mjL.get(i)!=null && !"".equals(mjL.get(i))){
                Map mjm = (Map) mjL.get(i);
//                if(mjm.get("mj")!=null && !"".equals(mjm.get("mj"))){
                String mj = "";
                if(mjm.get("mj")!=null && !"".equals(mjm.get("mj"))) {
                    mj = mjm.get("mj").toString();
                }
//                   String[] mjStr = mj.split("_");
                    int code = 0;
                    if("绝密".equals(mj)){
                        code = 3;
                    }else if("机密".equals(mj)){
                        code = 2;
                    }else if("秘密".equals(mj)){
                        code = 1;
                    }else if("".equals(mj)){
                        code = 0;
                    }else{
                        code = 4;
                    }
                   mjs.add(code);
//                }
            }
        }
        return mjs;
    }

    @Override
    public List<List> checkFlData(int row, String keyword, String arclvl, Map voldataM, Map excelAttr) {
        List fldata = new ArrayList();
        List flh = new ArrayList();
        List flmc = new ArrayList();
        String archiveType = excelAttr.get("archiveType").toString();
        List<Integer> l = new ArrayList<Integer>();
        String tablename = archiveType+arclvl+"data";
        String field = "flh,flname";
        String whereStr = "keyword like '"+keyword+"%' ORDER BY REVERSE(LEFT(REVERSE(keyword),INSTR(REVERSE(keyword),'-')-1))";
        List<Map> flL = rs.getData(field,tablename,whereStr);
        for(int i=0;i<flL.size();i++){
            Map<String,Object> flm = (Map)flL.get(i);
            for(Entry<String,Object> vo : flm.entrySet()){
                if("flh".equals(vo.getKey())){
                    flh.add(vo.getValue());
                }
                if("flname".equals(vo.getKey())){
                    flmc.add(vo.getValue());
                }
               System.out.println(vo.getKey()+"  "+vo.getValue());
            }
        }
        fldata.add(flh);
        fldata.add(flmc);
        return fldata;
    }

    @Override
    public boolean checkkg(String content) {
        boolean b = true;
        if(content.indexOf(" ")!=-1){
            b=false;
        }
        return b;
    }

    @Override
    public List getParamMJ(String version){
        List mjList = new ArrayList();
        String whereStr = "paramcode = 'p_security'";
        if(version!=null && !"".equals(version)){
            whereStr = whereStr+" and version = '"+version+"'";
        }
        List<Map> mjl = rs.getData("code,name","s_param",whereStr);
        for(int i=0;i<mjl.size();i++){
            if(mjl.get(i)!=null && !"".equals(mjl.get(i))){
                Map mjm = mjl.get(i);
                if((mjm.get("code")!=null && !"".equals(mjm.get("code"))) && (mjm.get("name")!=null && !"".equals(mjm.get("name")))){
                    String mj = mjm.get("code").toString()+"_"+mjm.get("name").toString();
                    mjList.add(mj);
                }
            }
        }
        return mjList;
    }

    @Override
    public String checkFileTile(int row, String keyword, String arclvl, Map voldataM, Map excelAttr,String configResult1) {
        Set titleSet = new HashSet();
        String archiveType = excelAttr.get("archiveType").toString();
        String tablename = archiveType+arclvl+"data";
        String field = "title,row";
        String whereStr = "keyword like '"+keyword+"%' ORDER BY REVERSE(LEFT(REVERSE(keyword),INSTR(REVERSE(keyword),'-')-1))";
        List<Map> TitleL = rs.getData(field,tablename,whereStr);
        StringBuffer errormessage = new StringBuffer();
        for(int i=0;i<TitleL.size();i++){
            Map TitleM = TitleL.get(i);
            String title = TitleM.get("title").toString();
            boolean titleb = this.isContainChinese(title);
            if (!titleb) {
                errormessage.append("该案卷的卷内文件，excel文件级第" + TitleM.get("row") + "行，文件题名" + title + "没有中文；");
            }else{
                int titleLength = title.length();
                if(titleLength<Integer.valueOf(configResult1)){
                    errormessage.append("该案卷的卷内文件，excel文件级第" + TitleM.get("row") + "行，文件题名" + title + "的长度小于规定长度"+configResult1+"；");
                }else{
                    if(titleSet.size()==0){
                        titleSet.add(title);
                    }else{
                        int titleSetb = titleSet.size();
                        titleSet.add(title);
                        int titleSeta = titleSet.size();
                        if(titleSetb==titleSeta){
                            errormessage.append("该案卷的卷内文件，excel文件级第" + TitleM.get("row") + "行，该文件的文件题名已存在；");
                        }
                    }
                }
            }
        }
        return errormessage.toString();
    }

    @Override
    public String digitalMd5(String sipPath) throws NoSuchAlgorithmException {
        String file = readToString(sipPath);
        String result = toHex(md5(file));
        System.out.println("数字摘要为："+result);
        return result;
    }

    @Override
    public boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

//    @Override
//    public boolean checkDzwj(String sipPath) {
//        boolean result = true;
//        Tika tika = new Tika();
//        File file = new File(sipPath);
//        try {
//            tika.parseToString(file);
//        } catch (IOException e) {
//            e.printStackTrace();
//            result = false;
//        } catch (TikaException e) {
//            e.printStackTrace();
//            result = false;
//        }
//        return result;
//    }

    private static byte[] md5(String src) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        digest.update(src.getBytes());
        byte[] result = digest.digest();
        System.out.println(result.length);
        return result;
    }
    private static String toHex(byte[] buffer){
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<buffer.length;i++){
            int hi = ((buffer[i]>>4)&0x0f);
            int lo = buffer[i] & 0x0f;
            sb.append(hi>9?(char)(hi-10+'a'):(char)(hi+'0'));
            sb.append(lo>9?(char)(lo-10+'a'):(char)(lo+'0'));
        }
        return sb.toString();
    }

    public static String readToString(String fileName) {
        String encoding = "UTF-8";
        File file = new File(fileName);
        Long filelength = file.length();
        byte[] filecontent = new byte[filelength.intValue()];
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(filecontent);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            return new String(filecontent, encoding);
        } catch (UnsupportedEncodingException e) {
            System.err.println("The OS does not support " + encoding);
            e.printStackTrace();
            return null;
        }
    }
}
