package com.unis.zkydatadetection.Controller;

import ch.qos.logback.classic.Logger;
import com.alibaba.druid.support.json.JSONUtils;
import com.unis.zkydatadetection.model.DataTableDTO;
import com.unis.zkydatadetection.service.logService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

@Slf4j
@Controller
@RequestMapping(value = "/zky")
public class logManageController {
    private final Logger log = (Logger) LoggerFactory.getLogger("logManageController.class");

    @Autowired
    private logService ls;

    @ResponseBody
    @RequestMapping(value = "/log/getLogData",method = RequestMethod.POST)
    public DataTableDTO getLogData(HttpServletRequest request, HttpServletResponse response){
        String draw = request.getParameter("draw");
        //排序
        String colNum = request.getParameter("order[0][column]");//第几列排序
        String orderType = request.getParameter("order[0][dir]");//第几列排序
        String colName = request.getParameter("columns["+colNum+"][data]");//列名

        String start = request.getParameter("start");//第几条数据开始
        String length = request.getParameter("length");//每页多少条
        Integer pagenum = (Integer.valueOf(start)/Integer.valueOf(length))+1;
        String libcode = request.getParameter("libcode");//档案类型
//      String unitsys = request.getParameter("unitsys");//所属全宗
        String archiveType = request.getParameter("archiveType");//先到专项或二期进馆
        if("1".equals(archiveType)){
            archiveType = "eq";
        }
        if("2".equals(archiveType)){
            archiveType = "xd";
        }
        String arcLvl = request.getParameter("arcLvl");//检测项file;vol-file
        String whereStr = request.getParameter("whereStr");//条件
        String checkField = "libcode,username,content,createtime";
        List data = ls.selectLog("s_log",checkField,Integer.valueOf(length),Integer.valueOf(pagenum),whereStr," order by "+colName+" "+orderType);
        int count = ls.getCountLog("s_log",whereStr);
        DataTableDTO dto = new DataTableDTO();
        dto.setDraw(Integer.valueOf(draw));
        dto.setStart(Integer.valueOf(start));
        dto.setRecordsTotal(count);
        dto.setRecordsFiltered(count);
        dto.setLength(Integer.valueOf(length));
        dto.setData(data);
        //记录日志logSession
        String context = "查询日志";
        HttpSession logSession = request.getSession();
        logSession.setAttribute("username","admin");
        logSession.setAttribute("libcode",libcode);
        logSession.setAttribute("context",context);
        return dto;
    }
}
