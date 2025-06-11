package com.example.langraph4j_researcher.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

public class State extends AgentState {

    public static final String USER_QUERY = "userQuery";
    public static final String LLM_RESPONSE = "llmResponse";
    public static final String IS_SUFFICIENT = "isSufficient";
    public static final String RESEARCH_QUERIES = "researchQueries";
    public static final String RESEARCH_QUERIES_RESULT = "researchQueriesResult";
    public static final String FOLLOW_UP_QUERIES = "followUpQueries";
    public static final String FINAL_ANSWER = "finalAnswer";
    public static final String RESEARCH_NODE_EXECUTION_COUNT = "researchNodeExecutionCount";

    public static final Map<String, Channel<?>> SCHEMA = Map.of(
        USER_QUERY, Channels.base(() -> ""),
        LLM_RESPONSE, Channels.base(() -> new ArrayList()),
        RESEARCH_QUERIES, Channels.base(() -> new ArrayList()),
        RESEARCH_QUERIES_RESULT, Channels.base(() -> new ArrayList()),
        FOLLOW_UP_QUERIES, Channels.base(() -> new ArrayList()),
        IS_SUFFICIENT, Channels.base(() -> false),
        FINAL_ANSWER, Channels.base(() -> ""),
        RESEARCH_NODE_EXECUTION_COUNT, Channels.base(() -> 0)
    );

    public State(Map<String, Object> initState) {
        super(initState);
    }

    public String getStringValueFromMap(String key) {
        Optional<String> valOpt = value(key);
        return valOpt.orElse(null);
    }

    public Integer getIntegerValueFromMap(String key) {
        Optional<Integer> valOpt = value(key);
        return valOpt.orElse(null);
    }

    public List<String> getListOfStringValueFromMap(String key) {
        Optional<List<String>> valOpt = value(key);
        return valOpt.orElse(new ArrayList<String>());
    }

    public Boolean getBoolValueFromMap(String key) {
        Optional<Boolean> valOpt = value(key);
        return valOpt.orElse(null);
    }

}
