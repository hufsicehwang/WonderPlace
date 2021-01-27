package com.taehyuk.wonderplace;

import com.taehyuk.wonderplace.model.address_search.AddressSearch;
import com.taehyuk.wonderplace.model.category_search.CategoryResult;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;


//밑에는 카카오와 통신하기 위해 헤더즈로 rest api를 밑과같은 형식으로 쓰고 @GET으로 @Query(카카오에서 보내라하는 파라메터들로 이뤄짐)로 요청한다.
public interface ApiInterface
{
    //장소이름으로 검색
//    @Headers("Authorization: KakaoAK 69406d6f78e94450237e9dc116fc151e")
    @FormUrlEncoded //Field 쓸때 해줘야되는 통신절차
    @POST("세부 URL") //우리가 만든 서버 세부 루트
    Call<CategoryResult> getSearchLocation(
            @Field("query") String query,  //Field로 뭐 요청할건지
            @Field("size") int size
    );

    //카테고리로 검색,
//    @Headers("Authorization: KakaoAK 69406d6f78e94450237e9dc116fc151e")
    @FormUrlEncoded
    @POST("세부 URL")
    Call<CategoryResult> getSearchCategory(
            @Field("category_group_code") String category_group_code,
            @Field("x") String x,
            @Field("y") String y,
            @Field("radius") int radius
    );



    //장소이름으로 검색
//    @Headers("Authorization: KakaoAK 69406d6f78e94450237e9dc116fc151e")
//    @GET("v2/local/search/keyword.json")
//    Call<CategoryResult> getSearchLocation(
//            @Query("query") String query,
//            @Query("size") int size
//    );
//
//    //카테고리로 검색,
//    @Headers("Authorization: KakaoAK 69406d6f78e94450237e9dc116fc151e")
//    @GET("v2/local/search/category.json")
//    Call<CategoryResult> getSearchCategory(
//            @Query("category_group_code") String category_group_code,
//            @Query("x") String x,
//            @Query("y") String y,
//            @Query("radius") int radius
//    );



    //장소이름으로 특정위치기준으로 검색
    @GET("v2/local/search/keyword.json")
    Call<CategoryResult> getSearchLocationDetail(
            @Header("Authorization") String token,
            @Query("query") String query,
            @Query("x") String x,
            @Query("y") String y,
            @Query("size") int size
    );

    @Headers("Authorization: KakaoAK 69406d6f78e94450237e9dc116fc151e")
    @GET("v2/local/search/address.json")
    Call<AddressSearch> getSearchAddress(
            @Query("query") String query,
            @Query("size") int size
    );
}