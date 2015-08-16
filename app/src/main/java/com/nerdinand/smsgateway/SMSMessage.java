package com.nerdinand.smsgateway;

import android.util.JsonWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class SMSMessage {
    private final String senderRecipient;
    private final String text;

    public SMSMessage(String senderRecipient, String text) {
        this.senderRecipient = senderRecipient;
        this.text = text;
    }

    public String getSenderRecipient() {
        return senderRecipient;
    }

    public String getText() {
        return text;
    }

    public String toJSON() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(byteArrayOutputStream, "UTF-8"));

        writer.beginObject();
        writer.name("sender_recipient").value(senderRecipient);
        writer.name("text").value(text);
        writer.endObject();
        writer.close();

        return byteArrayOutputStream.toString();
    }

    @Override
    public String toString() {
        return "SMSMessage{" +
                "sender_recipient='" + senderRecipient + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}
