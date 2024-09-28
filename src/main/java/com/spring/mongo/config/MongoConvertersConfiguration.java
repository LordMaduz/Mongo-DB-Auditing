package com.spring.mongo.config;

import java.util.ArrayList;
import java.util.List;

import org.bson.BsonString;
import org.bson.BsonValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

@Configuration
public class MongoConvertersConfiguration {
    @Bean
    MongoCustomConversions mongoCustomConversions() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(new StringBsonValueConverter());
        converters.add(new BsonValueStringConverter());
        return new MongoCustomConversions(converters);
    }


    @WritingConverter
    private static class StringBsonValueConverter implements Converter<String, BsonValue> {

        @Override
        public BsonValue convert(String source) {
            return new BsonString(source);
        }
    }

    @ReadingConverter
    private static class BsonValueStringConverter implements Converter<BsonValue, String> {

        @Override
        public String convert(BsonValue source) {
            return source.asString().toString();
        }
    }
}
