package edu.pec.dromeas.controller;

import edu.pec.dromeas.payload.Code;
import edu.pec.dromeas.service.ExecuteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/run")


public class ExecuteController
{
    private ExecuteService executeService;

    public ExecuteController(ExecuteService executeService)
    {
        this.executeService = executeService;
    }

    @PostMapping({"/python2","/py2"})
    public ResponseEntity<?> runPython2Code(@RequestBody @Valid Code code)
    {
        return ResponseEntity.status(HttpStatus.OK).body(executeService.runPython2(code));
    }

    @PostMapping({"/python3","/py3"})
    public ResponseEntity<?> runPython3Code(@RequestBody @Valid Code code)
    {
        return ResponseEntity.status(HttpStatus.OK).body(executeService.runPython3(code));
    }

    @PostMapping({"/c"})
    public ResponseEntity<?> runCCode(@RequestBody @Valid Code code)
    {
        return ResponseEntity.status(HttpStatus.OK).body(executeService.runC(code));
    }

    @PostMapping({"/cpp"})
    public ResponseEntity<?> runCppCode(@RequestBody @Valid Code code)
    {
        return ResponseEntity.status(HttpStatus.OK).body(executeService.runCpp(code));
    }

    @PostMapping({"/javascript","js"})
    public ResponseEntity<?> runJavascriptCode(@RequestBody @Valid Code code)
    {
        return ResponseEntity.status(HttpStatus.OK).body(executeService.runJavaScript(code));
    }

    //TODO Java
    @PostMapping({"/java"})
    public ResponseEntity<?> runJavaCode(@RequestBody @Valid Code code)
    {
        return ResponseEntity.status(HttpStatus.OK).body(executeService.runJava(code));
    }

    @PostMapping({"/php"})
    public ResponseEntity<?> runPhpCode(@RequestBody @Valid Code code)
    {
        return ResponseEntity.status(HttpStatus.OK).body(executeService.runPhp(code));
    }

    //TODO Scala
    @PostMapping({"/scala"})
    public ResponseEntity<?> runScalaCode(@RequestBody @Valid Code code)
    {
        return executeService.runScala(code);
    }

    //TODO Go
    @PostMapping({"/go"})
    public ResponseEntity<?> runGoCode(@RequestBody @Valid Code code)
    {
        return executeService.runGo(code);
    }

    //TODO Rust
    @PostMapping({"/rust"})
    public ResponseEntity<?> runRustCode(@RequestBody @Valid Code code)
    {
        return executeService.runRust(code);
    }

    //TODO Kotlin
    @PostMapping({"/kotlin"})
    public ResponseEntity<?> runKotlinCode(@RequestBody @Valid Code code)
    {
        return executeService.runKotlin(code);
    }

    @PostMapping({"/ruby","/rb"})
    public ResponseEntity<?> runRubyCode(@RequestBody @Valid Code code)
    {
        return ResponseEntity.status(HttpStatus.OK).body(executeService.runRuby(code));
    }

    //TODO Swift
    @PostMapping({"/swift"})
    public ResponseEntity<?> runSwiftCode(@RequestBody @Valid Code code)
    {
        return executeService.runSwift(code);
    }

    //TODO C#
    @PostMapping({"/cs"})
    public ResponseEntity<?> runCsCode(@RequestBody @Valid Code code)
    {
        return executeService.runCS(code);
    }
}

//TODO leetcode also inserts escape sequences to account for JSON rule
// - see how to add escape sequences
