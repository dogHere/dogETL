package com.github.doghere;


import com.github.doghere.util.Buffer;
import com.github.doghere.util.Option;

import java.nio.BufferUnderflowException;
import java.util.*;

/**
 * Created by dog on 12/19/16.
 *
 * @author dogHere@tutamail.com
 *         <p>
 *         Table
 */
public class T<S> extends Buffer {


    private F<S, Class<?>> f;
    private Object rows[];


    /**
     * Init Table with size cap
     *
     * @param cap init size
     */
    public T(int cap) {
        super(cap);
        this.rows = new Object[cap];
    }

//    public T(){
//        super(1000);
//        this.rows = new Object[1000];
//    }


    /**
     * Extend size
     * @param size
     * @return
     */
    public T extend(int size){
        super.extend(size);
        Object rows[] = new Object[capacity()];
        for(int i=0;i<this.position();i++){
            rows[i]= this.rows[i];
        }
        this.rows=rows;
        return this;
    }
    /**
     * To get Field.
     *
     * @return Field
     */
    public F<S, Class<?>> getF() {
        return f;
    }

    /**
     * To set Field
     *
     * @param f Field
     */
    public synchronized void setF(F<S, Class<?>> f) {
        this.f = f;
    }


    /**
     * To get a Row from Table.
     * <br><br>
     * <p>
     * If table has not remaining room ,then throw new BufferUnderflowException.
     *
     * @return Row
     */
    public Object get() {

        if (hasRemaining()) {
            Object row = rows[position()];
            if (row == null) {
                row = new R<>(f, f.size());
                rows[position()] = row;
            }
            grow();
            return row;
        } else {
            throw new BufferUnderflowException();
        }
    }


    /**
     * To put row to Table
     *
     * @param o Row
     * @return position
     */
    public synchronized int put(Object o) {

        if (hasRemaining()) {
            if (this.f == null) {
                f = ((R) o).getF();
            }
            rows[position()] = o;
            grow();
        }
        return position();
    }


    /**
     * To put table to table.
     * <p>
     * This will merge two tables.
     * <p>
     * If this table's Field equals to
     * that's,just add rows to this.
     * <p>
     * Else merge Field first,then merge rows.
     * If this table's size is not equals to that,the rest will be dropped!
     *
     * @param t table
     * @return position
     */
    private int put(T t) {
        //// TODO: 1/8/17  T.put(T t)
        if (!t.f.equals(f)) {
            this.f.setType(t.f);
        } else {

        }
        return 0;
    }

//    /**
//     * union .This will create new Table
//     * @param t
//     * @return
//     */
//    public T union(T t){
//        Set<T> set = new HashSet<T>();
//
//        set.addAll(Arrays.asList(this));
//        set.addAll(Arrays.asList(t));
//        T nt = new T(set.size());
//        nt.setF(this.f);
//        set.forEach(e->{
//           nt.put(e);
//        });
//
//        return nt;
//    }

//    public T intersection(T t){
//        List<T> list = new ArrayList<T>();
//
////        T nt = new T();
//        for (Object t : this) {
//            if(list2.contains(t)) {
//                list.add(t);
//            }
//        }
//    }


    public static T makeByPairColumn(HashMap<Object, Object> c1c2, F f,
                                     String fieldName1, String fieldName2, int size) {
        T t = new T(size);
        t.setF(f);
        c1c2.forEach((c1, c2) -> {
            t.put(R.makeByPair(f, fieldName1, fieldName2, c1, c2));
        });
        return t;

    }


    /**
     * To get new table by list Tables
     *
     * @param tables Tables
     * @param size   init size
     * @return Table
     */
    public static T newInstance(List<T> tables, int size) {
        T t = new T(size);
        for (T at : tables) {
            if (t.getF() == null) {
                t.setF(at.getF());
            }
            at.flip();
            while (at.hasRemaining()) {
                t.put(at.get());
            }
        }
        return t;
    }


    /**
     * To match value with option
     *
     * @param option Option
     * @param left   Value
     * @return boolean
     */
    private boolean match(Option option, Comparable left) {
        boolean res = true;
        Comparable right = (Comparable) option.getValue();
        int comInt = left.compareTo(right);

        switch (option.getSign()) {
            case EQ:
                res = res && comInt == 0;
                break;
            case MORE:

                res = res && comInt > 0;
                break;
            case LESS:

                res = res && comInt < 0;
                break;
            case N_EQ:

                res = res && comInt != 0;
                break;
            case N_MORE:

                res = res && comInt <= 0;
                break;
            case N_LESS:

                res = res && comInt >= 0;
                break;
        }
        return res;

    }


    /**
     * To filter table by Options.This will create new Table.
     *
     * @param options Options
     * @return Table
     */
    public T filter(Set<Option> options) {
        T<S> t = new T<>(position());
        t.setF(f);

        flip();
        while (hasRemaining()) {
            R<S, Object> r = (R<S, Object>) get();
            boolean res = true;
            for (Option<S> option : options) {
                Comparable left = (Comparable) r.getNumber(option.getFieldName());
                res = res && match(option, left);
            }
            if (res) {
                t.put(r);
            }
        }
        return t;
    }


    /**
     * Fat a table ,this will create new T
     * @param option
     * @return
     */
    private T fat(Map<String,Map<String,String>> option){
//// TODO: 1/23/17 fat
       option.forEach((k,v)->{

       });
       return this;
    }


    /**
     * To get a list of column number from table by field name
     *
     * @param fieldName field name
     * @return List Column number
     */
    public List<Object> getNumbers(String fieldName) {
        List<Object> column = new ArrayList<>();
        flip();
        while (hasRemaining()) {
            column.add(((R) get()).getNumber(fieldName));
        }
        return column;
    }

        /**
     * To convert table to arrays by column
     * @return arrays of list
     */
    public List[] toArray(){
        List[] arrays = new ArrayList[this.getF().size()];
        for(int i=0;i<arrays.length;i++){
            arrays[i]=new ArrayList<>();
        }
        flip();
        while (hasRemaining()){
            R r = (R)get();
            for(int i=0;i<f.size();i++){
                arrays[i].add(r.getColumn(i));
            }
        }
        return arrays;
    }

    /**
     * To convert table to map by column
     * @return
     */
    public Map<String,List> toMap(){
        Map<String,List> map = new HashMap<>();

        flip();
        while (hasRemaining()){
            R r = (R)get();
            f.forEach((k,v)->{
                if(!map.containsKey(k)){
                    map.put((String)k,new ArrayList<>());
                }
                map.get(k).add(r.getColumn(k));
            });
        }
        return map;
    }

    public Set<Object> getUniqueColumn(String fieldName) {
        Set<Object> column = new HashSet<>();
        flip();
        while (hasRemaining()) {
            column.add(((R) get()).getNumber(fieldName));
        }
        return column;
    }

    /**
     * To get a list of column number from table by options
     *
     * @param options options
     * @return list column number
     */
    public List<Object> getNumbers(Set<Option> options) {
        List<Object> column = new ArrayList<>();

        flip();
        while (hasRemaining()) {
            R<S, Object> r = (R<S, Object>) get();
            boolean res = true;
            for (Option<S> option : options) {
                Comparable left = (Comparable) r.getNumber(option.getFieldName());
                res = res && match(option, left);
            }
            if (res) {
                column.add(r);
            }
        }
        return column;
    }


    @Override
    public String toString() {
        return "T{" +
                "f=" + f +
                ", rows=\n" + Arrays.toString(rows) +
                '}';
    }

    public static void main(String[] args) {

        R<String, Object> row2 = new R<>();
        row2.setColumn("id", 10, Integer.class);
        row2.setColumn("username", "dog", String.class);


        T<String> table2 = new T<String>(2);
        table2.setF(row2.getF());
        table2.put(row2);
        table2.put(row2);
        if (table2.position() < table2.capacity())
            table2.put(row2);

        System.out.println(table2);
    }


    private String toJSON(){
//// TODO: 2/14/17 toJSON 
        return "";
    }
}
