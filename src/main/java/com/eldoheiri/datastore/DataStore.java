package com.eldoheiri.datastore;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import com.eldoheiri.datastore.factories.DefaultEntityFactory;
import com.eldoheiri.datastore.factories.EntityFactory;
import com.eldoheiri.datastore.implementations.insertion.DataStoreInsertor;
import com.eldoheiri.datastore.implementations.insertion.IDataStoreInsertor;
import com.eldoheiri.datastore.implementations.selection.DataStoreSelector;
import com.eldoheiri.datastore.implementations.selection.IDataStoreSelector;
import com.eldoheiri.datastore.sqlpredicatebuilder.basetypes.buildersinterfaces.IPredicate;
import com.eldoheiri.realtime_analytics.dataaccess.DataSource;

public class DataStore implements IDataStoreInsertor, IDataStoreSelector {

    private final EntityFactory factory;

    private final Connection dbConnection;

    public DataStore() throws SQLException {
        this(new DefaultEntityFactory(), DataSource.getConnection());
    }

    public DataStore(EntityFactory factory, Connection dbConnection) {
        Objects.requireNonNull(factory);
        Objects.requireNonNull(dbConnection);
        this.factory = factory;
        this.dbConnection = dbConnection;
    }

    @Override
    public <Entity> boolean insert(Entity entity) throws SQLException {
        Objects.requireNonNull(entity);
        return new DataStoreInsertor(dbConnection).insert(entity);
    }

    @Override
    public <Entity> int insert(List<Entity> entities) throws SQLException {
        Objects.requireNonNull(entities);
        if (entities.isEmpty()) {
            return 0;
        }
        return new DataStoreInsertor(dbConnection).insert(entities);
    }

    public <Entity> boolean update(Entity entity) {
        return true;
    }

    public <Entity> boolean delete(Entity entity) {
        return true;
    }

    @Override
    public <Entity> List<Entity> get(IPredicate predicate, Class<Entity> entityClass) throws SQLException {
        return new DataStoreSelector(factory, dbConnection).get(predicate, entityClass);
    }
}
