package com.unis.zkydatadetection.mapper;

import com.unis.zkydatadetection.model.classify;

public interface classifyMapper {
    int deleteByPrimaryKey(String syscode);

    int insert(classify record);

    int insertSelective(classify record);

    classify selectByPrimaryKey(String syscode);

    int updateByPrimaryKeySelective(classify record);

    int updateByPrimaryKey(classify record);
}