package com.example.mid.helpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.BaseAdapter;

import com.example.mid.R;

import java.util.List;

public class CallLogAdapter extends BaseAdapter {
    private final Context context;
    private final List<CallLogEntry> callLogs;

    public CallLogAdapter(Context context, List<CallLogEntry> callLogs) {
        this.context = context;
        this.callLogs = callLogs;
    }

    @Override
    public int getCount() {
        return callLogs.size();
    }

    @Override
    public Object getItem(int position) {
        return callLogs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.call_log_item, parent, false);
        }

        TextView nameText = convertView.findViewById(R.id.contactName);
        LinearLayout detailsLayout = convertView.findViewById(R.id.detailsLayout);
        TextView numberText = convertView.findViewById(R.id.phoneNumber);
        TextView typeText = convertView.findViewById(R.id.callType);
        TextView durationText = convertView.findViewById(R.id.callDuration);
        TextView dateText = convertView.findViewById(R.id.callDate);

        CallLogEntry entry = callLogs.get(position);

        // Ensure contact name is not null
        String contactName = entry.getContactName();
        nameText.setText((contactName != null && !contactName.isEmpty()) ? contactName : entry.getPhoneNumber());

        // Set details
        numberText.setText("📲 " + entry.getPhoneNumber());
        typeText.setText(entry.getCallType());
        durationText.setText("⏳ " + entry.getCallDuration() + " sec");

        dateText.setText("🗓️ " + entry.getCallDate());

        // Toggle details when clicked
        convertView.setOnClickListener(v -> {
            entry.setExpanded(!entry.isExpanded());
            detailsLayout.setVisibility(entry.isExpanded() ? View.VISIBLE : View.GONE);
        });

        // Ensure correct visibility state
        detailsLayout.setVisibility(entry.isExpanded() ? View.VISIBLE : View.GONE);

        return convertView;
    }
}
