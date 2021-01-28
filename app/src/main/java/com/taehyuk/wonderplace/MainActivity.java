package com.taehyuk.wonderplace;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

//import net.daum.android.map.MapActivity;
import com.taehyuk.wonderplace.model.category_search.CategoryResult;
import com.taehyuk.wonderplace.model.category_search.Document;

import net.daum.mf.map.api.MapCircle;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static net.daum.mf.map.api.MapPoint.mapPointWithGeoCoord;

public class MainActivity extends AppCompatActivity {


    Button search_btn;
    private String result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//화면 못움직이게 세로고정
        setContentView(R.layout.activity_main);

        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<ResponseBody> call = apiInterface.get_Test("json");
        call.enqueue(new Callback<ResponseBody>() { //실행하는 메소드
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    Log.e("D_Test", response.body().string());
                    result = response.body().string();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                result = "error!!";
                Log.e("D_Test","페일!");
            }
        });




//        search_btn = (Button) findViewById(R.id.search_btn);
//
//        search_btn.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View view)
//            {
//
//                Intent intent = new Intent(MainActivity.this, MapActivity.class);
//                startActivity(intent);
//            }
//        });

//        MapView mapView = new MapView(this);
//
//        ViewGroup mapViewContainer = (ViewGroup) findViewById(R.id.map_view);
//        mapViewContainer.addView(mapView);
    }
}