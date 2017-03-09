## Example 

```java

M model = new M(from); //from:Connection
T table = new T(1000); //init size
model.read(
    "select name,sum(socre) as total from test where score>10 group by name "
,table);


T newTable  = new T(table.position())
                .setF(table.getF());
table.flip();//flip
while (table.hasRemaining()){
    R<String,Object> row = (R<String, Object>) table.get();
    //deal with row
    ...
    newTable.put(row);
    
}

model.write(newTable,"result",H.make("name"),H.make(),to);//newTable:T;table name;primary key;fields;to:Connection

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