package edu.pec.dromeas.controller;

import edu.pec.dromeas.payload.Code;
import edu.pec.dromeas.service.ExecuteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/run")
public class ExecuteController {
  private ExecuteService executeService;

  public ExecuteController(ExecuteService executeService) {
    this.executeService = executeService;
  }

  @PostMapping({"/c"})
  public ResponseEntity<?> runCCode(@RequestBody @Valid Code code) {
    return ResponseEntity.status(HttpStatus.OK).body(executeService.runC(code));
  }

  @PostMapping({"/cpp"})
  public ResponseEntity<?> runCppCode(@RequestBody @Valid Code code) {
    return ResponseEntity.status(HttpStatus.OK).body(executeService.runCpp(code));
  }

  // TODO Java
  @PostMapping({"/java"})
  public ResponseEntity<?> runJavaCode(@RequestBody @Valid Code code) {
    return ResponseEntity.status(HttpStatus.OK).body(executeService.runJava(code));
  }

  // TODO Scala
  @PostMapping({"/scala"})
  public ResponseEntity<?> runScalaCode(@RequestBody @Valid Code code) {
    return executeService.runScala(code);
  }

  // TODO Go
  @PostMapping({"/go"})
  public ResponseEntity<?> runGoCode(@RequestBody @Valid Code code) {
    return executeService.runGo(code);
  }

  // TODO Rust
  @PostMapping({"/rust"})
  public ResponseEntity<?> runRustCode(@RequestBody @Valid Code code) {
    return executeService.runRust(code);
  }

  // TODO Kotlin
  @PostMapping({"/kotlin"})
  public ResponseEntity<?> runKotlinCode(@RequestBody @Valid Code code) {
    return executeService.runKotlin(code);
  }

  // TODO Swift
  @PostMapping({"/swift"})
  public ResponseEntity<?> runSwiftCode(@RequestBody @Valid Code code) {
    return executeService.runSwift(code);
  }

  // TODO C#
  @PostMapping({"/cs"})
  public ResponseEntity<?> runCsCode(@RequestBody @Valid Code code) {
    return executeService.runCS(code);
  }
}

// TODO leetcode also inserts escape sequences to account for JSON rule
// - see how to add escape sequences
