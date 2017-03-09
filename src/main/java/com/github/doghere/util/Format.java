package com.github.doghere.util;

/**
 * Created by dog on 1/17/17.
 *
 * <pre>
 * examples:
 *
 * JSON:
 *
 * [
 *  {"field1":1,"field2":2},
 *  {"field1":2,"field2":4},
 *  {"filed1":3,"field2":6}
 * ]
 *
 * JSON_ARRAY_FIELD:
 *
 * [
 *  [1,2,3],
 *  [2,4,6]
 * ]
 *
 * CSV:
 *
 * field1,field2
 * 1,2
 * 2,4
 * 3,6
 *
 * CSV_WITHOUT_HEAD:
 *
 * 1,2
 * 2,4
 * 3,6
 * </pre>
 *
 *
 */
public enum Format {
    JSON,
    JSON_ARRAY_FIELD,
    CSV,
    CSV_WITHOUT_HEAD,
    DB
}
