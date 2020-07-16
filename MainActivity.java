package com.example.stationtimetable;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    //APIから取得した文字列格納変数
    String apiStr = "";
    //変数apiStrに含まれる各路線名を格納するList型station_name
    List<String> station_name = new ArrayList<String>();

    //路線名を格納する文字型変数line
    public static String item = "";

    //画面遷移を可能か否かを識別する論理型変数flag
    public static boolean flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button start_processing = findViewById(R.id.start_processing);
        Button intent_button = findViewById(R.id.intent_button);
        ListView lvlines = findViewById(R.id.lvlines);

        //ここから東京都に存在する路線名をAPIで取得する。

        TokyoLinesReceiver receiver = new TokyoLinesReceiver();
        //TokyoKinesReceiverを実行。
        receiver.execute();

        //画面の表示を確認できたらボタンを押すような使用にする
        start_processing.setOnClickListener(new ButtonListener());

        intent_button.setOnClickListener(new ButtonListener());

        //リストビューがタップされた時の処理を設定する
        lvlines.setOnItemClickListener(new ListItemClickListener());

    }




    /**
     * 東京都に存在する路線名を取得するメンバクラス
     */
    private class TokyoLinesReceiver extends AsyncTask<String, String, String> {


        @Override
        public String doInBackground(String... params) {
            //String id = params[0];
            String urlStr = "http://express.heartrails.com/api/json?method=getLines&prefecture=" + "東京都";

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
            try {
                //JSON文字列からJSONObjectオブジェクトを生成。これをルートJSONオブジェクトとする。
                JSONObject rootJSON = new JSONObject(result);
                //ルートJSON直下の「response」JSONオブジェクトを取得。
                JSONObject responseJSON = rootJSON.getJSONObject("response");
                //「response」プロパティ直下の「line」文字列を取得。
                desc = responseJSON.getString("line");
                System.out.println(desc);
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

    //ボタンをクリックした時のリスナクラス。
    private class ButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {

            //タップされた画面部品のidのR値を取得。
            int id = view.getId();

            //各ボタンが押された処理を記述する
            switch(id) {
                case R.id.start_processing :
                    disassembly(apiStr);
                    break;

                case R.id.intent_button :
                    if(flag) {
                        Intent intent = new Intent(MainActivity.this, Station.class);
                        //第2画面に送るデータを格納。
                        intent.putExtra("item", item);
                        //第2画面を起動
                        startActivity(intent);
                    } else {
                        Toast.makeText(MainActivity.this, "路線をお選びください", Toast.LENGTH_LONG).show();
                    }
            }
        }
    }


    //リストがタップされた時の処理が記述されたメンバクラス。
    private class ListItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //タップされた駅名を取得。
            String item = (String) parent.getItemAtPosition(position);
            System.out.println(item);


            //「路線確認」ダイアログフラグメントオブジェクトを生成。
            StationConfirmDialogFragment dialogFragment  = new StationConfirmDialogFragment(item);
            //ダイアログを表示。
            dialogFragment.show(getSupportFragmentManager(), "StationConfirmDialogFragment");

        }
    }




    //apiStrに含まれる文字列を分解するメソッド
    private void disassembly(String apiStr) {
        //駅名を格納するためのポインタ
        int st_pointer = 0;
        //駅名を格納するための配列
        String[] station_name = new String[100];
        //駅名を格納するための変数
        String station = "";
        //JSON文字列から引っ張ってきた全文字列を格納するための文字型配列apiStr
        String[] apiStr_Array = apiStr.split("");
        //ArrayListを作成する
        List<String> eki = new ArrayList<String>();

        //分解作業開始
        for(int i = 2; !(apiStr_Array[i].equals("]")); i++) {
            if(apiStr_Array[i].equals("\"")) {
                //(")を見つけたのでポインタiを1加算する
                i++;
                for(int p = i; !(apiStr_Array[p].equals("\"")); p++) {
                    station = station + apiStr_Array[p];
                    i = p;
                }
                //うまい具合に調整するために変数iに1を加算する
                i++;
                //(")を発見したので、上記のfor文を抜ける
                station_name[st_pointer] = station;
                //駅名を消す
                station = "";
                //変数st_pointerに1を加算する
                st_pointer ++;
            }
        }

        //番兵をセットする
        station_name[st_pointer] = "null";

        /*
        ここで文字列の分解作業はできたので、ListViewにセットするため
        ArrayListへ格納作業を行う
         */

        for(int i = 0; !(station_name[i].equals("null")); i++) {
            eki.add(station_name[i]);
        }


        //ArrayList eki に適切に格納されたかチェクする
        Iterator<String> it = eki.iterator();
        while(it.hasNext()) {
            String e = it.next();
            System.out.println(e);
        }

        ListView lvLines = findViewById(R.id.lvlines);

        //アダプタオブジェクトを生成
        ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, eki);
        lvLines.setAdapter(adapter);

    }

}
