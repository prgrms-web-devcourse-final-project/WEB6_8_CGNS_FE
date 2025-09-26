CREATE TABLE IF NOT EXISTS SPRING_AI_CHAT_MEMORY (
    id VARCHAR(255) PRIMARY KEY,
    conversation_id VARCHAR(255) NOT NULL,
    message_type VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    metadata TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_spring_ai_chat_memory_conversation_id ON SPRING_AI_CHAT_MEMORY(conversation_id);
CREATE INDEX IF NOT EXISTS idx_spring_ai_chat_memory_created_at ON SPRING_AI_CHAT_MEMORY(created_at);