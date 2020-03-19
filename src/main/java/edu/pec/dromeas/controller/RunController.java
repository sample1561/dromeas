package edu.pec.dromeas.controller;

import edu.pec.dromeas.payload.InputCode;
import edu.pec.dromeas.service.RunService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/run")

public class RunController
{
    private RunService runService;

    public RunController(RunService runService)
    {
        this.runService = runService;
    }

    @PostMapping({"/python3","/py3"})
    public ResponseEntity<?> runPython3Code(@RequestBody @Valid InputCode code)
    {
        return runService.runPython3(code);
    }

    //TODO Python2
    @PostMapping({"python2","/py2"})
    public ResponseEntity<?> runPython2Code(@RequestBody @Valid InputCode code)
    {
        return runService.runPython2(code);
    }

    @PostMapping({"/c"})
    public ResponseEntity<?> runCCode(@RequestBody @Valid InputCode code)
    {
        return runService.runC(code);
    }

    //TODO CPP
    @PostMapping({"/cpp"})
    public ResponseEntity<?> runCppCode(@RequestBody @Valid InputCode code)
    {
        return runService.runCpp(code);
    }

    //TODO JavaScript
    @PostMapping({"/javascript","js"})
    public ResponseEntity<?> runJavascriptCode(@RequestBody @Valid InputCode code)
    {
        return runService.runJavaScript(code);
    }

    //TODO Java
    @PostMapping({"/java"})
    public ResponseEntity<?> runJavaCode(@RequestBody @Valid InputCode code)
    {
        return runService.runJava(code);
    }

    //TODO PHP
    @PostMapping({"/php"})
    public ResponseEntity<?> runPhpCode(@RequestBody @Valid InputCode code)
    {
        return runService.runPhp(code);
    }

    //TODO Scala
    @PostMapping({"/scala"})
    public ResponseEntity<?> runScalaCode(@RequestBody @Valid InputCode code)
    {
        return runService.runScala(code);
    }

    //TODO Go
    @PostMapping({"/go"})
    public ResponseEntity<?> runGoCode(@RequestBody @Valid InputCode code)
    {
        return runService.runGo(code);
    }

    //TODO Rust
    @PostMapping({"/rust"})
    public ResponseEntity<?> runRustCode(@RequestBody @Valid InputCode code)
    {
        return runService.runRust(code);
    }

    //TODO Kotlin
    @PostMapping({"/kotlin"})
    public ResponseEntity<?> runKotlinCode(@RequestBody @Valid InputCode code)
    {
        return runService.runKotlin(code);
    }

    //TODO Ruby
    @PostMapping({"/ruby"})
    public ResponseEntity<?> runRubyCode(@RequestBody @Valid InputCode code)
    {
        return runService.runRuby(code);
    }

    //TODO Swift
    @PostMapping({"/swift"})
    public ResponseEntity<?> runSwiftCode(@RequestBody @Valid InputCode code)
    {
        return runService.runSwift(code);
    }

    //TODO C#
    @PostMapping({"/cs"})
    public ResponseEntity<?> runCsCode(@RequestBody @Valid InputCode code)
    {
        return runService.runCS(code);
    }
}
