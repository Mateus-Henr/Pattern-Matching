@RestController
public class ExampleController {
    @GetMapping("/hello")
    public String hello() {
        return "Hello, world!";
    }

    @GetMapping("/hello")
    private String hello() {
        return "Hello, world!";
    }
}