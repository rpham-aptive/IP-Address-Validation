package org.example.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.model.InputModel;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.HashMap;

// Handler value: example.Handler
public class LambdaHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        String body = event.getBody() != null ? event.getBody() : "Empty body";
        System.out.println("body: " + body);

        InputModel input = null;
        try {
            input = new ObjectMapper().readValue(body, InputModel.class);
        } catch (IOException e) {
            APIGatewayV2HTTPResponse badRequestResponse = generateBadRequestResponse();
            return badRequestResponse;
        }

        // Connect to the Redis cache
        Jedis jedis = new Jedis("redis-not-cluster.ons8u1.ng.0001.usw1.cache.amazonaws.com",6379);

        // Get the value of the "mykey" kasdasdey
        String value = jedis.get(input.ipAddress);
        // Print the value
        System.out.println("value: " + value);

        // Disconnect from the Redis cache
        jedis.close();

        if (value == null) {
            System.out.println("value is null so return IP is not found.");
            APIGatewayV2HTTPResponse notFoundResponse = generateResponseIpIsNotFound();
            return notFoundResponse;
        } else {
            System.out.println("IP was found. Return that it is blacklisted.");
            APIGatewayV2HTTPResponse isFoundResponse = generateResponseIpIsFound();
            return isFoundResponse;
        }
    }

    private APIGatewayV2HTTPResponse generateResponseIpIsNotFound() {
        APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();
        response.setIsBase64Encoded(false);
        response.setStatusCode(200);
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        response.setHeaders(headers);
        response.setBody("IP Address is not blacklisted.");
        return response;
    }

    private APIGatewayV2HTTPResponse generateResponseIpIsFound() {
        APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();
        response.setIsBase64Encoded(false);
        response.setStatusCode(200);
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        response.setHeaders(headers);
        response.setBody("IP Address is Blacklisted.");
        return response;
    }

    private APIGatewayV2HTTPResponse generateBadRequestResponse() {
        APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();
        response.setIsBase64Encoded(false);
        response.setStatusCode(401);
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        response.setHeaders(headers);
        response.setBody("Bad Input. This is Version 3");
        return response;
    }


}
