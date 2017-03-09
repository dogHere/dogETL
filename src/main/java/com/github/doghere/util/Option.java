package com.github.doghere.util;

/**
 * Created by dog on 12/20/16.
 * <p>
 * Filter option.
 *
 * @author dogHere@tutamail.com
 */
public class Option<S> {
    private S fieldName;
    private Object value;
    private Sign sign;

    /**
     * Option map <br><br>
     * == <br>
     * !=<br>
     * ><br>
     * <<br>
     * !><br>
     * !<<br>
     *
     * @param fieldName
     * @param value
     */
    public Option(S fieldName, Object value) {
        this(fieldName, Sign.EQ, value);

    }

    /**
     * Option map <br><br>
     * Sign:<br>
     * == <br>
     * !=<br>
     * ><br>
     * <<br>
     * !><br>
     * !<<br>
     *
     * @param fieldName field name
     * @param sign      Sign
     * @param value     value
     */
    public Option(S fieldName, Sign sign, Object value) {
        this.fieldName = fieldName;
        this.value = value;
        this.sign = sign;
    }

    /**
     * To get Field name
     *
     * @return Field name
     */
    public S getFieldName() {
        return this.fieldName;
    }

    /**
     * To get value
     *
     * @return value
     */
    public Object getValue() {
        return this.value;
    }

    /**
     * To get Sign
     *
     * @return sign
     */
    public Sign getSign() {
        return this.sign;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Option<?> option = (Option<?>) o;

        if (!fieldName.equals(option.fieldName)) return false;
        if (!value.equals(option.value)) return false;
        return sign == option.sign;

    }

    @Override
    public int hashCode() {
        int result = fieldName.hashCode();
        result = 31 * result + value.hashCode();
        result = 31 * result + sign.hashCode();
        return result;
    }

    /**
     * To get new instance of option.
     * <br><br>
     * <p>
     * Default sign is EQ
     *
     * @param fieldName field name
     * @param value     value
     * @return Option
     */
    public static Option<String> make(String fieldName, Object value) {
        return make(fieldName, Sign.EQ, value);
    }

    /**
     * To get new instance of option.
     *
     * @param fieldName field name
     * @param sign      sign
     * @param value     value
     * @return Option
     */
    public static Option<String> make(String fieldName, Sign sign, Object value) {
        return new Option<String>(fieldName, sign, value);
    }
}
