package com.example.meditation;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
public class MainActivity extends AppCompatActivity {
    private boolean isPlaying = false;
    private boolean isPaused = false;

    private Button playButton, pauseButton, stopButton;
    private EditText breatheInInput, breatheOutInput, cycleInput, preparationInput;
    private TextView totalTimeDisplay, countdownDisplay;
    private MediaPlayer mediaPlayerBreatheIn;
    private MediaPlayer mediaPlayerBreatheOut;

    private CountDownTimer countDownTimer;
    private long remainingMillis;

    @Override
//    @overdrive:この注釈（アノテーション）は、このメソッドが親クラスのメソッドを上書きしていることを示します。親クラスとは、このクラスが継承しているクラスのことです。
//    この場合、MainActivityはAppCompatActivityクラスを継承しているので、AppCompatActivityが親クラスとなります。
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mediaPlayerBreatheIn = MediaPlayer.create(this, R.raw.up);
        mediaPlayerBreatheOut = MediaPlayer.create(this, R.raw.down);

        playButton = findViewById(R.id.play_button);
        pauseButton = findViewById(R.id.pause_button);
        stopButton = findViewById(R.id.stop_button);
        breatheInInput = findViewById(R.id.breathe_in_input);
        breatheOutInput = findViewById(R.id.breathe_out_input);
        cycleInput = findViewById(R.id.cycle_input);
        preparationInput = findViewById(R.id.preparation_input);
        totalTimeDisplay = findViewById(R.id.total_time_display);
        countdownDisplay = findViewById(R.id.countdown_display);
        // 以下の行で値を読み込む
        loadPreferences();
        long totalTimeMillis = calculateTotalTimeMillis();
        totalTimeDisplay.setText(formatTime(totalTimeMillis / 1000));
        // Playボタンの状態を設定
        updatePlayButtonState();

        // stopButtonのクリックリスナーでこのメソッドを呼び出します。
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
//            @Override:直後のonClickメソッドが、View.OnClickListenerインターフェースのメソッドを上書きしています。
//            したがって、このコンテキストにおける「親クラス」とは、クラス階層における直接の親ではなく、
//            実装されているインターフェース、すなわちView.OnClickListenerになります。
//            このインターフェースは、クリックされたときの動作を定義するために用いられます。
//            したがって、このインターフェースを実装するクラスは、そのクリック動作を具体的にどう処理するかをonClickメソッド内で定義する必要があります。
            public void onClick(View v) {
                resetToInitialState();
            }
//            この部分は、ボタンがクリックされたときに実行されるメソッドの定義です。クリックされたビュー自体は引数vに渡されます。
//            ボタンがクリックされたときにresetToInitialStateメソッドを呼び出します。
        });

        TextWatcher inputWatcher = new TextWatcher() {
//            TextWatcherは、テキストが変更されたときに反応するインターフェイスです。
            @Override
            public void afterTextChanged(Editable s) {
                updatePlayButtonState(); // Playボタンの状態を更新
                long totalTimeMillis = calculateTotalTimeMillis();
                totalTimeDisplay.setText(formatTime(totalTimeMillis / 1000));
//                計算された合計時間を秒単位に変換し、それをフォーマットして画面上の表示を更新しています。
                // ここで入力値を保存
                savePreferences();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
//            これらのメソッドが何も実行していないため、中身は空です。
//            このコードは、テキストが変更される前と変更されている最中に特別な処理を必要としていないことを示しています。
//            主な処理はafterTextChangedメソッドで実行されているため、これらのメソッドはプレースホルダーとして存在しています。
//                    (プレースホルダー（placeholder）
//            プレースホルダーとは、入力欄（テキストフィールド）に入力例などのテキストを薄いグレーで表示し、
//            入力方法をユーザーに提示することができる機能です。 入力を始めると、ユーザーの妨げにならないようプレースホルダーの表示は消えます。)
        };

        // それぞれの入力フィールドにウォッチャーを設定
        breatheInInput.addTextChangedListener(inputWatcher);
        breatheOutInput.addTextChangedListener(inputWatcher);
        cycleInput.addTextChangedListener(inputWatcher);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPlaying) {
                    startBreathingCycle();
                    isPlaying = true;
//                    isPlaying = true;は、再生中であることを示すフラグを真に設定します。
//                    これにより、アプリケーションは再生中であることを覚えておくことができ、他の部分のコードでこの情報を利用できます。
                } else if (isPaused) {
                    startCountdown(remainingMillis); // pauseからの再開
                    isPaused = false;
                }
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying && !isPaused) {
                    countDownTimer.cancel(); // カウントダウンを一時停止
//                    importされている(import android.media.MediaPlayer;import android.os.CountDownTimer;)を操作するコマンド
                    mediaPlayerBreatheIn.pause();
                    mediaPlayerBreatheOut.pause();
                    isPaused = true;
                }
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying) {
                    countDownTimer.cancel(); // カウントダウンを停止
                    mediaPlayerBreatheIn.stop();
                    mediaPlayerBreatheOut.stop();
                    mediaPlayerBreatheIn.prepareAsync(); // 再生の準備
                    mediaPlayerBreatheOut.prepareAsync();
                    isPlaying = false;
                    remainingMillis = 0; //残り時間を0にリセットします
                    countdownDisplay.setText("00min00sec"); // カウントダウンの表示を初めからの状態に戻します
                }
            }
        });

    }

    //データ保存
    private void savePreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("meditation", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
//        .getText(): この部分は、EditText から Editable オブジェクトを取得します。
//        Editable は、変更可能な文字列を表し、文字列の挿入、削除、置換などの操作をサポートしています。このオブジェクトは、ユーザーによって入力されたテキストを保持しています。
//      　.toString(): この部分は、Editable オブジェクトを通常のJavaのString オブジェクトに変換します。
//        この変換により、テキストの内容が文字列として取得できます。
        String breatheInStr = breatheInInput.getText().toString();
        String breatheOutStr = breatheOutInput.getText().toString();
        String cycleStr = cycleInput.getText().toString();
        String preparationStr = preparationInput.getText().toString();

//        Q.なぜわざわざ Float,int から String に変換するのか？
//           >EditTextから数値を取得するための一般的な手法です。EditTextはユーザーからの文字列入力を受け取るためのUIコンポーネントで、
//            数値を直接取得するメソッドは提供されていないため、このような変換が必要になります。

        if (!breatheInStr.isEmpty()) { //文字列 breatheInStr が空でない場合
            editor.putFloat("breathe_in", Float.parseFloat(breatheInStr));
            //breatheInStr を浮動小数点数に変換し、キー "breathe_in" で共有プリファレンスに保存します。
        }
        if (!breatheOutStr.isEmpty()) {
            editor.putFloat("breathe_out", Float.parseFloat(breatheOutStr));
        }
        if (!cycleStr.isEmpty()) {
            editor.putInt("cycle", Integer.parseInt(cycleStr));
            //cycleStr を整数に変換し、キー "cycle" で共有プリファレンスに保存します。
//            editorは、共有プリファレンスのエディタオブジェクトを指しています。
//              このエディタオブジェクトは、共有プリファレンスにデータを保存するために使用されます。
//            putIntは、エディタオブジェクトのメソッドで、整数値を共有プリファレンスに保存する役割を果たします。
//            putInt:第一引数には保存するキー（文字列）を指定し、第二引数には保存する整数値を指定します。
        }
        if (!preparationStr.isEmpty()) {
            editor.putFloat("preparation", Float.parseFloat(preparationStr));
        }

        editor.apply(); //これまでの変更を共有プリファレンスに反映します。
    }

    private long calculateTotalTimeMillis() {
        //この行は、合計の呼吸サイクル時間をミリ秒単位で計算するメソッドの宣言です。返り値の型はlongで、メソッド名はcalculateTotalTimeMillisです。
        try {
        //tryブロックの開始です。このブロック内で発生する特定の例外を捕捉するために使用されます。
            float breatheInSec = Float.parseFloat(breatheInInput.getText().toString());
//            吸う時間を取得しています。テキスト入力から文字列を取得し、Float.parseFloatメソッドで浮動小数点数に変換しています。
            float breatheOutSec = Float.parseFloat(breatheOutInput.getText().toString());
            int cycles = Integer.parseInt(cycleInput.getText().toString());
            return (long) ((breatheInSec + breatheOutSec) * cycles * 1000);
//            吸う時間と吐く時間の合計をサイクル数で掛け算し、1000をかけてミリ秒単位に変換します。その後、(long)で整数型にキャストして返り値としています。
        } catch (NumberFormatException e) {
            return 0; // エラーが発生した場合は0を返す.入力値が数値に変換できない場合のエラー処理です。
        }
    }

    //データ読み取り
    private void loadPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("meditation", MODE_PRIVATE);
        //SharedPreferencesオブジェクトを取得します。MODE_PRIVATEは、他のアプリからはアクセスできないプライベートモードを意味します。
        float breatheIn = sharedPreferences.getFloat("breathe_in", 0);
        // キーが"breathe_in"の値を取得します。この値が存在しない場合、デフォルト値として0が返されます。
        float breatheOut = sharedPreferences.getFloat("breathe_out", 0);
        int cycle = sharedPreferences.getInt("cycle", 0);
        float preparation = sharedPreferences.getFloat("preparation", 0);

//先ほどロードした値を各入力フィールドに設定しています。
        // breatheInの値を文字列に変換して、breatheInInputというEditTextにセットします。
        breatheInInput.setText(String.valueOf(breatheIn));
        breatheOutInput.setText(String.valueOf(breatheOut));
        cycleInput.setText(String.valueOf(cycle));
        preparationInput.setText(String.valueOf(preparation));
        // FloatやIntegerなどの数値型からString型に変換する理由は、入力フィールド（EditText）が文字列として値を取り扱うためです。
        // 画面上でユーザーに表示するには、数値を文字列形式に変換する必要があります。
    }
    private void updatePlayButtonState() {
        if (!breatheInInput.getText().toString().isEmpty() &&
                !breatheOutInput.getText().toString().isEmpty() &&
                !cycleInput.getText().toString().isEmpty()) {
//            各入力フィールドに値が入力されているかを確認します。
//            この確認は、入力フィールドのテキストを文字列に変換し、その文字列が空でないかどうかをチェックすることで行います
            playButton.setEnabled(true); // すべて入力されている場合にplayボタンを有効化
        } else {
            playButton.setEnabled(false);
        }
    }

    private void startBreathingCycle() {
        float preparationSec = Float.parseFloat(preparationInput.getText().toString()); //準備時間を取得
        final long totalTimeMillis = calculateTotalTimeMillis(); // calculateTotalTimeMillisメソッドを呼び出して、合計時間を計算

        //CountDownTimerクラスを使って新しいカウントダウンタイマーを作成しています。
        // このタイマーは、呼吸サイクルを開始する前の準備時間を管理するために使用されます。
        //最初の引数 preparationSec * 1000はタイマーの全体の時間で、準備時間をミリ秒に変換しています(long millisInFuture：カウントダウン開始時間)
        //2つ目の引数 1000はタイマーの間隔で、1秒ごとにタイマーが進むように設定しています(long countDownInterval：インターバル時間)
        new CountDownTimer((long) preparationSec * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // onTickメソッド: タイマーが1秒ごとに進むたびに呼び出されるメソッドです。ここで準備時間の表示を更新するコードを書くことができます。
                // onTick(long millisUntilFinished) が設定したインターバル時間で呼ばれる。millisUntilFinished　で残りの時間が分かる
            }
        //startCountdown(totalTimeMillis)が呼び出されており、準備時間が終了した後に本来の呼吸サイクルのカウントダウンを開始しています。
            @Override
            public void onFinish() {
                startCountdown(totalTimeMillis);
            }
        }.start(); //.start()は、タイマーを開始するためのメソッド
    }

    private void startCountdown(long totalTimeMillis) {
        cancelExistingTimer(); // 既存のタイマーが実行中であれば、そのタイマーをキャンセルします。
        initializeCountDownTimer(totalTimeMillis); //新しいカウントダウンタイマーを初期化します。
        countDownTimer.start();
    }

    private void resetToInitialState() { //アプリの状態を初期状態に戻すために使用されます。
        if (isPlaying) {
            countDownTimer.cancel();
            try {
                mediaPlayerBreatheIn.stop();
                mediaPlayerBreatheIn.prepare(); // ここを同期的に準備するように変更
                mediaPlayerBreatheOut.stop();
                mediaPlayerBreatheOut.prepare(); // ここも同期的に準備するように変更
            } catch (IOException e) {
                e.printStackTrace(); // エラーが発生した場合にログに出力
            }
            isPlaying = false;
            remainingMillis = 0;
            countdownDisplay.setText("00min00sec"); //カウントダウンの表示を初期状態に戻します。
        }
    }

    private String formatTime(long timeInSeconds) {
        int min = (int) (timeInSeconds % 3600) / 60; //timeInSeconds / 60; これでintなので余りを無視して商(min)を求められるはず
        int sec = (int) timeInSeconds % 60;
        return String.format("%02dmin%02dsec", min, sec);
        //分数と秒数を整形して文字列に変換します。
        // ここで、%02dは2桁の整数を表し、左側が0の場合は0で埋められます。5分7秒の場合、05min07secという形式の文字列が返されます。
    }
//countDownTimerオブジェクトがnullでないかを確認します。(nullは、プログラミング言語において、変数が何も参照していない状態を表す特別な値です。)
// nullであれば、その後の処理は実行されません。これは、countDownTimerが初期化されていない可能性があるため、エラーを防ぐためのチェックです。
    private void cancelExistingTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private void initializeCountDownTimer(long totalTimeMillis) { //このメソッドはカウントダウンタイマーを初期化する役割があります。
        countDownTimer = new CountDownTimer(totalTimeMillis, 1000) {
            public void onTick(long millisUntilFinished) { // 引数millisUntilFinishedは、カウントダウンが終了するまでの残り時間を表します
                remainingMillis = millisUntilFinished; // 残り時間をremainingMillis変数に保存しています。
                long breatheInSec = (long) Float.parseFloat(breatheInInput.getText().toString()) * 1000;
                long breatheOutSec = (long) Float.parseFloat(breatheOutInput.getText().toString()) * 1000;
                long cycleMillis = breatheInSec + breatheOutSec;
                manageBreathingCycle(totalTimeMillis, millisUntilFinished, breatheInSec, breatheOutSec, cycleMillis);
                countdownDisplay.setText(formatTime(millisUntilFinished / 1000));
                //カウントダウンの表示を更新しています。残り時間を秒単位に変換し、formatTimeメソッドでフォーマットして表示しています。
            }
            public void onFinish() {
                resetToInitialState();
            }
        };
    }

    private void manageBreathingCycle(long totalTimeMillis, long millisUntilFinished, long breatheInSec, long breatheOutSec, long cycleMillis) {
        long timeInCurrentCycle = (totalTimeMillis - millisUntilFinished) % cycleMillis;
        //現在のサイクル内での経過時間を計算しています。
        // 合計時間から残り時間を引いた後、サイクルの合計時間で割った余りを取得することで、現在のサイクルの進行状況を把握します。

        // 吸う・吐くサイクルの管理ロジック

        //現在のサイクル内での経過時間（timeInCurrentCycle）が吸う時間（breatheInSec）より小さいかどうかをチェックします。
        //以下、具体例
        //timeInCurrentCycleが3秒の場合、3秒は吸う時間の4秒より小さいです
        // だから、このときはまだ吸うフェーズです。吸う関連の処理（たとえば、吸う音を再生するなど）を行います。
        //timeInCurrentCycleが5秒の場合、5秒は吸う時間の4秒より大きいです
        // このときはもう吸うフェーズではないので、このif文の中の処理は実行されません。次のフェーズ、つまり吐くフェーズの処理に移ります。
        if (timeInCurrentCycle < breatheInSec) {
            if (mediaPlayerBreatheOut.isPlaying()) {
                mediaPlayerBreatheOut.pause();
                mediaPlayerBreatheOut.seekTo(0); // 吐く側のメディアプレーヤーの再生位置を先頭に戻します。
            }
            if (!mediaPlayerBreatheIn.isPlaying()) {
                mediaPlayerBreatheIn.start();
            }

//「吸う」フェーズの終了に向けて、フェードアウト処理を行うためのコードです。
            if (timeInCurrentCycle > breatheInSec - 5000) {
                // 現在のサイクル内での経過時間が、吸う時間から5秒（5000ミリ秒）減った値よりも大きい場合、
                float volume = (breatheInSec - timeInCurrentCycle) / 5000f;
                //フェードアウトするボリュームの値を計算します。
                //吸う時間から現在のサイクル内での経過時間を引いて、5000で割ります。これにより、最後の5秒間でボリュームが0になるように減衰させます。
                mediaPlayerBreatheIn.setVolume(volume, volume); //計算した値で、吸う側のメディアプレーヤーのボリュームを設定します
            } else {
                mediaPlayerBreatheIn.setVolume(1f, 1f); //吸う側のメディアプレーヤーのボリュームを最大（1.0）に設定
            }
//「吸う」フェーズ以外の場合、すなわち「吐く」フェーズの処理に進みます
        } else {
            if (mediaPlayerBreatheIn.isPlaying()) {
                mediaPlayerBreatheIn.pause();
                mediaPlayerBreatheIn.seekTo(0); // 位置を先頭に戻す
            }
            if (!mediaPlayerBreatheOut.isPlaying()) {
                mediaPlayerBreatheOut.start();
            }

            if (timeInCurrentCycle > breatheInSec + breatheOutSec - 5000) {
                // 現在のサイクル内での経過時間が、吸う時間と吐く時間の合計から5秒（5000ミリ秒）減った値よりも大きい場合、
                float volume = (breatheInSec + breatheOutSec - timeInCurrentCycle) / 5000f;
                mediaPlayerBreatheOut.setVolume(volume, volume);
            } else {
                mediaPlayerBreatheOut.setVolume(1f, 1f);
            }
        }
    }

}