## Example 

**Without queue:**

```java

M model = new M(from); //from:Connection
T table = new T(1000); //init size
model.read(
    "select name,sum(socre) as total from test where score>10 group by name "
,table);


T newTable  = new T(table.position());
table.flip();//flip
while (table.hasRemaining()){
    R<String,Object> row = (R<String, Object>) table.get();
    //deal with row
    ...
    newTable.put(row);
}

model.write(newTable,"result",H.make("name"),H.make(),to);//newTable:T;table name;primary key;fields;to:Connection

```

**With queue:**

```java

M model = new M();
T table = null; //init size
Q q = new Q();//queue
model.read(
    "select name,sum(socre) as total from test where score>10 group by name "
,q,from); //from:Connection


while ((table=q.fetch(3000,model)).size()!=0){
    T newTable  = new T(table.position());
    table.flip();//flip
    while (table.hasRemaining()){
        R<String,Object> row = (R<String, Object>) table.get();
        //deal with row
        ...
        newTable.put(row);
        
    }
    model.write(newTable,"result",H.make("name"),H.make(),to);//newTable:T;table name;primary key;fields;to:Connection
}


```


**Deal with rows:**

```java
model.read(
    "select id,url  from user_data "
,table);

T newTable  = new T(table.position());
while (table.hasRemaining()){
    R<String,Object> row = (R<String, Object>) table.get();

    R<String,Object> newRow = new R<>();
    nowRow.setColumn(row)
            .setColumn("protocol",protocol,String.class)
            .setColumn("path",path,String.class)
            .setColumn("host",host,String.class)
            .setColumn("query",query,String.class)
            .setColumn("ref",ref,String.class)
            .removeColumn("url");
    newTable.put(row);
}
model.write(newTable,"result",H.make("id"),H.make("id","protocol","path","host","query","ref"),to);//newTable:T;table name;primary key;fields;to:Connection
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