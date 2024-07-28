package com.tenten.studybadge.common.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DepositRefundsBatchScheduler {

    private final JobLauncher jobLauncher;
    private final DepositRefundsBatchConfig config;

    @Scheduled(cron = "0 0 6 * * ?")
    public void execute() throws JobExecutionException {
        log.info("execute time : {}", System.currentTimeMillis());
        try {
            JobParameters parameters = new JobParametersBuilder()
                    .addLocalDateTime("date", LocalDateTime.now())
                    .toJobParameters();
            jobLauncher.run(config.depositRefundsJob(), parameters);
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
    }

}
