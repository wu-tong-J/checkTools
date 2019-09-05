package com.unis.zkydatadetection.Controller;

import ch.qos.logback.classic.Logger;
import com.alibaba.druid.support.json.JSONUtils;
import com.unis.zkydatadetection.model.DataTableDTO;
import com.unis.zkydatadetection.service.statisticsService;
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
public class statisticsManageController {
    private final Logger log = (Logger) LoggerFactory.getLogger("statisticsManageController.class");

    @Autowired
    private statisticsService ss;

    @ResponseBody
    @RequestMapping(value = "/statistics/getStatisticsData",method = RequestMethod.POST)
    public DataTableDTO getStatisticsData(HttpServletRequest request, HttpServletResponse response){
        String draw = request.getParameter("draw");
        //排序
        String start = request.getParameter("start");//第几条数据开始
        String length = request.getParameter("length");//每页多少条
        Integer pagenum = (Integer.valueOf(start)/Integer.valueOf(length))+1;
        String libcode = request.getParameter("libcode");//档案类型
        String unitsys = request.getParameter("unitsys");//所属全宗
        String mj = request.getParameter("mj");//档案按密级统计时需传值
//        String archiveType = request.getParameter("archiveType");//先到专项或二期进馆
        String whereStr = request.getParameter("whereStr");//条件
        String ifHistory = request.getParameter("ifHistory");//0 当次 1 历史记录
//        ifHistory = "0";
        List data = ss.selectStat(libcode,mj,whereStr,ifHistory);
        int count = ss.getCountStat("s_statistics",whereStr,libcode,mj,ifHistory);
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
