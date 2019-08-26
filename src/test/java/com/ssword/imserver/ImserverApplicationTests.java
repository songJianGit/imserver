package com.ssword.imserver;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ImserverApplicationTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Test
    public void contextLoads() {
        String name = jdbcTemplate.queryForObject("select name from t_iim_userinfo where id = '1'", String.class);
        System.out.println(name);
    }

}
