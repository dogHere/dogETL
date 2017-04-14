package com.github.doghere.jdbc;

import com.github.doghere.Field;
import com.github.doghere.Reader;
import com.github.doghere.Row;
import com.lmax.disruptor.RingBuffer;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;

/**
 * Created by dog on 4/6/17.
 */
public class JDBCReader implements Reader {

    private String SQL;
    private Connection connection;
    private volatile boolean hasRemaining = true;

    private Statement statement;
    private ResultSet rs;
    private Field field;

    private RingBuffer<Row> ringBuffer;



    public JDBCReader(){}


    @Override
    public void before() throws SQLException {

        this.statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY);
        this.statement.setFetchSize(Integer.MIN_VALUE);
        this.rs = statement.executeQuery(SQL);
        this.field = getField();

    }


    private Field getField() throws SQLException {
        if (this.rs == null) throw new SQLException("Err:ResultSet is Null");

        ResultSetMetaData metaData = rs.getMetaData();
        int size = metaData.getColumnCount();
        Field<String, Class<?>> field = new Field<>(size);


        for (int i = 1; i <= size; i++) {
            String fieldName = metaData.getColumnLabel(i);
            int tN = metaData.getColumnType(i);
            Class c = JDBCType.dbJava.get(tN);
            field.setType(fieldName, c);
        }
        return field;

    }



    /**
     * To fill a Row
     *
     * @param row Row
     * @param field   Field
     * @return boolean has next
     * @throws SQLException
     */
    private boolean setRow(Row<String, Comparable> row, Field<String, Class<?>> field) throws SQLException {

        if (rs.isClosed()) {
            return false;
        }
        if (this.rs == null)
            throw new SQLException("Err:ResultSet is Null");

        if (row == null)
            throw new RuntimeException("row is null!");

        ResultSetMetaData metaData = rs.getMetaData();
        if (this.rs.next()) {
            for (int i = 1; i <= field.size(); i++) {
                String fieldName = metaData.getColumnLabel(i);

                Class c = field.getType(fieldName);

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

    @Override
    public void read() throws Exception {
        long sequence = ringBuffer.next();  // Grab the next sequence
        try {
            Row row = ringBuffer.get(sequence); // Get the entry in the Disruptor
            if(!row.isCanWrite()) row.setCanWrite(true);
            if(row.getField()==null) {
                row.setField((Field)field.clone());
            }
            // for the sequence
            if(!setRow(row, field)){
                hasRemaining=false;
            }
        }catch (Exception e){
            e.printStackTrace();
            throw new Exception(e);
        } finally {
            if(hasRemaining) ringBuffer.publish(sequence);
        }

    }

    @Override
    public void setRingBuffer(RingBuffer ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    @Override
    public boolean hasRemaining() {
        return hasRemaining;
    }

    @Override
    public void after() throws SQLException {

        rs.close();
        statement.close();
        connection.close();
    }

    public Statement getStatement() {
        return statement;
    }

    public JDBCReader setStatement(Statement statement) {
        this.statement = statement;
        return this;
    }

    public ResultSet getRs() {
        return rs;
    }

    public JDBCReader setRs(ResultSet rs) {
        this.rs = rs;
        return this;
    }


    @Override
    public JDBCReader setField(Field field) {
        this.field = field;
        return this;
    }


    public String getSQL() {
        return this.SQL;
    }

    public JDBCReader setSQL(String sql) {
        this.SQL = sql;
        return this;
    }

    public Connection getConnection() {

        return this.connection;
    }

    public JDBCReader setConnection(Connection connection) {
        this.connection = connection;
        return this;
    }



}
