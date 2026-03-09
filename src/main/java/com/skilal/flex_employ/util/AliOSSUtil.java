package com.skilal.flex_employ.util;

import com.aliyun.oss.*;
import com.aliyun.oss.common.auth.*;
import com.aliyun.oss.common.comm.SignVersion;
import com.aliyun.oss.model.PutObjectRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Slf4j
@Component
//改类名了，所以oss的自定义配置类没有报错
public class AliOSSUtil {

    // 作为 starter 的配置项）
//    private final String endpoint = "https://oss-cn-hangzhou.aliyuncs.com";
//    private final String bucketName = "skil-bucket01";
//    private final String region = "cn-hangzhou";
    @Autowired
    private AliOSSProperties aliOssProperties;

    /**
     * 上传文件流到 OSS
     * 
     * @param inputStream 文件流
     * @param objectName  在 OSS 中的完整路径名
     * @return 上传结果
     */
    public String upload(InputStream inputStream, String objectName) {
        String endpoint = aliOssProperties.getEndpoint();
        String bucketName = aliOssProperties.getBucketName();
        String region = aliOssProperties.getRegion();
        OSS ossClient = null;
        try {
            // 【关键点】：为了解决 Unhandled exception 报警，必须在内部 try-catch
            // 从环境变量获取凭证（务必确保本地已配置好 OSS_ACCESS_KEY_ID 和 OSS_ACCESS_KEY_SECRET）
            EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory
                    .newEnvironmentVariableCredentialsProvider();

            // 创建配置
            ClientBuilderConfiguration conf = new ClientBuilderConfiguration();
            conf.setSignatureVersion(SignVersion.V4);

            // 创建客户端
            ossClient = OSSClientBuilder.create()
                    .endpoint(endpoint)
                    .credentialsProvider(credentialsProvider)
                    .clientConfiguration(conf)
                    .region(region)
                    .build();

            // 发起请求
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, inputStream);
            ossClient.putObject(putObjectRequest);

            // 返回文件访问链接（根据你的 Bucket 权限和域名拼接）
            return "https://" + bucketName + "." + endpoint.replace("https://", "") + "/" + objectName;

        } catch (Exception e) {
            log.error("OSS 上传失败: {}", e.getMessage());
            throw new RuntimeException("文件上传至云端失败", e);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}
