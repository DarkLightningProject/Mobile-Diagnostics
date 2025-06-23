package com.example.mid.helpers;

public class CallLogEntry {
    private String contactName;
    private String phoneNumber;
    private String callType;
    private int callDuration;  // Correct field name
    private String callDate;   // Correct field name
    private boolean isExpanded;

    // ✅ Fix the constructor parameter order
    public CallLogEntry(String contactName, String phoneNumber, int callDuration, String callType, String callDate) {
        this.contactName = contactName;
        this.phoneNumber = phoneNumber;
        this.callDuration = callDuration;
        this.callType = callType;
        this.callDate = callDate;
        this.isExpanded = false;
    }

    // ✅ Correct Getter Methods
    public String getContactName() { return contactName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getCallType() { return callType; }
    public int getCallDuration() { return callDuration; }
    public String getCallDate() { return callDate; }
    public boolean isExpanded() { return isExpanded; }
    public void setExpanded(boolean expanded) { isExpanded = expanded; }
}
