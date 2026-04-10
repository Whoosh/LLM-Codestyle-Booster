package quality.invalid;

@SpringBootApplication
public class SpringBootMainNotStatic {

    public void main(String[] args) {
        SpringApplication.run(SpringBootMainNotStatic.class, args);
    }
}
