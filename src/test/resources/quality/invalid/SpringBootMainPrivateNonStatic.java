package quality.invalid;

@SpringBootApplication
public class SpringBootMainPrivateNonStatic {

    private void main(String[] args) {
        SpringApplication.run(SpringBootMainPrivateNonStatic.class, args);
    }
}
