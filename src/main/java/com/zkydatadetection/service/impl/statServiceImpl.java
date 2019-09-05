package com.unis.zkydatadetection.service.impl;

import ch.qos.logback.classic.Logger;
import com.unis.zkydatadetection.service.resultService;
import com.unis.zkydatadetection.service.statisticsService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service("statisticsService")
public class statServiceImpl implements statisticsService {
    private final Logger log = (Logger) LoggerFactory.getLogger("baseDataServiceImpl.class");

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    private resultService rs;

    @Override
    public boolean addDataStat(Map statMap) {
        String tablename = statMap.get("tablename").toString();
        String archiveType = statMap.get("archiveType").toString();
        String arclvl = statMap.get("arcLvl").toString();
        String keyword = statMap.get("keyword").toString();
        String libcode = statMap.get("libcode").toString();
        String mj = statMap.get("mj").toString();
        if("vol".equals(arclvl)){
            String syscode = this.getCode();
            String createtime =this.getDate();
            int js = 0;
            int ys = 0;
            int hfs = 0;
            int volnum = 0;
            if(statMap.get("zjs")!=null && !"".equals(statMap.get("zjs"))){
                js = Integer.valueOf(statMap.get("zjs").toString());
            }
            if(statMap.get("zys")!=null && !"".equals(statMap.get("zys"))) {
                ys = Integer.valueOf(statMap.get("zys").toString());
            }
            if(statMap.get("hfs")!=null && !"".equals(statMap.get("hfs"))) {
                hfs = Integer.valueOf(statMap.get("hfs").toString());
            }
            if(statMap.get("volnum")!=null && !"".equals(statMap.get("volnum"))) {
                volnum = Integer.valueOf(statMap.get("volnum").toString());
            }
            StringBuffer addResultData = new StringBuffer();
            addResultData.append("insert into ").append(tablename).append(" (syscode,libcode,archivetype,arclvl,keyword,mj,js,ys,hfs,volnum,createtime,status) value ('");
            addResultData.append(syscode).append("','").append(libcode).append("','").append(archiveType).append("','").append(arclvl).append("','").append(keyword).append("','").append(mj);
            addResultData.append("',").append(js).append(",").append(ys).append(",").append(hfs).append(",").append(volnum).append(",'").append(createtime).append("',0)");
            this.jdbcTemplate.update(addResultData.toString());
        }
        if("file".equals(arclvl)){
            String syscode = this.getCode();
            String createtime =this.getDate();
            int js = 0;
            int ys = 0;
            int hfs = 0;
            if(statMap.get("js")!=null && !"".equals(statMap.get("js"))) {
                js = Integer.valueOf(statMap.get("js").toString());
            }
            if(statMap.get("ys")!=null && !"".equals(statMap.get("ys"))) {
                ys = Integer.valueOf(statMap.get("ys").toString());
            }
            if(statMap.get("hfs")!=null && !"".equals(statMap.get("hfs"))) {
                hfs = Integer.valueOf(statMap.get("hfs").toString());
            }
            StringBuffer addResultData = new StringBuffer();
            addResultData.append("insert into ").append(tablename).append(" (syscode,libcode,archivetype,arclvl,keyword,mj,js,ys,hfs,createtime,status) value ('");
            addResultData.append(syscode).append("','").append(libcode).append("','").append(archiveType).append("','").append(arclvl).append("','").append(keyword).append("','").append(mj);
            addResultData.append("',").append(js).append(",").append(ys).append(",").append(hfs).append(",'").append(createtime).append("',0)");
            this.jdbcTemplate.update(addResultData.toString());
        }
        if("efile".equals(arclvl)){
            String syscode = this.getCode();
            String createtime =this.getDate();
            String ext = statMap.get("ext").toString();
            StringBuffer addResultData = new StringBuffer();
            addResultData.append("insert into ").append(tablename).append(" (syscode,libcode,archivetype,arclvl,keyword,mj,js,ys,hfs,volnum,efilenum,ext,createtime,status) value ('");
            addResultData.append(syscode).append("','").append(libcode).append("','").append(archiveType).append("','").append(arclvl).append("','").append(keyword).append("','").append(mj);
            addResultData.append("',0,0,0,0,1,'").append(ext).append("','").append(createtime).append("',0)");
            this.jdbcTemplate.update(addResultData.toString());
        }

        return false;
    }

    @Override
    public int getCountStat(String tablename, String whereStr,String libcode,String mj,String ifHistory) {
        String where = "";
        if(whereStr==null || "".equals(whereStr)){
            where =" 1=1";
        }else {
            if(whereStr.contains("~")) {
                String[] whereStrs = whereStr.split("~");
                where = "createtime>='"+ whereStrs[0].trim()+"' and createtime<='"+whereStrs[1].trim()+"'";
            }if("1=1".equals(whereStr)){
                where =" 1=1";
            }
        }
        if(ifHistory!=null && !"".equals(ifHistory)){
            if("0".equals(ifHistory)){
                where = where +" and status = 0";
            }else if("1".equals(ifHistory)){
                where = where +" and status = 1";
            }
        }
        StringBuffer getCountSql = new StringBuffer();
        getCountSql.append("select count(1) from ").append(tablename).append(" where (").append(where).append(")");
        if(libcode!=null && !"".equals(libcode)){
            getCountSql.append(" and libcode = '").append(libcode).append("'");
        }
        if(mj!=null && !"".equals(mj)){
            getCountSql.append(" and mj = '").append(mj).append("'");
        }
        int count = this.jdbcTemplate.queryForObject(getCountSql.toString(),Integer.class);
        return count;
    }

    @Override
    public List<Map<String, Object>> selectStat(String libcode,String mj,String whereStr,String ifHistory) {
        String getLib = "select libcode from s_arc where chname = '"+libcode+"'";
        List libL = this.jdbcTemplate.queryForList(getLib);
        String lib = "";
        if(libL.size()>0) {
            Map libM = (Map) libL.get(0);
            lib = libM.get("libcode").toString();
            libcode = lib;
        }
        boolean statVol = false;
        boolean statFile = false;
        String arctype = "";
        String libname = "";
        //测试
//        if(libcode == null || "".equals(libcode)){
//            libcode="1";
//        }
        //获取档案门类名称和整理方式vf卷件，f单件
        if(libcode != null && !"".equals(libcode)) {
            String whereLib = "libcode = '" + libcode + "'";
            List arctypeL = rs.getData("arctype,chname", "s_arc", whereLib);
            if (arctypeL.size() > 0) {
                Map arctypeM = (Map) arctypeL.get(0);
                if (arctypeM.get("arctype") != null && !"".equals(arctypeM.get("arctype"))) {
                    arctype = arctypeM.get("arctype").toString();
                    libname = arctypeM.get("chname").toString();
                }
            }
            if("vf".equals(arctype)){
                statVol = true;
                statFile = true;
            }
            if("f".equals(arctype)){
                statFile = true;
            }
        }else{
            statVol = true;
            statFile = true;
        }
        int fileVolnum = 0;
        List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
        String where = "";
        if(whereStr==null || "".equals(whereStr)){
            where =" 1=1";
        }else {
            if(whereStr.contains("~")) {
                String[] whereStrs = whereStr.split("~");
                where = "createtime>='"+ whereStrs[0].trim()+"' and createtime<='"+whereStrs[1].trim()+"'";
            }if("1=1".equals(whereStr)){
                where =" 1=1";
            }
        }
        if(ifHistory!=null && !"".equals(ifHistory)){
            if("0".equals(ifHistory)){
                where = where +" and status = 0";
            }else if("1".equals(ifHistory)){
                where = where +" and status = 1";
            }
        }
        //获取电子文件统计信息
        String getLibs = "select libcode from s_arc where arctype = 'vf'";
        List libsL = this.jdbcTemplate.queryForList(getLibs);
        String libsys = "";
        for(int l=0;l<libsL.size();l++){
            Map libsM = (Map)libsL.get(l);
            libsys =libsys+"'"+libsM.get("libcode").toString()+"',";
        }
        if(libsys!=null && !"".equals(libsys)){
            libsys = libsys.substring(0,libsys.length()-1);
        }
        //卷内文件级
        String getCountEfile = "select count(1) from s_statistics where arclvl = 'efile' ";
        if (libcode != null && !"".equals(libcode)) {
            getCountEfile = getCountEfile + " and libcode = '" + libcode + "'";
        }
        getCountEfile = getCountEfile + " and " + where;
        int efilenum = this.jdbcTemplate.queryForObject(getCountEfile,Integer.class);
        //案卷级
        String getCountEfileForVol = "select count(1) from s_statistics where arclvl = 'efile' ";
        if (libcode != null && !"".equals(libcode)) {
            getCountEfileForVol = getCountEfileForVol + " and libcode = '" + libcode + "'";
        }else{
            getCountEfileForVol = getCountEfileForVol + " and libcode in (" + libsys + ")";
        }
        getCountEfileForVol = getCountEfileForVol + " and " + where;
        int efilenumForVol = this.jdbcTemplate.queryForObject(getCountEfileForVol,Integer.class);
        //卷内文件级
        String getExtEfile = "select distinct(ext) from s_statistics where arclvl = 'efile' ";
        if (libcode != null && !"".equals(libcode)) {
            getExtEfile = getExtEfile + " and libcode = '" + libcode + "'";
        }
        getExtEfile = getExtEfile + " and " + where;
        List extL = this.jdbcTemplate.queryForList(getExtEfile);
        String ext = "";
        if(extL.size()>0) {
            for (int e = 0; e < extL.size(); e++) {
                Map extM = (Map) extL.get(e);
                if (extM.get("ext") != null && !"".equals(extM.get("ext"))) {
                    ext = ext + extM.get("ext").toString() + ",";
                }
            }
            ext = ext.substring(0, ext.length() - 1);
        }
        //案卷级
        String getExtEfileForVol = "select distinct(ext) from s_statistics where arclvl = 'efile' ";
        if (libcode != null && !"".equals(libcode)) {
            getExtEfileForVol = getExtEfileForVol + " and libcode = '" + libcode + "'";
        }else{
            getExtEfileForVol = getExtEfileForVol + " and libcode in (" + libsys + ")";
        }
        getExtEfileForVol = getExtEfileForVol + " and " + where;
        List extLForVol = this.jdbcTemplate.queryForList(getExtEfileForVol);
        String extForVol = "";
        if(extLForVol.size()>0) {
            for (int e = 0; e < extLForVol.size(); e++) {
                Map extM = (Map) extLForVol.get(e);
                if (extM.get("ext") != null && !"".equals(extM.get("ext"))) {
                    extForVol = extForVol + extM.get("ext").toString() + ",";
                }
            }
            extForVol = extForVol.substring(0, extForVol.length() - 1);
        }
        //卷内文件级
        String getCCA4Efile = "select count(cc) from s_statistics where arclvl = 'efile' and cc='A4及以下' ";
        if (libcode != null && !"".equals(libcode)) {
            getCCA4Efile = getCCA4Efile + " and libcode = '" + libcode + "'";
        }
        getCCA4Efile = getCCA4Efile + " and " + where;
        int cca4 = this.jdbcTemplate.queryForObject(getCCA4Efile,Integer.class);
        //案卷级
        String getCCA4EfileForVol = "select count(cc) from s_statistics where arclvl = 'efile' and cc='A4及以下' ";
        if (libcode != null && !"".equals(libcode)) {
            getCCA4EfileForVol = getCCA4EfileForVol + " and libcode = '" + libcode + "'";
        }else{
            getCCA4EfileForVol = getCCA4EfileForVol + " and libcode in (" + libsys + ")";
        }
        getCCA4EfileForVol = getCCA4EfileForVol + " and " + where;
        int cca4ForVol = this.jdbcTemplate.queryForObject(getCCA4EfileForVol,Integer.class);
        //卷内文件级
        String getCCA3Efile = "select count(cc) from s_statistics where arclvl = 'efile' and cc='A3及以上' ";
        if (libcode != null && !"".equals(libcode)) {
            getCCA3Efile = getCCA3Efile + " and libcode = '" + libcode + "'";
        }
        getCCA3Efile = getCCA3Efile + " and " + where;
        int cca3 = this.jdbcTemplate.queryForObject(getCCA3Efile,Integer.class);
        //案卷级
        String getCCA3EfileForVol = "select count(cc) from s_statistics where arclvl = 'efile' and cc='A3及以上' ";
        if (libcode != null && !"".equals(libcode)) {
            getCCA3EfileForVol = getCCA3EfileForVol + " and libcode = '" + libcode + "'";
        }else{
            getCCA3EfileForVol = getCCA3EfileForVol + " and libcode in (" + libsys + ")";
        }
        getCCA3EfileForVol = getCCA3EfileForVol + " and " + where;
        int cca3ForVol = this.jdbcTemplate.queryForObject(getCCA3EfileForVol,Integer.class);
        //卷内文件级
        String getCCA3A4Efile = "select count(cc) from s_statistics where arclvl = 'efile' and cc='A4至A3' ";
        if (libcode != null && !"".equals(libcode)) {
            getCCA3A4Efile = getCCA3A4Efile + " and libcode = '" + libcode + "'";
        }
        getCCA3A4Efile = getCCA3A4Efile + " and " + where;
        int cca3a4 = this.jdbcTemplate.queryForObject(getCCA3A4Efile,Integer.class);
        //案卷级
        String getCCA3A4EfileForVol = "select count(cc) from s_statistics where arclvl = 'efile' and cc='A4至A3' ";
        if (libcode != null && !"".equals(libcode)) {
            getCCA3A4EfileForVol = getCCA3A4EfileForVol + " and libcode = '" + libcode + "'";
        }else{
            getCCA3A4EfileForVol = getCCA3A4EfileForVol + " and libcode in (" + libsys + ")";
        }
        getCCA3A4EfileForVol = getCCA3A4EfileForVol + " and " + where;
        int cca3a4ForVol = this.jdbcTemplate.queryForObject(getCCA3A4EfileForVol,Integer.class);
        if(statVol && statFile) {
            //获取案卷总卷数，总件数，总页数，总画幅数
            StringBuffer getVolDataSql = new StringBuffer();
            getVolDataSql.append("select count(1) as volnum, sum(js) as zjs, sum(ys) as zys,sum(hfs) as zhfs from s_statistics where arclvl = 'vol' ");
            if (libcode != null && !"".equals(libcode)) {
                getVolDataSql.append(" and libcode = '").append(libcode).append("'");
            }
            getVolDataSql.append(" and (").append(where).append(")");
            List<Map<String, Object>> dataL = this.jdbcTemplate.queryForList(getVolDataSql.toString());
            Map dataM = (Map) dataL.get(0);
            String getmjStatvolSql = "select mj,count(1) as countvol ,sum(ys) as countys,sum(js) as countjs,sum(hfs) as counthfs from s_statistics where arclvl='vol' ";
            if (libcode != null && !"".equals(libcode)) {
                getmjStatvolSql = getmjStatvolSql + " and libcode = '" + libcode + "'";
            }
            getmjStatvolSql = getmjStatvolSql + " and " + where + " GROUP BY mj";
            List<Map<String, Object>> getmjStatvolL = this.jdbcTemplate.queryForList(getmjStatvolSql);
            String volnum = "";
            String voljs = "";
            String volys = "";
            String volhfs = "";
            for (int i = 0; i < getmjStatvolL.size(); i++) {
                Map getmjStatvolM = getmjStatvolL.get(i);
                String key = getmjStatvolM.get("mj").toString();
                String valueVolnum = getmjStatvolM.get("countvol").toString();
                String valueVoljs = getmjStatvolM.get("countjs").toString();
                String valueVolys = getmjStatvolM.get("countys").toString();
                String valueVolhfs = getmjStatvolM.get("counthfs").toString();
                volnum = volnum + key + "共" + valueVolnum + ",";
                voljs = voljs + key + "共" + valueVoljs + ",";
                volys = volys + key + "共" + valueVolys + ",";
                volhfs = volhfs + key + "共" + valueVolhfs + ",";
            }
            String volmjnum = "";
            if(volnum!=null && !"".equals(volnum)){
                volmjnum = volnum.substring(0, volnum.lastIndexOf(","));
            }
            String volmjjs = "";
            if(voljs!=null && !"".equals(voljs)) {
                volmjjs = voljs.substring(0, voljs.lastIndexOf(","));
            }
            String volmjys = "";
            if(volys!=null && !"".equals(volys)) {
                volmjys = volys.substring(0, volys.lastIndexOf(","));
            }
            String volmjhfs = "";
            if(volhfs!=null && !"".equals(volhfs)) {
                volmjhfs = volhfs.substring(0, volhfs.lastIndexOf(","));
            }
            dataM.put("mjnum", volmjnum);
            dataM.put("mjjs", volmjjs);
            dataM.put("mjys", volmjys);
            dataM.put("mjhfs", volmjhfs);
            dataM.put("ly", "案卷级目录");
            dataM.put("fileEnum", efilenumForVol);
            dataM.put("ext", extForVol);
            dataM.put("A3up", cca3ForVol);
            dataM.put("A3A4", cca3a4ForVol);
            dataM.put("A4down", cca4ForVol);
            if (libcode != null && !"".equals(libcode)) {
                dataM.put("libname", libname);
            }else{
                dataM.put("libname", "所有门类");
            }
            Map dataM1 = new HashMap();
            for(Object key:dataM.keySet()){
               if(dataM.get(key)!=null && !"".equals(dataM.get(key))){
                   dataM1.put(key,dataM.get(key));
               }else{
                   dataM1.put(key,0);
               }
            }
            resultList.add(dataM1);
        }
        if(statFile) {
            //获取文件总件数，总页数，总画幅数，卷数（密级），件数（密级），页数（密级）
            StringBuffer getFileDataSql = new StringBuffer();
            getFileDataSql.append("select count(1) as zjs,sum(ys) as zys,sum(hfs) as zhfs from s_statistics where arclvl = 'file' ");
            if (libcode != null && !"".equals(libcode)) {
                getFileDataSql.append(" and libcode = '").append(libcode).append("'");
            }
            getFileDataSql.append(" and (").append(where).append(")");
            List<Map<String, Object>> fileDataL = this.jdbcTemplate.queryForList(getFileDataSql.toString());
            Map fileDataM = (Map) fileDataL.get(0);
            //通过distinct文件档号获取文件所属的总案卷数，卷数（密级），件数（密级），页数（密级）
            StringBuffer getFileVolnum = new StringBuffer();
            getFileVolnum.append("select DISTINCT(REVERSE(SUBSTR(REVERSE(keyword) FROM INSTR(REVERSE(keyword),'-')+1))) as vol from s_statistics where arclvl='file' ");
            if (libcode != null && !"".equals(libcode)) {
                getFileVolnum.append(" and libcode = '").append(libcode).append("'");
            }
            getFileVolnum.append(" and (").append(where).append(")");
            List<Map<String, Object>> getFileVolnumL = this.jdbcTemplate.queryForList(getFileVolnum.toString());
            fileVolnum = getFileVolnumL.size();
//            if("f".equals(arctype)){
//                fileDataM.put("volnum", 0);
//            }else{
//                fileDataM.put("volnum", fileVolnum);
//            }
            fileDataM.put("volnum", 0);
            String getmjStatSql = "select mj,COUNT(1) as countJS,SUM(ys) as countYS,SUM(hfs) as countHFS from s_statistics where arclvl = 'file' ";
            if (libcode != null && !"".equals(libcode)) {
                getmjStatSql = getmjStatSql + " and libcode = '" + libcode + "'";
            }
            getmjStatSql = getmjStatSql + " and " + where + " GROUP BY mj";
            List<Map<String, Object>> getmjStatL = this.jdbcTemplate.queryForList(getmjStatSql);
            String filejs = "";
            String fileys = "";
            String filehfs = "";
            String filevol = "";
            for (int i = 0; i < getmjStatL.size(); i++) {
                Map getmjStatM = getmjStatL.get(i);
                String key = getmjStatM.get("mj").toString();
                String valuejs = getmjStatM.get("countJS").toString();
                String valueys = getmjStatM.get("countYS").toString();
                String valuehfs = getmjStatM.get("countHFS").toString();
                filejs = filejs + key + "共" + valuejs + ",";
                fileys = fileys + key + "共" + valueys + ",";
                filehfs = filehfs + key + "共" + valuehfs + ",";
            }
            String filemjjs ="";
            if(filejs!=null && !"".equals(filejs)) {
                filemjjs = filejs.substring(0, filejs.lastIndexOf(","));
            }
            String filemjys ="";
            if(fileys!=null && !"".equals(fileys)) {
                filemjys = fileys.substring(0, fileys.lastIndexOf(","));
            }
            String filemjhfs = "";
            if(filehfs!=null && !"".equals(filehfs)) {
                filemjhfs = filehfs.substring(0, filehfs.lastIndexOf(","));
            }
            String getmjStatVolSql = "select mj,count(REVERSE(SUBSTR(REVERSE(keyword) FROM INSTR(REVERSE(keyword),'-')+1))) as vol from s_statistics where arclvl='file' ";
            if (libcode != null && !"".equals(libcode)) {
                getmjStatVolSql = getmjStatVolSql + " and libcode = '" + libcode + "'";
            }
            getmjStatVolSql = getmjStatVolSql + " and " + where + " GROUP BY mj";
            List<Map<String, Object>> getmjStatVolL = this.jdbcTemplate.queryForList(getmjStatVolSql);
            for (int i = 0; i < getmjStatVolL.size(); i++) {
                Map getmjStatVolM = getmjStatVolL.get(i);
                String key = getmjStatVolM.get("mj").toString();
                String valuevol = getmjStatVolM.get("vol").toString();
                filevol = filevol+key + "共" + valuevol+",";
            }
            String filemjvol ="";
            if(filevol!=null && !"".equals(filevol)) {
                filemjvol = filevol.substring(0, filevol.lastIndexOf(","));
            }
            fileDataM.put("mjjs", filemjjs);
            fileDataM.put("mjys", filemjys);
            fileDataM.put("mjhfs", filemjhfs);
            fileDataM.put("mjnum", 0);
            fileDataM.put("ly", "文件级目录");
            if (libcode != null && !"".equals(libcode)) {
                fileDataM.put("libname", libname);
            }else{
                fileDataM.put("libname", "所有门类");
            }
            fileDataM.put("fileEnum", efilenum);
            fileDataM.put("A3up", cca3);
            fileDataM.put("A3A4", cca3a4);
            fileDataM.put("A4down", cca4);
            fileDataM.put("ext", ext);
            Map fileDataM1 = new HashMap();
            for(Object key:fileDataM.keySet()){
                if(fileDataM.get(key)!=null && !"".equals(fileDataM.get(key))){
                    fileDataM1.put(key,fileDataM.get(key));
                }else{
                    fileDataM1.put(key,0);
                }
            }
            resultList.add(fileDataM1);
        }
        return resultList;
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
