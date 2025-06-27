package lock.prac.Lock_Practice.global.apiPayload;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestLoginController {

    @GetMapping("/login/success")
    @ResponseBody
    public String loginSuccess() {
        return "로그인 성공!";
    }
}
