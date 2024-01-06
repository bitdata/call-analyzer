package bitdata.code;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude= {DataSourceAutoConfiguration.class})
public class CallAnalyzerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CallAnalyzerApplication.class, args);
    }

}
