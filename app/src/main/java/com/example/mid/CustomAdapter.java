package com.example.mid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.mid.helpers.CallLogEntry;

import java.util.List;

public class CustomAdapter extends BaseAdapter {
    private final Context context;
    private final List<CallLogEntry> callLogs;

    public CustomAdapter(Context context, List<CallLogEntry> callLogs) {
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

        TextView contactName = convertView.findViewById(R.id.contactName);
        TextView phoneNumber = convertView.findViewById(R.id.phoneNumber);
        TextView callType = convertView.findViewById(R.id.callType);
        TextView callDuration = convertView.findViewById(R.id.callDuration);
        TextView callDate = convertView.findViewById(R.id.callDate);
        LinearLayout detailsLayout = convertView.findViewById(R.id.detailsLayout);

        CallLogEntry entry = callLogs.get(position);

        // Handle cases where contact name might be null
        String name = entry.getContactName();
        contactName.setText((name != null && !name.isEmpty()) ? name : entry.getPhoneNumber());

        // Set details
        phoneNumber.setText("📞 " + entry.getPhoneNumber());
        callType.setText(getCallTypeEmoji(entry.getCallType()));  // Fix for call type handling
        callDuration.setText("⏳ Duration: " + entry.getCallDuration() + " sec");
        callDate.setText("📅 " + entry.getCallDate());

        // Toggle details visibility
        contactName.setOnClickListener(v -> {
            if (detailsLayout.getVisibility() == View.GONE) {
                detailsLayout.setVisibility(View.VISIBLE);
            } else {
                detailsLayout.setVisibility(View.GONE);
            }
        });

        return convertView;
    }

    // ✅ Fix: Handle String-based call types properly
    private String getCallTypeEmoji(String type) {
        switch (type.toLowerCase()) {
            case "incoming": return "📥 Incoming";
            case "outgoing": return "📤 Outgoing";
            case "missed": return "❌ Missed";
            case "blocked": return "🚫 Blocked";
            default: return "❓ Unknown";
        }
    }
}
