package org.acme;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

@Entity
public class MyEntity extends PanacheEntity {
    public String field;

    public MyEntity() {
    }

    public MyEntity(String field) {
        this.field = field;
    }
}
