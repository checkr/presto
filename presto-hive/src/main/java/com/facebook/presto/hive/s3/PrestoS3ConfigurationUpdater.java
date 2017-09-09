/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.hive.s3;

import com.facebook.presto.hive.S3ConfigurationUpdater;
import io.airlift.units.DataSize;
import io.airlift.units.Duration;
import org.apache.hadoop.conf.Configuration;

import javax.inject.Inject;

import java.io.File;

public class PrestoS3ConfigurationUpdater
        implements S3ConfigurationUpdater
{
    private final String awsAccessKey;
    private final String awsSecretKey;
    private final String endpoint;
    private final PrestoS3SignerType signerType;
    private final boolean pathStyleAccess;
    private final boolean useInstanceCredentials;
    private final boolean sslEnabled;
    private final boolean sseEnabled;
    private final PrestoS3SseType sseType;
    private final String encryptionMaterialsProvider;
    private final String kmsKeyId;
    private final String sseKmsKeyId;
    private final int maxClientRetries;
    private final int maxErrorRetries;
    private final Duration maxBackoffTime;
    private final Duration maxRetryTime;
    private final Duration connectTimeout;
    private final Duration socketTimeout;
    private final int maxConnections;
    private final DataSize multipartMinFileSize;
    private final DataSize multipartMinPartSize;
    private final File stagingDirectory;
    private final boolean pinClientToCurrentRegion;
    private final String userAgentPrefix;

    @Inject
    public PrestoS3ConfigurationUpdater(HiveS3Config config)
    {
        this.awsAccessKey = config.getS3AwsAccessKey();
        this.awsSecretKey = config.getS3AwsSecretKey();
        this.endpoint = config.getS3Endpoint();
        this.signerType = config.getS3SignerType();
        this.pathStyleAccess = config.isS3PathStyleAccess();
        this.useInstanceCredentials = config.isS3UseInstanceCredentials();
        this.sslEnabled = config.isS3SslEnabled();
        this.sseEnabled = config.isS3SseEnabled();
        this.sseType = config.getS3SseType();
        this.encryptionMaterialsProvider = config.getS3EncryptionMaterialsProvider();
        this.kmsKeyId = config.getS3KmsKeyId();
        this.sseKmsKeyId = config.getS3SseKmsKeyId();
        this.maxClientRetries = config.getS3MaxClientRetries();
        this.maxErrorRetries = config.getS3MaxErrorRetries();
        this.maxBackoffTime = config.getS3MaxBackoffTime();
        this.maxRetryTime = config.getS3MaxRetryTime();
        this.connectTimeout = config.getS3ConnectTimeout();
        this.socketTimeout = config.getS3SocketTimeout();
        this.maxConnections = config.getS3MaxConnections();
        this.multipartMinFileSize = config.getS3MultipartMinFileSize();
        this.multipartMinPartSize = config.getS3MultipartMinPartSize();
        this.stagingDirectory = config.getS3StagingDirectory();
        this.pinClientToCurrentRegion = config.isPinS3ClientToCurrentRegion();
        this.userAgentPrefix = config.getS3UserAgentPrefix();
    }

    @Override
    public void updateConfiguration(Configuration config)
    {
        // re-map filesystem schemes to match Amazon Elastic MapReduce
        config.set("fs.s3.impl", PrestoS3FileSystem.class.getName());
        config.set("fs.s3a.impl", PrestoS3FileSystem.class.getName());
        config.set("fs.s3n.impl", PrestoS3FileSystem.class.getName());

        if (awsAccessKey != null) {
            config.set(PrestoS3FileSystem.S3_ACCESS_KEY, awsAccessKey);
        }
        if (awsSecretKey != null) {
            config.set(PrestoS3FileSystem.S3_SECRET_KEY, awsSecretKey);
        }
        if (endpoint != null) {
            config.set(PrestoS3FileSystem.S3_ENDPOINT, endpoint);
        }
        if (signerType != null) {
            config.set(PrestoS3FileSystem.S3_SIGNER_TYPE, signerType.name());
        }
        config.setBoolean(PrestoS3FileSystem.S3_PATH_STYLE_ACCESS, pathStyleAccess);
        config.setBoolean(PrestoS3FileSystem.S3_USE_INSTANCE_CREDENTIALS, useInstanceCredentials);
        config.setBoolean(PrestoS3FileSystem.S3_SSL_ENABLED, sslEnabled);
        config.setBoolean(PrestoS3FileSystem.S3_SSE_ENABLED, sseEnabled);
        config.set(PrestoS3FileSystem.S3_SSE_TYPE, sseType.name());
        if (encryptionMaterialsProvider != null) {
            config.set(PrestoS3FileSystem.S3_ENCRYPTION_MATERIALS_PROVIDER, encryptionMaterialsProvider);
        }
        if (kmsKeyId != null) {
            config.set(PrestoS3FileSystem.S3_KMS_KEY_ID, kmsKeyId);
        }
        if (sseKmsKeyId != null) {
            config.set(PrestoS3FileSystem.S3_SSE_KMS_KEY_ID, sseKmsKeyId);
        }
        config.setInt(PrestoS3FileSystem.S3_MAX_CLIENT_RETRIES, maxClientRetries);
        config.setInt(PrestoS3FileSystem.S3_MAX_ERROR_RETRIES, maxErrorRetries);
        config.set(PrestoS3FileSystem.S3_MAX_BACKOFF_TIME, maxBackoffTime.toString());
        config.set(PrestoS3FileSystem.S3_MAX_RETRY_TIME, maxRetryTime.toString());
        config.set(PrestoS3FileSystem.S3_CONNECT_TIMEOUT, connectTimeout.toString());
        config.set(PrestoS3FileSystem.S3_SOCKET_TIMEOUT, socketTimeout.toString());
        config.set(PrestoS3FileSystem.S3_STAGING_DIRECTORY, stagingDirectory.toString());
        config.setInt(PrestoS3FileSystem.S3_MAX_CONNECTIONS, maxConnections);
        config.setLong(PrestoS3FileSystem.S3_MULTIPART_MIN_FILE_SIZE, multipartMinFileSize.toBytes());
        config.setLong(PrestoS3FileSystem.S3_MULTIPART_MIN_PART_SIZE, multipartMinPartSize.toBytes());
        config.setBoolean(PrestoS3FileSystem.S3_PIN_CLIENT_TO_CURRENT_REGION, pinClientToCurrentRegion);
        config.set(PrestoS3FileSystem.S3_USER_AGENT_PREFIX, userAgentPrefix);
    }
}
