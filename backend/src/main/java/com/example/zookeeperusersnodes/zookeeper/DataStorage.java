package com.example.zookeeperusersnodes.zookeeper;

import com.example.zookeeperusersnodes.models.Person;

import java.util.ArrayList;
import java.util.List;

public final class DataStorage {
    private static List<Person> personList = new ArrayList<>();

    public static List<Person> getPersonList() {
        return personList;
    }

    public static void addPersonToList(Person person) {
        personList.add(person);
    }

    private DataStorage() {}
}
