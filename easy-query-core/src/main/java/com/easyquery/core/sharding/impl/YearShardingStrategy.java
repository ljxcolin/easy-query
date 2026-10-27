package com.easyquery.core.sharding.impl;

import com.easyquery.core.enums.StrategyType;
import com.easyquery.core.model.ShardingRuleConfig;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 按年分片策略。
 *
 * <p>物理表名形如 {@code t_order_2023}、{@code t_order_2024}，后缀即 4 位年份。
 * 无需额外配置（{@code padZero} 是给月/天策略用的，本策略固定输出 4 位年份）。</p>
 *
 * <p>支持的分片键值形式：</p>
 * <ul>
 *     <li>日期时间类型：{@link java.time.LocalDate}、{@link java.time.LocalDateTime}、{@link java.util.Date}</li>
 *     <li>日期文本：{@code '2024-03-15'}、{@code '2024-03-15 10:30:00'}、{@code '2024/03/15'}、{@code '2024'}</li>
 *     <li>数字：{@code 2024}、{@code 202403}、{@code 20240315}、{@code 20240315103000}、13 位毫秒时间戳</li>
 * </ul>
 *
 * <p>支持的条件与 {@link RangeShardingStrategy} 一致：{@code =}、{@code IN}、比较运算、{@code BETWEEN}。</p>
 */
public class YearShardingStrategy extends AbstractNumericSuffixShardingStrategy {

    /**
     * 允许的年份下界，用于过滤明显的非法值（如把 1700000000 当成 yyyy 前缀）
     */
    private static final int MIN_YEAR = 1900;

    /**
     * 允许的年份上界
     */
    private static final int MAX_YEAR = 2999;

    /**
     * 日期文本开头的 4 位年份
     */
    private static final Pattern LEADING_YEAR = Pattern.compile("^(\\d{4})");

    @Override
    public StrategyType getType() {
        return StrategyType.YEAR;
    }

    @Override
    protected String suffixPattern() {
        return "\\d{4}";
    }

    @Override
    protected Integer toShardNumber(ShardingRuleConfig shardingRuleConfig, Object value) {
        return extractYear(value);
    }

    /**
     * 从分片键值中提取年份，无法识别时返回 null。
     */
    private Integer extractYear(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof TemporalAccessor) {
            TemporalAccessor temporal = (TemporalAccessor) value;
            return temporal.isSupported(ChronoField.YEAR) ? validYear(temporal.get(ChronoField.YEAR)) : null;
        }

        if (value instanceof java.util.Date) {
            Instant instant = Instant.ofEpochMilli(((java.util.Date) value).getTime());
            return validYear(instant.atZone(ZoneId.systemDefault()).getYear());
        }

        if (value instanceof Number) {
            return fromDigits(Long.toString(((Number) value).longValue()));
        }

        return fromText(value.toString());
    }

    private Integer fromText(String text) {
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.matches("\\d+")) {
            return fromDigits(trimmed);
        }
        Matcher matcher = LEADING_YEAR.matcher(trimmed);
        return matcher.find() ? validYear(Integer.parseInt(matcher.group(1))) : null;
    }

    /**
     * 纯数字文本 → 年份。按长度区分 yyyy / yyyyMM / yyyyMMdd / yyyyMMddHHmmss / 毫秒时间戳。
     */
    private Integer fromDigits(String digits) {
        if (digits.isEmpty()) {
            return null;
        }
        switch (digits.length()) {
            case 4:
                return validYear(Integer.parseInt(digits));
            case 6:
            case 8:
            case 12:
            case 14:
                return validYear(Integer.parseInt(digits.substring(0, 4)));
            case 13:
                return fromEpochMillis(digits);
            default:
                return null;
        }
    }

    private Integer fromEpochMillis(String digits) {
        try {
            return fromInstant(Instant.ofEpochMilli(Long.parseLong(digits)));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer fromInstant(Instant instant) {
        try {
            return validYear(instant.atZone(ZoneId.systemDefault()).getYear());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private Integer validYear(int year) {
        return year >= MIN_YEAR && year <= MAX_YEAR ? year : null;
    }
}
