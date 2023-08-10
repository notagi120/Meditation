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

        // stopButtonのクリックリスナーでこのメソッドを呼び出します。
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetToInitialState();
            }
        });

        playButton.setEnabled(false); // 初期状態ではplayボタンを無効化

        TextWatcher inputWatcher = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (!breatheInInput.getText().toString().isEmpty() &&
                        !breatheOutInput.getText().toString().isEmpty() &&
                        !cycleInput.getText().toString().isEmpty()) {
                    playButton.setEnabled(true); // すべて入力されている場合にplayボタンを有効化
                    long totalTimeMillis = calculateTotalTimeMillis();
                    totalTimeDisplay.setText(formatTime(totalTimeMillis / 1000));
                } else {
                    playButton.setEnabled(false);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
        };

        // 入力フィールドにウォッチャーを設定
        breatheInInput.addTextChangedListener(inputWatcher);
        breatheOutInput.addTextChangedListener(inputWatcher);
        cycleInput.addTextChangedListener(inputWatcher);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPlaying) {
                    startBreathingCycle();
                    isPlaying = true;
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
                    mediaPlayerBreatheIn.pause();
                    mediaPlayerBreatheOut.pause();
                    isPaused = true;
//                } else if (isPlaying && isPaused) {
//                    startCountdown(remainingMillis); // カウントダウンを再開
//                    isPaused = false;
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
                    remainingMillis = 0;
                    countdownDisplay.setText("00min00sec"); // 初めからの状態に戻す
                }
            }
        });

    }

    //データ保存
    private void savePreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("meditation", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("breathe_in", Float.parseFloat(breatheInInput.getText().toString()));
        editor.putFloat("breathe_out", Float.parseFloat(breatheOutInput.getText().toString()));
        editor.putInt("cycle", Integer.parseInt(cycleInput.getText().toString()));
        editor.putFloat("preparation", Float.parseFloat(preparationInput.getText().toString()));
        editor.apply();
    }
    //データ読み取り
    private void loadPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("meditation", MODE_PRIVATE);
        float breatheIn = sharedPreferences.getFloat("breathe_in", 0); // 0はデフォルト値
        float breatheOut = sharedPreferences.getFloat("breathe_out", 0);
        int cycle = sharedPreferences.getInt("cycle", 0);
        float preparation = sharedPreferences.getFloat("preparation", 0);

        breatheInInput.setText(String.valueOf(breatheIn));
        breatheOutInput.setText(String.valueOf(breatheOut));
        cycleInput.setText(String.valueOf(cycle));
        preparationInput.setText(String.valueOf(preparation));
    }

    private long calculateTotalTimeMillis() {
        float breatheInSec = Float.parseFloat(breatheInInput.getText().toString());
        float breatheOutSec = Float.parseFloat(breatheOutInput.getText().toString());
        int cycles = Integer.parseInt(cycleInput.getText().toString());
        return (long) ((breatheInSec + breatheOutSec) * cycles * 1000);
    }

    private void startBreathingCycle() {
        float preparationSec = Float.parseFloat(preparationInput.getText().toString());
        final long totalTimeMillis = calculateTotalTimeMillis();

        // 準備時間を待ってからカウントダウンを開始
        new CountDownTimer((long) preparationSec * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // ここで準備時間の表示を更新できます
            }

            @Override
            public void onFinish() {
                startCountdown(totalTimeMillis);
            }
        }.start();
    }

    private void startCountdown(long totalTimeMillis) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(totalTimeMillis, 1000) {
            public void onTick(long millisUntilFinished) {
                remainingMillis = millisUntilFinished;
                long breatheInSec = (long) Float.parseFloat(breatheInInput.getText().toString()) * 1000;
                long breatheOutSec = (long) Float.parseFloat(breatheOutInput.getText().toString()) * 1000;
                long cycleMillis = breatheInSec + breatheOutSec;

                long timeInCurrentCycle = (totalTimeMillis - millisUntilFinished) % cycleMillis;

                if (timeInCurrentCycle < breatheInSec) {
                    if (mediaPlayerBreatheOut.isPlaying()) {
                        mediaPlayerBreatheOut.pause();
                        mediaPlayerBreatheOut.seekTo(0); // 位置を先頭に戻す
                    }
                    if (!mediaPlayerBreatheIn.isPlaying()) {
                        mediaPlayerBreatheIn.start();
                    }

                    if (timeInCurrentCycle > breatheInSec - 5000) {
                        // フェードアウトの開始
                        float volume = (breatheInSec - timeInCurrentCycle) / 5000f;
                        mediaPlayerBreatheIn.setVolume(volume, volume);
                    } else {
                        mediaPlayerBreatheIn.setVolume(1f, 1f);
                    }
                } else {
                    if (mediaPlayerBreatheIn.isPlaying()) {
                        mediaPlayerBreatheIn.pause();
                        mediaPlayerBreatheIn.seekTo(0); // 位置を先頭に戻す
                    }
                    if (!mediaPlayerBreatheOut.isPlaying()) {
                        mediaPlayerBreatheOut.start();
                    }

                    if (timeInCurrentCycle > breatheInSec + breatheOutSec - 5000) {
                        // フェードアウトの開始
                        float volume = (breatheInSec + breatheOutSec - timeInCurrentCycle) / 5000f;
                        mediaPlayerBreatheOut.setVolume(volume, volume);
                    } else {
                        mediaPlayerBreatheOut.setVolume(1f, 1f);
                    }
                }

                countdownDisplay.setText(formatTime(millisUntilFinished / 1000));
            }

            public void onFinish() {
                try {
                    mediaPlayerBreatheIn.stop();
                    mediaPlayerBreatheIn.prepare(); // ここを同期的に準備するように変更
                    mediaPlayerBreatheOut.stop();
                    mediaPlayerBreatheOut.prepare(); // ここも同期的に準備するように変更
                } catch (IOException e) {
                    e.printStackTrace(); // エラーが発生した場合にログに出力
                }
                resetToInitialState();
            }
        }.start();
    }
    private void resetToInitialState() {
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
            countdownDisplay.setText("00min00sec");
        }
    }

    private String formatTime(long timeInSeconds) {
        int min = (int) (timeInSeconds % 3600) / 60;
        int sec = (int) timeInSeconds % 60;
        return String.format("%02dmin%02dsec", min, sec);
    }
}