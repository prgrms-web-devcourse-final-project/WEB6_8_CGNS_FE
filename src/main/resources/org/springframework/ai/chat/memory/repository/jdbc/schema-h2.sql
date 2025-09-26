CREATE TABLE IF NOT EXISTS SPRING_AI_CHAT_MEMORY (
    id VARCHAR(255) DEFAULT RANDOM_UUID() PRIMARY KEY,
    conversation_id VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    content CLOB NOT NULL,
    metadata CLOB,
    "timestamp" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_spring_ai_chat_memory_conversation_id ON SPRING_AI_CHAT_MEMORY(conversation_id);
CREATE INDEX IF NOT EXISTS idx_spring_ai_chat_memory_timestamp ON SPRING_AI_CHAT_MEMORY("timestamp");