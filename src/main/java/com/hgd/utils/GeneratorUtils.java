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

package com.hgd.utils;

import com.hgd.entity.ColumnEntity;
import com.hgd.entity.TableEntity;
import com.hgd.proxy.GeneratorProxy;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.springframework.core.env.Environment;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 代码生成器   工具类
 *
 * @author chenshun
 * @version 1.0
 * @email sunlightcs@gmail.com
 */
public class GeneratorUtils {

    private static Environment env;

    static{
        env = SpringContextUtil.getBeanByType(Environment.class);
    }

    private static List<String> getTemplates() {
        List<String> templates = new ArrayList<>();

        Environment environment = env;
        String ignore = environment.getProperty("vm-template.ignore");

        List<String> ignoreTemplate;
        if (org.apache.commons.lang3.StringUtils.isNotBlank(ignore)) {
            ignoreTemplate = Arrays.asList(org.apache.commons.lang3.StringUtils.split(ignore, ","));
        } else {
            ignoreTemplate = new ArrayList<>();
        }

        if (!ignoreTemplate.contains("index.js")) {
            templates.add("template/index.js.vm");
        }
        if (!ignoreTemplate.contains("index.vue")) {
            templates.add("template/index.vue.vm");
        }
        if (!ignoreTemplate.contains("mapper.xml")) {
            templates.add("template/mapper.xml.vm");
        }
        if (!ignoreTemplate.contains("biz.java")) {
            templates.add("template/biz.java.vm");
        }
        if (!ignoreTemplate.contains("entity.java")) {
            templates.add("template/entity.java.vm");
        }
        if (!ignoreTemplate.contains("mapper.java")) {
            templates.add("template/mapper.java.vm");
        }
        if (!ignoreTemplate.contains("controller.java")) {
            templates.add("template/controller.java.vm");
        }
        return templates;
    }

    /**
     * 生成代码
     */
    public static void generatorCode(TableEntity table, List<ColumnEntity> columns, ZipOutputStream zip,
        GeneratorProxy generatorProxy) {
        //配置信息
        Configuration config = getConfig(generatorProxy);
        //表信息
        //表名转换成Java类名
        String className = tableToJava(table.getTableName(), getCommonProperty("tablePrefix"));
        table.setClassName(className);
        table.setClassname(StringUtils.uncapitalize(className));

        //列信息
        List<ColumnEntity> columsList = new ArrayList<>();
        for (ColumnEntity column : columns) {
            //列名转换成Java属性名
            String attrName = columnToJava(column.getColumnName());
            column.setAttrName(attrName);
            column.setAttrname(StringUtils.uncapitalize(attrName));

            generatorProxy.convertColumnDataType(column);

            /*
             * 列的数据类型，转换成Java类型
             * 在columns中封装了一系列数据库中的类型，如
             * smallint
             * integer
             * boolean
             * json
             *
             * 需要将数据库类型与java类型进行对应
             * 如
             * samllint Integer
             * integer  Integer
             * boolean  Boolean
             * json     Object
             */
            String attrType = config.getString(column.getDataType(), "unknowType");
            column.setAttrType(attrType);

            boolean pkey = ("PRI".equalsIgnoreCase(column.getColumnKey()) || org.apache.commons.lang3.StringUtils
                .endsWithIgnoreCase(column.getColumnKey(), "_pkey")) && table.getPk() == null;
            //是否主键
            if (pkey) {
                table.setPk(column);
            }

            columsList.add(column);
        }
        table.setColumns(columsList);

        //没主键，则第一个字段为主键
        if (table.getPk() == null) {
            table.setPk(table.getColumns().get(0));
        }

        //设置velocity资源加载器
        Properties prop = new Properties();
        prop.put("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init(prop);

        //封装模板数据
        Map<String, Object> map = new HashMap<>();
        map.put("tableName", nullToEmpty(table.getTableName()));
        map.put("tableComment", nullToEmpty(table.getTableComment()));
        map.put("pk", table.getPk());
        map.put("className", nullToEmpty(nullToEmpty(table.getClassName())));
        map.put("classname", nullToEmpty(table.getClassname()));
        map.put("pathName", nullToEmpty(table.getClassname().toLowerCase()));
        map.put("columns", table.getColumns());
        map.put("package", nullToEmpty(getCommonProperty("package.basePackage")));
        map.put("author", nullToEmpty(getCommonProperty("author")));
        map.put("email", nullToEmpty(getCommonProperty("email")));

        map.put("commonBusinessBiz", nullToEmpty(getCommonProperty("commonBusinessBiz")));
        map.put("commonMapper", nullToEmpty(getCommonProperty("commonMapper")));
        map.put("commonController", nullToEmpty(getCommonProperty("commonController")));

        map.put("datetime", nullToEmpty(DateUtils.format(new Date(), DateUtils.DATE_TIME_PATTERN)));
        map.put("moduleName", nullToEmpty(getCommonProperty("mainModule")));
        map.put("secondModuleName", nullToEmpty(toLowerCaseFirstOne(className)));

        map.put("lowerBizPackage", nullToEmpty(toLowerCaseFirstOne(getCommonProperty("package.bizPackage"))));
        map.put("upperBizPackage", nullToEmpty(toUpperCaseFirstOne(getCommonProperty("package.bizPackage"))));

        map.put("lowerEntityPackage", nullToEmpty(toLowerCaseFirstOne(getCommonProperty("package.entityPackage"))));

        map.put("lowerControllerPackage", nullToEmpty(toLowerCaseFirstOne(getCommonProperty("package.controllerPackage"))));
        map.put("upperControllerPackage", nullToEmpty(toUpperCaseFirstOne(getCommonProperty("package.controllerPackage"))));

        map.put("lowerMapperPackage", nullToEmpty(toLowerCaseFirstOne(getCommonProperty("package.mapperPackage"))));
        map.put("upperMapperPackage", nullToEmpty(toUpperCaseFirstOne(getCommonProperty("package.mapperPackage"))));

        VelocityContext context = new VelocityContext(map);

        //获取模板列表
        List<String> templates = getTemplates();
        for (String template : templates) {
            //渲染模板
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, "UTF-8");
            tpl.merge(context, sw);

            try {
                //添加到zip
                zip.putNextEntry(new ZipEntry(Objects.requireNonNull(
                    getFileName(template, table.getClassName(), config.getString("package"),
                        config.getString("mainModule")))));
                IOUtils.write(sw.toString(), zip, "UTF-8");
                IOUtils.closeQuietly(sw);
                zip.closeEntry();
            } catch (IOException e) {
                throw new RuntimeException("渲染模板失败，表名：" + table.getTableName(), e);
            }
        }
    }

    private static String nullToEmpty(String s){
        if(null == s){
            return "";
        }

        return s;
    }


    private static String getCommonProperty(String dataType) {
        String property = EnvironmentUtil.getProperty(env, dataType);
        if (null == property) {
            throw new RuntimeException("渲染模板失败，缺少配置属性：" + dataType);
        }
        return property;
    }

    /**
     * 列名转换成Java属性名
     */
    private static String columnToJava(String columnName) {
        return WordUtils.capitalizeFully(columnName, new char[]{ '_' }).replace("_", "");
    }

    /**
     * 表名转换成Java类名
     */
    private static String tableToJava(String tableName, String tablePrefix) {
        if (StringUtils.isNotBlank(tablePrefix)) {
            tableName = tableName.replace(tablePrefix, "");
        }
        return columnToJava(tableName);
    }

    /**
     * 获取配置信息
     */
    private static Configuration getConfig(GeneratorProxy generatorProxy) {
        try {
            return new PropertiesConfiguration(generatorProxy.getPropertiesPath());
        } catch (ConfigurationException e) {
            throw new RuntimeException("获取配置文件失败，", e);
        }
    }

    /**
     * 获取文件名
     */
    private static String getFileName(String template, String className, String packageName, String moduleName) {
        String packagePath = File.separator + "main" + File.separator + "java" + File.separator;
        String frontPath = "ui" + File.separator;
        if (StringUtils.isNotBlank(packageName)) {
            packagePath += packageName.replace(".", File.separator) + File.separator;
        }

        if (template.contains("index.js.vm")) {
            return frontPath + "api" + File.separator + moduleName + File.separator + toLowerCaseFirstOne(className)
                + File.separator + "index.js";
        }

        if (template.contains("index.vue.vm")) {
            return frontPath + "views" + File.separator + moduleName + File.separator + toLowerCaseFirstOne(className)
                + File.separator + "index.vue";
        }

        if (template.contains("biz.java.vm")) {
            return packagePath + getCommonProperty("package.bizPackage") + File.separator + className
                + toUpperCaseFirstOne(getCommonProperty("package.bizPackage")) + ".java";
        }
        if (template.contains("mapper.java.vm")) {
            return packagePath + getCommonProperty("package.mapperPackage") + File.separator + className
                + toUpperCaseFirstOne(getCommonProperty("package.mapperPackage")) + ".java";
        }
        if (template.contains("entity.java.vm")) {
            return packagePath + getCommonProperty("package.entityPackage") + File.separator + className + ".java";
        }
        if (template.contains("controller.java.vm")) {
            return packagePath + getCommonProperty("package.controllerPackage") + File.separator + className
                + toUpperCaseFirstOne(getCommonProperty("package.controllerPackage")) + ".java";
        }
        if (template.contains("mapper.xml.vm")) {
            return "main" + File.separator + "resources" + File.separator + getCommonProperty("package.mapperXMLFolder")
                + File.separator + className + "Mapper.xml";
        }

        return null;
    }

    //首字母转小写
    private static String toLowerCaseFirstOne(String s) {
        if (Character.isLowerCase(s.charAt(0))) {
            return s;
        } else {
            return Character.toLowerCase(s.charAt(0)) + s.substring(1);
        }
    }

    //首字母转小写
    private static String toUpperCaseFirstOne(String s) {
        if (Character.isUpperCase(s.charAt(0))) {
            return s;
        } else {
            return Character.toUpperCase(s.charAt(0)) + s.substring(1);
        }
    }
}
