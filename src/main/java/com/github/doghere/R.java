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
public class R<S, E> extends ArrayList<E> {
    private F<S, Class<?>> f;
    private Object lock = new Object();

    /**
     * Init a R default with a F
     */
    public R() {
        this(new F<S, Class<?>>());
    }

    /**
     * Init Row by Field .<br><br>
     * <p>
     * The size is to init the arrayList.
     *
     * @param f    Field
     * @param size the number of init column
     */
    public R(F<S, Class<?>> f, int size) {
        super(size);
        this.f = f;
        setNull(size);
    }

    /**
     * Init Row by Field .<br><br>
     * <p>
     * Default size is the Field size.
     *
     * @param f Field
     */
    public R(F<S, Class<?>> f) {
        this(f, f.size());
    }

    private void setNull(int size) {
        for (int i = 0; i < size; i++) {
            this.add(null);
        }
    }


    /**
     * To get Field
     *
     * @return Field
     */
    public F<S, Class<?>> getF() {
        return this.f;
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
    public R<S, E> setColumn(int index, E e) {
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
    public R<S, E> setColumn(S fieldName, E e) {

        if (this.f.containsKey(fieldName)) {
            synchronized (this) {
                if (e == null) {
                    this.set(this.f.getNumber(fieldName), e);
                } else if (e != null && e.getClass().equals(this.f.getType(fieldName))) {
                    this.set(this.f.getNumber(fieldName), e);
                } else
                    throw new RuntimeException("type `" + e.getClass() + "` of value `" + e + "` not matched `" + this.f.getType(fieldName) + "`");
            }
        } else
            throw new RuntimeException("f name `" + fieldName + "` not found!");
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
    public R<S, E> setColumn(S fieldName, E e, Class<?> c) {
        synchronized (lock) {
            if (!this.f.hasName(fieldName)) {
                this.add(null);
            }
            this.f.setType(fieldName, c);
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
        if(f.containsKey(fieldName)) {
            int num = this.f.getNumber(fieldName);
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
     * To set column from R.<br><br>
     * <p>
     * This method will update the old value and field,add new value and field.
     *
     * @param r R
     * @return self
     */
    public R<S, E> setColumn(R<S, E> r) {
        r.f.keySet().forEach(name -> {
            this.setColumn(name, r.getColumn(name), r.f.getType(name));
        });
        return this;
    }


    /**
     * To remove column from row by field name.If it not contains the key,do nothing.
     *
     * @param fieldName field name
     * @return self
     */
    public R<S, E> removeColumn(S fieldName) {
        synchronized (lock) {
            if(f.containsKey(fieldName)) {
                int num = f.getNumber(fieldName);
                this.remove(num);
                this.f.removeColumn(fieldName);
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
    public R<S, E> removeColumn(S... fieldNames) {
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
        return this.get(this.f.getNumber(fieldName));
    }


    public static R makeByPair(F f, String fieldName1, String fieldName2, Object v1, Object v2) {
        R r = new R(f, f.size());
        r.setColumn(fieldName1, v1);
        r.setColumn(fieldName2, v2);
        return r;
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

        R<?, ?> row2 = (R<?, ?>) o;

        return f.equals(row2.f);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + f.hashCode();
        return result;
    }


    public static void main(String[] args) {
        F<String, Class<?>> f = new F<>();
        f.setType("id", Integer.class);
        f.setType("good", String.class);
        f.setType("name", String.class);
        R<String, Object> r = new R<String, Object>(f);

        r.setColumn("id", 12);
        r.setColumn("good", "yes good");
        r.setColumn("name", "dog");

        System.out.println(r);
        r.setColumn("pass", "12345", String.class);
        System.out.println(r);
        System.out.println(r.getF());


        r.setColumn("good", "nonono");
        System.out.println(r);
        System.out.println(r.getF());


        r.setColumn("good", 123, Integer.class);
        System.out.println(r);
        System.out.println(r.getF());

        r.removeColumn("id");
        System.out.println(r);
        System.out.println(r.getF());

        R<String, Object> r2 = new R<String, Object>();
        r2.setColumn("pp", "pp", String.class);
        r2.setColumn("good", "ppgood", String.class);

        r.setColumn(r2);
        System.out.println(r);
        System.out.println(r.getF());
    }


}
