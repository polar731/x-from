package cn.polar.x;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Configurable
@MapperScan("cn.polar.x.infrastructure.dao")
public class Application {

    public static void main(String[] args){
        SpringApplication.run(Application.class);
    }

}
