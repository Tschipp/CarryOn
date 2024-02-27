/*
 * GNU Lesser General Public License v3
 * Copyright (C) 2024 Tschipp
 * mrtschipp@gmail.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package tschipp.carryon.config;

import java.lang.reflect.Field;

public record PropertyData(Object fieldClass, Field field, AnnotationData data) {

    public String getId() {
        return field().getName();
    }

    public boolean getBoolean() throws IllegalAccessException {
        return field().getBoolean(fieldClass());
    }

    public void setBoolean(boolean _boolean) {
        try {
            field.setBoolean(fieldClass, _boolean);
        } catch (Exception e) {
            //Ignore
        }
    }

    public int getInt() throws IllegalAccessException {
        return field().getInt(fieldClass());
    }

    public void setInt(int _int) {
        try {
            field.setInt(fieldClass, _int);
        } catch (Exception e) {
            //Ignore
        }
    }

    public double getDouble() throws IllegalAccessException {
        return field().getDouble(fieldClass());
    }

    public void setDouble(double _double) {
        try {
            field.setDouble(fieldClass, _double);
        } catch (Exception e) {
            //Ignore
        }
    }

    public String[] getStringArray() throws  IllegalAccessException {
        return (String[])field().get(fieldClass());
    }

    public void setStringArray(String[] arr)
    {
        try {
            field.set(fieldClass, arr);
        } catch (Exception e) {
            //Ignore
        }
    }
}
