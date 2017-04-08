package com.github.doghere.jdbc;

import com.github.doghere.Each;
import com.github.doghere.Writer;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.sql.Date;
import java.util.*;

/**
 * Created by dog on 4/6/17.
 */
abstract public class JDBCWriter<E> implements Writer<E>,Each<E>{
    private Connection connection;
    private String target;
    private Set<String> primaryKeys;
    private Set<String> columnNames = new HashSet<>();
    private Set<String> newColumnNames;

    private PreparedStatement selectStatement ;
    private PreparedStatement insertStatement ;
    private PreparedStatement updateStatement ;

    private Sql select ;
    private Sql insert;
    private Sql update;

    public JDBCWriter(){}


    public Connection getConnection() {
        return null;
    }

    public JDBCWriter<E> setConnection(Connection connection) {
        this.connection = connection;
        return this;
    }

    @Override
    public void before() throws Exception {

        connection.setAutoCommit(false);
    }

    @Override
    public void after() throws Exception {
        connection.commit();
        connection.close();
    }


    private void createStatement(Set<String> newColumnNames) throws SQLException {

        select = buildSelect(target, primaryKeys);
        insert = buildInsert(target, newColumnNames);
        update = buildUpdate(target, primaryKeys, newColumnNames);

        if (!select.isEmpty()) {

            selectStatement = connection.prepareStatement(select.getSql());
            insertStatement = connection.prepareStatement(insert.getSql());
            if (!update.isEmpty()) {
                updateStatement = connection.prepareStatement(update.getSql());
            }
        } else {
            insertStatement = connection.prepareStatement(insert.getSql());
        }
    }




    @Override
    public void onEvent(E r) throws Exception {
        //deal with o

//        if(l%10000==0) {
//            connection.commit();
//            System.out.println(l);
//        }
        r = dealWithEach(r);
        Field field = ((Row)r).getField();
        Row row = (Row)r;
        if (newColumnNames == null) {
            newColumnNames = columnNames.size() == 0 ? field.keySet() : columnNames;
            createStatement(newColumnNames);
        }


        //select.
        int isThere = 0;
        if(!select.isEmpty) {
            isThere = execSelect(selectStatement, row, select, field);
//            System.out.println(selectStatement+"\tselect result: "+isThere);
        }

        //insert or update
        try {
            if (isThere == 0) {
                setInsert(insertStatement, row, insert,field);
//                System.out.println(insertStatement);
            } else if (isThere == 1) {
                if (updateStatement != null) {

                    setUpdate(updateStatement, row, update, field);
//                    System.out.println(updateStatement);
                }
            } else {
                throw new RuntimeException("Error:please check if your primary key is wrong !");
            }
        }catch (Exception e){
            e.printStackTrace();
            connection.rollback();
            connection.close();
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
    private void setValue(Class<?> c, int col, String fieldName, Row<String, Comparable> row, PreparedStatement statement) throws SQLException {
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
    private void setValues(PreparedStatement preparedStatement, Row<String, Comparable> row, Sql sql, Field<String, Class<?>> field) throws SQLException {
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
    private int execSelect(PreparedStatement preparedStatement, Row<String, Comparable> row, Sql sql, Field<String, Class<?>> field) throws SQLException {
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
    private void setInsert(PreparedStatement preparedStatement, Row<String, Comparable> row, Sql sql, Field<String, Class<?>> field) throws SQLException {

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
    private void setUpdate(PreparedStatement preparedStatement, Row<String, Comparable> row, Sql sql, Field<String, Class<?>> field) throws SQLException {
        setValues(preparedStatement, row, sql, field);
        preparedStatement.executeUpdate();
    }


    public String getTarget() {
        return target;
    }

    public JDBCWriter setTarget(String target) {
        this.target = target;
        return this;
    }



    public Set<String> getPrimaryKeys() {
        return primaryKeys;
    }

    public JDBCWriter setPrimaryKeys(Set<String> primaryKeys) {
        this.primaryKeys = primaryKeys;
        return this;
    }
    public JDBCWriter setPrimaryKeys(String primaryKeys) {
        String arr[] = primaryKeys.split(",");
        Set<String> ps = new HashSet<>();
        for(String a:arr) ps.add(a);
        this.primaryKeys = ps;
        return this;
    }

    public Set<String> getColumnNames() {
        return columnNames;
    }



    public JDBCWriter setColumnNames(Set<String> columnNames) {
        this.columnNames = columnNames;
        return this;
    }


    public JDBCWriter setColumnNames() {
        return setColumnNames("");
    }
    public JDBCWriter setColumnNames(String columnNames) {
        if(!columnNames.equals("")) {
            String arr[] = columnNames.split(",");
            Set<String> ps = new HashSet<>();
            for (String a : arr) ps.add(a);
            this.columnNames = ps;
        }
        return this;
    }

    abstract public E dealWithEach(E e);
}
