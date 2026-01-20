package org.example.repository;

import org.example.domain.Friendship;
import org.example.domain.validators.Validator;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Repository pentru prietenii, persistat in fisier, cu utilitare pentru verificare si operatii pe perechi.
 */
public class FriendshipFileRepository extends AbstractFileRepository<Long, Friendship> {

    private long maxId = 0;

    /**
     * Creeaza repository-ul si determina ID-ul maxim existent pentru a continua secventa.
     * @param fileName calea fisierului de prietenii
     * @param validator validatorul pentru entitatea Friendship
     */
    public FriendshipFileRepository(String fileName, Validator<Friendship> validator) {
        super(fileName, validator);
        for (Friendship f : entities.values()) {
            if (f.getID() != null && f.getID() > maxId) maxId = f.getID();
        }
    }

    @Override
    /**
     * Extrage o prietenie din lista de atribute.
     */
    public Friendship extractEntity(List<String> a) {
        Long id = Long.parseLong(a.get(0));
        Long u1 = Long.parseLong(a.get(1));
        Long u2 = Long.parseLong(a.get(2));
        long ts = Long.parseLong(a.get(3));
        return new Friendship(id, u1, u2, ts);
    }

    @Override
    /**
     * Creeaza reprezentarea text a unei prietenii pentru scriere in fisier.
     */
    protected String createEntityAsString(Friendship e) {
        return e.getID() + ";" + e.getUserId1() + ";" + e.getUserId2() + ";" + e.getCreatedAt();
    }

    /**
     * Verifica daca exista deja o prietenie intre cei doi utilizatori (ordine normalizata).
     * @param a ID utilizator A
     * @param b ID utilizator B
     * @return true daca exista, altfel false
     */
    public boolean existsBetween(Long a, Long b) {
        Long x = Math.min(a, b), y = Math.max(a, b);
        for (Friendship f : entities.values()) {
            Long fx = Math.min(f.getUserId1(), f.getUserId2());
            Long fy = Math.max(f.getUserId1(), f.getUserId2());
            if (Objects.equals(fx, x) && Objects.equals(fy, y)) return true;
        }
        return false;
    }

    /**
     * Salveaza o prietenie intre cei doi utilizatori daca nu exista deja.
     * Genereaza un ID unic si persista modificarile.
     * @param a ID utilizator A
     * @param b ID utilizator B
     * @return prietenia salvata sau null daca exista deja
     */
    public Friendship savePair(Long a, Long b) {
        Long x = Math.min(a, b), y = Math.max(a, b);
        if (existsBetween(x, y)) return null;
        Friendship f = new Friendship(++maxId, x, y, System.currentTimeMillis());
        entities.put(f.getID(), f);
        writeAllToFile();
        return f;
    }

    /**
     * Sterge prietenia dintre doi utilizatori daca exista.
     * @param a ID utilizator A
     * @param b ID utilizator B
     * @return true daca o inregistrare a fost stearsa, altfel false
     */
    public boolean deleteBetween(Long a, Long b) {
        Long x = Math.min(a, b), y = Math.max(a, b);
        Long key = null;
        for (Map.Entry<Long, Friendship> e : entities.entrySet()) {
            Friendship f = e.getValue();
            Long fx = Math.min(f.getUserId1(), f.getUserId2());
            Long fy = Math.max(f.getUserId1(), f.getUserId2());
            if (Objects.equals(fx, x) && Objects.equals(fy, y)) {
                key = e.getKey();
                break;
            }
        }
        if (key == null) return false;
        entities.remove(key);
        writeAllToFile();
        return true;
    }
}



