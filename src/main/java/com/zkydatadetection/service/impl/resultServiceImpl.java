package com.unis.zkydatadetection.service.impl;

import ch.qos.logback.classic.Logger;
import com.alibaba.druid.support.json.JSONUtils;
import com.unis.zkydatadetection.service.statisticsService;
import jdk.nashorn.internal.parser.JSONParser;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import com.unis.zkydatadetection.service.resultService;
import com.unis.zkydatadetection.service.baseDataService;

import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service(value = "resultService")
public class resultServiceImpl implements resultService{
    private final Logger log = (Logger) LoggerFactory.getLogger("resultServiceImpl.class");
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    private baseDataService bds;

    @Autowired
    private statisticsService ss;

    @Autowired
    private resultService rs;

    @Override
    public boolean addResultInField(Map excelAttr, List contrastFieldList) {
        boolean result = true;
        try{
            String unitsys = excelAttr.get("unitsys").toString();
            String libcode = excelAttr.get("libcode").toString();
            String arcLvl = excelAttr.get("arcLvl").toString();
            String excelNotInAmsStr="";
            String amsNotInExcelStr="";
            String errormessage="";
            List excelNotInAms = (List) contrastFieldList.get(0);
            List amsNotInExcel = (List) contrastFieldList.get(1);
            String createtime =this.getDate();
            if(excelNotInAms.size()>0) {
                excelNotInAmsStr = org.apache.commons.lang3.StringUtils.join(excelNotInAms, ",");
                errormessage ="导入的"+arcLvl+"级excel元数据以下字段不在档案系统中"+ excelNotInAmsStr+"；";
            }
            if(amsNotInExcel.size()>0) {
                amsNotInExcelStr = org.apache.commons.lang3.StringUtils.join(amsNotInExcel, ",");
                errormessage = errormessage+"档案系统中以下字段不在导入的"+arcLvl+"级excel元数据中"+ amsNotInExcelStr+"；";
            }

            String tablename = "s_"+arcLvl+libcode+"_"+unitsys+"_result";
            String syscode = this.getCode();
            StringBuffer addResultInFieldSql = new StringBuffer();
            addResultInFieldSql.append("insert into ").append(tablename).append("syscode,libcode,unitsys,archivetype,errormessage,createtime,errortype,status) values ('");
            addResultInFieldSql.append(syscode).append("','").append(libcode).append("','").append(unitsys).append("','").append(arcLvl).append("','");
            addResultInFieldSql.append(errormessage).append("','").append(createtime).append("',1,0)");
            log.info("检测出问题数据写入数据库 = "+addResultInFieldSql.toString());
            jdbcTemplate.execute(addResultInFieldSql.toString());
        }catch(Exception e){
            log.error(e.getMessage());
            result=false;
        }
        return result;
    }

    @Override
    public boolean addResultInValue(Map<String,Object> checkResult) {
       try{
//           String unitsys = checkResult.get("unitsys").toString();
//           String libcode = checkResult.get("libcode").toString();
           String arcLvl = checkResult.get("arclvl").toString();
           String createtime =this.getDate();
           String syscode = this.getCode();
           checkResult.put("syscode",syscode);
           checkResult.put("createtime",createtime);
           checkResult.put("status",0);
           String field = "";
           String value = "";
           for(Map.Entry<String,Object> vo : checkResult.entrySet()){
               if("arclvl".equals(vo.getKey())){
                   continue;
               }else{
                   field = field+vo.getKey()+",";
                   value = value+"'"+vo.getValue()+"',";
               }
           }
           field = field.substring(0,field.length()-1);
           value = value.substring(0,value.length()-1);
           String tablename = "s_"+arcLvl+"result";
           StringBuffer addResultInFieldSql = new StringBuffer();
           //addResultInFieldSql.append("insert into ").append(tablename).append ("syscode,libcode,unitsys,archivetype,errormessage,createtime,errortype,status) values ('");
           //addResultInFieldSql.append(syscode).append("','").append(libcode).append("','").append(unitsys).append("','").append(arcLvl).append("','");
           //addResultInFieldSql.append(errormessage).append("','").append(createtime).append("',1,0)");
           addResultInFieldSql.append("insert into ").append(tablename).append("(");
           addResultInFieldSql.append(field).append (") values (").append(value).append(")");
           log.info("检测出问题数据写入数据库 = "+addResultInFieldSql.toString());
           jdbcTemplate.execute(addResultInFieldSql.toString());
       }catch (Exception e){
           log.info(e.getMessage());
           return false;
       }
        return true;
    }

    @Override
    public boolean addSipResultInValue(Map<String, Object> checkResult) throws Exception {
//        try{
            String libcode = checkResult.get("libcode").toString();
//            String arcLvl = checkResult.get("arcLvl").toString();
            String createtime =this.getDate();
            String syscode = this.getCode();
            checkResult.put("syscode",syscode);
            checkResult.put("createtime",createtime);
            checkResult.put("status",0);
            String field = "";
            String value = "";
            for(Map.Entry<String,Object> vo : checkResult.entrySet()){
                field = field+vo.getKey()+",";
                value = value+"'"+vo.getValue()+"',";
            }
            field = field.substring(0,field.length()-1);
            value = value.substring(0,value.length()-1);
            String tablename = "s_sipresult";
            StringBuffer addResultInFieldSql = new StringBuffer();
            addResultInFieldSql.append("insert into ").append(tablename).append("(");
            addResultInFieldSql.append(field).append (") values (").append(value).append(")");
            log.info("检测出问题数据写入数据库 = "+addResultInFieldSql.toString());
            jdbcTemplate.execute(addResultInFieldSql.toString());
//        }catch (Exception e){
//            log.info(e.getMessage());
//            return false;
//        }
        return true;
    }

    @Override
    public int getCount(String tablename, String whereStr) {
        String where = "";
        if(whereStr==null || "".equals(whereStr)){
            where =" 1=1";
        }else {
            String[] whereStrs = whereStr.split(" ~ ");
            where = "createtime>='"+whereStrs[0]+"' and createtime<='"+whereStrs[1]+"'";
        }
        String getCountSql = "select count(1) from "+tablename+" where "+where;
        log.info("获取数据表条目总量sql = "+getCountSql);
        int i = jdbcTemplate.queryForObject(getCountSql, Integer.class);
        return i;
    }

    @Override
    public int getCacheCount(String tablename, String whereStr) {
        String where = "";
        if(whereStr==null || "".equals(whereStr)){
            where =" 1=1";
        }else{
            where = whereStr;
        }
        String getCountSql = "select count(1) from "+tablename+" where "+where;
        log.info("获取数据表条目总量sql = "+getCountSql);
        int i = jdbcTemplate.queryForObject(getCountSql, Integer.class);
        return i;
    }

    @Override
    public int getCheckDataCount(String tablename, String whereStr,String ifHistory) {
        String where = "";
        if(whereStr==null || "".equals(whereStr)){
            where =" 1=1";
        }else{
            where = whereStr;
        }
//        else {
//            String[] whereStrs = whereStr.split(" ~ ");
//            where = "createtime>='"+whereStrs[0]+"' and createtime<='"+whereStrs[1]+"'";
//        }
        if("0".equals(ifHistory)){
            where = where+" and status = 0";
        }else if("1".equals(ifHistory)){
            where = where+" and status in (0,1)";
        }
//        where = where+" and status in (0,1)";
        String getCountSql = "select count(1) from "+tablename+" where "+where;
        log.info("获取数据表条目总量sql = "+getCountSql);
        int i = jdbcTemplate.queryForObject(getCountSql, Integer.class);
        return i;
    }

    @Override
    public void deleteSql(String tablename, String whereStr) {
        String deleteSql = "delete from "+tablename+" where "+whereStr;
        log.info("删除数据sql = "+deleteSql);
        jdbcTemplate.execute(deleteSql);
    }

    @Override
    public int addField(String[] excelTitles, Map excelAttr) {
        String unitsys = excelAttr.get("unitsys").toString();
        String libcode = excelAttr.get("libcode").toString();
        String arcLvl = excelAttr.get("arcLvl").toString();
        String archiveType = excelAttr.get("archiveType").toString();
        String syscode = this.getCode();
        if ("xd".equals(archiveType)){
            if("vol".equals(arcLvl)){
                String whereStr = "archiveType='"+archiveType+"' and arcLvl='"+arcLvl+"'";
                int i = this.getCacheCount("excelfile",whereStr);
                if(i>0){
                    this.deleteSql("excelfile",whereStr);
                }
            }
            if("file".equals(arcLvl)){
                String whereStr = "archiveType='"+archiveType+"' and arcLvl='"+arcLvl+"'";
                int i = this.getCacheCount("excelfile",whereStr);
                if(i>0){
                    this.deleteSql("excelfile",whereStr);
                }
            }
            if("efile".equals(arcLvl)){
                String whereStr = "archiveType='"+archiveType+"' and arcLvl='"+arcLvl+"'";
                int i = this.getCacheCount("excelfile",whereStr);
                if(i>0){
                    this.deleteSql("excelfile",whereStr);
                }
            }
        }
        if ("eq".equals(archiveType)){
            if("vol".equals(arcLvl)){
                String whereStr = "archiveType='"+archiveType+"' and arcLvl='"+arcLvl+"'";
                int i = this.getCacheCount("excelfile",whereStr);
                if(i>0){
                    this.deleteSql("excelfile",whereStr);
                }
            }
            if("file".equals(arcLvl)){
                String whereStr = "archiveType='"+archiveType+"' and arcLvl='"+arcLvl+"'";
                int i = this.getCacheCount("excelfile",whereStr);
                if(i>0){
                    this.deleteSql("excelfile",whereStr);
                }
            }
            if("efile".equals(arcLvl)){
                String whereStr = "archiveType='"+archiveType+"' and arcLvl='"+arcLvl+"'";
                int i = this.getCacheCount("excelfile",whereStr);
                if(i>0){
                    this.deleteSql("excelfile",whereStr);
                }
            }
        }
        for(int i =0 ; i<excelTitles.length;i++){
            String insertExcelData = "insert into excelfield (syscode,fieldname,arclvl,archiveType) value ('"+syscode+"','"+excelTitles[i]+"','"+arcLvl+""+"','"+archiveType+"')";
            jdbcTemplate.execute(insertExcelData);
        }
        return 0;
    }

    @Override
    public int addExcelData(Map<Integer, Map<String, Object>> result, Map excelAttr) throws Exception{
        String unitsys = excelAttr.get("unitsys").toString();
        String libcode = excelAttr.get("libcode").toString();
        String where = "libcode = '"+libcode+"'";
        String libname = "";
        List libnameL = rs.getData("chname","s_arc",where);
        if(libnameL.size()>0){
            Map libnameM = (Map)libnameL.get(0);
            if(libnameM.get("chname")!=null && !"".equals(libnameM.get("chname"))) {
                libname = libnameM.get("chname").toString();
            }
        }
        String arcLvl = excelAttr.get("arcLvl").toString();
        String archiveType = excelAttr.get("archiveType").toString();
        if("xd".equals(archiveType)) {
            if(libname.contains("照片")){
                if("file".equals(arcLvl)){
                    String whereStr = "libcode ='" + libcode + "'";
                    int res = this.getCacheCount("xdfiledata",whereStr);
                    if(res>0){
                        this.deleteSql("xdfiledata",whereStr);
                    }
                    for (int i = 1; i <= result.size(); i++) {
                        Map resultm = (Map) result.get(i);
                        String keyword = "";
                        if(resultm.get("keyword")!=null && !"".equals(resultm.get("keyword"))) {
                            keyword = resultm.get("keyword").toString().trim();//先导专项档号
                        }
                        String filetitle = "";
                        if(resultm.get("title")!=null && !"".equals(resultm.get("title"))) {
                            filetitle = resultm.get("title").toString().trim();//先导文件题名
                        }
                        String rq = "";
                        if(resultm.get("rq")!=null && !"".equals(resultm.get("rq"))) {
                            rq = resultm.get("rq").toString().trim();//先导照片摄制时间
                        }
                        String zrz = "";
                        if(resultm.get("zrz")!=null && !"".equals(resultm.get("zrz"))) {
                            zrz = resultm.get("zrz").toString().trim();//先导照片摄制者
                        }
                        String tzzsStr = "";
                        int tzzs = 0;
                        if(resultm.get("tzzs")!=null && !"".equals(resultm.get("tzzs"))) {
                            tzzsStr = resultm.get("tzzs").toString().trim();
                            if(tzzsStr.contains(".")){
                                tzzsStr = tzzsStr.substring(0,tzzsStr.lastIndexOf("."));
                            }
                            tzzs =Integer.valueOf(tzzsStr);//先导照片图纸张数
                        }
                        String efilegs = "";
                        if(resultm.get("efilegs")!=null && !"".equals(resultm.get("efilegs"))) {
                            efilegs = resultm.get("efilegs").toString().trim();//先导照片文件格式
                        }
                        String mj = "";
                        if(resultm.get("mj")!=null && !"".equals(resultm.get("mj"))) {
                            mj = resultm.get("mj").toString().trim();//先导照片密级
                            if (mj != null && !"".equals(mj)) {
                                List mjList = bds.getParamMJ("");
                                for (int m = 0; m < mjList.size(); m++) {
                                    if (mjList.get(m) != null && !"".equals(mjList.get(m))) {
                                        String mjValue = mjList.get(m).toString();
                                        if (mjValue.contains(mj)) {
                                            mj = mjValue;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        String bmqx ="";
                        if(resultm.get("bmqx")!=null && !"".equals(resultm.get("bmqx"))) {
                            bmqx = resultm.get("bmqx").toString().trim();//先导照片保密期限
                        }
                        String syscode = this.getCode();
                        StringBuffer addExcelDataSql = new StringBuffer();
                        addExcelDataSql.append("insert into xdfiledata (syscode,title,rq,zrz,tzzs,efilegs,mj,bmqx,keyword,libcode,row) value ('");
                        addExcelDataSql.append(syscode).append("','").append(filetitle).append("','").append(rq).append("','");
                        addExcelDataSql.append(zrz).append("',").append(tzzs).append(",'").append(efilegs).append("','").append(mj);
                        addExcelDataSql.append("','").append(bmqx).append("','").append(keyword).append("','").append(libcode).append("',").append(i).append(")");
                        log.info("先导专项照片档案文件excel数据写入临时库sql = "+addExcelDataSql.toString());
                        jdbcTemplate.execute(addExcelDataSql.toString());
                        //写入统计表
                        String resultTablename = "s_statistics";
                        Map statMap = new HashMap();
                        statMap.put("tablename",resultTablename);
                        statMap.put("libcode",libcode);
                        statMap.put("arcLvl",arcLvl);
                        statMap.put("mj",mj);
                        statMap.put("js",1);
                        statMap.put("keyword",keyword);
                        statMap.put("ys",tzzs);
                        statMap.put("hfs","");
                        statMap.put("archiveType",archiveType);
                        ss.addDataStat(statMap);
                    }
                }
            } else  if(libname.contains("音视频") || libname.contains("音频") || libname.contains("视频")){
                String whereStr = "libcode ='" + libcode + "'";
                int res = this.getCacheCount("xdfiledata",whereStr);
                if(res>0){
                    this.deleteSql("xdfiledata",whereStr);
                }
                for (int i = 1; i <= result.size(); i++) {
                    Map resultm = (Map) result.get(i);
                    String keyword = "";
                    if(resultm.get("keyword")!=null && !"".equals(resultm.get("keyword"))) {
                        keyword = resultm.get("keyword").toString().trim();//先导音视频专项档号
                    }
                    String filetitle = "";
                    if(resultm.get("title")!=null && !"".equals(resultm.get("title"))) {
                        filetitle = resultm.get("title").toString().trim();//先导音视频文件题名
                    }
                    String rq = "";
                    if(resultm.get("rq")!=null && !"".equals(resultm.get("rq"))) {
                        rq = resultm.get("rq").toString().trim();//先导音视频摄制时间
                    }
                    String zrz = "";
                    if(resultm.get("zrz")!=null && !"".equals(resultm.get("zrz"))) {
                        zrz = resultm.get("zrz").toString().trim();//先导音视频摄制者
                    }
                    String sc = "";
                    if(resultm.get("sc")!=null && !"".equals(resultm.get("sc"))) {
                        sc = resultm.get("sc").toString().trim();//先导音视频时长
                    }
                    String efilesize = "";
                    if(resultm.get("efilesize")!=null && !"".equals(resultm.get("efilesize"))) {
                        efilesize = resultm.get("efilesize").toString().trim();//先导音视频容量
                    }
                    String mj = "";
                    if(resultm.get("mj")!=null && !"".equals(resultm.get("mj"))) {
                        mj = resultm.get("mj").toString().trim();//先导音视频密级
                        if (mj != null && !"".equals(mj)) {
                            List mjList = bds.getParamMJ("");
                            for (int m = 0; m < mjList.size(); m++) {
                                if (mjList.get(m) != null && !"".equals(mjList.get(m))) {
                                    String mjValue = mjList.get(m).toString();
                                    if (mjValue.contains(mj)) {
                                        mj = mjValue;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    String bmqx = "";
                    if(resultm.get("bmqx")!=null && !"".equals(resultm.get("bmqx"))) {
                        bmqx = resultm.get("bmqx").toString().trim();//先导音视频保密期限
                    }
                    String efilegs = "";
                    if(resultm.get("efilegs")!=null && !"".equals(resultm.get("efilegs"))) {
                        efilegs = resultm.get("efilegs").toString().trim();//先导音视频文件格式
                    }
                    String syscode = this.getCode();
                    StringBuffer addExcelDataSql = new StringBuffer();
                    addExcelDataSql.append("insert into xdfiledata (syscode,title,rq,zrz,sc,efilesize,efilegs,mj,bmqx,keyword,libcode,row) value ('");
                    addExcelDataSql.append(syscode).append("','").append(filetitle).append("','").append(rq).append("','");
                    addExcelDataSql.append(zrz).append("','").append(sc).append(",'").append(efilesize).append("','").append(efilegs).append("','").append(mj);
                    addExcelDataSql.append("','").append(bmqx).append("','").append(keyword).append("','").append(libcode).append("',").append(i).append(")");
                    log.info("先导专项音视频档案文件excel数据写入临时库sql = "+addExcelDataSql.toString());
                    jdbcTemplate.execute(addExcelDataSql.toString());
                    //写入统计表
                    String resultTablename = "s_statistics";
                    Map statMap = new HashMap();
                    statMap.put("tablename",resultTablename);
                    statMap.put("libcode",libcode);
                    statMap.put("arcLvl",arcLvl);
                    statMap.put("mj",mj);
                    statMap.put("js",1);
                    statMap.put("keyword",keyword);
                    statMap.put("ys","");
                    statMap.put("hfs","");
                    statMap.put("archiveType",archiveType);
                    ss.addDataStat(statMap);
                }
            } else if(libname.contains("电子文件")){
                String whereStr = "libcode ='" + libcode + "'";
                int res = this.getCacheCount("xdfiledata",whereStr);
                if(res>0){
                    this.deleteSql("xdfiledata",whereStr);
                }
                for (int i = 1; i <= result.size(); i++) {
                    Map resultm = (Map) result.get(i);
                    String keyword ="";
                    if(resultm.get("keyword")!=null && !"".equals(resultm.get("keyword"))) {
                        keyword = resultm.get("keyword").toString().trim();//先导电子专项档号
                    }
                    String filetitle = "";
                    if(resultm.get("title")!=null && !"".equals(resultm.get("title"))) {
                        filetitle = resultm.get("title").toString().trim();//先导电子题名
                    }
                    String wenhao = "";
                    if(resultm.get("wenhao")!=null && !"".equals(resultm.get("wenhao"))) {
                        wenhao = resultm.get("wenhao").toString().trim();//先导电子文件编号
                    }
                    String zrz = "";
                    if(resultm.get("zrz")!=null && !"".equals(resultm.get("zrz"))) {
                        zrz = resultm.get("zrz").toString().trim();//先导电子责任者
                    }
                    String rq = "";
                    if(resultm.get("rq")!=null && !"".equals(resultm.get("rq"))) {
                        rq = resultm.get("rq").toString().trim();//先导电子日期
                    }
                    //先导电子文件页数
                    String pagenumStr = "";
                    int pagenum = 0;
                    if(resultm.get("pagenum")!=null && !"".equals(resultm.get("pagenum"))) {
                        pagenumStr = resultm.get("pagenum").toString().trim();
                        if (pagenumStr.contains(".")) {
                            pagenumStr = pagenumStr.substring(0, pagenumStr.lastIndexOf("."));
                        }
                        pagenum = Integer.valueOf(pagenumStr);//先导电子文件页数
                    }
                    String mj = "";
                    if(resultm.get("mj")!=null && !"".equals(resultm.get("mj"))) {
                        mj = resultm.get("mj").toString().trim();//先导电子密级
                        if (mj != null && !"".equals(mj)) {
                            List mjList = bds.getParamMJ("");
                            for (int m = 0; m < mjList.size(); m++) {
                                if (mjList.get(m) != null && !"".equals(mjList.get(m))) {
                                    String mjValue = mjList.get(m).toString();
                                    if (mjValue.contains(mj)) {
                                        mj = mjValue;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    String bmqx = "";
                    if(resultm.get("bmqx")!=null && !"".equals(resultm.get("bmqx"))) {
                        bmqx = resultm.get("bmqx").toString().trim();//先导电子保密期限
                    }
                    String efilesize = "";
                    if(resultm.get("efilesize")!=null && !"".equals(resultm.get("efilesize"))) {
                        efilesize = resultm.get("efilesize").toString().trim();//先导电子容量
                    }
                    String efilegs = "";
                    if(resultm.get("efilegs")!=null && !"".equals(resultm.get("efilegs"))) {
                        efilegs = resultm.get("efilegs").toString().trim();//先导电子文件格式
                    }
                    String syscode = this.getCode();
                    StringBuffer addExcelDataSql = new StringBuffer();
                    addExcelDataSql.append("insert into xdfiledata (syscode,title,wenhao,rq,zrz,pagenum,efilegs,mj,bmqx,efilesize,keyword,libcode,row) value ('");
                    addExcelDataSql.append(syscode).append("','").append(filetitle).append("','").append(wenhao).append("','").append(rq).append("','");
                    addExcelDataSql.append(zrz).append("',").append(pagenum).append(",'").append(efilegs).append("','").append(mj);
                    addExcelDataSql.append("','").append(bmqx).append("','").append(efilesize).append("','").append(keyword).append("','").append(libcode).append("',").append(i).append(")");
                    log.info("先导专项电子文件档案文件excel数据写入临时库sql = "+addExcelDataSql.toString());
                    jdbcTemplate.execute(addExcelDataSql.toString());
                    //写入统计表
                    String resultTablename = "s_statistics";
                    Map statMap = new HashMap();
                    statMap.put("tablename",resultTablename);
                    statMap.put("libcode",libcode);
                    statMap.put("arcLvl",arcLvl);
                    statMap.put("mj",mj);
                    statMap.put("js",1);
                    statMap.put("keyword",keyword);
                    statMap.put("ys","");
                    statMap.put("hfs","");
                    statMap.put("archiveType",archiveType);
                    ss.addDataStat(statMap);
                }
            }else {
                if("vol".equals(arcLvl)){
                    String whereStr = "libcode ='" + libcode + "'";
                    int res = this.getCacheCount("xdvoldata",whereStr);
                    if(res>0){
                        this.deleteSql("xdvoldata",whereStr);
                    }
                    for (int i = 1; i <= result.size(); i++) {
                        Map resultm = (Map) result.get(i);
                        String keyword ="";
                        if(resultm.get("keyword")!=null && !"".equals(resultm.get("keyword"))) {
                            keyword = resultm.get("keyword").toString().trim();//先导专项档号
                        }
                        String voltitle = "";
                        if(resultm.get("title")!=null && !"".equals(resultm.get("title"))) {
                            voltitle = resultm.get("title").toString().trim();//先导案卷题名
                        }
                        String qsrq = "";
                        if(resultm.get("qsrq")!=null && !"".equals(resultm.get("qsrq"))) {
                            qsrq = resultm.get("qsrq").toString().trim();//先导起始日期
                        }
                        String zzrq = "";
                        if(resultm.get("zzrq")!=null && !"".equals(resultm.get("zzrq"))) {
                            zzrq = resultm.get("zzrq").toString().trim();//先导终止日期
                        }
                        //先导总件数
                        String zjsStr = "";
                        int zjs = 0;
                        if(resultm.get("zjs")!=null && !"".equals(resultm.get("zjs"))) {
                            zjsStr = resultm.get("zjs").toString().trim();
                            if (zjsStr.contains(".")) {
                                zjsStr = zjsStr.substring(0, zjsStr.lastIndexOf("."));
                            }
                            zjs = Integer.valueOf(zjsStr);
                        }
                        //先导总页数
                        String zysStr = "";
                        int zys = 0;
                        if(resultm.get("zys")!=null && !"".equals(resultm.get("zys"))) {
                            zysStr = resultm.get("zys").toString().trim();
                            if (zysStr.contains(".")) {
                                zysStr = zysStr.substring(0, zysStr.lastIndexOf("."));
                            }
                            zys = Integer.valueOf(zysStr);
                        }
                        String mj = "";
                        if(resultm.get("mj")!=null && !"".equals(resultm.get("mj"))) {
                            mj = resultm.get("mj").toString().trim();//先导密级
                            if (mj != null && !"".equals(mj)) {
                                List mjList = bds.getParamMJ("");
                                for (int m = 0; m < mjList.size(); m++) {
                                    if (mjList.get(m) != null && !"".equals(mjList.get(m))) {
                                        String mjValue = mjList.get(m).toString();
                                        if (mjValue.contains(mj)) {
                                            mj = mjValue;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        String syscode = this.getCode();
                        StringBuffer addExcelDataSql = new StringBuffer();
                        addExcelDataSql.append("insert into xdvoldata (syscode,title,qsrq,zzrq,zjs,zys,mj,keyword,libcode,row) value ('");
                        addExcelDataSql.append(syscode).append("','").append(voltitle).append("','");
                        addExcelDataSql.append(qsrq).append("','").append(zzrq).append("',").append(zjs);
                        addExcelDataSql.append(",").append(zys).append(",'").append(mj).append("','").append(keyword).append("','").append(libcode).append("',").append(i).append(")");
                        log.info("先导专项案卷excel数据写入临时库sql = "+addExcelDataSql.toString());
                        jdbcTemplate.execute(addExcelDataSql.toString());
                        //写入统计表
                        String resultTablename = "s_statistics";
                        Map statMap = new HashMap();
                        statMap.put("tablename",resultTablename);
                        statMap.put("libcode",libcode);
                        statMap.put("arcLvl",arcLvl);
                        statMap.put("mj",mj);
                        statMap.put("zjs",zjs);
                        statMap.put("keyword",keyword);
                        statMap.put("zys",zys);
                        statMap.put("volnum",1);
                        statMap.put("hfs","");
                        statMap.put("archiveType",archiveType);
                        ss.addDataStat(statMap);
                    }
                }
                if("file".equals(arcLvl)){
                    String whereStr = "libcode ='" + libcode + "'";
                    int res = this.getCacheCount("xdfiledata",whereStr);
                    if(res>0){
                        this.deleteSql("xdfiledata",whereStr);
                    }
                    for (int i = 1; i <= result.size(); i++) {
                        Map resultm = (Map) result.get(i);
                        String keyword ="";
                        if(resultm.get("keyword")!=null && !"".equals(resultm.get("keyword"))) {
                            keyword = resultm.get("keyword").toString().trim();//先导文件专项档号
                        }
                        String filetitle = "";
                        if(resultm.get("title")!=null && !"".equals(resultm.get("title"))) {
                            filetitle = resultm.get("title").toString().trim();//先导文件题名
                        }
                        String rq = "";
                        if(resultm.get("rq")!=null && !"".equals(resultm.get("rq"))) {
                            rq = resultm.get("rq").toString().trim();//先导文件日期
                        }
                        String zrz = "";
                        if(resultm.get("zrz")!=null && !"".equals(resultm.get("zrz"))) {
                            zrz = resultm.get("zrz").toString().trim();//先导文件责任者
                        }
                        //先导文件页数
                        String pagenumStr = "";
                        int pagenum = 0;
                        if(resultm.get("pagenum")!=null && !"".equals(resultm.get("pagenum"))) {
                            pagenumStr = resultm.get("pagenum").toString().trim();
                            if (pagenumStr.contains(".")) {
                                pagenumStr = pagenumStr.substring(0, pagenumStr.lastIndexOf("."));
                            }
                            pagenum = Integer.valueOf(pagenumStr);
                        }
                        String yh = "";
                        if(resultm.get("yh")!=null && !"".equals(resultm.get("yh"))) {
                            yh = resultm.get("yh").toString().trim();//先导文件页号
                        }
                        String mj = "";
                        if(resultm.get("mj")!=null && !"".equals(resultm.get("mj"))) {
                            mj = resultm.get("mj").toString().trim();//先导密级
                            if (mj != null && !"".equals(mj)) {
                                List mjList = bds.getParamMJ("");
                                for (int m = 0; m < mjList.size(); m++) {
                                    if (mjList.get(m) != null && !"".equals(mjList.get(m))) {
                                        String mjValue = mjList.get(m).toString();
                                        if (mjValue.contains(mj)) {
                                            mj = mjValue;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        String bmqx = "";
                        if(resultm.get("bmqx")!=null && !"".equals(resultm.get("bmqx"))) {
                            bmqx = resultm.get("bmqx").toString().trim();//先导文件保密期限
                        }
                        String syscode = this.getCode();
                        StringBuffer addExcelDataSql = new StringBuffer();
                        addExcelDataSql.append("insert into xdfiledata (syscode,title,rq,zrz,pagenum,yh,mj,bmqx,keyword,libcode,row) value ('");
                        addExcelDataSql.append(syscode).append("','").append(filetitle).append("','").append(rq).append("','");
                        addExcelDataSql.append(zrz).append("',").append(pagenum).append(",'").append(yh).append("','").append(mj);
                        addExcelDataSql.append("','").append(bmqx).append("','").append(keyword).append("','").append(libcode).append("',").append(i).append(")");
                        log.info("先导专项卷内文件excel数据写入临时库sql = "+addExcelDataSql.toString());
                        jdbcTemplate.execute(addExcelDataSql.toString());
                        //写入统计表
                        String resultTablename = "s_statistics";
                        Map statMap = new HashMap();
                        statMap.put("tablename",resultTablename);
                        statMap.put("libcode",libcode);
                        statMap.put("arcLvl",arcLvl);
                        statMap.put("mj",mj);
                        statMap.put("js",1);
                        statMap.put("keyword",keyword);
                        statMap.put("ys","");
                        statMap.put("hfs","");
                        statMap.put("archiveType",archiveType);
                        ss.addDataStat(statMap);
                    }
                }
            }
        }
        if("eq".equals(archiveType)) {
            if(libname.contains("文书")){
                if("vol".equals(arcLvl)) {
                    String whereStr = "libcode ='" + libcode + "'";
                    int res = this.getCacheCount("eqvoldata", whereStr);
                    if (res > 0) {
                        this.deleteSql("eqvoldata", whereStr);
                    }
                    for (int i = 1; i <= result.size(); i++) {
                        Map<String,Object> resultm = (Map) result.get(i);
                        String syscode = this.getCode();
                        resultm.put("syscode",syscode);
                        resultm.put("libcode",libcode);
                        String field = "";
                        String value = "";
                        String mj = "";
                        String keyword = "";
                        int zjs = 0;
                        int zys = 0;
                        int hfs = 0;
                        for(Map.Entry<String,Object> vo : resultm.entrySet()){
                            field = field+vo.getKey()+",";
                            if(vo.getValue()!=null && !"".equals(vo.getValue())){
                                if(vo.getValue().toString().contains(".") && !"mj".equals(vo.getKey())){
                                    String v = vo.getValue().toString();
                                    v = v.substring(0, v.lastIndexOf("."));
                                    int vv = Integer.valueOf(v);
                                    value = value+vv+",";
                                    if("zjs".equals(vo.getKey())){
                                        zjs = vv;
                                    }
                                    if("zys".equals(vo.getKey())){
                                        zys = vv;
                                    }
                                    if("hfs".equals(vo.getKey())){
                                        hfs = vv;
                                    }
                                }else if("mj".equals(vo.getKey())){
                                    String v = vo.getValue().toString().trim();
                                    List mjList = bds.getParamMJ("");
                                    boolean mjb = false;
                                    for (int m = 0; m < mjList.size(); m++) {
                                        if (mjList.get(m) != null && !"".equals(mjList.get(m))) {
                                            String mjValue = mjList.get(m).toString();
                                            if (mjValue.contains(v)) {
                                                v = mjValue;
                                                value = value+"'"+v+"',";
                                                mj = v;
                                                mjb = true;
                                                break;
                                            }
                                        }
                                    }
                                    if(!mjb){
                                        value = value+"'"+vo.getValue().toString().trim()+"',";
                                    }
                                }else{
                                    value = value+"'"+vo.getValue().toString().trim()+"',";
                                    if("keyword".equals(vo.getKey())){
                                        keyword = vo.getValue().toString().trim();
                                    }
                                    if("zjs".equals(vo.getKey())){
                                        zjs = Integer.valueOf(vo.getValue().toString().trim());
                                    }
                                    if("zys".equals(vo.getKey())){
                                        zys = Integer.valueOf(vo.getValue().toString().trim());
                                    }
                                    if("hfs".equals(vo.getKey())){
                                        hfs = Integer.valueOf(vo.getValue().toString().trim());
                                    }
                                }
                            }else{
                                value = value+"'',";
                            }

                        }
//                        field = field.substring(0,field.length()-1);
//                        value = value.substring(0,value.length()-1);
                        field = field+"row";
                        value = value+i;
                        StringBuffer addResultInFieldSql = new StringBuffer();
                        addResultInFieldSql.append("insert into eqvoldata (");
                        addResultInFieldSql.append(field).append (") values (").append(value).append(")");
                        log.info("检测出问题数据写入数据库 = "+addResultInFieldSql.toString());
                        this.jdbcTemplate.execute(addResultInFieldSql.toString());
                        //写入统计表
                        String resultTablename = "s_statistics";
                        Map statMap = new HashMap();
                        statMap.put("tablename",resultTablename);
                        statMap.put("libcode",libcode);
                        statMap.put("arcLvl",arcLvl);
                        statMap.put("mj",mj);
                        statMap.put("zjs",zjs);
                        statMap.put("keyword",keyword);
                        statMap.put("zys",zys);
                        statMap.put("volnum",1);
                        statMap.put("hfs",hfs);
                        statMap.put("archiveType",archiveType);
                        ss.addDataStat(statMap);
                    }
                }
                if("file".equals(arcLvl)){
                    String whereStr = "libcode ='" + libcode + "'";
                    int res = this.getCacheCount("eqfiledata",whereStr);
                    if(res>0){
                        this.deleteSql("eqfiledata",whereStr);
                    }
                    for (int i = 1; i <= result.size(); i++) {
                        Map<String,Object> resultm = (Map) result.get(i);
                        String syscode = this.getCode();
                        resultm.put("syscode",syscode);
                        resultm.put("libcode",libcode);
                        String field = "";
                        String value = "";
                        String mj = "";
                        String keyword = "";
                        int pagenum = 0;
                        int hfs = 0;
                        for(Map.Entry<String,Object> vo : resultm.entrySet()){
                            field = field+vo.getKey()+",";
                            if(vo.getValue()!=null && !"".equals(vo.getValue())){
                                if(vo.getValue().toString().contains(".") && !"mj".equals(vo.getKey())){
                                    String v = vo.getValue().toString();
                                    v = v.substring(0, v.lastIndexOf("."));
                                    int vv = Integer.valueOf(v);
                                    value = value+vv+",";
                                    if("pagenum".equals(vo.getKey())){
                                        pagenum = vv;
                                    }
                                    if("hfs".equals(vo.getKey())){
                                        hfs = vv;
                                    }
                                }else if("mj".equals(vo.getKey())){
                                    String v = vo.getValue().toString().trim();
                                    List mjList = bds.getParamMJ("");
                                    boolean mjb = false;
                                    for (int m = 0; m < mjList.size(); m++) {
                                        if (mjList.get(m) != null && !"".equals(mjList.get(m))) {
                                            String mjValue = mjList.get(m).toString();
                                            if (mjValue.contains(v)) {
                                                v = mjValue;
                                                value = value+"'"+v+"',";
                                                mj = v;
                                                mjb = true;
                                                break;
                                            }
                                        }
                                    }
                                    if(!mjb){
                                        value = value+"'"+vo.getValue().toString().trim()+"',";
                                    }
                                }else{
                                    value = value+"'"+vo.getValue().toString().trim()+"',";
                                    if("keyword".equals(vo.getKey())){
                                        keyword = vo.getValue().toString().trim();
                                    }
                                    if("pagenum".equals(vo.getKey())){
                                        pagenum = Integer.valueOf(vo.getValue().toString().trim());
                                    }
                                    if("hfs".equals(vo.getKey())){
                                        hfs = Integer.valueOf(vo.getValue().toString().trim());
                                    }
                                }
                            }else{
                                value = value+"'',";
                            }
                        }
//                        field = field.substring(0,field.length()-1);
//                        value = value.substring(0,value.length()-1);
                        field = field+"row";
                        value = value+i;
                        StringBuffer addResultInFieldSql = new StringBuffer();
                        addResultInFieldSql.append("insert into eqfiledata (");
                        addResultInFieldSql.append(field).append (") values (").append(value).append(")");
                        log.info("检测出问题数据写入数据库 = "+addResultInFieldSql.toString());
                        this.jdbcTemplate.execute(addResultInFieldSql.toString());
                        //写入统计表
                        String resultTablename = "s_statistics";
                        Map statMap = new HashMap();
                        statMap.put("tablename",resultTablename);
                        statMap.put("libcode",libcode);
                        statMap.put("arcLvl",arcLvl);
                        statMap.put("mj",mj);
                        statMap.put("js",1);
                        statMap.put("keyword",keyword);
                        statMap.put("ys",pagenum);
                        statMap.put("hfs",hfs);
                        statMap.put("archiveType",archiveType);
                        ss.addDataStat(statMap);
                    }
                }
            }else if(libname.contains("科研")){
                if("vol".equals(arcLvl)) {
                    String whereStr = "libcode ='" + libcode + "'";
                    int res = this.getCacheCount("eqvoldata", whereStr);
                    if (res > 0) {
                        this.deleteSql("eqvoldata", whereStr);
                    }
                    for (int i = 1; i <= result.size(); i++) {
                        Map<String,Object> resultm = (Map) result.get(i);
                        String syscode = this.getCode();
                        resultm.put("syscode",syscode);
                        resultm.put("libcode",libcode);
                        String field = "";
                        String value = "";
                        String mj = "";
                        String keyword = "";
                        int zjs = 0;
                        int zys = 0;
                        int hfs = 0;
                        for(Map.Entry<String,Object> vo : resultm.entrySet()){
                            if("f16".equals(vo.getKey())){
                                field = field+"f9,";
                            }else if ("f22".equals(vo.getKey())){
                                field = field+"volnum,";
                            }else if ("f25".equals(vo.getKey())){
                                field = field+"f10,";
                            }else{
                                field = field+vo.getKey()+",";
                            }
                            if(vo.getValue()!=null && !"".equals(vo.getValue())){
                                if(vo.getValue().toString().contains(".") && !"mj".equals(vo.getKey())){
                                    String v = vo.getValue().toString();
                                    v = v.substring(0, v.lastIndexOf("."));
                                    int vv = Integer.valueOf(v);
                                    value = value+vv+",";
                                    if("zjs".equals(vo.getKey())){
                                        zjs = vv;
                                    }
                                    if("zys".equals(vo.getKey())){
                                        zys = vv;
                                    }
                                    if("hfs".equals(vo.getKey())){
                                        hfs = vv;
                                    }
                                }else if("mj".equals(vo.getKey())){
                                    String v = vo.getValue().toString().trim();
                                    List mjList = bds.getParamMJ("");
                                    boolean mjb = false;
                                    for (int m = 0; m < mjList.size(); m++) {
                                        if (mjList.get(m) != null && !"".equals(mjList.get(m))) {
                                            String mjValue = mjList.get(m).toString();
                                            if (mjValue.contains(v)) {
                                                v = mjValue;
                                                value = value+"'"+v+"',";
                                                mj = v;
                                                mjb = true;
                                                break;
                                            }
                                        }
                                    }
                                    if(!mjb){
                                        value = value+"'"+vo.getValue().toString().trim()+"',";
                                    }
                                }else{
                                    value = value+"'"+vo.getValue().toString().trim()+"',";
                                    if("keyword".equals(vo.getKey())){
                                        keyword = vo.getValue().toString().trim();
                                    }
                                    if("zjs".equals(vo.getKey())){
                                        zjs = Integer.valueOf(vo.getValue().toString().trim());
                                    }
                                    if("zys".equals(vo.getKey())){
                                        zys = Integer.valueOf(vo.getValue().toString().trim());
                                    }
                                    if("hfs".equals(vo.getKey())){
                                        hfs = Integer.valueOf(vo.getValue().toString().trim());
                                    }
                                }
                            }else{
                                if("zjs".equals(vo.getKey()) || "zys".equals(vo.getKey()) || "hfs".equals(vo.getKey()) || "f22".equals(vo.getKey())){
                                    value = value+"0,";
                                }else{
                                    value = value+"'',";
                                }
                            }
                        }
//                        field = field.substring(0,field.length()-1);
//                        value = value.substring(0,value.length()-1);
                        field = field+"row";
                        value = value+i;
                        StringBuffer addResultInFieldSql = new StringBuffer();
                        addResultInFieldSql.append("insert into eqvoldata (");
                        addResultInFieldSql.append(field).append (") values (").append(value).append(")");
                        log.info("检测出问题数据写入数据库 = "+addResultInFieldSql.toString());
                        this.jdbcTemplate.execute(addResultInFieldSql.toString());
                        //写入统计表
                        String resultTablename = "s_statistics";
                        Map statMap = new HashMap();
                        statMap.put("tablename",resultTablename);
                        statMap.put("libcode",libcode);
                        statMap.put("arcLvl",arcLvl);
                        statMap.put("mj",mj);
                        statMap.put("zjs",zjs);
                        statMap.put("keyword",keyword);
                        statMap.put("zys",zys);
                        statMap.put("volnum",1);
                        statMap.put("hfs",hfs);
                        statMap.put("archiveType",archiveType);
                        ss.addDataStat(statMap);
                    }
                }
                if("file".equals(arcLvl)){
                    String whereStr = "libcode ='" + libcode + "'";
                    int res = this.getCacheCount("eqfiledata",whereStr);
                    if(res>0){
                        this.deleteSql("eqfiledata",whereStr);
                    }
                    for (int i = 1; i <= result.size(); i++) {
                        Map<String,Object> resultm = (Map) result.get(i);
                        String syscode = this.getCode();
                        resultm.put("syscode",syscode);
                        resultm.put("libcode",libcode);
                        String field = "";
                        String value = "";
                        String mj = "";
                        String keyword = "";
                        int pagenum = 0;
                        int hfs = 0;
                        for(Map.Entry<String,Object> vo : resultm.entrySet()){
                            if("f8".equals(vo.getKey())){
                                field = field+"f6,";
                            }else if ("f3".equals(vo.getKey())){
                                field = field+"f4,";
                            }else if ("f4".equals(vo.getKey())){
                                field = field+"f5,";
                            }else if ("f12".equals(vo.getKey())){
                                field = field+"f9,";
                            }else if ("f6".equals(vo.getKey())){
                                field = field+"gjflh,";
                            }else if ("f13".equals(vo.getKey())){
                                field = field+"f11,";
                            }else if ("f14".equals(vo.getKey())){
                                field = field+"f10,";
                            }else{
                                field = field+vo.getKey()+",";
                            }
                            if(vo.getValue()!=null && !"".equals(vo.getValue())) {
                                if (vo.getValue().toString().contains(".") && !"mj".equals(vo.getKey())) {
                                    String v = vo.getValue().toString();
                                    v = v.substring(0, v.lastIndexOf("."));
                                    int vv = Integer.valueOf(v);
                                    value = value+vv+",";
                                    if("pagenum".equals(vo.getKey())){
                                        pagenum = vv;
                                    }
                                    if("hfs".equals(vo.getKey())){
                                        hfs = vv;
                                    }
                                }else if("mj".equals(vo.getKey())){
                                    String v = vo.getValue().toString().trim();
                                    List mjList = bds.getParamMJ("");
                                    boolean mjb = false;
                                    for (int m = 0; m < mjList.size(); m++) {
                                        if (mjList.get(m) != null && !"".equals(mjList.get(m))) {
                                            String mjValue = mjList.get(m).toString();
                                            if (mjValue.contains(v)) {
                                                v = mjValue;
                                                value = value+"'"+v+"',";
                                                mj = v;
                                                mjb = true;
                                                break;
                                            }
                                        }
                                    }
                                    if(!mjb){
                                        value = value+"'"+vo.getValue().toString().trim()+"',";
                                    }
                                }else{
                                    value = value+"'"+vo.getValue().toString().trim()+"',";
                                    if("keyword".equals(vo.getKey())){
                                        keyword = vo.getValue().toString().trim();
                                    }
                                    if("pagenum".equals(vo.getKey())){
                                        pagenum = Integer.valueOf(vo.getValue().toString().trim());
                                    }
                                    if("hfs".equals(vo.getKey())){
                                        hfs = Integer.valueOf(vo.getValue().toString().trim());
                                    }
                                }
                            }else {
                                value = value + "'',";
                            }
                        }
//                        field = field.substring(0,field.length()-1);
//                        value = value.substring(0,value.length()-1);
                        field = field+"row";
                        value = value+i;
                        StringBuffer addResultInFieldSql = new StringBuffer();
                        addResultInFieldSql.append("insert into eqfiledata (");
                        addResultInFieldSql.append(field).append (") values (").append(value).append(")");
                        log.info("检测出问题数据写入数据库 = "+addResultInFieldSql.toString());
                        this.jdbcTemplate.execute(addResultInFieldSql.toString());
                        //写入统计表
                        String resultTablename = "s_statistics";
                        Map statMap = new HashMap();
                        statMap.put("tablename",resultTablename);
                        statMap.put("libcode",libcode);
                        statMap.put("arcLvl",arcLvl);
                        statMap.put("mj",mj);
                        statMap.put("js",1);
                        statMap.put("keyword",keyword);
                        statMap.put("ys",pagenum);
                        statMap.put("hfs",hfs);
                        statMap.put("archiveType",archiveType);
                        ss.addDataStat(statMap);
                    }
                }
            }else if(libname.contains("照片")){
                if("vol".equals(arcLvl)) {
                    String whereStr = "libcode ='" + libcode + "'";
                    int res = this.getCacheCount("eqvoldata", whereStr);
                    if (res > 0) {
                        this.deleteSql("eqvoldata", whereStr);
                    }
                    for (int i = 1; i <= result.size(); i++) {
                        Map<String,Object> resultm = (Map) result.get(i);
                        String syscode = this.getCode();
                        resultm.put("syscode",syscode);
                        resultm.put("libcode",libcode);
                        String field = "";
                        String value = "";
                        String mj = "";
                        String keyword = "";
                        int zjs = 0;
                        int zys = 0;
                        int hfs = 0;
//                        Map resultm = (Map) result.get(i);
                        for(Map.Entry<String,Object> vo : resultm.entrySet()){
                            if("f12".equals(vo.getKey())){
                                field = field+"zbr,";
                            }else if ("f13".equals(vo.getKey())){
                                field = field+"zbrq,";
                            }else{
                                field = field+vo.getKey()+",";
                            }
                            if(vo.getValue()!=null && !"".equals(vo.getValue())){
                                if(vo.getValue().toString().contains(".") && !"mj".equals(vo.getKey())){
                                    String v = vo.getValue().toString();
                                    v = v.substring(0, v.lastIndexOf("."));
                                    int vv = Integer.valueOf(v);
                                    value = value+vv+",";
                                    if("zjs".equals(vo.getKey())){
                                        zjs = vv;
                                    }
                                    if("zys".equals(vo.getKey())){
                                        zys = vv;
                                    }
                                    if("hfs".equals(vo.getKey())){
                                        hfs = vv;
                                    }
                                }else if("mj".equals(vo.getKey())){
                                    String v = vo.getValue().toString().trim();
                                    List mjList = bds.getParamMJ("");
                                    boolean mjb = false;
                                    for (int m = 0; m < mjList.size(); m++) {
                                        if (mjList.get(m) != null && !"".equals(mjList.get(m))) {
                                            String mjValue = mjList.get(m).toString();
                                            if (mjValue.contains(v)) {
                                                v = mjValue;
                                                value = value+"'"+v+"',";
                                                mj = v;
                                                mjb = true;
                                                break;
                                            }
                                        }
                                    }
                                    if(!mjb){
                                        value = value+"'"+vo.getValue().toString().trim()+"',";
                                    }
                                }else{
                                    value = value+"'"+vo.getValue().toString().trim()+"',";
                                    if("keyword".equals(vo.getKey())){
                                        keyword = vo.getValue().toString().trim();
                                    }
                                    if("zjs".equals(vo.getKey())){
                                        zjs = Integer.valueOf(vo.getValue().toString().trim());
                                    }
                                    if("zys".equals(vo.getKey())){
                                        zys = Integer.valueOf(vo.getValue().toString().trim());
                                    }
                                    if("hfs".equals(vo.getKey())){
                                        hfs = Integer.valueOf(vo.getValue().toString().trim());
                                    }
                                }
                            }else{
                                if("zjs".equals(vo.getKey()) || "zys".equals(vo.getKey()) || "hfs".equals(vo.getKey()) || "f22".equals(vo.getKey())){
                                    value = value+"0,";
                                }else{
                                    value = value+"'',";
                                }
                            }
                        }
//                        field = field.substring(0,field.length()-1);
//                        value = value.substring(0,value.length()-1);
                        field = field+"row";
                        value = value+i;
                        StringBuffer addResultInFieldSql = new StringBuffer();
                        addResultInFieldSql.append("insert into eqvoldata (");
                        addResultInFieldSql.append(field).append (") values (").append(value).append(")");
                        log.info("检测出问题数据写入数据库 = "+addResultInFieldSql.toString());
                        this.jdbcTemplate.execute(addResultInFieldSql.toString());
//                        String qzh ="";
//                        if(resultm.get("unitsys")!=null && !"".equals(resultm.get("unitsys"))) {
//                            qzh = resultm.get("unitsys").toString().trim();//二期进馆全宗号
//                        }
//                        String unitname ="";
//                        if(resultm.get("unitname")!=null && !"".equals(resultm.get("unitname"))) {
//                            unitname = resultm.get("unitname").toString().trim();//二期进馆全宗名称
//                        }
//                        String keyword ="";
//                        if(resultm.get("keyword")!=null && !"".equals(resultm.get("keyword"))) {
//                            keyword = resultm.get("keyword").toString().trim();//二期进馆文件级档号
//                        }
//                        String flh ="";
//                        if(resultm.get("flh")!=null && !"".equals(resultm.get("flh"))) {
//                            flh = resultm.get("flh").toString().trim();//二期进馆分类号
//                        }
//                        String flmc ="";
//                        if(resultm.get("flname")!=null && !"".equals(resultm.get("flname"))) {
//                            flmc = resultm.get("flname").toString().trim();//二期进馆分类名称
//                        }
//                        String voltitle ="";
//                        if(resultm.get("title")!=null && !"".equals(resultm.get("title"))) {
//                            voltitle = resultm.get("title").toString().trim();//二期进馆案卷题名
//                        }
//                        String ajh ="";
//                        if(resultm.get("ajh")!=null && !"".equals(resultm.get("ajh"))) {
//                            ajh = resultm.get("ajh").toString().trim();//二期进馆案卷号
//                        }
//                        String dah ="";
//                        if(resultm.get("dah")!=null && !"".equals(resultm.get("dah"))) {
//                            dah = resultm.get("dah").toString().trim();//二期进馆档案馆序号
//                        }
                        //二期进馆总件数
//                        String zjsStr ="";
//                        int zjs =0;
//                        if(resultm.get("zjs")!=null && !"".equals(resultm.get("zjs"))) {
//                            zjsStr = resultm.get("zjs").toString().trim();
//                            if (zjsStr.contains(".")) {
//                                zjsStr = zjsStr.substring(0, zjsStr.lastIndexOf("."));
//                            }
//                            zjs = Integer.valueOf(zjsStr);
//                        }
                        //二期进馆总页数
//                        String zysStr ="";
//                        int zys =0;
//                        if(resultm.get("zys")!=null && !"".equals(resultm.get("zys"))) {
//                            zysStr = resultm.get("zys").toString().trim();
//                            if (zysStr.contains(".")) {
//                                zysStr = zysStr.substring(0, zysStr.lastIndexOf("."));
//                            }
//                            zys = Integer.valueOf(zysStr);
//                        }
                        //二期进馆画幅数
//                        String hfsStr ="";
//                        int hfs =0;
//                        if(resultm.get("hfs")!=null && !"".equals(resultm.get("hfs"))) {
//                            hfsStr = resultm.get("hfs").toString().trim();
//                            if (hfsStr.contains(".")) {
//                                hfsStr = hfsStr.substring(0, hfsStr.lastIndexOf("."));
//                            }
//                            hfs = Integer.valueOf(hfsStr);
//                        }
//                        String mj ="";
//                        if(resultm.get("mj")!=null && !"".equals(resultm.get("mj"))) {
//                            mj = resultm.get("mj").toString().trim();//二期进馆密级
//                            if (mj != null && !"".equals(mj)) {
//                                List mjList = bds.getParamMJ("");
//                                for (int m = 0; m < mjList.size(); m++) {
//                                    if (mjList.get(m) != null && !"".equals(mjList.get(m))) {
//                                        String mjValue = mjList.get(m).toString();
//                                        if (mjValue.contains(mj)) {
//                                            mj = mjValue;
//                                            break;
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                        String qsrq ="";
//                        if(resultm.get("qsrq")!=null && !"".equals(resultm.get("qsrq"))) {
//                            qsrq = resultm.get("qsrq").toString().trim();//二期进馆起始日期
//                        }
//                        String zzrq ="";
//                        if(resultm.get("zzrq")!=null && !"".equals(resultm.get("zzrq"))) {
//                            zzrq = resultm.get("zzrq").toString().trim();//二期进馆终止日期
//                        }
//                        二期进馆案卷总数
//                        String volnumStr = resultm.get("volnum").toString().trim();
//                        if(volnumStr.contains(".")){
//                            volnumStr = volnumStr.substring(0,volnumStr.lastIndexOf("."));
//                        }
//                        int volnum = Integer.valueOf(volnumStr);
//                        String szh ="";
//                        if(resultm.get("szhsm")!=null && !"".equals(resultm.get("szhsm"))) {
//                            szh = resultm.get("szhsm").toString();//二期进馆数字化情况说明
//                        }
//                        String syscode = this.getCode();
//                        StringBuffer addExcelDataSql = new StringBuffer();
//                        addExcelDataSql.append("insert into eqvoldata (syscode,unitsys,unitname,qsrq,zzrq,keyword,flh,flname,title,zjs,zys,hfs,mj,volnum,libcode,szhsm,ajh,dah,row) value ('");
//                        addExcelDataSql.append(syscode).append("','").append(qzh).append("','").append(unitname).append("','").append(qsrq).append("','").append(zzrq).append("','");
//                        addExcelDataSql.append(keyword).append("','").append(flh).append("','").append(flmc).append("','").append(voltitle).append("',").append(zjs).append(",");
//                        addExcelDataSql.append(zys).append(",").append(hfs).append(",'").append(mj).append("',").append(volnum).append(",'").append(libcode).append("','").append(szh).append("','").append(ajh).append("','").append(dah).append("',").append(i).append(")");
//                        log.info("二期进馆照片档案案卷excel数据写入临时库sql = "+addExcelDataSql.toString());
//                        jdbcTemplate.execute(addExcelDataSql.toString());
                        //写入统计表
                        String resultTablename = "s_statistics";
                        Map statMap = new HashMap();
                        statMap.put("tablename",resultTablename);
                        statMap.put("libcode",libcode);
                        statMap.put("arcLvl",arcLvl);
                        statMap.put("mj",mj);
                        statMap.put("zjs",zjs);
                        statMap.put("keyword",keyword);
                        statMap.put("zys",zys);
                        statMap.put("volnum",1);
                        statMap.put("hfs",hfs);
                        statMap.put("archiveType",archiveType);
                        ss.addDataStat(statMap);
                    }
                }
                if("file".equals(arcLvl)){
                    String whereStr = "libcode ='" + libcode + "'";
                    int res = this.getCacheCount("eqfiledata",whereStr);
                    if(res>0){
                        this.deleteSql("eqfiledata",whereStr);
                    }
                    for (int i = 1; i <= result.size(); i++) {
                        Map resultm = (Map) result.get(i);
                        String qzh ="";
                        if(resultm.get("unitsys")!=null && !"".equals(resultm.get("unitsys"))) {
                            qzh = resultm.get("unitsys").toString().trim();//二期进馆全宗号
                        }
                        String unitname ="";
                        if(resultm.get("unitname")!=null && !"".equals(resultm.get("unitname"))) {
                            unitname = resultm.get("unitname").toString().trim();//二期进馆全宗名称
                        }
                        String keyword ="";
                        if(resultm.get("keyword")!=null && !"".equals(resultm.get("keyword"))) {
                            keyword = resultm.get("keyword").toString().trim();//二期进馆文件级档号
                        }
                        String filetitle ="";
                        if(resultm.get("title")!=null && !"".equals(resultm.get("title"))) {
                            filetitle = resultm.get("title").toString().trim();//二期进馆文件题名
                        }
                        String flh ="";
                        if(resultm.get("flh")!=null && !"".equals(resultm.get("flh"))) {
                            flh = resultm.get("flh").toString().trim();//二期进馆分类号
                        }
                        String flmc ="";
                        if(resultm.get("flname")!=null && !"".equals(resultm.get("flname"))) {
                            flmc = resultm.get("flname").toString().trim();//二期进馆分类名称
                        }
                        String wenhao ="";
                        if(resultm.get("wenhao")!=null && !"".equals(resultm.get("wenhao"))) {
                            wenhao = resultm.get("wenhao").toString().trim();//二期进馆文件编号
                        }
                        String rq ="";
                        if(resultm.get("rq")!=null && !"".equals(resultm.get("rq"))) {
                            rq = resultm.get("rq").toString().trim();//二期进馆文件日期
                        }
                        String zrz ="";
                        if(resultm.get("zrz")!=null && !"".equals(resultm.get("zrz"))) {
                            zrz = resultm.get("zrz").toString().trim();//二期进馆文件责任者
                        }
                        String yh ="";
                        if(resultm.get("yh")!=null && !"".equals(resultm.get("yh"))) {
                            yh = resultm.get("yh").toString().trim();//二期进馆文件页号
                        }
                        String mj ="";
                        if(resultm.get("mj")!=null && !"".equals(resultm.get("mj"))) {
                            mj = resultm.get("mj").toString().trim();//二期进馆密级
                            if (mj != null && !"".equals(mj)) {
                                List mjList = bds.getParamMJ("");
                                for (int m = 0; m < mjList.size(); m++) {
                                    if (mjList.get(m) != null && !"".equals(mjList.get(m))) {
                                        String mjValue = mjList.get(m).toString();
                                        if (mjValue.contains(mj)) {
                                            mj = mjValue;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        String ajh ="";
                        if(resultm.get("ajh")!=null && !"".equals(resultm.get("ajh"))) {
                            ajh = resultm.get("ajh").toString().trim();//二期进馆案卷号
                        }
                        String dah ="";
                        if(resultm.get("dah")!=null && !"".equals(resultm.get("dah"))) {
                            dah = resultm.get("dah").toString().trim();//二期进馆档案馆序号
                        }
                        String jh ="";
                        if(resultm.get("jh")!=null && !"".equals(resultm.get("jh"))) {
                            jh = resultm.get("jh").toString().trim();//二期进馆卷内顺序号
                        }
                        //二期进馆画幅数
                        String hfsStr ="";
                        int hfs =0;
                        if(resultm.get("hfs")!=null && !"".equals(resultm.get("hfs"))) {
                            hfsStr = resultm.get("hfs").toString().trim();
                            if (hfsStr.contains(".")) {
                                hfsStr = hfsStr.substring(0, hfsStr.lastIndexOf("."));
                            }
                            hfs = Integer.valueOf(hfsStr);
                        }
                        String szh ="";
                        if(resultm.get("szhsm")!=null && !"".equals(resultm.get("szhsm"))) {
                            szh = resultm.get("szhsm").toString();//二期进馆数字化情况说明
                        }
                        String syscode = this.getCode();
                        StringBuffer addExcelDataSql = new StringBuffer();
                        addExcelDataSql.append("insert into eqfiledata (syscode,unitsys,unitname,title,flh,flname,wenhao,rq,zrz,yh,mj,dah,hfs,keyword,libcode,szhsm,ajh,jh,row) value ('");
                        addExcelDataSql.append(syscode).append("','").append(qzh).append("','").append(unitname).append("','").append(filetitle).append("','");
                        addExcelDataSql.append(flh).append("','").append(flmc).append("','").append(wenhao).append("','").append(rq).append("','");
                        addExcelDataSql.append(zrz).append("','").append(yh).append("','").append(mj).append("','").append(dah);
                        addExcelDataSql.append("',").append(hfs).append(",'").append(keyword).append("','").append(libcode).append("','").append(szh).append("','").append(ajh).append("','").append(jh).append("',").append(i).append(")");
                        log.info("二期进馆照片档案文件excel数据写入临时库sql = "+addExcelDataSql.toString());
                        jdbcTemplate.execute(addExcelDataSql.toString());
                        //写入统计表
                        String resultTablename = "s_statistics";
                        Map statMap = new HashMap();
                        statMap.put("tablename",resultTablename);
                        statMap.put("libcode",libcode);
                        statMap.put("arcLvl",arcLvl);
                        statMap.put("mj",mj);
                        statMap.put("js",1);
                        statMap.put("keyword",keyword);
                        statMap.put("ys",hfs);
                        statMap.put("hfs","");
                        statMap.put("archiveType",archiveType);
                        ss.addDataStat(statMap);
                    }
                }
            }else if(libname.contains("音视频") || libname.contains("音频") || libname.contains("视频")){
                if("file".equals(arcLvl)){
                    String whereStr = "libcode ='" + libcode + "'";
                    int res = this.getCacheCount("eqfiledata",whereStr);
                    if(res>0){
                        this.deleteSql("eqfiledata",whereStr);
                    }
                    for (int i = 1; i <= result.size(); i++) {
                        Map resultm = (Map) result.get(i);
                        String qzh ="";
                        if(resultm.get("unitsys")!=null && !"".equals(resultm.get("unitsys"))) {
                            qzh = resultm.get("unitsys").toString().trim();//二期进馆全宗号
                        }
                        String unitname ="";
                        if(resultm.get("unitname")!=null && !"".equals(resultm.get("unitname"))) {
                            unitname = resultm.get("unitname").toString().trim();//二期进馆全宗名称
                        }
                        String keyword ="";
                        if(resultm.get("keyword")!=null && !"".equals(resultm.get("keyword"))) {
                            keyword = resultm.get("keyword").toString().trim();//二期进馆文件级档号
                        }
                        String filetitle ="";
                        if(resultm.get("title")!=null && !"".equals(resultm.get("title"))) {
                            filetitle = resultm.get("title").toString().trim();//二期进馆文件题名
                        }
                        String flh ="";
                        if(resultm.get("flh")!=null && !"".equals(resultm.get("flh"))) {
                            flh = resultm.get("flh").toString().trim();//二期进馆分类号
                        }
                        String flmc ="";
                        if(resultm.get("flname")!=null && !"".equals(resultm.get("flname"))) {
                            flmc = resultm.get("flname").toString().trim();//二期进馆分类名称
                        }
                        String wenhao ="";
                        if(resultm.get("wenhao")!=null && !"".equals(resultm.get("wenhao"))) {
                            wenhao = resultm.get("wenhao").toString().trim();//二期进馆文件编号
                        }
                        String rq ="";
                        if(resultm.get("rq")!=null && !"".equals(resultm.get("rq"))) {
                            rq = resultm.get("rq").toString().trim();//二期进馆文件日期
                        }
                        String zrz ="";
                        if(resultm.get("zrz")!=null && !"".equals(resultm.get("zrz"))) {
                            zrz = resultm.get("zrz").toString().trim();//二期进馆文件责任者
                        }
                        String sc ="";
                        if(resultm.get("sc")!=null && !"".equals(resultm.get("sc"))) {
                            sc = resultm.get("sc").toString().trim();//二期进馆音视频时长
                        }
                        String efilesize ="";
                        if(resultm.get("efilesize")!=null && !"".equals(resultm.get("efilesize"))) {
                            efilesize = resultm.get("efilesize").toString().trim();//二期进馆音视频容量
                        }
                        String mj ="";
                        if(resultm.get("mj")!=null && !"".equals(resultm.get("mj"))) {
                            mj = resultm.get("mj").toString().trim();//二期进馆密级
                            if (mj != null && !"".equals(mj)) {
                                List mjList = bds.getParamMJ("");
                                for (int m = 0; m < mjList.size(); m++) {
                                    if (mjList.get(m) != null && !"".equals(mjList.get(m))) {
                                        String mjValue = mjList.get(m).toString();
                                        if (mjValue.contains(mj)) {
                                            mj = mjValue;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        String efilegs ="";
                        if(resultm.get("efilegs")!=null && !"".equals(resultm.get("efilegs"))) {
                            efilegs = resultm.get("efilegs").toString().trim();//二期进馆音视频文件格式
                        }
                        String dah ="";
                        if(resultm.get("dah")!=null && !"".equals(resultm.get("dah"))) {
                            dah = resultm.get("dah").toString().trim();//二期进馆档案馆序号
                        }
                        String szh ="";
                        if(resultm.get("szhsm")!=null && !"".equals(resultm.get("szhsm"))) {
                            szh = resultm.get("szhsm").toString().trim();//二期进馆数字化情况说明
                        }
                        String jh ="";
                        if(resultm.get("jh")!=null && !"".equals(resultm.get("jh"))) {
                            jh = resultm.get("jh").toString().trim();//二期进馆卷内顺序号
                        }
                        String syscode = this.getCode();
                        StringBuffer addExcelDataSql = new StringBuffer();
                        addExcelDataSql.append("insert into eqfiledata (syscode,unitsys,unitname,title,rq,zrz,sc,efilesize,efilegs,mj,jh,keyword,libcode,szhsm,dah,row) value ('");
                        addExcelDataSql.append(syscode).append("','").append(qzh).append("','").append(unitname).append("','").append(filetitle).append("','").append(rq).append("','");
                        addExcelDataSql.append(zrz).append("','").append(sc).append("','").append(efilesize).append("','").append(efilegs).append("','").append(mj).append("','").append(jh);
                        addExcelDataSql.append("','").append(keyword).append("','").append(libcode).append("','").append(szh).append("','").append(dah).append("',").append(i).append(")");
                        log.info("二期进馆音视频档案文件excel数据写入临时库sql = "+addExcelDataSql.toString());
                        jdbcTemplate.execute(addExcelDataSql.toString());
                        //写入统计表
                        String resultTablename = "s_statistics";
                        Map statMap = new HashMap();
                        statMap.put("tablename",resultTablename);
                        statMap.put("libcode",libcode);
                        statMap.put("arcLvl",arcLvl);
                        statMap.put("mj",mj);
                        statMap.put("js",1);
                        statMap.put("keyword",keyword);
                        statMap.put("ys","");
                        statMap.put("hfs","");
                        statMap.put("archiveType",archiveType);
                        ss.addDataStat(statMap);
                    }
                }
            }else if(libname.contains("名人")){
                if("file".equals(arcLvl)){
                    String whereStr = "libcode ='" + libcode + "'";
                    int res = this.getCacheCount("eqfiledata",whereStr);
                    if(res>0){
                        this.deleteSql("eqfiledata",whereStr);
                    }
                    for (int i = 1; i <= result.size(); i++) {
                        Map<String,Object> resultm = (Map) result.get(i);
                        String syscode = this.getCode();
                        resultm.put("syscode",syscode);
                        resultm.put("libcode",libcode);
                        String field = "";
                        String value = "";
                        String mj = "";
                        String keyword = "";
                        int pagenum = 0;
                        int hfs = 0;
                        for(Map.Entry<String,Object> vo : resultm.entrySet()){
                            if("f2".equals(vo.getKey())){
                                field = field+"f5,";
                            }else if ("f11".equals(vo.getKey())){
                                field = field+"mlh,";
                            }else if ("f13".equals(vo.getKey())){
                                field = field+"ljr,";
                            }else if ("f14".equals(vo.getKey())){
                                field = field+"ljrq,";
                            }else if ("f15".equals(vo.getKey())){
                                field = field+"jcr,";
                            }else if ("f4".equals(vo.getKey())){
                                field = field+"hnsm,";
                            }else if ("f5".equals(vo.getKey())){
                                field = field+"f11,";
                            }else if ("f6".equals(vo.getKey())){
                                field = field+"f10,";
                            }else{
                                field = field+vo.getKey()+",";
                            }
                            if(vo.getValue()!=null && !"".equals(vo.getValue())) {
                                if (vo.getValue().toString().contains(".") && !"mj".equals(vo.getKey())) {
                                    String v = vo.getValue().toString();
                                    v = v.substring(0, v.lastIndexOf("."));
                                    int vv = Integer.valueOf(v);
                                    value = value+vv+",";
                                    if("pagenum".equals(vo.getKey())){
                                        pagenum = vv;
                                    }
                                    if("hfs".equals(vo.getKey())){
                                        hfs = vv;
                                    }
                                }else if("mj".equals(vo.getKey())){
                                    String v = vo.getValue().toString().trim();
                                    List mjList = bds.getParamMJ("");
                                    boolean mjb = false;
                                    for (int m = 0; m < mjList.size(); m++) {
                                        if (mjList.get(m) != null && !"".equals(mjList.get(m))) {
                                            String mjValue = mjList.get(m).toString();
                                            if (mjValue.contains(v)) {
                                                v = mjValue;
                                                value = value+"'"+v+"',";
                                                mj = v;
                                                mjb = true;
                                                break;
                                            }
                                        }
                                    }
                                    if(!mjb){
                                        value = value+"'"+vo.getValue().toString().trim()+"',";
                                    }
                                }else{
                                    value = value+"'"+vo.getValue().toString().trim()+"',";
                                    if("keyword".equals(vo.getKey())){
                                        keyword = vo.getValue().toString().trim();
                                    }
                                    if("pagenum".equals(vo.getKey())){
                                        pagenum = Integer.valueOf(vo.getValue().toString().trim());
                                    }
                                    if("hfs".equals(vo.getKey())){
                                        hfs = Integer.valueOf(vo.getValue().toString().trim());
                                    }
                                }
                            }else {
                                value = value + "'',";
                            }
                        }
//                        field = field.substring(0,field.length()-1);
//                        value = value.substring(0,value.length()-1);
                        field = field+"row";
                        value = value+i;
                        StringBuffer addResultInFieldSql = new StringBuffer();
                        addResultInFieldSql.append("insert into eqfiledata (");
                        addResultInFieldSql.append(field).append (") values (").append(value).append(")");
                        log.info("检测出问题数据写入数据库 = "+addResultInFieldSql.toString());
                        this.jdbcTemplate.execute(addResultInFieldSql.toString());
//                        String qzh ="";
//                        if(resultm.get("unitsys")!=null && !"".equals(resultm.get("unitsys"))) {
//                            qzh = resultm.get("unitsys").toString().trim();//二期进馆全宗号
//                        }
//                        String unitname ="";
//                        if(resultm.get("unitname")!=null && !"".equals(resultm.get("unitname"))) {
//                            unitname = resultm.get("unitname").toString().trim();//二期进馆全宗名称
//                        }
//                        String keyword ="";
//                        if(resultm.get("keyword")!=null && !"".equals(resultm.get("keyword"))) {
//                            keyword = resultm.get("keyword").toString().trim();//二期进馆文件级档号
//                        }
//                        String filetitle ="";
//                        if(resultm.get("title")!=null && !"".equals(resultm.get("title"))) {
//                            filetitle = resultm.get("title").toString().trim();//二期进馆文件题名
//                        }
//                        String flh ="";
//                        if(resultm.get("flh")!=null && !"".equals(resultm.get("flh"))) {
//                            flh = resultm.get("flh").toString().trim();//二期进馆分类号
//                        }
//                        String flmc ="";
//                        if(resultm.get("flname")!=null && !"".equals(resultm.get("flname"))) {
//                            flmc = resultm.get("flname").toString().trim();//二期进馆分类名称
//                        }
//                        String wenhao ="";
//                        if(resultm.get("wenhao")!=null && !"".equals(resultm.get("wenhao"))) {
//                            wenhao = resultm.get("wenhao").toString().trim();//二期进馆文件编号
//                        }
//                        String rq ="";
//                        if(resultm.get("rq")!=null && !"".equals(resultm.get("rq"))) {
//                            rq = resultm.get("rq").toString().trim();//二期进馆文件日期
//                        }
//                        String zrz ="";
//                        if(resultm.get("zrz")!=null && !"".equals(resultm.get("zrz"))) {
//                            zrz = resultm.get("zrz").toString().trim();//二期进馆文件责任者
//                        }
//                        String mj ="";
//                        if(resultm.get("mj")!=null && !"".equals(resultm.get("mj"))) {
//                            mj = resultm.get("mj").toString().trim();//二期进馆密级
//                            if (mj != null && !"".equals(mj)) {
//                                List mjList = bds.getParamMJ("");
//                                for (int m = 0; m < mjList.size(); m++) {
//                                    if (mjList.get(m) != null && !"".equals(mjList.get(m))) {
//                                        String mjValue = mjList.get(m).toString();
//                                        if (mjValue.contains(mj)) {
//                                            mj = mjValue;
//                                            break;
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                        String dah ="";
//                        if(resultm.get("dah")!=null && !"".equals(resultm.get("dah"))) {
//                            dah = resultm.get("dah").toString().trim();//二期进馆档案馆序号
//                        }
//                        String jh ="";
//                        if(resultm.get("jh")!=null && !"".equals(resultm.get("jh"))) {
//                            jh = resultm.get("jh").toString().trim();//二期进馆卷内顺序号
//                        }
//
//                        //二期进馆名人文件页数
//                        String pagenumStr = "";
//                        int pagenum = 0;
//                        if(resultm.get("pagenum")!=null && !"".equals(resultm.get("pagenum"))) {
//                            pagenumStr = resultm.get("pagenum").toString().trim();
//                            if (pagenumStr.contains(".")) {
//                                pagenumStr = pagenumStr.substring(0, pagenumStr.lastIndexOf("."));
//                            }
//                            pagenum = Integer.valueOf(pagenumStr);
//                        }
//                        //二期进馆名人画幅数
//                        String hfsStr = "";
//                        int hfs = 0;
//                        if(resultm.get("hfs")!=null && !"".equals(resultm.get("hfs"))) {
//                            hfsStr = resultm.get("hfs").toString().trim();
//                            if (hfsStr.contains(".")) {
//                                hfsStr = hfsStr.substring(0, hfsStr.lastIndexOf("."));
//                            }
//                            hfs = Integer.valueOf(hfsStr);
//                        }
//                        String szh ="";
//                        if(resultm.get("szhsm")!=null && !"".equals(resultm.get("szhsm"))) {
//                            szh = resultm.get("szhsm").toString().trim();//二期进馆数字化情况说明
//                        }
//                        String syscode = this.getCode();
//                        StringBuffer addExcelDataSql = new StringBuffer();
//                        addExcelDataSql.append("insert into eqfiledata (syscode,unitsys,unitname,title,rq,zrz,flh,flname,pagenum,mj,dah,hfs,keyword,libcode,szhsm,jh,row) value ('");
//                        addExcelDataSql.append(syscode).append("','").append(qzh).append("','").append(unitname).append("','").append(filetitle).append("','").append(rq).append("','");
//                        addExcelDataSql.append(zrz).append("','").append(flh).append("','").append(flmc).append("',").append(pagenum).append(",'").append(mj).append("','").append(dah);
//                        addExcelDataSql.append("',").append(hfs).append(",'").append(keyword).append("','").append(libcode).append("','").append(szh).append("','").append(jh).append("',").append(i).append(")");
//                        log.info("二期进馆名人档案文件excel数据写入临时库sql = "+addExcelDataSql.toString());
//                        jdbcTemplate.execute(addExcelDataSql.toString());
                        //写入统计表
                        String resultTablename = "s_statistics";
                        Map statMap = new HashMap();
                        statMap.put("tablename",resultTablename);
                        statMap.put("libcode",libcode);
                        statMap.put("arcLvl",arcLvl);
                        statMap.put("mj",mj);
                        statMap.put("js",1);
                        statMap.put("keyword",keyword);
                        statMap.put("ys",pagenum);
                        statMap.put("hfs",hfs);
                        statMap.put("archiveType",archiveType);
                        ss.addDataStat(statMap);
                    }
                }
            }
        }
        return 0;
    }

    @Override
    public List getData(String field ,String tablename,String whereStr){
        String getData = "select "+field+" from "+tablename+" where "+whereStr;
        log.info("通过条件字段获取数据sql = "+getData);
        return jdbcTemplate.queryForList(getData);
    }

    @Override
    public int getSum(String field, String tablename, String whereStr) {
        String getSum = "select "+field+" from "+tablename+" where "+whereStr;
        log.info("获取数据之和sql = "+getSum);
        return jdbcTemplate.update(getSum);
    }

    @Override
    public boolean updData(Map<String, Object> checkResult) {
        try{
            String unitsys = checkResult.get("unitsys").toString();
            String libcode = checkResult.get("libcode").toString();
            String arcLvl = checkResult.get("arclvl").toString();
            String keyword = checkResult.get("keyword").toString();
            String createtime =this.getDate();
            String syscode = this.getCode();
            checkResult.put("syscode",syscode);
            checkResult.put("createtime",createtime);
            checkResult.put("status",0);
            String field = "";
            String value = "";
            String kv = "";
            for(Map.Entry<String,Object> vo : checkResult.entrySet()){
                kv = kv+vo.getKey()+" = '"+vo.getValue()+"',";
            }
            kv = kv.substring(0,kv.length()-1);
            String whereStr = "keyword = '"+keyword+"'";
            String tablename = "s_"+arcLvl+"result";
            StringBuffer updResultInFieldSql = new StringBuffer();
            updResultInFieldSql.append("update ").append(tablename).append(" set ");
            updResultInFieldSql.append(kv).append (" where ").append(whereStr);
            log.info("检测出问题数据写入数据库 = "+updResultInFieldSql.toString());
            jdbcTemplate.execute(updResultInFieldSql.toString());
        }catch (Exception e){
            log.info(e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean updStatus(Map<String, Object> checkResult) {
        try{
        String unitsys = checkResult.get("unitsys").toString();
        String libcode = checkResult.get("libcode").toString();
//        String arcLvl = checkResult.get("arcLvl").toString();
        String createtime =this.getDate();
        String syscode = this.getCode();
        checkResult.put("syscode",syscode);
        checkResult.put("createtime",createtime);
        checkResult.put("status",0);
        String siptablename ="s_sipresult";
        StringBuffer updSipResultInStatusSql = new StringBuffer();
        updSipResultInStatusSql.append("update ").append(siptablename).append(" set status=1");
        updSipResultInStatusSql.append(" where status=0 ");
        log.info("更新sip检测结果status状态sql = "+updSipResultInStatusSql.toString());
        this.jdbcTemplate.execute(updSipResultInStatusSql.toString());

        String voltablename ="s_volresult";
        StringBuffer updVolResultInStatusSql = new StringBuffer();
        updVolResultInStatusSql.append("update ").append(voltablename).append(" set status=1");
        updVolResultInStatusSql.append(" where status=0 ");
        log.info("更新案卷检测结果status状态sql = "+updVolResultInStatusSql.toString());
        this.jdbcTemplate.execute(updVolResultInStatusSql.toString());

        String filetablename ="s_fileresult";
        StringBuffer updFileResultInStatusSql = new StringBuffer();
        updFileResultInStatusSql.append("update ").append(filetablename).append(" set status=1");
        updFileResultInStatusSql.append(" where status=0 ");
        log.info("更新案卷检测结果status状态sql = "+updFileResultInStatusSql.toString());
        this.jdbcTemplate.execute(updFileResultInStatusSql.toString());

        String efiletablename ="s_efileresult";
        StringBuffer updEFResultInStatusSql = new StringBuffer();
        updEFResultInStatusSql.append("update ").append(efiletablename).append(" set status=1");
        updEFResultInStatusSql.append(" where status=0 ");
        log.info("更新案卷检测结果status状态sql = "+updEFResultInStatusSql.toString());
        this.jdbcTemplate.execute(updEFResultInStatusSql.toString());

//        if(checkResult.get("sip")!=null && !"".equals(checkResult.get("sip"))){
//            tablename = "s_sipresult";
//            StringBuffer updResultInStatusSql = new StringBuffer();
//            updResultInStatusSql.append("update ").append(tablename).append(" set status=1");
//            updResultInStatusSql.append(" where status=0 ");
//            log.info("更新案卷检测结果status状态sql = "+updResultInStatusSql.toString());
//            this.jdbcTemplate.execute(updResultInStatusSql.toString());
//        }else{
//            if(checkResult.get("arcvol")!=null && !"".equals(checkResult.get("arcvol"))) {
//                String arcvol = checkResult.get("arcvol").toString();
//                tablename = "s_" + arcvol + "result";
//                StringBuffer updResultInStatusSql = new StringBuffer();
//                updResultInStatusSql.append("update ").append(tablename).append(" set status=1");
//                updResultInStatusSql.append(" where status=0 ");
//                log.info("更新案卷检测结果status状态sql = "+updResultInStatusSql.toString());
//                this.jdbcTemplate.execute(updResultInStatusSql.toString());
//            }
//            if(checkResult.get("arcfile")!=null && !"".equals(checkResult.get("arcfile"))){
//                String arcfile = checkResult.get("arcfile").toString();
//                tablename = "s_" + arcfile + "result";
//                StringBuffer updResultInStatusSql = new StringBuffer();
//                updResultInStatusSql.append("update ").append(tablename).append(" set status=1");
//                updResultInStatusSql.append(" where status=0 ");
//                log.info("更新文件检测结果status状态sql = "+updResultInStatusSql.toString());
//                this.jdbcTemplate.execute(updResultInStatusSql.toString());
//
//                tablename = "s_efileresult";
//                StringBuffer updEfileResultInStatusSql = new StringBuffer();
//                updEfileResultInStatusSql.append("update ").append(tablename).append(" set status=1");
//                updEfileResultInStatusSql.append(" where status=0 ");
//                log.info("更新电子文件检测结果status状态sql = "+updEfileResultInStatusSql.toString());
//                this.jdbcTemplate.execute(updEfileResultInStatusSql.toString());
//            }
//        }
        }catch (Exception e){
            log.info(e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public String getConfig(String where) {
        String result = "";
        String getConfigSql = "select * from s_config where "+where;
        log.info("获取检测工具配置sql = "+getConfigSql);
        List<Map<String,Object>> configL = this.jdbcTemplate.queryForList(getConfigSql);
        Map<String,Object> configM = configL.get(0);
        int checkStatus = Integer.valueOf(configM.get("check_status").toString());
        if(checkStatus==0){
            result = "0";
        }else if(checkStatus==1){
            if(configM.get("content")!=null && !"".equals(configM.get("content"))){
                String content = configM.get("content").toString();
                if(content!=null && !"".equals(content)){
                    result = content;
                }
            }else{
                result = "1";
            }
        }
        return result;
    }

    @Override
    public Map getConfigEfile(String field, String fieldType) {
        Map resultM = new HashMap();
        String getConfigSql = "select * from s_config where pname = '"+field+"'";
        log.info("获取检测工具电子文件配置sql = "+getConfigSql);
        List<Map<String,Object>> configL = this.jdbcTemplate.queryForList(getConfigSql);
        for(int i =0;i<configL.size();i++){
            Map<String,Object> configM = configL.get(i);
            int checkStatus = Integer.valueOf(configM.get("check_status").toString());
            if(checkStatus==1){
                if(configM.get("content")!=null && !"".equals(configM.get("content"))) {
                    String content = configM.get("content").toString();
                    String name = configM.get("name").toString();
                    if (content != null && !"".equals(content)) {
                        resultM.put(name, content);
                    }
                }else {
                    String name = configM.get("name").toString();
                    resultM.put(name, "true");
                }
            }
        }
        return resultM;
    }

    @Override
    public List getCheckData(String tablename, String field, String ifHistory, String whereStr) {
        String where = "1=1";
        if(whereStr==null || "".equals(whereStr)){
            where =" 1=1";
        } else {
            where = where+" and "+whereStr;
        }
        if("0".equals(ifHistory)){
            where = where+" and status = 0";
        }else if("1".equals(ifHistory)){
            where = where+" and status in (0,1)";
        }
//        where = where+" and status in (0,1)";
        StringBuffer getCheckDataSql = new StringBuffer();
        getCheckDataSql.append("select ").append(field).append(" from ").append(tablename);
        getCheckDataSql.append(" where ").append(where);
        log.info("获取导出excel数据sql = "+getCheckDataSql.toString());
        List Data = this.jdbcTemplate.queryForList(getCheckDataSql.toString());
        return Data;
    }

    @Override
    public List getCheckData(String tablename, String field, int pagesize, int pagenum,String ifHistory, String whereStr,String order) {
        String where = "";
        if(whereStr==null || "".equals(whereStr)){
            where =" 1=1";
        }else{
            where = whereStr;
        }
//        else {
//            String[] whereStrs = whereStr.split(" ~ ");
//            where = "createtime>='"+whereStrs[0]+"' and createtime<='"+whereStrs[1]+"'";
//        }
        if("0".equals(ifHistory)){
            where = where+" and status = 0";
        }else if("1".equals(ifHistory)){
            where = where+" and status in (0,1)";
        }
//        where = where+" and status in (0,1)";
        int rpos=0;
        if(pagenum==1){
            rpos=0;
        }else{
            rpos=(pagenum-1)*pagesize;
        }
        StringBuffer getCheckDataSql = new StringBuffer();
        getCheckDataSql.append("select ").append(field).append(" from ").append(tablename);
        getCheckDataSql.append(" where ").append(where).append(order);
        getCheckDataSql.append(" limit ").append(rpos).append(",").append(pagesize);
        log.info("获取查看结果数据sql = "+getCheckDataSql.toString());
        List Data = this.jdbcTemplate.queryForList(getCheckDataSql.toString());
        return Data;
    }

    @Override
    public String getMysqlField(String tablename,String tablespace,String arcLvl) {
        String sql = "select COLUMN_name from information_schema.`COLUMNS` where TABLE_NAME = '"+tablename+"' and TABLE_SCHEMA='"+tablespace+"'";
        log.info("查询数据表字段sql = "+sql);
        List fieldL = this.jdbcTemplate.queryForList(sql,String.class);
        String field = "";
        for(int i=0;i<fieldL.size();i++){
            String value = fieldL.get(i).toString();
            if("vol".equals(arcLvl)){
                if("syscode".equals(value) || "libcode".equals(value) || "archivetype".equals(value) || "keyword".equals(value) || "title".equals(value)
                        || "status".equals(value)|| "remark".equals(value) || "errormessage".equals(value)|| "createtime".equals(value) || "errortype".equals(value)){
                    continue;
                }else{
                    field = field+value+",";
                }
            }
            if("file".equals(arcLvl)){
                if("syscode".equals(value) || "libcode".equals(value) ||"arclvl".equals(value) || "archivetype".equals(value) || "keyword".equals(value) || "title".equals(value)
                        || "status".equals(value)|| "remark".equals(value) || "errormessage".equals(value)|| "createtime".equals(value) || "errortype".equals(value)){
                    continue;
                }else{
                    field = field+value+",";
                }
            }
            if("efile".equals(arcLvl)){
                if("syscode".equals(value) || "libcode".equals(value) ||"arclvl".equals(value) || "archivetype".equals(value) || "keyword".equals(value) || "title".equals(value)
                        || "status".equals(value)|| "remark".equals(value) || "errormessage".equals(value)|| "createtime".equals(value) || "errortype".equals(value)){
                    continue;
                }else{
                    field = field+value+",";
                }
            }
            if("sip".equals(arcLvl)){
                if("syscode".equals(value) || "libcode".equals(value) || "archivetype".equals(value) || "keyword".equals(value) || "title".equals(value)
                        || "status".equals(value)|| "remark".equals(value) || "errormessage".equals(value)|| "createtime".equals(value) || "errortype".equals(value)){
                    continue;
                }else{
                    field = field+value+",";
                }
            }
        }
        field = field.substring(0,field.length()-1);
        return field;
    }

    public String getCode() {
        UUID uuid = UUID.randomUUID();
        String sys = uuid.toString();
        if(sys.contains("-")){
            sys = sys.replaceAll("-","");
        }
        return sys;
    }

    public String getDate(){
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String createtime = sdf.format(date);
        return createtime;
    }
}
