package com.genn;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;

public class zeroTow {
    public static void main(String[] args) throws IOException {
        String payload = "你好呀黄俊严，今天的存量数据已经到了";
        Message message = new Message(payload, payload.length());

        // 默认序列化
        Instant defaultStart = Instant.now();
        int defaultLength = 0;
        for (int i = 0; i < 1000000; i++) {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream oo = new ObjectOutputStream(bo);
            oo.writeObject(message);
            oo.flush();
            oo.close();
            byte[] bytes = bo.toByteArray();
            defaultLength = bytes.length;
            oo.close();
        }
        Instant defaultEnd = Instant.now();
        long defaultGap = Duration.between(defaultStart, defaultEnd).toMillis();
        System.out.println(String.format("使用Java默认反序列化操作1000000次，耗时%s ms，长度%s", defaultGap, defaultLength));

        // jackson序列化
        ObjectMapper objectMapper = new ObjectMapper();
        Instant jacksonStart = Instant.now();
        int jacksonLength = 0;
        for (int i = 0; i < 1000000; i++) {
            String json = objectMapper.writeValueAsString(message);
            jacksonLength = json.getBytes().length;
        }
        Instant jacksonEnd = Instant.now();
        long jacksonGap = Duration.between(jacksonStart, jacksonEnd).toMillis();
        System.out.println(String.format("使用Java默认反序列化操作1000000次，耗时%s ms，长度%s", jacksonGap, jacksonLength));

    }


    public static class Message implements Serializable {
        String value;
        int length;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public Message(String value, int length) {
            this.value = value;
            this.length = length;
        }

    }
}
