package site.geekie.shop.shoppingmall.config;

import com.alipay.api.FileItem;
import com.alipay.api.internal.util.AbstractHttpClient;
import okhttp3.*;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 自定义支付宝 HTTP 客户端，强制使用 TLSv1.2 协议。
 * <p>
 * 原因：支付宝 SDK 默认的 HttpClientUtil 使用 SSLContext.getInstance("TLS")，
 * 在 Java 21 中会优先协商 TLSv1.3，但支付宝沙箱环境不完全兼容 TLSv1.3，
 * 导致 SSLHandshakeException: Remote host terminated the handshake。
 * <p>
 * 通过 AlipayConfig.setCustomizedHttpClient() 注入本类，替换 SDK 默认的 HTTP 实现。
 */
public class Tls12AlipayHttpClient extends AbstractHttpClient {

    private final OkHttpClient client;

    public Tls12AlipayHttpClient(int connectTimeout, int readTimeout) {
        try {
            X509TrustManager trustManager = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            };

            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, new TrustManager[]{trustManager}, new SecureRandom());

            client = new OkHttpClient.Builder()
                    .sslSocketFactory(sslContext.getSocketFactory(), trustManager)
                    .hostnameVerifier((hostname, session) -> true)
                    .connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
                    .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create TLSv1.2 HTTP client for Alipay", e);
        }
    }

    @Override
    public String doPost(String url, Map<String, String> params, String charset,
                         Map<String, String> headers) throws IOException {
        FormBody.Builder formBuilder = new FormBody.Builder();
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (entry.getValue() != null) {
                    formBuilder.add(entry.getKey(), entry.getValue());
                }
            }
        }

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(formBuilder.build());

        applyHeaders(requestBuilder, headers);

        try (Response response = client.newCall(requestBuilder.build()).execute()) {
            ResponseBody body = response.body();
            return body != null ? body.string() : "";
        }
    }

    @Override
    public String doPost(String url, Map<String, String> params,
                         Map<String, FileItem> fileParams, String charset,
                         Map<String, String> headers) throws IOException {
        MultipartBody.Builder multipartBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);

        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (entry.getValue() != null) {
                    multipartBuilder.addFormDataPart(entry.getKey(), entry.getValue());
                }
            }
        }

        if (fileParams != null) {
            for (Map.Entry<String, FileItem> entry : fileParams.entrySet()) {
                FileItem fileItem = entry.getValue();
                byte[] content = fileItem.getContent();
                String mimeType = fileItem.getMimeType();
                MediaType mediaType = mimeType != null ? MediaType.parse(mimeType) : MediaType.parse("application/octet-stream");
                multipartBuilder.addFormDataPart(
                        entry.getKey(),
                        fileItem.getFileName(),
                        RequestBody.create(mediaType, content)
                );
            }
        }

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(multipartBuilder.build());

        applyHeaders(requestBuilder, headers);

        try (Response response = client.newCall(requestBuilder.build()).execute()) {
            ResponseBody body = response.body();
            return body != null ? body.string() : "";
        }
    }

    private void applyHeaders(Request.Builder requestBuilder, Map<String, String> headers) {
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                if (entry.getValue() != null) {
                    requestBuilder.addHeader(entry.getKey(), entry.getValue());
                }
            }
        }
    }
}
