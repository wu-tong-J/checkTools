package com.unis.zkydatadetection.mapper;

import com.unis.zkydatadetection.model.file10001result;

public interface result10001Mapper {
    int deleteByPrimaryKey(String syscode);

    int insert(file10001result record);

    int insertSelective(file10001result record);

    file10001result selectByPrimaryKey(String syscode);

    int updateByPrimaryKeySelective(file10001result record);

    int updateByPrimaryKey(file10001result record);
}