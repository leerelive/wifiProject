package com.example.testserver;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

/**
 * @author frankiewei.
 * Json封装的工具类.
 */


        public class JsonUtils {

            public static List<Student> parseStudentFromJson(String data) {

                Type listType = new TypeToken<LinkedList<Student>>() {

                }.getType();

                Gson gson = new Gson();

                LinkedList<Student> list = gson.fromJson(data, listType);

                return list;

            }

        }

