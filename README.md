


## Example 

### Start a job

```java
Model model = new Model() 
        .setReader(reader)
        .setTransformer(transformer)
        .setWriter(writers) 

model.start(); 
```


### Reader

```java

//jdbc reader

Reader reader = new JDBCReader()
                .setConnection(connection) // jdbc connection
                .setSQL("select uid,count(*) as pv from logs group by uid"); // any sql

```
Support readers : jdbc,csv,json

### Transformer

```java
Transformer transformer = new Transformer<Row>() {
    @Override
    public void onEvent(Row event, long sequence, boolean endOfBatch) throws Exception {
        //deal each row here
    }
};
```


### Writer

```java

// jdbc multi-writer

Writer [] writers = new JDBCWriter()
        .setTarget("rpt_pv")
        .setPrimaryKeys("uid")
        .setWriterSize(POOL_SIZE)
        .setDataSource(dataSource)
        .create();

```

Support writers : jdbc,csv

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