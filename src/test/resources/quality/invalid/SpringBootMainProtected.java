package quality.invalid;

@SpringBootApplication
public class SpringBootMainProtected {

    protected static void main(String[] args) {
        SpringApplication.run(SpringBootMainProtected.class, args);
    }
}
