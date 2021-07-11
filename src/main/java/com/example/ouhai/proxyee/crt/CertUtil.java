package com.example.ouhai.proxyee.crt;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class CertUtil {

    private static KeyFactory keyFactory = null;

    static {
        //注册BouncyCastleProvider加密库
        //不注册会报BC异常
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * 动态生成服务器证书,并进行CA签授
     *
     * @param issuer 颁发机构
     */
    public static X509Certificate genCert(String issuer, PrivateKey caPriKey, Date caNotBefore,
                                          Date caNotAfter, PublicKey serverPubKey,
                                          String... hosts) throws Exception {
        /* String issuer = "C=CN, ST=GD, L=SZ, O=lee, OU=study, CN=ProxyeeRoot";
        String subject = "C=CN, ST=GD, L=SZ, O=lee, OU=study, CN=" + host;*/
        //根据CA证书subject来动态生成目标服务器证书的issuer和subject
        // 核心流程：host相关的特殊string + caPri + crt证书的相关信息 + 随机PubKey 返回给客户端
        String subject = Stream.of(issuer.split(", ")).map(item -> {
            String[] arr = item.split("=");
            if ("CN".equals(arr[0])) {
                return "CN=" + hosts[0];
            } else {
                return item;
            }
        }).collect(Collectors.joining(", "));

        //doc from https://www.cryptoworkshop.com/guide/
        JcaX509v3CertificateBuilder jv3Builder = new JcaX509v3CertificateBuilder(new X500Name(issuer),
                //issue#3 修复ElementaryOS上证书不安全问题(serialNumber为1时证书会提示不安全)，避免serialNumber冲突，采用时间戳+4位随机数生成
                BigInteger.valueOf(System.currentTimeMillis() + (long) (Math.random() * 10000) + 1000),
                caNotBefore,
                caNotAfter,
                new X500Name(subject),
                serverPubKey);
        //SAN扩展证书支持的域名，否则浏览器提示证书不安全
        GeneralName[] generalNames = new GeneralName[hosts.length];
        for (int i = 0; i < hosts.length; i++) {
            generalNames[i] = new GeneralName(GeneralName.dNSName, hosts[i]);
        }
        GeneralNames subjectAltName = new GeneralNames(generalNames);
        jv3Builder.addExtension(Extension.subjectAlternativeName, false, subjectAltName);
        //SHA256 用SHA1浏览器可能会提示证书不安全
        ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSAEncryption").build(caPriKey);
        return new JcaX509CertificateConverter().getCertificate(jv3Builder.build(signer));
    }

    /**
     * 从文件加载证书
     */
    public static X509Certificate loadCert(InputStream inputStream) throws CertificateException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return (X509Certificate) cf.generateCertificate(inputStream);
    }

    /**
     * 从文件加载RSA私钥 openssl pkcs8 -topk8 -nocrypt -inform PEM -outform DER -in ca.key -out
     * ca_private.der
     */
    public static PrivateKey loadPriKey(byte[] bts)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(bts);
        return getKeyFactory().generatePrivate(privateKeySpec);
    }

    private static KeyFactory getKeyFactory() throws NoSuchAlgorithmException {
        if (keyFactory == null) {
            keyFactory = KeyFactory.getInstance("RSA");
        }
        return keyFactory;
    }

    /**
     * 从文件加载RSA私钥 openssl pkcs8 -topk8 -nocrypt -inform PEM -outform DER -in ca.key -out
     * ca_private.der
     */
    public static PrivateKey loadPriKey(InputStream inputStream)
            throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] bts = new byte[1024];
        int len;
        while ((len = inputStream.read(bts)) != -1) {
            outputStream.write(bts, 0, len);
        }
        inputStream.close();
        outputStream.close();
        return loadPriKey(outputStream.toByteArray());
    }

    /**
     * 读取ssl证书使用者信息
     */
    public static String getSubject(X509Certificate certificate) throws Exception {
        //读出来顺序是反的需要反转下
        List<String> tempList = Arrays.asList(certificate.getIssuerDN().toString().split(", "));
        return IntStream.rangeClosed(0, tempList.size() - 1)
                .mapToObj(i -> tempList.get(tempList.size() - i - 1)).collect(Collectors.joining(", "));
    }

    /**
     * 生成RSA公私密钥对,长度为2048
     */
    public static KeyPair genKeyPair() throws Exception {
        KeyPairGenerator caKeyPairGen = KeyPairGenerator.getInstance("RSA", "BC");
        caKeyPairGen.initialize(2048, new SecureRandom());
        return caKeyPairGen.genKeyPair();
    }
}
