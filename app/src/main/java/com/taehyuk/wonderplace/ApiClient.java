package com.taehyuk.wonderplace;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
// 여기는 레트로핏 통신을 하기 위한 기본 세팅
public class ApiClient {

    private static final String BASE_URL = "https://dapi.kakao.com/"; //이제 우리 서버와 통신하면 URL바뀜 "https://51f1ef846be7.ngrok.io/"
    private static Retrofit retrofit;

    public static Retrofit getApiClient(){
        if(retrofit == null){
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return  retrofit;
    }

}

