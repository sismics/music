package com.sismics.music.resource;

import android.content.Context;

import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;

import java.util.HashSet;
import java.util.List;

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

        RequestParams params = new RequestParams();
        for (String id : idList) {
            params.add("id", id);
        }
        for (Long date : dateList) {
            params.add("date", date.toString());
        }
        return client.post(getApiUrl(context) + "/player/listened", params, responseHandler);
    }
}
