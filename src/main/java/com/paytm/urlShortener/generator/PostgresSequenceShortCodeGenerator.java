package com.paytm.urlShortener.generator;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class PostgresSequenceShortCodeGenerator implements ShortCodeGenerator {

    private static final String NEXT_SEQUENCE_VALUE_SQL = "SELECT nextval('short_code_sequence')";

    private final JdbcTemplate jdbcTemplate;

    public PostgresSequenceShortCodeGenerator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public String generate() {
        Long sequenceValue = jdbcTemplate.queryForObject(NEXT_SEQUENCE_VALUE_SQL, Long.class);

        if (sequenceValue == null) {
            throw new IllegalStateException("PostgreSQL did not return a short-code sequence value");
        }

        return Base62Encoder.encode(sequenceValue);
    }
}