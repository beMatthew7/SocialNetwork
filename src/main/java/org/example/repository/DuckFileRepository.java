package org.example.repository;

import org.example.domain.Duck;
import org.example.domain.DuckType;
import org.example.domain.FlyingDuck;
import org.example.domain.SwimmingDuck;
import org.example.domain.FlyingSwimmingDuck;
import org.example.domain.validators.Validator;
import org.example.interactions.DuckCard;

import java.util.List;

/**
 * Repository pentru entitati Duck, persistat in fisier.
 */
public class DuckFileRepository extends AbstractFileRepository<Long, Duck> {


    /**
     * Creeaza repository-ul pentru Duck.
     * @param fileName calea fisierului
     * @param validator validatorul entitatii Duck
     */
    public DuckFileRepository(String fileName, Validator<Duck> validator) {
        super(fileName, validator);

    }



    @Override
    /**
     * Extrage un Duck din lista de atribute citite din fisier.
     * Format: id;username;email;password;type;speed;endurance;cardId (optional)
     */
    public Duck extractEntity(List<String> attributes) {
        Long id = Long.parseLong(attributes.get(0));
        String username = attributes.get(1);
        String email = attributes.get(2);
        String password = attributes.get(3);
        DuckType type = DuckType.valueOf(attributes.get(4));
        double speed = Double.parseDouble(attributes.get(5));
        double endurance = Double.parseDouble(attributes.get(6));



        Duck duck;
        if (type == DuckType.FLYING) {
            duck = new FlyingDuck(username, email, password, type, speed, endurance);
        } else if (type == DuckType.SWIMMING) {
            duck = new SwimmingDuck(username, email, password, type, speed, endurance);
        } else {
            duck = new FlyingSwimmingDuck(username, email, password, type, speed, endurance);
        }
        duck.setID(id);

        return duck;
    }

    @Override
    /**
     * Creeaza reprezentarea text a unui Duck pentru scriere in fisier.
     * Format: id;username;email;password;type;speed;endurance;cardId
     */
    protected String createEntityAsString(Duck entity) {
        StringBuilder sb = new StringBuilder();
        sb.append(entity.getID()).append(";");
        sb.append(entity.getUsername()).append(";");
        sb.append(entity.getEmail()).append(";");
        sb.append(entity.getPassword()).append(";");
        sb.append(entity.getType().name()).append(";");
        sb.append(entity.getSpeed()).append(";");
        sb.append(entity.getEndurance());


        if (entity.getCard() != null && entity.getCard().getID() != null) {
            sb.append(";").append(entity.getCard().getID());
        }

        return sb.toString();
    }

    @Override
    public Duck save(Duck duck) {
        Duck result = super.save(duck);
        if (result == null) {
            writeAllToFile();
        }
        return result;
    }

    @Override
    public Duck update(Duck duck) {
        Duck result = super.update(duck);
        if (result == null) {
            writeAllToFile();
        }
        return result;
    }
}