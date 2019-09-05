package com.unis.zkydatadetection.service.impl;

import ch.qos.logback.classic.Logger;
import com.unis.zkydatadetection.service.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.*;

@Service(value = "checkService")
public class checkServiceImpl implements checkService{
    private final Logger log = (Logger) LoggerFactory.getLogger("checkServiceImpl.class");

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    private resultService rs;

    @Autowired
    private baseDataService bds;

    @Autowired
    private getPageService gps;

    @Autowired
    private getEfileAttr gea;

    @Autowired
    private excelService es;

    @Autowired
    private statisticsService ss;

    @Value("${checktype}")
    private String checktype;

    String dh = "";
    int filenum = 0;
    int updfilenum = 0;
    int fileIndex=0;

    @Override
    public String checkField(String[] titles, Map excelAttr) {
        String result = "";
        String unitsys = "";
        if(excelAttr.get("unitsys")!=null && !"".equals(excelAttr.get("unitsys"))){
            unitsys = excelAttr.get("unitsys").toString();
        }else{
            unitsys="0001";
        }

        String libcode = excelAttr.get("libcode").toString();
        String arcLvl = excelAttr.get("arcLvl").toString();
        String tablename = "d_"+arcLvl+libcode+"_"+unitsys;
        String getkfieldSql = "select * from s_field where arclvl = '"+arcLvl+"' and tablename = '"+tablename+"' and status=0";
        log.info("获取档案系统档案门类字段sql="+getkfieldSql);
        List<Map<String,Object>> fieldL = jdbcTemplate.queryForList(getkfieldSql);
        List resultField = this.contrastField(titles,fieldL);
        List resultFieldOfXls = (List) resultField.get(0);
//        List resultFieldOfAms = (List) resultField.get(1);
//        if(resultFieldOfAms.size()==0 && resultFieldOfXls.size()==0){
        if(resultFieldOfXls.size()==0){
            log.info("字段检测无误");
            result = "success";
        }else {
//            rs.addResultInField(excelAttr, resultField);
//            if(resultFieldOfXls.size()>0 && resultFieldOfAms.size()==0){
            if(resultFieldOfXls.size()>0){
                result = "excel以下字段"+StringUtils.join(resultFieldOfXls.toArray(),",")+"不在档案系统中";
            }
//            else if(resultFieldOfAms.size()>0 && resultFieldOfXls.size()==0){
//                result = "档案系统以下字段"+StringUtils.join(resultFieldOfAms.toArray(),",")+"不在excel中";
//            }else if(resultFieldOfXls.size()>0 && resultFieldOfAms.size()>0){
//                result = "excel以下字段"+StringUtils.join(resultFieldOfXls.toArray(),",")+"不在档案系统中,档案系统以下字段"+StringUtils.join(resultFieldOfAms.toArray(),",")+"不在excel中";
//            }
        }
        return result;
    }

    @Override
    public boolean checkFieldValue(Map excelAttr) {
        boolean checkFiedlValueResult = false;
        String unitsys = excelAttr.get("unitsys").toString();
        String libcode = excelAttr.get("libcode").toString();
        String whereStr = "libcode = '"+libcode+"'";
        String libname = "";
        List libnameL = rs.getData("chname","s_arc",whereStr);
        if(libnameL.size()>0){
            Map libnameM = (Map)libnameL.get(0);
            if(libnameM.get("chname")!=null && !"".equals(libnameM.get("chname"))) {
                libname = libnameM.get("chname").toString();
            }
        }
        String efilepath = excelAttr.get("efilepath").toString();
        String archiveType = excelAttr.get("archiveType").toString();
        Map checkContent = (Map)excelAttr.get("checkContent");
        //各个检测结果表status字段的值0为本次检测，1位历史检测，2为删除。每次导入excel或sip后，都会将表内上次状态数据有0改为1
        boolean b = rs.updStatus(excelAttr);
        if("xd".equals(archiveType)){//先导
            if(libname.contains("照片")){//照片档案
                String getFileDataSql = "select * from xdfiledata where libcode='"+libcode+"'";
                log.info("获取档案系统档案门类字段sql="+getFileDataSql);
                List filedataL = this.jdbcTemplate.queryForList(getFileDataSql);
                boolean fileDataResult = this.checkFiledata(filedataL,excelAttr);
                if(checkContent.get("可用性检查")!=null && !"".equals(checkContent.get("可用性检查"))) {
                    if("true".equals(checkContent.get("可用性检查").toString())) {
                        boolean efileDataResult = this.checkEfiledata(excelAttr);
                        if (fileDataResult && efileDataResult) {
                            checkFiedlValueResult = true;
                        }
                    }
                }else{
                    if(fileDataResult){
                        checkFiedlValueResult = true;
                    }
                }
            }else if(libname.contains("音视频") || libname.contains("音频") || libname.contains("视频")){//音视频档案
                String getFileDataSql = "select * from xdfiledata where libcode='"+libcode+"'";
                List filedataL = this.jdbcTemplate.queryForList(getFileDataSql);
                boolean fileDataResult = this.checkFiledata(filedataL,excelAttr);
                if(checkContent.get("可用性检查")!=null && !"".equals(checkContent.get("可用性检查"))) {
                    if("true".equals(checkContent.get("可用性检查").toString())) {
                        boolean efileDataResult = this.checkEfiledata(excelAttr);
                        if (fileDataResult && efileDataResult) {
                            checkFiedlValueResult = true;
                        }
                    }
                }else{
                    if(fileDataResult){
                        checkFiedlValueResult = true;
                    }
                }
            } else if(libname.contains("电子文件")) {//电子文件档案处理文件
                String getFileDataSql = "select * from xdfiledata where libcode='"+libcode+"'";
                List filedataL = this.jdbcTemplate.queryForList(getFileDataSql);
                boolean fileDataResult = this.checkFiledata(filedataL,excelAttr);
                if(checkContent.get("可用性检查")!=null && !"".equals(checkContent.get("可用性检查"))) {
                    if("true".equals(checkContent.get("可用性检查").toString())) {
                        boolean efileDataResult = this.checkEfiledata(excelAttr);
                        if (fileDataResult && efileDataResult) {
                            checkFiedlValueResult = true;
                        }
                    }
                }else{
                    if(fileDataResult){
                        checkFiedlValueResult = true;
                    }
                }
            }else {//剩下的是案卷-卷内-电子文件
                String getVolDataSql = "select * from xdvoldata where libcode='"+libcode+"'";
                List voldataL = this.jdbcTemplate.queryForList(getVolDataSql);
                boolean volDataResult = this.checkVoldata(voldataL,excelAttr);

                String getFileDataSql = "select * from xdfiledata where libcode='"+libcode+"'";
                List filedataL = this.jdbcTemplate.queryForList(getFileDataSql);
                boolean fileDataResult = this.checkFiledata(filedataL,excelAttr);
                if(checkContent.get("可用性检查")!=null && !"".equals(checkContent.get("可用性检查"))) {
                    if("true".equals(checkContent.get("可用性检查").toString())) {
                        boolean efileDataResult = this.checkEfiledata(excelAttr);
                        if (volDataResult && fileDataResult && efileDataResult) {
                            checkFiedlValueResult = true;
                        }
                    }
                }else{
                    if(volDataResult && fileDataResult){
                        checkFiedlValueResult = true;
                    }
                }
            }
        }
        if("eq".equals(archiveType)){
            if(libname.contains("名人") || libname.contains("音视频") || libname.contains("音频") || libname.contains("视频")){//音视频档案，名人档案处理文件-电子文件
                String getFileDataSql = "select * from eqfiledata where libcode='"+libcode+"'";
                List filedataL = this.jdbcTemplate.queryForList(getFileDataSql);
                boolean fileDataResult = this.checkFiledata(filedataL,excelAttr);
                if(checkContent.get("可用性检查")!=null && !"".equals(checkContent.get("可用性检查"))) {
                    if("true".equals(checkContent.get("可用性检查").toString())) {
                        boolean efileDataResult = this.checkEfiledata(excelAttr);
                        if (fileDataResult && efileDataResult) {
                            checkFiedlValueResult = true;
                        }
                    }
                }else{
                    if(fileDataResult){
                        checkFiedlValueResult = true;
                    }
                }
            }else{//剩下的是案卷-卷内
                String getVolDataSql = "select * from eqvoldata where libcode='"+libcode+"'";
                List voldataL = this.jdbcTemplate.queryForList(getVolDataSql);
                boolean volDataResult = this.checkVoldata(voldataL,excelAttr);
                String getFileDataSql = "select * from eqfiledata where libcode='"+libcode+"'";
                List filedataL = this.jdbcTemplate.queryForList(getFileDataSql);
                boolean fileDataResult = this.checkFiledata(filedataL,excelAttr);
                if(checkContent.get("可用性检查")!=null && !"".equals(checkContent.get("可用性检查"))) {
                    if("true".equals(checkContent.get("可用性检查").toString())) {
                        boolean efileDataResult = this.checkEfiledata(excelAttr);
                        if (volDataResult && fileDataResult && efileDataResult) {
                            checkFiedlValueResult = true;
                        }
                    }
                }else{
                    if(volDataResult && fileDataResult){
                        checkFiedlValueResult = true;
                    }
                }
            }
        }
        //检测结束以后初始化参数
        dh = "";
        filenum = 0;
        updfilenum = 0;
        fileIndex=0;

        return checkFiedlValueResult;
    }

    @Override
    public boolean checkEfile(String path,Map excelAttr) {
        return false;
    }

    @Override
    public String parasExcel(String excelPath, Map excelAttr) throws Exception{
        String[] excelTitles = null;
        try{
            //获取excel的字段
            excelTitles = es.getExcelTitle(excelPath,excelAttr);
        }catch (Exception e){
            log.info("获取excel字段错误");
            return "获取excel首行字段错误";
        }
        //检测excel的字段
        String checkfieldResult = null;
        try {
            checkfieldResult = this.checkField(excelTitles, excelAttr);
        }catch (Exception e1){
            log.info("检测excel字段错误");
            return "检测excel的字段错误";
        }
        if("success".equals(checkfieldResult)){
            String[] amsField = es.getAmsField(excelTitles,excelAttr);
            //获取excel的内容
            Map<Integer, Map<String,Object>> result = es.readExcel(excelPath,excelAttr,amsField);
            //将excel的数据写入临时表
            rs.addExcelData(result,excelAttr);
            return "ok";
        }else{
            return checkfieldResult;
        }
    }

    @Override
    public Map getCheckType() {
        Map kvM = new HashMap();
        String[] checkVolfields = checktype.split(",");
        for(int i=0;i<checkVolfields.length;i++){
            String kv = checkVolfields[i];
            String[] kvs = kv.split("@");
            kvM.put(kvs[0],kvs[1]);
        }
        return kvM;
    }

    public List contrastField(String[] titles, List<Map<String,Object>> fieldL){
        List contrastFieldResult = new ArrayList();
        List excelField = new ArrayList();
//        List amsField = new ArrayList();
        try{
            for (int i=0;i<titles.length;i++){
                boolean bj = false;
                for (int j=0;j<fieldL.size();j++){
                    Map fieldm = (Map)fieldL.get(j);
                    if (titles[i].trim().equals(fieldm.get("name").toString().trim())){
                        bj=true;
                    }
                }
                if(!bj){
                    excelField.add(titles[i]);
                    log.info("excel字段"+titles[i]+"不在档案系统表中");
                }
            }
        }catch (Exception e){
            log.info(e.getMessage());
        }
        contrastFieldResult.add(excelField);

//        for (int i=0;i<fieldL.size();i++){
//            boolean bj = false;
//            Map fieldm = (Map)fieldL.get(i);
//            for (int j=0;j<titles.length;j++){
//                if (titles[j].equals(fieldm.get("name"))){
//                    bj=true;
//                }
//            }
//            if(!bj){
//                amsField.add(fieldm.get("name"));
//                log.info("档案系统字段"+fieldm.get("name")+"不在excel表中");
//            }
//        }
//        contrastFieldResult.add(amsField);

        return contrastFieldResult;
    }

    public boolean checkVoldata(List voldata,Map excelAttr){
        boolean checkVolDataResult = true;
        String errtype = "";
        Map<String,Object> checkResult = new HashMap();
        try{
        excelAttr.put("arclvl","vol");
        String archiveType = excelAttr.get("archiveType").toString();
        String libcode = excelAttr.get("libcode").toString();
        String selectStr = "libcode = '"+libcode+"'";
        String libname = "";
        List libnameL = rs.getData("chname","s_arc",selectStr);
        if(libnameL.size()>0){
            Map libnameM = (Map)libnameL.get(0);
            if(libnameM.get("chname")!=null && !"".equals(libnameM.get("chname"))) {
                libname = libnameM.get("chname").toString();
            }
        }
        Map checkContentM = (Map)excelAttr.get("checkContent");
        String version ="";
        if(excelAttr.get("version")!=null && !"".equals(excelAttr.get("version"))) {
            version = excelAttr.get("version").toString();
        }
        Set unitcodes = new HashSet();
        Set unitnames = new HashSet();
        //检测全宗号、全宗机构名称
        Map kvm = getCheckType();
        Set titleSet = new HashSet();
        for(int i=0;i<voldata.size();i++) { checkResult.put("libcode",libcode);
            checkResult.put("archiveType",archiveType);
            checkResult.put("arclvl","vol");
            checkResult.put("libname",libname);
            StringBuffer errormessage = new StringBuffer();
            int row = i+2;
            Map<String,Object> voldataM = (Map) voldata.get(i);
            String keyword = voldataM.get("keyword").toString();
            checkResult.put("keyword", keyword);
            //检查字段是否包含特殊符号
            if(checkContentM.get("真实性检查")!=null && !"".equals(checkContentM.get("真实性检查"))) {
                String kv = kvm.get("c1").toString();
                String where = "pname = '"+kv+"' and name = '特殊字符检查'";
                String configResult = rs.getConfig(where);
                boolean resultb = false;
                if ("0".equals(configResult)) {
                    resultb = false;
                } else if (("1".equals(configResult))) {
                    resultb = true;
                } else {
                    resultb = true;
                }
                if(resultb){
                    boolean volB = true;
                    for (String key : voldataM.keySet()) {
                        if(voldataM.get(key)!=null && !"".equals(voldataM.get(key))){
                            String value = voldataM.get(key).toString();
                            System.out.println(key + "  " + value);
                            char isLegalChar = bds.validateLegalString(value,configResult);
                            boolean space = bds.checkkg(value);
                            if ("t".equals(isLegalChar) || !space) {
                                volB = true;
                            }
                        }
                    }
                    if(volB){
                        errormessage.append("文件信息中的字段包含特殊符号或空格；");
                        checkResult.put("volfield", "文件信息中的字段包含特殊符号或空格；");
                        errtype = errtype + "文件信息中的字段包含特殊符号或空格,";
                    }
                }
            }
            if ("eq".equals(archiveType)) {//如果是二期进馆则检测全宗信息和分类号信息
                if(checkContentM.get("真实性检查")!=null && !"".equals(checkContentM.get("真实性检查"))) {
                    String kv = kvm.get("c1").toString();
                    String where = "pname = '"+kv+"' and name = '检查全宗号、全宗名称是否一致'";
                    String configResult = rs.getConfig(where);
                    boolean resultb = false;
                    if ("0".equals(configResult)) {
                        resultb = false;
                    } else if (("1".equals(configResult))) {
                        resultb = true;
                    } else {
                        resultb = true;
                    }
                    String unitsys = "";
                    String unitname = "";
                    if(resultb) {
                        if (voldataM.get("unitsys") != null && !"".equals(voldataM.get("unitsys"))) {
                            unitsys = voldataM.get("unitsys").toString();
                            if (unitcodes.size() == 0) {
                                unitcodes.add(unitsys);
                            } else {
                                int unitcodeb = unitcodes.size();
                                unitcodes.add(unitsys);
                                int unitcodea = unitcodes.size();
                                if (unitcodea > unitcodeb) {
                                    errormessage.append("第" + row + "行，该案卷全宗号与其他的案卷不一致；");
                                    checkResult.put("unitsys", "第" + row + "行，该案卷全宗号与其他的案卷不一致；");
                                    errtype = errtype + "全宗号错误,";
                                }
                            }
                        } else {
                            errormessage.append("第" + row + "行，二期进馆案卷全宗号的值为空；");
                            checkResult.put("unitsys", "第" + row + "行，二期进馆案卷全宗号的值为空；");
                            errtype = errtype + "全宗号错误,";
                        }
                        if (voldataM.get("unitname") != null && !"".equals(voldataM.get("unitname"))) {
                            unitname = voldataM.get("unitname").toString();
                            if (unitnames.size() == 0) {
                                unitnames.add(unitname);
                            } else {
                                int unitnameb = unitnames.size();
                                unitnames.add(unitname);
                                int unitnamea = unitnames.size();
                                if (unitnamea > unitnameb) {
                                    errormessage.append("第" + row + "行，该案卷全宗名称与其他的案卷不一致；");
                                    checkResult.put("unitname", "第" + row + "行，该案卷全宗名称与其他的案卷不一致；");
                                    errtype = errtype + "全宗名称错误,";
                                }
                            }
                        } else {
                            errormessage.append("第" + row + "行，二期进馆案卷全宗名称的值为空；");
                            checkResult.put("unitname", "第" + row + "行，二期进馆案卷全宗名称的值为空；");
                            errtype = errtype + "全宗名称错误,";
                        }
                    }
                    String where1 = "pname = '"+kv+"' and name = '检查案卷目录中全宗号、全宗名称与卷内目录的全宗号、全宗名称是否一致  '";
                    String configResult1 = rs.getConfig(where1);
                    boolean resultb1 = false;
                    if ("0".equals(configResult1)) {
                        resultb1 = false;
                    } else if (("1".equals(configResult1))) {
                        resultb1 = true;
                    } else {
                        resultb1 = true;
                    }
                    if(resultb1) {
                        String tablename = archiveType+"voldata";
                        String getFileUnitcode = "select distinct(unitsys) from  "+tablename+" where keyword like '"+keyword+"%'";
                        String getFileUnitN = "select distinct(unitname) from  "+tablename+" where keyword like '"+keyword+"%'";
                        List unitsysL = this.jdbcTemplate.queryForList(getFileUnitcode);
                        List unitNL = this.jdbcTemplate.queryForList(getFileUnitN);
                        if(unitsysL.size()!=1){
                            errormessage.append("第" + row + "行，二期进馆案卷的卷内全宗号不唯一；");
                            checkResult.put("unitsys", "第" + row + "行，二期进馆案卷的卷内全宗号不唯一；");
                            errtype = errtype + "二期进馆案卷的卷内全宗号不唯一,";
                        }else{
                            Map unitsysM = (Map)unitsysL.get(0);
                            String unitcode = unitsysM.get("unitsys").toString();
                            if(!unitsys.equals(unitcode)){
                                errormessage.append("第" + row + "行，二期进馆案卷全宗号与卷内文件全宗号不一致；");
                                checkResult.put("unitsys", "第" + row + "行，二期进馆案卷全宗号与卷内文件全宗号不一致；");
                                errtype = errtype + "二期进馆案卷全宗号与卷内文件全宗号不一致,";
                            }
                        }
                        if(unitNL.size()!=1){
                            errormessage.append("第" + row + "行，二期进馆案卷的卷内全宗名称不唯一；");
                            checkResult.put("unitsys", "第" + row + "行，二期进馆案卷的卷内全宗名称不唯一；");
                            errtype = errtype + "二期进馆案卷的卷内全宗名称不唯一,";
                        }else{
                            Map unitNM = (Map)unitNL.get(0);
                            String unitN = unitNM.get("unitname").toString();
                            if(!unitname.equals(unitN)){
                                errormessage.append("第" + row + "行，二期进馆案卷全宗名称与卷内文件全宗名称不一致；");
                                checkResult.put("unitname", "第" + row + "行，二期进馆案卷全宗名称与卷内文件全宗名称不一致；");
                                errtype = errtype + "二期进馆案卷全宗名称与卷内文件全宗名称不一致,";
                            }
                        }
                    }
                }
                if(checkContentM.get("真实性检查")!=null && !"".equals(checkContentM.get("真实性检查"))) {
                    if ((voldataM.get("flh") != null && !"".equals(voldataM.get("flh"))) && (voldataM.get("flname") != null && !"".equals(voldataM.get("flname")))) {
                        String flh = voldataM.get("flh").toString();
                        String flmc = voldataM.get("flname").toString();
                        String kv = kvm.get("c1").toString();
                        String where = "pname = '"+kv+"' and name = '检查分类号、分类名称是否一致'";
                        String configResult = rs.getConfig(where);
                        boolean resultb = false;
                        if ("0".equals(configResult)) {
                            resultb = false;
                        } else if (("1".equals(configResult))) {
                            resultb = true;
                        } else {
                            resultb = true;
                        }
//                        if(resultb) {
//                            String flname = "";
//                            String fenlh = "";
//                            String classtypesys = "D_CLASSIFY" + libcode;
//                            String whereStr = "classtypesys = '" + classtypesys + "'";
//                            if (version != null && !"".equals(version)) {
//                                whereStr = whereStr + " and version = '" + version + "'";
//                            }
//                            List<Map> flL = rs.getData("classcode,classname", "s_classify", whereStr);
//                            for (int f = 0; f < flL.size(); f++) {
//                                Map flm = (Map) flL.get(f);
//                                if (flm.get("classcode") != null && !"".equals(flm.get("classcode"))) {
//                                    fenlh = flm.get("classcode").toString();
//                                    if (fenlh.equals(flh)) {
//                                        flname = flm.get("classname").toString();
//                                        if (!flname.equals(flmc)) {
//                                            errormessage.append("第" + row + "行，二期进馆该案卷分类号对应的分类名称不在档案系统中；");
//                                            checkResult.put("flmc", "第" + row + "行，二期进馆该案卷分类号对应的分类名称不在档案系统中；");
//                                            errtype = errtype + "分类名称错误,";
//                                        }
//                                    }
//                                }
//                            }
//                            if (fenlh == null || "".equals(fenlh) || fenlh == "null") {
//                                errormessage.append("第" + row + "行，二期进馆该案卷分类号不在档案系统中；");
//                                checkResult.put("flh", "第" + row + "行，二期进馆该案卷分类号不在档案系统中；");
//                                errtype = errtype + "分类号错误,";
//                            }
//                        }
                        String where1 = "pname = '"+kv+"' and name = '检查案卷目录中分类号、分类名称与卷内目录的分类号、分类名称是否一致'";
                        String configResult1 = rs.getConfig(where1);
                        boolean resultb1 = false;
                        if ("0".equals(configResult1)) {
                            resultb1 = false;
                        } else if (("1".equals(configResult1))) {
                            resultb1 = true;
                        } else {
                            resultb1 = true;
                        }
                        if(resultb1) {
                            List<List> flDataL = bds.checkFlData(i, keyword, "file", voldataM, excelAttr);
                            for (int f1 = 0; f1 < flDataL.size(); f1++) {
                                List fileflcodeL = flDataL.get(0);
                                List fileflnameL = flDataL.get(1);
                                Set<String> fileflcodes = new HashSet<String>(fileflcodeL);
                                Set<String> fileflnames = new HashSet<String>(fileflnameL);
                                if (fileflcodes.size() == 1) {
                                    if (fileflnames.size() == 1) {
                                        for (String fileflcodestr : fileflcodes) {
                                            if (!fileflcodestr.equals(flh)) {
                                                errormessage.append("第" + row + "行，二期进馆该案卷的分类号不等于卷内文件分类号；");
                                                checkResult.put("flh", "第" + row + "行，二期进馆该案卷的分类号不等于卷内文件分类号；");
                                                errtype = errtype + "分类号错误,";
                                            }
                                        }
                                        for (String fileflnamestr : fileflnames) {
                                            if (!fileflnamestr.equals(flmc)) {
                                                errormessage.append("第" + row + "行，二期进馆该案卷的分类名称不等于卷内文件分类名称；");
                                                checkResult.put("flmc", "第" + row + "行，二期进馆该案卷的分类名称不等于卷内文件分类名称；");
                                                errtype = errtype + "分类名称错误,";
                                            }
                                        }
                                    } else {
                                        errormessage.append("第" + row + "行，二期进馆该案卷的卷内文件分类名称为空或有多个；");
                                        checkResult.put("flmc", "第" + row + "行，二期进馆该案卷的卷内文件分类名称为空或有多个；");
                                        errtype = errtype + "分类名称错误,";
                                    }
                                } else {
                                    errormessage.append("第" + row + "行，二期进馆该案卷的卷内文件分类号为空或有多个；");
                                    checkResult.put("flh", "第" + row + "行，二期进馆该案卷的卷内文件分类号为空或有多个；");
                                    errtype = errtype + "分类号错误,";
                                }
                            }
                        }
                        } else {
                            if (voldataM.get("flh") == null || "".equals(voldataM.get("flh"))) {
                                errormessage.append("第" + row + "行，二期进馆案卷分类号的值为空；");
                                checkResult.put("flh", "第" + row + "行，二期进馆案卷分类号的值为空；");
                                errtype = errtype + "分类号错误,";
                            }
                            if (voldataM.get("flname") == null || "".equals(voldataM.get("flname"))) {
                                errormessage.append("第" + row + "行，二期进馆案卷分类号的值为空；");
                                checkResult.put("flmc", "第" + row + "行，二期进馆案卷分类名称的值为空；");
                                errtype = errtype + "分类名称错误,";
                            }
                        }
                }
                //检测二期进馆页数和画幅数，数字化说明
                if(checkContentM.get("页数画幅数汇总统计")!=null && !"".equals(checkContentM.get("页数画幅数汇总统计"))) {
                        int zys = 0;
                        int hfs = 0;
                        boolean checkszh = true;
                        if ((voldataM.get("zys") != null && !"".equals(voldataM.get("zys"))) && (voldataM.get("hfs") != null && !"".equals(voldataM.get("hfs")))) {
                            zys = Integer.valueOf(voldataM.get("zys").toString());
                            hfs = Integer.valueOf(voldataM.get("hfs").toString());
                            //检测数字化情况
                            String szhsm = "";
                            String[] szhsms = new String[2];
                            if (voldataM.get("szhsm") != null && !"".equals(voldataM.get("szhsm"))) {
                                szhsm = voldataM.get("szhsm").toString();
                                boolean szhsmb = false;
                                if (szhsm.contains(";")) {
                                    szhsms = szhsm.split(";");
                                    szhsmb = true;
                                } else if (szhsm.contains("；")) {
                                    szhsms = szhsm.split("；");
                                    szhsmb = true;
                                } else {
                                    errormessage.append("第" + row + "行，二期进馆案卷数字化说明内容不是标准规范；");
                                    checkResult.put("szhsm", "第" + row + "行，二期进馆案卷数字化说明内容不是标准规范；");
                                    errtype = errtype + "数字化说明错误,";
                                    checkszh = false;
                                }
                                if(szhsmb) {
                                    String szhsmsFirst = szhsms[0];
                                    if (szhsmsFirst.startsWith("未扫_共")) {
                                        if (!szhsmsFirst.endsWith("页")) {
                                            errormessage.append("第" + row + "行，二期进馆案卷数字化说明内容不是标准规范；");
                                            checkResult.put("szhsm", "第" + row + "行，二期进馆案卷数字化说明内容不是标准规范；");
                                            errtype = errtype + "数字化说明错误,";
                                            checkszh = false;
                                        }
                                    } else {
                                        errormessage.append("第" + row + "行，二期进馆案卷数字化说明内容不是标准规范；");
                                        checkResult.put("szhsm", "第" + row + "行，二期进馆案卷数字化说明内容不是标准规范；");
                                        errtype = errtype + "数字化说明错误,";
                                        checkszh = false;
                                    }
                                    String szhsmsEnd = szhsms[1];
                                    if (szhsmsEnd.startsWith("重页_共")) {
                                        if (!szhsmsEnd.endsWith("页")) {
                                            errormessage.append("第" + row + "行，二期进馆案卷数字化说明内容不是标准规范；");
                                            checkResult.put("szhsm", "第" + row + "行，二期进馆案卷数字化说明内容不是标准规范；");
                                            errtype = errtype + "数字化说明错误,";
                                            checkszh = false;
                                        }
                                    } else {
                                        errormessage.append("第" + row + "行，二期进馆案卷数字化说明内容不是标准规范；");
                                        checkResult.put("szhsm", "第" + row + "行，二期进馆案卷数字化说明内容不是标准规范；");
                                        errtype = errtype + "数字化说明错误,";
                                        checkszh = false;
                                    }
                                    if (zys != hfs) {
                                        if (!checkszh) {
                                            errormessage.append("第" + row + "行，二期进馆案卷数字化说明内容不是标准规范；");
                                            checkResult.put("szhsm", "第" + row + "行，二期进馆案卷数字化说明内容不是标准规范；");
                                            errtype = errtype + "数字化说明错误,";
                                        } else {
                                            String ws = szhsms[0];//未扫说明,重页部分需求不明确，未写逻辑
                                            String wsys = ws.substring(ws.indexOf("未扫_共") + 4, ws.indexOf("页"));
                                            int wsnum = Integer.valueOf(wsys);
                                            if (wsnum + hfs != zys) {
                                                errormessage.append("第" + row + "行，二期进馆文件数字化说明内容不准确；");
                                                checkResult.put("szhsm", "第" + row + "行，二期进馆文件数字化说明内容不准确；");
                                                errtype = errtype + "数字化说明错误,";
                                            }
                                            String cy = szhsms[1];//重页说明
                                            String cyys = ws.substring(ws.indexOf("重页_共") + 4, ws.indexOf("页"));
                                            int cynum = Integer.valueOf(cyys);
                                            String tablename = archiveType + "filedata";
                                            String whereStr = "keyword like '" + keyword + "%' ORDER BY REVERSE(LEFT(REVERSE(keyword),INSTR(REVERSE(keyword),'-')-1))";
                                            int sum = rs.getSum("sum(pagenum)", tablename, whereStr);//案卷的卷内文件总页数
                                            if (sum > zys) {
                                                if (zys + cynum != sum) {
                                                    errormessage.append("第" + row + "行，二期进馆文件数字化说明内容不准确；");
                                                    checkResult.put("szhsm", "第" + row + "行，二期进馆文件数字化说明内容不准确；");
                                                    errtype = errtype + "数字化说明错误,";
                                                }
                                            }
                                        }
                                    }
                                }
                            }
//                            else {
//                                errormessage.append("第" + row + "行，二期进馆案卷数字化说明内容的值为空；");
//                                checkResult.put("szhsm", "第" + row + "行，二期进馆案卷数字化说明内容的值为空；");
//                                errtype = errtype + "数字化说明错误,";
//                                checkszh = false;
//                            }
                        } else {
                            if (voldataM.get("zys") == null || "".equals(voldataM.get("zys"))) {
                                errormessage.append("第" + row + "行，二期进馆案卷总页数的值为空；");
                                checkResult.put("zys", "第" + row + "行，二期进馆案卷总页数的值为空；");
                                errtype = errtype + "总页数错误,";
                            }
                            if (voldataM.get("hfs") == null || "".equals(voldataM.get("hfs"))) {
                                errormessage.append("第" + row + "行，二期进馆案卷总画幅数的值为空；");
                                checkResult.put("hfs", "第" + row + "行，二期进馆案卷总画幅数的值为空；");
                                errtype = errtype + "总画幅数错误,";
                            }
                        }
                }
            }
            //检测数据档号
//            if(checkContentM.get("keyword")!=null && !"".equals(checkContentM.get("keyword"))) {
//                String configResult = rs.getConfig("");
//                boolean resultb = false;
//                if ("0".equals(configResult)) {
//                    resultb = false;
//                } else if (("1".equals(configResult))) {
//                    resultb = true;
//                } else {
//                    resultb = true;
//                }
//                if(resultb) {
                    String result = bds.checkKeyword(row, keyword, "vol", voldataM, excelAttr);
                    if (result != null && !"".equals(result)) {
                        //rs.addResultInValue(excelAttr,result);
                        errormessage.append(result);
                        checkResult.put("keywordresult", result);
                        errtype = errtype + "档号错误,";
                    }
//                }
//            }
            //检测案卷题名是否包含中文
            if(checkContentM.get("真实性检查")!=null && !"".equals(checkContentM.get("真实性检查"))) {
                String kv = kvm.get("c1").toString();
                String where = "pname = '"+kv+"' and name = '题名中全部为英文字符检查 '";
                String configResult = rs.getConfig(where);
                boolean resultb = false;
                if ("0".equals(configResult)) {
                    resultb = false;
                } else if (("1".equals(configResult))) {
                    resultb = true;
                } else {
                    resultb = true;
                }
                String title = "";
                if(resultb) {
                    if (voldataM.get("title") != null && !"".equals(voldataM.get("title"))) {
                        title = voldataM.get("title").toString();
                        checkResult.put("title", title);
                        boolean titleb = bds.isContainChinese(title);
                        if (!titleb) {
                            errormessage.append("第" + row + "行，案卷题名" + title + "没有中文；");
                            checkResult.put("titleresult", "第" + row + "行，案卷题名" + title + "没有中文；");
                            errtype = errtype + "案卷题名错误,";
                        }else{
                            String kv1 = kvm.get("c1").toString();
                            String where1 = "pname = '"+kv1+"' and name = '题名长度 '";
                            String configResult1 = rs.getConfig(where1);
                            boolean resultb1 = false;
                            if ("0".equals(configResult1)) {
                                resultb1 = false;
                            } else if (("1".equals(configResult1))) {
                                resultb1 = true;
                            } else {
                                resultb1 = true;
                            }
                            if(resultb1) {
                                int titleLength = title.length();
                                if(titleLength<Integer.valueOf(configResult1)){
                                    errormessage.append("第" + row + "行，案卷题名" + title + "的长度小于规定长度"+configResult1+"；");
                                    checkResult.put("titleresult", "第" + row + "行，案卷题名" + title + "的长度小于规定长度"+configResult1+"；");
                                    errtype = errtype + "案卷题名错误,";
                                }else{
                                    //检测卷内文件
                                    String checkFileTileresult = bds.checkFileTile(row, keyword, "file", voldataM, excelAttr,configResult1);
                                    if (checkFileTileresult != null && !"".equals(checkFileTileresult)) {
                                        errormessage.append(checkFileTileresult);
                                        checkResult.put("titleresult", checkFileTileresult);
                                        errtype = errtype + "卷内文件题名错误,";
                                    }
                                }
                            }
                        }
                    } else {
                        errormessage.append("第" + row + "行，案卷题名的值为空；");
                        checkResult.put("titleresult", "第" + row + "行，案卷题名的值为空；");
                        errtype = errtype + "案卷题名错误,";
                    }
                }
                String where1 = "pname = '"+kv+"' and name = '案卷题名与卷内题名是否重复检查 '";
                String configResult1 = rs.getConfig(where1);
                boolean resultb1 = false;
                if ("0".equals(configResult1)) {
                    resultb1 = false;
                } else if (("1".equals(configResult1))) {
                    resultb1 = true;
                } else {
                    resultb1 = true;
                }
                if(resultb1) {
                    if(title!=null && !"".equals(title)){
                        if(titleSet.size()==0){
                            titleSet.add(title);
                        }else {
                            int titleSetb = titleSet.size();
                            titleSet.add(title);
                            int titleSeta = titleSet.size();
                            if (titleSetb == titleSeta) {
                                errormessage.append("第" + row + "行，该案卷的案卷题名已存在；");
                                checkResult.put("titleResult", "第" + row + "行，该案卷的案卷题名已存在；");
                                errtype = errtype + "文件题名错误,";
                            }
                        }
                    }
                }
            }
            //检测责任者是否包含特殊符号
            if(checkContentM.get("真实性检查")!=null && !"".equals(checkContentM.get("真实性检查"))) {
                String kv = kvm.get("c1").toString();
                String where = "pname = '"+kv+"' and name = '责任者检查 '";
                String configResult = rs.getConfig(where);
                boolean resultb = false;
                if ("0".equals(configResult)) {
                    resultb = false;
                } else if (("1".equals(configResult))) {
                    resultb = true;
                } else {
                    resultb = true;
                }
                if(resultb) {
                    if(voldataM.get("zrz")!=null && !"".equals(voldataM.get("zrz"))) {
                        String zrz = voldataM.get("zrz").toString();
                        char isLegalChar = bds.validateLegalString(zrz, configResult);
                        if ("t".equals(isLegalChar)) {
                            errormessage.append("第" + row + "行，责任者" + zrz + "包含特殊符号；");
                            checkResult.put("zrz", "第" + row + "行，责任者" + zrz + "包含特殊符号；");
                            errtype = errtype + "责任者错误,";
                        }
                    }else{
                        errormessage.append("第" + row + "行，责任者的值为空；");
                        checkResult.put("zrz", "第" + row + "行，责任者的值为空；");
                        errtype = errtype + "责任者错误,";
                    }
                }
            }
            //检测起始时间和终止时间
            if(checkContentM.get("真实性检查")!=null && !"".equals(checkContentM.get("真实性检查"))) {
                String kv = kvm.get("c1").toString();
                String where = "pname = '"+kv+"' and name = '日期格式检查  '";
                String configResult = rs.getConfig(where);
                boolean resultb = false;
                if ("0".equals(configResult)) {
                    resultb = false;
                } else if (("1".equals(configResult))) {
                    resultb = true;
                } else {
                    resultb = true;
                }
                if(resultb) {
                    String qsrq = voldataM.get("qsrq").toString();
                    String zzrq = voldataM.get("zzrq").toString();
                    if ((voldataM.get("qsrq") != null && !"".equals(voldataM.get("qsrq"))) && (voldataM.get("qsrq") != null && !"".equals(voldataM.get("qsrq")))) {
                        String[] dates = bds.checkqzdate(i, keyword, "file", voldataM, excelAttr,configResult);//查询文件级日期数据
                        if (dates.length < 2) {
                            errormessage.append("第" + row + "行，案卷的卷内文件日期异常可能格式不正确或日期的内容为空；");
                            checkResult.put("qsrq", "第" + row + "行，案卷的卷内文件日期异常可能格式不正确或日期的内容为空；");
                            checkResult.put("zzrq", "第" + row + "行，案卷的卷内文件日期异常可能格式不正确或日期的内容为空；");
                            errtype = errtype + "日期错误,";
                        } else if (dates.length == 2) {
                            if (qsrq.length() == 6) {
                                if (!dates[0].equals(qsrq)) {
                                    errormessage.append("第" + row + "行，案卷起始日期不等于卷内文件成文日期最早的日期；");
                                    checkResult.put("qsrq", "第" + row + "行，案卷起始日期不等于卷内文件成文日期最早的日期；");
                                    errtype = errtype + "日期错误,";
                                }
                            } else if (qsrq.length() == 8) {
                                if (!dates[0].equals(qsrq)) {
                                    errormessage.append("第" + row + "行，案卷起始日期不等于卷内文件成文日期最早的日期；");
                                    checkResult.put("qsrq", "第" + row + "行，案卷起始日期不等于卷内文件成文日期最早的日期；");
                                    errtype = errtype + "日期错误,";
                                }
                            } else if (qsrq.length() == 10 && qsrq.contains("-")) {
                                String[] rqs = qsrq.split("-");
                                String date = rqs[0] + rqs[1]+rqs[2];
                                if (!dates[0].equals(date)) {
                                    errormessage.append("第" + row + "行，案卷起始日期不等于卷内文件成文日期最早的日期；");
                                    checkResult.put("qsrq", "第" + row + "行，案卷起始日期不等于卷内文件成文日期最早的日期；");
                                    errtype = errtype + "日期错误,";
                                }
                            }
                            if (zzrq.length() == 6) {
                                if (!dates[1].equals(zzrq)) {
                                    errormessage.append("第" + row + "行，案卷终止日期不等于卷内文件成文日期最晚的日期；");
                                    checkResult.put("zzrq", "第" + row + "行，案卷终止日期不等于卷内文件成文日期最晚的日期；");
                                    errtype = errtype + "日期错误,";
                                }
                            } else if (zzrq.length() == 8) {
                                if (!dates[1].equals(zzrq)) {
                                    errormessage.append("第" + row + "行，案卷终止日期不等于卷内文件成文日期最晚的日期；");
                                    checkResult.put("zzrq", "第" + row + "行，案卷终止日期不等于卷内文件成文日期最晚的日期；");
                                    errtype = errtype + "日期错误,";
                                }
                            } else if (zzrq.length() == 10 && zzrq.contains("-")) {
                                String[] rqs = zzrq.split("-");
                                String date = rqs[0] + rqs[1]+rqs[2];
                                if (!dates[1].equals(date)) {
                                    errormessage.append("第" + row + "行，案卷终止日期不等于卷内文件成文日期最晚的日期；");
                                    checkResult.put("zzrq", "第" + row + "行，案卷终止日期不等于卷内文件成文日期最晚的日期；");
                                    errtype = errtype + "日期错误,";
                                }
                            }
                        }
                    } else {
                        if (voldataM.get("qsrq") != null && !"".equals(voldataM.get("qsrq"))) {
                            errormessage.append("第" + row + "行，案卷起始时间的值为空；");
                            checkResult.put("qsrq", "第" + row + "行，案卷起始时间的值为空；");
                            errtype = errtype + "日期错误,";
                        }
                        if (voldataM.get("zzrq") != null && !"".equals(voldataM.get("zzrq"))) {
                            errormessage.append("第" + row + "行，案卷终止日期的值为空；");
                            checkResult.put("zzrq", "第" + row + "行，案卷终止日期的值为空；");
                            errtype = errtype + "日期错误,";
                        }
                    }
                }
            }
            //检测总件数
            if(checkContentM.get("可用性检查")!=null && !"".equals(checkContentM.get("可用性检查"))) {
                String kv = kvm.get("c9").toString();
                String where = "pname = '"+kv+"' and name = '数量一致性检查'";
                String configResult = rs.getConfig(where);
                boolean resultb = false;
                if ("0".equals(configResult)) {
                    resultb = false;
                } else if (("1".equals(configResult))) {
                    resultb = true;
                } else {
                    resultb = true;
                }
                if(resultb) {
                    int zjs = 0;
                    if (voldataM.get("zjs") != null && !"".equals(voldataM.get("zjs"))) {
                        zjs = Integer.valueOf(voldataM.get("zjs").toString());
                        int filejs = bds.checkzjs(i, keyword, "file", voldataM, excelAttr);
                        if (zjs < filejs) {
                            errormessage.append("第" + row + "行，案卷的总件数小于卷内文件个数；");
                            checkResult.put("zjs", "第" + row + "行，案卷的总件数小于卷内文件个数；");
                            errtype = errtype + "总件数错误,";
                        } else if (zjs > filejs) {
                            errormessage.append("第" + row + "行，案卷的总件数大于卷内文件个数；");
                            checkResult.put("zjs", "第" + row + "行，案卷的总件数大于卷内文件个数；");
                            errtype = errtype + "总件数错误,";
                        }
                    }
                }
            }
            //检测页号
            if(checkContentM.get("页数页号检查")!=null && !"".equals(checkContentM.get("页数页号检查"))) {
                    String yhresult = bds.checkyh(row, keyword, "file", voldataM, excelAttr);
                    boolean yhb = bds.isContainChinese(yhresult);
                    if (yhb) {
                        errormessage.append(yhresult);
                        checkResult.put("yh", yhresult);
                        errtype = errtype + "页号错误,";
                    } else {
                        //检测总页数
                        int zys = 0;
                        if (voldataM.get("zys") != null && !"".equals(voldataM.get("zys"))) {
                            zys = Integer.valueOf(voldataM.get("zys").toString());
                            int fileys = bds.checkzys(i, keyword, "file", voldataM, excelAttr);
                            //String yh = bds.getzzyh(i,keyword,"file",voldataM,excelAttr);
                            if (yhresult != null && !"".equals(yhresult)) {
                                int yh = Integer.valueOf(yhresult);
                                //int zzyh = Integer.valueOf(yh);
                                if (zys < fileys) {
                                    errormessage.append("第" + row + "行，案卷的总页数小于卷内文件页数之和；");
                                    checkResult.put("zys", "第" + row + "行，案卷的总页数小于卷内文件页数之和；");
                                    errtype = errtype + "总页数错误,";
                                } else if (zys > fileys) {
                                    errormessage.append("第" + row + "行，案卷的总页数大于卷内文件页数之和；");
                                    checkResult.put("zys", "第" + row + "行，案卷的总页数大于卷内文件页数之和；");
                                    errtype = errtype + "总页数错误,";
                                } else if (zys < yh) {
                                    errormessage.append("第" + row + "行，案卷的总页数小于卷内文件终止页号；");
                                    checkResult.put("zys", "第" + row + "行，案卷的总页数小于卷内文件终止页号；");
                                    errtype = errtype + "总页数错误,";
                                } else if (zys > yh) {
                                    errormessage.append("第" + row + "行，案卷的总页数大于卷内文件终止页号；");
                                    checkResult.put("zys", "第" + row + "行，案卷的总页数大于卷内文件终止页号；");
                                    errtype = errtype + "总页数错误,";
                                }
                            } else {
                                errormessage.append("第" + row + "行，案卷的卷内文件，页号不符合规则，或没有页号；");
                                checkResult.put("zys", "第" + row + "行，案卷的卷内文件，页号不符合规则，或没有页号；");
                                errtype = errtype + "总页数错误,";
                            }
                        }
                    }
            }
            //检测密级，保密期限
            if(checkContentM.get("真实性检查")!=null && !"".equals(checkContentM.get("真实性检查"))) {
                String kv = kvm.get("c1").toString();
                String where = "pname = '"+kv+"' and name = '案卷目录密级为对应卷内目录最高密级  '";
                String configResult = rs.getConfig(where);
                boolean resultb = false;
                if ("0".equals(configResult)) {
                    resultb = false;
                } else if (("1".equals(configResult))) {
                    resultb = true;
                } else {
                    resultb = true;
                }
                if (resultb) {
                    String mj = "";
//                    if (voldataM.get("mj") != null && !"".equals(voldataM.get("mj"))) {
                    if (voldataM.get("mj") != null && !"".equals(voldataM.get("mj"))) {
                        mj = voldataM.get("mj").toString();
                    }
                    if("绝密".equals(mj) || "机密".equals(mj) || "秘密".equals(mj) || "".equals(mj)){
//                      String mjcode = mj.split("_")[0];
                        Set mjs = bds.getmj(i, keyword, "file", voldataM, excelAttr);
                        if (mjs.size() >= 0) {
                            List mjvalue = new ArrayList(mjs);
                            Collections.sort(mjvalue);
                            int code = 0;
                            if("绝密".equals(mj)){
                                code = 3;
                            }else if("机密".equals(mj)){
                                code = 2;
                            }else if("秘密".equals(mj)){
                                code = 1;
                            }else if("".equals(mj)){
                                code = 0;
                            }else {
                                code = 4;
                            }
                            int value = Integer.valueOf(mjvalue.get(mjvalue.size() - 1).toString());
                            if(value>3){
                                errormessage.append("第" + row + "行，密级与档案系统密级不匹配；");
                                checkResult.put("mj", "第" + row + "行，密级与档案系统密级不匹配；");
                                errtype = errtype + "密级错误,";
                            }else{
                                if (code < value) {
                                    errormessage.append("第" + row + "行，有卷内文件密级的等级大于案卷的密级等级；");
                                    checkResult.put("mj", "第" + row + "行，有卷内文件密级的等级大于案卷的密级等级；");
                                    errtype = errtype + "密级错误,";
                                } else if (code > value) {
                                    errormessage.append("第" + row + "行，案卷的密级等级大于所有卷内文件的密级；");
                                    checkResult.put("mj", "第" + row + "行，案卷的密级等级大于所有卷内文件的密级；");
                                    errtype = errtype + "密级错误,";
                                }
                            }
                        } else {
                            errormessage.append("第" + row + "行，案卷的卷内文件均无密级，或密级与档案系统密级不匹配；");
                            checkResult.put("mj", "第" + row + "行，案卷的卷内文件均无密级，或密级与档案系统密级不匹配；");
                            errtype = errtype + "密级错误,";
                        }
                    }else{
                        errormessage.append("第" + row + "行，密集中只能出现“绝密”、“机密”、“秘密”或空白；");
                        checkResult.put("mj", "第" + row + "行，案卷的卷内文件均无密级，或密级与档案系统密级不匹配；");
                        errtype = errtype + "密级错误,";
                    }
//               }
                    if (checkContentM.get("bmqx") != null && !"".equals(checkContentM.get("bmqx"))) {
                        String bmqx = "";
                        if (voldataM.get("bmqx") != null && !"".equals(voldataM.get("bmqx"))) {
                            bmqx = voldataM.get("bmqx").toString();
                            if (mj != null && !"".equals(mj)) {
                                if (bmqx == null || "".equals(bmqx) || bmqx == "null") {
                                    errormessage.append("第" + row + "行，案卷保密期限的值为空；");
                                    checkResult.put("bmqx", "第" + row + "行，案卷保密期限的值为空；");
                                    errtype = errtype + "保密期限错误,";
                                }
                            }
                        }
                    }
                }
            }
            if(errtype!=null && !"".equals(errtype)){
                errtype = errtype.substring(0,errtype.length()-1);
                checkResult.put("errortype",errtype);
            }
            checkResult.put("errormessage",errormessage);
            checkResult.put("dataly","excel");
            if((errtype!=null && !"".equals(errtype)) && ((errormessage!=null && !"".equals(errormessage)))){
                rs.addResultInValue(checkResult);
                checkResult = new HashMap();
            }
        }
            //科研档案案卷目录检测
            if(libname.contains("科研")) {
                if (checkContentM.get("科研档案案卷目录检查") != null && !"".equals(checkContentM.get("科研档案案卷目录检查"))) {
//                    String kv = kvm.get("c7").toString();
//                    String where = "pname = '" + kv + "' and name = '特殊字符检查'";
//                    String configResult = rs.getConfig(where);
//                    boolean resultb = false;
//                    if ("0".equals(configResult)) {
//                        resultb = false;
//                    } else if (("1".equals(configResult))) {
//                        resultb = true;
//                    } else {
//                        resultb = true;
//                    }
//                    if (resultb) {
                        String getkytitleSql = "select distinct(kytitle) from eqvoldata";
                        List kytitleL = this.jdbcTemplate.queryForList(getkytitleSql);
                        for(int i=0;i<kytitleL.size();i++){
                            Map kytitleM = (Map)kytitleL.get(i);
                            if (kytitleM.get("kytitle") != null && !"".equals(kytitleM.get("kytitle"))) {
                                String kytitle = kytitleM.get("kytitle").toString();
                                String checkKYvolSql = "select syscode,keyword,flh,kytitle,ssxk,szmc,ktbh,ktfzr,ktcjr,ktxzdw,rwly,djj,gjj from eqvoldata where kytitle = '"+kytitle+"'";
                                List kyVolL = this.jdbcTemplate.queryForList(checkKYvolSql);
                                Set flhS = new HashSet();
                                Set ssxkS = new HashSet();
                                Set szmcS = new HashSet();
                                Set ktbhS = new HashSet();
                                Set ktfzrS = new HashSet();
                                Set ktcjrS = new HashSet();
                                Set ktxzdwS = new HashSet();
                                Set rwlyS = new HashSet();
                                int szVol = 0;
                                for(int j=0;j<kyVolL.size();j++){
                                    Map kyVolM = (Map)kyVolL.get(j);
                                    String keyword = "";
                                    if (kyVolM.get("keyword") != null && !"".equals(kyVolM.get("keyword"))) {
                                        keyword = kyVolM.get("keyword").toString();
                                    }
                                    if (kyVolM.get("flh") != null && !"".equals(kyVolM.get("flh"))) {
                                        String flh = kyVolM.get("flh").toString();
                                        flhS.add(flh);
                                    }
                                    if (kyVolM.get("ssxk") != null && !"".equals(kyVolM.get("ssxk"))) {
                                        String ssxk = kyVolM.get("ssxk").toString();
                                        ssxkS.add(ssxk);
                                    }
                                    if (kyVolM.get("szmc") != null && !"".equals(kyVolM.get("szmc"))) {
                                        String szmc = kyVolM.get("szmc").toString();
                                        szmcS.add(szmc);
                                    }
                                    if (kyVolM.get("ktbh") != null && !"".equals(kyVolM.get("ktbh"))) {
                                        String ktbh = kyVolM.get("ktbh").toString();
                                        ktbhS.add(ktbh);
                                    }
                                    if (kyVolM.get("ktfzr") != null && !"".equals(kyVolM.get("ktfzr"))) {
                                        String ktfzr = kyVolM.get("ktfzr").toString();
                                        ktfzrS.add(ktfzr);
                                    }
                                    if (kyVolM.get("ktcjr") != null && !"".equals(kyVolM.get("ktcjr"))) {
                                        String ktcjr = kyVolM.get("ktcjr").toString();
                                        ktcjrS.add(ktcjr);
                                    }
                                    if (kyVolM.get("ktxzdw") != null && !"".equals(kyVolM.get("ktxzdw"))) {
                                        String ktxzdw = kyVolM.get("ktxzdw").toString();
                                        ktxzdwS.add(ktxzdw);
                                    }
                                    if (kyVolM.get("rwly") != null && !"".equals(kyVolM.get("rwly"))) {
                                        String rwly = kyVolM.get("rwly").toString();
                                        rwlyS.add(rwly);
                                    }
                                    if (kyVolM.get("djj") != null && !"".equals(kyVolM.get("djj"))) {
                                        int djj = Integer.valueOf(kyVolM.get("djj").toString());
                                        if(djj==0 || djj>kyVolL.size()){
                                            String updKYvol = "update s_volresult set djj = '科研案卷目录"+kytitle+"该课题名称的所在卷为0或大于总卷数' where kytitle = '" + kytitle + "' and keyword = '"+keyword+"'";
                                            this.jdbcTemplate.update(updKYvol);
                                        }
                                        if(szVol==0){
                                            if(djj==1){
                                                szVol = djj;
                                                continue;
                                            }else {
                                                String updKYvol = "update s_volresult set djj = '科研案卷目录"+kytitle+"该课题名称的所在卷不是以1开始' where kytitle = '" + kytitle + "' and keyword = '"+keyword+"'";
                                                this.jdbcTemplate.update(updKYvol);
                                            }
                                        }else{
                                            if(djj-szVol!=1){
                                                String updKYvol = "update s_volresult set djj = '科研案卷目录"+kytitle+"该课题名称的所在卷顺序不正确' where kytitle = '" + kytitle + "' and keyword = '"+keyword+"'";
                                                this.jdbcTemplate.update(updKYvol);
                                            }
                                        }
                                    }
                                    if (kyVolM.get("gjj") != null && !"".equals(kyVolM.get("gjj"))) {
                                        int gjj = Integer.valueOf(kyVolM.get("gjj").toString());
                                        if(gjj!=kyVolL.size()){
                                            String updKYvol = "update s_volresult set gjj = '科研案卷目录"+kytitle+"该课题名称的总卷数和课题的案卷数不一致' where kytitle = '" + kytitle + "' and keyword = '"+keyword+"'";
                                            this.jdbcTemplate.update(updKYvol);
                                        }
                                    }
                                }
                                String updFlds = "";
                                if(flhS.size()>1){
                                    updFlds = updFlds+"flh = '科研案卷目录"+kytitle+"该课题名称的分类号不一致',";
                                }
                                if(ssxkS.size()>1){
                                    updFlds = updFlds+"ssxk = '科研案卷目录"+kytitle+"该课题名称的所属学科不一致',";
                                }
                                if(szmcS.size()>1){
                                    updFlds = updFlds+"szmc = '科研案卷目录"+kytitle+"该课题名称的室组名称不一致',";
                                }
                                if(ktbhS.size()>1){
                                    updFlds = updFlds+"ktbh = '科研案卷目录"+kytitle+"该课题名称的课题编号不一致',";
                                }
                                if(ktfzrS.size()>1){
                                    updFlds = updFlds+"ktfzr = '科研案卷目录"+kytitle+"该课题名称的科研负责人不一致',";
                                }
                                if(ktcjrS.size()>1){
                                    updFlds = updFlds+"ktcjr = '科研案卷目录"+kytitle+"该课题名称的科研参与人不一致',";
                                }
                                if(ktxzdwS.size()>1){
                                    updFlds = updFlds+"ktxzdw = '科研案卷目录"+kytitle+"该课题名称的课题协助单位不一致',";
                                }
                                if(rwlyS.size()>1){
                                    updFlds = updFlds+"rwly = '科研案卷目录"+kytitle+"该课题名称的任务来源不一致',";
                                }
                                updFlds = updFlds.substring(0,updFlds.length()-1);
                                if(updFlds.length()>0) {
                                    String updKYvol = "update s_volresult set " + updFlds + " where kytitle = '" + kytitle + "'";
                                    this.jdbcTemplate.update(updKYvol);
                                }
                            }
                        }
//                    }
                }
            }
        }catch (Exception e){
            log.info(e.getMessage());
            checkVolDataResult = false;
        }
        return checkVolDataResult;
    }

    public boolean checkFiledata(List filedata,Map excelAttr){
        String errtype = "";
        Map<String,Object> checkResult = new HashMap();
        boolean checkFileDataResult = true;
        try {
            excelAttr.put("arclvl", "file");
            String archiveType = excelAttr.get("archiveType").toString();
            String libcode = excelAttr.get("libcode").toString();
            String version ="";
            if(excelAttr.get("version")!=null && !"".equals(excelAttr.get("version"))) {
                version = excelAttr.get("version").toString();
            }
            String selectStr = "libcode = '"+libcode+"'";
            String libname = "";
            String arctype = "";
            List libnameL = rs.getData("chname,arctype","s_arc",selectStr);
            if(libnameL.size()>0){
                Map libnameM = (Map)libnameL.get(0);
                if(libnameM.get("chname")!=null && !"".equals(libnameM.get("chname"))) {
                    libname = libnameM.get("chname").toString();
                }
                if(libnameM.get("arctype")!=null && !"".equals(libnameM.get("arctype"))) {
                    arctype = libnameM.get("arctype").toString();
                }
            }
            Map checkContentM = (Map)excelAttr.get("checkContent");
            Set wenhaos = new HashSet();
            Set titleSet = new HashSet();
            String yhb = "";
            int ys = 0;
            int yhs = 0;
            String yh = "";
            Map kvm = getCheckType();
            String pior="000";
            Set unitsyss = new HashSet();
            Set unitnames = new HashSet();
            //检测全宗号、全宗机构名称
            for(int i=0;i<filedata.size();i++) {
                checkResult.put("libcode",libcode);
                checkResult.put("archiveType",archiveType);
                checkResult.put("arclvl","file");
                checkResult.put("libname",libname);
                StringBuffer errormessage = new StringBuffer();
                int row = i+2;
                Map<String,Object> filedataM = (Map) filedata.get(i);
                String keyword = filedataM.get("keyword").toString();
                checkResult.put("keyword",keyword);
                //检查字段是否包含特殊符号
                if(checkContentM.get("真实性检查")!=null && !"".equals(checkContentM.get("真实性检查"))){
                    String kv = kvm.get("c1").toString();
                    String where = "pname = '"+kv+"' and name = '特殊字符检查'";
                    String configResult = rs.getConfig(where);
                    boolean resultb = false;
                    if ("0".equals(configResult)) {
                        resultb = false;
                    } else if (("1".equals(configResult))) {
                        resultb = true;
                    } else {
                        resultb = true;
                    }
                    if(resultb) {
                        boolean fileB = false;
                        for (String key : filedataM.keySet()) {
                            if(filedataM.get(key)!=null && !"".equals(filedataM.get(key))) {
                                String value = filedataM.get(key).toString();
                                System.out.println(key + "  " + value);
                                char isLegalChar = bds.validateLegalString(value, configResult);
                                boolean space = bds.checkkg(value);
                                if ("t".equals(isLegalChar) || !space) {
                                    fileB = true;
                                }
                            }
                        }
                        if(fileB){
                            errormessage.append("文件信息中的字段包含特殊符号或空格；");
                            checkResult.put("filefield", "文件信息中的字段包含特殊符号或空格；");
                            errtype = errtype + "文件信息中的字段包含特殊符号或空格,";
                        }
                    }
                }
                if ("eq".equals(archiveType)) {//如果是二期进馆则检测全宗信息和分类号信息
                    //检测二期进馆文件全宗信息
                    if ("vf".equals(arctype)) {
                        if (checkContentM.get("真实性检查") != null && !"".equals(checkContentM.get("真实性检查"))) {
                            String kv = kvm.get("c1").toString();
                            String where1 = "pname = '" + kv + "' and name = '检查全宗号、全宗名称是否一致'";
                            String configResult = rs.getConfig(where1);
                            boolean resultb = false;
                            if ("0".equals(configResult)) {
                                resultb = false;
                            } else if (("1".equals(configResult))) {
                                resultb = true;
                            } else {
                                resultb = true;
                            }
                            if (resultb) {
                                String table = archiveType + "voldata";
                                String volKeyword = keyword.substring(0, keyword.lastIndexOf("-"));
                                String checkVolUnitSql = "select unitsys,unitname from " + table + " where keyword = '" + volKeyword + "'";
                                List volUnitL = this.jdbcTemplate.queryForList(checkVolUnitSql);
                                if (volUnitL.size() >= 1) {
                                    Map volUnitM = (Map) volUnitL.get(0);
                                    if (volUnitM.get("unitsys") != null && !"".equals(volUnitM.get("unitsys"))) {
                                        String volUnitcode = volUnitM.get("unitsys").toString();
                                        if (!volUnitcode.equals((filedataM.get("unitsys")))) {
                                            errormessage.append("第" + row + "行，该文件全宗号与所属案卷不一致；");
                                            checkResult.put("unitsys", "第" + row + "行，该文件全宗号与所属案卷不一致；");
                                            errtype = errtype + "全宗号错误,";
                                        }
                                    } else {
                                        errormessage.append("第" + row + "行，该文件所属案卷全宗号为空；");
                                        checkResult.put("unitsys", "第" + row + "行，该文件所属案卷全宗号为空；");
                                        errtype = errtype + "全宗号错误,";
                                    }
                                    if (volUnitM.get("unitname") != null && !"".equals(volUnitM.get("unitname"))) {
                                        String volUnitname = volUnitM.get("unitname").toString();
                                        if (!volUnitname.equals((filedataM.get("unitname")))) {
                                            errormessage.append("第" + row + "行，该文件全宗名称与所属案卷不一致；");
                                            checkResult.put("unitname", "第" + row + "行，该文件全宗名称与所属案卷不一致；");
                                            errtype = errtype + "全宗名称错误,";
                                        }
                                    } else {
                                        errormessage.append("第" + row + "行，该文件所属案卷全宗名称为空；");
                                        checkResult.put("unitname", "第" + row + "行，该文件所属案卷全宗名称为空；");
                                        errtype = errtype + "全宗名称错误,";
                                    }
                                } else {
                                    errormessage.append("第" + row + "行，该文件所属案卷全宗号为空；");
                                    checkResult.put("unitsys", "第" + row + "行，该文件所属案卷全宗号为空；");
                                    errtype = errtype + "全宗号错误,";
                                    errormessage.append("第" + row + "行，该文件所属案卷全宗名称为空；");
                                    checkResult.put("unitname", "第" + row + "行，该文件所属案卷全宗名称为空；");
                                    errtype = errtype + "全宗名称错误,";
                                }
                            }
                        }
                    } else {
                        if (filedataM.get("unitsys") != null && !"".equals(filedataM.get("unitsys"))) {
                            String unitsys = filedataM.get("unitsys").toString();
                            unitsyss.add(unitsys);
                            if (unitsyss.size() != 1) {
                                errormessage.append("sip包，全宗号不唯一");
                                checkResult.put("unitsys", "全宗号不唯一");
                                errtype = errtype + "全宗号不唯一";
                            }
                        } else {
                            errormessage.append("sip包，全宗号为空");
                            checkResult.put("unitsys", "全宗号为空");
                            errtype = errtype + "全宗号为空";
                        }
                        if (filedataM.get("unitname") != null && !"".equals(filedataM.get("unitname"))) {
                            String unitname = filedataM.get("unitname").toString();
                            unitnames.add(unitname);
                            String unitsys = filedataM.get("unitsys").toString();
                            unitsyss.add(unitsys);
                            if (unitsyss.size() != 1) {
                                errormessage.append("sip包，全宗名称不唯一");
                                checkResult.put("unitname", "全宗名称不唯一");
                                errtype = errtype + "全宗名称不唯一";
                            }
                        } else {
                            errormessage.append("sip包，全宗名称为空");
                            checkResult.put("unitname", "全宗名称为空");
                            errtype = errtype + "全宗名称为空";
                        }
                    }

                    //检测二期进馆文件分类信息
                    if ("vf".equals(arctype)) {
                    if (checkContentM.get("真实性检查") != null && !"".equals(checkContentM.get("真实性检查"))) {
                        String kv = kvm.get("c1").toString();
                        String where = "pname = '" + kv + "' and name = '检查分类号、分类名称是否一致'";
                        String configResult = rs.getConfig(where);
                        boolean resultb = false;
                        if ("0".equals(configResult)) {
                            resultb = false;
                        } else if (("1".equals(configResult))) {
                            resultb = true;
                        } else {
                            resultb = true;
                        }
                        if (resultb) {
//                            if ((filedataM.get("flh") != null && !"".equals(filedataM.get("flh"))) && (filedataM.get("flname") != null && !"".equals(filedataM.get("flname")))) {
//                                String flh = filedataM.get("flh").toString();
//                                String flmc = filedataM.get("flname").toString();
//                                String flname = "";
//                                String fenlh = "";
//                                String classtypesys = "D_CLASSIFY" + libcode;
//                                String whereStr = "classtypesys = '" + classtypesys + "'";
//                                if (version != null && !"".equals(version)) {
//                                    whereStr = whereStr + " and version = '" + version + "'";
//                                }
//                                List<Map> flL = rs.getData("classcode,classname", "s_classify", whereStr);
//                                for (int f = 0; f < flL.size(); f++) {
//                                    Map flm = (Map) flL.get(f);
//                                    if (flm.get("classcode") != null && !"".equals(flm.get("classcode"))) {
//                                        fenlh = flm.get("classcode").toString();
//                                        if (fenlh.equals(flh)) {
//                                            flname = flm.get("classname").toString();
//                                            if (!flname.equals(flmc)) {
//                                                errormessage.append("第" + row + "行，二期进馆该文件分类号对应的分类名称不在档案系统中；");
//                                                checkResult.put("flmc", "第" + row + "行，二期进馆该文件分类号对应的分类名称不在档案系统中；");
//                                                errtype = errtype + "分类名称错误,";
//                                            }
//                                        }
//                                    }
//                                }
//                                if (fenlh == null || "".equals(fenlh) || fenlh == "null") {
//                                    errormessage.append("第" + row + "行，二期进馆该文件分类号不在档案系统中；");
//                                    checkResult.put("flh", "第" + row + "行，二期进馆该文件分类号不在档案系统中；");
//                                    errtype = errtype + "分类号错误,";
//                                }
//                            } else {
//                                if (filedataM.get("flh") != null && !"".equals(filedataM.get("flh"))) {
//                                    errormessage.append("第" + row + "行，二期进馆文件分类号的值为空；");
//                                    checkResult.put("flh", "第" + row + "行，二期进馆文件分类号的值为空；");
//                                    errtype = errtype + "分类号错误,";
//                                }
//                                if (filedataM.get("flmc") != null && !"".equals(filedataM.get("flmc"))) {
//                                    errormessage.append("第" + row + "行，二期进馆文件分类名称的值为空；");
//                                    checkResult.put("flmc", "第" + row + "行，二期进馆文件分类名称的值为空；");
//                                    errtype = errtype + "分类名称错误,";
//                                }
//                            }
//                        }
                            String table = archiveType + "voldata";
                            String volKeyword = keyword.substring(0, keyword.lastIndexOf("-"));
                            String checkVolUnitSql = "select flh,flname from " + table + " where keyword = '" + volKeyword + "'";
                            List volFlL = this.jdbcTemplate.queryForList(checkVolUnitSql);
                            if (volFlL.size() >= 1) {
                                Map volFlM = (Map) volFlL.get(0);
                                if (volFlM.get("flh") != null && !"".equals(volFlM.get("flh"))) {
                                    String volFlcode = volFlM.get("flh").toString();
                                    if (!volFlcode.equals((filedataM.get("flh")))) {
                                        errormessage.append("第" + row + "行，该文件分类号与所属案卷不一致；");
                                        checkResult.put("flh", "第" + row + "行，该文件分类号与所属案卷不一致；");
                                        errtype = errtype + "分类号错误,";
                                    }
                                } else {
                                    errormessage.append("第" + row + "行，该文件所属案卷分类号为空；");
                                    checkResult.put("flh", "第" + row + "行，该文件所属案卷分类号为空；");
                                    errtype = errtype + "分类号错误,";
                                }
                                if (volFlM.get("flname") != null && !"".equals(volFlM.get("flname"))) {
                                    String volFlname = volFlM.get("flname").toString();
                                    if (!volFlname.equals((filedataM.get("flname")))) {
                                        errormessage.append("第" + row + "行，该文件分类名称与所属案卷不一致；");
                                        checkResult.put("flmc", "第" + row + "行，该文件分类名称与所属案卷不一致；");
                                        errtype = errtype + "分类名称错误,";
                                    }
                                } else {
                                    log.info("111111111111111111111111分类名称");
                                    errormessage.append("第" + row + "行，该文件所属案卷分类名称为空；");
                                    checkResult.put("flmc", "第" + row + "行，该文件所属案卷全分类名称为空；");
                                    errtype = errtype + "分类名称错误,";
                                }
                            } else {
                                errormessage.append("第" + row + "行，该文件所属案卷分类号为空；");
                                checkResult.put("flh", "第" + row + "行，该文件所属案卷分类号为空；");
                                errtype = errtype + "分类号错误,";
                                errormessage.append("第" + row + "行，该文件所属案卷分类名称为空；");
                                checkResult.put("flmc", "第" + row + "行，该文件所属案卷分类名称为空；");
                                errtype = errtype + "分类名称错误,";
                            }
                        }
                    }
                }
                    //检测二期进馆页数和画幅数，数字化说明
                    if(checkContentM.get("页数画幅数汇总统计")!=null && !"".equals(checkContentM.get("页数画幅数汇总统计"))) {
//                        String kv = kvm.get("c8").toString();
//                        String where = "pname = '"+kv;
//                        String configResult = rs.getConfig(where);
//                        boolean resultb = false;
//                        if ("0".equals(configResult)) {
//                            resultb = false;
//                        } else if (("1".equals(configResult))) {
//                            resultb = true;
//                        } else {
//                            resultb = true;
//                        }
//                        if (resultb) {
                            int pagenum = 0;
                            int hfs = 0;
                            boolean checkszh = true;
                            if ((filedataM.get("pagenum") != null && !"".equals(filedataM.get("pagenum"))) && (filedataM.get("hfs") != null && !"".equals(filedataM.get("hfs")))) {
                                pagenum = Integer.valueOf(filedataM.get("pagenum").toString());
                                hfs = Integer.valueOf(filedataM.get("hfs").toString());
                                //检测数字化情况
                                String szhsm = "";
                                String[] szhsms = new String[2];
                                if (filedataM.get("szhsm") != null && !"".equals(filedataM.get("szhsm"))) {
                                    szhsm = filedataM.get("szhsm").toString();
                                    boolean szhsmb = false;
                                    if (szhsm.contains(";")) {
                                        szhsms = szhsm.split(";");
                                        szhsmb = true;
                                    } else if (szhsm.contains("；")) {
                                        szhsms = szhsm.split("；");
                                        szhsmb = true;
                                    } else {
                                        errormessage.append("第" + row + "行，二期进馆文件数字化说明内容不是标准规范；");
                                        checkResult.put("szhsm", "第" + row + "行，二期进馆文件数字化说明内容不是标准规范；");
                                        errtype = errtype + "数字化说明错误,";
                                        checkszh = false;
                                    }
                                    if(szhsmb) {
                                        String szhsmsFirst = szhsms[0];
                                        szhsmsFirst = szhsmsFirst.substring(0, szhsmsFirst.lastIndexOf("_"));
                                        if (szhsmsFirst.startsWith("未扫_第")) {
                                            if (!szhsmsFirst.endsWith("页_")) {
                                                errormessage.append("第" + row + "行，二期进馆文件数字化说明内容不是标准规范；");
                                                checkResult.put("szhsm", "第" + row + "行，二期进馆文件数字化说明内容不是标准规范；");
                                                errtype = errtype + "数字化说明错误,";
                                                checkszh = false;
                                            }
                                        } else {
                                            errormessage.append("第" + row + "行，二期进馆文件数字化说明内容不是标准规范；");
                                            checkResult.put("szhsm", "第" + row + "行，二期进馆文件数字化说明内容不是标准规范；");
                                            errtype = errtype + "数字化说明错误,";
                                            checkszh = false;
                                        }
                                        String szhsmsEnd = szhsms[1];
                                        if (szhsmsEnd.startsWith("重页_第")) {
                                            if (!szhsmsEnd.endsWith("页")) {
                                                errormessage.append("第" + row + "行，二期进馆文件数字化说明内容不是标准规范；");
                                                checkResult.put("szhsm", "第" + row + "行，二期进馆文件数字化说明内容不是标准规范；");
                                                errtype = errtype + "数字化说明错误,";
                                                checkszh = false;
                                            }
                                        } else {
                                            errormessage.append("第" + row + "行，二期进馆文件数字化说明内容不是标准规范；");
                                            checkResult.put("szhsm", "第" + row + "行，二期进馆文件数字化说明内容不是标准规范；");
                                            errtype = errtype + "数字化说明错误,";
                                            checkszh = false;
                                        }
                                    }
                                } else {
//                                    errormessage.append("第" + row + "行，二期进馆文件数字化说明内容的值为空；");
//                                    checkResult.put("szhsm", "第" + row + "行，二期进馆文件数字化说明内容的值为空；");
//                                    errtype = errtype + "数字化说明错误,";
                                    checkszh = false;
                                }
                                if (pagenum != hfs) {
                                    if (!checkszh) {
                                        errormessage.append("第" + row + "行，二期进馆文件数字化说明内容不是标准规范或内容为空；");
                                        checkResult.put("szhsm", "第" + row + "行，二期进馆文件数字化说明内容不是标准规范或内容为空；");
                                        errtype = errtype + "数字化说明错误,";
                                    } else {
                                        String ws = szhsms[0];//未扫说明,重页部分需求不明确，未写逻辑
                                        String wsys = ws.substring(ws.indexOf("未扫_第") + 4, ws.indexOf("页_"));
                                        String[] wsyss = wsys.split("、");
                                        int wsnum = 0;
                                        for (int i1 = 0; i1 < wsyss.length; i1++) {
                                            if (i1 != wsyss.length - 1) {
                                                wsnum = wsnum + Integer.valueOf(wsyss[i1]);
                                            } else {
                                                String wspg = wsyss[i1];
                                                String[] wspgs = wspg.split("-");
                                                int wsb = Integer.valueOf(wspgs[0]);
                                                int wsa = Integer.valueOf(wspgs[1]);
                                                wsnum = wsnum + (wsa - wsb + 1);
                                            }
                                        }
                                        if (wsnum + hfs != pagenum) {
                                            errormessage.append("第" + row + "行，二期进馆文件数字化说明内容不准确；");
                                            checkResult.put("szhsm", "第" + row + "行，二期进馆文件数字化说明内容不准确；");
                                            errtype = errtype + "数字化说明错误,";
                                        }
                                    }
                                }
                            } else {
                                if (filedataM.get("pagenum") == null || "".equals(filedataM.get("pagenum"))) {
                                    errormessage.append("第" + row + "行，二期进馆文件页数的值为空；");
                                    checkResult.put("pagenum", "第" + row + "行，二期进馆文件页数的值为空；");
                                    errtype = errtype + "页数错误,";
                                }
                                if (filedataM.get("hfs") == null || "".equals(filedataM.get("hfs"))) {
                                    errormessage.append("第" + row + "行，二期进馆文件画幅数的值为空；");
                                    checkResult.put("hfs", "第" + row + "行，二期进馆文件画幅数的值为空；");
                                    errtype = errtype + "画幅数错误,";
                                }
                            }
//                        }
                    }
                    //卷内顺序号
                    if(checkContentM.get("完整性检查")!=null && !"".equals(checkContentM.get("完整性检查"))) {
                        String kv = kvm.get("c3").toString();
                        String where = "pname = '"+kv+"' and name = '卷内顺序号检查'";
                        String configResult = rs.getConfig(where);
                        boolean resultb = false;
                        if ("0".equals(configResult)) {
                            resultb = false;
                        } else if (("1".equals(configResult))) {
                            resultb = true;
                        } else {
                            resultb = true;
                        }
                        if(resultb) {
                            boolean jnsxhB = false;
                            if (filedataM.get("f6") != null && !"".equals(filedataM.get("f6")) && libname.contains("文书")) {
                                String jh =  filedataM.get("f6").toString();
                                if((libname.contains("名人") || libname.contains("音频") || libname.contains("视频")) && "eq".equals(archiveType)){
                                    String vollsh = keyword.substring(keyword.lastIndexOf("-")+1,keyword.length());
                                    if(vollsh.equals(jh)){
                                        int piorInt = Integer.valueOf(pior);
                                        int jhInt = Integer.valueOf(jh);
                                        if(jhInt==piorInt+1){
                                            pior = vollsh;
                                        }else{
                                            errormessage.append("第" + row + "行，二期进馆文件卷内顺序号与之对应的文件档号有跳号或重号；");
                                            checkResult.put("jh", "第" + row + "行，二期进馆文件卷内顺序号与之对应的文件档号有跳号或重号；");
                                            errtype = errtype + "二期进馆文件卷内顺序号与之对应的文件档号有跳号或重号,";
                                        }
                                    }else{
                                        errormessage.append("第" + row + "行，二期进馆文件卷内顺序号与文件档号不相等；");
                                        checkResult.put("jh", "第" + row + "行，二期进馆文件卷内顺序号与文件档号不相等；");
                                        errtype = errtype + "二期进馆文件卷内顺序号与文件档号不相等,";
                                    }
                                }else {
                                    if(jh.length()==3){
                                        String vollsh = keyword.substring(keyword.lastIndexOf("-")+1,keyword.length());
                                        if(vollsh.equals(jh)){
                                            int piorInt = Integer.valueOf(pior);
                                            int jhInt = Integer.valueOf(jh);
                                            if(jhInt==piorInt+1){
                                                pior = vollsh;
                                            }else{
                                                errormessage.append("第" + row + "行，二期进馆文件卷内顺序号与之对应的文件档号有跳号或重号；");
                                                checkResult.put("jh", "第" + row + "行，二期进馆文件卷内顺序号与之对应的文件档号有跳号或重号；");
                                                errtype = errtype + "二期进馆文件卷内顺序号与之对应的文件档号有跳号或重号,";
                                            }
                                        }else{
                                            errormessage.append("第" + row + "行，二期进馆文件卷内顺序号与文件档号不相等；");
                                            checkResult.put("jh", "第" + row + "行，二期进馆文件卷内顺序号与文件档号不相等；");
                                            errtype = errtype + "二期进馆文件卷内顺序号与文件档号不相等,";
                                        }
                                    }else {
                                        errormessage.append("第" + row + "行，二期进馆文件卷内顺序号不是三位；");
                                        checkResult.put("jh", "第" + row + "行，二期进馆文件卷内顺序号不是三位；");
                                        errtype = errtype + "二期进馆文件卷内顺序号不是三位,";
                                    }
                                }
                            }
                        }
                    }
                }
                //检测文件档号
//                if(checkContentM.get("keyword")!=null && !"".equals(checkContentM.get("keyword"))){
//                    String configResult = rs.getConfig("");
//                    boolean resultb = false;
//                    if ("0".equals(configResult)) {
//                        resultb = false;
//                    } else if (("1".equals(configResult))) {
//                        resultb = true;
//                    } else {
//                        resultb = true;
//                    }
//                    if(resultb) {
                        String result = bds.checkKeyword(row, keyword, "file", filedataM, excelAttr);
                        if (result != null && !"".equals(result)) {
                            //rs.addResultInValue(excelAttr,result);
                            errormessage.append(result);
                            checkResult.put("keywordresult", result);
                            errtype = errtype + "档号错误,";
                        }
//                    }
//                }
                //检测文件编号
//                if(checkContentM.get("wenhao")!=null && !"".equals(checkContentM.get("wenhao"))){
//                    String configResult = rs.getConfig("");
//                    boolean resultb = false;
//                    if ("0".equals(configResult)) {
//                        resultb = false;
//                    } else if (("1".equals(configResult))) {
//                        resultb = true;
//                    } else {
//                        resultb = true;
//                    }
//                    if(resultb) {
                        if (filedataM.get("wenhao") != null && !"".equals(filedataM.get("wenhao"))) {
                            String wenhao = filedataM.get("wenhao").toString();
                            if (wenhaos.size() == 0) {
                                wenhaos.add(wenhao);
                            } else {
                                int whsb = wenhaos.size();
                                wenhaos.add(wenhao);
                                int whsa = wenhaos.size();
                                if (whsb == whsa) {
                                    errormessage.append("第" + row + "行，该文件的文件编号已存在；");
                                    checkResult.put("wenhao", "第" + row + "行，该文件的文件编号已存在；");
                                    errtype = errtype + "文件编号错误,";
                                }
                            }
                        } else {
                            errormessage.append("第" + row + "行，文件编号的值为空；");
                            checkResult.put("wenhao", "第" + row + "行，文件编号的值为空；");
                            errtype = errtype + "文件编号错误,";
                        }
//                    }
//                }
                //检测题名
                if(checkContentM.get("真实性检查")!=null && !"".equals(checkContentM.get("真实性检查"))){
                    if (filedataM.get("title") != null && !"".equals(filedataM.get("title"))) {
                        String title = filedataM.get("title").toString();
                        checkResult.put("title", title);
                        String kv = kvm.get("c1").toString();
                        String where = "pname = '"+kv+"' and name = '题名中全部为英文字符检查 '";
                        String configResult = rs.getConfig(where);
                        boolean resultb = false;
                        if ("0".equals(configResult)) {
                            resultb = false;
                        } else if (("1".equals(configResult))) {
                            resultb = true;
                        } else {
                            resultb = true;
                        }
                        if(resultb) {
                            boolean titleb = bds.isContainChinese(title);
                            if (!titleb) {
                                errormessage.append("第" + row + "行，文件题名" + title + "没有中文；");
                                checkResult.put("titleResult", "第" + row + "行，文件题名" + title + "没有中文；");
                                errtype = errtype + "文件题名错误,";
                            }else{
                                String kv1 = kvm.get("c1").toString();
                                String where1 = "pname = '"+kv1+"' and name = '题名长度 '";
                                String configResult1 = rs.getConfig(where1);
                                boolean resultb1 = false;
                                if ("0".equals(configResult1)) {
                                    resultb1 = false;
                                } else if (("1".equals(configResult1))) {
                                    resultb1 = true;
                                } else {
                                    resultb1 = true;
                                }
                                if(resultb1) {
                                    int titleLength = title.length();
                                    if(titleLength<Integer.valueOf(configResult1)){
                                        errormessage.append("第" + row + "行，案卷题名" + title + "的长度小于规定长度"+configResult1+"；");
                                        checkResult.put("titleresult", "第" + row + "行，案卷题名" + title + "的长度小于规定长度"+configResult1+"；");
                                        errtype = errtype + "案卷题名错误,";
                                    }
                                }
                            }
                        }
                        String where1 = "pname = '"+kv+"' and name = '案卷题名与卷内题名是否重复检查 '";
                        String configResult1 = rs.getConfig(where1);
                        boolean resultb1 = false;
                        if ("0".equals(configResult1)) {
                            resultb1 = false;
                        } else if (("1".equals(configResult1))) {
                            resultb1 = true;
                        } else {
                            resultb1 = true;
                        }
                        if(resultb1) {
                            if (titleSet.size() == 0) {
                                titleSet.add(title);
                            } else {
                                int titleSetb = titleSet.size();
                                titleSet.add(title);
                                int titleSeta = titleSet.size();
                                if (titleSetb == titleSeta) {
                                    errormessage.append("第" + row + "行，该文件的文件题名已存在；");
                                    checkResult.put("titleResult", "第" + row + "行，该文件的文件题名已存在；");
                                    errtype = errtype + "文件题名错误,";
                                }
                            }
                        }
                    } else {
                        errormessage.append("第" + row + "行，文件题名的值为空；");
                        checkResult.put("titleResult", "第" + row + "行，文件题名的值为空；");
                        errtype = errtype + "文件题名错误,";
                    }
                }
                //检测责任者是否包含特殊符号
                if(checkContentM.get("真实性检查")!=null && !"".equals(checkContentM.get("真实性检查"))) {
                    String kv = kvm.get("c1").toString();
                    String where = "pname = '"+kv+"' and name = '责任者检查 '";
                    String configResult = rs.getConfig(where);
                    boolean resultb = false;
                    if ("0".equals(configResult)) {
                        resultb = false;
                    } else if (("1".equals(configResult))) {
                        resultb = true;
                    } else {
                        resultb = true;
                    }
                    if(resultb) {
                        if (filedataM.get("zrz") != null && !"".equals(filedataM.get("zrz"))) {
                            String zrz = filedataM.get("zrz").toString();
                            char isLegalChar = bds.validateLegalString(zrz,configResult);
                            if ("t".equals(isLegalChar)) {
                                errormessage.append("第" + row + "行，责任者" + zrz + "包含特殊符号；");
                                checkResult.put("zrz", "第" + row + "行，责任者" + zrz + "包含特殊符号；");
                                errtype = errtype + "责任者错误,";
                            }
                        } else {
                            errormessage.append("第" + row + "行，责任者的值为空；");
                            checkResult.put("zrz", "第" + row + "行，责任者的值为空；");
                            errtype = errtype + "责任者错误,";
                        }
                    }
                }
                //文件日期检查
                if(checkContentM.get("真实性检查")!=null && !"".equals(checkContentM.get("真实性检查"))) {
                    String kv = kvm.get("c1").toString();
                    String where = "pname = '"+kv+"' and name = '日期格式检查 '";
                    String configResult = rs.getConfig(where);
                    boolean resultb = false;
                    if ("0".equals(configResult)) {
                        resultb = false;
                    } else if (("1".equals(configResult))) {
                        resultb = true;
                    } else {
                        resultb = true;
                    }
                    if(resultb) {
                        if (filedataM.get("rq") != null && !"".equals(filedataM.get("rq"))) {
                            String rq = filedataM.get("rq").toString();
                            if (rq.length() == 6) {
                                boolean rqResult = bds.isInteger(rq);
                                if(!rqResult){
                                    errormessage.append("第" + row + "行，日期6位但日期内容不正确；");
                                    checkResult.put("rq", "第" + row + "行，日期6位但日期内容不正确；");
                                    errtype = errtype + "日期6位但日期内容不正确,";
                                }
                            } else if (rq.length() == 8) {
                                boolean rqResult = bds.isInteger(rq);
                                if(!rqResult){
                                    errormessage.append("第" + row + "行，日期8位但日期内容不正确；");
                                    checkResult.put("rq", "第" + row + "行，日期8位但日期内容不正确；");
                                    errtype = errtype + "日期8位但日期内容不正确,";
                                }
                            } else if (rq.length() == 10) {
                                if(!rq.contains("-")){
                                    errormessage.append("第" + row + "行，日期10位但不包含“-”；");
                                    checkResult.put("rq", "第" + row + "行，日期10位但不包含“-”；");
                                    errtype = errtype + "日期10位但不包含“-”,";
                                }else{
                                    String[] rqs = rq.split("-");
                                    if(rqs.length!=3){
                                        errormessage.append("第" + row + "行，日期10位但格式不正确；");
                                        checkResult.put("rq", "第" + row + "行，日期10位但格式不正确；");
                                        errtype = errtype + "日期10位但格式不正确,";
                                    }
                                }
                            }else{
                                errormessage.append("第" + row + "行，日期格式不正确；");
                                checkResult.put("rq", "第" + row + "行，日期格式不正确；");
                                errtype = errtype + "日期格式不正确,";
                            }
                        } else {
                            errormessage.append("第" + row + "行，日期的值为空；");
                            checkResult.put("rq", "第" + row + "行，日期的值为空；");
                            errtype = errtype + "日期错误,";
                        }
                    }
                }
                //检测密级和保密期限
                if(checkContentM.get("真实性检查")!=null && !"".equals(checkContentM.get("真实性检查"))) {
                    String kv = kvm.get("c1").toString();
                    String where = "pname = '"+kv+"' and name = '案卷目录密级为对应卷内目录最高密级  '";
                    String configResult = rs.getConfig(where);
                    boolean resultb = false;
                    if ("0".equals(configResult)) {
                        resultb = false;
                    } else if (("1".equals(configResult))) {
                        resultb = true;
                    } else {
                        resultb = true;
                    }
                    if(resultb) {
                            String mj = "";
                            if(filedataM.get("mj")!=null && !"".equals(filedataM.get("mj"))){
                                mj = filedataM.get("mj").toString();
                            }
                            if("绝密".equals(mj) || "机密".equals(mj) || "秘密".equals(mj) || "".equals(mj)){
                                List mjList = bds.getParamMJ("");
                                String mjStr = StringUtils.join(mjList.toArray(), ",");
                                if (!mjStr.contains(mj)) {
                                    errormessage.append("第" + row + "行，该文件密级与档案系统密级不匹配；");
                                    checkResult.put("mj", "第" + row + "行，该文件密级与档案系统密级不匹配；");
                                    errtype = errtype + "密级错误,";
                                }
                            }else{
                                errormessage.append("第" + row + "行，密集中只能出现“绝密”、“机密”、“秘密”或空白；");
                                checkResult.put("mj", "第" + row + "行，案卷的卷内文件均无密级，或密级与档案系统密级不匹配；");
                                errtype = errtype + "密级错误,";
                            }
                            if (checkContentM.get("bmqx") != null && !"".equals(checkContentM.get("bmqx"))) {
                                String configResult1 = rs.getConfig("");
                                boolean resultb1 = false;
                                if ("0".equals(configResult)) {
                                    resultb1 = false;
                                } else if (("1".equals(configResult))) {
                                    resultb1 = true;
                                } else {
                                    resultb1 = true;
                                }
                                if(resultb1) {
                                    if (filedataM.get("bmqx") != null && !"".equals(filedataM.get("bmqx"))) {
                                        String bmqx = filedataM.get("bmqx").toString();
                                        if(mj!=null && !"".equals(mj)){
                                            if (bmqx == null || "".equals(bmqx)) {
                                                errormessage.append("第" + row + "行，该文件无保密期限的值；");
                                                checkResult.put("bmqx", "第" + row + "行，该文件无保密期限的值；");
                                                errtype = errtype + "保密期限错误,";
                                            }
                                        }
                                    }
                                }
                            }
                    }
                }
                //检测页数，页号
                if(checkContentM.get("页数页号检查")!=null && !"".equals(checkContentM.get("页数页号检查"))) {
                    if("vf".equals(arctype)) {
                        String volKey = "";
                        String volKeyword = keyword.substring(0, keyword.lastIndexOf("-"));
                        String tablename = archiveType + "filedata";
                        String whereStr = "keyword like '" + volKeyword + "%' ORDER BY REVERSE(LEFT(REVERSE(keyword),INSTR(REVERSE(keyword),'-')-1))";
                        List<Map> yhL = rs.getData("*", tablename, whereStr);
                        String jha = keyword.substring(keyword.lastIndexOf("-") + 1, keyword.length());
                        if (filedataM.get("pagenum") != null && !"".equals(filedataM.get("pagenum"))) {
                            int pagenum = Integer.valueOf(filedataM.get("pagenum").toString());
                            if (filedataM.get("yh") != null && !"".equals(filedataM.get("yh"))) {
                                yh = filedataM.get("yh").toString();
                                if (ys == 0) {
                                    //第一个文件
                                    if (!"1".equals(yh)) {
                                        errormessage.append("第" + row + "行，该文件为第一个文件页号的值不是1；");
                                        checkResult.put("yh", "第" + row + "行，该文件为第一个文件页号的值不是1；");
                                        errtype = errtype + "页号错误,";
                                        ys = pagenum;
                                        yhs = Integer.valueOf(yh);
                                    } else {
                                        ys = pagenum;
                                        yhs = Integer.valueOf(yh);
                                    }
                                } else if (yhL.size() == Integer.valueOf(jha)) {
                                    //该卷最后一件
                                    int yhys = 0;
                                    if (!yh.contains("-")) {
                                        errormessage.append("第" + row + "行，该文件是卷内最后一个但页号中不包含“-”；");
                                        checkResult.put("yh", "第" + row + "行，该文件是卷内最后一个但页号中不包含“-”；");
                                        errtype = errtype + "页号错误,";
                                    } else {
                                        int yhAddys = Integer.valueOf(yhs) + ys;
                                        String[] yhStr = yh.split("-");
                                        yhys = Integer.valueOf(yhStr[0]) + pagenum;
                                        if (yhAddys == Integer.valueOf(yhStr[0]) && yhys - 1 == Integer.valueOf(yhStr[1])) {
                                            log.info("页号正常");
                                        } else {
                                            errormessage.append("第" + row + "行，该文件页次（号）不等于上一件的页数加页次（号）；");
                                            checkResult.put("yh", "第" + row + "行，该文件页次（号）不等于上一件的页数加页次（号）；");
                                            errtype = errtype + "页号错误,";
                                        }
                                    }
                                } else {
                                    int yhAddys = Integer.valueOf(yhs) + ys;
                                    if (yhAddys != Integer.valueOf(yh)) {
                                        errormessage.append("第" + row + "行，该文件页次（号）不等于上一件的页数加页次（号）；");
                                        checkResult.put("yh", "第" + row + "行，该文件页次（号）不等于上一件的页数加页次（号）；");
                                        errtype = errtype + "页号错误,";
                                        yhs = Integer.valueOf(yh);
                                        ys = pagenum;
                                    } else {
                                        yhs = Integer.valueOf(yh);
                                        ys = pagenum;
                                    }
                                }
//                            String fileLsh = keyword.substring(keyword.lastIndexOf("-") + 1, keyword.length());
                            } else {
                                errormessage.append("第" + row + "行，该文件无页号的值；");
                                checkResult.put("yh", "第" + row + "行，该文件无页号的值；");
                                errtype = errtype + "页号错误,";
                            }
                            if (volKey == null || "".equals(volKey)) {
                                //excel中第一个文件
                                volKey = volKeyword;
                            } else {
                                if (volKey.equals(volKeyword)) {
                                    //该案卷卷内文件未检测完继续监测
                                } else {
                                    //下一个案卷
                                    ys = 0;
                                    yhs = 0;
                                    yh = "";
                                }
                            }
                        } else {
                            errormessage.append("第" + row + "行，该文件无文件页数的值；");
                            checkResult.put("pagenum", "第" + row + "行，该文件无文件页数的值；");
                            errtype = errtype + "页数错误,";
                        }
                    }
                }
                if(errtype!=null && !"".equals(errtype)){
                    errtype = errtype.substring(0,errtype.length()-1);
                    checkResult.put("errortype",errtype);
                }
                checkResult.put("errormessage",errormessage);
                checkResult.put("dataly","excel");
                if((errtype!=null && !"".equals(errtype)) && ((errormessage!=null && !"".equals(errormessage)))){
                    rs.addResultInValue(checkResult);
                    checkResult = new HashMap();
                }
            }
        }catch (Exception e){
            log.info("ERROE:"+e.getMessage());
            checkFileDataResult = false;
        }
        return checkFileDataResult;
    }

    public boolean checkEfiledata(Map excelAttr){
        boolean checkEfileDataResult = true;
        Map<String,Object> checkResult = new HashMap();
        String efilepath = excelAttr.get("efilepath").toString();
        String archiveType = excelAttr.get("archiveType").toString();
        String libcode = excelAttr.get("libcode").toString();
        String version ="";
        if(excelAttr.get("version")!=null && !"".equals(excelAttr.get("version"))) {
            version = excelAttr.get("version").toString();
        }
        String arctype = "";
        String libname = "";
        if(libcode!=null && !"".equals(libcode)){
            String whereLib = "libcode = '" + libcode + "'";
            List arctypeL = rs.getData("arctype,chname", "s_arc", whereLib);
            if (arctypeL.size() > 0) {
                Map arctypeM = (Map) arctypeL.get(0);
                if (arctypeM.get("arctype") != null && !"".equals(arctypeM.get("arctype"))) {
                    arctype = arctypeM.get("arctype").toString();
                    libname = arctypeM.get("chname").toString();
                }
            }
        }
        checkResult.put("libcode",libcode);
        checkResult.put("archiveType",archiveType);
        checkResult.put("arclvl","efile");
        checkResult.put("checkArclvl",arctype);
        checkResult.put("libname",libname);
        //电子文件
        StringBuffer errormessage = new StringBuffer();
        try{
            File efileP = new File(efilepath);
            if(efileP.exists()){
                boolean result = this.getEfile(efileP,checkResult);
            }else{
                log.info("ERROE:电子文件路跟目录不存在，请检查");
                checkEfileDataResult = false;
            }
        }catch (Exception e){
            log.info("ERROE:"+e.getMessage());
            checkEfileDataResult = false;
        }
        return checkEfileDataResult;
    }

    public boolean getEfile(File efileP,Map<String,Object> checkResult){
        boolean checkEfile = true;
        try{
            String archiveType = "";
            String libcode = "";
            if(checkResult.get("archiveType")!=null && !"".equals(checkResult.get("archiveType"))){
                archiveType = checkResult.get("archiveType").toString();
            }
            if(checkResult.get("libcode")!=null && !"".equals(checkResult.get("libcode"))){
                libcode = checkResult.get("libcode").toString();
            }
            List efilegs = new ArrayList();
            String pathName = "";
            String nextMenu ="";
            File[] files = efileP.listFiles();
            String keyword ="";
            for(int i=0;i<files.length;i++){
                int dzwjgs = files.length;
                if(!files[i].isDirectory()){
                    nextMenu = files[i].getName();
                    if(!nextMenu.contains("Thumbs.db")) {
                        keyword = nextMenu.substring(0, nextMenu.lastIndexOf("-"));
                        log.info("keyword:"+keyword);
                        log.info("gs"+files.length);
                    }else {
                        dzwjgs = dzwjgs-1;
                    }
                }
                if(files[i].isDirectory()){
                    log.info("有下一层文件夹");
                    nextMenu = files[i].getName();
                    String tifPath =files[i].toString();
                    File tifFile = new File(tifPath);
                    if(tifFile.exists()){
                        pathName=files[i].toString();
                        File tifPathf = new File(pathName);
                        this.getEfile(tifPathf,checkResult);
                    }
                }else {
                    log.info("keyword:"+keyword);
                    String name = files[i].getName();
                    if(!name.contains("Thumbs.db")) {
                        log.info(name);
                        String dh = name.substring(0,name.lastIndexOf("-"));
                        String tifPath =files[i].toString();
                        if(keyword.equals(dh)){
                            efilegs.add(tifPath);
                        }
                        if(dzwjgs==efilegs.size()){
                            log.info("文件的附件已集齐");
                            this.checkEfile(keyword,efilegs,checkResult);
                            for(int m=0;m<efilegs.size();m++){
                                String p = efilegs.get(m).toString();
                                Map imageMap = gea.getImage(p);
                                String cc = "";//计算尺寸
                                if ((imageMap.get("width") != null && !"".equals(imageMap.get("width"))) && (imageMap.get("height") != null && !"".equals(imageMap.get("height")))) {
                                    int width = Integer.valueOf(imageMap.get("width").toString());
                                    int height = Integer.valueOf(imageMap.get("height").toString());
                                    if(width<=210 && height<=297){
                                        cc = "A4及以下";
                                    } else if (width>=297 && height>=420){
                                        cc = "A3及以上";
                                    }else {
                                        cc = "A4至A3";
                                    }
                                }
                                String ext = p.substring(p.lastIndexOf("."),p.length());
                                Map statMap = new HashMap();
                                String resultTablename = "s_statistics";
                                statMap.put("tablename",resultTablename);
                                statMap.put("libcode",libcode);
                                statMap.put("arcLvl","efile");
                                statMap.put("mj","");
                                statMap.put("js",0);
                                statMap.put("keyword",keyword);
                                statMap.put("ys",0);
                                statMap.put("hfs","");
                                statMap.put("ext",ext);
                                statMap.put("archiveType",archiveType);
                                statMap.put("cc",cc);
                                ss.addDataStat(statMap);
                            }
                        }else{
                            log.info(tifPath);
                        }
                    }else{
//                    log.info(name);
//                    String dh = name.substring(0,name.lastIndexOf("-"));
                        String tifPath =files[i].toString();
//                    if(keyword.equals(dh)){
//                        efilegs.add(tifPath);
//                    }
                        if(dzwjgs==efilegs.size()){
                            log.info("文件的附件已集齐");
                            this.checkEfile(keyword,efilegs,checkResult);
                            for (int m = 0; m < efilegs.size(); m++) {
                                String p = efilegs.get(m).toString();
                                Map imageMap = gea.getImage(p);
                                String cc = "";//计算尺寸
                                if ((imageMap.get("width") != null && !"".equals(imageMap.get("width"))) && (imageMap.get("height") != null && !"".equals(imageMap.get("height")))) {
                                    int width = Integer.valueOf(imageMap.get("width").toString());
                                    int height = Integer.valueOf(imageMap.get("height").toString());
                                    if (width <= 210 && height <= 297) {
                                        cc = "A4及以下";
                                    } else if (width >= 297 && height >= 420) {
                                        cc = "A3及以上";
                                    } else {
                                        cc = "A4至A3";
                                    }
                                }
                                String ext = p.substring(p.lastIndexOf("."), p.length());
                                Map statMap = new HashMap();
                                String resultTablename = "s_statistics";
                                statMap.put("tablename", resultTablename);
                                statMap.put("libcode", libcode);
                                statMap.put("arcLvl", "efile");
                                statMap.put("mj", "");
                                statMap.put("js", 0);
                                statMap.put("keyword", keyword);
                                statMap.put("ys", 0);
                                statMap.put("hfs", "");
                                statMap.put("ext", ext);
                                statMap.put("archiveType", archiveType);
                                statMap.put("cc", cc);
                                ss.addDataStat(statMap);
                            }
                        }else{
                            log.info(tifPath);
                        }
                    }
                }
            }
        }catch (Exception e){
            log.info(e.getMessage());
            checkEfile = false;
            return checkEfile;
        }
        return checkEfile;
    }

    public boolean checkEfile(String keyword,List fileList,Map<String,Object> checkResult) throws Exception{
        boolean checkEfileResult = true;
//        try {
            String errtype = "";
            Long efilesize = 0L;
            StringBuffer errormessage = new StringBuffer();
            List<Map> filedateL = new ArrayList<Map>();
            List<Map> voldateL = new ArrayList<Map>();
            List<Map> fileListdateL = new ArrayList<Map>();
            String archiveType = checkResult.get("archiveType").toString();
            String libcode = checkResult.get("libcode").toString();
            String checkArclvl = checkResult.get("checkArclvl").toString();
            String libname = checkResult.get("libname").toString();
            String field = "title,keyword,unitsys,hfs,efilegs,nd,jh,f6,pagenum,efilesize,sc,spml,btl,cyl,xs,fbl,efileys,dah,ajh";
            String vfield = "keyword";
            String flfield = "keyword";
            if (checkArclvl.contains("vf")) {//vol-file
            String voltablename = archiveType+"voldata";
            String volkeyword = keyword.substring(0,keyword.lastIndexOf("-"));
            String volWhereStr = "keyword like '"+volkeyword+"%' ORDER BY REVERSE(LEFT(REVERSE(keyword),INSTR(REVERSE(keyword),'-')-1))";
            voldateL = rs.getData(vfield,voltablename,volWhereStr);
                String filetablename = archiveType + "filedata";
                String fileWhereStr = "keyword = '" + keyword + "' ORDER BY REVERSE(LEFT(REVERSE(keyword),INSTR(REVERSE(keyword),'-')-1))";
                String fileLisrWhereStr = "keyword like '" + volkeyword + "%' ORDER BY REVERSE(LEFT(REVERSE(keyword),INSTR(REVERSE(keyword),'-')-1))";
                filedateL = rs.getData(field, filetablename, fileWhereStr);
                fileListdateL = rs.getData(flfield, filetablename, fileLisrWhereStr);
            } else {//file
                String filetablename = archiveType + "filedata";
                String fileWhereStr = "keyword = '" + keyword + "' ORDER BY REVERSE(LEFT(REVERSE(keyword),INSTR(REVERSE(keyword),'-')-1))";
                filedateL = rs.getData(field, filetablename, fileWhereStr);
            }
            int pagenum = 0;
            Map filedateM = filedateL.get(0);
            if (filedateL.size() > 0) {
                Map kvm = getCheckType();
                String kv = kvm.get("c9").toString();
                Map configResultM = rs.getConfigEfile(kv, "");
                boolean isbkb = false;
            for (int i = 0; i < fileList.size(); i++) {
                checkResult.put("libcode",libcode);
                checkResult.put("archiveType",archiveType);
                checkResult.put("arclvl","efile");
                checkResult.put("checkArclvl",checkArclvl);
                checkResult.put("libname",libname);
                String efilePath = fileList.get(i).toString();
                String ep = efilePath.replaceAll("\\\\","/");
                checkResult.put("efilepath", ep);
                File efile = new File(efilePath);
                if (!efile.exists()) {
                    errormessage.append(efilePath + "路径下找不到文件；");
                    checkResult.put("efilepathResult", efilePath + "路径下找不到文件；");
                    errtype = errtype + "找不到文件,";
                } else {
                    //检测文件路径格式
                    if (configResultM.get("存储结构检查") != null && !"".equals(configResultM.get("存储结构检查"))) {
                        String checkP = this.checkPath(efilePath, libcode, archiveType, filedateM, checkArclvl, keyword);
                        if (checkP != null && !"".equals(checkP)) {
                            errormessage.append(checkP);
                            checkResult.put("efilepathResult", checkP);
                            errtype = errtype + "路径格式不正确,";
                        }
                    }
                    //检测文件名称格式
                    String efilename = efilePath.substring(efilePath.lastIndexOf("\\")+1, efilePath.length());
                    checkResult.put("efilename", efilename);
                    if (configResultM.get("电子文件命名规则检查") != null && !"".equals(configResultM.get("电子文件命名规则检查"))) {
                        String checkN = this.checkName(efilename, efilePath, i);
                        if (checkN != null && !"".equals(checkN)) {
                            errormessage.append(checkN);
                            checkResult.put("efilenameResult", checkN);
                            errtype = errtype + "电子文件名称不正确,";
                        }
                    }
                    String ext = efilename.substring(efilename.lastIndexOf("."), efilename.length());
                    if (efile.length() == 0) {
                        errormessage.append("该电子文件大小为0KB；");
                        checkResult.put("efilesize", "该电子文件大小为0KB；");
                        errtype = errtype + "电子文件大小为0KB,";
                    } else {
                        efilesize = efilesize + efile.length();
                        String targetPath = efilePath.substring(0, efilePath.lastIndexOf(".")+1) + "pdf";
                        if (configResultM.get("文档正确格式") != null && !"".equals(configResultM.get("文档正确格式"))) {
                            String office = configResultM.get("文档正确格式").toString();
                            if(office.contains(ext)){
                                boolean transferResult = false;
                                if ("doc".equals(ext) || "docx".equals(ext) || ".doc".equals(ext) || ".docx".equals(ext)) {//office文档类型
                                    transferResult = gps.officeChangepdf(efilePath, targetPath);
                                }
                                if("xls".equals(ext) || "xlsx".equals(ext) || ".xls".equals(ext) || ".xlsx".equals(ext)){
                                    transferResult = gps.excel2PDF(efilePath, targetPath);
                                }
                                if("ppt".equals(ext) || "pptx".equals(ext) || ".ppt".equals(ext) || ".pptx".equals(ext)){
                                    transferResult = gps.ppt2PDF(efilePath, targetPath);
                                }
                                if (transferResult) {
                                    pagenum = pagenum + gps.getPageNum(targetPath);
                                    File delFile = new File(targetPath);
                                    if(delFile.exists()){
                                        delFile.delete();
                                    }
                                } else {
                                    errormessage.append("该电子文件打不开或加密；");
                                    checkResult.put("wjzt", "该电子文件打不开或加密；");
                                    errtype = errtype + "该电子文件打不开或加密,";
                                }
                                if (filedateM.get("efilegs") != null && !"".equals(filedateM.get("efilegs"))) {
                                    String efilegs = filedateM.get("efilegs").toString();
                                    if (!efilegs.contains(ext)) {
                                        errormessage.append("该电子文件文件格式不正确；");
                                        checkResult.put("efilegs", "该电子文件文件格式不正确；");
                                        errtype = errtype + "该电子文件文件格式不正确,";
                                    }
                                }else {
                                    errormessage.append("该电子文件所属的文件中格式字段为空；");
                                    checkResult.put("efilegs", "该电子文件所属的文件中格式字段为空；");
                                    errtype = errtype + "该电子文件所属的文件中格式字段为空,";
                                }
                            }
                        }
                        if (configResultM.get("视频正确格式") != null && !"".equals(configResultM.get("视频正确格式"))) {
                            String video = configResultM.get("视频正确格式").toString();
                            if (video.contains(ext)) {//视频类型
                                if (configResultM.get("电子文件属性统计") != null && !"".equals(configResultM.get("电子文件属性统计"))) {
                                    Map videoMap = gea.getVideo(efilePath);
                                    if (filedateM.get("efilegs") != null && !"".equals(filedateM.get("efilegs"))) {
                                        String efilegs = filedateM.get("efilegs").toString();
                                        String gs = videoMap.get("efilegs").toString();
                                        if (!gs.equals(efilegs)) {
                                            errormessage.append("该视频格式不正确；");
                                            checkResult.put("efilegs", "该视频格式不正确；");
                                            errtype = errtype + "该视频格式不正确,";
                                        }
                                    }else {
                                        errormessage.append("该视频所属的文件中格式字段为空；");
                                        checkResult.put("efilegs", "该视频所属的文件中格式字段为空；");
                                        errtype = errtype + "该视频所属的文件中格式字段为空,";
                                    }
                                    if (filedateM.get("sc") != null && !"".equals(filedateM.get("sc"))) {
                                        Long sc = Long.valueOf(filedateM.get("sc").toString());
                                        Long ls = Long.valueOf(videoMap.get("sc").toString());
                                        if (ls != sc) {
                                            errormessage.append("该视频文件时长不正确；");
                                            checkResult.put("sc", "该视频文件时长不正确；");
                                            errtype = errtype + "该视频文件时长不正确,";
                                        }
                                    }else {
                                        errormessage.append("该视频所属的文件中时长字段为空；");
                                        checkResult.put("sc", "该视频所属的文件中时长字段为空；");
                                        errtype = errtype + "该视频所属的文件中时长字段为空,";
                                    }
                                    if (filedateM.get("spml") != null && !"".equals(filedateM.get("spml"))) {
                                        String spml = filedateM.get("spml").toString();
                                        String spmRate = videoMap.get("spml").toString();
                                        if (!spmRate.equals(spml)) {
                                            errormessage.append("该视频的视频码率不正确；");
                                            checkResult.put("spml", "该视频的视频码率不正确；");
                                            errtype = errtype + "该视频的视频码率不正确,";
                                        }
                                    }else {
                                        errormessage.append("该视频所属的文件中视频码率字段为空；");
                                        checkResult.put("spml", "该视频所属的文件中视频码率字段为空；");
                                        errtype = errtype + "该视频所属的文件中视频码率字段为空,";
                                    }
                                }
                            }
                        }
                        if (configResultM.get("音频正确格式") != null && !"".equals(configResultM.get("音频正确格式"))) {
                            String audio = configResultM.get("音频正确格式").toString();
                            if (audio.contains(ext)) {//音频类型
                                if (configResultM.get("电子文件属性统计") != null && !"".equals(configResultM.get("电子文件属性统计"))) {
                                    Map audioMap = gea.getAudio(efilePath);
                                    if (filedateM.get("efilegs") != null && !"".equals(filedateM.get("efilegs"))) {
                                        String efilegs = filedateM.get("efilegs").toString();
                                        String gs = audioMap.get("efilegs").toString();
                                        if (!gs.equals(efilegs)) {
                                            errormessage.append("该音频格式不正确；");
                                            checkResult.put("efilegs", "该音频格式不正确；");
                                            errtype = errtype + "该音频格式不正确,";
                                        }
                                    }else {
                                        errormessage.append("该音频所属的文件中格式字段为空；");
                                        checkResult.put("efilegs", "该音频所属的文件中格式字段为空；");
                                        errtype = errtype + "该音频所属的文件中格式字段为空,";
                                    }
                                    if (filedateM.get("sc") != null && !"".equals(filedateM.get("sc"))) {
                                        Long sc = Long.valueOf(filedateM.get("sc").toString());
                                        Long ls = Long.valueOf(audioMap.get("sc").toString());
                                        if (ls != sc) {
                                            errormessage.append("该音频文件时长不正确；");
                                            checkResult.put("sc", "该音频文件时长不正确；");
                                            errtype = errtype + "该音频文件时长不正确,";
                                        }
                                    }else {
                                        errormessage.append("该音频所属的文件中时长字段为空；");
                                        checkResult.put("sc", "该音频所属的文件中时长字段为空；");
                                        errtype = errtype + "该音频所属的文件中时长字段为空,；";
                                    }
                                    if (filedateM.get("btl") != null && !"".equals(filedateM.get("btl"))) {
                                        String btl = filedateM.get("btl").toString();
                                        String byteRate = audioMap.get("btl").toString();
                                        if (!byteRate.equals(btl)) {
                                            errormessage.append("该音频的比特率不正确；");
                                            checkResult.put("btl", "该音频的比特率不正确；");
                                            errtype = errtype + "该音频的比特率不正确,";
                                        }
                                    }else {
                                        errormessage.append("该音频所属的文件中比特率字段为空；");
                                        checkResult.put("btl", "该音频所属的文件中比特率字段为空；");
                                        errtype = errtype + "该音频所属的文件中比特率字段为空,";
                                    }
                                    if (filedateM.get("cyl") != null && !"".equals(filedateM.get("cyl"))) {
                                        String cyl = filedateM.get("cyl").toString();
                                        String cyRate = audioMap.get("cyl").toString();
                                        if (!cyRate.equals(cyl)) {
                                            errormessage.append("该音频的采样率不正确；");
                                            checkResult.put("cyl", "该音频的采样率不正确；");
                                            errtype = errtype + "该音频的采样率不正确,";
                                        }
                                    }else {
                                        errormessage.append("该音频所属的文件中采样率字段为空；");
                                        checkResult.put("cyl", "该音频所属的文件中采样率字段为空；");
                                        errtype = errtype + "该音频所属的文件中采样率字段为空,";
                                    }
                                }
                            }
                        }
                        if (configResultM.get("图像正确格式") != null && !"".equals(configResultM.get(""))) {
                            String image = configResultM.get("图像正确格式").toString();
                            if (image.contains(ext)) {//照片类型
                                if (configResultM.get("电子文件属性统计") != null && !"".equals(configResultM.get("电子文件属性统计"))) {
                                    Map imageMap = gea.getImage(efilePath);
                                    if (filedateM.get("efilegs") != null && !"".equals(filedateM.get("efilegs"))) {
                                        String efilegs = filedateM.get("efilegs").toString();
                                        String gs = imageMap.get("efilegs").toString();
                                        if (!gs.equals(efilegs)) {
                                            errormessage.append("该图像格式不正确；");
                                            checkResult.put("efilegs", "该图像格式不正确；");
                                            errtype = errtype + "该图像格式不正确,";
                                        }
                                    }else {
                                        errormessage.append("该图像所属的文件中电子文件格式字段为空；");
                                        checkResult.put("efilegs", "该图像所属的文件中电子文件格式字段为空；");
                                        errtype = errtype + "该图像所属的文件中电子文件格式字段为空,";
                                    }
                                    if (filedateM.get("fbl") != null && !"".equals(filedateM.get("fbl"))) {
                                        String fbl = filedateM.get("fbl").toString();
                                        String fbRate = imageMap.get("fbl").toString();
                                        if (fbRate.equals(fbl)) {
                                            errormessage.append("该图像分辨率不正确；");
                                            checkResult.put("fbl", "该图像分辨率不正确；");
                                            errtype = errtype + "该图像分辨率不正确,";
                                        }
                                    }else {
                                        errormessage.append("该图像所属的文件中分辨率字段为空；");
                                        checkResult.put("fbl", "该图像所属的文件中分辨率字段为空；");
                                        errtype = errtype + "该图像所属的文件中分辨率字段为空,";
                                    }
                                    if (filedateM.get("xs") != null && !"".equals(filedateM.get("xs"))) {
                                        String xs = filedateM.get("xs").toString();
                                        String xiangsu = imageMap.get("xs").toString();
                                        if (!xiangsu.equals(xs)) {
                                            errormessage.append("该图像的像素不正确；");
                                            checkResult.put("xs", "该图像的像素不正确；");
                                            errtype = errtype + "该图像的像素不正确,";
                                        }
                                    }else {
                                        errormessage.append("该图像所属的文件中像素字段为空；");
                                        checkResult.put("xs", "该图像所属的文件中像素字段为空；");
                                        errtype = errtype + "该图像所属的文件中像素字段为空,";
                                    }
                                }
                                String bkb = efilePath.substring(efilePath.lastIndexOf("-")+1,efilePath.lastIndexOf("."));
                                if("000".equals(bkb) || "111".equals(bkb)){
                                    isbkb = true;
                                }
                            }
                        }else {
                            errormessage.append("电子文件类型不是检测类型；");
                            checkResult.put("efilegs", "电子文件类型不是检测类型；");
                            errtype = errtype + "电子文件类型不是检测类型,";
                        }
                    }
                }
                if (errtype != null && !"".equals(errtype)) {
                    errtype = errtype.substring(0, errtype.length() - 1);
                    checkResult.put("errortype", errtype);
                }
                checkResult.put("errormessage", errormessage);
                if(filedateM.get("title")!=null && !"".equals(filedateM.get("title"))){
                    checkResult.put("title", filedateM.get("title").toString());
                }
                if(filedateM.get("keyword")!=null && !"".equals(filedateM.get("keyword"))){
                    checkResult.put("keyword", keyword);
                }
                checkResult.put("errormessage", errormessage);
                checkResult.put("dataly","excel");
                if((errtype!=null && !"".equals(errtype)) && ((errormessage!=null && !"".equals(errormessage)))){
                    boolean addResult = rs.addResultInValue(checkResult);
                    if (!addResult) {
                        checkEfileResult = false;
                    }
                    checkResult = new HashMap();
                }
            }
            StringBuffer updmessage = new StringBuffer();
            Map<String, Object> updateResult = new HashMap();
            updateResult.put("libcode", libcode);
            updateResult.put("archiveType", archiveType);
            updateResult.put("arclvl", "file");
//            updateResult.put("checkArclvl", checkArclvl);
            updateResult.put("unitsys", "0001");
            boolean upd = false;
            if (configResultM.get("数量一致性检查") != null && !"".equals(configResultM.get("数量一致性检查"))) {
                if (filedateM.get("efilesize") != null && !"".equals(filedateM.get("efilesize"))) {
                    Long size = Long.valueOf(filedateM.get("efilesize").toString());
                    if (efilesize != size) {
                        updmessage.append("该文件的电子文件大小不正确");
                        updateResult.put("efilesize", "该文件的电子文件大小不正确");
                        errtype = errtype + "该文件的电子文件大小不正确,";
                        upd = true;
                    }
                }
            }
            if (filedateL.size() > 0) {
                if (configResultM.get("数量一致性检查") != null && !"".equals(configResultM.get("数量一致性检查"))) {
                    if (filedateM.get("pagenum") != null && !"".equals(filedateM.get("pagenum"))) {
                        int ys = Integer.valueOf(filedateM.get("pagenum").toString());
                        if (ys != pagenum) {
                            updmessage.append("该电子文件页数之和不等于文件元数据页数");
                            updateResult.put("pagenum", "该电子文件页数之和不等于文件元数据页数");
                            errtype = errtype + "电子文件页数错误,";
                            upd = true;
                        }
                        if ("".equals(libcode) && "".equals(archiveType)) {
                            Long efiledx = Long.valueOf(filedateM.get("efilesize").toString());
                            if (efiledx != efilesize) {
                                updmessage.append("该文件的电子文件大小不正确");
                                updateResult.put("efilesize", "该文件的电子文件大小不正确");
                                errtype = errtype + "该文件的电子文件大小不正确,";
                                upd = true;
                            }
                        }
                    }
                }
                if (configResultM.get("数量一致性检查") != null && !"".equals(configResultM.get("数量一致性检查"))) {
                    if (filedateM.get("hfs") != null && !"".equals(filedateM.get("hfs"))) {
                        int hfs = Integer.valueOf(filedateM.get("hfs").toString());
                        if (fileList.size() != hfs) {
                            updmessage.append("实际图像数量和目录不一致");
                            updateResult.put("hfs", "实际图像数量和目录不一致");
                            errtype = errtype + "实际图像数量和目录不一致,";
                            upd = true;
                        }
                    }
                }
                if("eq".equals(archiveType) && !isbkb){
                    updmessage.append("该文件没有备考表");
                    updateResult.put("bkb", "该文件没有备考表");
                    errtype = errtype + "该文件没有备考表,";
                    upd = true;
                }
            }
            if (upd) {
                updateResult.put("keyword", keyword);
                boolean updResult = rs.updData(updateResult);
                if (!updResult) {
                    checkEfileResult = false;
                }
            }
                //检测案卷画幅数
                if("eq".equals(archiveType) && "vf".equals(checkArclvl) && (configResultM.get("图像正确格式") != null && !"".equals(configResultM.get("图像正确格式")))){
                    String volkeyword = keyword.substring(0,keyword.lastIndexOf("-"));
                    if(dh == null || "".equals(dh)){//第一个文件，此时档号为空
                        dh = volkeyword;//将案卷档号的值给dh变量
                        filenum = fileList.size(); //将此次文件的电子文件数给filenum变量
                        fileIndex=1;
                    }else{
                        if (dh.equals(volkeyword)){//不是第一个文件，判断如果案卷档号相同，则累加文件电子文件数
                            fileIndex=fileIndex+1;
                            filenum = filenum+fileList.size();
                            Map volkM =voldateL.get(voldateL.size()-1);
                            String vk = volkM.get("keyword").toString();
                            if(fileListdateL.size()==fileIndex){
                                if(vk.equals(volkeyword)){
                                    //本次仅一卷或excel中最后一卷
                                    updfilenum = filenum;
                                    String volfield = "hfs";
                                    String voltablename = archiveType+"voldata";
                                    String volWhereStr = "keyword = '"+dh+"' ORDER BY REVERSE(LEFT(REVERSE(keyword),INSTR(REVERSE(keyword),'-')-1))";
                                    List<Map> hfsL  = rs.getData(volfield,voltablename,volWhereStr);
                                    if(hfsL.size()==1) {
                                        Map hfsM = (Map) hfsL.get(0);
                                        int hfs = Integer.valueOf(hfsM.get("hfs").toString());
                                        if(hfs!=updfilenum){
                                            String hfsR = "案卷总画幅数不等于该卷电子文件图像之和";
                                            String updVolResultSql = "update s_volresult set hfs='"+hfsR+"' where keyword = '"+dh+"'";
                                            log.info("修改案卷画幅数sql="+updVolResultSql);
                                            this.jdbcTemplate.update(updVolResultSql);
                                        }
                                    }
                                }
                            }
                        }else{//案卷档号不相同，则将档号重新赋值给dh变量，并且将电子文件数重新赋值给filenum变量
                            fileIndex=0;
                            updfilenum = filenum;
                            String volfield = "hfs";
                            String voltablename = archiveType+"voldata";
                            String volWhereStr = "keyword = '"+dh+"' ORDER BY REVERSE(LEFT(REVERSE(keyword),INSTR(REVERSE(keyword),'-')-1))";
                            List<Map> hfsL  = rs.getData(volfield,voltablename,volWhereStr);
                            if(hfsL.size()==1) {
                                Map hfsM = (Map) hfsL.get(0);
                                int hfs = Integer.valueOf(hfsM.get("hfs").toString());
                                if(hfs!=updfilenum){
                                    String hfsR = "案卷总画幅数不等于该卷电子文件图像之和";
                                    String updVolResultSql = "update s_volresult set hfs='"+hfsR+"' where keyword = '"+dh+"'";
                                    log.info("修改案卷画幅数sql="+updVolResultSql);
                                    this.jdbcTemplate.update(updVolResultSql);
                                }
                            }
                            dh = volkeyword;
                            filenum = fileList.size();
                        }
                    }
                }
        }else{
                checkEfileResult = false;
            }
//        }catch (Exception e){
//            log.info(e.getMessage());
//            checkEfileResult = false;
//        }
        return checkEfileResult;
    }
    public String checkPath(String efilePath,String libcode,String archiveType,Map filedateM,String checkArclvl,String keyword){
        String whereStr = "libcode = '"+libcode+"'";
        String libname = "";
        List libnameL = rs.getData("chname","s_arc",whereStr);
        if(libnameL.size()>0){
            Map libnameM = (Map)libnameL.get(0);
            if(libnameM.get("chname")!=null && !"".equals(libnameM.get("chname"))) {
                libname = libnameM.get("chname").toString();
            }
        }
        String checkPresult = "";
        efilePath = efilePath.replaceAll("\\\\","/");
        //检测文件路径规则
        String filelvl = efilePath.substring(efilePath.indexOf("root")+5,efilePath.length());
        if("xd".equals(archiveType)){
            if("vf".equals(checkArclvl)){
                String[] filelvls = efilePath.split("/");
                String ep = filelvls[filelvls.length-2];
                log.info("ep = "+ep);
                String wjjName = filelvls[filelvls.length-2];
                log.info("wjjName = "+wjjName);
                String xdwjj = keyword.substring(0,keyword.lastIndexOf("-"));
                if (!wjjName.equals(xdwjj)) {
                    checkPresult="路径格式不正确,全宗不正确；";
                }else{
                    String volkey = filelvls[filelvls.length-3];
                    String xdFilewjj = keyword.substring(0,keyword.lastIndexOf("-"));
                    String xdvolwjj = xdFilewjj.substring(0,xdFilewjj.lastIndexOf("-"));
                    if(!volkey.equals(xdvolwjj)){
                        checkPresult="路径格式不正确,全宗不正确；";
                    }
                }
//                String[] filelvls = efilePath.split("/");
//                boolean unitsysb = false;
//                int location=0;
//                String xd = filelvl.substring(0,filelvl.indexOf("\\\\"));
//                String sypath = filelvl.substring(filelvl.indexOf("\\\\")+1,filelvl.length());
//                for(int i=0;i<filelvls.length;i++){
//                    if(filelvls[i].startsWith("XD") || filelvls[i].startsWith("xd")){
//                        String xd = filelvls[i];
//                        if(!xd.contains("-")){
//                            checkPresult="路径格式不正确,全宗不正确";
//                        }else{
//                            if(xd.contains("·")){
//                                String xdbj = keyword.substring(0,keyword.indexOf("·")+3);
//                                if(!xd.equals(xdbj)){
//                                    checkPresult="路径格式不正确,全宗不正确";
//                                }else{
//                                    String xdlx = sypath.substring(0, sypath.indexOf("\\\\"));
//                                    String xdlx = filelvls[i+1];
//                                    sypath = filelvl.substring(filelvl.indexOf("\\\\")+1,filelvl.length());
//                                    String xdbtype = keyword.substring(0,keyword.indexOf("·")+5);
//                                    if(!xdlx.equals(xdbtype)){
//                                        checkPresult="路径格式不正确,全宗不正确";
//                                    }else{
//                                        String xdlxfile = sypath.substring(0, sypath.indexOf("\\\\"));
//                                        String xdlxfile = filelvls[i+2];
//                                        String xdbtfile = keyword.substring(0,keyword.indexOf("·")+5);
//                                        if(!xdlxfile.equals(xdbtfile)) {
//                                            checkPresult = "路径格式不正确,全宗不正确";
//                                        }
//                                    }
//                                }
//                            }else{
//                                String xdbj = keyword.substring(0,keyword.indexOf("-")+3);
//                                if(!xd.equals(xdbj)){
//                                    checkPresult="路径格式不正确,全宗不正确";
//                                }else{
//                                    String xdlx = sypath.substring(0, sypath.indexOf("\\\\"));
//                                    String xdlx = filelvls[i+1];
//                                    sypath = filelvl.substring(filelvl.indexOf("\\\\")+1,filelvl.length());
//                                    String xdbtype = keyword.substring(0,keyword.indexOf("-")+5);
//                                    if(!xdlx.equals(xdbtype)){
//                                        checkPresult="路径格式不正确,全宗不正确";
//                                    }else{
//                                        String xdlxfile = sypath.substring(0, sypath.indexOf("\\\\"));
//                                        String xdlxfile = filelvls[i+2];
//                                        String xdbtfile = keyword.substring(0,keyword.indexOf("-")+7);
//                                        if(!xdlxfile.equals(xdbtfile)) {
//                                            checkPresult = "路径格式不正确,全宗不正确";
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }else{
//                        continue;
//                    }
//                }
            }else{
                String en = efilePath.substring(efilePath.lastIndexOf("/")+1,efilePath.length());
                log.info("en = "+en);
                String ep = efilePath.substring(0,efilePath.lastIndexOf("/"));
                log.info("ep = "+ep);
                String wjjName = ep.substring(ep.lastIndexOf("/")+1,ep.length());
                log.info("wjjName = "+wjjName);
                String xdwjj = keyword.substring(0,keyword.indexOf("-"));
                if (!wjjName.equals(xdwjj)) {
                    checkPresult="路径格式不正确,全宗不正确；";
                }
//                String xd = filelvl.substring(0,filelvl.indexOf("/"));
//                String sypath = filelvl.substring(filelvl.indexOf("/")+1,filelvl.length());
//                if(!xd.contains("-")){
//                    checkPresult="路径格式不正确,全宗不正确";
//                }else{
//                    if(xd.contains("·")){
//                        String xdbj = keyword.substring(0,keyword.indexOf("·")+3);
//                        if(!xd.equals(xdbj)){
//                            checkPresult="路径格式不正确,全宗不正确";
//                        }else{
//                            String xdlx = sypath.substring(0, sypath.indexOf("/"));
//                            sypath = filelvl.substring(filelvl.indexOf("/")+1,filelvl.length());
//                            String xdbtype = keyword.substring(0,keyword.indexOf("·")+5);
//                            if(!xdlx.equals(xdbtype)){
//                                checkPresult="路径格式不正确,全宗不正确";
//                            }
//                        }
//                    }else{
//                        String xdbj = keyword.substring(0,keyword.indexOf("-")+3);
//                        if(!xd.equals(xdbj)){
//                            checkPresult="路径格式不正确,全宗不正确";
//                        }else{
//                            String xdlx = sypath.substring(0, sypath.indexOf("/"));
//                            sypath = filelvl.substring(filelvl.indexOf("/")+1,filelvl.length());
//                            String xdbtype = keyword.substring(0,keyword.indexOf("-")+5);
//                            if(!xdlx.equals(xdbtype)){
//                                checkPresult="路径格式不正确,全宗不正确";
//                            }
//                        }
//                    }
//                }
            }
        }
        if("vf".equals(checkArclvl) && "eq".equals(archiveType) ){
            String[] filelvls = efilePath.split("/");
            boolean unitsysb = false;
            int location=0;
            if(filedateM.get("unitsys")!=null && !"".equals(filedateM.get("unitsys"))) {
                String unit = filedateM.get("unitsys").toString();
                for(int i=0;i<filelvls.length;i++){
                    if (!filelvls[i].equals(unit)) {
                        continue;
                    } else {
                        location=i;
                        unitsysb = true;
                        String arctype =  filelvls[i+1];
                        if(arctype.contains("-")){
                            String[] arctypes = arctype.split("-");
                            if(!"WS".equals(arctypes[1]) && !"KY".equals(arctypes[1]) && !"ZP".equals(arctypes[1])){
                                checkPresult="路径格式不正确,档案类型不正确；";
                            }else {
                                String dah = filedateM.get("dah").toString();
                                String vollsh = filelvls[i+2];
                                if(vollsh.contains("-")){
                                    vollsh = vollsh.substring(vollsh.lastIndexOf("-")+1,vollsh.length());
                                    if(!vollsh.equals(dah)){
                                        checkPresult="路径格式不正确,年度不正确；";
                                    }else{
                                        String jh = filedateM.get("f6").toString();
                                        String jlsh = filelvls[i+3];
                                        if(jlsh.contains("-")){
                                            jlsh = jlsh.substring(jlsh.lastIndexOf("-")+1,jlsh.length());
                                            if(!jlsh.equals(jh)){
                                                int jlshi =0;
                                                int jhi = 0;
                                                if(jlsh.contains(".")){
                                                    jlshi = Double.valueOf(jlsh).intValue();
                                                }else{
                                                    jlshi = Integer.valueOf(jlsh);
                                                }
                                                if(jh.contains(".")){
                                                    jhi = Double.valueOf(jh).intValue();
                                                }else{
                                                    jhi = Integer.valueOf(jh);
                                                }
                                                if(jlshi!=jhi){
                                                    checkPresult="路径格式不正确,卷内顺序号不正确；";
                                                }
                                            }
                                        }else{
                                            checkPresult="电子文件路径格式不正确；";
                                        }
                                    }
                                }else{
                                    checkPresult="电子文件路径格式不正确；";
                                }
                            }
                        }else{
                            checkPresult="电子文件路径格式不正确；";
                        }
                    }
                    }
                if(!unitsysb){
                    checkPresult = "路径格式不正确,全宗不正确；";
                }
                }
        }else{
            if(libname.contains("文书") && "eq".equals(archiveType)) {//文书件
                String[] filelvls = efilePath.split("/");
                boolean unitsysb = false;
                int location=0;
                if(filedateM.get("unitsys")!=null && !"".equals(filedateM.get("unitsys"))) {
                    String unit = filedateM.get("unitsys").toString();
                    for(int i=0;i<filelvls.length;i++) {
                        if (!filelvls[i].equals(unit)) {
                            continue;
                        } else {
                            location=i;
                            unitsysb = true;
                            String arctype = filelvls[i+1];
                            if (arctype.contains("-")) {
//                                sypath = arctype.substring(sypath.indexOf("\\\\") + 1, sypath.length());
                                String[] arctypes = arctype.split("-");
                                if (!"WS".equals(arctypes[1])) {
                                    checkPresult = "路径格式不正确,档案类型不正确；";
                                } else {
                                    String year = filedateM.get("nd").toString();
                                    String nd = filelvls[i+2];
                                    if (nd.contains("-")) {
                                        nd = nd.substring(nd.lastIndexOf("-") + 1, nd.length());
//                                        sypath = sypath.substring(sypath.indexOf("\\\\") + 1, sypath.length());
                                        if (!nd.equals(year)) {
                                            checkPresult = "路径格式不正确,年度不正确；";
                                        } else {
                                            String jh = filedateM.get("f6").toString();
                                            String jlsh = filelvls[i+3];
                                            if (jlsh.contains("-")) {
                                                jlsh = jlsh.substring(jlsh.lastIndexOf("-") + 1, jlsh.length());
//                                                sypath = sypath.substring(sypath.indexOf("\\\\") + 1, sypath.length());
                                                if (!jlsh.equals(jh)) {
                                                    int jlshi =0;
                                                    int jhi = 0;
                                                    if(jlsh.contains(".")){
                                                        jlshi = Double.valueOf(jlsh).intValue();
                                                    }else{
                                                        jlshi = Integer.valueOf(jlshi);
                                                    }
                                                    if(jh.contains(".")){
                                                        jhi = Double.valueOf(jh).intValue();
                                                    }else{
                                                        jhi = Integer.valueOf(jh);
                                                    }
                                                    if(jlshi!=jhi){
                                                        checkPresult="路径格式不正确,卷内顺序号不正确；";
                                                    }
                                                }
                                            } else {
                                                checkPresult = "电子文件路径格式不正确；";
                                            }
                                        }
                                    } else {
                                        checkPresult = "电子文件路径格式不正确；";
                                    }
                                }
                            } else {
                                checkPresult = "电子文件路径格式不正确；";
                            }
                        }
                    }
                    if(!unitsysb){
                        checkPresult = "路径格式不正确,全宗不正确；";
                    }
                }
            }else if((libname.contains("音视频") || libname.contains("音频") || libname.contains("视频")) && "eq".equals(archiveType)){//音视频件
                String[] filelvls = efilePath.split("/");
                boolean unitsysb = false;
                int location=0;
//                String qzh = filelvl.substring(0,filelvl.indexOf("\\\\"));
//                String sypath = filelvl.substring(filelvl.indexOf("\\\\")+1,filelvl.length());
                if(filedateM.get("unitsys")!=null && !"".equals(filedateM.get("unitsys"))) {
                    String unit = filedateM.get("unitsys").toString();
                    for(int i=0;i<filelvls.length;i++) {
                        if (!filelvls[i].equals(unit)) {
//                            checkPresult = "路径格式不正确,全宗不正确";
                            continue;
                        } else {
                            location=i;
                            unitsysb = true;
                            String arctype = filelvls[i+1];
                            if (arctype.contains("-")) {
//                                sypath = arctype.substring(sypath.indexOf("\\\\") + 1, sypath.length());
                                String[] arctypes = arctype.split("-");
                                if (!"YP".equals(arctypes[1]) && "SP".equals(arctypes[1])) {
                                    checkPresult = "路径格式不正确,档案类型不正确；";
                                } else {
                                    String jh = filedateM.get("f6").toString();
                                    String jlsh = filelvls[i+2];
                                    if (jlsh.contains("-")) {
                                        jlsh = jlsh.substring(jlsh.lastIndexOf("-") + 1, jlsh.length());
//                                        sypath = sypath.substring(sypath.indexOf("\\\\") + 1, sypath.length());
                                        int jlshL = jlsh.length();
                                        int jhL = jh.length();
                                        if(jlshL!=jhL){
                                            for(int i1=0;i1<jlshL-jhL;i1++){
                                                jh = "0"+jh;
                                            }
                                        }
                                        if (!jlsh.equals(jh)) {
                                            int jlshi =0;
                                            int jhi = 0;
                                            if(jlsh.contains(".")){
                                                jlshi = Double.valueOf(jlsh).intValue();
                                            }else{
                                                jlshi = Integer.valueOf(jlshi);
                                            }
                                            if(jh.contains(".")){
                                                jhi = Double.valueOf(jh).intValue();
                                            }else{
                                                jhi = Integer.valueOf(jh);
                                            }
                                            if(jlshi!=jhi){
                                                checkPresult="路径格式不正确,卷内顺序号不正确；";
                                            }
                                        }
                                    } else {
                                        checkPresult = "电子文件路径格式不正确；";
                                    }
                                }
                            } else {
                                checkPresult = "电子文件路径格式不正确；";
                            }
                        }
                    }
                    if(!unitsysb){
                        checkPresult = "路径格式不正确,全宗不正确；";
                    }
                }
            }else if(libname.contains("名人") && "eq".equals(archiveType)){//名人件
                String[] filelvls = efilePath.split("/");
                boolean unitsysb = false;
                int location=0;
//                String qzh = filelvl.substring(0,filelvl.indexOf("\\\\"));
//                String sypath = filelvl.substring(filelvl.indexOf("\\\\")+1,filelvl.length());
                if(filedateM.get("unitsys")!=null && !"".equals(filedateM.get("unitsys"))) {
                    String unit = filedateM.get("unitsys").toString();
                    for(int i=0;i<filelvls.length;i++) {
                        if (!filelvls[i].equals(unit)) {
//                            checkPresult = "路径格式不正确,全宗不正确";
                            continue;
                        } else {
                            location=i;
                            unitsysb = true;
                            String arctype = filelvls[i+1];
                            if (arctype.contains("-")) {
//                                sypath = arctype.substring(sypath.indexOf("\\\\") + 1, sypath.length());
                                String[] arctypes = arctype.split("-");
                                if (!"A".equals(arctypes[1]) && !"B".equals(arctypes[1])) {
                                    checkPresult = "路径格式不正确,档案类型不正确；";
                                } else {
                                    String jh = filedateM.get("jh").toString();
                                    String jlsh = filelvls[i+1];
                                    jlsh = jlsh.substring(jlsh.lastIndexOf("-") + 1, jlsh.length());
                                    int jlshL = jlsh.length();
                                    int jhL = jh.length();
                                    if(jlshL!=jhL){
                                        for(int i1=0;i1<jlshL-jhL;i1++){
                                            jh = "0"+jh;
                                        }
                                    }
                                    if (!jlsh.equals(jh)) {
                                        int jlshi =0;
                                        int jhi = 0;
                                        if(jlsh.contains(".")){
                                            jlshi = Double.valueOf(jlsh).intValue();
                                        }else{
                                            jlshi = Integer.valueOf(jlshi);
                                        }
                                        if(jh.contains(".")){
                                            jhi = Double.valueOf(jh).intValue();
                                        }else{
                                            jhi = Integer.valueOf(jh);
                                        }
                                        if(jlshi!=jhi){
                                            checkPresult="路径格式不正确,卷内顺序号不正确；";
                                        }
                                    }
                                }
                            } else {
                                checkPresult = "电子文件路径格式不正确；";
                            }
                        }
                    }
                    if(!unitsysb){
                        checkPresult = "路径格式不正确,全宗不正确；";
                    }
                }
            }else if("".equals(libcode) && "eq".equals(archiveType)) {//文书件

            }
        }
        return checkPresult;
    }

    public String checkName(String efilename,String efilePath,int i){
        efilePath = efilePath.replaceAll("\\\\","/");
        String checkNresult = "";
        String efilelsh = efilename.substring(efilename.lastIndexOf("-")+1,efilename.lastIndexOf("."));
        String efilef = efilename.substring(0,efilename.lastIndexOf("-"));
        int lsh = Integer.valueOf(efilelsh);
        String[] paths = efilePath.split("/");
        String filedh = paths[paths.length-2];
        if(filedh.equals(efilef)){
            if(lsh!=i+1){
                checkNresult = "电子文件名称不正确,电子文件名称最后的文件流水号不正确；";
            }
        }else{
            checkNresult = "电子文件名称不正确,电子文件名称最后的文件流水号不正确；";
        }
        return checkNresult;
    }
}
