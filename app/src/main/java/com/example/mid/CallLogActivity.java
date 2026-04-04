package com.example.mid;

import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mid.helpers.CallLogAdapter;
import com.example.mid.helpers.CallLogEntry;
import com.example.mid.helpers.CallLogHelper;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CallLogActivity extends AppCompatActivity {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_log);

        ListView listView = findViewById(R.id.callLogListView);

        executorService.execute(() -> {
            List<CallLogEntry> logs = CallLogHelper.getCallLogs(this);
            runOnUiThread(() -> {
                CallLogAdapter adapter = new CallLogAdapter(this, logs);
                listView.setAdapter(adapter);
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
