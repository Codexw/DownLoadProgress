package me.jessyan.progressmanager.demo;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Author: xu
 * Date:2018/9/14
 * Description:
 */
public interface Api {

    @GET("https://raw.githubusercontent.com/iielse/behavior-learn/master/preview2.gif")
    Call<ResponseBody> getCall();

    @Multipart
    @POST("http://upload.qiniu.com/")
    Call<ResponseBody> getRequestCall(@Part MultipartBody.Part file);
}
