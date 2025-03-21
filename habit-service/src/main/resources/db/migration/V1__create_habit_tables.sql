CREATE TABLE IF NOT EXISTS categories (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    color VARCHAR(50),
    icon VARCHAR(100),
    CONSTRAINT uk_category_user_name UNIQUE (user_id, name)
);

CREATE TABLE IF NOT EXISTS habits (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    frequency VARCHAR(50) NOT NULL,
    schedule TEXT,
    time_of_day VARCHAR(50),
    priority INTEGER,
    color VARCHAR(50),
    icon VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    category_id UUID,
    CONSTRAINT fk_habit_category FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE TABLE IF NOT EXISTS habit_streaks (
    habit_id UUID PRIMARY KEY,
    current_streak INTEGER DEFAULT 0,
    best_streak INTEGER DEFAULT 0,
    last_completed_at TIMESTAMP,
    next_due_at TIMESTAMP,
    completion_rate DOUBLE PRECISION DEFAULT 0.0,
    CONSTRAINT fk_streak_habit FOREIGN KEY (habit_id) REFERENCES habits(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS habit_tracking_records (
    id UUID PRIMARY KEY,
    habit_id UUID NOT NULL,
    completed_at TIMESTAMP NOT NULL,
    notes TEXT,
    duration_minutes INTEGER,
    mood_rating INTEGER CHECK (mood_rating BETWEEN 1 AND 5),
    difficulty_rating INTEGER CHECK (difficulty_rating BETWEEN 1 AND 5),
    CONSTRAINT fk_record_habit FOREIGN KEY (habit_id) REFERENCES habits(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS habit_reminders (
    id UUID PRIMARY KEY,
    habit_id UUID NOT NULL,
    remind_at TIME NOT NULL,
    days_of_week VARCHAR(50),
    notification_type VARCHAR(20),
    message TEXT,
    enabled BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_reminder_habit FOREIGN KEY (habit_id) REFERENCES habits(id) ON DELETE CASCADE
);

CREATE INDEX idx_habits_user_id ON habits(user_id);
CREATE INDEX idx_habits_category_id ON habits(category_id);
CREATE INDEX idx_records_habit_id ON habit_tracking_records(habit_id);
CREATE INDEX idx_records_completed_at ON habit_tracking_records(completed_at);
CREATE INDEX idx_reminders_habit_id ON habit_reminders(habit_id);
CREATE INDEX idx_reminders_remind_at ON habit_reminders(remind_at);
