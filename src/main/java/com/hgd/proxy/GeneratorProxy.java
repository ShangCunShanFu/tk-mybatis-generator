package com.hgd.proxy;

import com.hgd.entity.ColumnEntity;
import com.hgd.entity.TableEntity;
import com.hgd.mapper.BaseGeneratorMapper;
import com.hgd.mapper.MysqlGeneratorMapper;
import com.hgd.mapper.PgGeneratorMapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**************************************
 * Copyright (C), Navinfo
 * Package: 
 * @Author: 尚村山夫
 * @Date: Created in 2019/7/16 19:39
 * @Description:
 **************************************/
@Component
public class GeneratorProxy implements BaseGeneratorMapper {

    @Autowired
    private MysqlGeneratorMapper mysqlGeneratorMapper;

    @Autowired
    private PgGeneratorMapper pgGeneratorMapper;

    public Environment getEnvironment() {
        return environment;
    }

    @Autowired
    private Environment environment;

    private Map<String, Object> getGeneratorConfig() {
        String driverClassName = environment.getProperty("spring.datasource.driverClassName");

        Map<String, Object> config = new HashMap<>(8);
        switch (driverClassName) {
            case "org.postgresql.Driver":
                // pg
                config.put("baseGeneratorMapper", pgGeneratorMapper);
                config.put("generator-properties", "properties/pg-generator.properties");
                break;
            case "com.mysql.cj.jdbc.Driver":
            case "com.mysql.jdbc.Driver":
                // mysql
                config.put("baseGeneratorMapper", mysqlGeneratorMapper);
                config.put("generator-properties", "properties/mysql-generator.properties");
                break;
            default:
        }

        return config;
    }

    /**
     * 获取配置文件路径
     * @return
     */
    public String getPropertiesPath(){
        return (String) getGeneratorConfig().get("generator-properties");
    }

    public void convertColumnDataType(ColumnEntity column) {
        String dataType = column.getDataType();
        String metaData = (String) getGeneratorConfig().get("generator-properties");

        switch (metaData) {
            case "properties/pg-generator.properties":
                if (StringUtils.startsWith(dataType, "time")) {
                    column.setDataType("date");
                }
                if (StringUtils.startsWith(dataType, "character")) {
                    column.setDataType("character");
                }
                break;
            case "properties/mysql-generator.properties":
                break;
            default:
        }
    }

    @Override
    public List<TableEntity> queryList(Map<String, Object> map) {
        BaseGeneratorMapper generatorMapper = (BaseGeneratorMapper) getGeneratorConfig().get("baseGeneratorMapper");

        long offset = Long.parseLong(map.get("offset").toString());
        long limit = Long.parseLong(map.get("limit").toString());
        map.put("offset", offset);
        map.put("limit", limit);

        assert generatorMapper != null;
        return generatorMapper.queryList(map);
    }

    @Override
    public int queryTotal(Map<String, Object> map) {
        BaseGeneratorMapper generatorMapper = (BaseGeneratorMapper) getGeneratorConfig().get("baseGeneratorMapper");
        return generatorMapper.queryTotal(map);
    }

    @Override
    public TableEntity queryTable(String tableName) {
        BaseGeneratorMapper generatorMapper = (BaseGeneratorMapper) getGeneratorConfig().get("baseGeneratorMapper");
        assert generatorMapper != null;
        return generatorMapper.queryTable(tableName);
    }

    @Override
    public List<ColumnEntity> queryColumns(String tableName) {
        BaseGeneratorMapper generatorMapper = (BaseGeneratorMapper) getGeneratorConfig().get("baseGeneratorMapper");
        assert generatorMapper != null;
        return generatorMapper.queryColumns(tableName);
    }
}
