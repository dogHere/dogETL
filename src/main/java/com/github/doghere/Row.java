package com.github.doghere;

import java.util.ArrayList;


/**
 * Created by dog on 12/17/16.
 *
 * @author dogHere@tutamail.com
 *         <p>
 *         This is the Row model of Db.
 *         <br><br>
 */
public class Row<S, E> extends ArrayList<E> implements Strict,CanRead,CanWrite{


    private boolean canWrite = true;
    private boolean canRead  = true;
    private boolean useStrict  = true;
    private boolean isBlank = false;

    private Field<S, Class<?>> field;
    private Object lock = new Object();

    private String tableName ;

    /**
//     * Init a Row default with a Field
     */
    public Row() {
//        this(new Field<S, Class<?>>());
    }

    /**
     * Init Row by Field .<br><br>
     * <p>
     * The size is to init the arrayList.
     *
     * @param field    Field
     * @param size the number of init column
     */
    public Row(Field<S, Class<?>> field, int size) {
        super(size);
        this.field = field;
        setNull(size);
    }

    /**
     * Init Row by Field .<br><br>
     * <p>
     * Default size is the Field size.
     *
     * @param field Field
     */
    public Row(Field<S, Class<?>> field) {
        this(field, field.size());
    }

    private void setNull(int size) {
        for (int i = 0; i < size; i++) {
            this.add(null);
        }
    }



    public Row<S,E> setField(Field<S,Class<?>> field){
        this.field = field;
        setNull(field.size());
        return this;
    }

    /**
     * To get Field
     *
     * @return Field
     */
    public Field<S, Class<?>> getField() {
        return this.field;
    }


    /**
     * To set column value by column number.
     * <p>
     * Not checked ,Not safe.
     *
     * @param index column number
     * @param e     new column value
     * @return self
     */
    public Row<S, E> setColumn(int index, E e) {
        if (index < this.size()) {
            synchronized (this) {
                this.set(index, e);
            }
        } else {
            throw new RuntimeException("outflow row column number :" + index + ":" + e + ",size is " + this.size());
        }
        return this;
    }

    /**
     * To set column value by field name.<br><br>
     * <p>
     * This method will check if the field name  exists.If not,
     * It will throw RuntimeException.
     *
     * @param fieldName field name
     * @param e         column value
     * @return self
     */
    public Row<S, E> setColumn(S fieldName, E e) {

        if (this.field.containsKey(fieldName)) {
            if(!useStrict) {
                this.set(this.field.getNumber(fieldName), e);
                return this;
            }
            synchronized (this) {
                if (e == null) {
                    this.set(this.field.getNumber(fieldName), e);
                } else if (e != null && e.getClass().equals(this.field.getType(fieldName))) {
                    this.set(this.field.getNumber(fieldName), e);
                } else
                    throw new RuntimeException("type `" + e.getClass() + "` of value `" + e + "` not matched `" + this.field.getType(fieldName) + "`");
            }
        } else
            throw new RuntimeException("field name `" + fieldName + "` not found!");
        return this;
    }

    public Row<S,E> setUseStrict(boolean useStrict){
        this.useStrict = useStrict;
        return this;
    }

    /**
     * To add new column to row .<br><br>
     * <p>
     * If field name exist ,this column will be updated,then just call setColumn.
     * else it will add null and put new value .
     *
     * @param fieldName field name
     * @param e         column value
     * @param c         column type
     * @return self
     */
    public Row<S, E> setColumn(S fieldName, E e, Class<?> c) {
        synchronized (lock) {
            if(this.field==null) this.field = new Field<>();
            if (!this.field.hasName(fieldName)) {
                this.add(null);
            }
            this.field.setType(fieldName, c);
            this.setColumn(fieldName, e);
        }
        return this;
    }

    /**
     * To get column value by field name.
     *
     * @param fieldName filed name
     * @return column value
     */
    public E getColumn(S fieldName) {
        if(field.containsKey(fieldName)) {
            int num = this.field.getNumber(fieldName);
            E e = this.get(num);
            return e;
        }else {
            throw new RuntimeException("Field do not exist:"+fieldName);
        }
    }
    /**
     * To get column value by field number
     * @param num
     * @return
     */
    public E getColumn(int num){
        E e = this.get(num);
        return e;
    }

    /**
     * To set column from Row.<br><br>
     * <p>
     * This method will update the old value and field,add new value and field.
     *
     * @param row Row
     * @return self
     */
    public Row<S, E> setColumn(Row<S, E> row) {
        if(this.field==null) this.setField( row.field);
        if(!this.field.equals(row.field)) this.field = row.field;
        row.field.keySet().forEach(name -> {
            this.setColumn(name, row.getColumn(name), row.field.getType(name));
        });
        return this;
    }


    /**
     * To remove column from row by field name.If it not contains the key,do nothing.
     *
     * @param fieldName field name
     * @return self
     */
    @Deprecated
    public Row<S, E> removeColumn(S fieldName) {
        synchronized (lock) {
            if(field.containsKey(fieldName)) {
                int num = field.getNumber(fieldName);
                this.remove(num);
                this.field.removeColumn(fieldName);
            }
        }
        return this;
    }

    /**
     * To remove columns from row by field name
     * <p>
     * .
     *
     * @param fieldNames field names
     * @return self
     */
    public Row<S, E> removeColumn(S... fieldNames) {
        for (S s : fieldNames) {
            removeColumn(s);
        }
        return this;
    }


    /**
     * To get the field number by field name.
     *
     * @param fieldName field name
     * @return field number
     */
    public E getNumber(S fieldName) {
        return this.get(this.field.getNumber(fieldName));
    }


    public static Row makeByPair(Field field, String fieldName1, String fieldName2, Object v1, Object v2) {
        Row row = new Row(field, field.size());
        row.setColumn(fieldName1, v1);
        row.setColumn(fieldName2, v2);
        return row;
    }

    @Override
    public String toString() {
        StringBuffer s = new StringBuffer();


        s.append("[");
        int i[] = new int[1];
        this.forEach(e -> {
            s.append(e);
            if (this.size() - 1 != i[0])
                s.append(",");
            i[0]++;
        });
        s.append("]\n");
        return s.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Row<?, ?> row2 = (Row<?, ?>) o;

        return field.equals(row2.field);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + field.hashCode();
        return result;
    }


    public boolean isCanWrite() {
        return canWrite;
    }

    public Row setCanWrite(boolean canWrite) {
        this.canWrite = canWrite;
        return this;
    }

    public boolean isCanRead() {
        return canRead;
    }

    public Row setCanRead(boolean canRead) {
        this.canRead = canRead;
        return this;
    }

    public static void main(String[] args) {
        Field<String, Class<?>> field = new Field<>();
        field.setType("id", Integer.class);
        field.setType("good", String.class);
        field.setType("name", String.class);
        Row<String, Object> row = new Row<String, Object>(field);

        row.setColumn("id", 12);
        row.setColumn("good", "yes good");
        row.setColumn("name", "dog");

        System.out.println(row);
        row.setColumn("pass", "12345", String.class);
        System.out.println(row);
        System.out.println(row.getField());


        row.setColumn("good", "nonono");
        System.out.println(row);
        System.out.println(row.getField());


        row.setColumn("good", 123, Integer.class);
        System.out.println(row);
        System.out.println(row.getField());

        row.removeColumn("id");
        System.out.println(row);
        System.out.println(row.getField());

        Row<String, Object> row2 = new Row<String, Object>();
        row2.setColumn("pp", "pp", String.class);
        row2.setColumn("good", "ppgood", String.class);

        row.setColumn(row2);
        System.out.println(row);
        System.out.println(row.getField());
    }


    @Override
    public Row<S, E> setStrict(boolean isStrict) {
        this.useStrict = isStrict;
        return this;
    }


    public String[] toStringArray(){
        String [] arr = new String[size()];
        int[]i=new int[]{0};
        this.forEach(k->{
            if(k==null)
                arr[i[0]] = null;
            else
                arr[i[0]] = k.toString();
            i[0]+=1;
        });
        return arr;
    }

    public String getTableName() {
        return tableName;
    }

    public Row setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    /**
     * It is a tag to determine hasRemaining
     * @return
     */
    public boolean isBlank() {
        return isBlank;
    }

    public Row setBlank(boolean blank) {
        isBlank = blank;
        return this;
    }
}
