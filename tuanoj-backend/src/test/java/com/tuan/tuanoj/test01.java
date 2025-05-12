package com.tuan.tuanoj;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class test01 {
    @Test
    public void test01() throws ClassNotFoundException {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://47.94.249.3:3306/tuanoj", "root", "123456");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
