package com.study.system.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.CreateDbSnapshotRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemService {

    @Value("${aws.region}")
    private String region;

    @Value("${aws.rds-instance-id}")
    private String rdsInstanceId;

    @Value("${aws.backup.enabled:false}")
    private boolean backupEnabled;

    @Value("${system.cache.enabled:false}")
    private boolean cacheEnabled;

    public void createBackup(Long adminId) {

        if (!backupEnabled) {
            log.info("🔎 [LOCAL MODE] RDS Backup SKIPPED. adminId={}", adminId);
            return;
        }

        RdsClient rdsClient = RdsClient.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .region(Region.of(region))
                .build();

        String snapshotId = rdsInstanceId + "-snapshot-" + System.currentTimeMillis();

        CreateDbSnapshotRequest request = CreateDbSnapshotRequest.builder()
                .dbInstanceIdentifier(rdsInstanceId)
                .dbSnapshotIdentifier(snapshotId)
                .build();

        rdsClient.createDBSnapshot(request);

        log.info("✅ AWS RDS Snapshot Created by adminId={} snapshotId={}", adminId, snapshotId);
        rdsClient.close();
    }

    public void clearCache(Long adminId) {
        if (!cacheEnabled) {
            log.info("🔎 [LOCAL MODE] Cache clear SKIPPED. adminId={}", adminId);
            return;
        }

        log.info("✅ Cache cleared by adminId={}", adminId);
    }
}
