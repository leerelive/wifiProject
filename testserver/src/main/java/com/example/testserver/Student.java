package com.example.testserver;

/**
 * Created by 李景晨 on 2015/9/25.
 */
public class Student {
    private String name;

    private String age;

    private String id;



    public Student() {

        super();

    }



    public Student(String name, String age, String id) {

        super();

        this.name = name;

        this.age = age;

        this.id = id;

    }



    public String getName() {

        return name;

    }



    public void setName(String name) {

        this.name = name;

    }



    public String getAge() {

        return age;

    }



    public void setAge(String age) {

        this.age = age;

    }



    public String getId() {

        return id;

    }



    public void setId(String id) {

        this.id = id;

    }


}
