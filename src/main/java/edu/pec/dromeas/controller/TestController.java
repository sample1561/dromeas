package edu.pec.dromeas.controller;

import edu.pec.dromeas.service.TestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController
{
    final TestService testService;

    public TestController(TestService testService)
    {
        this.testService = testService;
    }

    @GetMapping("/stat")
    public ResponseEntity<?> systemStat()
    {
        return testService.systemStat();
    }

    @GetMapping({"","/"})
    public ResponseEntity<?> systemTest()
    {
        return testService.systemTest();
    }

    @GetMapping({"support","lang","language","languages"})
    public ResponseEntity<?> seeSupportedLanguages()
    {
        return testService.testLanguages();
    }

    @GetMapping("/all")
    public ResponseEntity<?> testAllLanguages()
    {
        return ResponseEntity.ok(testService.testAllCodes());
    }

    @GetMapping("/c")
    public ResponseEntity<?> testCCodes()
    {
        return ResponseEntity.ok(testService.testCCode());
    }

    @GetMapping("/cpp")
    public ResponseEntity<?> testCppCodes()
    {
        return ResponseEntity.ok(testService.testCppCode());
    }

    @GetMapping({"/js","/javascript"})
    public ResponseEntity<?> testJsCodes()
    {
        return ResponseEntity.ok(testService.testJsCode());
    }

    @GetMapping({"/php"})
    public ResponseEntity<?> testPhpCodes()
    {
        return ResponseEntity.ok(testService.testPhpCode());
    }
}
