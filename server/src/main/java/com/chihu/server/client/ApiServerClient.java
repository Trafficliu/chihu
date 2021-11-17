package com.chihu.server.client;

import com.chihu.server.common.ApiServerConstants;
import com.chihu.server.model.User;
import com.chihu.server.serializer.ApiServerSerializer;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.net.HttpHeaders;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.commons.io.IOUtils;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.List;

@Builder(access = AccessLevel.PRIVATE)
@Slf4j
public class ApiServerClient implements Closeable {

    private static final int DEFAULT_HTTP_PORT = 8894;

    private final CloseableHttpClient httpClient;

    /** DEFAULT_HOST = 127.0.0.1 */
    private final String host;

    /** DEFAULT_HTTP_PORT */
    private final int port;

    private final List<HttpRecord> httpHistory;
    // Flag that determines whether we store the request and response information to
    // disk
    private final boolean enableHistory = false;

    public static ApiServerClient create(String host) {
        return ApiServerClient.create(
                host, DEFAULT_HTTP_PORT, Lists.newArrayList(), null);
    }

    public static ApiServerClient create(ApiServerClient client,
                                         String accessToken) {
        return ApiServerClient.create(
                client.host, DEFAULT_HTTP_PORT, client.httpHistory, accessToken);
    }

    private static ApiServerClient create(String host,
                                          int port,
                                          List<HttpRecord> httpHistory,
                                          String accessToken) {
        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        if (!Strings.isNullOrEmpty(accessToken)) {
            httpClientBuilder.setDefaultHeaders(ImmutableList.of(new BasicHeader(
                    HttpHeaders.AUTHORIZATION,
                    String.format(
                            "%s%s", ApiServerConstants.BEARER_HEADER_PREFIX, accessToken))));
        }
        return ApiServerClient.builder()
                .host(host)
                .port(port)
                .httpClient(httpClientBuilder.build())
                .httpHistory(httpHistory)
                .build();
    }

    @Override
    public void close() throws IOException {
        httpClient.close();
    }

    public void userRegister(User user) throws Exception {
        log.info("Registering user {} ", user);
        HttpPost post = new HttpPost();
        post.setURI(generateUri("/auth/register").build());

        List<NameValuePair> parameters = Lists.newArrayList();
        parameters.add(new BasicNameValuePair("username", user.getUsername()));
        parameters.add(new BasicNameValuePair("password", user.getPassword()));
        parameters.add(new BasicNameValuePair("email", user.getEmail()));
        parameters.add(new BasicNameValuePair("phone", user.getPhone()));
        post.setEntity(new UrlEncodedFormEntity(parameters));

        CloseableHttpResponse response = httpClient.execute(post);
        handleHttp(post, response);
    }

    public String requestActivationToken(String username) throws Exception {
        log.info("Requesting activation for user {} ", username);
        HttpGet get = new HttpGet();
        get.setURI(
                generateUri("/auth/request_activation_token")
                        .addParameter("username", username)
                        .build());

        CloseableHttpResponse response = httpClient.execute(get);
        handleHttp(get, response);

        return IOUtils.toString(
                response.getEntity().getContent(),
                StandardCharsets.UTF_8.displayName());
    }

    public void activateUser(String activationToken) throws Exception {
        log.info("Trying to activate user via activation token {} ", activationToken);
        HttpPost post = new HttpPost();
        post.setURI(generateUri("/auth/activate").build());

        List<NameValuePair> parameters = Lists.newArrayList();
        parameters.add(new BasicNameValuePair("token", activationToken));
        post.setEntity(new UrlEncodedFormEntity(parameters));

        CloseableHttpResponse response = httpClient.execute(post);
        handleHttp(post, response);
    }

    public String userSignIn(String username, String password) throws Exception {
        log.info("Authenticate user {}", username);
        HttpPost post = new HttpPost();
        post.setURI(generateUri("/auth/sign_in").build());

        List<NameValuePair> parameters = Lists.newArrayList();
        parameters.add(new BasicNameValuePair("username", username));
        parameters.add(new BasicNameValuePair("password", password));
        post.setEntity(new UrlEncodedFormEntity(parameters));

        CloseableHttpResponse response = httpClient.execute(post);
        handleHttp(post, response);

        return IOUtils.toString(
                response.getEntity().getContent(),
                StandardCharsets.UTF_8.displayName());
    }

    public String requestPasswordResetToken(String username) throws Exception {
        log.info("Requesting password reset token for user {} ", username);
        HttpGet get = new HttpGet();
        get.setURI(
                generateUri("/auth/request_pwd_reset_token")
                        .addParameter("username", username)
                        .build());

        CloseableHttpResponse response = httpClient.execute(get);
        handleHttp(get, response);

        return IOUtils.toString(
                response.getEntity().getContent(),
                StandardCharsets.UTF_8.displayName());
    }

    public void resetPasswordWithToken(
            String token, String username, String password, String confirmPassword) throws Exception {
        HttpPost post = new HttpPost();
        post.setURI(generateUri("/auth/reset_password_with_token").build());

        List<NameValuePair> parameters = Lists.newArrayList();
        parameters.add(new BasicNameValuePair("token", token));
        parameters.add(new BasicNameValuePair("username", username));
        parameters.add(new BasicNameValuePair("password", password));
        parameters.add(new BasicNameValuePair("confirm_password", confirmPassword));
        post.setEntity(new UrlEncodedFormEntity(parameters));

        CloseableHttpResponse response = httpClient.execute(post);
        handleHttp(post, response);
    }

    public User getUser() throws Exception {
        HttpGet get = new HttpGet();
        get.setURI(generateUri("/auth/get_user").build());

        CloseableHttpResponse response = httpClient.execute(get);
        handleHttp(get, response);

        return ApiServerSerializer.toUser(
                IOUtils.toString(
                        response.getEntity().getContent(),
                        StandardCharsets.UTF_8.displayName()));
    }

    // Helper methods
    private URIBuilder generateUri(String requestPath) {
        return new URIBuilder().setScheme("http").setHost(host).setPort(port).setPath(requestPath);
    }

    private void handleHttp(HttpRequestBase request,
                            CloseableHttpResponse response) {
        if (enableHistory) {
            httpHistory.add(HttpRecord.builder()
                    .request(request.toString())
                    .response(response.toString())
                    .build());
        }
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            String exceptionDetails = response.toString();
            try {
                exceptionDetails = IOUtils.toString(
                        response.getEntity().getContent(),
                        StandardCharsets.UTF_8.displayName());
            } catch (Exception e) {
                log.warn("Suppress exception while extracting entity from response!");
            }
            throw new RuntimeException(exceptionDetails);
        } else {
            log.warn("handle http succeeded");
        }
    }

    @Builder
    static class HttpRecord {
        private final String request;
        private final String response;
    }
}
