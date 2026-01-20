package org.example.repository;

import org.example.domain.DuckType;
import org.example.domain.validators.NoOpValidator;
import org.example.interactions.DuckCard;

import java.util.List;

public class CardFileRepository extends AbstractFileRepository<Long, DuckCard> {

    public CardFileRepository(String fileName) {
        super(fileName, new NoOpValidator<>());
    }

    @Override
    public DuckCard extractEntity(List<String> attributes) {
        Long id = Long.parseLong(attributes.get(0));
        String cardName = attributes.get(1);
        DuckType targetType = DuckType.valueOf(attributes.get(2));

        DuckCard card = new DuckCard(cardName, targetType);
        card.setID(id);
        return card;
    }

    @Override
    protected String createEntityAsString(DuckCard entity) {
        return entity.getID() + ";" +
                entity.getCardName() + ";" +
                entity.getTargetType().name();
    }

    @Override
    public DuckCard save(DuckCard card) {
        DuckCard result = super.save(card);
        if (result == null) {
            writeAllToFile();
        }
        return result;
    }

    @Override
    public DuckCard update(DuckCard card) {
        DuckCard result = super.update(card);
        if (result == null) {
            writeAllToFile();
        }
        return result;
    }
}