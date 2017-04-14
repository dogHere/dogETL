package com.github.doghere;

import java.util.*;

/**
 * Created by dog on 12/14/16.
 * <p>
 * This is the Field model of Db field.
 * <p>
 * The Field has two value,one is the field'JDBCType the other is the field's number.<br>
 * <br>
 * The Field model extends HashMap.The key is the field's name,the value is the field type. <br>
 * The Field model has a field number mapper.The key is the field's name,the value is the field's number.
 *
 * @param <S> the type of Field names
 * @param <C> the type of Filed types
 * @author dogHere@tutamail.com
 */
public class Field<S, C> extends LinkedHashMap<S, C> {

    private int position = -1;
    private Map<S, Integer> fieldNumber;

    /**
     * Capacity size to init Field.
     * <p>
     * The size points to how many columns a field instance contains.
     *
     * @param size
     */
    public Field(int size) {
        super(size);
        this.fieldNumber = new HashMap<>(size);
    }

    /**
     * Init Field with a hashMap ,default size is 8.
     */
    public Field() {
        this.fieldNumber = new HashMap<>(8);
    }

    /**
     * To get column's type by the gaven field name.
     * <p>
     * If failed,it will return null.
     *
     * @param fieldName
     * @return column's type
     */
    public C getType(S fieldName) {
        return (C) this.get(fieldName);
    }

    /**
     * To get column's number by the gaven field name.
     * <p>
     * If failed,it will return null.
     *
     * @param fieldName field name
     * @return the number of the gaven field
     */
    public Integer getNumber(S fieldName) {
        return this.fieldNumber.get(fieldName);
    }


    /**
     * To set type by field name ,
     * <p>
     * If this field is not set,then  grow the column size and set it and
     * else,replace the old type.
     *
     * @param fieldName
     * @param type
     * @return self
     */
    public Field<S, C> setType(S fieldName, C type) {
        if (!this.containsKey(fieldName)) {
            synchronized (this) {
                this.position++;
                this.put(fieldName, type);
                this.fieldNumber.put(fieldName, position);
            }
        } else {
            synchronized (this) {
                this.put(fieldName, type);
                int oldPosition = this.fieldNumber.get(fieldName);
                this.fieldNumber.put(fieldName, oldPosition);
            }
        }

        return this;
    }

    /**
     * To set type from a field
     * <p>
     * If this field is not empty,it will be added to it.
     * If this field has the added field name,it will replace the old.
     *
     * @param field Field model
     * @return self
     */
    public Field<S, C> setType(Field<S, C> field) {
        field.forEach((s, c) -> {
            this.setType(s, c);
        });
        return this;
    }


    /**
     * To update number index when removed key from field number.
     * <p>
     * size --
     *
     * @param number this number is removed
     */
    private void updateNumberIndex(int number) {
        this.fieldNumber.forEach((k, v) -> {
            if (v > number) {
                this.fieldNumber.put(k, v - 1);
            }
        });
    }

    /**
     * To remove the column by name.
     * <p>
     * If this has not this field ,it will do nothing.
     * <p>
     * After the column is removed ,it will update the index of column by auto.The position --
     *
     * @param name field name
     * @return self
     */
    public Field<S, C> removeColumn(S name) {
        if (this.hasName(name) && this.hasNumber(name)) {
            synchronized (this) {
                int num = this.getNumber(name);
                this.remove(name);
                this.fieldNumber.remove(name);
                this.updateNumberIndex(num);
                position--;
            }
        }
        return this;
    }


    /**
     * To remove the column by name.
     * <p>
     * If this has not this field ,it will do nothing.
     * <p>
     * After the column is removed ,it will update the index of column by auto.
     *
     * @param names field names
     * @return self
     */
    public Field<S, C> removeColumns(S... names) {
        for (S s : names) {
            removeColumn(s);
        }
        return this;
    }


    /**
     * To check if this field is empty
     *
     * @return is empty
     */
    public boolean isEmpty() {
        return this.size() == 0;
    }

    /**
     * To check this field if has field name
     *
     * @param name field name
     * @return boolean
     */
    public boolean hasName(S name) {
        return this.containsKey(name);
    }

    /**
     * To check this field if has column number
     *
     * @param name field name
     * @return boolean
     */
    public boolean hasNumber(S name) {
        return this.fieldNumber.containsKey(name);
    }

    /**
     * To get the size of the field
     *
     * @return int
     */
    public int size() {
        return position + 1;
    }


    /**
     * filter will create new Field
     *
     * @param fs field set
     * @return new Field
     */
    public Field<S,C> filter(Set<S> fs){
        Field<S,C> field = new Field<>();
        this.forEach((k,v)->{
            if(fs.contains(k)){
                field.setType(k,v);
            }
        });
        return field;
    }

    @Override
    public String toString() {
        StringBuffer s = new StringBuffer();
        this.forEach((k, v) -> {
            s.append(k + ":" + fieldNumber.get(k) + "->" + v);
            s.append(",\n");
        });
        return "Field{" + "size:" + size() + "\n" +
                s +
                '}';
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Field<?, ?> field = (Field<?, ?>) o;

        if (position != field.position) return false;
        return fieldNumber.equals(field.fieldNumber);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + position;
        result = 31 * result + fieldNumber.hashCode();
        return result;
    }

    public String[] toStringArray(){
        String [] arr = new String[size()];
        int[]i=new int[]{0};
        keySet().forEach(k->{
            if(k==null)
                arr[i[0]] = null;
            else
                arr[i[0]] = k.toString();
            i[0]+=1;
        });
        return arr;
    }

    public static void main(String[] args) {
        Field<String, Class<?>> field = new Field<>(2);
        field.setType("id", Integer.class);
        field.setType("id", Integer.class);
        field.setType("username", String.class);
        field.setType("good", Double.class);

        System.out.println(
                field.getNumber("username")
        );
        System.out.println(
                field.getNumber("id")
        );

        System.out.println(
                field.getType("idll")
        );
        System.out.println(field);
        field.removeColumns("id");
        System.out.println(field);

        Field<String, Class<?>> field2 = new Field<>(3);

        field2.setType("qq", String.class);
        field2.setType("good", Float.class);
        field2.setType(field);

        System.out.println(field2);
        System.out.println(field2.size());

    }
}
