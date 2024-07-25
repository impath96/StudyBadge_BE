package com.tenten.studybadge.common.config;

import com.tenten.studybadge.common.quartz.AutowiringSpringBeanJobFactory;
import com.tenten.studybadge.common.quartz.SchedulerJobListener;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.quartz.Scheduler;
import org.quartz.spi.JobFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.autoconfigure.quartz.QuartzDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

@Configuration
@RequiredArgsConstructor
public class QuartzConfig {

    private final AutowireCapableBeanFactory beanFactory;

    @Value("${spring.quartz.properties.org.quartz.database.quartzDataSource.url}")
    private String quartzDataSourceUrl;

    @Value("${spring.quartz.properties.org.quartz.database.quartzDataSource.driver-class-name}")
    private String quartzDataSourceDriverClassName;

    @Value("${spring.quartz.properties.org.quartz.database.quartzDataSource.username}")
    private String quartzDataSourceUsername;

    @Value("${spring.quartz.properties.org.quartz.database.quartzDataSource.password}")
    private String quartzDataSourcePassword;

    @Bean
    public JobFactory jobFactory() {
      return new AutowiringSpringBeanJobFactory(beanFactory);
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(JobFactory jobFactory, DataSource quartzDataSource, SchedulerJobListener jobListener) {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setJobFactory(jobFactory);
        factory.setDataSource(quartzDataSource);
        factory.setGlobalJobListeners(jobListener);
        return factory;
    }

    @Bean
    @QuartzDataSource
    public DataSource quartzDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(quartzDataSourceDriverClassName);
        dataSource.setUrl(quartzDataSourceUrl);
        dataSource.setUsername(quartzDataSourceUsername);
        dataSource.setPassword(quartzDataSourcePassword);
        return dataSource;
    }

    @Bean
    public Scheduler scheduler(SchedulerFactoryBean schedulerFactoryBean) throws Exception {
        return schedulerFactoryBean.getScheduler();
    }

    @Bean
    public SchedulerJobListener jobListener() {
        return new SchedulerJobListener();
    }
}