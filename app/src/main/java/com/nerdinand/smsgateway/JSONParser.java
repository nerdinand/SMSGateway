package com.nerdinand.smsgateway;

import android.util.JsonReader;

import java.io.IOException;
import java.io.StringReader;

public class JSONParser {
    private final String jsonString;

    public JSONParser(String jsonString) {
        this.jsonString = jsonString;
    }

    public SMSMessage parse() throws IOException {
        JsonReader jsonReader = new JsonReader(new StringReader(jsonString));

        String recipient = null;
        String message = null;

        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String attributeName = jsonReader.nextName();
            if (attributeName.equals("sender_recipient")) {
                recipient = jsonReader.nextString();
            } else if (attributeName.equals("text")) {
                message = jsonReader.nextString();
            } else {
                jsonReader.skipValue();
            }
        }
        jsonReader.endObject();

        return new SMSMessage(recipient, message);
    }
}
