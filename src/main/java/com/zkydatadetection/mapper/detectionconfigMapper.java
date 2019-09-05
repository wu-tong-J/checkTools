package com.unis.zkydatadetection.mapper;

import com.unis.zkydatadetection.model.detectionconfig;

public interface detectionconfigMapper {
    int deleteByPrimaryKey(String syscode);

    int insert(detectionconfig record);

    int insertSelective(detectionconfig record);

    detectionconfig selectByPrimaryKey(String syscode);

    int updateByPrimaryKeySelective(detectionconfig record);

    int updateByPrimaryKey(detectionconfig record);
}