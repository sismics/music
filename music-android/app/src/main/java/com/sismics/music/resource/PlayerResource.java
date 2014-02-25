package com.sismics.music.resource;

import android.content.Context;

import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;

import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Access to /player API.
 * 
 * @author bgamard
 */
public class PlayerResource extends BaseResource {
    /**
     * Post a set of tracks played before.
     * @param context Context
     * @param idList Track IDs
     * @param dateList Dates of completion
     * @param responseHandler Response handler
     * @return Request handle used to cancel
     */
    public static RequestHandle listened(Context context, List<String> idList, List<Long> dateList,
                                         JsonHttpResponseHandler responseHandler) {
        init(context);

        RequestParams params = new RequestParams() {
            @Override
            protected List<BasicNameValuePair> getParamsList() {
                List<BasicNameValuePair> lparams = new LinkedList<>();

                for (ConcurrentHashMap.Entry<String, String> entry : urlParams.entrySet()) {
                    lparams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }

                lparams.addAll(getParamsList(null, urlParamsWithObjects));
                return lparams;
            }

            private List<BasicNameValuePair> getParamsList(String key, Object value) {
                List<BasicNameValuePair> params = new LinkedList<>();
                if (value instanceof Map) {
                    Map<String, Object> map = (Map<String, Object>) value;
                    List<String> list = new ArrayList<>(map.keySet());
                    // Ensure consistent ordering in query string
                    Collections.sort(list);
                    for (String nestedKey : list) {
                        Object nestedValue = map.get(nestedKey);
                        if (nestedValue != null) {
                            params.addAll(getParamsList(key == null ? nestedKey : String.format("%s[%s]", key, nestedKey),
                                    nestedValue));
                        }
                    }
                } else if (value instanceof List) {
                    List<Object> list = (List<Object>) value;
                    for (Object nestedValue : list) {
                        params.addAll(getParamsList(key, nestedValue));
                    }
                } else if (value instanceof Object[]) {
                    Object[] array = (Object[]) value;
                    for (Object nestedValue : array) {
                        params.addAll(getParamsList(key, nestedValue));
                    }
                } else if (value instanceof Set) {
                    Set<Object> set = (Set<Object>) value;
                    for (Object nestedValue : set) {
                        params.addAll(getParamsList(key, nestedValue));
                    }
                } else if (value instanceof String) {
                    params.add(new BasicNameValuePair(key, (String) value));
                } else if (value instanceof Long) {
                    params.add(new BasicNameValuePair(key, value.toString()));
                }
                return params;
            }
        };

        params.put("id", idList);
        params.put("date", dateList);

        return client.post(getApiUrl(context) + "/player/listened", params, responseHandler);
    }
}
