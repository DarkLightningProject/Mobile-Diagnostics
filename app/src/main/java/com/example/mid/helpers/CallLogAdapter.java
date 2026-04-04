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
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.call_log_item, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        CallLogEntry entry = callLogs.get(position);

        String contactName = entry.getContactName();
        holder.nameText.setText((contactName != null && !contactName.isEmpty()) ? contactName : entry.getPhoneNumber());
        holder.numberText.setText("📲 " + entry.getPhoneNumber());
        holder.typeText.setText(entry.getCallType());
        holder.durationText.setText("⏳ " + entry.getCallDuration() + " sec");
        holder.dateText.setText("🗓️ " + entry.getCallDate());
        holder.detailsLayout.setVisibility(entry.isExpanded() ? View.VISIBLE : View.GONE);

        convertView.setOnClickListener(v -> {
            entry.setExpanded(!entry.isExpanded());
            holder.detailsLayout.setVisibility(entry.isExpanded() ? View.VISIBLE : View.GONE);
        });

        return convertView;
    }

    private static class ViewHolder {
        final TextView nameText, numberText, typeText, durationText, dateText;
        final LinearLayout detailsLayout;

        ViewHolder(View view) {
            nameText = view.findViewById(R.id.contactName);
            detailsLayout = view.findViewById(R.id.detailsLayout);
            numberText = view.findViewById(R.id.phoneNumber);
            typeText = view.findViewById(R.id.callType);
            durationText = view.findViewById(R.id.callDuration);
            dateText = view.findViewById(R.id.callDate);
        }
    }
}
