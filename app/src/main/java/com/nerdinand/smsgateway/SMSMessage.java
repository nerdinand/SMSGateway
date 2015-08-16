package com.nerdinand.smsgateway;

import android.util.JsonWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class SMSMessage {
    private final String recipient;
    private final String text;

    public SMSMessage(String recipient, String text) {
        this.recipient = recipient;
        this.text = text;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getText() {
        return text;
    }

    public String toJSON() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(byteArrayOutputStream, "UTF-8"));

        writer.beginObject();
        writer.name("sender").value(recipient);
        writer.name("text").value(text);
        writer.endObject();
        writer.close();

        return byteArrayOutputStream.toString();
    }

    @Override
    public String toString() {
        return "SMSMessage{" +
                "recipient='" + recipient + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}
