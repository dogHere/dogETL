package com.github.doghere;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by dog on 12/14/16.
 * <p>
 * This is the Field model of Db field.
 * <p>
 * The Field has two value,one is the field'Type the other is the field's number.<br>
 * <br>
 * The Field model extends HashMap.The key is the field's name,the value is the field type. <br>
 * The Field model has a field number mapper.The key is the field's name,the value is the field's number.
 *
 * @param <S> the type of Field names
 * @param <C> the type of Filed types
 * @author dogHere@tutamail.com
 */
public class F<S, C> extends HashMap<S, C> {

    private int position = -1;
    private Map<S, Integer> fieldNumber;

    /**
     * Capacity size to init Field.
     * <p>
     * The size points to how many columns a field instance contains.
     *
     * @param size
     */
    public F(int size) {
        super(size);
        this.fieldNumber = new HashMap<>(size);
    }

    /**
     * Init Field with a hashMap ,default size is 8.
     */
    public F() {
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
    public F<S, C> setType(S fieldName, C type) {
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
    public F<S, C> setType(F<S, C> field) {
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
    public F<S, C> removeColumn(S name) {
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
    public F<S, C> removeColumns(S... names) {
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
     * filter will create new F
     *
     * @param fs field set
     * @return new F
     */
    public  F<S,C> filter(Set<S> fs){
        F<S,C> f = new F<>();
        this.forEach((k,v)->{
            if(fs.contains(k)){
                f.setType(k,v);
            }
        });
        return f;
    }

    @Override
    public String toString() {
        StringBuffer s = new StringBuffer();
        this.forEach((k, v) -> {
            s.append(k + ":" + fieldNumber.get(k) + "->" + v);
            s.append(",\n");
        });
        return "F{" + "size:" + size() + "\n" +
                s +
                '}';
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        F<?, ?> f = (F<?, ?>) o;

        if (position != f.position) return false;
        return fieldNumber.equals(f.fieldNumber);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + position;
        result = 31 * result + fieldNumber.hashCode();
        return result;
    }

    public static void main(String[] args) {
        F<String, Class<?>> f = new F<>(2);
        f.setType("id", Integer.class);
        f.setType("id", Integer.class);
        f.setType("username", String.class);
        f.setType("good", Double.class);

        System.out.println(
                f.getNumber("username")
        );
        System.out.println(
                f.getNumber("id")
        );

        System.out.println(
                f.getType("idll")
        );
        System.out.println(f);
        f.removeColumns("id");
        System.out.println(f);

        F<String, Class<?>> f2 = new F<>(3);

        f2.setType("qq", String.class);
        f2.setType("good", Float.class);
        f2.setType(f);

        System.out.println(f2);
        System.out.println(f2.size());

    }
}
