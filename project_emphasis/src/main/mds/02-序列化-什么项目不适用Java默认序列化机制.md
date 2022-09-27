# 无法跨平台

Java序列化后的二进制流，只能被Java反序列化为对象。在这个分布式跨语言的开发模式下，如果使用Java序列化机制，就必须使上下游交互系统都要基于Java开发，不太现实

# 效率差

```java
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
```



使用Java默认反序列化操作1000000次，耗时2136 ms，长度144
使用Java默认反序列化操作1000000次，耗时499 ms，长度78

Process finished with exit code 0

通过demo可以看到，在同一份对象的前提下，Java的序列化反序列化，比Jackson的序列化和反序列化要慢、并且数据量也大。

# 其他的序列化机制

除了Json外，还有Dubbo的hessian、Google的protobuf、以及thirft。