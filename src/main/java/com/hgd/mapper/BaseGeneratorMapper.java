package com.hgd.mapper;

import com.hgd.entity.ColumnEntity;
import com.hgd.entity.TableEntity;

import java.util.List;
import java.util.Map;

/**************************************
 * Copyright (C), Navinfo
 * Package: 
 * @Author: 尚村山夫
 * @Date: Created in 2019/7/16 19:26
 * @Description:
 **************************************/
public interface BaseGeneratorMapper {

    List<TableEntity> queryList(Map<String, Object> map);

    int queryTotal(Map<String, Object> map);

    TableEntity queryTable(String tableName);

    List<ColumnEntity> queryColumns(String tableName);
}
