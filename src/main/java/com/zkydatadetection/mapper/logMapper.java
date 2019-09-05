package com.unis.zkydatadetection.mapper;

import com.unis.zkydatadetection.model.log;

public interface logMapper {
    int deleteByPrimaryKey(String syscode);

    int insert(log record);

    int insertSelective(log record);

    log selectByPrimaryKey(String syscode);

    int updateByPrimaryKeySelective(log record);

    int updateByPrimaryKey(log record);
}