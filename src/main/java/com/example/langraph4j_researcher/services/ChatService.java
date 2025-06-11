package com.example.langraph4j_researcher.services;

import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final AgentBuilderService agentBuilderService;

    public String research(String userQuery) throws GraphStateException {

        CompiledGraph<State> agent = agentBuilderService.build();

        Optional<State> response = agent.invoke(Map.of(State.USER_QUERY, userQuery));

        if (response.isEmpty()) {
            return "LLM does not have enough information to answer your question. Try asking a different question.";
        }

        State state = response.get();
        return state.getStringValueFromMap(State.FINAL_ANSWER);

    }
}
