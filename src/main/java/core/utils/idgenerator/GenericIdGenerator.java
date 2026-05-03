package core.utils.idgenerator;

import core.utils.idgenerator.implementation.GeneratedId;
import org.hibernate.Session;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;
import org.hibernate.generator.EventTypeSets;
import org.hibernate.generator.GeneratorCreationContext;

import java.lang.reflect.Member;
import java.util.EnumSet;

public class GenericIdGenerator implements BeforeExecutionGenerator {
    private final String prefix;
    private final int numberLength;
    private final String sequenceName;

    public GenericIdGenerator(GeneratedId annotation, Member member, GeneratorCreationContext context) {
        this.prefix = annotation.prefix();
        this.numberLength = annotation.numberLength();
        this.sequenceName = annotation.sequenceName();
    }

    @Override
    public EnumSet<EventType> getEventTypes() {
        return EventTypeSets.INSERT_ONLY;
    }

    @Override
    public Object generate(SharedSessionContractImplementor session, Object owner, Object currentValue, EventType eventType) {
        Long next = session.unwrap(Session.class)
                .createNativeQuery("SELECT NEXT VALUE FOR " + sequenceName, Number.class)
                .getSingleResult()
                .longValue();
        return prefix + String.format("%0" + numberLength + "d", next);
    }
}