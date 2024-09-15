package com.example.multi_thread_test_android;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    // (1) Set Number of task to run on each thread
    static final Integer NUM_OF_TASK = 5;
    Disposable disposable = null;
    ILogger logger = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView resultTv = findViewById(R.id.result);
        resultTv.setMovementMethod(new ScrollingMovementMethod());

        Button requestBt = findViewById(R.id.button);
        logger = new SimpleLogger(this, resultTv);

        requestBt.setOnClickListener(view -> {
            resultTv.setText("");

            Sleep.sleepFor(500);
            disposable = Observable.range(0, NUM_OF_TASK)
                    .flatMap(id ->
                            Observable.fromCallable(

                                    // (2) Below code is callable to run ToneConvert Task
                                    () -> new ToneConvert(id, logger).convert()

                                    )
                                    .subscribeOn(Schedulers.io()))
                    .subscribe(

                            // (3) Set result of task to TextView
                            result -> logger.log(result),
                            error -> logger.log(error.getMessage()),

                            // (4) Dispose observable to clear resource
                            () -> { if (disposable != null) disposable.dispose(); }
                    );
        });
    }
}

class ToneConvert {
    private final Integer id;
    private final ILogger logger;
    public ToneConvert(Integer id, ILogger logger) {
        this.id = id;
        this.logger = logger;
    }
    public String convert() {
        for (int i = 0; i < 5; ++i) {
            Sleep.sleepFor(1000);
            logger.log("Running... step " + i + " on " + Thread.currentThread().getName());
        }
        return "**Task id: " + id.toString() + " end on " + Thread.currentThread().getName();
    }
}

interface ILogger {
    void log(String str);
}

class SimpleLogger implements ILogger {
    private Activity activity;
    private TextView resultTv;

    SimpleLogger(Activity activity, TextView resultTv) {
        this.activity = activity;
        this.resultTv = resultTv;
    }

    @Override
    public void log(String str) {
        activity.runOnUiThread(() -> resultTv.append(str + '\n'));
    }
}

class Sleep {
    static void sleepFor(Integer milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }
}