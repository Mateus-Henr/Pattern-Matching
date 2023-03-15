@RestController/*  */
public class ExampleController {
    @GetMapping("/hello")
    private String hello() {
        return "Hello, world!";
    }
}