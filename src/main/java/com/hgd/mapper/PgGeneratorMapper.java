package com.hgd.mapper;

import com.hgd.entity.ColumnEntity;
import com.hgd.entity.TableEntity;

import java.util.List;
import java.util.Map;

/**************************************
 * Copyright (C), Navinfo
 * Package: 
 * @Author: 尚村山夫
 * @Date: Created in 2019/7/16 19:25
 * @Description:
 **************************************/
public interface PgGeneratorMapper  extends BaseGeneratorMapper{

    @Override
    List<TableEntity> queryList(Map<String, Object> map);

    @Override
    int queryTotal(Map<String, Object> map);

    @Override
    TableEntity queryTable(String tableName);

    @Override
    List<ColumnEntity> queryColumns(String tableName);
}
