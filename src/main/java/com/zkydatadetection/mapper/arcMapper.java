package com.unis.zkydatadetection.mapper;

import com.unis.zkydatadetection.model.arc;

public interface arcMapper {
    int deleteByPrimaryKey(String syscode);

    int insert(arc record);

    int insertSelective(arc record);

    arc selectByPrimaryKey(String syscode);

    int updateByPrimaryKeySelective(arc record);

    int updateByPrimaryKey(arc record);
}