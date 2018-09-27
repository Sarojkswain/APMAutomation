package com.ca.apm.systemtest.fld.agent;

import java.lang.reflect.Method;

public class TestIsa {

    public static void main(String[] args) throws Exception {
        Class<?> c1 = Object.class;
        Class<?> c2 = Double.class;

        System.out.println(c1.isAssignableFrom(c2));
        System.out.println(c2.isAssignableFrom(c1));

        System.out.println(Integer.class.getName() + " : " + Integer.TYPE.getName());
        System.out.println(Integer.class.equals(Integer.TYPE));

        TestIsa it = new TestIsa();
        Method m = it.getClass().getMethod("fooMe", Integer.TYPE);
        Object result = m.invoke(it, 99);
        System.out.println("result: " + result);
    }

    public void fooMe(int id) {
        if (id == 100) {
            throw new IllegalArgumentException("100 is a bad number");
        }
        System.out.println("You wrote " + id);
    }
}
