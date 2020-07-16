package com.example.stationtimetable;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.animation.ScaleAnimation;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

public class StationConfirmDialogFragment extends DialogFragment {

    //選択された路線を格納する文字型変数station
    String line = "";

    //コンストラクタ
    public StationConfirmDialogFragment(String line) {

        this.line = line;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //ダイアログビルダーを生成。
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //ダイアログのタイトルを設定。
        builder.setTitle("路線確認");
        //ダイアログのメッセージを設定。
        builder.setTitle(line);
        //Positive Buttonを設定。
        builder.setPositiveButton(R.string.dialog_btn_ok, new DialogButtonClickListener(line));
        //Negative Buttonを設定。
        builder.setNegativeButton(R.string.dialog_btn_ng, new DialogButtonClickListener(line));
        //ダイアログオブジェクトを生成し、リターン。
        AlertDialog dialog = builder.create();
        return dialog;
    }

    /**
     * ダイアログのアクションボタンがタップされた時の処理が記述されたメンバクラス。
     */
    private class DialogButtonClickListener implements DialogInterface.OnClickListener {


        //選択された路線を格納する文字型変数line
        String line = "";

        //コンストラクタ
        public DialogButtonClickListener(String line) {

            this.line = line;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {

            //タップされたアクションボタンで分岐。
            switch(which) {
                //Positive Buttonならば・・・
                case DialogInterface.BUTTON_POSITIVE :
                    //ここに具体的な処理を記述
                    MainActivity.flag = true;
                    Toast.makeText(getActivity(), line + "を選びましたね\n検索ボタンを押してください" , Toast.LENGTH_LONG).show();
                    MainActivity.item = line;
                    break;

                case DialogInterface.BUTTON_NEGATIVE :
                    //ここに具体的な処理を記述
                    Toast.makeText(getActivity(), "路線をお選びください", Toast.LENGTH_LONG).show();
                    break;

            }

        }

    }
}
