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
}

// TODO leetcode also inserts escape sequences to account for JSON rule
// - see how to add escape sequences
