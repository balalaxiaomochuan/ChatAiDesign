package com.huawei.chataidesign.config;

import jakarta.annotation.Resource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

@Configuration
@MapperScan("com.huawei.chataidesign.mapper")
public class MyBatisConfig {

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);

        // 设置 MyBatis 配置文件路径
//        sessionFactory.setConfigLocation(new PathMatchingResourcePatternResolver()
//                .getResource("classpath:mybatis-config.xml"));

        // 设置 Mapper XML 文件路径
        sessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources("classpath:mapper/*.xml"));

        // 设置实体类别名包
        sessionFactory.setTypeAliasesPackage("com.huawei.chataidesign.entity");

        return sessionFactory.getObject();
    }
}


