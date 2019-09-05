package com.unis.zkydatadetection.Controller;

import ch.qos.logback.classic.Logger;
import com.alibaba.druid.support.json.JSONUtils;
import com.unis.zkydatadetection.model.DataTableDTO;
import com.unis.zkydatadetection.service.resultService;
import com.unis.zkydatadetection.service.sipService;
import com.unis.zkydatadetection.util.EctractZip;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xmlpull.v1.XmlPullParserException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping(value = "/zky")
public class sipMangeController {
    private final Logger log = (Logger) LoggerFactory.getLogger("sipMangeController.class");

    @Autowired
    private sipService ssi;

    @Autowired
    private resultService rs;

    @RequestMapping(value = "/sip/imp")
    public void checkSip(HttpServletRequest request, HttpServletResponse response){
        String libcode = request.getParameter("libcode");//档案类型
        String unitsys = request.getParameter("unitsys");//所属全宗
        String archiveType = request.getParameter("archiveType");//先到专项或二期进馆
        String arcLvl = request.getParameter("arcLvl");//检测项file;vol-file
        String sipPath = request.getParameter("sipPath");//sip包路径
        String ext = sipPath.substring(sipPath.lastIndexOf("."),sipPath.length());
        String targetPath = sipPath.substring(0,sipPath.lastIndexOf("/")+1);
        String zipName = sipPath.substring(sipPath.lastIndexOf("/")+1,sipPath.lastIndexOf("."));
        Map sipAttr = new HashMap();
        sipAttr.put("libcode",libcode);
        sipAttr.put("unitsys",unitsys);
        sipAttr.put("archiveType",archiveType);
        sipAttr.put("arcLvl",arcLvl);
        sipAttr.put("sipPath",sipPath);
        String path = "";
        if(".zip".equals(ext)){
            EctractZip ez = new EctractZip();
            try {
                String bool = ez.unZip(sipPath,targetPath);
                if(bool=="yes"){
                    log.info("解压缩文件成功");
                }else{
                    log.info("解压缩文件失败");
                }
                path = sipPath+"/archive.xml";
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            path = targetPath+zipName+"/archive.xml";
        }
        File pathF = new File(path);
        if(pathF.exists()){
            try {
                List sipList = ssi.parseSipInfo(path,libcode,sipAttr);
                ssi.checkSip(sipList,sipAttr);
                //记录日志logSession
                String context = "检测在线SIP数据包";
                HttpSession logSession = request.getSession();
                logSession.setAttribute("username","admin");
                logSession.setAttribute("libcode",libcode);
                logSession.setAttribute("context",context);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            log.info("文件路径不存在");
        }
    }

    @ResponseBody
    @RequestMapping(value = "/check/getSipData",method = RequestMethod.POST)
    public DataTableDTO getCheckData(HttpServletRequest request, HttpServletResponse response){
        String draw = request.getParameter("draw");
        //排序
        String colNum = request.getParameter("order[0][column]");//第几列排序
        String orderType = request.getParameter("order[0][dir]");//第几列排序
        String colName = request.getParameter("columns["+colNum+"][data]");//列名

        String ifHistory = request.getParameter("ifHistory");//0当次 1 历史
        String start = request.getParameter("start");//第几条数据开始
        String length = request.getParameter("length");//每页多少条
        Integer pagenum = (Integer.valueOf(start)/Integer.valueOf(length))+1;
        String libcode = request.getParameter("libcode");//档案类型
        String unitsys = request.getParameter("unitsys");//所属全宗
        String arcLvl = request.getParameter("arcLvl");//检测项file;vol-file
        String whereSql = request.getParameter("whereSql");//准确条件
        String whereErr = request.getParameter("whereErr");//模糊查询条件
        String dateStr = request.getParameter("dateStr");//日期条件
        String whereStr="";
        String whereField = "";
        if(whereErr!=null && !"".equals(whereErr)) {
            whereField = rs.getMysqlField("s_sipresult", "zkydatadetection", "sip");
        }
        if(whereSql!=null && !"".equals(whereSql)){
            whereStr = whereSql;
        }else{
            whereStr = "1=1";
        }
        if(dateStr!=null && !"".equals(dateStr)) {
            String[] dateStrs = dateStr.split(" ~ ");
            whereStr = whereStr +" and createtime>='" + dateStrs[0] + "' and createtime<='" + dateStrs[1] + "' and";
        }
        if(whereField!=null && !"".equals(whereField)){
            String[] whereFields = whereField.split(",");
            String wf = "";
            for(int i=0;i<whereFields.length;i++){
                wf = wf+"IFNULL("+whereFields[i]+",''),";
            }
            wf = wf.substring(0,wf.length()-1);
            whereStr = whereStr+" OR CONCAT("+wf+") like '%"+whereErr+"%'";
        }
        String checkField = "sipfilename,sipfilepath,voltitle,filetitle,volzdfh,filezdfh,efileStatus,parserSip,digital,dzwj,createtime,libname";
        String tablename = "s_sipresult";
        List data = rs.getCheckData(tablename,checkField,Integer.valueOf(length),Integer.valueOf(pagenum),ifHistory,whereStr," order by "+colName+" "+orderType);
        int count = rs.getCheckDataCount(tablename,whereStr,ifHistory);
        DataTableDTO dto = new DataTableDTO();
        dto.setDraw(Integer.valueOf(draw));
        dto.setStart(Integer.valueOf(start));
        dto.setRecordsTotal(count);
        dto.setRecordsFiltered(count);
        dto.setLength(Integer.valueOf(length));
        dto.setData(data);
        //记录日志logSession
        String context = "获取在线SIP数据包数据";
        HttpSession logSession = request.getSession();
        logSession.setAttribute("username","admin");
        logSession.setAttribute("libcode",libcode);
        logSession.setAttribute("context",context);
        return dto;
    }
}
