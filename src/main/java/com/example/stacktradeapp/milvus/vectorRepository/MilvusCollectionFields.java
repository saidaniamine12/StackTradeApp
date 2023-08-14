package com.example.stacktradeapp.milvus.vectorRepository;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MilvusCollectionFields {

    String collectionName;
    String idFieldName;
    String vectorFieldName;

}
