package com.sizer.data;


import com.sizer.model.ApiResponse;

import org.json.JSONObject;

import java.util.Map;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import retrofit2.Call;


public interface IRemoteRepository {
    /**
     * Fetch remote version
     * @return
     */
    Call<Void> getVersion();

    /**
     * Create/save user
     * @param params
     * @return
     */
    Observable<ApiResponse<JSONObject>> saveUser(Map<String, String> params);

    /**
     * Upload scan image
     * @param image
     * @param imageId
     * @param userId
     * @param scanId
     * @return
     */
    Observable<ApiResponse<JSONObject>> uploadScan(MultipartBody.Part image, String imageId,
                                                   String userId, String scanId);
}