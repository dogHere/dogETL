package com.github.doghere.csv;

import com.github.doghere.Field;
import com.github.doghere.Reader;
import com.github.doghere.Row;
import com.github.doghere.util.StringType;
import com.lmax.disruptor.RingBuffer;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import static com.github.doghere.util.StringType.*;

/**
 * Created by dog on 4/13/17.
 */
public class CSVReader implements Reader<Row> {

    private RingBuffer<Row> ringBuffer;
    private Field field;
    private java.io.BufferedReader ioReader;
    private boolean hasRemaining = true;
    private boolean hasHeader = false;
    private static String format = "yyyy-MM-dd hh:mm:ss.SSS";
    private String nullMapper;
    private boolean resetNullMapper = false;

    private CSVFormat csvFormat = CSVFormat.MYSQL;
    private CSVParser csvParser;

    private Iterator <CSVRecord> iterator ;

    @Override
    public void read() throws Exception {

        long sequence = ringBuffer.next();  // Grab the next sequence
        try {
            Row row = ringBuffer.get(sequence); // Get the entry in the Disruptor
//            row.setUseStrict(false);//not use strict
            if(!row.isCanWrite()) row.setCanWrite(true);
            if(row.getField()==null) {
                row.setField((Field)field.clone());
            }
            // for the sequence
            setRow(row);
        }catch (Exception e){
            e.printStackTrace();
            throw new Exception(e);
        } finally {
            if(hasRemaining) ringBuffer.publish(sequence);
        }
    }

    private void setField() throws IOException {
        if(field!=null)  return;
        if(hasHeader) return;

        field = new Field();
        Map <String,Integer>map = csvParser.getHeaderMap();
        map.forEach((k,v)->{
            field.setType(k,String.class);
        });

    }

    private boolean setRow(Row row) throws IOException {

        if (row == null)
            throw new RuntimeException("row is null!");

        if (iterator.hasNext()) {
            CSVRecord csvRecord = iterator.next();


            int i[] = new int[]{0};
            field.forEach((k, v) -> {
                Class c = (Class) field.getType(k);
                String s = csvRecord.get(i[0]);
                if (c.equals(String.class)) row.setColumn(k, string2String(s), c);
                else if (c.equals(Byte.class)) row.setColumn(k, string2Byte(s), c);
                else if (c.equals(Short.class)) row.setColumn(k, string2Short(s), c);
                else if (c.equals(Integer.class)) row.setColumn(k, string2Integer(s), c);
                else if (c.equals(Long.class)) row.setColumn(k, string2Long(s), c);
                else if (c.equals(BigInteger.class)) row.setColumn(k, string2BigInteger(s), c);
                else if (c.equals(BigDecimal.class)) row.setColumn(k, string2BigDecimal(s), c);
                else if (c.equals(Time.class)) row.setColumn(k, string2Time(s, format), c);
                else if (c.equals(Date.class)) row.setColumn(k, string2Date(s, format), c);
                else if (c.equals(Timestamp.class)) row.setColumn(k, string2Timestamp(s, format), c);
                else if (c.equals(Double.class)) row.setColumn(k, string2Double(s), c);
                i[0] += 1;

            });
            return true;
        } else {
            hasRemaining = false;
            return false;
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
    public CSVReader setField(Field field) {
        this.field = field;
        hasHeader = true;
        skipLine();
        return this;
    }

    private String skipLine(){
        String s = null;
        try {
            s= ioReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s;
    }

    @Override
    public void before() throws Exception {

        this.csvParser = this.csvFormat.parse(this.ioReader);
        iterator = csvParser.iterator();


        if(nullMapper!=null){
            if(nullMapper.equals("")) {
                if(resetNullMapper) {
                    StringType.set = new HashSet<>();
                }
                set.add("");
            }else {
                String[] ns = nullMapper.split(",");
                if (ns.length > 0) {
                    if(resetNullMapper) {
                        StringType.set = new HashSet<>();
                    }
                    for (String n : ns) {
                        StringType.set.add(n);
                    }
                }
                if(nullMapper.endsWith(",")) StringType.set.add("");
            }
        }
        setField();
    }

    @Override
    public void after() throws Exception {
        csvParser.close();
    }

    public java.io.Reader getIoReader() {
        return ioReader;
    }

    public CSVReader setIoReader(java.io.Reader ioReader) {
        this.ioReader = new BufferedReader( ioReader);
        return this;
    }

    public boolean isHasHeader() {
        return hasHeader;
    }

    public CSVReader setHasHeader(boolean hasHeader) {
        this.hasHeader = hasHeader;
        return this;
    }

    public CSVReader setHeaders(String line){
        if(!line.equals("")){
            String[]heads = line.split(",");
            for(String head:heads){
                this.field.setType(head,String.class);
            }
            hasHeader = true;
        }
        return this;
    }

    public CSVReader setHeaders(Field field){
        this.field = field;
        hasHeader = true;
        return this;
    }

    public static String getFormat() {
        return format;
    }

    public static void setFormat(String format) {
        CSVReader.format = format;
    }

    public String getNullMapper() {
        return nullMapper;
    }

    public CSVReader setNullMapper(String nullMapper) {
        this.nullMapper = nullMapper;
        return this;
    }

    public CSVFormat getCsvFormat() {
        return csvFormat;
    }

    public CSVReader setCsvFormat(CSVFormat csvFormat) {
        this.csvFormat = csvFormat;
        return this;
    }

    public boolean isResetNullMapper() {
        return resetNullMapper;
    }

    public CSVReader setResetNullMapper(boolean resetNullMapper) {
        this.resetNullMapper = resetNullMapper;
        return this;
    }
}
