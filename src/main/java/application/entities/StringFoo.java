package application.entities;

import javax.persistence.*;

@Entity
@Table(name = "string_foo")
public class StringFoo {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column
    private String value;

    public StringFoo() {}

    public StringFoo(String value) {
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "StringFoo{" +
                "id=" + id +
                ", value='" + value + '\'' +
                '}';
    }
}
