package com.unis.zkydatadetection.mapper;

import com.unis.zkydatadetection.model.param;

public interface paramMapper {
    int deleteByPrimaryKey(String syscode);

    int insert(param record);

    int insertSelective(param record);

    param selectByPrimaryKey(String syscode);

    int updateByPrimaryKeySelective(param record);

    int updateByPrimaryKey(param record);
}