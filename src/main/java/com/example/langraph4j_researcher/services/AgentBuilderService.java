package com.example.langraph4j_researcher.services;

import com.example.langraph4j_researcher.utils.ResponseUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.AsyncEdgeAction;
import org.bsc.langgraph4j.action.EdgeAction;
import org.springframework.stereotype.Service;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * Service responsible for building and configuring the LangGraph agent.
 * This service defines the workflow for research-based topic exploration
 * using a directed graph approach with multiple specialized nodes.
 */
@Service
@RequiredArgsConstructor
public class AgentBuilderService {

    // Maximum number of initial research queries to generate
    private static final int MAX_QUERY_COUNT = 2;

    // Maximum number of research cycles before forcing termination
    private static final int MAX_RESEARCH_NODE_EXECUTION_COUNT = 3;

    private final OllamaService ollamaService;

    /**
     * Builds and compiles the research agent graph structure.
     * The graph defines the workflow of the research process:
     * 1. Generate initial queries
     * 2. Conduct research for each query
     * 3. Reflect on results and determine if more research is needed
     * 4. Either continue with follow-up queries or generate final answer
     */
    public CompiledGraph<State> build() throws GraphStateException {

        // Define the conditional edge function that determines whether to continue research or finish
        var shouldContinue = (EdgeAction<State>) state -> {

            int researchNodeExecutionCount = state.getIntegerValueFromMap(State.RESEARCH_NODE_EXECUTION_COUNT);
            boolean isSufficient = state.getBoolValueFromMap(State.IS_SUFFICIENT);

            if (isSufficient || researchNodeExecutionCount >= MAX_RESEARCH_NODE_EXECUTION_COUNT) {
                return "end";
            }
            return "searchMore";
        };

        StateGraph<State> stateGraph = new StateGraph<>(State.SCHEMA, State::new)
            .addNode("generateQueryNode", node_async(AgentBuilderService.this::executeGenerateQueryNode))
            .addNode("researchNode", node_async(AgentBuilderService.this::executeResearchNode))
            .addNode("reflectionNode", node_async(AgentBuilderService.this::executeReflectionNode))
            .addNode("finalAnswerNode", node_async(AgentBuilderService.this::executeFinalAnswerNode))
            .addEdge(StateGraph.START, "generateQueryNode")
            .addEdge("generateQueryNode", "researchNode")
            .addEdge("researchNode", "reflectionNode")
            .addConditionalEdges("reflectionNode", AsyncEdgeAction.edge_async(shouldContinue),
                Map.of("end", "finalAnswerNode",
                    "searchMore", "researchNode"))
            .addEdge("finalAnswerNode", StateGraph.END);

        return stateGraph.compile();
    }

    /**
     * Generates initial research queries based on the user's input.
     * This is the first step in the research process.
     */
    private Map<String, Object> executeGenerateQueryNode(State state) {

        String userPrompt = state.getStringValueFromMap(State.USER_QUERY);
        String systemPrompt = SystemPrompts.GENERATE_QUERY_PROMPT;

        String llmPrompt = String.format(systemPrompt, MAX_QUERY_COUNT, userPrompt);

        String llmResponseStr = ollamaService.getLLMResponse(llmPrompt);
        Map<String, Object> llmResponse = ResponseUtils.extractJsonFromLLMResponse(llmResponseStr);

        List<String> researchQueries = (List<String>) llmResponse.get("query");

        return Map.of(State.RESEARCH_QUERIES, researchQueries);
    }

    /**
     * Executes research for each query (original or follow-up).
     * This node is responsible for gathering information based on the queries.
     */
    private Map<String, Object> executeResearchNode(State state) {

        List<String> researchQueries = state.getListOfStringValueFromMap(State.RESEARCH_QUERIES);
        List<String> followUpQueries = state.getListOfStringValueFromMap(State.FOLLOW_UP_QUERIES);
        List<String> researchQueryResults = state.getListOfStringValueFromMap(State.RESEARCH_QUERIES_RESULT);

        // Process either follow-up queries (if available) or original queries
        if (followUpQueries != null && !followUpQueries.isEmpty()) {
            // Process follow-up queries from reflection node

            List<String> followUpQueryResults = getResearchQueryResults(followUpQueries);
            researchQueryResults.addAll(followUpQueryResults);

        } else {
            // Process original queries from generate query node
            List<String> originalQueryResults = getResearchQueryResults(researchQueries);
            researchQueryResults.addAll(originalQueryResults);
        }

        // Increment the execution counter to track iteration count
        int researchNodeExecutionCount = state.getIntegerValueFromMap(State.RESEARCH_NODE_EXECUTION_COUNT);
        researchNodeExecutionCount++;


        return Map.of(State.RESEARCH_QUERIES_RESULT, researchQueryResults,
            State.RESEARCH_NODE_EXECUTION_COUNT, researchNodeExecutionCount);
    }

    private List<String> getResearchQueryResults(List<String> researchQueries) {

        String systemPrompt = SystemPrompts.RESEARCH_PROMPT;

        List<String> researchQueryResults = new ArrayList<>();

        for (String researchQuery : researchQueries) {

            String llmPrompt = String.format(systemPrompt, researchQuery, researchQuery);
            String llmResponseStr = ollamaService.getLLMResponse(llmPrompt);
            Map<String, Object> llmResponse = ResponseUtils.extractJsonFromLLMResponse(llmResponseStr);
            String summary = (String) llmResponse.get("summary");
            researchQueryResults.add(summary);
        }
        return researchQueryResults;
    }

    /**
     * Evaluates the research results to determine if they sufficiently answer the user's query.
     * If not, generates follow-up queries to fill knowledge gaps.
     */
    private Map<String, Object> executeReflectionNode(State state) {

        List<String> researchQueryResults = state.getListOfStringValueFromMap(State.RESEARCH_QUERIES_RESULT);
        String userPrompt = state.getStringValueFromMap(State.USER_QUERY);
        String systemPrompt = SystemPrompts.REFLECTION_PROMPT;

        String llmPrompt = String.format(systemPrompt, userPrompt, researchQueryResults.stream().collect(
            Collectors.joining("\n\n-----\n\n")));

        String llmResponseStr = ollamaService.getLLMResponse(llmPrompt);
        Map<String, Object> llmResponse = ResponseUtils.extractJsonFromLLMResponse(llmResponseStr);

        Boolean isSufficient = ((Boolean) llmResponse.get("isSufficient"));
        List<String> followUpQueries = ((List<String>) llmResponse.get("followUpQueries"));

        return Map.of(
            State.IS_SUFFICIENT, isSufficient,
            State.FOLLOW_UP_QUERIES, followUpQueries);
    }

    /**
     * Generates the final answer by synthesizing all research results.
     * This is the last step in the research process.
     */
    private Map<String, Object> executeFinalAnswerNode(State state) {

        String userPrompt = state.getStringValueFromMap(State.USER_QUERY);
        List<String> researchQueryResults = state.getListOfStringValueFromMap(State.RESEARCH_QUERIES_RESULT);
        String systemPrompt = SystemPrompts.FINAL_ANSWER_PROMPT;

        String llmPrompt = String.format(systemPrompt, userPrompt, researchQueryResults.stream().collect(
            Collectors.joining("\n\n-----\n\n")));

        String llmResponseStr = ollamaService.getLLMResponse(llmPrompt);

        Map<String, Object> llmResponse = ResponseUtils.extractJsonFromLLMResponse(llmResponseStr);

        String synthesisedResponse = ((String) llmResponse.get("synthesisedResponse"));

        return Map.of(State.FINAL_ANSWER, synthesisedResponse);
    }
}
