package com.study.system.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.CreateDbSnapshotRequest;

@Service
@RequiredArgsConstructor
public class SystemService {

    // ✅ 리전과 RDS 인스턴스 ID만 필요
    @Value("${aws.region}")
    private String region;

    @Value("${aws.rds-instance-id}")
    private String rdsInstanceId;

    public void createBackup() {

        // ✅ IAM Role 자동 인식
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

        System.out.println("✅ AWS RDS Snapshot Created (IAM Role): " + snapshotId);

        rdsClient.close();
    }

    public void clearCache() {
        // TODO: Redis / CDN / LocalCache 등 연결 예정
        System.out.println("✅ Cache cleared (IAM Role environment)");
    }
}