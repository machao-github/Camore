package com.machao.camore.app.computation_core.im_data;


import java.lang.reflect.Field;

public class PrimFieldToStr {
    public static final String LOG_TAG = "PrimFieldToString";
    public static String toString(Object obj){
        StringBuilder builder = new StringBuilder();
        builder.append(obj.getClass().getName() + "\n");

        Class objClass = obj.getClass();
        Field[] fields = objClass.getDeclaredFields();
        for (Field field :fields) {

            field.setAccessible(true);
            Class fieldClass = field.getType();
            if (!fieldClass.isPrimitive()) {
                continue;
            }

            builder.append(field.getName());
            builder.append("=");

            String fieldTypeName = fieldClass.getName();
            try {
                if (fieldTypeName.equals(int.class.getName())) {
                    builder.append(field.getInt(obj));
                } else if (fieldTypeName.equals(double.class.getName())) {
                    builder.append(field.getDouble(obj));
                } else if (fieldTypeName.equals(long.class.getName())) {
                    builder.append(field.getLong(obj));
                } else if (fieldTypeName.equals(float.class.getName())) {
                    builder.append(field.getFloat(obj));
                }
            } catch (IllegalAccessException e) {
                builder.append(e.getMessage());
            }

            builder.append(" ");
        }
        return  builder.toString();
    }


    static public class dummyOjb{
        int inta = 1;
        long longb = 2l;
        float floatc = 3.2f;
        double doubled = 3.3d;
    }

    public static void main(String[] args){
        dummyOjb obj = new dummyOjb();
        String str  = PrimFieldToStr.toString(obj);
        System.out.print(str);
    }
}
