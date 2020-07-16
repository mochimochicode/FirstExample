package com.example.stationtimetable;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Station extends AppCompatActivity {

    //APIから取得した文字列格納変数
    String apiStr = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station);

        //インテントオブジェクトを取得。
        Intent intent = getIntent();
        //リストから送られてきたデータを取得。
        String station_name = intent.getStringExtra("item");

        TextView tv_station_name = findViewById(R.id.tv_station_name);

        tv_station_name.setText(station_name);

        //アクションバーを取得。
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        //Button bt_search = findViewById(R.id.bt_search);
        LinesStationReceiver receiver = new LinesStationReceiver(station_name);
        //LinesStationReceiverを実行。
        receiver.execute();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //選択されたメニューIDを取得。
        int itemId = item.getItemId();
        //選択されたメニューが「戻る」の場合、アクティビティを終了。
        if(itemId == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 各路線に存在する路線名を取得するメンバクラス
     */
    private class LinesStationReceiver extends AsyncTask<String, String, String> {

        private String station_name = "";

        //コンストラクタ
        public LinesStationReceiver(String station_name) {
            this.station_name = station_name;
        }

        @Override
        public String doInBackground(String... params) {
            //可変長引数の1個目(インデックス0)を取得。これが都市ID
            //String id = params[0];
            //都市IDを使って接続URL文字列を作成。
            String urlStr = "http://express.heartrails.com/api/json?method=getStations&line=" + station_name;
            //天気情報サービスから取得したJSON文字列。天気情報が格納されている。
            String result = "";

            //http接続を行うHttpURLConnectionオブジェクトを宣言。finallyで確実に解放するためにtry外で宣言。
            HttpURLConnection con = null;
            //http接続のレスポンスデータとして取得するInputStreamオブジェクトを宣言。同じくtry外で宣言。
            InputStream is = null;
            try {
                //URLオブジェクトを生成。
                URL url = new URL(urlStr);
                //URLオブジェクトからHttpURLConnectionオブジェクトを取得。
                con = (HttpURLConnection) url.openConnection();
                //http接続メソッドを設定。
                con.setRequestMethod("GET");
                //接続。
                con.connect();
                //HttpURLConnectionオブジェクトからレスポンスデータを取得。
                is = con.getInputStream();
                //レスポンスデータであるInputStreamオブジェクトを文字列に変換。
                result = is2String(is);
            }
            catch(MalformedURLException ex) {
            }
            catch(IOException ex) {
            }
            finally {
                //HttpURLConnectionオブジェクトがnullでないなら解放。
                if(con != null) {
                    con.disconnect();
                }
                //InputStreamオブジェクトがnullでないなら解放。
                if(is != null) {
                    try {
                        is.close();
                    }
                    catch(IOException ex) {
                    }
                }
            }

            //JSON文字列を返す。
            return result;
        }

        @Override
        public void onPostExecute(String result) {

            String desc = "";
            //nullチェック用の文字型変数check_null
            String check_null = "";
            //何駅分回収するかを格納する整数型変数cycle
            int cycle = 0;

            TextView siken = findViewById(R.id.siken);
            try {

                //JSON文字列からJSONObjectオブジェクトを生成。これをルートJSONオブジェクトとする。
                JSONObject rootJSON = new JSONObject(result);
                //ルートJSON直下の「response」JSONオブジェクトを取得。
                JSONObject responseJSON = rootJSON.getJSONObject("response");
                //ルートJSON直下の[station]JSON配列を取得。
                JSONArray stationArray = responseJSON.getJSONArray("station");

                /**
                 * 各路線の駅数を求める
                 */

                for(int i = 0; i < 999; i++) {

                    JSONObject nextJSON = stationArray.getJSONObject(i);
                    check_null = nextJSON.getString("next");
                    if(check_null.equals("null")) {
                        cycle = i;
                        break;
                    }
                }

                //ここからループ処理を施す

                for(int i = 0;i < (cycle + 1); i++) {
                    JSONObject stationName = stationArray.getJSONObject(i);
                    //「station」1つ目のJSONオブジェクトから「name」文字列(駅名)を取得
                    desc = desc + stationName.getString("name") + "\n";

                }

                //試験用にテキストを配置
                siken.setText(desc);

            } catch (JSONException ex) {

            }

            apiStr = desc;
        }


        private String is2String(InputStream is) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuffer sb = new StringBuffer();
            char[] b = new char[1024];
            int line;
            while (0 <= (line = reader.read(b))) {
                sb.append(b, 0, line);
            }
            return sb.toString();
        }
    }

    /**
     * 戻るボタンを押した時の処理。
     */
    public void onBackButtonClick(View view) {
        finish();
    }

}
