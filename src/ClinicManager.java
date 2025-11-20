import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Prosty manager klinik demonstrujący użycie HashSet i HashMap.
 * Wklej jako ClinicManager.java i uruchom.
 */
public class ClinicManager {

    // Model reprezentujący klinikę
    static class Clinic {
        private final String id;        // unikalny identyfikator
        private String name;
        private String city;
        private String specialty;

        public Clinic(String id, String name, String city, String specialty) {
            this.id = id;
            this.name = name;
            this.city = city;
            this.specialty = specialty;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getCity() { return city; }
        public String getSpecialty() { return specialty; }

        public void setName(String name) { this.name = name; }
        public void setCity(String city) { this.city = city; }
        public void setSpecialty(String specialty) { this.specialty = specialty; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Clinic)) return false;
            Clinic clinic = (Clinic) o;
            // równość na podstawie unikalnego id
            return Objects.equals(id, clinic.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public String toString() {
            return String.format("Clinic{id='%s', name='%s', city='%s', specialty='%s'}",
                    id, name, city, specialty);
        }
    }

    // Główne struktury: unikalny zbiór klinik i mapa specjalizacji -> zbiór klinik
    private final Set<Clinic> clinics = new HashSet<>();
    private final Map<String, Set<Clinic>> bySpecialty = new HashMap<>();

    // Dodaj klinikę (jeśli id jest unikalne)
    public boolean addClinic(Clinic c) {
        boolean added = clinics.add(c);
        if (!added) return false; // już istnieje
        bySpecialty.computeIfAbsent(c.getSpecialty(), k -> new HashSet<>()).add(c);
        return true;
    }

    // Usuń klinikę po id
    public boolean removeClinicById(String id) {
        Clinic found = clinics.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElse(null);
        if (found == null) return false;
        clinics.remove(found);
        Set<Clinic> set = bySpecialty.get(found.getSpecialty());
        if (set != null) {
            set.remove(found);
            if (set.isEmpty()) bySpecialty.remove(found.getSpecialty());
        }
        return true;
    }

    // Zaktualizuj specjalizację kliniki (przeniesienie w mapie)
    public boolean updateSpecialty(String id, String newSpecialty) {
        Clinic found = clinics.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElse(null);
        if (found == null) return false;
        String old = found.getSpecialty();
        if (Objects.equals(old, newSpecialty)) return true;
        // usuń z starego zbioru
        Set<Clinic> oldSet = bySpecialty.get(old);
        if (oldSet != null) {
            oldSet.remove(found);
            if (oldSet.isEmpty()) bySpecialty.remove(old);
        }
        // zaktualizuj i dodaj do nowego zbioru
        found.setSpecialty(newSpecialty);
        bySpecialty.computeIfAbsent(newSpecialty, k -> new HashSet<>()).add(found);
        return true;
    }

    // Pobierz kliniki wg specjalizacji
    public Set<Clinic> getBySpecialty(String specialty) {
        return bySpecialty.getOrDefault(specialty, Set.of());
    }

    // Wyszukaj kliniki w danym mieście
    public Set<Clinic> searchByCity(String city) {
        return clinics.stream()
                .filter(c -> c.getCity().equalsIgnoreCase(city))
                .collect(Collectors.toSet());
    }

    // Wypisz krótki raport
    public void printReport() {
        System.out.println("=== All clinics (" + clinics.size() + ") ===");
        clinics.forEach(System.out::println);
        System.out.println("\n=== Clinics by specialty ===");
        for (Map.Entry<String, Set<Clinic>> e : bySpecialty.entrySet()) {
            System.out.println(e.getKey() + " (" + e.getValue().size() + "):");
            e.getValue().forEach(c -> System.out.println("  - " + c.getName() + " [" + c.getCity() + "]"));
        }
    }

    // Prosty eksport do CSV (na konsolę)
    public void exportCsv() {
        System.out.println("\nid,name,city,specialty");
        clinics.forEach(c -> System.out.println(
                String.join(",",
                        c.getId(),
                        c.getName().replace(",", " "),
                        c.getCity().replace(",", " "),
                        c.getSpecialty().replace(",", " ")
                )
        ));
    }

    // Przykładowe dane do szybkiego uruchomienia
    private void seedSampleData() {
        addClinic(new Clinic("C001", "Klinika Alfa", "Warszawa", "Dermatologia"));
        addClinic(new Clinic("C002", "Klinika Beta", "Kraków", "Kardiologia"));
        addClinic(new Clinic("C003", "Centrum Zdrowia Gamma", "Wrocław", "Ortopedia"));
        addClinic(new Clinic("C004", "MediCare Delta", "Gdańsk", "Pediatria"));
        addClinic(new Clinic("C005", "Nova Klinika", "Warszawa", "Dermatologia"));
    }

    // Main: demonstracja użycia
    public static void main(String[] args) {
        ClinicManager manager = new ClinicManager();
        manager.seedSampleData();

        System.out.println("Initial data:");
        manager.printReport();

        System.out.println("\nDodajemy nową klinikę (unikalna):");
        Clinic newClinic = new Clinic("C006", "Zdrowie Plus", "Poznań", "Kardiologia");
        System.out.println("Added: " + manager.addClinic(newClinic));
        manager.printReport();

        System.out.println("\nPróbujemy dodać klinikę o istniejącym ID (C006) — powinno zwrócić false:");
        Clinic duplicate = new Clinic("C006", "Inna Nazwa", "Łódź", "Neurologia");
        System.out.println("Added duplicate: " + manager.addClinic(duplicate));

        System.out.println("\nWyszukanie klinik w Warszawie:");
        manager.searchByCity("Warszawa").forEach(System.out::println);

        System.out.println("\nKliniki kardiologiczne:");
        manager.getBySpecialty("Kardiologia").forEach(System.out::println);

        System.out.println("\nAktualizujemy specjalizację C002 -> 'Neurologia':");
        manager.updateSpecialty("C002", "Neurologia");
        manager.printReport();

        System.out.println("\nUsuwamy klinikę C003:");
        manager.removeClinicById("C003");
        manager.printReport();

        System.out.println("\nEksport CSV:");
        manager.exportCsv();
    }
}
