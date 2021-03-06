package cnu.lineup.com.cnulineup;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.kakao.auth.ApiResponseCallback;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();

    public static InterstitialAd interstitialAd;
    public static String serverIP = "168.188.127.132";
    public static Button btnRefresh;
    public static JSONObject currentProportion;
    public static DBOpenHelper dbOpenHelper;
    public static SupportMapFragment mapFragment;

    private ToggleButton btnBob, btnNoodle, btnCafe, btnDrink, btnFastfood, btnFork,
            btnFavorite, btnVote;
    private ToggleButton btnBobInfo, btnNoodleInfo, btnCafeInfo, btnDrinkInfo, btnFastfoodInfo
            ,btnForkInfo;
    private ImageButton btnSearch;
    private TabHost tabHost;
    private ExpandListAdapter expAdapter;
    private ArrayList<Group> expListItems;
    private ExpandableListView expandListFavorite, expandListVote;
    private int lastExpandedPosition = -1;
    private ImageView kakaoProfile;
    private Bitmap kakaoThumbnail;

    private ProgressDialog pd;
    private LinearLayout frameFavorite, frameVote;
    private ViewPager vp;
    public static TextView voteCount;

    private LineChart lineChart;
    private PieChart pieChart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 서버에서 정보 한번에 다운받는다.
        try {
            MainActivity.currentProportion = new UtilMethod.threadVote(MainActivity.this)
                    .execute().get();
            Log.e(TAG,"hello");
        }catch (ExecutionException e){
            e.printStackTrace();
        }catch (InterruptedException e){
            e.printStackTrace();
        }


        //setFullAd(); //메인엑티비티 로딩되면 광고 요청날림
        dbOpenHelper = new DBOpenHelper(this).open();


        /** status bar 색상 변경 */
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(this.getResources().getColor(R.color.colorBlack));
        }

        /**
         * 남은 투표횟수 갱신
         */
        Typeface customFont;
        customFont = Typeface.createFromAsset(getAssets(),"fonts/BMHANNA.ttf");

        voteCount = (TextView)findViewById(R.id.text_count);
        voteCount.setTypeface(customFont);
        voteCount.setText("남은 투표 횟수 : " + dbOpenHelper.resetCount() + "회");


        tabHost = (TabHost) findViewById(R.id.footer);

        btnBob = (ToggleButton) findViewById(R.id.btn_category_bob);
        btnNoodle = (ToggleButton) findViewById(R.id.btn_category_noodle);
        btnCafe = (ToggleButton) findViewById(R.id.btn_category_cafe);
        btnDrink = (ToggleButton) findViewById(R.id.btn_category_beer);
        btnFastfood = (ToggleButton) findViewById(R.id.btn_category_fastfood);
        btnFork = (ToggleButton) findViewById(R.id.btn_category_fork);

        btnBobInfo = (ToggleButton) findViewById(R.id.btn_category_bob_info);
        btnNoodleInfo = (ToggleButton) findViewById(R.id.btn_category_noodle_info);
        btnCafeInfo = (ToggleButton) findViewById(R.id.btn_category_cafe_info);
        btnDrinkInfo = (ToggleButton) findViewById(R.id.btn_category_beer_info);
        btnFastfoodInfo = (ToggleButton) findViewById(R.id.btn_category_fastfood_info);
        btnForkInfo = (ToggleButton) findViewById(R.id.btn_category_fork_info);

        btnSearch = (ImageButton) findViewById(R.id.btn_search);
        btnFavorite = (ToggleButton) findViewById(R.id.btn_favorite);
        btnVote = (ToggleButton) findViewById(R.id.btn_vote_list);
        frameFavorite = (LinearLayout) findViewById(R.id.layout_favorite);
        frameVote = (LinearLayout) findViewById(R.id.layout_vote_list);

        /** Resgister ViewPager **/
        btnBob.setOnClickListener(movePageListener);
        btnBob.setTag(0);
        btnNoodle.setOnClickListener(movePageListener);
        btnNoodle.setTag(1);
        btnFastfood.setOnClickListener(movePageListener);
        btnFastfood.setTag(2);
        btnFork.setOnClickListener(movePageListener);
        btnFork.setTag(3);
        btnCafe.setOnClickListener(movePageListener);
        btnCafe.setTag(4);
        btnDrink.setOnClickListener(movePageListener);
        btnDrink.setTag(5);

        btnBobInfo.setOnClickListener(movePageListenerInfo);
        btnNoodleInfo.setOnClickListener(movePageListenerInfo);
        btnFastfoodInfo.setOnClickListener(movePageListenerInfo);
        btnForkInfo.setOnClickListener(movePageListenerInfo);
        btnCafeInfo.setOnClickListener(movePageListenerInfo);
        btnDrinkInfo.setOnClickListener(movePageListenerInfo);


        /** ViewPager Setting **/
        vp = (ViewPager) findViewById(R.id.vp);
        vp.setAdapter(new pagerAdapter(getSupportFragmentManager()));
        vp.setCurrentItem(0);
        vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //현재 페이지가 스크롤되었을때 발생하는 CallBack함수
            }

            @Override
            public void onPageSelected(int position) {
                disable_all_button();
                switch (position){
                    case 0:
                        btnBob.setChecked(true);
                        break;
                    case 1:
                        btnNoodle.setChecked(true);
                        break;
                    case 2:
                        btnFastfood.setChecked(true);
                        break;
                    case 3:
                        btnFork.setChecked(true);
                        break;
                    case 4:
                        btnCafe.setChecked(true);
                        break;
                    case 5:
                        btnDrink.setChecked(true);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                //스크롤 상태가 변했을 경우에 불리는 함수인데 스크롤 상태가 변한것이 어떤상태인지 아직 파악 못함
            }
        });
        btnBob.setChecked(true);



        /**
         * 새로고침 버튼
         * 서버에서 새로운 데이터를 받아 Refresh시킴
         * TODO :  화면에도 바로 갱신시켜주는 것 필요함 */
        btnRefresh = (Button) findViewById(R.id.btn_refresh);
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    currentProportion = new UtilMethod.threadVote(MainActivity.this).execute().get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Toast.makeText(MainActivity.this, "데이터 새로고침", Toast.LENGTH_SHORT).show();
            }
        });


        /** 검색버튼 */
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Search Activity 로 이동하는 코드
                Intent intent_search = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent_search);
            }
        });


        /**
         * 밑에 탭 이미지 등록
         */
        ImageView tab_home_icon = new ImageView(this);
        tab_home_icon.setImageResource(R.drawable.selector_tab_home);
        ImageView tab_info_icon = new ImageView(this);
        tab_info_icon.setImageResource(R.drawable.selector_tab_info);
        ImageView tab_account_icon = new ImageView(this);
        tab_account_icon.setImageResource(R.drawable.selector_tab_account);
        ImageView tab_statistics_icon = new ImageView(this);
        tab_statistics_icon.setImageResource(R.drawable.selector_tab_statistics);


        /** 카카오 프로필사진 가져오기 */
        kakaoProfile = (ImageView) findViewById(R.id.kakao_thumbnail);
        Thread getThumbnail = new Thread() {
            @Override
            public void run() {
                try {
                    URL url = new URL(UserInfo.PROFILE_IMAGE_PATH);

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true);
                    conn.connect();

                    InputStream is = conn.getInputStream();
                    kakaoThumbnail = BitmapFactory.decodeStream(is);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                super.run();
            }
        };
        getThumbnail.start();
        try {
            getThumbnail.join(); //쓰레드가 끝나기전에 이미지설정을 하면 안되므로 join으로 기다리기
            kakaoProfile.setImageBitmap(kakaoThumbnail);
        } catch (Exception e) {
            e.printStackTrace();
        }


        /**
         * 하단 탭 등록
         */
        tabHost.setup();
        TabHost.TabSpec tab1 = tabHost.newTabSpec("Tab1").setContent(R.id.tab1)
                .setIndicator(tab_home_icon);
        TabHost.TabSpec tab2 = tabHost.newTabSpec("Tab2").setContent(R.id.tab2)
                .setIndicator(tab_info_icon);
        TabHost.TabSpec tab3 = tabHost.newTabSpec("Tab3").setContent(R.id.tab3)
                .setIndicator(tab_statistics_icon);
        TabHost.TabSpec tab4 = tabHost.newTabSpec("Tab4").setContent(R.id.tab4)
                .setIndicator(tab_account_icon);

        tabHost.addTab(tab1);
        tabHost.addTab(tab2);
        tabHost.addTab(tab3);
        tabHost.addTab(tab4);

        /**
         * 통계 탭
         */
        lineChart = (LineChart)findViewById(R.id.line_chart);
        List<Entry> entries = new ArrayList<Entry>();


        entries.add(new Entry(1,1));
        entries.add(new Entry(1.2f,1.2f));
        entries.add(new Entry(1.4f,1.4f));
        entries.add(new Entry(1.6f,1.6f));


        LineDataSet dataSet = new LineDataSet(entries, "Example"); // add entries to dataset

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        //lineChart.invalidate(); // refresh
        lineChart.animateX(10000, Easing.EasingOption.EaseOutBack);

        pieChart = (PieChart)findViewById(R.id.pie_chart);
        setData(4,100);





        /**
         * 내정보 탭
         */
        btnFavorite.setOnClickListener(userInfoListener);
        btnVote.setOnClickListener(userInfoListener);
        expandListFavorite = (ExpandableListView) findViewById(R.id.list_favorite);
        expandListVote = (ExpandableListView) findViewById(R.id.list_vote);
        expListItems = UtilMethod.setItemsFavorite();
        setExpandListAdapter(expandListFavorite, expListItems);


        //지도 코드

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                ArrayList<Double> longitudeList = new ArrayList<Double>();
                ArrayList<Double> latitudeList = new ArrayList<Double>();

                googleMap.clear();
                ArrayList<String> positionList = getPosition("bob");
                Iterator<String> iter = positionList.iterator();
                while(iter.hasNext()){
                    String[] position = iter.next().split(",");
                    double longitude = Double.parseDouble(position[0]);
                    double latitude = Double.parseDouble(position[1]);
                    String title = position[2];
                    googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(longitude,latitude))
                            .title(title));

                    longitudeList.add(longitude);
                    latitudeList.add(latitude);
                }
                double centerLongitude=0, centerLatitude = 0;
                for(Double longitude :longitudeList){
                    centerLongitude += longitude;
                }
                centerLongitude /= longitudeList.size();

                for(Double latitude : latitudeList){
                    centerLatitude += latitude;
                }
                centerLatitude /= latitudeList.size();

                googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        String title = marker.getTitle();
                        Bundle arg = new Bundle();
                        arg.putString("title",title);
                        Intent intent = new Intent(MainActivity.this, RestaurantInfoActivity.class);
                        intent.putExtras(arg);
                        startActivity(intent);
                    }
                });
                LatLng gungdong = new LatLng(centerLongitude, centerLatitude);
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gungdong,15));
            }
        });

        tabHost.getTabWidget().getChildTabViewAt(2).setEnabled(false);




    }


    Button.OnClickListener userInfoListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_favorite:
                    btnVote.setChecked(false);
                    btnFavorite.setChecked(true);
                    frameFavorite.setVisibility(LinearLayout.VISIBLE);
                    frameVote.setVisibility(LinearLayout.INVISIBLE);
                    expListItems = UtilMethod.setItemsFavorite();
                    setExpandListAdapter(expandListFavorite, expListItems);
                    break;
                case R.id.btn_vote_list:
                    btnVote.setChecked(true);
                    btnFavorite.setChecked(false);
                    expListItems = UtilMethod.setItemsVote();
                    setExpandListAdapter(expandListVote, expListItems);
                    frameFavorite.setVisibility(LinearLayout.INVISIBLE);
                    frameVote.setVisibility(LinearLayout.VISIBLE);

                    break;
            }
        }
    };


    /**
     * ExpandListView 어댑터 설정 함수.
     * 총 3개의 ExpandListView(전체 가게, 즐겨찾기, 투표한 리스트)들에 대하여 어댑터 설정
     * @param expandableListView
     * @param ExpListItems
     */
    public void setExpandListAdapter(final ExpandableListView expandableListView, ArrayList<Group> ExpListItems) {
        expAdapter = new ExpandListAdapter(MainActivity.this, ExpListItems, expandableListView);
        expandableListView.setAdapter(expAdapter);
        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                if (lastExpandedPosition != -1
                        && groupPosition != lastExpandedPosition) {
                    expandableListView.collapseGroup(lastExpandedPosition);
                }
                lastExpandedPosition = groupPosition;
            }
        });
    }



    private void setFullAd() {
        interstitialAd = new InterstitialAd(this); //새 광고를 만듭니다.
        interstitialAd.setAdUnitId(getResources().getString(R.string.ad_test_id)); //이전에 String에 저장해 두었던 광고 ID를 전면 광고에 설정합니다.
        AdRequest adRequest1 = new AdRequest.Builder().build(); //새 광고요청
        interstitialAd.loadAd(adRequest1); //요청한 광고를 load 합니다.
        interstitialAd.setAdListener(new AdListener() { //전면 광고의 상태를 확인하는 리스너 등록

            @Override
            public void onAdClosed() { //전면 광고가 열린 뒤에 닫혔을 때
                AdRequest adRequest1 = new AdRequest.Builder().build();  //새 광고요청
                interstitialAd.loadAd(adRequest1); //요청한 광고를 load 합니다.
            }
        });

    }


    public static void displayAD(Context context) {
        if (interstitialAd.isLoaded()) {
            interstitialAd.show();
        } else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setMessage("광고 요청중입니다. 다시 시도해 주세요.");
            dialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            dialog.show();
        }
    }

    protected void requestUpdateProfile(String sex, String age) { //유저의 정보를 받아오는 함수
        final Map<String, String> properties = new HashMap<String, String>();
        properties.put("sex", sex);
        properties.put("age", age);

        UserManagement.requestUpdateProfile(new ApiResponseCallback<Long>() {
            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                Log.e(TAG, errorResult.getErrorMessage());
            }

            @Override
            public void onNotSignedUp() {

            }

            @Override
            public void onSuccess(Long userId) {
                Log.i(TAG, "succeeded to update user profile");
            }
        }, properties);
    }


    private class pagerAdapter extends FragmentStatePagerAdapter {
        public pagerAdapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            /**
             * ViewPager특성상 0 과 1번째 페이지는 디폴트로 로딩된다.(어쩔수 없이 불림)
             */
            switch (position) {
                case 0:
                    return FragList.newInstance("bob");
                case 1:
                    return FragList.newInstance("noodle");
                case 2:
                    return FragList.newInstance("fastfood");
                case 3:
                    return FragList.newInstance("meat");
                case 4:
                    return FragList.newInstance("cafe");
                case 5:
                    return FragList.newInstance("drink");
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 6;
        }

    }

    /**
     * 메인화면 상단에 Toggle Button들의 OnClickListener
     */
    View.OnClickListener movePageListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int tag = (int) v.getTag();
            if (vp.getCurrentItem() != tag) {
                vp.setCurrentItem(tag);
            }
        }
    };

    /**
     * 식당정보의 Toggle Button들의 OnClickListener
     */
    View.OnClickListener movePageListenerInfo = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            disable_all_button_info(); // 다른 모든 토글버든 비활성화
            final ArrayList<Double> longitudeList = new ArrayList<Double>();
            final ArrayList<Double> latitudeList = new ArrayList<Double>();

            switch(v.getId()){
                case R.id.btn_category_bob_info:
                    btnBobInfo.setChecked(true);
                    mapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            googleMap.clear();
                            ArrayList<String> positionList = getPosition("bob");
                            Iterator<String> iter = positionList.iterator();
                            while(iter.hasNext()){
                                String[] position = iter.next().split(",");
                                double longitude = Double.parseDouble(position[0]);
                                double latitude = Double.parseDouble(position[1]);
                                String title = position[2];
                                googleMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(longitude,latitude))
                                        .title(title));

                                longitudeList.add(longitude);
                                latitudeList.add(latitude);
                            }
                            double centerLongitude=0, centerLatitude = 0;
                            for(Double longitude :longitudeList){
                                centerLongitude += longitude;
                            }
                            centerLongitude /= longitudeList.size();

                            for(Double latitude : latitudeList){
                                centerLatitude += latitude;
                            }
                            centerLatitude /= latitudeList.size();

                            LatLng gungdong = new LatLng(centerLongitude, centerLatitude);
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gungdong,15));
                        }
                    });
                    break;
                case R.id.btn_category_noodle_info:
                    btnNoodleInfo.setChecked(true);
                    mapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            googleMap.clear();
                            ArrayList<String> positionList = getPosition("noodle");
                            Iterator<String> iter = positionList.iterator();
                            while(iter.hasNext()){
                                String[] position = iter.next().split(",");
                                double longitude = Double.parseDouble(position[0]);
                                double latitude = Double.parseDouble(position[1]);
                                String title = position[2];
                                googleMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(longitude,latitude))
                                        .title(title));

                                longitudeList.add(longitude);
                                latitudeList.add(latitude);
                            }
                            double centerLongitude=0, centerLatitude = 0;
                            for(Double longitude :longitudeList){
                                centerLongitude += longitude;
                            }
                            centerLongitude /= longitudeList.size();

                            for(Double latitude : latitudeList){
                                centerLatitude += latitude;
                            }
                            centerLatitude /= latitudeList.size();

                            LatLng gungdong = new LatLng(centerLongitude, centerLatitude);
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gungdong,15));
                        }
                    });
                    break;
                case R.id.btn_category_fastfood_info:
                    btnFastfoodInfo.setChecked(true);
                    mapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            googleMap.clear();
                            ArrayList<String> positionList = getPosition("fastfood");
                            Iterator<String> iter = positionList.iterator();
                            while(iter.hasNext()){
                                String[] position = iter.next().split(",");
                                double longitude = Double.parseDouble(position[0]);
                                double latitude = Double.parseDouble(position[1]);
                                String title = position[2];
                                googleMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(longitude,latitude))
                                        .title(title));

                                longitudeList.add(longitude);
                                latitudeList.add(latitude);
                            }
                            double centerLongitude=0, centerLatitude = 0;
                            for(Double longitude :longitudeList){
                                centerLongitude += longitude;
                            }
                            centerLongitude /= longitudeList.size();

                            for(Double latitude : latitudeList){
                                centerLatitude += latitude;
                            }
                            centerLatitude /= latitudeList.size();

                            LatLng gungdong = new LatLng(centerLongitude, centerLatitude);
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gungdong,15));
                        }
                    });
                    break;
                case R.id.btn_category_fork_info:
                    btnForkInfo.setChecked(true);
                    mapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            googleMap.clear();
                            ArrayList<String> positionList = getPosition("meat");
                            Iterator<String> iter = positionList.iterator();
                            while(iter.hasNext()){
                                String[] position = iter.next().split(",");
                                double longitude = Double.parseDouble(position[0]);
                                double latitude = Double.parseDouble(position[1]);
                                String title = position[2];
                                googleMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(longitude,latitude))
                                        .title(title));

                                longitudeList.add(longitude);
                                latitudeList.add(latitude);
                            }
                            double centerLongitude=0, centerLatitude = 0;
                            for(Double longitude :longitudeList){
                                centerLongitude += longitude;
                            }
                            centerLongitude /= longitudeList.size();

                            for(Double latitude : latitudeList){
                                centerLatitude += latitude;
                            }
                            centerLatitude /= latitudeList.size();

                            LatLng gungdong = new LatLng(centerLongitude, centerLatitude);
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gungdong,15));
                        }
                    });
                    break;
                case R.id.btn_category_cafe_info:
                    btnCafeInfo.setChecked(true);
                    mapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            googleMap.clear();
                            ArrayList<String> positionList = getPosition("cafe");
                            Iterator<String> iter = positionList.iterator();
                            while(iter.hasNext()){
                                String[] position = iter.next().split(",");
                                double longitude = Double.parseDouble(position[0]);
                                double latitude = Double.parseDouble(position[1]);
                                String title = position[2];
                                googleMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(longitude,latitude))
                                        .title(title));

                                longitudeList.add(longitude);
                                latitudeList.add(latitude);
                            }
                            double centerLongitude=0, centerLatitude = 0;
                            for(Double longitude :longitudeList){
                                centerLongitude += longitude;
                            }
                            centerLongitude /= longitudeList.size();

                            for(Double latitude : latitudeList){
                                centerLatitude += latitude;
                            }
                            centerLatitude /= latitudeList.size();

                            LatLng gungdong = new LatLng(centerLongitude, centerLatitude);
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gungdong,15));
                        }
                    });
                    break;
                case R.id.btn_category_beer_info:
                    btnDrinkInfo.setChecked(true);
                    mapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            googleMap.clear();
                            ArrayList<String> positionList = getPosition("drink");
                            Iterator<String> iter = positionList.iterator();
                            while(iter.hasNext()){
                                String[] position = iter.next().split(",");
                                double longitude = Double.parseDouble(position[0]);
                                double latitude = Double.parseDouble(position[1]);
                                String title = position[2];
                                googleMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(longitude,latitude))
                                        .title(title));

                                longitudeList.add(longitude);
                                latitudeList.add(latitude);
                            }
                            double centerLongitude=0, centerLatitude = 0;
                            for(Double longitude :longitudeList){
                                centerLongitude += longitude;
                            }
                            centerLongitude /= longitudeList.size();

                            for(Double latitude : latitudeList){
                                centerLatitude += latitude;
                            }
                            centerLatitude /= latitudeList.size();

                            LatLng gungdong = new LatLng(centerLongitude, centerLatitude);
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gungdong,15));
                        }
                    });
                    break;
                default:
                    btnBobInfo.setChecked(true);
                    mapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            googleMap.clear();
                            ArrayList<String> positionList = getPosition("bob");
                            Iterator<String> iter = positionList.iterator();
                            while(iter.hasNext()){
                                String[] position = iter.next().split(",");
                                double longitude = Double.parseDouble(position[0]);
                                double latitude = Double.parseDouble(position[1]);
                                String title = position[2];
                                googleMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(longitude,latitude))
                                        .title(title));

                                longitudeList.add(longitude);
                                latitudeList.add(latitude);
                            }
                            double centerLongitude=0, centerLatitude = 0;
                            for(Double longitude :longitudeList){
                                centerLongitude += longitude;
                            }
                            centerLongitude /= longitudeList.size();

                            for(Double latitude : latitudeList){
                                centerLatitude += latitude;
                            }
                            centerLatitude /= latitudeList.size();

                            LatLng gungdong = new LatLng(centerLongitude, centerLatitude);
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gungdong,15));
                        }
                    });
                    break;
            }

        }
    };

    public ArrayList<String> getPosition(String catgeory) {
        try {

            ArrayList<String> result = new ArrayList<String>();
            JSONArray categoryRestaurant = currentProportion.getJSONArray(catgeory);


            for(int i = 0; i<categoryRestaurant.length(); i++){
                JSONObject row = categoryRestaurant.getJSONObject(i);
                String longitude = (String)row.get("longitude");
                String latitude = (String)row.get("latitude");
                String name = (String)row.get("title");
                result.add(longitude + "," +latitude + "," + name);
            }

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 모든 Toggle 버튼을 disable시키는 함수
     */
    public void disable_all_button() {
        btnBob.setChecked(false);
        btnNoodle.setChecked(false);
        btnFastfood.setChecked(false);
        btnFork.setChecked(false);
        btnCafe.setChecked(false);
        btnDrink.setChecked(false);
    }

    public void disable_all_button_info() {
        btnBobInfo.setChecked(false);
        btnNoodleInfo.setChecked(false);
        btnFastfoodInfo.setChecked(false);
        btnForkInfo.setChecked(false);
        btnCafeInfo.setChecked(false);
        btnDrinkInfo.setChecked(false);
    }

    //PieChart 데이터
    private void setData(int count, float range) {

        float mult = range;

        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();

        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.
        for (int i = 0; i < count ; i++) {
            entries.add(new PieEntry((float) ((Math.random() * mult) + mult / 5)));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Election Results");

        dataSet.setDrawIcons(false);

        dataSet.setSliceSpace(3f);
        dataSet.setIconsOffset(new MPPointF(0, 40));
        dataSet.setSelectionShift(5f);

        // add a lot of colors

        ArrayList<Integer> colors = new ArrayList<Integer>();

        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);

        colors.add(ColorTemplate.getHoloBlue());

        dataSet.setColors(colors);
        //dataSet.setSelectionShift(0f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);

        pieChart.setData(data);

        // undo all highlights
        pieChart.highlightValues(null);

        pieChart.invalidate();
    }

}
