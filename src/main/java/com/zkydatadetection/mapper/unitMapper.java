package com.unis.zkydatadetection.mapper;

import com.unis.zkydatadetection.model.unit;

import java.util.List;

public interface unitMapper {
    int deleteByPrimaryKey(String syscode);

    int insert(unit record);

    int insertSelective(unit record);

    unit selectByPrimaryKey(String syscode);

    int updateByPrimaryKeySelective(unit record);

    int updateByPrimaryKey(unit record);

    List<unit> findALLUnit();
}