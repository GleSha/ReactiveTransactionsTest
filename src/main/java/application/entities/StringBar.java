package application.entities;

import javax.persistence.*;

@Entity
@Table(name = "string_bar")
public class StringBar {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column
    private String value;

    public StringBar() {}

    public StringBar(String value) {
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
        return "StringBar{" +
                "id=" + id +
                ", value='" + value + '\'' +
                '}';
    }
}
