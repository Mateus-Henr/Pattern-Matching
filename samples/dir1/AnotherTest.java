@RestController
public class ExampleController {
    @GetMapping("/hello")
    public String hello() {
        return "Hello, world!";
    }

    @GetMapping("/hello")
    protected String hello() {
        return "Hello, world!";
    }
}