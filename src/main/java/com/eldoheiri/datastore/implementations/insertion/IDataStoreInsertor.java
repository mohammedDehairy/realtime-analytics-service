package com.eldoheiri.datastore.implementations.insertion;

import java.sql.SQLException;
import java.util.List;

public interface IDataStoreInsertor {
    <Entity> boolean insert(Entity entity) throws SQLException;

    <Entity> int insert(List<Entity> entities) throws SQLException;
}
