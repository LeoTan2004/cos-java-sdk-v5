package com.qcloud.cos.demo.ci;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.ciModel.snapshot.PrivateM3U8Request;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class LiveTranscodeDemo {
    private static String appId = "1251704708";
    private static String bucket = "markjrzhang-1251704708";
    private static String objectKey = "output/media/test.m3u8";
    private static String expires = "3600";
    private static byte[] secret = "ui0KZSbUbhApT2OjF1BNiU04FvqloQEL".getBytes();


    public static void main(String[] args) {
        COSClient client = ClientUtils.getTestClient();
        String url = generateCosDomainPrivateM3U8Url(client);
        System.out.println("Generated url: " + url);
    }

    public static String  generateCosDomainPrivateM3U8Url(COSClient client) {
        PrivateM3U8Request request = new PrivateM3U8Request();
        request.setBucketName(bucket);
        request.setObject(objectKey);
        request.setExpires(expires);
        String token =generateToken(appId, bucket, objectKey, secret,expires);
        request.setTokenType(token);
        return client.generateCosDomainPrivateM3U8Url(request);
    }

    public static String generateToken(String appId, String bucketId, String objectKey, byte[] secret, String expires) {

        Instant now = Instant.now();
        Instant expire = now.plus(6, ChronoUnit.DAYS);

        String encodedObjectKey;
        try {
            encodedObjectKey = URLEncoder.encode(objectKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Error encoding object key", e);
        }

        Algorithm algorithm = Algorithm.HMAC256(secret);
        JWTCreator.Builder builder = JWT.create().withIssuer("client").withIssuedAt(Date.from(now)).withExpiresAt(Date.from(expire)).withClaim("Type", "CosCiToken").withClaim("AppId", appId).withClaim("BucketId", bucketId).withClaim("Object", encodedObjectKey).withClaim("Issuer", "client").withClaim("IssuedTimeStamp", now.getEpochSecond()).withClaim("ExpireTimeStamp", expire.getEpochSecond()).withClaim("UsageLimit", 20).withClaim("ProtectSchema", "rsa1024")
//                .withClaim("PublicKey", "xxx")
                .withClaim("ProtectContentKey", 0);
        return builder.sign(algorithm);
    }


}
