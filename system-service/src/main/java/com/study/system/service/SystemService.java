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

    // ✅ 리전과 RDS 인스턴스 ID
    @Value("${aws.region}")
    private String region;

    @Value("${aws.rds-instance-id}")
    private String rdsInstanceId;

    /**
     * RDS 스냅샷 백업 생성
     */
    public void createBackup(Long adminId) {

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

    /**
     * 캐시 무효화
     */
    public void clearCache(Long adminId) {
        // TODO: Redis / CDN / LocalCache 등 연결 예정
        log.info("✅ Cache cleared by adminId={}", adminId);
    }
}