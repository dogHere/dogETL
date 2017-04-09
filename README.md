


## Example 

```java


// create single reader

Reader reader = new JDBCReader()
                .setConnection(dataSource.getConnection())
                .setSQL("select uid,count(distinct event_id) as pv from logs where app_id='dog_here' group by uid limit 2000000");


// create writers

Writer [] writers = new JDBCWriter() {
        @Override
        public void dealEach(Row row) {
            // deal with ecah row here
        }
    }.setTarget("rpt_pv_by_uid")//target table name
        .setPrimaryKeys("uid")//target table primary keys,split with `,`
        .setWriterSize(POOL_SIZE)
        .setDataSource(dataSource)
        .create();

// transform

new Model() //new model
        .setReader(reader) 
        .setWriter(writers) 
        .setFactory(new RowFactory()) //set row factory to create row instances
        .start(); 
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