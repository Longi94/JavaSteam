package in.dragonbra.javasteam.steam.webapi;

import in.dragonbra.javasteam.types.KeyValue;
import in.dragonbra.javasteam.util.WebHelpers;
import in.dragonbra.javasteam.util.compat.Consumer;
import okhttp3.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a single interface that exists within the Web API.
 */
public class WebAPI {

    public static final String DEFAULT_BASE_ADDRESS = "https://api.steampowered.com/";

    private final OkHttpClient client;

    private final HttpUrl baseAddress;

    private final String _interface;

    private final String webAPIKey;

    public WebAPI(OkHttpClient httpClient, String baseAddress, String _interface, String webAPIKey) {
        this.baseAddress = HttpUrl.parse(baseAddress);

        if (this.baseAddress == null) {
            throw new IllegalArgumentException();
        }

        this._interface = _interface;
        this.webAPIKey = webAPIKey;
        client = httpClient;
    }

    /**
     * Manually calls the specified Web API function with the provided details. This method is synchronous.
     *
     * @param httpMethod The http request method. Either "POST" or "GET".
     * @param function   The function name to call.
     * @param version    The version of the function to call.
     * @param parameters A map of string key value pairs representing arguments to be passed to the API.
     * @return A {@link KeyValue} object representing the results of the Web API call.
     * @throws IOException if the request could not be executed
     * @throws WebAPIRequestException the request was successful but returned a non success response code
     *
     */
    public KeyValue call(String httpMethod, String function, int version, Map<String, String> parameters)
            throws IOException, WebAPIRequestException {
        Request request = buildRequest(httpMethod, function, version, parameters);
        Response response = client.newCall(request).execute();

        if (!response.isSuccessful()) {
            throw new WebAPIRequestException(response);
        }

        return parseResponse(response);
    }

    /**
     * Manually calls the specified Web API function with the provided details. This method is synchronous.
     *
     * @param function   The function name to call.
     * @param version    The version of the function to call.
     * @param parameters A map of string key value pairs representing arguments to be passed to the API.
     * @return A {@link KeyValue} object representing the results of the Web API call.
     * @throws IOException if the request could not be executed
     */
    public KeyValue call(String function, int version, Map<String, String> parameters) throws IOException {
        return call("GET", function, version, parameters);
    }

    /**
     * Manually calls the specified Web API function with the provided details. This method is synchronous.
     *
     * @param httpMethod The http request method. Either "POST" or "GET".
     * @param function   The function name to call.
     * @param parameters A map of string key value pairs representing arguments to be passed to the API.
     * @return A {@link KeyValue} object representing the results of the Web API call.
     * @throws IOException if the request could not be executed
     */
    public KeyValue call(String httpMethod, String function, Map<String, String> parameters) throws IOException {
        return call(httpMethod, function, 1, parameters);
    }

    /**
     * Manually calls the specified Web API function with the provided details. This method is synchronous.
     *
     * @param function   The function name to call.
     * @param parameters A map of string key value pairs representing arguments to be passed to the API.
     * @return A {@link KeyValue} object representing the results of the Web API call.
     * @throws IOException if the request could not be executed
     */
    public KeyValue call(String function, Map<String, String> parameters) throws IOException {
        return call("GET", function, 1, parameters);
    }

    /**
     * Manually calls the specified Web API function with the provided details. This method is synchronous.
     *
     * @param httpMethod The http request method. Either "POST" or "GET".
     * @param function   The function name to call.
     * @param version    The version of the function to call.
     * @return A {@link KeyValue} object representing the results of the Web API call.
     * @throws IOException if the request could not be executed
     */
    public KeyValue call(String httpMethod, String function, int version) throws IOException {
        return call(httpMethod, function, version, (Map<String, String>) null);
    }

    /**
     * Manually calls the specified Web API function with the provided details. This method is synchronous.
     *
     * @param function   The function name to call.
     * @param version    The version of the function to call.
     * @return A {@link KeyValue} object representing the results of the Web API call.
     * @throws IOException if the request could not be executed
     */
    public KeyValue call(String function, int version) throws IOException {
        return call("GET", function, version, (Map<String, String>) null);
    }

    /**
     * Manually calls the specified Web API function with the provided details. This method is synchronous.
     *
     * @param httpMethod The http request method. Either "POST" or "GET".
     * @param function   The function name to call.
     * @return A {@link KeyValue} object representing the results of the Web API call.
     * @throws IOException if the request could not be executed
     */
    public KeyValue call(String httpMethod, String function) throws IOException {
        return call(httpMethod, function, 1, (Map<String, String>) null);
    }

    /**
     * Manually calls the specified Web API function with the provided details. This method is synchronous.
     *
     * @param function   The function name to call.
     * @return A {@link KeyValue} object representing the results of the Web API call.
     * @throws IOException if the request could not be executed
     */
    public KeyValue call(String function) throws IOException {
        return call("GET", function, 1, (Map<String, String>) null);
    }

    /**
     * Manually calls the specified Web API function with the provided details. This method is asynchronous.
     *
     * @param httpMethod The http request method. Either "POST" or "GET".
     * @param function   The function name to call.
     * @param version    The version of the function to call.
     * @param parameters A map of string key value pairs representing arguments to be passed to the API.
     * @param callback   the callback that will be called with the resulting {@link KeyValue} object.
     * @param error      the callback for handling response errors.
     * @throws IOException if the request could not be executed
     */
    public void call(String httpMethod, String function, int version, Map<String, String> parameters,
                     final Consumer<KeyValue> callback, final Consumer<WebAPIRequestException> error) throws IOException {
        Request request = buildRequest(httpMethod, function, version, parameters);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                throw new IllegalStateException("request unsuccessful", e);
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (!response.isSuccessful()) {
                    error.accept(new WebAPIRequestException(response));
                } else {
                    callback.accept(parseResponse(response));
                }
            }
        });
    }

    /**
     * Manually calls the specified Web API function with the provided details. This method is asynchronous.
     *
     * @param httpMethod The http request method. Either "POST" or "GET".
     * @param function   The function name to call.
     * @param version    The version of the function to call.
     * @param callback   the callback that will be called with the resulting {@link KeyValue} object.
     * @param error      the callback for handling response errors.
     * @throws IOException if the request could not be executed
     */
    public void call(String httpMethod, String function, int version, final Consumer<KeyValue> callback,
                     final Consumer<WebAPIRequestException> error) throws IOException {
        call(httpMethod, function, version, null, callback, error);
    }

    /**
     * Manually calls the specified Web API function with the provided details. This method is asynchronous.
     *
     * @param httpMethod The http request method. Either "POST" or "GET".
     * @param function   The function name to call.
     * @param parameters A map of string key value pairs representing arguments to be passed to the API.
     * @param callback   the callback that will be called with the resulting {@link KeyValue} object.
     * @param error      the callback for handling response errors.
     * @throws IOException if the request could not be executed
     */
    public void call(String httpMethod, String function, Map<String, String> parameters, final Consumer<KeyValue> callback,
                     final Consumer<WebAPIRequestException> error) throws IOException {
        call(httpMethod, function, 1, parameters, callback, error);
    }

    /**
     * Manually calls the specified Web API function with the provided details. This method is asynchronous.
     *
     * @param httpMethod The http request method. Either "POST" or "GET".
     * @param function   The function name to call.
     * @param callback   the callback that will be called with the resulting {@link KeyValue} object.
     * @param error      the callback for handling response errors.
     * @throws IOException if the request could not be executed
     */
    public void call(String httpMethod, String function, final Consumer<KeyValue> callback,
                     final Consumer<WebAPIRequestException> error) throws IOException {
        call(httpMethod, function, 1, null, callback, error);
    }

    /**
     * Manually calls the specified Web API function with the provided details. This method is asynchronous.
     *
     * @param function   The function name to call.
     * @param version    The version of the function to call.
     * @param callback   the callback that will be called with the resulting {@link KeyValue} object.
     * @param error      the callback for handling response errors.
     * @throws IOException if the request could not be executed
     */
    public void call(String function, int version, final Consumer<KeyValue> callback,
                     final Consumer<WebAPIRequestException> error) throws IOException {
        call("GET", function, version, null, callback, error);
    }

    /**
     * Manually calls the specified Web API function with the provided details. This method is asynchronous.
     *
     * @param function   The function name to call.
     * @param parameters A map of string key value pairs representing arguments to be passed to the API.
     * @param callback   the callback that will be called with the resulting {@link KeyValue} object.
     * @param error      the callback for handling response errors.
     * @throws IOException if the request could not be executed
     */
    public void call(String function, Map<String, String> parameters, final Consumer<KeyValue> callback,
                     final Consumer<WebAPIRequestException> error) throws IOException {
        call("GET", function, 1, parameters, callback, error);
    }

    /**
     * Manually calls the specified Web API function with the provided details. This method is asynchronous.
     *
     * @param function   The function name to call.
     * @param callback   the callback that will be called with the resulting {@link KeyValue} object.
     * @param error      the callback for handling response errors.
     * @throws IOException if the request could not be executed
     */
    public void call(String function, final Consumer<KeyValue> callback, final Consumer<WebAPIRequestException> error)
            throws IOException {
        call("GET", function, 1, null, callback, error);
    }

    private KeyValue parseResponse(Response response) {
        KeyValue kv = new KeyValue();

        try (InputStream is = response.body().byteStream()) {
            kv.readAsText(is);
        } catch (Exception e) {
            throw new IllegalStateException("An internal error occurred when attempting to parse the response from the WebAPI server. This can indicate a change in the VDF format.", e);
        }

        return kv;
    }

    private Request buildRequest(String httpMethod, String function, int version, Map<String, String> parameters) {
        if (httpMethod == null) {
            throw new IllegalArgumentException("httpMethod is null");
        }
        if (!httpMethod.equalsIgnoreCase("GET") && !httpMethod.equalsIgnoreCase("POST")) {
            throw new IllegalArgumentException("only GET and POST is supported right now");
        }
        if (function == null) {
            throw new IllegalArgumentException("function is null");
        }
        if (parameters == null) {
            parameters = new HashMap<>();
        }

        parameters.put("format", "vdf");

        if (webAPIKey != null) {
            parameters.put("key", webAPIKey);
        }

        Request.Builder builder = new Request.Builder();

        HttpUrl.Builder urlBuilder = baseAddress.newBuilder()
                .addPathSegment(_interface)
                .addPathSegment(function)
                .addPathSegment("v" + version);

        if (httpMethod.equalsIgnoreCase("GET")) {
            for (Map.Entry<String, String> param : parameters.entrySet()) {
                urlBuilder.addEncodedQueryParameter(WebHelpers.urlEncode(param.getKey()), param.getValue());
            }
            builder.get();
        } else {
            FormBody.Builder bodyBuilder = new FormBody.Builder();
            for (Map.Entry<String, String> param : parameters.entrySet()) {
                bodyBuilder.addEncoded(WebHelpers.urlEncode(param.getKey()), param.getValue());
            }
            builder.post(bodyBuilder.build());
        }

        HttpUrl url = urlBuilder
                .build();

        return builder.url(url).build();
    }

    public HttpUrl getBaseAddress() {
        return baseAddress;
    }

    public String getInterface() {
        return _interface;
    }

    public String getWebAPIKey() {
        return webAPIKey;
    }

    /**
     * Thrown when WebAPI request fails (non success response code).
     */
    public class WebAPIRequestException extends IOException {

        private int statusCode;

        private Map<String, List<String>> headers;

        /**
         * Initializes a new instance of the {@link WebAPIRequestException} class.
         * @param response the response object from the call
         */
        WebAPIRequestException(Response response) {
            super(response.message());
            statusCode = response.code();
            headers = response.headers().toMultimap();
        }

        /**
         * @return the status code of the response
         */
        public int getStatusCode() {
            return statusCode;
        }

        /**
         * @return headers of the response
         */
        public Map<String, List<String>> getHeaders() {
            return headers;
        }
    }
}
