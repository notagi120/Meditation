package com.example.meditation;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private boolean isPlaying = false;
    private boolean isPaused = false;

    private Button playButton, pauseButton, stopButton;
    private EditText breatheInInput, breatheOutInput, cycleInput, preparationInput;
    private TextView totalTimeDisplay, countdownDisplay;

    private CountDownTimer countDownTimer;
    private long remainingMillis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playButton = findViewById(R.id.play_button);
        pauseButton = findViewById(R.id.pause_button);
        stopButton = findViewById(R.id.stop_button);
        breatheInInput = findViewById(R.id.breathe_in_input);
        breatheOutInput = findViewById(R.id.breathe_out_input);
        cycleInput = findViewById(R.id.cycle_input);
        preparationInput = findViewById(R.id.preparation_input);
        totalTimeDisplay = findViewById(R.id.total_time_display);
        countdownDisplay = findViewById(R.id.countdown_display);

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
                    isPaused = true;
                } else if (isPlaying && isPaused) {
                    startCountdown(remainingMillis); // カウントダウンを再開
                    isPaused = false;
                }
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying) {
                    countDownTimer.cancel(); // カウントダウンを停止
                    isPlaying = false;
                    remainingMillis = 0;
                    countdownDisplay.setText("00min00sec"); // 初めからの状態に戻す
                }
            }
        });
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
                countdownDisplay.setText(formatTime(millisUntilFinished / 1000));
            }

            public void onFinish() {
                // カウントダウン終了時の処理
            }
        }.start();
    }

    private String formatTime(long timeInSeconds) {
        int min = (int) (timeInSeconds % 3600) / 60;
        int sec = (int) timeInSeconds % 60;
        return String.format("%02dmin%02dsec", min, sec);
    }
}