@RestController
public class ExampleController {
    @GetMapping("/hello")
    public String hello() {
        return "Hello, world!";
    }

    private String hello() {
        return "Hello, world!";
    }
}