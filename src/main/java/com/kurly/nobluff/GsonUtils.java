package com.kurly.nobluff;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GsonUtils {
    private static String PATTERN_DATE = "yyyy-MM-dd";
    private static String PATTERN_TIME = "HH:mm:ss";
    private static String PATTERN_DATETIME = String.format("%s %s", PATTERN_DATE, PATTERN_TIME);

    private static Gson gson = new Gson().newBuilder()
                                         .setDateFormat(PATTERN_DATETIME)
                                         .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter().nullSafe())
                                         .registerTypeAdapter(LocalDate.class, new LocalDateAdapter().nullSafe())
                                         .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter().nullSafe())
                                         .create();

    public static String toJson(Object o) {
        String result = gson.toJson(o);
        if("null".equals(result))
            return null;
        return result;
    }

    public static <T> T fromJson(String s, Class<T> clazz) {
        try {
            return gson.fromJson(s, clazz);
        } catch(JsonSyntaxException e) {
            log.error(e.getMessage());
        }
        return null;
    }
    static class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {

        @Override
        public void write(JsonWriter out, LocalDateTime value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }
            out.beginObject();
            out.name("date");
            out.beginObject();
            out.name("year").value(value.getYear());
            out.name("month").value(value.getMonthValue());
            out.name("day").value(value.getDayOfMonth());
            out.endObject();
            out.name("time");
            out.beginObject();
            out.name("hour").value(value.getHour());
            out.name("minute").value(value.getMinute());
            out.name("second").value(value.getSecond());
            out.name("nano").value(value.getNano());
            out.endObject();
            out.endObject();
        }

        @Override
        public LocalDateTime read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }

            int year = 0, month = 0, day = 0, hour = 0, minute = 0, second = 0, nano = 0;

            in.beginObject();
            while (in.hasNext()) {
                String name = in.nextName();
                switch (name) {
                    case "date":
                        in.beginObject();
                        while (in.hasNext()) {
                            String dateName = in.nextName();
                            switch (dateName) {
                                case "year":
                                    year = in.nextInt();
                                    break;
                                case "month":
                                    month = in.nextInt();
                                    break;
                                case "day":
                                    day = in.nextInt();
                                    break;
                                default:
                                    in.skipValue(); // 필요 없는 속성인 경우 스킵
                                    break;
                            }
                        }
                        in.endObject();
                        break;
                    case "time":
                        in.beginObject();
                        while (in.hasNext()) {
                            String timeName = in.nextName();
                            switch (timeName) {
                                case "hour":
                                    hour = in.nextInt();
                                    break;
                                case "minute":
                                    minute = in.nextInt();
                                    break;
                                case "second":
                                    second = in.nextInt();
                                    break;
                                case "nano":
                                    nano = in.nextInt();
                                    break;
                                default:
                                    in.skipValue(); // 필요 없는 속성인 경우 스킵
                                    break;
                            }
                        }
                        in.endObject();
                        break;
                    default:
                        in.skipValue(); // 필요 없는 속성인 경우 스킵
                        break;
                }
            }
            in.endObject();
            return LocalDateTime.of(year, month, day, hour, minute, second, nano);
        }
    }

    static class LocalDateAdapter extends TypeAdapter<LocalDate> {
        DateTimeFormatter format = DateTimeFormatter.ofPattern(PATTERN_DATE);

        @Override
        public void write(JsonWriter out, LocalDate value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }
            out.beginObject();
            out.name("year").value(value.getYear());
            out.name("month").value(value.getMonthValue());
            out.name("day").value(value.getDayOfMonth());
            out.endObject();
        }

        @Override
        public LocalDate read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            in.beginObject();
            int year = 0, month = 0, day = 0;
            while (in.hasNext()) {
                String name = in.nextName();
                switch (name) {
                    case "year":
                        year = in.nextInt();
                        break;
                    case "month":
                        month = in.nextInt();
                        break;
                    case "day":
                        day = in.nextInt();
                        break;
                    default:
                        break;
                }
            }
            in.endObject();
            return LocalDate.of(year, month, day);
        }
    }

    static class LocalTimeAdapter extends TypeAdapter<LocalTime> {
        @Override
        public void write(JsonWriter out, LocalTime value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }
            out.beginObject();
            out.name("hour").value(value.getHour());
            out.name("minute").value(value.getMinute());
            out.name("second").value(value.getSecond());
            out.endObject();
        }

        @Override
        public LocalTime read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            in.beginObject();
            int hour = 0, minute = 0, second = 0;
            while (in.hasNext()) {
                String name = in.nextName();
                switch (name) {
                    case "hour":
                        hour = in.nextInt();
                        break;
                    case "minute":
                        minute = in.nextInt();
                        break;
                    case "second":
                        second = in.nextInt();
                        break;
                    default:
                        break;
                }
            }
            in.endObject();
            return LocalTime.of(hour, minute, second);
        }
    }
}