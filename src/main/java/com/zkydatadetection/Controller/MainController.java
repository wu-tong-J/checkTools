package com.unis.zkydatadetection.Controller;

import com.unis.zkydatadetection.model.MenuItem;
import com.unis.zkydatadetection.model.Rule;
import com.unis.zkydatadetection.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class MainController {
    @GetMapping("/main")
    public String index(ModelMap modelMap,HttpServletRequest request){
        List<MenuItem> menu = Arrays.asList(
            MenuItem.builder().id("1").iconClass("fa fa-folder").text("检测规则设置").url("#/1").build(),
			MenuItem.builder().id("2").iconClass("fa fa-folder").text("检测结果").children(
					Arrays.asList(
							MenuItem.builder().id("3").iconClass("fa fa-file").url("#/3").text("案卷级").build(),
							MenuItem.builder().id("4").iconClass("fa fa-file").url("#/4").text("文件级").build(),
							MenuItem.builder().id("5").iconClass("fa fa-file").url("#/5").text("电子文件").build()
							//MenuItem.builder().id("6").iconClass("fa fa-file").url("#/6").text("SIP").build()
					)
			).build(),
			MenuItem.builder().id("7").iconClass("fa fa-folder").text("历史记录").children(
					Arrays.asList(
							MenuItem.builder().id("8").iconClass("fa fa-file").url("#/8").text("案卷级").build(),
							MenuItem.builder().id("9").iconClass("fa fa-file").url("#/9").text("文件级").build(),
							MenuItem.builder().id("10").iconClass("fa fa-file").url("#/10").text("电子文件").build()
							//MenuItem.builder().id("11").iconClass("fa fa-file").url("#/11").text("SIP").build()
					)
			).build(),
			MenuItem.builder().id("12").iconClass("fa fa-folder").text("汇总统计").children(
					Arrays.asList(
							MenuItem.builder().id("13").iconClass("fa fa-file").url("#/13").text("历史").build(),
							MenuItem.builder().id("14").iconClass("fa fa-file").url("#/14").text("本次").build()
					)
			).build(),
			MenuItem.builder().id("15").iconClass("fa fa-folder").text("日志").url("#/15").build()
        );
        modelMap.put("username","admin");
        modelMap.put("menu",menu);
        //记录日志logSession
        String context = "登录";
        HttpSession logSession = request.getSession();
        logSession.setAttribute("username","admin");
        logSession.setAttribute("libcode","");
        logSession.setAttribute("context",context);
        return "index";
    }

    private List<User> mockUser() {
        List<User> users = new ArrayList<>();
        return null;
    }
}
