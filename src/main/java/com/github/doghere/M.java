package com.github.doghere;


import com.github.doghere.util.Format;
import com.github.doghere.util.Log;
import com.github.doghere.util.Status;
import com.github.doghere.util.Type;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by dog on 12/19/16.
 * <p>
 * A model read and write table
 *
 * @author dogHere@tutamail.com
 */
public class M {

    private ResultSet rs;
    private Statement statement;
    private Connection conn;

    private Status status = Status.NOT_READY;

    /**
     * to count read times
     */
    private int readTimes = 0;
    private volatile boolean keepReading = true;


    public int getReadTimes() {
        return readTimes;
    }

    public synchronized void setReadTimes(int readTimes) {
        this.readTimes = readTimes;
    }

    /**
     * To change read status
     * @param status
     */
    public void setStatus(Status status) {

        synchronized (this) {
            this.notifyAll();
            this.status = status;
        }

    }

    /**
     * To get status
     * @return
     */
    public Status getStatus() {
        return this.status;
    }


    /**
     * To stop reading,maybe this method cat not stop reading immediately.
     */
    public void stopReading() {
        keepReading = false;
    }

    /**
     * To get reading status.
     * @return boolean
     */
    public boolean isKeepReading() {
        return keepReading;
    }

    /**
     * Init Model with a Connection .
     *
     * @param conn conncetion
     */
    public M(Connection conn) {
        this.conn = conn;
    }


    /**
     * Init a model.
     */
    public M() {
    }

    /**
     * read data from db to table
     *
     * @param sql   sql
     * @param table Table
     * @return self
     * @throws SQLException
     */
    public M read(String sql, T<String> table) throws SQLException {
        if (this.conn == null) throw new RuntimeException("Connection is null!");
        return read(sql, table, this.conn);
    }

    /**
     * read db from input stream to table with format
     *
     * @param Table
     * @param inputStream
     * @param format
     * @return
     */
    private M read(T<String> Table, InputStream inputStream, Format format) {
        return this;//// TODO: 1/17/17 read db from input stream to table with format
    }

    /**
     * read data  to table from a db connection
     *
     * @param sql        sql
     * @param table      Table
     * @param connection Connection
     * @return self
     * @throws SQLException
     */
    public M read(String sql, T<String> table, Connection connection, int limit) throws SQLException {
        this.statement = connection.createStatement();
//        ((com.mysql.jdbc.Statement)statement).enableStreamingResults();
        if (limit == -1) {
            rs = statement.executeQuery(sql);
        } else {
            if (!sql.contains("limit")) {
                rs = statement.executeQuery(sql + " limit " + limit);
            } else {
                rs = statement.executeQuery(sql);
            }
        }
//        if (!sql.contains("limit"))
//            rs = statement.executeQuery(sql + " limit " + table.capacity());
//        else {
//        }

        table.setF(getField());
        setTable(table);
        this.rs.close();
        this.statement.close();
        return this;
    }

    /**
     * Read to queue
     * @param sql
     * @param queue
     * @param connection
     * @return self
     * @throws SQLException
     * @throws InterruptedException
     */
    public M read(String sql, Q queue, Connection connection) throws SQLException, InterruptedException {

//        this.statement = connection.createStatement();
        this.statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY);
        this.statement.setFetchSize(Integer.MIN_VALUE);
        this.rs = statement.executeQuery(sql);
        F f = getField();
        queue.setF(f);


        Thread t = new Thread() {
            public void run() {
                setStatus(Status.READING);

                while (keepReading) {

                    R r = new R(f);
                    try {
                        if (setRow(r, f)) {
                            if (keepReading) {
                                queue.put(r);
                                setReadTimes(getReadTimes() + 1);
                            }
                        } else {
                            setStatus(Status.READ_OUT);

                            rs.close();
                            statement.close();
                            break;
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        setStatus(Status.READ_ERR);
                        try {
                            rs.close();
                            statement.close();
                        } catch (SQLException e1) {
                            e1.printStackTrace();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        setStatus(Status.READ_ERR);
                        try {
                            rs.close();
                            statement.close();
                        } catch (SQLException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
                setStatus(Status.READ_OUT);
            }
        };

        t.start();

        return this;

    }

    /**
     *
     * @param sql
     * @param table
     * @param connection
     * @return
     * @throws SQLException
     */
    public M read(String sql, T<String> table, Connection connection) throws SQLException {
        read(sql, table, connection, -1);
        return this;
    }


    /**
     * To fill table
     *
     * @param table
     * @return self
     * @throws SQLException
     */
    private M setTable(T<String> table) throws SQLException {

        while (true) {
            if (table.hasRemaining()) {
                if (!setRow((R<String, Comparable>) table.get(), table.getF())) {
                    table.down();
                    break;
                }
            } else {
                boolean hasNext = rs.next();
                if (hasNext) {
                    table.extend(table.capacity() / 2);
                    rs.previous();
                } else {
                    break;
                }
            }
        }


        return this;
    }


    private F getField() throws SQLException {
        if (this.rs == null) throw new SQLException("Err:ResultSet is Null");

        ResultSetMetaData metaData = rs.getMetaData();
        int size = metaData.getColumnCount();
        F<String, Class<?>> f = new F<>(size);


        for (int i = 1; i <= size; i++) {
            String fieldName = metaData.getColumnLabel(i);
            int tN = metaData.getColumnType(i);
            Class c = Type.dbJava.get(tN);
            f.setType(fieldName, c);
        }
        return f;

    }

    /**
     * To fill a Row
     *
     * @param row Row
     * @param f   Field
     * @return boolean has next
     * @throws SQLException
     */
    private boolean setRow(R<String, Comparable> row, F<String, Class<?>> f) throws SQLException {

        if (rs.isClosed()) {
            return false;
        }
        if (this.rs == null)
            throw new SQLException("Err:ResultSet is Null");

        if (row == null)
            throw new RuntimeException("row is null!");

        ResultSetMetaData metaData = rs.getMetaData();
        if (this.rs.next()) {
            for (int i = 1; i <= f.size(); i++) {
                String fieldName = metaData.getColumnLabel(i);

                Class c = f.getType(fieldName);

                if (c.equals(String.class)) {
                    row.setColumn(fieldName, rs.getString(i));
                } else if (c.equals(Integer.class)) {
                    row.setColumn(fieldName, rs.getInt(i));

                } else if (c.equals(Double.class)) {
                    row.setColumn(fieldName, rs.getDouble(i));

                } else if (c.equals(Float.class)) {
                    row.setColumn(fieldName, rs.getFloat(i));

                } else if (c.equals(BigDecimal.class)) {
                    row.setColumn(fieldName, rs.getBigDecimal(i));

                } else if (c.equals(BigInteger.class)) {
                    row.setColumn(fieldName, rs.getBigDecimal(i) != null ? rs.getBigDecimal(i).toBigInteger() : null);

                } else if (c.equals(Time.class)) {
                    row.setColumn(fieldName, rs.getTime(i));

                } else if (c.equals(Timestamp.class)) {
                    row.setColumn(fieldName, rs.getTimestamp(i));

                } else if (c.equals(Date.class)) {
                    row.setColumn(fieldName, rs.getDate(i));

                } else if (c.equals(Byte.class)) {
                    row.setColumn(fieldName, rs.getByte(i));
                } else if (c.equals(Boolean.class)) {
                    row.setColumn(fieldName, rs.getBoolean(i));
                } else if (c.equals(Short.class)) {
                    row.setColumn(fieldName, rs.getShort(i));
                }

            }


            return true;
        } else {
            return false;
        }

    }


    /**
     * Sql model
     */
    private static class Sql {
        private HashMap<String, Integer> map;
        private String sql;
        private boolean isEmpty;

        public HashMap<String, Integer> getMap() {
            return map;
        }

        public void setMap(HashMap<String, Integer> map) {
            this.map = map;
        }

        public String getSql() {
            return sql;
        }

        public void setSql(String sql) {
            this.sql = sql;
        }

        public Sql(HashMap<String, Integer> map, String sql) {
            this.map = map;
            this.sql = sql;
            isEmpty = map.isEmpty();
        }

        public Sql() {
            this(new HashMap<String, Integer>(), "");
        }

        public boolean isEmpty() {
            return isEmpty;
        }

        @Override
        public String toString() {
            return "Sql{" +
                    "map=" + map +
                    ", sql='" + sql + '\'' +
                    '}';
        }
    }

    /**
     * To build select
     *
     * @param tableName   table name
     * @param primaryKeys primary key
     * @return Sql
     */
    private Sql buildSelect(String tableName, Set<String> primaryKeys) {

        if (primaryKeys.size() != 0) {

            HashMap<String, Integer> selectMap = new HashMap<>();
            StringBuffer select = new StringBuffer();
            select.append("select count(*) from " + tableName + " where ");
            int[] si = new int[]{0};
            primaryKeys.forEach(key -> {
                selectMap.put(key, si[0] + 1);
                select.append(key + " = ? ");
                if (si[0] != primaryKeys.size() - 1) {
                    select.append(" and ");
                }
                si[0]++;
            });
            return new Sql(selectMap, select.toString());
        } else {
            return new Sql();
        }
    }

    /**
     * To build update
     *
     * @param tableName
     * @param primaryKeys
     * @param fields      allFields or limited fields
     * @return Sql
     */
    private Sql buildUpdate(String tableName, Set<String> primaryKeys, Set<String> fields) {
        //remove from all primary key from field
        Set<String> newFields = new HashSet<String>();
        newFields.addAll(fields);
        newFields.removeAll(primaryKeys);

        if (primaryKeys.size() != 0 && newFields.size() != 0) {


            HashMap<String, Integer> updateMap = new HashMap<>();
            StringBuffer update = new StringBuffer();
            update.append("update " + tableName + " set ");

            int[] count = new int[]{0};
            int[] usi = new int[]{0};
            int newFieldsSize = newFields.size();
            newFields.forEach(key -> {
                updateMap.put(key, count[0] + 1);
                update.append(key + " = ?");
                if (usi[0] != newFieldsSize - 1) {
                    update.append(" , ");
                }
                usi[0]++;
                count[0]++;
            });
            update.append(" where ");
            int[] uwi = new int[]{0};
            primaryKeys.forEach(key -> {
                updateMap.put(key, count[0] + 1);
                update.append(key + " = ? ");
                if (uwi[0] != primaryKeys.size() - 1) {
                    update.append(" and ");
                }
                uwi[0]++;
                count[0]++;
            });

            return new Sql(updateMap, update.toString());
        } else
            return new Sql();
    }

    /**
     * To build Insert
     *
     * @param tableName
     * @param fields
     * @return Sql
     */
    private Sql buildInsert(String tableName, Set<String> fields) {

        if (fields.size() != 0) {
            HashMap<String, Integer> insertMap = new HashMap<>();
            StringBuffer insert = new StringBuffer();
            insert.append("insert into " + tableName + " ( ");
            int[] ii = new int[]{0};
            int newFieldsSize = fields.size();
            fields.forEach(key -> {
                insertMap.put(key, ii[0] + 1);
                insert.append(key);
                if (ii[0] != newFieldsSize - 1) {
                    insert.append(" , ");
                }
                ii[0]++;
            });
            insert.append(" ) values (");
            for (int i = 0; i < newFieldsSize; i++) {
                insert.append("?");
                if (i != newFieldsSize - 1) {
                    insert.append(" , ");
                }
            }
            insert.append(" ) ");

            return new Sql(insertMap, insert.toString());
        } else {
            return new Sql();
        }
    }


    /**
     * To set value for prepareStatement
     *
     * @param c
     * @param col
     * @param fieldName
     * @param row
     * @param statement
     * @throws SQLException
     */
    private void setValue(Class<?> c, int col, String fieldName, R<String, Comparable> row, PreparedStatement statement) throws SQLException {
        Object value = row.getColumn(fieldName);
        if (c.equals(String.class)) {
            String v = value == null ? null : (String) value;

            if (v == null) {
                statement.setObject(col, null);
            } else {
                statement.setString(col, v);
            }
        } else if (c.equals(Integer.class)) {

            Integer v = value == null ? null : (Integer) value;
            if (v == null) {
                statement.setObject(col, null);
            } else {
                statement.setInt(col, v);
            }
        } else if (c.equals(Long.class)) {
            Long v = value == null ? null : (Long) value;
            if (v == null) {
                statement.setObject(col, null);
            } else {
                statement.setLong(col, v);
            }

        } else if (c.equals(Short.class)) {
            Short v = value == null ? null : (Short) value;
            if (v == null) {
                statement.setObject(col, null);
            } else {
                statement.setShort(col, v);
            }

        } else if (c.equals(Byte.class)) {
            Byte v = value == null ? null : (Byte) value;
            if (v == null) {
                statement.setObject(col, null);
            } else {
                statement.setByte(col, v);
            }

        } else if (c.equals(BigDecimal.class)) {
            BigDecimal v = value == null ? null : (BigDecimal) value;
            if (v == null) {
                statement.setObject(col, null);
            } else {
                statement.setBigDecimal(col, v);
            }

        } else if (c.equals(BigInteger.class)) {
            BigInteger v = value == null ? null : (BigInteger) value;
            if (v != null) {
                statement.setBigDecimal(col, new BigDecimal(v));
            } else {

                statement.setObject(col, v);
            }
        } else if (c.equals(Float.class)) {
            Float v = value == null ? Float.NaN : (Float) value;
            statement.setFloat(col, v);

        } else if (c.equals(Double.class)) {
            Double v = value == null ? Double.NaN : (Double) value;
            statement.setDouble(col, v);

        } else if (c.equals(Time.class)) {
            Time v = value == null ? null : (Time) value;
            statement.setTime(col, v);

        } else if (c.equals(Timestamp.class)) {
            Timestamp v = value == null ? null : (Timestamp) value;
            statement.setTimestamp(col, v);

        } else if (c.equals(Date.class)) {
            Date v = value == null ? null : (Date) value;
            statement.setDate(col, v);

        } else if (c.equals(Boolean.class)) {
            Boolean v = value == null ? null : (Boolean) value;
            statement.setBoolean(col, v);
        } else {
            throw new RuntimeException("Not matched type:" + c.getName());
        }
    }


    /**
     * To set values for prepareStatement
     *
     * @param preparedStatement
     * @param row
     * @param sql
     * @param field
     * @throws SQLException
     */
    private void setValues(PreparedStatement preparedStatement, R<String, Comparable> row, Sql sql, F<String, Class<?>> field) throws SQLException {
        HashMap<String, Integer> map = sql.getMap();

        Set<String> fields = map.keySet();
        for (String f : fields) {
            Class<?> c = field.getType(f);
            int col = map.get(f);
            setValue(c, col, f, row, preparedStatement);
        }
    }

    /**
     * exec select
     *
     * @param preparedStatement
     * @param row
     * @param sql
     * @param field
     * @return
     * @throws SQLException
     */
    private int execSelect(PreparedStatement preparedStatement, R<String, Comparable> row, Sql sql, F<String, Class<?>> field) throws SQLException {
        setValues(preparedStatement, row, sql, field);
        preparedStatement.executeQuery();
        ResultSet rs = preparedStatement.executeQuery();

        if (rs.next()) {
            return rs.getInt(1);
        }
        return -1;
    }

    /**
     * @param preparedStatement
     * @param row
     * @param sql
     * @param field
     * @throws SQLException
     */
    private void setInsert(PreparedStatement preparedStatement, R<String, Comparable> row, Sql sql, F<String, Class<?>> field) throws SQLException {

        setValues(preparedStatement, row, sql, field);
        preparedStatement.executeUpdate();
    }


    /**
     * @param preparedStatement
     * @param row
     * @param sql
     * @param field
     * @throws SQLException
     */
    private void setUpdate(PreparedStatement preparedStatement, R<String, Comparable> row, Sql sql, F<String, Class<?>> field) throws SQLException {
        setValues(preparedStatement, row, sql, field);
        preparedStatement.executeUpdate();
    }

    /**
     * write table to a connection
     *
     * @param table
     * @param tableName
     * @param primaryKeys
     * @param fields
     * @return
     * @throws SQLException
     */
    public M write(T<String> table, String tableName, Set<String> primaryKeys, Set<String> fields, Connection connection) throws SQLException {

        Set<String> newFields = fields.size() == 0 ? table.getF().keySet() : fields;

        PreparedStatement selectStatement = null;
        PreparedStatement insertStatement = null;
        PreparedStatement updateStatement = null;

        Sql select = buildSelect(tableName, primaryKeys);
        Sql insert = buildInsert(tableName, newFields);
        Sql update = buildUpdate(tableName, primaryKeys, newFields);

        if (!select.isEmpty()) {

            selectStatement = connection.prepareStatement(select.getSql());
            insertStatement = connection.prepareStatement(insert.getSql());
            if (!update.isEmpty()) {
                updateStatement = connection.prepareStatement(update.getSql());
            }
        } else {
            insertStatement = connection.prepareStatement(insert.getSql());
        }


        try {
            //select.
            table.flip();
            ArrayList<Integer> selectRes = new ArrayList<>();
            while (table.hasRemaining()) {
                R<String, Comparable> row = (R<String, Comparable>) table.get();

                F<String, Class<?>> f = table.getF();

                if (select.isEmpty()) {
                    selectRes.add(0);
                } else {
                    int sRes = execSelect(selectStatement, row, select, f);
                    Log.getLogger().trace(selectStatement.toString());
                    Log.getLogger().trace("select result is " + sRes);
                    selectRes.add(sRes);
                }
            }

            //insert or update
            connection.setAutoCommit(false);
            table.flip();
            int i = 0;
            while (table.hasRemaining()) {
                R<String, Comparable> row = (R<String, Comparable>) table.get();
                F<String, Class<?>> f = table.getF();
                int sRes = selectRes.get(i);
                if (sRes == 0) {
                    setInsert(insertStatement, row, insert, f);

                    Log.getLogger().trace(insertStatement.toString());
                } else if (sRes == 1) {
                    if (updateStatement != null) {
                        setUpdate(updateStatement, row, update, f);
                        Log.getLogger().trace(updateStatement.toString());
                    }
                } else {
                    throw new RuntimeException("Error:please check if your primary key is wrong !");
                }
                i++;
            }
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            Log.getLogger().error("ERROR",e);
            throw new SQLException(e);
        } finally {
            if (insertStatement != null && !insertStatement.isClosed()) {
                insertStatement.close();
            }
            if (updateStatement != null && !updateStatement.isClosed()) {
                updateStatement.close();
            }
            if (selectStatement != null && !selectStatement.isClosed()) {
                selectStatement.close();
            }
        }

        return this;
    }

    /**
     * write table to db
     *
     * @param table
     * @param tableName
     * @param primaryKeys
     * @param fields
     * @return
     * @throws SQLException
     */
    public M write(T<String> table, String tableName, Set<String> primaryKeys, Set<String> fields) throws SQLException {
        if (this.conn == null) throw new RuntimeException("Connection is null !");
        return write(table, tableName, primaryKeys, fields, this.conn);
    }


    /**
     * Write table to outputStream with format
     *
     * @param table
     * @param tableName
     * @param fields
     * @param outputStream
     * @return
     */
    private M write(T<String> table, String tableName, Set<String> fields, OutputStream outputStream, Format format) {
        StringBuffer buffer = new StringBuffer();
        fields = (fields == null || fields.size() == 0) ? table.getF().keySet() : fields;

        if (format.equals(Format.JSON) || format.equals(Format.JSON_ARRAY_FIELD)) {

            table.flip();
            while (table.hasRemaining()) {
                R<String, Comparable> row = (R<String, Comparable>) table.get();

                fields.forEach(k -> {
                    row.getColumn(k);
                });
            }
        } else if (format.equals(Format.CSV) || format.equals(Format.CSV_WITHOUT_HEAD)) {

        } else {
            ////todo:write :default gave a WARNING and  output to stdout with format csv.
        }


        return this;
    }
}
