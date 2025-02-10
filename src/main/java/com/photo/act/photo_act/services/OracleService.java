package com.photo.act.photo_act.services;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

@Service
public class OracleService {

    public DataSource getDataSource() {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName("oracle.jdbc.OracleDriver");
        dataSourceBuilder.url("jdbc:oracle:thin:@photoact.net:30333/FREEPDB1");
        dataSourceBuilder.username("system");
        dataSourceBuilder.password("mariadb");
        return dataSourceBuilder.build();
    }
}
