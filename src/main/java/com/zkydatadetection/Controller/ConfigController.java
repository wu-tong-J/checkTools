package com.unis.zkydatadetection.Controller;

import com.unis.zkydatadetection.model.Config;
import com.unis.zkydatadetection.model.ConfigDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/config")
public class ConfigController {
    @Autowired
    JdbcTemplate jdbcTemplate;

    @ResponseBody
    @GetMapping("/getConfig")
    public Map<String, Config> getConfig() {
        Map<String, Config> map = null;
        try {
            String sql = "select * from s_config";
            BeanPropertyRowMapper<Config> rowMapper = new BeanPropertyRowMapper<Config>(Config.class);
            List<Config> configList = jdbcTemplate.query(sql, rowMapper);
            map = configList.stream().collect(Collectors.toMap(Config::getName, Function.identity()));
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
        return map;
    }

    @ResponseBody
    @PostMapping("/saveConfig")
    public String saveConfig(HttpServletRequest request, HttpServletResponse response, ConfigDTO configDTO){
        String sql = "update s_config set check_status=?,content=? where name=?";
        try {
            List<Config> list = configDTO.getConfigs();
            int[] result = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setInt(1,list.get(i).getCheckStatus()==null?0:list.get(i).getCheckStatus());
                    ps.setString(2,list.get(i).getContent());
                    ps.setString(3,list.get(i).getName());
                }
                @Override
                public int getBatchSize() {
                    return list.size();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
        return "ok";
    }
}
