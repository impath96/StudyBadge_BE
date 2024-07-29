package com.tenten.studybadge.common.batch;

import com.tenten.studybadge.study.channel.domain.entity.StudyChannel;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class DepositRefundsBatchConfig {

    private final static String JOB_NAME = "depositRefundsBatchJob";
    private final static String SELECT_STUDY_CHANNEL = "SELECT sc FROM StudyChannel sc " +
            "WHERE sc.studyDuration.studyEndDate = :date ";

    private final EntityManagerFactory entityManagerFactory;
    private final AttendanceRatioProcessor attendanceRatioProcessor;
    private final AttendanceRatioWriter attendanceRatioWriter;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job depositRefundsJob() {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(depositRefundsStep())
                .build();
    }

    @Bean
    public Step depositRefundsStep() {
        return new StepBuilder(JOB_NAME + "_STEP", jobRepository)
                .<StudyChannel, StudyMemberAttendanceRatioList>chunk(10, transactionManager)
                .reader(itemReader())
                .processor(attendanceRatioProcessor)
                .writer(attendanceRatioWriter)
                .build();
    }

    @Bean
    public ItemReader<StudyChannel> itemReader() {
        LocalDate now = LocalDate.now();
        Map<String, Object> map = new HashMap<>();
        map.put("date", now.minusDays(1));
        return new JpaPagingItemReaderBuilder<StudyChannel>()
                .name(JOB_NAME + "_READER")
                .entityManagerFactory(entityManagerFactory)
                .queryString(SELECT_STUDY_CHANNEL)
                .parameterValues(map)
                .pageSize(10)
                .build();
    }
}
