


## Example 

### Start a job

```java
Model model = new Model() 
        .setReader(reader)  
        .setWriter(writers) 

model.start(); 

//todo : to support multiple readers
```

### Reader

```java

//jdbc reader

Reader reader = new JDBCReader()
                .setConnection(connection) // jdbc connection
                .setSQL("select uid,count(*) as pv from logs group by uid"); // any sql

// or csv reader

Reader reader = new CSVReader()
        .setIoReader(new FileReader(new File("~/logs.csv")))
        .setField(new Field()
            .setType("uid",Integer.class)
            .setType("eid",String.class)
            .setType("create_date",Timestamp.class)
        );
        
// or any reader 

// todo : to support more reader .
```

### Writer

```java

// jdbc multi-writer

Writer [] writers = new JDBCWriter() {
        @Override
        public void dealEach(Row row)throws Exception {
            // map row here
        }
    }
        .setTarget("rpt_pv")
        .setPrimaryKeys("uid")
        .setWriterSize(POOL_SIZE)
        .setDataSource(dataSource)
        .create();

//csv writer


Writer[] writers = new Writer[]{
        new CSVWriter() {
            @Override
            public void dealEach(Row row) throws Exception {
                // map row here
            }
        }.setIoWriter(new FileWriter(new File("~/rpt_pv.csv")))
};



// or any reader 

// todo : to support more reader .

```

## License 

```
Copyright 2016-2017 dogHere

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```