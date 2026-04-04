package com.example.mid.helpers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.net.Uri;
import android.util.Log;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;

public class CallLogHelper {
    private static final int MAX_CALL_LOG_ENTRIES = 50;

    private static final String[] CALL_LOG_PROJECTION = {
            CallLog.Calls.NUMBER,
            CallLog.Calls.DURATION,
            CallLog.Calls.TYPE,
            CallLog.Calls.DATE
    };

    public static List<CallLogEntry> getCallLogs(Context context) {
        List<CallLogEntry> callLogs = new ArrayList<>();

        // ✅ Step 1: Check Permission Before Querying
        if (!hasCallLogPermission(context)) {
            return callLogs; // Return empty list if no permission
        }

        // ✅ Step 2: Query Call Logs with Exception Handling
        try (Cursor cursor = queryCallLogs(context)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    // Extract call log details
                    String number = cursor.getString(0);
                    String contactName = getContactName(context, number);
                    int duration = cursor.getInt(1);
                    int type = cursor.getInt(2);
                    long dateMillis = cursor.getLong(3);
                    String formattedDate = formatTimestamp(dateMillis);

                    // ✅ Create CallLogEntry Object and Add to List
                    callLogs.add(new CallLogEntry(contactName, number, duration, getCallTypeString(type), formattedDate));

                } while (cursor.moveToNext() && callLogs.size() < MAX_CALL_LOG_ENTRIES);
            }
        } catch (SecurityException e) {
            Log.e("CallLogHelper", "Permission denied reading call logs", e);
        } catch (Exception e) {
            Log.e("CallLogHelper", "Error reading call logs", e);
        }

        return callLogs;
    }

    // ✅ Step 3: Check Permission for Call Logs
    private static boolean hasCallLogPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG)
                == PackageManager.PERMISSION_GRANTED;
    }

    // ✅ Step 4: Query Call Logs Safely
    private static Cursor queryCallLogs(Context context) {
        return context.getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                CALL_LOG_PROJECTION,
                null,
                null,
                CallLog.Calls.DATE + " DESC" // Sort by most recent first
        );
    }

    // ✅ Step 5: Get Contact Name from Contacts List
    private static String getContactName(Context context, String phoneNumber) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = context.getContentResolver().query(
                uri,
                new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME},
                null, null, null
        );

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String name = cursor.getString(0);
                cursor.close();
                return name + " 🏷️"; // Show contact name with emoji
            }
            cursor.close();
        }

        return "Unknown Contact ❓"; // If not found, return default message
    }

    // ✅ Step 7: Convert Call Type to Readable String
    private static String getCallTypeString(int type) {
        switch (type) {
            case CallLog.Calls.INCOMING_TYPE: return "📥 Incoming";
            case CallLog.Calls.OUTGOING_TYPE: return "📤 Outgoing";
            case CallLog.Calls.MISSED_TYPE: return "❗ Missed";
            case CallLog.Calls.REJECTED_TYPE: return "🚫 Rejected";
            case CallLog.Calls.BLOCKED_TYPE: return "🔒 Blocked";
            default: return "❓ Unknown";
        }
    }

    // ✅ Step 8: Format Timestamp
    private static String formatTimestamp(long timestamp) {
        return android.text.format.DateFormat.format("📅 dd-MM-yyyy 🕒 HH:mm", timestamp).toString();
    }
}
