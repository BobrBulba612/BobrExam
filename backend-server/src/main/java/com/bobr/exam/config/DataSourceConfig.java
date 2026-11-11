package com.bobr.exam.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Configuration
public class DataSourceConfig {

    private static final Pattern PG_URI = Pattern.compile(
            "^(?:postgres|postgresql)://([^:]+):([^@]+)@([^:/]+)(?::(\\d+))?/([^?]+)(\\?.*)?$");

    @Value("${spring.datasource.url:jdbc:h2:mem:bobrdb}")
    private String rawUrl;

    @Value("${spring.datasource.username:sa}")
    private String rawUser;

    @Value("${spring.datasource.password:}")
    private String rawPassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariDataSource ds = new HikariDataSource();
        String url = rawUrl == null ? "" : rawUrl.trim();

        if (url.startsWith("jdbc:")) {
            ds.setJdbcUrl(url);
            ds.setUsername(rawUser);
            ds.setPassword(rawPassword);
            ds.setDriverClassName(url.startsWith("jdbc:h2:") ? "org.h2.Driver" : "org.postgresql.Driver");
            System.out.println("[BobrExam] JDBC as-is: " + url);
        } else {
            Matcher m = PG_URI.matcher(url);
            if (!m.matches()) {
                throw new IllegalStateException("Unsupported datasource url: " + url);
            }
            String user = m.group(1);
            String pass = m.group(2);
            String host = m.group(3);
            String port = m.group(4) == null ? "5432" : m.group(4);
            String db   = m.group(5);
            String qs   = m.group(6) == null ? "" : m.group(6);

            String jdbc = "jdbc:postgresql://" + host + ":" + port + "/" + db + qs;
            ds.setJdbcUrl(jdbc);
            ds.setUsername(user);
            ds.setPassword(pass);
            ds.setDriverClassName("org.postgresql.Driver");
            System.out.println("[BobrExam] Converted to JDBC: " + jdbc);
        }

        ds.setMaximumPoolSize(5);
        ds.setMinimumIdle(1);
        ds.setConnectionTimeout(30000);
        return ds;
    }
}
