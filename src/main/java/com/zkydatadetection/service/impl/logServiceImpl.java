package com.unis.zkydatadetection.service.impl;

import ch.qos.logback.classic.Logger;
import com.unis.zkydatadetection.mapper.logMapper;
import com.unis.zkydatadetection.model.log;
import com.unis.zkydatadetection.service.logService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service(value = "logService")
public class logServiceImpl implements logService {
    private final Logger log = (Logger) LoggerFactory.getLogger("logServiceImpl.class");
//    @Autowired
//    private logMapper lm;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public boolean addLog(String usercode,String libcode,String content) {
        String syscode = this.getCode();
        String createtime =this.getDate();
        StringBuffer addSql = new StringBuffer();
        addSql.append("insert into s_log (syscode,libcode,createtime,username,content) value ('");
        addSql.append(syscode).append("','").append(libcode).append("','").append(createtime).append("','");
        addSql.append(usercode).append("','").append(content).append("')");
//        log log = new log();
//        log.setSyscode(syscode);
//        log.setLibcode(libcode);
//        log.setCreatetime(createtime);
//        log.setUsername(usercode);
//        log.setcontent(content);
//        lm.insert(log);
        this.jdbcTemplate.update(addSql.toString());
        return false;
    }

    @Override
    public List<Map<String, Object>> selectLog(String tablename, String field, int pagesize, int pagenum, String whereStr,String order) {
        String where = "";
        if(whereStr==null || "".equals(whereStr)){
            where =" 1=1";
        }else {
            String[] whereStrs = whereStr.split(" ~ ");
            where = "createtime>='"+whereStrs[0]+"' and createtime<='"+whereStrs[1]+"'";
        }
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
        List Data = this.jdbcTemplate.queryForList(getCheckDataSql.toString());
        return Data;
    }

    @Override
    public List<Map<String, Object>> selectLog(String tablename, String field, String whereStr) {
        String where = "";
        if(whereStr==null || "".equals(whereStr)){
            where =" 1=1";
        }else {
            String[] whereStrs = whereStr.split("~");
            where = "createtime>='"+whereStrs[0]+" and createtime<='"+whereStrs[1]+"'";
        }
        StringBuffer getCheckDataSql = new StringBuffer();
        getCheckDataSql.append("select ").append(field).append(" from ").append(tablename);
        getCheckDataSql.append(" where ").append(where).append(" order by createtime desc");
        List Data = this.jdbcTemplate.queryForList(getCheckDataSql.toString());
        return Data;
    }

    @Override
    public int getCountLog(String tablename, String whereStr) {
        String where = "";
        if(whereStr==null || "".equals(whereStr)){
            where =" 1=1";
        }else {
            String[] whereStrs = whereStr.split(" ~ ");
            where = "createtime>='"+whereStrs[0]+"' and createtime<='"+whereStrs[1]+"'";
        }
        String getCountSql = "select count(1) from "+tablename+" where "+where;
        int count = this.jdbcTemplate.queryForObject(getCountSql,Integer.class);
        return count;
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
