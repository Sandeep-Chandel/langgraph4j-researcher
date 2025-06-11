package com.example.langraph4j_researcher.controllers;

import com.example.langraph4j_researcher.services.ChatService;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.GraphStateException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("query")
    @ResponseBody
    public String getUserQueryResponse(@RequestBody String query) throws GraphStateException {

        return chatService.research(query);
    }
}
