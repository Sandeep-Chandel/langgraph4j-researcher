# LangGraph4j Based Topic Researcher
A topic research agent built with Spring AI, LangGraph4j, and Qwen3:1.7b LLM hosted locally on Ollama.
## Overview
This project implements an autonomous research assistant that can explore topics thoroughly and provide comprehensive summaries. The agent is designed to iteratively improve its research by identifying and filling knowledge gaps until it has gathered sufficient information to address the user's query comprehensively.

This is project is based on another python project https://github.com/google-gemini/gemini-fullstack-langgraph-quickstart

## Key Features
- **Automated Research Process**: Takes a user query and autonomously conducts in-depth research
- **Intelligent Query Generation**: Creates targeted research queries based on the initial topic
- **Iterative Improvement**: Evaluates research quality and identifies knowledge gaps
- **Comprehensive Summarization**: Synthesizes multiple research findings into coherent summaries
- **Locally Hosted LLM**: Uses Qwen3:1.7b running on Ollama for privacy and control

## Technology Stack
- **Spring Boot** (v3.5.0): Provides the application framework
- **Spring AI** (v1.0.0): Facilitates AI model integration and prompt engineering
- **LangGraph4j** (v1.5.14): Enables the creation of complex agent workflows with state management
- **Qwen3:1.7b**: Lightweight but capable Large Language Model
- **Ollama**: Local LLM hosting platform
- **Java 21**: Latest language features for efficient development

## How It Works
1. **Input Processing**: The agent receives a user query about a topic of interest
2. **Query Generation**: Based on the initial input, the agent generates multiple research queries to explore different aspects of the topic
3. **Data Collection**: For each research query, the agent uses the LLM to generate detailed information
4. **Evaluation**: The agent evaluates the collected information against the original query to identify gaps in research
5. **Gap Filling**: Additional targeted queries are generated to fill any identified knowledge gaps
6. **Iteration**: Steps 3-5 are repeated until sufficient information is gathered
7. **Summarization**: All collected research is synthesized into a comprehensive summary
8. **Output**: The final summary is presented to the user

## Getting Started
### Prerequisites
- Java 21 or higher
- Maven
- Ollama with Qwen3:1.7b model installed

### Installation
1. Clone the repository:
``` bash
   git clone https://github.com/yourusername/langgraph4j-researcher.git
   cd langgraph4j-researcher
```
2Install dependencies:
``` bash
   mvn install
```
3Ensure Ollama is running with the required model:
``` bash
   ollama pull qwen3:1.7b
   ollama serve
```
### Running the Application
Start the application using Maven:
``` bash
mvn spring-boot:run
```
The API will be available at `http://localhost:8077`
## Usage
Send a POST request to the API endpoint with your research query:
``` bash
curl -XGET 'http://localhost:8077/chat/query' --data "Why is the sky blue?"
```
The agent will process your request and return a comprehensive research summary.

This project is open source and available under the [MIT License](LICENSE).

_Note: This project is for educational and research purposes. Always ensure you comply with the terms of service for any third-party APIs or models used._
