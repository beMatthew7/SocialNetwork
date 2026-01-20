package org.example.repository;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CardMembershipRepository {
    private final String fileName;
    // Map<CardID, Set<DuckIDs>>
    private Map<Long, Set<Long>> cardMembers;

    public CardMembershipRepository(String fileName) {
        this.fileName = fileName;
        this.cardMembers = new HashMap<>();
        loadMemberships();
    }

    /**
     * Incarca membrii din fisier
     */
    private void loadMemberships() {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(";");
                if (parts.length < 2) continue;

                try {
                    Long cardId = Long.parseLong(parts[0].trim());
                    Long duckId = Long.parseLong(parts[1].trim());

                    cardMembers.computeIfAbsent(cardId, k -> new HashSet<>()).add(duckId);
                } catch (NumberFormatException e) {
                    // Ignora linii invalide
                }
            }
        } catch (FileNotFoundException e) {
            // Fisierul va fi creat la primul save
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returneaza ID-urile membrilor unui card
     */
    public Set<Long> getMemberIds(Long cardId) {
        return cardMembers.getOrDefault(cardId, new HashSet<>());
    }

    /**
     * --- METODA NOUA ---
     * Gaseste ID-ul cardului in care se afla o rata.
     * Cauta in map-ul din memorie.
     */
    public Long getCardIdForDuck(Long duckId) {
        for (Map.Entry<Long, Set<Long>> entry : cardMembers.entrySet()) {
            // Verificam daca setul de membri ai acestui card contine rata cautata
            if (entry.getValue().contains(duckId)) {
                return entry.getKey(); // Returnam ID-ul cardului
            }
        }
        return null; // Rata nu e in niciun card
    }

    /**
     * Salveaza o membru in fisier
     */
    public void saveMembership(Long cardId, Long duckId) {
        cardMembers.computeIfAbsent(cardId, k -> new HashSet<>()).add(duckId);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            writer.write(cardId + ";" + duckId);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sterge o membru din fisier
     */
    public void deleteMembership(Long cardId, Long duckId) {
        Set<Long> members = cardMembers.get(cardId);
        if (members != null) {
            members.remove(duckId);
        }

        // Rescrie tot fisierul
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, false))) {
            for (Map.Entry<Long, Set<Long>> entry : cardMembers.entrySet()) {
                for (Long duckIdValue : entry.getValue()) {
                    writer.write(entry.getKey() + ";" + duckIdValue);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Rescrie tot fisierul cu membrii actuali
     */
    public void saveAll() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, false))) {
            for (Map.Entry<Long, Set<Long>> entry : cardMembers.entrySet()) {
                for (Long duckId : entry.getValue()) {
                    writer.write(entry.getKey() + ";" + duckId);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}