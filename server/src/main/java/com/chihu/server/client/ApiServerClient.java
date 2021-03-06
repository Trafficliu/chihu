package com.chihu.server.client;

import com.chihu.server.common.ApiServerConstants;
import com.chihu.server.model.*;
import com.chihu.server.serializer.ApiServerSerializer;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.net.HttpHeaders;
import com.google.gson.reflect.TypeToken;
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
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.commons.io.IOUtils;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Builder(access = AccessLevel.PRIVATE)
@Slf4j
public class ApiServerClient implements Closeable {

    private static final int DEFAULT_HTTP_PORT = 8894;

    private static final Type ADDRESS_LIST_TYPE =
        new TypeToken<ArrayList<Address>>() {}.getType();

    private static final Type DISH_COUNT_MAP_TYPE =
        new TypeToken<Map<Long, Integer>>() {}.getType();

    private static final Type ORDER_DETAIL_LIST_TYPE =
        new TypeToken<List<OrderDetail>>() {}.getType();

    private static final Type ORDER_SUMMARY_LIST_TYPE =
        new TypeToken<List<OrderSummary>>() {}.getType();

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

    // User Registration and Authentication
    public void userRegister(User user) throws Exception {
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
        HttpPost post = new HttpPost();
        post.setURI(generateUri("/auth/activate").build());

        List<NameValuePair> parameters = Lists.newArrayList();
        parameters.add(new BasicNameValuePair("token", activationToken));
        post.setEntity(new UrlEncodedFormEntity(parameters));

        CloseableHttpResponse response = httpClient.execute(post);
        handleHttp(post, response);
    }

    public String userSignIn(String username, String password) throws Exception {
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
        parameters.add(
            new BasicNameValuePair("confirm_password", confirmPassword));
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

    // Business Groups
    public void createBusinessGroup(BusinessGroup businessGroup) throws Exception {
        HttpPost post = new HttpPost();
        post.setURI(generateUri("/business/create_business_group").build());

        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
        entityBuilder.addPart(
                "business_group_str",
                new StringBody(
                        ApiServerSerializer.serialize(businessGroup),
                        ContentType.APPLICATION_JSON));
        post.setEntity(entityBuilder.build());

        CloseableHttpResponse response = httpClient.execute(post);
        handleHttp(post, response);
    }

    public void updateBusinessGroup(BusinessGroup businessGroup) throws Exception {
        HttpPost post = new HttpPost();
        post.setURI(generateUri("/business/update_business_group").build());

        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
        entityBuilder.addPart(
            "business_group_str",
            new StringBody(
                ApiServerSerializer.serialize(businessGroup),
                ContentType.APPLICATION_JSON));
        post.setEntity(entityBuilder.build());

        CloseableHttpResponse response = httpClient.execute(post);
        handleHttp(post, response);
    }

    public void deleteBusinessGroup(Long businessGroupId) throws Exception {
        HttpPost post = new HttpPost();
        post.setURI(generateUri("/business/delete_business_group").build());

        List<NameValuePair> parameters = Lists.newArrayList();
        parameters.add(
            new BasicNameValuePair(
                "business_group_id", Long.toString(businessGroupId)));
        post.setEntity(new UrlEncodedFormEntity(parameters));

        CloseableHttpResponse response = httpClient.execute(post);
        handleHttp(post, response);
    }

    public BusinessGroup getBusinessGroupById(Long businessGroupId) throws Exception {
        HttpGet get = new HttpGet();
        get.setURI(
            generateUri("/business/get_business_group_by_id")
                .addParameter(
                    "business_group_id",
                    Long.toString(businessGroupId))
                .build());

        CloseableHttpResponse response = httpClient.execute(get);
        handleHttp(get, response);

        return ApiServerSerializer.toBusinessGroup(
                IOUtils.toString(
                        response.getEntity().getContent(),
                        StandardCharsets.UTF_8.displayName()));
    }

    public BusinessGroup getBusinessGroupByName(String businessGroupName) throws Exception {
        HttpGet get = new HttpGet();
        get.setURI(
            generateUri("/business/get_business_group_by_name")
                .addParameter(
                    "business_group_name",
                    businessGroupName)
                .build());

        CloseableHttpResponse response = httpClient.execute(get);
        handleHttp(get, response);

        return ApiServerSerializer.toBusinessGroup(
            IOUtils.toString(
                response.getEntity().getContent(),
                StandardCharsets.UTF_8.displayName()));
    }

    // Business Entities
    public void createBusinessEntity(BusinessEntity businessEntity) throws Exception {
        HttpPost post = new HttpPost();
        post.setURI(generateUri("/business/create_business_entity").build());

        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
        entityBuilder.addPart(
            "business_entity_str",
            new StringBody(
                ApiServerSerializer.serialize(businessEntity),
                ContentType.APPLICATION_JSON));
        post.setEntity(entityBuilder.build());

        CloseableHttpResponse response = httpClient.execute(post);
        handleHttp(post, response);
    }

    public void updateBusinessEntity(BusinessEntity businessEntity) throws Exception {
        HttpPost post = new HttpPost();
        post.setURI(generateUri("/business/update_business_entity").build());

        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
        entityBuilder.addPart(
            "business_entity_str",
            new StringBody(
                ApiServerSerializer.serialize(businessEntity),
                ContentType.APPLICATION_JSON));
        post.setEntity(entityBuilder.build());

        CloseableHttpResponse response = httpClient.execute(post);
        handleHttp(post, response);
    }

    public void deleteBusinessEntity(Long businessEntityId) throws Exception {
        HttpPost post = new HttpPost();
        post.setURI(generateUri("/business/delete_business_entity").build());

        List<NameValuePair> parameters = Lists.newArrayList();
        parameters.add(
            new BasicNameValuePair(
                "business_entity_id", Long.toString(businessEntityId)));
        post.setEntity(new UrlEncodedFormEntity(parameters));

        CloseableHttpResponse response = httpClient.execute(post);
        handleHttp(post, response);
    }

    public BusinessEntity getBusinessEntityById(Long businessEntityId) throws Exception {
        HttpGet get = new HttpGet();
        get.setURI(
            generateUri("/business/get_business_entity_by_id")
                .addParameter(
                    "business_entity_id",
                    Long.toString(businessEntityId))
                .build());

        CloseableHttpResponse response = httpClient.execute(get);
        handleHttp(get, response);

        return ApiServerSerializer.toBusinessEntity(
            IOUtils.toString(
                response.getEntity().getContent(),
                StandardCharsets.UTF_8.displayName()));
    }

    public BusinessEntity getBusinessEntityByName(String businessEntityName) throws Exception {
        HttpGet get = new HttpGet();
        get.setURI(
            generateUri("/business/get_business_entity_by_name")
                .addParameter(
                    "business_entity_name",
                    businessEntityName)
                .build());

        CloseableHttpResponse response = httpClient.execute(get);
        handleHttp(get, response);

        return ApiServerSerializer.toBusinessEntity(
            IOUtils.toString(
                response.getEntity().getContent(),
                StandardCharsets.UTF_8.displayName()));
    }

    public void setWorkingDate(Long businessEntityId, String workingDateStr)
        throws Exception {
        HttpPost post = new HttpPost();
        post.setURI(generateUri("/business/set_working_date").build());

        List<NameValuePair> parameters = Lists.newArrayList();
        parameters.add(
            new BasicNameValuePair(
                "business_entity_id", Long.toString(businessEntityId)));
        parameters.add(
            new BasicNameValuePair("working_date_str", workingDateStr));
        post.setEntity(new UrlEncodedFormEntity(parameters));

        CloseableHttpResponse response = httpClient.execute(post);
        handleHttp(post, response);
    }

    public void setWorkingDays(Long businessEntityId, Integer workingDays)
        throws Exception {
        HttpPost post = new HttpPost();
        post.setURI(generateUri("/business/set_working_days").build());

        List<NameValuePair> parameters = Lists.newArrayList();
        parameters.add(
            new BasicNameValuePair(
                "business_entity_id", Long.toString(businessEntityId)));
        parameters.add(
            new BasicNameValuePair(
                "working_days", Integer.toString(workingDays)));
        post.setEntity(new UrlEncodedFormEntity(parameters));

        CloseableHttpResponse response = httpClient.execute(post);
        handleHttp(post, response);
    }

    public void setWorkingHours(Long businessEntityId, Long workingHours)
        throws Exception {
        HttpPost post = new HttpPost();
        post.setURI(generateUri("/business/set_working_hours").build());

        List<NameValuePair> parameters = Lists.newArrayList();
        parameters.add(
            new BasicNameValuePair(
                "business_entity_id", Long.toString(businessEntityId)));
        parameters.add(
            new BasicNameValuePair(
                "working_hours", Long.toString(workingHours)));
        post.setEntity(new UrlEncodedFormEntity(parameters));

        CloseableHttpResponse response = httpClient.execute(post);
        handleHttp(post, response);
    }

    // Dishes
    public void createDish(Dish dish) throws Exception {
        HttpPost post = new HttpPost();
        post.setURI(generateUri("/dish/create_dish").build());

        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
        entityBuilder.addPart(
            "dish_str",
            new StringBody(
                ApiServerSerializer.serialize(dish),
                ContentType.APPLICATION_JSON));
        post.setEntity(entityBuilder.build());

        CloseableHttpResponse response = httpClient.execute(post);
        handleHttp(post, response);
    }

    public void updateDish(Dish dish) throws Exception {
        HttpPost post = new HttpPost();
        post.setURI(generateUri("/dish/update_dish").build());

        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
        entityBuilder.addPart(
            "dish_str",
            new StringBody(
                ApiServerSerializer.serialize(dish),
                ContentType.APPLICATION_JSON));
        post.setEntity(entityBuilder.build());

        CloseableHttpResponse response = httpClient.execute(post);
        handleHttp(post, response);
    }

    public void deleteDish(Long dishId) throws Exception {
        HttpPost post = new HttpPost();
        post.setURI(generateUri("/dish/delete_dish").build());

        List<NameValuePair> parameters = Lists.newArrayList();
        parameters.add(
            new BasicNameValuePair(
                "dish_id", Long.toString(dishId)));
        post.setEntity(new UrlEncodedFormEntity(parameters));

        CloseableHttpResponse response = httpClient.execute(post);
        handleHttp(post, response);
    }

    public Dish getDishById(Long dishId) throws Exception {
        HttpGet get = new HttpGet();
        get.setURI(
            generateUri("/dish/get_dish_by_id")
                .addParameter(
                    "dish_id",
                    Long.toString(dishId))
                .build());

        CloseableHttpResponse response = httpClient.execute(get);
        handleHttp(get, response);

        return ApiServerSerializer.toDish(
            IOUtils.toString(
                response.getEntity().getContent(),
                StandardCharsets.UTF_8.displayName()));
    }

    public Dish getDishByName(Long businessGroupId, String dishName)
        throws Exception {
        HttpGet get = new HttpGet();
        get.setURI(
            generateUri("/dish/get_dish_by_name")
                .addParameter(
                    "business_group_id",
                    Long.toString(businessGroupId))
                .addParameter(
                    "dish_name",
                    dishName)
                .build());

        CloseableHttpResponse response = httpClient.execute(get);
        handleHttp(get, response);

        return ApiServerSerializer.toDish(
            IOUtils.toString(
                response.getEntity().getContent(),
                StandardCharsets.UTF_8.displayName()));
    }

    // Addresses
    public void createAddress(Address address) throws Exception {
        HttpPost post = new HttpPost();
        post.setURI(generateUri("/address/create_address").build());

        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
        entityBuilder.addPart(
            "address_str",
            new StringBody(
                ApiServerSerializer.serialize(address),
                ContentType.APPLICATION_JSON));
        post.setEntity(entityBuilder.build());

        CloseableHttpResponse response = httpClient.execute(post);
        handleHttp(post, response);
    }

    public void updateAddress(Address address) throws Exception {
        HttpPost post = new HttpPost();
        post.setURI(generateUri("/address/update_address").build());

        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
        entityBuilder.addPart(
            "address_str",
            new StringBody(
                ApiServerSerializer.serialize(address),
                ContentType.APPLICATION_JSON));
        post.setEntity(entityBuilder.build());

        CloseableHttpResponse response = httpClient.execute(post);
        handleHttp(post, response);
    }

    public void deleteAddress(Long addressId) throws Exception {
        HttpPost post = new HttpPost();
        post.setURI(generateUri("/address/delete_address").build());

        List<NameValuePair> parameters = Lists.newArrayList();
        parameters.add(
            new BasicNameValuePair(
                "address_id", Long.toString(addressId)));
        post.setEntity(new UrlEncodedFormEntity(parameters));

        CloseableHttpResponse response = httpClient.execute(post);
        handleHttp(post, response);
    }

    public Address getAddressById(Long addressId) throws Exception {
        HttpGet get = new HttpGet();
        get.setURI(
            generateUri("/address/get_address_by_id")
                .addParameter(
                    "address_id",
                    Long.toString(addressId))
                .build());

        CloseableHttpResponse response = httpClient.execute(get);
        handleHttp(get, response);

        return ApiServerSerializer.toAddress(
            IOUtils.toString(
                response.getEntity().getContent(),
                StandardCharsets.UTF_8.displayName()));
    }

    public List<Address> getUserAddresses() throws Exception {
        HttpGet get = new HttpGet();
        get.setURI(
            generateUri("/address/get_user_addresses")
                .build());

        CloseableHttpResponse response = httpClient.execute(get);
        handleHttp(get, response);

        return ApiServerSerializer.getGsonInstance()
            .fromJson(
                IOUtils.toString(
                    response.getEntity().getContent(),
                    StandardCharsets.UTF_8.displayName()),
            ADDRESS_LIST_TYPE);
    }

    // Orders
    public void updateCart(Map<Long, Integer> dishCount, Long businessEntityId)
        throws Exception {
        HttpPost post = new HttpPost();
        post.setURI(generateUri("/order/update_cart")
            .addParameter(
                "dish_count_str", ApiServerSerializer.serialize(dishCount))
            .addParameter(
                "business_entity_id", Long.toString(businessEntityId))
            .build());

        CloseableHttpResponse response = httpClient.execute(post);
        handleHttp(post, response);
    }

    public void createOrder(Map<Long, Integer> dishCount,
                            Long businessEntityId,
                            Long toAddressId,
                            String deliveryType,
                            Integer tipInCent) throws Exception {
        HttpPost post = new HttpPost();
        post.setURI(generateUri("/order/create_order")
            .addParameter(
                "dish_count_str", ApiServerSerializer.serialize(dishCount))
            .addParameter(
                "business_entity_id", Long.toString(businessEntityId))
            .addParameter(
                "to_address_id", Long.toString(toAddressId))
            .addParameter(
                "delivery_type", deliveryType)
            .addParameter(
                "tip_in_cent", Integer.toString(tipInCent))
            .build());

        CloseableHttpResponse response = httpClient.execute(post);
        handleHttp(post, response);
    }

    public void updateOrder(Long orderId,
                            Map<Long, Integer> dishCount,
                            Long businessEntityId) throws Exception {
        HttpPost post = new HttpPost();
        post.setURI(generateUri("/order/update_order")
            .addParameter(
                "order_id", Long.toString(orderId))
            .addParameter(
                "dish_count_str", ApiServerSerializer.serialize(dishCount))
            .addParameter(
                "business_entity_id", Long.toString(businessEntityId))
            .build());

        CloseableHttpResponse response = httpClient.execute(post);
        handleHttp(post, response);
    }

    public void eaterUpdateOrderStatus(Long orderId,
                                       String status) throws Exception {
        HttpPost post = new HttpPost();
        post.setURI(generateUri("/order/eater_update_order_status")
            .addParameter(
                "order_id", Long.toString(orderId))
            .addParameter(
                "status", status)
            .build());

        CloseableHttpResponse response = httpClient.execute(post);
        handleHttp(post, response);
    }

    public void ownerUpdateOrderStatus(Long orderId,
                                       String status) throws Exception {
        HttpPost post = new HttpPost();
        post.setURI(generateUri("/order/owner_update_order_status")
            .addParameter(
                "order_id", Long.toString(orderId))
            .addParameter(
                "status", status)
            .build());

        CloseableHttpResponse response = httpClient.execute(post);
        handleHttp(post, response);
    }

    public List<OrderDetail> getDishesInCart() throws Exception {
        HttpGet get = new HttpGet();
        get.setURI(generateUri("/order/get_dishes_in_cart").build());

        CloseableHttpResponse response = httpClient.execute(get);
        handleHttp(get, response);

        return ApiServerSerializer.getGsonInstance()
            .fromJson(
                IOUtils.toString(
                    response.getEntity().getContent(),
                    StandardCharsets.UTF_8.displayName()),
                ORDER_DETAIL_LIST_TYPE);
    }

    public List<OrderDetail> getOrderDishes(Long orderId) throws Exception {
        HttpGet get = new HttpGet();
        get.setURI(
            generateUri("/order/get_order_dishes")
                .addParameter("order_id", Long.toString(orderId))
                .build());

        CloseableHttpResponse response = httpClient.execute(get);
        handleHttp(get, response);

        return ApiServerSerializer.getGsonInstance()
            .fromJson(
                IOUtils.toString(
                    response.getEntity().getContent(),
                    StandardCharsets.UTF_8.displayName()),
                ORDER_DETAIL_LIST_TYPE);
    }

    public OrderSummary getOrderSummary(Long orderId) throws Exception {
        HttpGet get = new HttpGet();
        get.setURI(
            generateUri("/order/get_order_summary")
                .addParameter("order_id", Long.toString(orderId))
                .build());

        CloseableHttpResponse response = httpClient.execute(get);
        handleHttp(get, response);

        return ApiServerSerializer.toOrderSummary(
            IOUtils.toString(
                response.getEntity().getContent(),
                StandardCharsets.UTF_8.displayName()));
    }

    public List<OrderSummary> getAllOrderSummaries() throws Exception {
        HttpGet get = new HttpGet();
        get.setURI(
            generateUri("/order/get_all_order_summaries").build());

        CloseableHttpResponse response = httpClient.execute(get);
        handleHttp(get, response);

        return ApiServerSerializer.getGsonInstance()
            .fromJson(
                IOUtils.toString(
                    response.getEntity().getContent(),
                    StandardCharsets.UTF_8.displayName()),
                ORDER_SUMMARY_LIST_TYPE);
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
