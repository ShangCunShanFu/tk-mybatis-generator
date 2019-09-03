/*
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.

 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.hgd.service;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.hgd.entity.ColumnEntity;
import com.hgd.entity.TableEntity;
import com.hgd.mapper.MysqlGeneratorMapper;
import com.hgd.proxy.GeneratorProxy;
import com.hgd.utils.GeneratorUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;

/**
 * 代码生成器
 *
 * @author Mr.AG
 * @version 1.0
 * @email 576866311@qq.com
 */
@Service
public class GeneratorService {

    @Autowired
    private MysqlGeneratorMapper mysqlGeneratorMapper;

    @Autowired
    private GeneratorProxy generatorProxy;

    public List<TableEntity> queryList(Map<String, Object> map) {
		JSONObject param = new JSONObject(map);
		PageHelper.startPage(param.getIntValue("offset"),param.getIntValue("limit"));
		List<TableEntity> tableEntities = generatorProxy.queryList(map);
        for (TableEntity tableEntity : tableEntities) {
            if (StringUtils.isBlank(tableEntity.getTableComment())) {
                tableEntity.setTableComment(tableEntity.getTableName());
            }
        }
        return tableEntities;
    }

    public TableEntity queryTable(String tableName) {
        return generatorProxy.queryTable(tableName);
    }

    public List<ColumnEntity> queryColumns(String tableName) {
        return generatorProxy.queryColumns(tableName);
    }

    public byte[] generatorCode(String[] tableNames) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(outputStream);

        for (String tableName : tableNames) {
            //查询表信息
            TableEntity table = queryTable(tableName);
            //查询列信息
            List<ColumnEntity> columns = queryColumns(tableName);
            //生成代码
            GeneratorUtils.generatorCode(table, columns, zip, generatorProxy);
        }
        IOUtils.closeQuietly(zip);
        return outputStream.toByteArray();
    }

    public int queryTotal(Map<String, Object> params) {
        return generatorProxy.queryTotal(params);
    }
}
