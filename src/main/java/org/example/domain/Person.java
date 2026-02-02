package org.example.domain;

import java.time.format.DateTimeFormatter;
import java.util.Date;

public class Person extends User{
    private String firstName;
    private String secondName;
    private Date dateOfBirth;
    private String occupation;

    private int empathyNivel;

    public Person (String username, String email, String password, String firstName,
                   String secondName, Date dateOfBirth, String occupation, int empathyNivel){
        super(username, email, password);
        this.firstName = firstName;
        this.secondName = secondName;
        this.dateOfBirth = dateOfBirth;
        this.occupation = occupation;
        this.empathyNivel = empathyNivel;
    }



    public String getFirstName() {
        return firstName;
    }

    public String getSecondName() {
        return secondName;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public String getOccupation() {
        return occupation;
    }

    public int getEmpathyNivel() {
        return empathyNivel;
    }



    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd yyyy");


        String dataNasterii = new java.sql.Date(getDateOfBirth().getTime())
                .toLocalDate()
                .format(formatter);
        return "Person{" +
                "firstName='" + firstName + '\'' +
                ", secondName='" + secondName + '\'' +
                ", dateOfBirth=" + dataNasterii +
                ", occupation='" + occupation + '\'' +
                ", empathyNivel=" + empathyNivel +
                '}';
    }
}
