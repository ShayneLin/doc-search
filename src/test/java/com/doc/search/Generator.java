package com.doc.search;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.util.Collections;

public class Generator {
    public static void main(String[] args) {
        String outputDir = "D://";
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        hikariConfig.setJdbcUrl("jdbc:mysql://localhost:13306/test");
        hikariConfig.setUsername("root");
        hikariConfig.setPassword("123456");
        DataSource dataSource = new HikariDataSource(hikariConfig);
        DataSourceConfig.Builder dataSourceBuilder = new DataSourceConfig.Builder(dataSource);
//        FastAutoGenerator.create("jdbc:h2:file:./db/doc_index_db", "root", "123456")
        FastAutoGenerator.create(dataSourceBuilder)
                .globalConfig(builder -> {
                    builder.author("lcs") // 设置作者
                            .enableSwagger() // 开启 swagger 模式
                            .fileOverride() // 覆盖已生成文件
                            .outputDir(outputDir); // 指定输出目录
                })
                .packageConfig(builder -> {
                    builder.parent("com.doc.search") // 设置父包名
                            .pathInfo(Collections.singletonMap(OutputFile.xml, outputDir)); // 设置mapperXml生成路径
                })
                .strategyConfig(builder -> {
                    builder.addInclude("t_document") // 设置需要生成的表名
                            .addTablePrefix("t_", "c_"); // 设置过滤表前缀
                })
                .templateEngine(new FreemarkerTemplateEngine()) // 使用Freemarker引擎模板，默认的是Velocity引擎模板
                .execute();

    }
}
