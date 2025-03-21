package com.habittracker.habit.service.impl;

import com.habittracker.habit.model.Habit;
import com.habittracker.habit.model.HabitReminder;
import com.habittracker.habit.repository.HabitRepository;
import com.habittracker.habit.repository.HabitReminderRepository;
import com.habittracker.habit.service.HabitReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class HabitReminderServiceImpl implements HabitReminderService {

    private final HabitReminderRepository reminderRepository;
    private final HabitRepository habitRepository;
    
    @Override
    public List<HabitReminder> getRemindersByHabitId(UUID habitId) {
        return reminderRepository.findByHabitId(habitId);
    }
    
    @Override
    public HabitReminder createReminder(HabitReminder reminder) {
        Habit habit = habitRepository.findById(reminder.getHabit().getId())
                .orElseThrow(() -> new EntityNotFoundException("Habit not found with id: " + 
                        reminder.getHabit().getId()));
        
        reminder.setHabit(habit);
        return reminderRepository.save(reminder);
    }
    
    @Override
    public HabitReminder updateReminder(UUID id, HabitReminder reminderDetails) {
        HabitReminder reminder = reminderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reminder not found with id: " + id));
        
        reminder.setRemindAt(reminderDetails.getRemindAt());
        reminder.setDaysOfWeek(reminderDetails.getDaysOfWeek());
        reminder.setNotificationType(reminderDetails.getNotificationType());
        reminder.setMessage(reminderDetails.getMessage());
        reminder.setEnabled(reminderDetails.getEnabled());
        
        return reminderRepository.save(reminder);
    }
    
    @Override
    public void deleteReminder(UUID id) {
        HabitReminder reminder = reminderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reminder not found with id: " + id));
        
        reminderRepository.delete(reminder);
    }
    
    @Override
    public void toggleReminderStatus(UUID id, boolean enabled) {
        HabitReminder reminder = reminderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reminder not found with id: " + id));
        
        reminder.setEnabled(enabled);
        reminderRepository.save(reminder);
    }
    
    @Override
    public List<HabitReminder> getActiveRemindersByUserId(UUID userId) {
        return reminderRepository.findActiveRemindersByUserId(userId);
    }
    
    @Override
    public List<HabitReminder> getActiveRemindersDueWithinTimeRange(LocalTime startTime, LocalTime endTime) {
        return reminderRepository.findActiveRemindersDueWithinTimeRange(startTime, endTime);
    }
    
    @Override
    public List<HabitReminder> getActiveRemindersByUserIdAndDayOfWeek(UUID userId, String dayOfWeek) {
        return reminderRepository.findActiveRemindersByUserIdAndDayOfWeek(userId, dayOfWeek);
    }
    
    @Override
    public List<HabitReminder> getActiveRemindersByUserIdAndNotificationType(UUID userId, String notificationType) {
        return reminderRepository.findActiveRemindersByUserIdAndNotificationType(userId, notificationType);
    }
}
