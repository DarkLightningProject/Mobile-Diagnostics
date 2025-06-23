package com.example.mid;

import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mid.helpers.CallLogAdapter;
import com.example.mid.helpers.CallLogEntry;
import com.example.mid.helpers.CallLogHelper;

import java.util.List;

public class CallLogActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_log);

        ListView listView = findViewById(R.id.callLogListView);

        List<CallLogEntry> logs = CallLogHelper.getCallLogs(this);
        CallLogAdapter adapter = new CallLogAdapter(this, logs);
        listView.setAdapter(adapter);
    }
}
