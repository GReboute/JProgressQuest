package com.JProgressQuest.storage;

import com.JProgressQuest.model.Character;
import java.util.Map;

/**
 * Interface for game data storage.
 * This is a Java conversion of the storage functionality from the original JavaScript code.
 */
public interface GameStorage {
    /**
     * Save a character to storage
     * @param character Character to save
     * @param callback Callback to run after saving
     */
    void saveCharacter(Character character, Runnable callback);
    
    /**
     * Load a character by name
     * @param name Character name
     * @param callback Callback with the loaded character
     */
    void loadCharacter(String name, CharacterCallback callback);
    
    /**
     * Load all characters
     * @param callback Callback with the loaded roster
     */
    void loadRoster(RosterCallback callback);
    
    /**
     * Add a character to the roster
     * @param character Character to add
     * @param callback Callback to run after adding
     */
    void addToRoster(Character character, Runnable callback);
    
    /**
     * Remove a character from storage
     * @param name Character name to remove
     */
    void removeCharacter(String name);
    
    /**
     * Callback interface for character loading
     */
    interface CharacterCallback {
        void onCharacterLoaded(Character character);
    }
    
    /**
     * Callback interface for roster loading
     */
    interface RosterCallback {
        void onRosterLoaded(Map<String, Character> roster);
    }
}
