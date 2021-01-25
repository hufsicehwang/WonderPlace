package com.taehyuk.wonderplace;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.taehyuk.wonderplace.adapter.LocationAdapter;
import com.taehyuk.wonderplace.model.address_search.AddressSearch;
import com.taehyuk.wonderplace.model.category_search.CategoryResult;
import com.taehyuk.wonderplace.model.category_search.Document;


import net.daum.mf.map.api.MapCircle;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapReverseGeoCoder;
import net.daum.mf.map.api.MapView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static net.daum.mf.map.api.MapPoint.mapPointWithGeoCoord;


public class MapActivity extends AppCompatActivity implements MapView.CurrentLocationEventListener, MapReverseGeoCoder.ReverseGeoCodingResultListener, MapView.MapViewEventListener, MapView.POIItemEventListener, MapView.OpenAPIKeyAuthenticationResultListener {

    private static final String LOG_TAG = "MapActivity";

    private MapView mMapView;
    ViewGroup mMapViewContainer;
    private Button btn;
    private Button search_btn;
    private Button currentlocation;
    EditText mSearchEdit;
    RecyclerView recyclerView;


    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION};

    MapPoint currentMapPoint;
    private double mCurrentLng; //Long = X, Lat = Yㅌ
    private double mCurrentLat;
    private double mSearchLng = -1;
    private double mSearchLat = -1;
    private String mSearchName;
    boolean isTrackingMode = false;
    Bus bus = BusProvider.getInstance();
    ArrayList<Document> bigMartList = new ArrayList<>();

    ArrayList<Document> documentArrayList = new ArrayList<>(); //지역명 검색 결과 리스트

    MapPOIItem searchMarker = new MapPOIItem();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//화면 못움직이게
        setContentView(R.layout.activity_map);

//        mMapView = (MapView) findViewById(R.id.map_view);
        mMapView = new MapView(this);
        mMapViewContainer = findViewById(R.id.map_view);
        mMapViewContainer.addView(mMapView);
        bus.register(this); //정류소 등록


        btn = findViewById(R.id.btn);
        btn.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                requestSearchLocal(mCurrentLng, mCurrentLat);
            }
        });

//        search_btn = findViewById(R.id.search_btn);
//        search_btn.setOnClickListener(new Button.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                isTrackingMode = false;
//                mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);
//                if (mSearchLat != -1 && mSearchLng != -1) {
//                    mMapView.removeAllPOIItems();
//                    mMapView.removeAllCircles();
//                    mMapView.addPOIItem(searchMarker);
//                    requestSearchLocal(mSearchLng, mSearchLat);
//                } else {
//                }
//
//            }
//        });

        currentlocation = findViewById(R.id.currentlocation);
        currentlocation.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!checkLocationServicesStatus()) {

                    showDialogForLocationServiceSetting();
                }else {

                    checkRunTimePermission();


                }
            }
        });







        mSearchEdit = findViewById(R.id.map_et_search);
        recyclerView = findViewById(R.id.map_recyclerview);
        LocationAdapter locationAdapter = new LocationAdapter(documentArrayList, getApplicationContext(), mSearchEdit, recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false); //레이아웃매니저 생성
        recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL)); //아래구분선 세팅
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(locationAdapter);

        mSearchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                // 입력하기 전에
                recyclerView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (charSequence.length() >= 1) {
                    // if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {

                    documentArrayList.clear();
                    locationAdapter.clear();
                    locationAdapter.notifyDataSetChanged();
                    ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
                    Call<CategoryResult> call = apiInterface.getSearchLocation( charSequence.toString(), 15);
                    call.enqueue(new Callback<CategoryResult>() {
                        @Override
                        public void onResponse(@NotNull Call<CategoryResult> call, @NotNull Response<CategoryResult> response) {
                            if (response.isSuccessful()) {
                                assert response.body() != null;
                                for (Document document : response.body().getDocuments()) {
                                    locationAdapter.addItem(document);
                                }
                                locationAdapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onFailure(@NotNull Call<CategoryResult> call, @NotNull Throwable t) {

                        }
                    });
                    //}
                    //mLastClickTime = SystemClock.elapsedRealtime();
                } else {
                    if (charSequence.length() <= 0) {
                        recyclerView.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // 입력이 끝났을 때
            }
        });

        mSearchEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                } else {
                    recyclerView.setVisibility(View.GONE);
                }
            }
        });
        mSearchEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                FancyToast.makeText(getApplicationContext(), "검색리스트에서 장소를 선택해주세요", FancyToast.LENGTH_SHORT, FancyToast.INFO, true).show();
            }
        });




        //mMapView.setDaumMapApiKey(MapApiConst.DAUM_MAPS_ANDROID_APP_API_KEY);
//        mMapView.setCurrentLocationEventListener(this);



//        mMapView.setMapViewEventListener(this); // this에 MapView.MapViewEventListener 구현.
//        mMapView.setPOIItemEventListener(this);
//        mMapView.setOpenAPIKeyAuthenticationResultListener(this);

//        mMapView.setCurrentLocationEventListener(this);
//        //setCurrentLocationTrackingMode (지도랑 현재위치 좌표 찍어주고 따라다닌다.)
//        mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);



//        requestSearchLocal(mCurrentLng, mCurrentLat);
//        mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
//        mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);




//        if (!checkLocationServicesStatus()) {
//
//            showDialogForLocationServiceSetting();
//        }else {
//
//            checkRunTimePermission();
//
//
//        }

//        mMapView.setCurrentLocationEventListener(this);
//        requestSearchLocal(mCurrentLng, mCurrentLat);

    }


    private void requestSearchLocal(double x, double y) {

        //여밑에가 통신 코드를 메인에서 세팅해서 필요한거 불러오기
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);   // ApiInterface 객체에 레트로핏 객체를 매핑시키는 부분이다.
        Call<CategoryResult> call = apiInterface.getSearchCategory("MT1", x + "", y + "", 1000);
        call.enqueue(new Callback<CategoryResult>() {
            @Override
            public void onResponse(@NonNull Call<CategoryResult> call, @NonNull Response<CategoryResult> response) {

                if (response.isSuccessful()) {


                    bigMartList.addAll(response.body().getDocuments());
                    Log.d(LOG_TAG, "bigMartList Success");


//                   통신 성공 시 circle 생성
                    MapCircle circle1 = new MapCircle(
                            mapPointWithGeoCoord(y, x), // center
                            1000, // radius
                            Color.argb(128, 255, 0, 0), // strokeColor
                            Color.argb(128, 0, 255, 0) // fillColor
                    );
                    circle1.setTag(1234);
                    mMapView.addCircle(circle1);
                    Log.d("SIZE1", bigMartList.size() + "");

                    int tagNum = 0;
                    for (Document document : bigMartList) {
                        MapPOIItem marker = new MapPOIItem();

                        marker.setItemName(document.getPlaceName());
                        marker.setTag(tagNum++);
                        double x = Double.parseDouble(document.getY());
                        double y = Double.parseDouble(document.getX());
                        //카카오맵은 참고로 new MapPoint()로  생성못함. 좌표기준이 여러개라 이렇게 메소드로 생성해야함
                        MapPoint mapPoint = mapPointWithGeoCoord(x, y);
                        marker.setMapPoint(mapPoint);
                        marker.setMarkerType(MapPOIItem.MarkerType.BluePin);
//                        marker.setMarkerType(MapPOIItem.MarkerType.CustomImage); // 마커타입을 커스텀 마커로 지정.
//                        marker.setCustomImageResourceId(R.drawable.ic_add_black_24dp); // 마커 이미지.
//                        marker.setCustomImageAutoscale(false); // hdpi, xhdpi 등 안드로이드 플랫폼의 스케일을 사용할 경우 지도 라이브러리의 스케일 기능을 꺼줌.
//                        marker.setCustomImageAnchor(0.5f, 1.0f); // 마커 이미지중 기준이 되는 위치(앵커포인트) 지정 - 마커 이미지 좌측 상단 기준 x(0.0f ~ 1.0f), y(0.0f ~ 1.0f) 값.
                        mMapView.addPOIItem(marker);
                    }

                }
            }

            @Override
            public void onFailure(Call<CategoryResult> call, Throwable t) {
                Log.d(LOG_TAG, "FAIL");

            }
        });



    }





    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);
        mMapView.setShowCurrentLocationMarker(false);
    }

    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint currentLocation, float accuracyInMeters) {
        MapPoint.GeoCoordinate mapPointGeo = currentLocation.getMapPointGeoCoord();
        Log.i(LOG_TAG, String.format("MapView onCurrentLocationUpdate (%f,%f) accuracy (%f)", mapPointGeo.latitude, mapPointGeo.longitude, accuracyInMeters));
        currentMapPoint = MapPoint.mapPointWithGeoCoord(mapPointGeo.latitude, mapPointGeo.longitude);
//        이 좌표로 지도 중심 이동
        mMapView.setMapCenterPoint(currentMapPoint, true);
//        전역변수로 현재 좌표 저장
        mCurrentLat = mapPointGeo.latitude;
        mCurrentLng = mapPointGeo.longitude;
        Log.d(LOG_TAG, "현재위치 => " + mCurrentLat + "  " + mCurrentLng);
//        mLoaderLayout.setVisibility(View.GONE);
        //트래킹 모드가 아닌 단순 현재위치 업데이트일 경우, 한번만 위치 업데이트하고 트래킹을 중단시키기 위한 로직
        if (!isTrackingMode) {
            mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);
        }


    }


    @Override
    public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {

    }

    @Override
    public void onCurrentLocationUpdateFailed(MapView mapView) {

    }

    @Override
    public void onCurrentLocationUpdateCancelled(MapView mapView) {

    }

    @Override
    public void onReverseGeoCoderFoundAddress(MapReverseGeoCoder mapReverseGeoCoder, String s) {
        mapReverseGeoCoder.toString();
        onFinishReverseGeoCoding(s);
    }

    @Override
    public void onReverseGeoCoderFailedToFindAddress(MapReverseGeoCoder mapReverseGeoCoder) {
        onFinishReverseGeoCoding("Fail");
    }

    private void onFinishReverseGeoCoding(String result) {
//        Toast.makeText(LocationDemoActivity.this, "Reverse Geo-coding : " + result, Toast.LENGTH_SHORT).show();
    }




    /*
     * ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드입니다.
     */
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if ( permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면

            boolean check_result = true;


            // 모든 퍼미션을 허용했는지 체크합니다.

            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }


            if ( check_result ) {
                Log.d("@@@", "start");
                //위치 값을 가져올 수 있음
                mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithHeading);
            }
            else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {

                    Toast.makeText(com.taehyuk.wonderplace.MapActivity.this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                    finish();


                }else {

                    Toast.makeText(com.taehyuk.wonderplace.MapActivity.this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();

                }
            }

        }
    }

    void checkRunTimePermission(){

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(com.taehyuk.wonderplace.MapActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED ) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)


            // 3.  위치 값을 가져올 수 있음

            mMapView.setCurrentLocationEventListener(this);

            mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithHeading);
//            requestSearchLocal(mCurrentLng, mCurrentLat);



        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(com.taehyuk.wonderplace.MapActivity.this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(com.taehyuk.wonderplace.MapActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(com.taehyuk.wonderplace.MapActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);


            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(com.taehyuk.wonderplace.MapActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }

    }



    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(com.taehyuk.wonderplace.MapActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
                        checkRunTimePermission();
                        return;
                    }
                }

                break;
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    public void onMapViewInitialized(MapView mapView) {

    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {

    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {

    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {

    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {

    }

    @Override
    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {

    }

    @Override
    public void onDaumMapOpenAPIKeyAuthenticationResult(MapView mapView, int i, String s) {

    }

    @Subscribe //검색예시 클릭시 이벤트 오토버스
    public void search(Document document) {//public항상 붙여줘야함
//        FancyToast.makeText(getApplicationContext(), document.getPlaceName() + " 검색", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, true).show();
        mSearchName = document.getPlaceName();
        mSearchLng = Double.parseDouble(document.getX());
        mSearchLat = Double.parseDouble(document.getY());
//        mMapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(mSearchLat, mSearchLng), true);
//        mMapView.removePOIItem(searchMarker);
        mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);
        mMapView.removeAllCircles();
        mMapView.removeAllPOIItems();
        searchMarker.setItemName(mSearchName);
        searchMarker.setTag(10000);
        MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(mSearchLat, mSearchLng);
        searchMarker.setMapPoint(mapPoint);
        searchMarker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
//        searchMarker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
        //마커 드래그 가능하게 설정
//        searchMarker.setDraggable(true);
        mMapView.addPOIItem(searchMarker);
    }

    @Override
    public void finish() {
        super.finish();
        bus.unregister(this); //이액티비티 떠나면 정류소 해제해줌
    }


}

























