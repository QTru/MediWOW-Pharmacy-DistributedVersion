package core.utils.idgenerator;

import core.utils.idgenerator.implementation.GeneratedId;
import org.hibernate.FlushMode;
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
        Session hibernateSession = session.unwrap(Session.class);
        FlushMode originalFlushMode = hibernateSession.getHibernateFlushMode();
        hibernateSession.setHibernateFlushMode(FlushMode.COMMIT);

        try {
            String result = hibernateSession
                    .createNativeQuery("SELECT nextval(" + sequenceName + ")", String.class)
                    .getSingleResult();
            return prefix + String.format("%0" + numberLength + "d", Long.parseLong(result));
        } finally {
            hibernateSession.setHibernateFlushMode(originalFlushMode);
        }
    }
}